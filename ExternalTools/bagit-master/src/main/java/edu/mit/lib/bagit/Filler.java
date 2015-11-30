/**
 * Copyright 2013 MIT Libraries
 * Licensed under: http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.mit.lib.bagit;

import static edu.mit.lib.bagit.Bag.BAGIT_VSN;
import static edu.mit.lib.bagit.Bag.CS_ALGO;
import static edu.mit.lib.bagit.Bag.DATA_DIR;
import static edu.mit.lib.bagit.Bag.DATA_PATH;
import static edu.mit.lib.bagit.Bag.DECL_FILE;
import static edu.mit.lib.bagit.Bag.DFLT_FMT;
import static edu.mit.lib.bagit.Bag.ENCODING;
import static edu.mit.lib.bagit.Bag.LIB_VSN;
import static edu.mit.lib.bagit.Bag.MANIF_FILE;
import static edu.mit.lib.bagit.Bag.META_FILE;
import static edu.mit.lib.bagit.Bag.REF_FILE;
import static edu.mit.lib.bagit.Bag.SPACER;
import static edu.mit.lib.bagit.Bag.TAGMANIF_FILE;
import static edu.mit.lib.bagit.Bag.scaledSize;
import static edu.mit.lib.bagit.Bag.toHex;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import edu.mit.lib.bagit.Bag.MetadataName;

/**
 * Filler is a builder class used to construct bags conformant to LC Bagit spec
 * - version 0.97. Filler objects serialize themselves to either a loose
 * directory, a compressed archive file (supported formats zip or tgz) or a
 * stream, abiding by the serialization recommendations of the specification.
 * 
 * See README for sample invocations and API description.
 * 
 * @author richardrodgers
 */

public class Filler {

	// directory root of bag
	private final File base;
	// checksum algorithm
	private final String csAlg;
	// automatic metadata generation flag
	private boolean autogen = true;
	// total payload size
	private long payloadSize = 0L;
	// number of payload files
	private int payloadCount = 0;
	// manifest writers
	private final FlatWriter tagWriter;
	private final FlatWriter manWriter;
	// optional flat writers
	private final Map<String, FlatWriter> writers;
	// optional bag streams
	private final Map<String, BagOutputStream> streams;
	// has bag been built?
	private boolean built;

	/**
	 * Returns a new Filler (bag builder) instance using temporary directory to
	 * hold bag and default checksum algorithm (MD5).
	 * 
	 * @throws IOException
	 * 
	 */
	public Filler() throws IOException {
		this(null, null);
	}

	/**
	 * Returns a new Filler (bag builder) instance using passed directory to
	 * hold bag and default checksum algorithm (MD5).
	 * 
	 * @param base
	 *            the base directory in which to construct the bag
	 * @throws IOException
	 */
	public Filler(File base) throws IOException {
		this(base, null);
	}

	/**
	 * Returns a new filler (bag builder) instances using passed directory and
	 * checksum algorithm.
	 * 
	 * @param base
	 *            directory for bag - if null, create temporary directory
	 * @param csAlgorithm
	 *            checksum algorithm string - if null use default
	 * @throws IOException
	 */
	public Filler(File base, String csAlgorithm) throws IOException {
		this.base = (base != null) ? base : Files.createTempDirectory("bag")
				.toFile();
		csAlg = (csAlgorithm != null) ? csAlgorithm : CS_ALGO;
		File dFile = bagFile(DATA_DIR);
		if (!dFile.exists()) {
			dFile.mkdirs();
		}
		// prepare manifest writers
		String sfx = csAlg.toLowerCase() + ".txt";
		tagWriter = new FlatWriter(bagFile(TAGMANIF_FILE + sfx), null, null);
		manWriter = new FlatWriter(bagFile(MANIF_FILE + sfx), null, tagWriter);
		writers = new HashMap<>();
		streams = new HashMap<>();
	}

