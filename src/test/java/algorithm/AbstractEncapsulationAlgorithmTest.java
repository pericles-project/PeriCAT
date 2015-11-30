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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.junit.Before;
import org.junit.Test;

import main.TestDataProvider;
import model.EncapsulationData;
import model.RestoredFile;
import model.Scenario;

public class AbstractEncapsulationAlgorithmTest {
    AbstractAlgorithm algorithm;
    EncapsulationData dataset;

    @Before
    public void setUp() {
	algorithm = new AbstractAlgorithm() {
	    @Override
	    public String getName() {
		return null;
	    }

	    @Override
	    public String getDescription() {
		return null;
	    }

	    @Override
	    Scenario defineScenario() {
		return null;
	    }

	    @Override
	    SuffixFileFilter configureCarrierFileFilter() {
		List<String> supportedFileFormats = new ArrayList<String>();
		supportedFileFormats.add("png");
		supportedFileFormats.add("jpeg");
		supportedFileFormats.add("jp2");
		return new SuffixFileFilter(supportedFileFormats);
	    }

	    @Override
	    SuffixFileFilter configurePayloadFileFilter() {
		List<String> supportedFileFormats = new ArrayList<String>();
		supportedFileFormats.add("txt");
		supportedFileFormats.add("xml");
		return new SuffixFileFilter(supportedFileFormats);
	    }

	    @Override
	    SuffixFileFilter configureDecapsulationFileFilter() {
		return null;
	    }

	    @Override
	    protected List<RestoredFile> restore(File data) throws IOException {
		return null;
	    }

	    @Override
	    public boolean fulfilledTechnicalCriteria(File carrier, List<File> payloadList) {
		return true;
	    }

	    @Override
	    public File encapsulate(File carrier, List<File> payload) throws IOException {
		return null;
	    }

	};
	List<File> payload = new ArrayList<File>();
	dataset = new EncapsulationData(null, payload, "testDataset");
    }

    @Test
    public void fileTypeSupportedTest() {
	dataset.setCarrier(TestDataProvider.PNG_FILE);
	dataset.addPayload(TestDataProvider.TXT_FILE);
	assertTrue(algorithm.fileTypesSupported(dataset.getCarrier(), dataset.getPayload()));
	assertTrue(algorithm.fileTypesSupported(dataset.getCarrier(), dataset.getPayload()));
	dataset.addPayload(TestDataProvider.JPG_FILE);
	assertFalse(algorithm.fileTypesSupported(dataset.getCarrier(), dataset.getPayload()));
    }
}
