/**
 * Copyright 2013 MIT Libraries
 * Licensed under: http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.mit.lib.bagit;

import static edu.mit.lib.bagit.Bag.DATA_DIR;
import static edu.mit.lib.bagit.Bag.DFLT_FMT;
import static edu.mit.lib.bagit.Bag.ENCODING;
import static edu.mit.lib.bagit.Bag.MANIF_FILE;
import static edu.mit.lib.bagit.Bag.REF_FILE;
import static edu.mit.lib.bagit.Bag.TAGMANIF_FILE;
import static edu.mit.lib.bagit.Bag.TGZIP_FMT;
import static edu.mit.lib.bagit.Bag.toHex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

/**
 * Loader is a Bag builder class that interprets bag serializations and other
 * manfestations as Bags. It also exposes methods allowing clients to fill bag
 * 'holes' (fetch.txt contents), and updates the bag to reflect this.
 * 
 * See README for sample invocations and API description.
 * 
 * @author richardrodgers
 */
public class Loader {
	// base directory of bag
	private File base;
	// checksum algorithm used in bag
	private String csAlg;
	// map of unresolved fetch.txt files
	private final ConcurrentMap<String, String> payloadRefMap = new ConcurrentHashMap<>();
	// manifest writer
	private LoaderWriter manWriter;

	/**
	 * Returns a new Loader (bag loader) instance using passed file (loose
	 * directory or archive file)
	 * 
	 * @param file
	 *            the base directory or archive file from which to extract the
	 *            bag
	 * @throws IOException
	 */
	public Loader(File file) throws IOException {
		if (file == null || !file.exists()) {
			throw new IOException("Missing or nonexistent bag file");
		}
		// is it an archive file? If so, inflate into bag
		String baseName = file.getName();
		int sfxIdx = baseName.lastIndexOf(".");
		String suffix = (sfxIdx != -1) ? baseName.substring(sfxIdx + 1) : null;
		if (!file.isDirectory() && suffix != null
				&& (suffix.equals(DFLT_FMT) || suffix.equals(TGZIP_FMT))) {
			String dirName = baseName.substring(0, sfxIdx);
			base = new File(file.getParent(), dirName);
			File dFile = new File(base, DATA_DIR);
			dFile.mkdirs();
			inflate(new FileInputStream(file), suffix);
			// remove archive original
			file.delete();
		} else {
			base = file;
		}
	}

	/**
	 * Returns a new Loader (bag loader) instance using passed I/O stream and
	 * format with bag in a temporary directory location
	 * 
	 * @param in
	 *            the input stream containing the serialized bag
	 * @param format
	 *            the expected serialization format
	 * @throws IOException
	 */
	public Loader(InputStream in, String format) throws IOException {
		this(null, in, format);
	}

	/**
	 * Returns a new Loader (bag loader) instance using passed I/O stream and
	 * format with bag in the passed directory location
	 * 
	 * @param base
	 *            the base directory or archive file into which to extract the
	 *            bag
	 * @param in
	 *            the input stream containing the serialized bag
	 * @param format
	 *            the expected serialization format
	 * @throws IOException
	 */
	public Loader(File base, InputStream in, String format) throws IOException {
		this.base = (base != null) ? base : Files.createTempDirectory("bag")
				.toFile();
		inflate(in, format);
	}

	/**
	 * Returns the checksum algortihm used in bag manifests.
	 * 
	 * @return algorithm the checksum algorithm
	 */
	public String csAlgorithm() {
		if (csAlg == null) {
			csAlg = Bag.csAlgorithm(base);
		}
		return csAlg;
	}

	/**
	 * Returns sealed Bag from Loader. Sealed Bags cannot be serialized.
	 * 
	 * @return bag the loaded sealed Bag instance
	 * @throws IOException
	 */
	public Bag seal() throws IOException {
		finish();
		return new Bag(this.base, true);
	}

	/**
	 * Returns Bag from Loader
	 * 
	 * @return bag the loaded Bag instance
	 * @throws IOException
	 */
	public Bag load() throws IOException {
		finish();
		return new Bag(this.base, false);
	}

	private void finish() throws IOException {
		// if manWriter is non-null, some payload files were fetched.
		if (manWriter != null) {
			manWriter.close();
			// Update fetch.txt - remove if all holes plugged, else filter
			File refFile = bagFile(REF_FILE);
			List<String> refLines = bufferFile(refFile);
			if (payloadRefMap.size() > 0) {
				// now reconstruct fetch.txt filtering out those resolved
				FileOutputStream refOut = new FileOutputStream(refFile);
				for (String refline : refLines) {
					String[] parts = refline.split(" ");
					if (payloadRefMap.containsKey(parts[2])) {
						refOut.write(refline.getBytes(ENCODING));
					}
				}
				refOut.close();
			}
			// update tagmanifest with new manifest checksum, fetch stuff
			String sfx = csAlgorithm() + ".txt";
			File tagManFile = bagFile(TAGMANIF_FILE + sfx);
			List<String> tmLines = bufferFile(tagManFile);
			// now recompute manifest checksum
			String manCS = checksum(bagFile(MANIF_FILE + sfx), csAlgorithm());
			// likewise fetch.txt if it's still around
			String fetchCS = refFile.exists() ? checksum(bagFile(MANIF_FILE
					+ sfx), csAlgorithm()) : null;
			// recreate tagmanifest with new checksums
			FileOutputStream tagManOut = new FileOutputStream(tagManFile);
			for (String tline : tmLines) {
				String[] parts = tline.split(" ");
				if (parts[1].startsWith(MANIF_FILE)) {
					tagManOut.write((manCS + " " + MANIF_FILE + sfx + "\n")
							.getBytes(ENCODING));
				} else if (parts[1].startsWith(REF_FILE)) {
					if (fetchCS != null) {
						tagManOut.write((fetchCS + " " + REF_FILE + sfx + "\n")
								.getBytes(ENCODING));
					}
				} else {
					tagManOut.write(tline.getBytes(ENCODING));
				}
			}
			tagManOut.close();
		}
	}

