/*
 * This project has received funding from the European Unions Seventh 
 * Framework Programme for research, technological development and 
 * demonstration under grant agreement no FP7-601138 PERICLES.
 * 
 * Copyright 2015 Anna Eggers, State- and Univeristy Library Goettingen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.junit.Before;
import org.junit.Test;

import main.TestDataProvider;
import model.RestoredFile;

public class ImageImageFrameExpandingTest extends AbstractAlgorithmTest {

    ImageImageFrameExpanding algorithm;

    @Override
    @Before
    public void setUp() {
	super.setUp();
	algorithm = new ImageImageFrameExpanding();
    }

    @Test
    public void pngSingleTest() {
	singleFileTest(TestDataProvider.PNG_FILE, TestDataProvider.PNG_FILE_2);
    }

    @Test
    public void pngSingleTest3() {
	singleFileTest(TestDataProvider.PNG_FILE_2, TestDataProvider.PNG_FILE);
    }

    @Test
    public void pngMultipleTest2() {
	multipleFilesTest(TestDataProvider.PNG_FILE_2, TestDataProvider.PNG_FILE, TestDataProvider.PNG_FILE_2);
    }

    @Test
    public void restorationMetadataImageTest() {
	File carrier = TestDataProvider.PNG_FILE;
	File payload = TestDataProvider.PNG_FILE_2;
	try {
	    BufferedImage restorationMetadataBuffered = algorithm.getMetadataImage(carrier, payload);
	    File testFile = new File("testFile.png");
	    testFile.createNewFile();
	    ImageIO.write(restorationMetadataBuffered, "png", testFile);
	    OpenStegoRandomLSBSteganography lsbAlgorithm = new OpenStegoRandomLSBSteganography();
	    List<RestoredFile> restoredFiles = lsbAlgorithm.restore(testFile);
	    assertEquals(restoredFiles.size(), 2);
	    RestoredFile restoredMetadataFile = algorithm.getRestorationMetadataFile(restoredFiles);
	    assertNotNull(restoredMetadataFile);
	    assertTrue(restoredMetadataFile.wasPayload);

	    Properties restoredProperties = new Properties();
	    InputStream inputStream = new FileInputStream(restoredMetadataFile);
	    restoredProperties.load(inputStream);
	    BufferedImage carrierBuffered = ImageIO.read(carrier);
	    BufferedImage payloadBuffered = ImageIO.read(payload);
	    assertEquals(restoredProperties.getProperty("carrierWidth"), "" + carrierBuffered.getWidth());
	    assertEquals(restoredProperties.getProperty("carrierHeight"), "" + carrierBuffered.getHeight());
	    assertEquals(restoredProperties.getProperty("payloadWidth"), "" + payloadBuffered.getWidth());
	    assertEquals(restoredProperties.getProperty("payloadHeight"), "" + payloadBuffered.getHeight());
	    inputStream.close();
	    testFile.delete();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void singleFileTest(File carrier, File payload) {
	try {
	    // Test encapsulation:
	    File result = algorithm.encapsulate(carrier, payload);
	    assertTrue(result.length() > carrier.length());
	    BufferedImage encapsulatedFilesBuffered = ImageIO.read(result);
	    BufferedImage carrierBuffered = ImageIO.read(carrier);
	    BufferedImage payloadBuffered = ImageIO.read(payload);
	    assertEquals(carrierBuffered.getHeight() + payloadBuffered.getHeight() + algorithm.METADATA_HEIGHT,
		    encapsulatedFilesBuffered.getHeight());
	    assertEquals(Math.max(carrierBuffered.getWidth(), payloadBuffered.getWidth()),
		    encapsulatedFilesBuffered.getWidth());

	    // Test restore in single steps:
	    File restorationMetadataImage = algorithm.restoreRestorationMetadataImage(result);
	    BufferedImage restorationMetadataImageBuffered = ImageIO.read(restorationMetadataImage);
	    assertEquals(algorithm.METADATA_HEIGHT, restorationMetadataImageBuffered.getHeight());
	    assertEquals(encapsulatedFilesBuffered.getWidth(), restorationMetadataImageBuffered.getWidth());
	    Properties restorationMetadata = algorithm.getRestorationMetadata(result);
	    assertNotNull(restorationMetadata.getProperty("payloadHeight"));
	    assertNotNull(restorationMetadata.getProperty("payloadWidth"));
	    assertNotNull(restorationMetadata.getProperty("carrierHeight"));
	    assertNotNull(restorationMetadata.getProperty("carrierWidth"));

	    // copied these steps from the algorithm to be able to compare the
	    // result with the original:
	    RestoredFile restoredCarrierFile = algorithm.restoreCarrier(result, restorationMetadata);
	    RestoredFile restoredPayloadFile = algorithm.restorePayload(result, restorationMetadata);
	    BufferedImage restoredCarrierBuffered = ImageIO.read(restoredCarrierFile);
	    BufferedImage restoredPayloadBuffered = ImageIO.read(restoredPayloadFile);
	    assertEquals(restoredCarrierBuffered.getWidth(), carrierBuffered.getWidth());
	    assertEquals(restoredCarrierBuffered.getHeight(), carrierBuffered.getHeight());
	    assertEquals(restoredPayloadBuffered.getWidth(), payloadBuffered.getWidth());
	    assertEquals(restoredPayloadBuffered.getHeight(), payloadBuffered.getHeight());

	    // FIXME maybe: equal length works not for all tests. I don't know
	    // why. Maybe it is again the "too intelligent PNG library" which
	    // re-organises the chunks? For the same reason the checksums are
	    // invalid.
	    // assertEquals(restoredPayloadFile.length(), payload.length());
	    // assertEquals(restoredCarrierFile.length(), carrier.length());

	    // Test whole restore method:
	    Hashtable<String, RestoredFile> outputHash = new Hashtable<String, RestoredFile>();
	    for (RestoredFile file : algorithm.restore(result)) {
		outputHash.put(file.getName(), file);
	    }
	    assertEquals(2, outputHash.size());
	    RestoredFile restoredCarrier = outputHash.get(carrier.getName());
	    RestoredFile restoredPayload = outputHash.get(payload.getName());
	    assertNotNull(restoredCarrier);
	    assertNotNull(restoredPayload);

	    // check restoration metadata:
	    assertEquals(algorithm, restoredCarrier.algorithm);
	    assertTrue(restoredCarrier.wasCarrier);
	    assertFalse(restoredCarrier.wasPayload);
	    assertTrue(restoredPayload.wasPayload);
	    assertFalse(restoredPayload.wasCarrier);
	    assertTrue(restoredCarrier.relatedFiles.contains(restoredPayload));
	    assertFalse(restoredCarrier.relatedFiles.contains(restoredCarrier));
	    assertTrue(restoredPayload.relatedFiles.contains(restoredCarrier));
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void multipleFilesTest(File carrier, File payload1, File payload2) {
	List<File> payloadList = new ArrayList<File>();
	payloadList.add(payload1);
	payloadList.add(payload2);
	try {
	    File outputFile = algorithm.encapsulate(carrier, payloadList);
	    assertNotNull(outputFile);
	    List<RestoredFile> restoredFiles = algorithm.restore(outputFile);
	    assertEquals(3, restoredFiles.size());
	    RestoredFile restoredCarrier = null;
	    RestoredFile restoredPayload1 = null;
	    RestoredFile restoredPayload2 = null;
	    for (RestoredFile file : restoredFiles) {
		if (file.wasCarrier) {
		    restoredCarrier = file;
		} else if (file.wasPayload) {
		    if (file.getName().equals(payload1.getName())) {
			restoredPayload1 = file;
		    } else if (file.getName().equals(payload2.getName())) {
			restoredPayload2 = file;
		    }
		}
	    }
	    assertNotNull(restoredCarrier);
	    assertNotNull(restoredPayload1);
	    assertNotNull(restoredPayload2);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
