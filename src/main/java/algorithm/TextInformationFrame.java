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

import static main.Configuration.RESTORED_DIRECTORY;
import static model.Criterion.CARRIER_PROCESSABILITY;
import static model.Criterion.CARRIER_RESTORABILITY;
import static model.Criterion.COMPRESSION;
import static model.Criterion.DETECTABILITY;
import static model.Criterion.ENCAPSULATION_METHOD;
import static model.Criterion.ENCRYPTION;
import static model.Criterion.PAYLOAD_ACCESSIBILITY;
import static model.Criterion.PAYLOAD_RESTORABILITY;
import static model.Criterion.STANDARDS;
import static model.Criterion.VELOCITY;
import static model.Criterion.VISIBILITY;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import model.PayloadSegment;
import model.RestoredFile;
import model.Scenario;

/**
 * This algorithm simply adds a payload text section to text files.
 *
 * @author Anna Eggers
 */
public class TextInformationFrame extends AbstractAlgorithm {

    /**
     * Appends the bytes of all payload files to each carrier file.
     */
    @Override
    public File encapsulate(File carrier, List<File> payloadList) throws IOException {
	File outputFile = getOutputFile(carrier);
	for (File payload : payloadList) {
	    append(outputFile, payload, carrier);
	}
	return outputFile;
    }

    /**
     * Appends the payload bytes at the end of the carrier file.
     * 
     * @param outputCarrier
     *            carrier bytes + payload bytes
     * @param payload
     *            file to be appended
     */
    private void append(File outputCarrier, File payload, File originalCarrier) throws IOException {
	PayloadSegment metadata = new PayloadSegment(originalCarrier, payload, this);
	FileUtils.writeByteArrayToFile(outputCarrier, metadata.getPayloadSegmentBytes(), true);
    }

    @Override
    public List<RestoredFile> restore(File encapsulatedData) throws IOException {
	List<RestoredFile> restoredFiles = new ArrayList<RestoredFile>();
	String restoredCarrierName = getRestoredCarrierName(encapsulatedData);
	byte[] encapsulatedBytes = FileUtils.readFileToByteArray(encapsulatedData);
	String carrierChecksum = "";
	String carrierPath = "";
	while (true) {
	    PayloadSegment payloadSegment = PayloadSegment.getPayloadSegment(encapsulatedBytes);
	    if (payloadSegment == null) {
		RestoredFile carrier = new RestoredFile(restoredCarrierName);
		FileUtils.writeByteArrayToFile(carrier, encapsulatedBytes);
		carrier.validateChecksum(carrierChecksum);
		carrier.wasCarrier = true;
		carrier.algorithm = this;
		carrier.relatedFiles.addAll(restoredFiles);
		carrier.originalFilePath = carrierPath;
		for (RestoredFile file : restoredFiles) {
		    file.relatedFiles.add(carrier);
		    file.algorithm = this;
		}
		restoredFiles.add(carrier);
		return restoredFiles;
	    } else {
		RestoredFile payload = new RestoredFile(RESTORED_DIRECTORY + payloadSegment.getPayloadName());
		FileUtils.writeByteArrayToFile(payload, payloadSegment.getPayloadBytes());
		payload.validateChecksum(payloadSegment.getPayloadChecksum());
		payload.wasPayload = true;
		payload.originalFilePath = payloadSegment.getPayloadPath();
		payload.relatedFiles.addAll(restoredFiles);
		for (RestoredFile file : restoredFiles) {
		    file.relatedFiles.add(payload);
		}
		restoredFiles.add(payload);
		encapsulatedBytes = PayloadSegment.removeLeastPayloadSegment(encapsulatedBytes);
		carrierChecksum = payloadSegment.getCarrierChecksum();
		carrierPath = payloadSegment.getCarrierPath();
	    }
	}
    }

    @Override
    public String getName() {
	return "Txt information frame";
    }

    @Override
    public String getDescription() {
	String description = "This algorithm works on text files. It appends the payload text at the"
		+ " end of the carrier file. The user can see the appended payload in a standard text"
		+ " editor. The algorithm is able to append more than one payload file text and to "
		+ "restore the carrier and the payload files correctly in every bit.\n";
	return description;
    }

    @Override
    public boolean fulfilledTechnicalCriteria(File carrier, List<File> payloadList) {
	return carrier.isFile() && payloadList.size() > 0;
    }

    @Override
    Scenario defineScenario() {
	Scenario scenario = new Scenario("Text information frame scenario");
	scenario.description = "This is the ideal scenario for using the text information frame algorithm.";
	scenario.setCriterionValue(ENCAPSULATION_METHOD, EMBEDDING);
	scenario.setCriterionValue(VISIBILITY, VISIBLE);
	scenario.setCriterionValue(DETECTABILITY, DETECTABLE);
	scenario.setCriterionValue(CARRIER_RESTORABILITY, YES);
	scenario.setCriterionValue(PAYLOAD_RESTORABILITY, YES);
	scenario.setCriterionValue(CARRIER_PROCESSABILITY, YES);
	scenario.setCriterionValue(PAYLOAD_ACCESSIBILITY, YES);
	scenario.setCriterionValue(ENCRYPTION, NO);
	scenario.setCriterionValue(COMPRESSION, NO);
	scenario.setCriterionValue(VELOCITY, YES);
	scenario.setCriterionValue(STANDARDS, NO);
	return scenario;
    }

    @Override
    SuffixFileFilter configureCarrierFileFilter() {
	List<String> supportedFileFormats = new ArrayList<String>();
	supportedFileFormats.add("txt");
	return new SuffixFileFilter(supportedFileFormats);
    }

    @Override
    SuffixFileFilter configurePayloadFileFilter() {
	List<String> supportedFileFormats = new ArrayList<String>();
	supportedFileFormats.add("xml");
	supportedFileFormats.add("txt");
	return new SuffixFileFilter(supportedFileFormats);
    }

    @Override
    SuffixFileFilter configureDecapsulationFileFilter() {
	return configureCarrierFileFilter();
    }
}
