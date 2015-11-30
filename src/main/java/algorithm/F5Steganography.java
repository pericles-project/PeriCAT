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

import static main.Configuration.LIBRARY_DIRECTORY;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import model.PayloadSegment;
import model.RestoredFile;
import model.Scenario;

/**
 * This Plug-In uses the F5 implementation from cgaffga based on Andreas Westfelds work.
 * Because of license incompatibilities the sources of F5 are delivered externally of PeriCAT. Therefore this algorithm
 * will only work if the F5.jar is present.
 */
public class F5Steganography extends AbstractAlgorithm {

    public F5Steganography() {
    }

    @Override
    Scenario defineScenario() {
	Scenario scenario = new Scenario("F5 ideal scenario");
	scenario.description = "This is the ideal scenario to use the F5 algorithm.";
	scenario.setCriterionValue(ENCAPSULATION_METHOD, EMBEDDING);
	scenario.setCriterionValue(VISIBILITY, INVISIBLE);
	scenario.setCriterionValue(DETECTABILITY, NOT_DETECTABLE);
	scenario.setCriterionValue(CARRIER_RESTORABILITY, NO);
	scenario.setCriterionValue(PAYLOAD_RESTORABILITY, YES);
	scenario.setCriterionValue(CARRIER_PROCESSABILITY, YES);
	scenario.setCriterionValue(PAYLOAD_ACCESSIBILITY, NO);
	scenario.setCriterionValue(ENCRYPTION, NO);
	scenario.setCriterionValue(COMPRESSION, YES);
	scenario.setCriterionValue(VELOCITY, YES);
	scenario.setCriterionValue(STANDARDS, NO);
	return scenario;
    }

    @Override
    SuffixFileFilter configureCarrierFileFilter() {
	List<String> supportedFileFormats = new ArrayList<String>();
	supportedFileFormats.add("jpg");
	supportedFileFormats.add("tif");
	supportedFileFormats.add("gif");
	supportedFileFormats.add("bmp");
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
	return configureCarrierFileFilter();
    }

    @Override
    public File encapsulate(File carrierFile, List<File> payloadList) throws IOException {
	String carrier = getCarrier(carrierFile);
	String payload = getPayloadPathString(carrierFile, payloadList.get(0));
	String output = getOutputString(carrierFile);
	encapsulate(carrier, payload, output);
	FileUtils.forceDelete(new File(payload)); // tmp
	File outputFile = new File(output);
	if (outputFile.isFile()) {
	    return outputFile;
	}
	return null;
    }