	private void buildBag() throws IOException {
		if (built)
			return;
		// if auto-generating metadata, do so
		if (autogen) {
			metadata(MetadataName.BAGGING_DATE, new SimpleDateFormat(
					"yyyy-MM-dd").format(new Date()));
			metadata(MetadataName.BAG_SIZE, scaledSize(payloadSize, 0));
			metadata(MetadataName.PAYLOAD_OXNUM, String.valueOf(payloadSize)
					+ "." + String.valueOf(payloadCount));
			metadata("Bag-Software-Agent", "MIT BagIt Lib v:" + LIB_VSN);
		}
		// close all optional writers' tag files
		Iterator<String> wIter = writers.keySet().iterator();
		while (wIter.hasNext()) {
			getWriter(wIter.next()).close();
		}
		// close all optional output streams
		Iterator<String> sIter = streams.keySet().iterator();
		while (sIter.hasNext()) {
			getStream(null, sIter.next()).close();
		}
		// close the manifest file
		manWriter.close();
		// write out bagit declaration file
		FlatWriter fwriter = new FlatWriter(bagFile(DECL_FILE), null, tagWriter);
		fwriter.writeLine("BagIt-Version: " + BAGIT_VSN);
		fwriter.writeLine("Tag-File-Character-Encoding: " + ENCODING);
		fwriter.close();
		// close tag manifest file of previous tag files
		tagWriter.close();
		built = true;
	}

	/**
	 * Disables the automatic generation of metadata. Normally generated:
	 * Bagging-Date, Bag-Size, Payload-Oxnum, Bag-Software-Agent
	 * 
	 * @return filler
	 */
	public Filler noAutoGen() {
		autogen = false;
		return this;
	}

	/**
	 * Adds a file to the payload at the root of the data directory tree -
	 * convenience method when no payload hierarchy needed.
	 * 
	 * @param file
	 *            the file to add to the payload
	 * @return Filler this Filler
	 * @throws IOException
	 */
	public Filler payload(File file) throws IOException {
		return payload(file.getName(), file);
	}

	/**
	 * Adds a file to the payload at the specified relative path from the root
	 * of the data directory tree.
	 * 
	 * @param relPath
	 *            the relative path of the file
	 * @param file
	 *            the file to add to the payload
	 * @return Filler this Filler
	 * @throws IOException
	 */
	public Filler payload(String relPath, File file) throws IOException {
		return payload(relPath, new FileInputStream(file));
	}

	/**
	 * Adds the contents of the passed stream to the payload at the specified
	 * relative path in the data directory tree.
	 * 
	 * @param relPath
	 *            the relative path of the file
	 * @param is
	 *            the input stream to read.
	 * @return Filler this Filler
	 * @throws IOException
	 */
	public Filler payload(String relPath, InputStream is) throws IOException {
		if (dataFile(relPath).exists()) {

			// TODO: overwrite? or merge?

			// throw new
			// IllegalStateException("Payload file already exists at: "
			// + relPath);
		}
		// wrap stream in digest stream
		try (DigestInputStream dis = new DigestInputStream(is,
				MessageDigest.getInstance(csAlg))) {
			payloadSize += Files.copy(dis, dataFile(relPath).toPath());
			payloadCount++;
			// record checksum
			manWriter.writeLine(toHex(dis.getMessageDigest().digest()) + " "
					+ DATA_PATH + relPath);
		} catch (NoSuchAlgorithmException nsaE) {
			throw new IOException("no algorithm: " + csAlg);
		}
		return this;
	}

	/**
	 * Adds a reference URL to payload contents - ie. to the fetch.txt file.
	 * 
	 * @param relPath
	 *            the relative path of the resource
	 * @param size
	 *            the expected size in bytes of the resource
	 * @param url
	 *            the URL of the resource
	 * @return Filler this Filler
	 * @throws IOException
	 */
	public Filler payloadRef(String relPath, long size, String url)
			throws IOException {
		FlatWriter refWriter = getWriter(REF_FILE);
		String sizeStr = (size > 0L) ? Long.toString(size) : "-";
		refWriter.writeLine(url + " " + sizeStr + " " + DATA_PATH + relPath);
		payloadSize += size;
		payloadCount++;
		return this;
	}

	/**
	 * Obtains an output stream to a payload file at a relative path.
	 * 
	 * @param relPath
	 *            the relative path to the payload file
	 * @return stream an output stream to payload file
	 * @throws IOException
	 */
	public OutputStream payloadStream(String relPath) throws IOException {
		if (dataFile(relPath).exists()) {
			throw new IllegalStateException("Payload file already exists at: "
					+ relPath);
		}
		return getStream(dataFile(relPath), relPath);
	}

	/**
	 * Adds a tag (metadata) file at the specified relative path from the root
	 * of the bag directory tree.
	 * 
	 * @param relPath
	 *            the relative path of the file
	 * @param file
	 *            the tag file to add
	 * @return Filler this Filler
	 * @throws IOException
	 */
	public Filler tag(String relPath, File file) throws IOException {
		return tag(relPath, new FileInputStream(file));
	}

