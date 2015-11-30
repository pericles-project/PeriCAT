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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import model.PayloadSegment;
import model.RestoredFile;
import model.Scenario;
import view.GUIPanel;

/**
 * This plug-In uses the random LSB algorithm from the Open Stego tool.
 * Because of license incompatibilities the LSB algorithm is delivered externally of PeriCAT.
 * Therefore the technique will only work if the corresponding jar is present.
 */
public class OpenStegoRandomLSBSteganography extends AbstractAlgorithm {

    private final JRadioButton trueCompressionButton = new JRadioButton("true");
    private final JRadioButton falseCompressionButton = new JRadioButton("false");

    public OpenStegoRandomLSBSteganography() {
	createConfigurationGui();
    }

    @Override
    Scenario defineScenario() {
	Scenario scenario = new Scenario("Random LSB steganography scenario");
	scenario.description = "This is the ideal scenario for using the OpenStego random LSB algorithm.";
	scenario.setCriterionValue(ENCAPSULATION_METHOD, EMBEDDING);
	scenario.setCriterionValue(VISIBILITY, INVISIBLE);
	scenario.setCriterionValue(DETECTABILITY, NOT_DETECTABLE);
	scenario.setCriterionValue(CARRIER_RESTORABILITY, NO);
	scenario.setCriterionValue(PAYLOAD_RESTORABILITY, YES);
	scenario.setCriterionValue(CARRIER_PROCESSABILITY, YES);
	scenario.setCriterionValue(PAYLOAD_ACCESSIBILITY, NO);
	scenario.setCriterionValue(ENCRYPTION, YES);
	scenario.setCriterionValue(COMPRESSION, YES);
	scenario.setCriterionValue(VELOCITY, NO);
	scenario.setCriterionValue(STANDARDS, NO);
	return scenario;
    }

    private void createConfigurationGui() {
	initButtons();
	panel = new GUIPanel();
	panel.setLayout(new GridBagLayout());
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.anchor = GridBagConstraints.NORTHWEST;
	panel.add(new JLabel("<html><h2>LSB embedding options</h2></html>"), constraints);
	constraints.gridy++;
	panel.add(new JLabel("Compress payload files (default true):"), constraints);
	constraints.gridx++;
	panel.add(trueCompressionButton, constraints);
	constraints.gridx++;
	panel.add(falseCompressionButton, constraints);
	constraints.gridx = 0;
	constraints.gridy++;
    }

    /**
     * Buttons to toggle optional compression on and off
     */
    private void initButtons() {
	ButtonGroup buttonGroupCompression = new ButtonGroup();
	buttonGroupCompression.add(trueCompressionButton);
	buttonGroupCompression.add(falseCompressionButton);
	trueCompressionButton.setSelected(true);
    }

    @Override
    SuffixFileFilter configureCarrierFileFilter() {
	List<String> supportedFileFormats = new ArrayList<String>();
	supportedFileFormats.add("bmp");
	supportedFileFormats.add("git");
	supportedFileFormats.add("jpeg");
	supportedFileFormats.add("jpg");
	supportedFileFormats.add("png");
	supportedFileFormats.add("wbmp");
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
	List<String> supportedFileFormats = new ArrayList<String>();
	supportedFileFormats.add("png");
	return new SuffixFileFilter(supportedFileFormats);
    }

    public File encapsulate(File cover, File message) throws IOException {
	List<File> payload = new ArrayList<File>();
	payload.add(message);
	return encapsulate(cover, payload);
    }

    @Override
    public File encapsulate(File carrier, List<File> payloadList) throws IOException {
	String cover = getCover(carrier);
	String message = getPayload(carrier, payloadList.get(0));
	String output = getOutputFileName(carrier);
	if (trueCompressionButton.isSelected()) {
	    encapsulateAndCompress(cover, message, output);
	} else {
	    encapsulate(cover, message, output);
	}
	FileUtils.forceDelete(new File(message));// tmp
	File outputFile = new File(output);
	if (outputFile.isFile()) {
	    return outputFile;
	}
	return null;
    }

