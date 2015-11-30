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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import main.TestDataProvider;
import model.RestoredFile;

public class QRCodeWatermarkingTest extends AbstractAlgorithmTest {

    @Test
    public void separateFileTest() {
	try {
	    File carrier = TestDataProvider.PNG_FILE;
	    File payload = TestDataProvider.TXT_FILE_2;
	    QRCodeWatermarking algorithm = new QRCodeWatermarking();
	    // Test encapsulation:
	    List<File> payloadList = new ArrayList<File>();
	    payloadList.add(payload);
	    // separate file encapsulation:
	    algorithm.setCarrierEncapsulation(false);
	    File outputFile = algorithm.encapsulate(carrier, payloadList);
	    assertNotNull(outputFile);

	    // Test restore - separate file encapsulation:
	    List<RestoredFile> restoredFiles = algorithm.restore(outputFile);
	    assertEquals(1, restoredFiles.size());
	    RestoredFile restoredPayload = restoredFiles.get(0);
	    assertNotNull(restoredPayload);
	    assertEquals(FileUtils.checksumCRC32(payload), FileUtils.checksumCRC32(restoredPayload));

	    // check restoration metadata:
	    assertEquals(payload.getName(), restoredPayload.getName());
	    assertEquals(algorithm, restoredPayload.algorithm);
	    assertTrue(restoredPayload.checksumValid);
	    assertTrue(restoredPayload.wasPayload);
	    assertFalse(restoredPayload.wasCarrier);
	    assertFalse(restoredPayload.relatedFiles.contains(restoredPayload));
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    @Test
    public void onCarrierTest() {
	try {
	    File carrier = TestDataProvider.PNG_FILE;
	    File payload = TestDataProvider.TXT_FILE;
	    QRCodeWatermarking algorithm = new QRCodeWatermarking();
	    // Test if it works, if the position is not (0,0):
	    algorithm.yPositionField.setText("2");
	    algorithm.xPositionField.setText("2");
	    // Test encapsulation:
	    List<File> payloadList = new ArrayList<File>();
	    payloadList.add(payload);
	    // on carrier encapsulation:
	    algorithm.setCarrierEncapsulation(true);
	    File outputFile = algorithm.encapsulate(carrier, payloadList);
	    assertNotNull(outputFile);
	    // Test restore - on carrier encapsulation:
	    List<RestoredFile> restoredFiles = algorithm.restore(outputFile);
	    assertEquals(restoredFiles.size(), 2);
	    RestoredFile restoredPayload = null;
	    RestoredFile restoredCarrier = null;
	    if (restoredFiles.get(0).wasPayload) {
		restoredPayload = restoredFiles.get(0);
		restoredCarrier = restoredFiles.get(1);
	    } else {
		restoredPayload = restoredFiles.get(1);
		restoredCarrier = restoredFiles.get(0);
	    }
	    assertNotNull(restoredPayload);
	    assertNotNull(restoredCarrier);
	    assertEquals(FileUtils.checksumCRC32(payload), FileUtils.checksumCRC32(restoredPayload));
	    // carrier is equal to qr code image:
	    assertEquals(FileUtils.checksumCRC32(restoredCarrier), FileUtils.checksumCRC32(outputFile));
	    // check restoration metadata:
	    assertEquals(payload.getName(), restoredPayload.getName());
	    assertEquals(carrier.getName(), restoredCarrier.getName());
	    assertEquals(algorithm, restoredPayload.algorithm);
	    assertEquals(algorithm, restoredCarrier.algorithm);
	    assertTrue(restoredPayload.checksumValid);
	    assertTrue(restoredPayload.wasPayload);
	    assertFalse(restoredPayload.wasCarrier);
	    assertTrue(restoredCarrier.wasCarrier);
	    assertFalse(restoredCarrier.wasPayload);
	    assertFalse(restoredPayload.relatedFiles.contains(restoredPayload));
	    assertTrue(restoredPayload.relatedFiles.contains(restoredCarrier));
	    assertTrue(restoredCarrier.relatedFiles.contains(restoredPayload));
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