	/**
	 * Adds the contents of the passed stream to a tag (metadata) file at the
	 * specified relative path in the bag directory tree.
	 * 
	 * @param relPath
	 *            the relative path of the file
	 * @param is
	 *            the input stream to read.
	 * @return Filler this Filler
	 * @throws IOException
	 */
	public Filler tag(String relPath, InputStream is) throws IOException {
		// make sure tag files not written to payload directory
		if (relPath.startsWith(DATA_PATH)) {
			throw new IOException("Tag files not allowed in paylod directory");
		}
		if (bagFile(relPath).exists()) {
			throw new IllegalStateException("Tag file already exists at: "
					+ relPath);
		}
		// wrap stream in digest stream
		try (DigestInputStream dis = new DigestInputStream(is,
				MessageDigest.getInstance(csAlg))) {
			Files.copy(dis, tagFile(relPath).toPath());
			// record checksum
			tagWriter.writeLine(toHex(dis.getMessageDigest().digest()) + " "
					+ relPath);
		} catch (NoSuchAlgorithmException nsaE) {
			throw new IOException("no algorithm: " + csAlg);
		}
		return this;
	}

	/**
	 * Obtains an output stream to the tag file at a relative path.
	 * 
	 * @param relPath
	 *            the relative path to the tag file
	 * @return stream an output stream to the tag file
	 * @throws IOException
	 */
	public OutputStream tagStream(String relPath) throws IOException {
		if (tagFile(relPath).exists()) {
			throw new IllegalStateException("Tag file already exists at: "
					+ relPath);
		}
		return getStream(tagFile(relPath), relPath);
	}

	/**
	 * Adds a reserved metadata property to the standard file (bag-info.txt)
	 * 
	 * @param name
	 *            the property name
	 * @param value
	 *            the property value
	 * @return filler
	 * @throws IOException
	 */
	public Filler metadata(MetadataName name, String value) throws IOException {
		return property(META_FILE, name.getName(), value);
	}

	/**
	 * Adds a metadata property to the standard file (bag-info.txt)
	 * 
	 * @param name
	 *            the property name
	 * @param value
	 *            the property value
	 * @return filler
	 * @throws IOException
	 */
	public Filler metadata(String name, String value) throws IOException {
		return property(META_FILE, name, value);
	}

	/**
	 * Adds a property to the passed property file. Typically used for metadata
	 * properties in tag files.
	 * 
	 * @param relPath
	 *            the bag-relative path to the property file
	 * @param name
	 *            the property name
	 * @param value
	 *            the property value
	 * @return filler
	 * @throws IOException
	 */
	public Filler property(String relPath, String name, String value)
			throws IOException {
		FlatWriter writer = getWriter(relPath);
		writer.writeProperty(name, value);
		return this;
	}

	private File dataFile(String name) {
		// all user-defined files live in payload area - ie. under 'data'
		File dataFile = new File(bagFile(DATA_DIR), name);
		// create needed dirs
		File parentFile = dataFile.getParentFile();
		if (!parentFile.isDirectory()) {
			parentFile.mkdirs();
		}
		return dataFile;
	}

	private File tagFile(String name) {
		// all user-defined tag files live anywhere in the bag
		File tagFile = bagFile(name);
		// create needed dirs
		File parentFile = tagFile.getParentFile();
		if (!parentFile.isDirectory()) {
			parentFile.mkdirs();
		}
		return tagFile;
	}

	private File bagFile(String name) {
		return new File(base, name);
	}

	private FlatWriter getWriter(String name) throws IOException {
		FlatWriter writer = writers.get(name);
		if (writer == null) {
			writer = new FlatWriter(bagFile(name), null, tagWriter);
			writers.put(name, writer);
		}
		return writer;
	}

	private BagOutputStream getStream(File file, String name)
			throws IOException {
		BagOutputStream stream = streams.get(name);
		if (stream == null) {
			stream = new BagOutputStream(file, name, tagWriter);
			streams.put(name, stream);
		}
		return stream;
	}

	class FlatWriter extends BagOutputStream {

		private FlatWriter(File file, String brPath, FlatWriter tailWriter)
				throws IOException {
			super(file, brPath, tailWriter);
		}

		public void writeProperty(String key, String value) throws IOException {
			String prop = key + ": " + value;
			int offset = 0;
			while (offset < prop.length()) {
				int end = Math.min(prop.length() - offset, 80);
				if (offset > 0) {
					write(SPACER.getBytes(ENCODING));
				}
				writeLine(prop.substring(offset, offset + end));
				offset += end;
			}
		}

