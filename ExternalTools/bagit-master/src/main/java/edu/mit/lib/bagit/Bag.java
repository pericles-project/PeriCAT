/**
 * Copyright 2013 MIT Libraries
 * Licensed under: http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.mit.lib.bagit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bag represents a rudimentary bag conformant to LC Bagit spec - version 0.97.
 * A Bag is a directory with contents and a little structured metadata in it.
 * Although they don't have to be, these bags are 'wormy' - meaning that they
 * can be written only once (have no update semantics), and can be 'holey' -
 * meaning they can contain content by reference as well as inclusion.
 * 
 * The bag also can serialize itself to a compressed archive file (supported
 * formats zip or tgz) or be deserialized from same or a stream, abiding by the
 * serialization recommendations of the specification.
 * 
 * See README for sample invocations and API description.
 * 
 * @author richardrodgers
 */

public class Bag {
	// coding constants
	static final String ENCODING = "UTF-8";
	static final String CS_ALGO = "MD5";
	static final String BAGIT_VSN = "0.97";
	static final String LIB_VSN = "0.1";
	static final String DFLT_FMT = "zip";
	static final String TGZIP_FMT = "tgz";
	static final String SPACER = "  ";
	// mandated file and directory names
	static final String MANIF_FILE = "manifest-";
	static final String TAGMANIF_FILE = "tagmanifest-";
	static final String DECL_FILE = "bagit.txt";
	static final String META_FILE = "bag-info.txt";
	static final String REF_FILE = "fetch.txt";
	static final String DATA_DIR = "data";
	static final String DATA_PATH = DATA_DIR + "/";

	/**
	 * Reserved metadata property names
	 */
	public enum MetadataName {
		SOURCE_ORG("Source-Organization"), ORG_ADDR("Organization-Address"), CONTACT_NAME(
				"Contact-Name"), CONTACT_PHONE("Contact-Phone"), CONTACT_EMAIL(
				"Contact-Email"), EXTERNAL_DESC("External-Description"), EXTERNAL_ID(
				"External-Identifier"), BAGGING_DATE("Bagging-Date"), BAG_SIZE(
				"Bag-Size"), PAYLOAD_OXNUM("Payload-Oxnum"), BAG_GROUP_ID(
				"Bag-Group-Identifier"), BAG_COUNT("Bag-Count"), INTERNAL_SENDER_ID(
				"Internal-Sender-Identifier"), INTERNAL_SENDER_DESC(
				"Internal-Sender-Description");

		private String mdName;

		private MetadataName(String name) {
			mdName = name;
		}

		public String getName() {
			return mdName;
		}
	}

	// directory root of bag
	private final File baseDir;

	// allow serialization, etc of bag
	private final boolean sealed;

	// metadata cache
	private final Map<String, Map<String, List<String>>> mdCache = new HashMap<>();

	/**
	 * Constructor - creates a new bag from a Loader
	 * 
	 * @throws IOException
	 */
	Bag(File baseDir, boolean sealed) throws IOException {
		this.baseDir = baseDir;
		this.sealed = sealed;
	}

	/**
	 * Returns the specification version of the bag.
	 * 
	 * @return version the BagIt spec version string
	 */
	public static String bagItVersion() {
		return BAGIT_VSN;
	}

	/**
	 * Returns the software version of the library.
	 * 
	 * @return version the software version string
	 */
	public static String libVersion() {
		return LIB_VSN;
	}

	/**
	 * Returns the name of the bag.
	 * 
	 * @return name the name of the bag
	 */
	public String bagName() {
		return baseDir.getName();
	}

	/**
	 * Returns the checksum algortihm used in bag manifests.
	 * 
	 * @return algorithm the checksum algorithm
	 */
	public String csAlgorithm() {
		return csAlgorithm(baseDir);
	}