    private void encapsulate(String carrier, String payload, String output) throws IOException {
	try {
	    String[] args = new String[] { "java", "-jar", LIBRARY_DIRECTORY + "f5.jar", "e", "-e", payload, carrier,
		    output };
	    Process process = Runtime.getRuntime().exec(args);
	    process.waitFor();
	    InputStream inputStream = process.getInputStream();
	    byte b[] = new byte[inputStream.available()];
	    inputStream.read(b, 0, b.length);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    private String getCarrier(File carrierFile) {
	return "" + carrierFile.toPath();
    }

    private String getPayloadPathString(File carrier, File payload) throws IOException {
	PayloadSegment payloadSegment = new PayloadSegment(carrier, payload, this);
	File payloadSemgentFile = new File("tmp");
	FileUtils.writeByteArrayToFile(payloadSemgentFile, payloadSegment.getPayloadSegmentBytes());
	return "" + payloadSemgentFile.toPath();
    }

    private String getOutputString(File carrier) {
	File outputFile = getOutputFile(carrier);
	outputFile.delete(); // F5 won't override existing files!
	return "" + outputFile.toPath();
    }

    @Override
    public List<RestoredFile> restore(File carrier) throws IOException {
	List<RestoredFile> restoredFiles = new ArrayList<RestoredFile>();
	File tmpPayload = new File("tmp");
	tmpPayload.delete(); // F5 won't override existing files!
	restore("" + tmpPayload.toPath(), "" + carrier.toPath());
	RestoredFile copiedCarrier = new RestoredFile(RESTORED_DIRECTORY + carrier.getName());
	copiedCarrier.wasCarrier = true;
	copiedCarrier.checksumValid = false;
	copiedCarrier.restorationNote = "The carrier can't be restored with this steganography algorithm. It still contains the embedded payload file(s).";
	FileUtils.copyFile(carrier, copiedCarrier);
	byte[] payloadSegmentBytes = FileUtils.readFileToByteArray(tmpPayload);
	PayloadSegment payloadSegment = PayloadSegment.getPayloadSegment(payloadSegmentBytes);
	RestoredFile restoredPayload = new RestoredFile(RESTORED_DIRECTORY + payloadSegment.getPayloadName());
	FileUtils.writeByteArrayToFile(restoredPayload, payloadSegment.getPayloadBytes());
	restoredPayload.validateChecksum(payloadSegment.getPayloadChecksum());
	restoredPayload.restorationNote = "Payload can be restored correctly.";
	restoredPayload.wasPayload = true;
	restoredPayload.originalFilePath = payloadSegment.getPayloadPath();
	copiedCarrier.originalFilePath = payloadSegment.getCarrierPath();
	restoredFiles.add(restoredPayload);
	FileUtils.forceDelete(tmpPayload);
	restoredFiles.add(copiedCarrier);// carrier can not be restored
	for (RestoredFile file : restoredFiles) {
	    file.algorithm = this;
	    for (RestoredFile relatedFile : restoredFiles) {
		if (file != relatedFile) {
		    file.relatedFiles.add(relatedFile);
		}
	    }
	}
	return restoredFiles;
    }

    private void restore(String payload, String carrier) throws IOException {
	try {
	    Process process = Runtime.getRuntime()
		    .exec(new String[] { "java", "-jar", LIBRARY_DIRECTORY + "f5.jar", "x", "-e", payload, carrier });
	    process.waitFor();
	    InputStream inputStream = process.getInputStream();
	    byte b[] = new byte[inputStream.available()];
	    inputStream.read(b, 0, b.length);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public String getName() {
	return "F5 Steganography";
    }

    @Override
    public String getDescription() {
	String description = "Note: The used F5 algorithm is used via an external jar f5.jar, because it is "
		+ "distrubuted unter GPL v 2 license and therefore can't be included into the PeriCAT "
		+ "Apache v.2 project. PeriCAT will run without the f5.jar, but then the F5 algorithm "
		+ "can't be used anymore. The f5.jar has to be located in the PeriCAT_libs directory." + "\n"
		+ "\nF5 is a steganography algorithm described in the paper \"F5 - A Steganography "
		+ "Algorithm - High Capacity Despite Better Steganalysis - Andreas Westfeld\".\n"
		+ "F5 works on image carriers of the type jpg, tif, gif, or bmp and text payload. It is "
		+ "possible to restore the payload correctly in every bit, but, as typical for "
		+ "steganography, the carrier can't be brought back into its original state."
		+ "\nThe algorithm embedds payload images in carrier images.\n\n"
		+ "https://code.google.com/p/f5-steganography/\n"
		+ "f5-steganography.googlecode.com/files/F5%20Steganography.pdf\n"
		+ "Abstract. Many steganographic systems are weak against visual and statistical attacks."
		+ " Systems without these weaknesses offer only a relatively small capacity for "
		+ "steganographic messages. The newly developed algorithm F5 withstands visual and "
		+ "statistical attacks, yet it still offers a large steganographic capacity. F5 "
		+ "implements matrix encoding to improve the efficiency of embedding. Thus it "
		+ "reduces the number of nec- essary changes. F5 employs permutative straddling to "
		+ "uniformly spread out the changes over the whole steganogram.";
	if (!new File(LIBRARY_DIRECTORY + "f5.jar").isFile()) {
	    description = "WARNING: f5.jar doesn't exist.\n\n" + description;
	}
	return description;
    }

    @Override
    public boolean fulfilledTechnicalCriteria(File carrier, List<File> payloadList) {
	return carrier.isFile() && payloadList.size() == 1 && new File(LIBRARY_DIRECTORY + "f5.jar").isFile();
    }
}