		public void writeLine(String line) throws IOException {
			byte[] bytes = (line + "\n").getBytes(ENCODING);
			write(bytes);
		}
	}

	// wraps output stream in digester, and records results with tail writer
	class BagOutputStream extends OutputStream {

		private final String relPath;
		private final OutputStream out;
		private final DigestOutputStream dout;
		private final FlatWriter tailWriter;

		private BagOutputStream(File file, String relPath, FlatWriter tailWriter)
				throws IOException {
			try {
				out = new FileOutputStream(file);
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

	/**
	 * Returns backing bag directory file.
	 * 
	 * @return dir the bag directory
	 * @throws IOException
	 */
	public File toDirectory() throws IOException {
		buildBag();
		return base;
	}

	/**
	 * Returns bag serialized as an archive file using default packaging (zip
	 * archive).
	 * 
	 * @return file the bag archive package
	 * @throws IOException
	 */
	public File toPackage() throws IOException {
		return toPackage(DFLT_FMT);
	}

	/**
	 * Returns bag serialized as an archive file using passed packaging format.
	 * Supported formats: 'zip' - zip archive, 'tgz' - gzip compressed tar
	 * archive
	 * 
	 * @param format
	 *            the package format ('zip', or 'tgz')
	 * @return file the bag archive package
	 * @throws IOException
	 */
	public File toPackage(String format) throws IOException {
		return deflate(format);
	}

	/**
	 * Returns bag serialized as an IO stream using default packaging (zip
	 * archive).
	 * 
	 * @return file the bag archive package
	 * @throws IOException
	 */
	public InputStream toStream() throws IOException {
		return toStream(DFLT_FMT);
	}

	/**
	 * Returns bag serialized as an IO stream using passed packaging format.
	 * Supported formats: 'zip' - zip archive, 'tgz' - gzip compressed tar
	 * archive
	 * 
	 * @param format
	 *            the package format ('zip', or 'tgz')
	 * @return file the bag archive package
	 * @throws IOException
	 */
	public InputStream toStream(String format) throws IOException {
		return new FileInputStream(deflate(format));
	}

	private void empty() throws IOException {
		deleteDir(base);
		base.delete();
	}

	private void deleteDir(File dirFile) {
		for (File file : dirFile.listFiles()) {
			if (file.isDirectory()) {
				deleteDir(file);
			}
			file.delete();
		}
	}

	private File deflate(String format) throws IOException {
		// deflate this bag inplace (in current directory) using given packaging
		// format
		buildBag();
		File pkgFile = deflate(base.getParent(), format);
		// remove base
		empty();
		return pkgFile;
	}

	private File deflate(String destDir, String format) throws IOException {
		File defFile = new File(destDir, base.getName() + "." + format);
		deflate(new FileOutputStream(defFile), format);
		return defFile;
	}

	private void deflate(OutputStream out, String format) throws IOException {
		switch (format) {
		case "zip":
			ZipOutputStream zout = new ZipOutputStream(
					new BufferedOutputStream(out));
			fillZip(base, base.getName(), zout);
			zout.close();
			break;
		case "tgz":
			TarArchiveOutputStream tout = new TarArchiveOutputStream(
					new BufferedOutputStream(
							new GzipCompressorOutputStream(out)));
			fillArchive(base, base.getName(), tout);
			tout.close();
			break;
		default:
			throw new IOException("Unsupported package format: " + format);
		}
	}

	private void fillArchive(File dirFile, String relBase,
			ArchiveOutputStream out) throws IOException {
		for (File file : dirFile.listFiles()) {
			String relPath = relBase + File.separator + file.getName();
			if (file.isDirectory()) {
				fillArchive(file, relPath, out);
			} else {
				TarArchiveEntry entry = new TarArchiveEntry(relPath);
				entry.setSize(file.length());
				entry.setModTime(0L);
				out.putArchiveEntry(entry);
				Files.copy(file.toPath(), out);
				out.closeArchiveEntry();
			}
		}
	}

	private void fillZip(File dirFile, String relBase, ZipOutputStream zout)
			throws IOException {
		for (File file : dirFile.listFiles()) {
			String relPath = relBase + File.separator + file.getName();
			if (file.isDirectory()) {
				fillZip(file, relPath, zout);
			} else {
				ZipEntry entry = new ZipEntry(relPath);
				entry.setTime(0L);
				zout.putNextEntry(entry);
				Files.copy(file.toPath(), zout);
				zout.closeEntry();
			}
		}
	}
}