    private void encapsulate(String cover, String message, String output) throws IOException {
	try {
	    String[] args = new String[] { "java", "-jar", LIBRARY_DIRECTORY + "openstego.jar", "embed", "-a",
		    "RandomLSB", "-cf", cover, "-mf", message, "-sf", output, "-C", "-E" };
	    Process process = Runtime.getRuntime().exec(args);
	    process.waitFor();
	    InputStream inputStream = process.getInputStream();
	    byte b[] = new byte[inputStream.available()];
	    inputStream.read(b, 0, b.length);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    private void encapsulateAndCompress(String cover, String message, String output) throws IOException {
	try {
	    String[] args = new String[] { "java", "-jar", LIBRARY_DIRECTORY + "openstego.jar", "embed", "-a",
		    "RandomLSB", "-cf", cover, "-mf", message, "-sf", output, "-c", "-E" };
	    Process process = Runtime.getRuntime().exec(args);
	    process.waitFor();
	    InputStream inputStream = process.getInputStream();
	    byte b[] = new byte[inputStream.available()];
	    inputStream.read(b, 0, b.length);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    private String getCover(File carrier) {
	return "" + carrier.toPath();
    }

    private String getPayload(File carrier, File payload) throws IOException {
	PayloadSegment payloadSegment = new PayloadSegment(carrier, payload, this);
	File payloadSemgentFile = new File("tmp");
	FileUtils.writeByteArrayToFile(payloadSemgentFile, payloadSegment.getPayloadSegmentBytes());
	return "" + payloadSemgentFile.toPath();
    }

    @Override
    public List<RestoredFile> restore(File carrier) throws IOException {
	List<RestoredFile> restoredFiles = new ArrayList<RestoredFile>();
	File tmpDir = new File("tmpDir");
	tmpDir.mkdir();
	try {
	    String[] args = new String[] { "java", "-jar", LIBRARY_DIRECTORY + "openstego.jar", "extract", "-a",
		    "RandomLSB", "-sf", "" + carrier.toPath(), "-xd", "" + tmpDir.toPath() };
	    Process process = Runtime.getRuntime().exec(args);
	    process.waitFor();
	    InputStream inputStream = process.getInputStream();
	    byte b[] = new byte[inputStream.available()];
	    inputStream.read(b, 0, b.length);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	String originalCarrierPath = "";
	if (tmpDir.listFiles().length == 1) {
	    File tmpMessage = tmpDir.listFiles()[0];
	    byte[] payloadSegmentBytes = FileUtils.readFileToByteArray(tmpMessage);
	    PayloadSegment payloadSegment = PayloadSegment.getPayloadSegment(payloadSegmentBytes);
	    RestoredFile message = new RestoredFile(RESTORED_DIRECTORY + payloadSegment.getPayloadName());
	    message.originalFilePath = payloadSegment.getPayloadPath();
	    originalCarrierPath = payloadSegment.getCarrierPath();
	    FileUtils.writeByteArrayToFile(message, payloadSegment.getPayloadBytes());
	    message.validateChecksum(payloadSegment.getPayloadChecksum());
	    message.restorationNote = "Payload can be restored correctly.";
	    message.wasPayload = true;
	    restoredFiles.add(message);
	}
	FileUtils.forceDelete(tmpDir);
	RestoredFile copiedCarrier = new RestoredFile(RESTORED_DIRECTORY + carrier.getName());
	FileUtils.copyFile(carrier, copiedCarrier);
	copiedCarrier.wasCarrier = true;
	copiedCarrier.checksumValid = false;
	copiedCarrier.restorationNote = "The carrier can't be restored with this steganography algorithm. It still contains the embedded payload file(s).";
	copiedCarrier.originalFilePath = originalCarrierPath;
	restoredFiles.add(copiedCarrier); // The carrier can not be restored;
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

    @Override
    public String getName() {
	return "OpenStego Random Least Significant Bit Steganography";
    }

    @Override
    public String getDescription() {
	String description = "Note: The used random LSB algorithm of the openstego tool is used via an external jar openstego.jar, because it is "
		+ "distrubuted unter GPL v 2 license and therefore can't be included into the PeriCAT "
		+ "Apache v.2 project. PeriCAT will run without the openstego.jar, but then the random LSB algorithm "
		+ "can't be used anymore. The openstego.jar has to be located in the PeriCAT_libs directory." + "\n"
		+ "This algorithm is the Random Least Significant Bit implementation of the tool"
		+ " OpenStego. It embedds text into image files by manipulating the least significant"
		+ " bit-planes of the image. The carrier can't be restored, but the embedded payload "
		+ "can be brought into its original state.\n"
		+ "The algorithm works on carrier files of the type png, jpeg or jp2, and text payload files.\n"
		+ "\nProject website: http://www.openstego.info/\n"
		+ "Theses about Least Significant Bit Embeddings by Aaron Miller (2012): http://aaronmiller.in/thesis/\n";
	if (!new File(LIBRARY_DIRECTORY + "f5.jar").isFile()) {
	    description = "WARNING: openstego.jar doesn't exist.\n\n" + description;
	}
	return description;
    }

    @Override
    public boolean fulfilledTechnicalCriteria(File carrier, List<File> payloadList) {
	return carrier.isFile() && payloadList.size() == 1 && new File(LIBRARY_DIRECTORY + "openstego.jar").isFile();
    }
}
