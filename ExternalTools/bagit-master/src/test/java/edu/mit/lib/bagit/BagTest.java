/**
 * Copyright 2013 MIT Libraries
 * Licensed under: http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.mit.lib.bagit;

import static edu.mit.lib.bagit.Bag.DATA_DIR;
import static edu.mit.lib.bagit.Bag.DECL_FILE;
import static edu.mit.lib.bagit.Bag.MetadataName.BAGGING_DATE;
import static edu.mit.lib.bagit.Bag.MetadataName.BAG_SIZE;
import static edu.mit.lib.bagit.Bag.MetadataName.PAYLOAD_OXNUM;
import static edu.mit.lib.bagit.Bag.MetadataName.SOURCE_ORG;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/*
 * Basic unit tests for BagIt Library. Incomplete.
 */

@RunWith(JUnit4.class)
public class BagTest {

	public File payload1, payload2, tag1, tag2;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void createTestData() throws IOException {
		payload1 = tempFolder.newFile("payload1");
		// copy some random bits
		FileOutputStream out = new FileOutputStream(payload1);
		for (int i = 0; i < 1000; i++) {
			out.write("lskdflsfevmep".getBytes());
		}
		out.close();
		// copy to all other test files
		payload2 = tempFolder.newFile("payload2");
		Files.copy(payload1.toPath(), payload2.toPath(), REPLACE_EXISTING);
		tag1 = tempFolder.newFile("tag1");
		Files.copy(payload1.toPath(), tag1.toPath(), REPLACE_EXISTING);
		tag2 = tempFolder.newFile("tag2");
		Files.copy(payload1.toPath(), tag2.toPath(), REPLACE_EXISTING);
	}

	@Test
	public void basicBagPartsPresentMD5() throws IOException,
			IllegalAccessException {
		File bagFile = tempFolder.newFolder("bag1");
		new Filler(bagFile).payload(payload1).toDirectory();
		File decl = new File(bagFile, DECL_FILE);
		assertTrue(decl.exists());
		File manifest = new File(bagFile, "manifest-md5.txt");
		assertTrue(manifest.exists());
		File tagmanifest = new File(bagFile, "tagmanifest-md5.txt");
		assertTrue(tagmanifest.exists());
		File payloadDir = new File(bagFile, DATA_DIR);
		assertTrue(payloadDir.isDirectory());
		File payloadFile = new File(payloadDir, payload1.getName());
		assertTrue(payloadFile.exists());
		// assure completeness
		Bag bag = new Loader(bagFile).load();
		assertTrue(bag.isComplete());
		assertTrue(bag.isValid());
	}

	@Test
	public void basicBagPartsPresentSHA1() throws IOException,
			IllegalAccessException {
		File bagFile = tempFolder.newFolder("bag2");
		new Filler(bagFile, "SHA1").payload(payload1).toDirectory();
		File decl = new File(bagFile, DECL_FILE);
		assertTrue(decl.exists());
		File manifest = new File(bagFile, "manifest-sha1.txt");
		assertTrue(manifest.exists());
		File tagmanifest = new File(bagFile, "tagmanifest-sha1.txt");
		assertTrue(tagmanifest.exists());
		File payloadDir = new File(bagFile, DATA_DIR);
		assertTrue(payloadDir.isDirectory());
		File payloadFile = new File(payloadDir, payload1.getName());
		assertTrue(payloadFile.exists());
		// assure completeness
		Bag bag = new Loader(bagFile).load();
		assertTrue(bag.isComplete());
	}

	@Test
	public void multiPayloadBag() throws IOException {
		File bagFile = tempFolder.newFolder("bag3");
		new Filler(bagFile).payload("first.pdf", payload1)
				.payload("second/second.pdf", payload2).toDirectory();
		File payloadDir = new File(bagFile, DATA_DIR);
		assertTrue(payloadDir.isDirectory());
		File pload1 = new File(payloadDir, "first.pdf");
		assertTrue(pload1.exists());
		File pload2 = new File(payloadDir, "second/second.pdf");
		assertTrue(pload2.exists());
	}

	@Test
	public void multiTagBag() throws IOException {
		File bagFile = tempFolder.newFolder("bag4");
		new Filler(bagFile).tag("first.pdf", tag1)
				.tag("second/second.pdf", tag2).toDirectory();
		File tagDir = new File(bagFile, "second");
		assertTrue(bagFile.isDirectory());
		File ttag1 = new File(bagFile, "first.pdf");
		assertTrue(ttag1.exists());
		File ttag2 = new File(tagDir, "second.pdf");
		assertTrue(ttag2.exists());
	}

	@Test
	public void metadataBag() throws IOException {
		File bagFile = tempFolder.newFolder("bag5");
		Filler filler = new Filler(bagFile).payload("first.pdf", payload1);
		String val1 = "metadata value";
		String val2 = "JUnit4 Test Harness";
		filler.metadata("Metadata-test", val1);
		filler.metadata(SOURCE_ORG, val2);
		Bag bag = new Loader(filler.toDirectory()).load();
		File payloadDir = new File(bagFile, DATA_DIR);
		assertTrue(payloadDir.isDirectory());
		File payload1 = new File(payloadDir, "first.pdf");
		assertTrue(payload1.exists());
		assertTrue(bag.metadata("Metadata-test").get(0).equals(val1));
		assertTrue(bag.metadata(SOURCE_ORG).get(0).equals(val2));
	}

