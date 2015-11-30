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
package main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import algorithm.AbstractAlgorithmTest;
import controller.ScenarioSaverAndLoader;
import model.Scenario;

/**
 * Tests the PeriCAT API.
 */
public class PeriCATTest extends AbstractAlgorithmTest {

    /**
     * Test encapsulation and decapsulation with known algorithm.
     */
    @Test
    public void encapsulationAndDecapsulationTest() {
	File carrier = TestDataProvider.PDF_FILE;
	File pngFile = TestDataProvider.PNG_FILE;
	File txtFile = TestDataProvider.TXT_FILE;
	List<File> payload = new ArrayList<File>();
	payload.add(pngFile);
	payload.add(txtFile);
	File outputFile = PeriCAT.encapsulate(carrier, payload, PeriCAT.TAR_PACKAGING);
	assertNotNull(outputFile);
	List<File> decapsulatedFiles = PeriCAT.decapsulate(outputFile, PeriCAT.TAR_PACKAGING);
	assertEquals(decapsulatedFiles.size(), 1 + payload.size());
    }

    /**
     * Test encapsulation and decapsulation with scenario file.
     */
    @Test
    public void scenarioBasedEncapsulationAndDecapsulationTest() {
	File carrier = TestDataProvider.JPG_FILE;
	File txtFile = TestDataProvider.TXT_FILE_2;
	File xmlFile = TestDataProvider.XML_FILE;
	List<File> payload = new ArrayList<File>();
	payload.add(txtFile);
	payload.add(xmlFile);
	Scenario scenario = new Scenario("testScenario");
	scenario.setCriterionValue(model.Criterion.ENCAPSULATION_METHOD, 10);
	scenario.setCriterionValue(model.Criterion.VISIBILITY, 20);
	File scenarioFile = new File("testScenario.scenario");
	ScenarioSaverAndLoader.saveTo(scenario, scenarioFile);
	File outputFile = PeriCAT.encapsulate(carrier, payload, scenarioFile);
	assertNotNull(outputFile);
	// Test decapsulation with unknown algorithm:
	List<File> decapsulatedFiles = PeriCAT.decapsulate(outputFile);
	assertEquals(decapsulatedFiles.size(), 1 + payload.size());
	scenarioFile.delete();
    }
}
