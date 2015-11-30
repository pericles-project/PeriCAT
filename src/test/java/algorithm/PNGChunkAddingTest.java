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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import main.TestDataProvider;
import model.RestoredFile;

public class PNGChunkAddingTest extends AbstractAlgorithmTest {

    @Test
    public void pngMultipleChunkAddingTest() {
	try {
	    File carrier = TestDataProvider.PNG_FILE;
	    File payload = TestDataProvider.TXT_FILE;
	    File payload2 = TestDataProvider.TXT_FILE_2;

	    List<File> payloadList = new ArrayList<File>();
	    payloadList.add(payload);
	    payloadList.add(payload2);

	    PNGChunkAdding algorithm = new PNGChunkAdding();
	    // Test encapsulation:
	    File outputFile = algorithm.encapsulate(carrier, payloadList);
	    assertNotNull(outputFile);

	    // Test restore:
	    Hashtable<String, RestoredFile> outputHash = new Hashtable<String, RestoredFile>();
	    for (RestoredFile file : algorithm.restore(outputFile)) {
		outputHash.put(file.getName(), file);
	    }
	    assertEquals(3, outputHash.size());
	    RestoredFile restoredCarrier = outputHash.get(carrier.getName());
	    RestoredFile restoredPayload = outputHash.get(payload.getName());
	    RestoredFile restoredPayload2 = outputHash.get(payload2.getName());
	    assertNotNull(restoredCarrier);
	    assertNotNull(restoredPayload);
	    assertNotNull(restoredPayload2);
	    // TODO: The carrier checksum is not valid, because the PNG library
	    // reorders the chunks! We would have to use a different library.
	    // assertEquals(FileUtils.checksumCRC32(carrier),
	    // FileUtils.checksumCRC32(restoredCarrier));
	    assertEquals(FileUtils.checksumCRC32(payload), FileUtils.checksumCRC32(restoredPayload));
	    assertEquals(FileUtils.checksumCRC32(payload2), FileUtils.checksumCRC32(restoredPayload2));

	    // check restoration metadata:
	    assertEquals("" + carrier.getAbsolutePath(), restoredCarrier.originalFilePath);
	    assertEquals("" + payload.getAbsolutePath(), restoredPayload.originalFilePath);
	    assertEquals(algorithm, restoredCarrier.algorithm);
	    // can't be true for this algorithm:
	    // assertTrue(restoredCarrier.checksumValid);
	    assertTrue(restoredPayload.checksumValid);
	    assertTrue(restoredCarrier.wasCarrier);
	    assertFalse(restoredCarrier.wasPayload);
	    assertTrue(restoredPayload.wasPayload);
	    assertFalse(restoredPayload.wasCarrier);
	    assertTrue(restoredCarrier.relatedFiles.contains(restoredPayload));
	    assertFalse(restoredCarrier.relatedFiles.contains(restoredCarrier));
	    assertTrue(restoredPayload.relatedFiles.contains(restoredCarrier));
	    assertFalse(restoredPayload.relatedFiles.contains(restoredPayload));
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