	@Test
	public void autoGenMetadataBag() throws IOException {
		File bagFile = tempFolder.newFolder("bag6");
		Filler filler = new Filler(bagFile).payload("first.pdf", payload1);
		String val1 = "metadata value";
		String val2 = "JUnit4 Test Harness";
		filler.metadata("Metadata-test", val1);
		filler.metadata(SOURCE_ORG, val2);
		Bag bag = new Loader(filler.toDirectory()).load();
		File payloadDir = new File(bagFile, "data");
		assertTrue(payloadDir.isDirectory());
		File payload1 = new File(payloadDir, "first.pdf");
		assertTrue(payload1.exists());
		assertNotNull(bag.metadata(BAGGING_DATE));
		assertNotNull(bag.metadata(BAG_SIZE));
		assertNotNull(bag.metadata(PAYLOAD_OXNUM));
		File bagFile2 = tempFolder.newFolder("bag7");
		Filler filler2 = new Filler(bagFile2).payload("first.pdf", payload1);
		filler2.noAutoGen().metadata(SOURCE_ORG, val2);
		Bag bag2 = new Loader(filler2.toDirectory()).load();
		assertNull(bag2.metadata(BAGGING_DATE));
		assertNull(bag2.metadata(BAG_SIZE));
		assertNull(bag2.metadata(PAYLOAD_OXNUM));
	}

	@Test
	public void completeAndIncompleteBag() throws IOException {
		File bagFile = tempFolder.newFolder("bag8");
		Filler filler = new Filler(bagFile).payload("first.pdf", payload1)
				.payload("second.pdf", payload2);
		Bag bag = new Loader(filler.toDirectory()).load();
		assertTrue(bag.isComplete());
		// now remove a payload file
		File toDel = new File(bagFile, "data/first.pdf");
		toDel.delete();
		assertTrue(!bag.isComplete());
	}

	@Test
	public void validAndInvalidBag() throws IOException {
		File bagFile = tempFolder.newFolder("bag9");
		Filler filler = new Filler(bagFile).payload("first.pdf", payload1);
		Bag bag = new Loader(filler.toDirectory()).load();
		assertTrue(bag.isValid());
		// now remove a payload file
		File toDel = new File(bagFile, "data/first.pdf");
		toDel.delete();
		assertTrue(!bag.isValid());
	}

	@Test
	public void bagPackandLoadRoundtrip() throws IOException,
			IllegalAccessException {
		File bagFile = tempFolder.newFolder("bag10");
		Filler filler = new Filler(bagFile).payload("first.pdf", payload1);
		File bagPackage = filler.toPackage();
		Bag bag = new Loader(bagPackage).load();
		File payload = bag.payloadFile("first.pdf");
		assertTrue(payload1.length() == payload.length());
	}

//	@Test(expected = IllegalAccessException.class)
//	public void sealedBagAccess() throws IOException, IllegalAccessException {
//		File bagFile = tempFolder.newFolder("bag11");
//		Filler filler = new Filler(bagFile).payload("first.pdf", payload1);
//		File bagPackage = filler.toPackage();
//		Bag bag = new Loader(bagPackage).seal();
//		// stream access OK
//		assertNotNull(bag.payloadStream("first.pdf"));
//		// will throw IllegalAccessException
//		// File payload = bag.payloadFile("first.pdf");
//	}

	@Test
	public void streamReadBag() throws IOException {
		File bagFile = tempFolder.newFolder("bag12");
		Filler filler = new Filler(bagFile);
		InputStream plIS = new FileInputStream(payload1);
		InputStream tagIS = new FileInputStream(tag1);
		filler.payload("first.pdf", plIS).tag("firstTag.txt", tagIS)
				.toDirectory();
		File payloadDir = new File(bagFile, DATA_DIR);
		File pload1 = new File(payloadDir, "first.pdf");
		assertTrue(pload1.exists());
		File ttag1 = new File(bagFile, "firstTag.txt");
		assertTrue(ttag1.exists());
	}

	@Test
	public void streamWrittenBag() throws IOException {
		File bagFile = tempFolder.newFolder("bag13");
		Filler filler = new Filler(bagFile);
		OutputStream plout = filler.payloadStream("first.pdf");
		for (int i = 0; i < 1000; i++) {
			plout.write("lskdflsfevmep".getBytes());
		}
		plout.close();
		OutputStream tout = filler.tagStream("tags/firstTag.txt");
		for (int i = 0; i < 1000; i++) {
			tout.write("lskdflsfevmep".getBytes());
		}
		tout.close();
		filler.toDirectory();
		File payloadDir = new File(bagFile, DATA_DIR);
		File pload1 = new File(payloadDir, "first.pdf");
		assertTrue(pload1.exists());
		File ttag1 = new File(bagFile, "tags/firstTag.txt");
		assertTrue(ttag1.exists());
	}

}