	private List<String> bufferFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		List<String> lines = new ArrayList<String>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			lines.add(line + "\n");
		}
		reader.close();
		file.delete();
		return lines;
	}

	/**
	 * Returns a map of payload files to fetch URLs (ie contents of fetch.txt).
	 * 
	 * @return refMap the map of payload files to fetch URLs
	 * @throws IOException
	 */
	public Map<String, String> payloadRefs() throws IOException {
		File refFile = bagFile(REF_FILE);
		if (payloadRefMap.isEmpty() && refFile.exists()) {
			// load initial data
			payloadRefMap.putAll(Bag.payloadRefs(refFile));
		}
		return payloadRefMap;
	}

	/**
	 * Resolves a payload reference with passed stream content.
	 * 
	 * @param relPath
	 *            the bag-relative path to the payload file
	 * @param is
	 *            the content input stream for payload
	 * @throws IOException
	 */
	public void resolveRef(String relPath, InputStream is) throws IOException {
		// various checks - is ref known?
		if (!payloadRefMap.containsKey(relPath)) {
			throw new IOException("Unknown payload reference: " + relPath);
		}
		if (bagFile(relPath).exists()) {
			throw new IllegalStateException("Payload file already exists at: "
					+ relPath);
		}
		// wrap stream in digest stream
		try (DigestInputStream dis = new DigestInputStream(is,
				MessageDigest.getInstance(csAlgorithm()))) {
			Files.copy(dis, bagFile(relPath).toPath());
			// record checksum
			manifestWriter().writeLine(
					toHex(dis.getMessageDigest().digest()) + " " + relPath);
			// remove from map
			payloadRefMap.remove(relPath);
		} catch (NoSuchAlgorithmException nsaE) {
			throw new IOException("no algorithm: " + csAlg);
		}
	}

	private File bagFile(String name) {
		return new File(base, name);
	}

	// private File dataFile(String name) {
	// // all user-defined files live in payload area - ie. under 'data'
	// File dataFile = new File(bagFile(DATA_DIR), name);
	// // create needed dirs
	// File parentFile = dataFile.getParentFile();
	// if (!parentFile.isDirectory()) {
	// parentFile.mkdirs();
	// }
	// return dataFile;
	// }

	// lazy initialization of manifest writer
	private LoaderWriter manifestWriter() throws IOException {
		if (manWriter == null) {
			String sfx = csAlgorithm().toLowerCase() + ".txt";
			manWriter = new LoaderWriter(bagFile(MANIF_FILE + sfx), null, true,
					null);
		}
		return manWriter;
	}

	class LoaderWriter extends LoaderOutputStream {

		private LoaderWriter(File file, String brPath, boolean append,
				LoaderWriter tailWriter) throws IOException {
			super(file, brPath, append, tailWriter);
		}

		public void writeLine(String line) throws IOException {
			write((line + "\n").getBytes(ENCODING));
		}
	}

	// wraps output stream in digester, and records results with tail writer
	class LoaderOutputStream extends OutputStream {

		private final String relPath;
		private final OutputStream out;
		private final DigestOutputStream dout;
		private final LoaderWriter tailWriter;

		private LoaderOutputStream(File file, String relPath, boolean append,
				LoaderWriter tailWriter) throws IOException {
			try {
				out = new FileOutputStream(file, append);
				dout = new DigestOutputStream(out,
						MessageDigest.getInstance(csAlg));
				this.relPath = (relPath != null) ? relPath : file.getName();
				this.tailWriter = tailWriter;
			} catch (NoSuchAlgorithmException nsae) {
				throw new IOException("no such algorithm: " + csAlg);
			}
		}

		@Override
		public void write(int b) throws IOException {
			dout.write(b);
		}

		@Override
		public void close() throws IOException {
			dout.flush();
			out.close();
			if (tailWriter != null) {
				tailWriter.writeLine(toHex(dout.getMessageDigest().digest())
						+ " " + relPath);
			}
		}
	}

	// inflate compressesd archive in base directory
	private void inflate(InputStream in, String fmt) throws IOException {
		switch (fmt) {
		case "zip":
			ZipInputStream zin = new ZipInputStream(in);
			ZipEntry entry = null;
			while ((entry = zin.getNextEntry()) != null) {
				File outFile = new File(base.getParent(), entry.getName());
				outFile.getParentFile().mkdirs();
				Files.copy(zin, outFile.toPath());
			}
			zin.close();
			break;
		case "tgz":
			TarArchiveInputStream tin = new TarArchiveInputStream(
					new GzipCompressorInputStream(in));
			TarArchiveEntry tentry = null;
			while ((tentry = tin.getNextTarEntry()) != null) {
				File outFile = new File(base.getParent(), tentry.getName());
				outFile.getParentFile().mkdirs();
				Files.copy(tin, outFile.toPath());
			}
			tin.close();
			break;
		default:
			throw new IOException("Unsupported archive format: " + fmt);
		}
	}

	private String checksum(File file, String csAlg) throws IOException {
		byte[] buf = new byte[2048];
		int num = 0;
		// wrap stream in digest stream
		try (FileInputStream is = new FileInputStream(file);
				DigestInputStream dis = new DigestInputStream(is,
						MessageDigest.getInstance(csAlg))) {
			while (num != -1) {
				num = dis.read(buf);
			}
			return toHex(dis.getMessageDigest().digest());
		} catch (NoSuchAlgorithmException nsaE) {
			throw new IOException("no algorithm: " + csAlg);
		}
	}

}