	/**
	 * Returns whether the bag is complete.
	 * 
	 * @return complete true if bag is complete
	 * @throws IOException
	 */
	public boolean isComplete() throws IOException {
		if (bagFile(REF_FILE).exists()) {
			System.err.println("no fetch.txt?");
			return false;
		}
		if (!(bagFile(DECL_FILE).exists() && bagFile(DATA_DIR).isDirectory())) {
			System.err.println("mandatory files present?");
			return false;
		}
		// payload files map?
		Map<String, String> payloads = payloadManifest();
		if (fileCount(bagFile(DATA_DIR)) != payloads.size()) {
			System.err
					.println("# payload files and # manifest entries must agree");
			return false;
		}
		for (String path : payloads.keySet()) {
			if (path.startsWith(DATA_DIR) && !bagFile(path).exists()) {
				System.err.println("files themselves must match also");
				return false;
			}
		}
		// same drill for tag files
		Map<String, String> tags = tagManifest();
		// # tag files and # manifest entries must agree
		// tag files consist of any top-level files except:
		// tagmanifest itself, and the payload directory.
		File[] topTags = baseDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				String name = file.getName();
				return !(name.startsWith(TAGMANIF_FILE) || name
						.startsWith(DATA_DIR));
			}
		});
		int tagCount = 0;
		for (File tag : topTags) {
			if (tag.isDirectory())
				tagCount += fileCount(tag);
			else
				tagCount++;
		}
		if (tagCount != tags.size())
			return false;
		// files themselves must match also
		for (String path : tags.keySet()) {
			if (!bagFile(path).exists())
				return false;
		}
		return true;
	}

	/**
	 * Returns whether the bag is sealed.
	 * 
	 * @return sealed true if bag is sealed
	 */
	public boolean isSealed() {
		return sealed;
	}

	/**
	 * Returns whether the bag is valid.
	 * 
	 * @return valid true if bag validates
	 * @throws IOException
	 */
	public boolean isValid() throws IOException {
		if (!isComplete())
			return false;
		// recompute all checksums and compare against manifest values
		Map<String, String> payloads = payloadManifest();
		for (String relPath : payloads.keySet()) {
			String cutPath = relPath.substring(DATA_PATH.length());
			if (!validateFile(payloadStream(cutPath), payloads.get(relPath),
					csAlgorithm()))
				return false;
		}
		// same for tag files
		Map<String, String> tags = tagManifest();
		for (String relPath : tags.keySet()) {
			if (!validateFile(tagStream(relPath), tags.get(relPath),
					csAlgorithm()))
				return false;
		}
		return true;
	}

	/**
	 * Returns the payload file for the passed relative path name
	 * 
	 * @param relPath
	 *            the relative path of the file from the data root directory
	 * @return the payload file, or null if no file at the specified path
	 * @throws IOException
	 * @throws IllegalAccessException
	 */
	public File payloadFile(String relPath) throws IOException,
			IllegalAccessException {
		if (sealed) {
			throw new IllegalAccessException(
					"Sealed Bag: no file access allowed");
		}
		File dFile = dataFile(relPath);
		return dFile.exists() ? dFile : null;
	}

	/**
	 * Returns an input stream for the passed payload relative path name
	 * 
	 * @param relPath
	 *            the relative path of the file from the data root directory
	 * @return in InputStream, or null if no file at the specified path
	 * @throws IOException
	 */
	public InputStream payloadStream(String relPath) throws IOException {
		File dFile = dataFile(relPath);
		return dFile.exists() ? new FileInputStream(dFile) : null;
	}

	/**
	 * Returns a map of payload files to their fetch URLs (i.e. contents of
	 * fetch.txt).
	 * 
	 * @return refMap the map of payload files to fetch URLs
	 * @throws IOException
	 */
	public Map<String, String> payloadRefs() throws IOException {
		return payloadRefs(bagFile(REF_FILE));
	}

	/**
	 * Returns a tag file for the passed tag relative path name
	 * 
	 * @param relPath
	 *            the relative path of the file from the bag root directory
	 * @return tagfile the tag file, or null if no file at the specified path
	 * @throws IOException
	 * @throws IllegalAccessException
	 */
	public File tagFile(String relPath) throws IOException,
			IllegalAccessException {
		if (sealed) {
			throw new IllegalAccessException(
					"Sealed Bag: no file access allowed");
		}
		File tFile = bagFile(relPath);
		return tFile.exists() ? tFile : null;
	}

	/**
	 * Returns an input stream for the passed tag relative path name
	 * 
	 * @param relPath
	 *            the relative path of the file from the bag root directory
	 * @return in InputStream, or null if no file at the specified path
	 * @throws IOException
	 */
	public InputStream tagStream(String relPath) throws IOException {
		File tFile = bagFile(relPath);
		return tFile.exists() ? new FileInputStream(tFile) : null;
	}

	/**
	 * Returns the values, if any, of a reserved metadata property in the
	 * standard metadata file (bag-info.txt) in order declared.
	 * 
	 * @param mdName
	 *            the metadata property name
	 * @return values property values for passed name, or empty list if no such
	 *         property defined.
	 * @throws IOException
	 */
	public List<String> metadata(MetadataName mdName) throws IOException {
		return metadata(mdName.getName());
	}

	/**
	 * Returns the values, if any, of the named metadata property in standard
	 * metadata file (bag-info.txt) in order declared.
	 * 
	 * @param name
	 *            the metadata property name
	 * @return values property value for passed name, or empty list if no such
	 *         property defined.
	 * @throws IOException
	 */
	public List<String> metadata(String name) throws IOException {
		return property(META_FILE, name);
	}

	/**
	 * Returns the values of a property, if any, in named file in order
	 * declared. Typically used for metadata properties in tag files.
	 * 
	 * @param relPath
	 *            bag-relative path to the property file
	 * @param name
	 *            the property name
	 * @return values property value for passed name, or empty list if no such
	 *         property defined.
	 * @throws IOException
	 */
	public List<String> property(String relPath, String name)
			throws IOException {
		Map<String, List<String>> mdSet = mdCache.get(relPath);
		if (mdSet == null) {
			synchronized (mdCache) {
				mdSet = new HashMap<>();
				FlatReader reader = new FlatReader(bagFile(relPath));
				String propName = null;
				StringBuilder valSb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					// if line does not start with spacer, it is a new property
					if (!line.startsWith(SPACER)) {
						// write pendng data if present
						if (propName != null) {
							addProp(propName, valSb.toString(), mdSet);
							valSb = new StringBuilder();
						}
						int split = line.indexOf(":");
						propName = line.substring(0, split);
						valSb.append(line.substring(split + 1).trim());
					} else {
						valSb.append(line.substring(SPACER.length()));
					}
				}
				reader.close();
				addProp(propName, valSb.toString(), mdSet);
				mdCache.put(relPath, mdSet);
			}
		}
		return mdSet.get(name);
	}

	/**
	 * Returns the contents of the payload manifest file. Contents are relative
	 * paths as keys and checksums as values.
	 * 
	 * @return map a map of resource path names to checksums
	 * @throws IOException
	 */
	public Map<String, String> payloadManifest() throws IOException {
		String sfx = csAlgorithm().toLowerCase() + ".txt";
		return manifest(MANIF_FILE + sfx);
	}

	/**
	 * Returns the contents of the tag manifest file. Contents are relative
	 * paths as keys and checksums as values.
	 * 
	 * @return map a map of resource path names to checksums
	 * @throws IOException
	 */
	public Map<String, String> tagManifest() throws IOException {
		String sfx = csAlgorithm().toLowerCase() + ".txt";
		return manifest(TAGMANIF_FILE + sfx);
	}

	/**
	 * Returns the contents of manifest file at relative path name. Contents are
	 * relative paths as keys and checksums as values. An empty map is returned
	 * if no manifest at path.
	 * 
	 * @param relPath
	 *            the package-relative path to the manifest file
	 * @return map a map of resource path names to checksums
	 * @throws IOException
	 */
	public Map<String, String> manifest(String relPath) throws IOException {
		Map<String, String> mfMap = new HashMap<>();
		FlatReader fr = new FlatReader(bagFile(relPath));
		String line = null;
		while ((line = fr.readLine()) != null) {
			String[] parts = line.split(" ");
			mfMap.put(parts[1], parts[0]);
		}
		fr.close();
		return mfMap;
	}

	// count of files in a directory, including subdirectory files (but not the
	// subdir itself)
	private int fileCount(File dir) throws IOException {
		int count = 0;
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				count += fileCount(file);
			} else {
				count++;
			}
		}
		return count;
	}

	private void addProp(String name, String value,
			Map<String, List<String>> mdSet) {
		List<String> vals = mdSet.get(name);
		if (vals == null) {
			vals = new ArrayList<>();
			mdSet.put(name, vals);
		}
		vals.add(value.trim());
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

	private File bagFile(String name) {
		return new File(baseDir, name);
	}

	private boolean validateFile(InputStream is, String expectedChecksum,
			String csAlg) throws IOException {
		byte[] buf = new byte[2048];
		int num = 0;
		if (is == null) {
			throw new IOException("no input");
		}
		// wrap stream in digest stream
		try (DigestInputStream dis = new DigestInputStream(is,
				MessageDigest.getInstance(csAlg))) {
			while (num != -1) {
				num = dis.read(buf);
			}
			return (expectedChecksum.equals(toHex(dis.getMessageDigest()
					.digest())));
		} catch (NoSuchAlgorithmException nsaE) {
			throw new IOException("no algorithm: " + csAlg);
		}
	}

	private static class FlatReader {
		private BufferedReader reader = null;

		private FlatReader(File file) throws IOException {
			reader = new BufferedReader(new FileReader(file));
		}

		public String readLine() throws IOException {
			return reader.readLine();
		}

		public void close() throws IOException {
			reader.close();
		}
	}

	static Map<String, String> payloadRefs(File refFile) throws IOException {
		Map<String, String> refMap = new HashMap<>();
		if (refFile.exists()) {
			FlatReader fr = new FlatReader(refFile);
			String line = null;
			while ((line = fr.readLine()) != null) {
				String[] parts = line.split(" ");
				refMap.put(parts[2], parts[0]);
			}
			fr.close();
		}
		return refMap;
	}

	static String csAlgorithm(File base) {
		// determine checksum in use from the manifest file name
		String[] manNames = base.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(MANIF_FILE);
			}
		});
		// any manifest files?
		return (manNames.length == 0) ? null : manNames[0].substring(
				MANIF_FILE.length(), manNames[0].lastIndexOf("."));
	}

	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	static String toHex(byte[] data) {
		if ((data == null) || (data.length == 0)) {
			return null;
		}
		char[] chars = new char[2 * data.length];
		for (int i = 0; i < data.length; ++i) {
			chars[2 * i] = HEX_CHARS[(data[i] & 0xF0) >>> 4];
			chars[2 * i + 1] = HEX_CHARS[data[i] & 0x0F];
		}
		return new String(chars);
	}

	private static final String[] tags = { "bytes", "KB", "MB", "GB", "TB" };

	static String scaledSize(long size, int index) {
		if (size < 1000) {
			return size + " " + tags[index];
		} else {
			return scaledSize(size / 1000, index + 1);
		}
	}

}
