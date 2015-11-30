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

import static main.Configuration.OUTPUT_DIRECTORY;
import static main.Configuration.TOOL_DESCRIPTION;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import edu.harvard.hul.ois.mets.Agent;
import edu.harvard.hul.ois.mets.Checksumtype;
import edu.harvard.hul.ois.mets.Div;
import edu.harvard.hul.ois.mets.FLocat;
import edu.harvard.hul.ois.mets.FileGrp;
import edu.harvard.hul.ois.mets.FileSec;
import edu.harvard.hul.ois.mets.Fptr;
import edu.harvard.hul.ois.mets.Loctype;
import edu.harvard.hul.ois.mets.Mets;
import edu.harvard.hul.ois.mets.MetsHdr;
import edu.harvard.hul.ois.mets.Name;
import edu.harvard.hul.ois.mets.Note;
import edu.harvard.hul.ois.mets.Role;
import edu.harvard.hul.ois.mets.StructMap;
import edu.harvard.hul.ois.mets.Type;
import edu.harvard.hul.ois.mets.helper.MetsException;
import edu.harvard.hul.ois.mets.helper.MetsWriter;
import edu.harvard.hul.ois.mets.helper.PCData;
import model.RestoredFile;
import model.Scenario;
import view.GUIPanel;

/**
 * This plug-In uses the harvard METS implementation to create simple METS SIPs.
 */
public class MetsSubmissionInformationPackage extends AbstractAlgorithm {
    private final JRadioButton truePersonButton = new JRadioButton("true");
    private final JRadioButton falsePersonButton = new JRadioButton("false");
    private final JTextField personField = new JTextField(20);
    private final JRadioButton tarButton = new JRadioButton("tar");
    private final JRadioButton zipButton = new JRadioButton("zip");

    public MetsSubmissionInformationPackage() {
	createConfigurationPanel();
    }

    private void createConfigurationPanel() {
	panel = new GUIPanel();
	panel.setLayout(new GridBagLayout());
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.anchor = GridBagConstraints.NORTHWEST;
	panel.add(new JLabel("<html><h2>Add information to mets file</h2></html>"), constraints);
	constraints.gridy++;
	panel.add(new JLabel("Add person: "), constraints);
	constraints.gridx++;
	ButtonGroup buttonGroup = new ButtonGroup();
	buttonGroup.add(truePersonButton);
	panel.add(truePersonButton, constraints);
	constraints.gridx++;
	buttonGroup.add(falsePersonButton);
	falsePersonButton.setSelected(true);
	panel.add(falsePersonButton, constraints);
	constraints.gridy++;
	constraints.gridx = 0;
	panel.add(new JLabel("Name:"), constraints);
	constraints.gridx++;
	constraints.gridwidth = 2;
	panel.add(personField, constraints);
	constraints.gridwidth = 1;
	constraints.gridx = 0;
	constraints.gridy++;
	panel.add(new JLabel("Choose archiving technique:"), constraints);
	constraints.gridx++;
	ButtonGroup archivingGroup = new ButtonGroup();
	archivingGroup.add(tarButton);
	archivingGroup.add(zipButton);
	zipButton.setSelected(true);
	panel.add(zipButton, constraints);
	constraints.gridy++;
	panel.add(tarButton, constraints);
	constraints.gridx = 0;
	constraints.gridy++;
    }

    @SuppressWarnings("unchecked")
    @Override
    public File encapsulate(File carrier, List<File> userPayloadList) throws IOException {
	List<File> payloadList = new ArrayList<File>();
	payloadList.addAll(userPayloadList);
	Mets metsDocument = new Mets();
	metsDocument.setID(carrier.getName() + "_SIP");
	metsDocument.setLABEL("PeriCAT information package");
	metsDocument.setTYPE("digital object and metadata files");
	// HEADER:
	Agent periPack = new Agent();
	periPack.setID("peripack agent");
	periPack.setROLE(Role.PRESERVATION);
	periPack.setOTHERTYPE("SOFTWARE");
	Name name = new Name();
	PCData nameString = new PCData();
	nameString.add("peripack");
	name.getContent().add(nameString);
	periPack.getContent().add(name);
	Note note = new Note();
	PCData noteString = new PCData();
	noteString.add(TOOL_DESCRIPTION);
	note.getContent().add(noteString);
	periPack.getContent().add(note);
	MetsHdr header = new MetsHdr();
	header.setID("mets header");
	header.setCREATEDATE(new Date());
	header.setLASTMODDATE(new Date());
	header.setRECORDSTATUS("complete");
	header.getContent().add(periPack);
	if (truePersonButton.isSelected()) {
	    Agent person = new Agent();
	    person.setID("peripack user");
	    person.setROLE(Role.CREATOR);
	    person.setTYPE(Type.INDIVIDUAL);
	    Name personName = new Name();
	    PCData personNameString = new PCData();
	    personNameString.add(personField.getText());
	    personName.getContent().add(personNameString);
	    person.getContent().add(personName);
	    header.getContent().add(person);
	}
	metsDocument.getContent().add(header);
	// FILE SECTION:
	FileSec fileSection = new FileSec();
	FileGrp carrierGroup = new FileGrp();
	carrierGroup.setID("carrier files");
	carrierGroup.setUSE("digital object");
	// carrier div for structural map:
	Div carrierDiv = new Div();
	carrierDiv.setID("carrier list");
	carrierDiv.setTYPE("carrier files");
	// back to file section:
	edu.harvard.hul.ois.mets.File metsCarrier = new edu.harvard.hul.ois.mets.File();
	metsCarrier.setID(carrier.getAbsolutePath());
	metsCarrier.setGROUPID("carrier");
	metsCarrier.setCHECKSUM("" + FileUtils.checksumCRC32(carrier));
	metsCarrier.setMIMETYPE(Files.probeContentType(carrier.toPath()));
	metsCarrier.setOWNERID(Files.getOwner(carrier.toPath(), LinkOption.NOFOLLOW_LINKS).toString());
	metsCarrier.setSIZE(Files.size(carrier.toPath()));
	metsCarrier.setUSE(Files.getPosixFilePermissions(carrier.toPath(), LinkOption.NOFOLLOW_LINKS).toString());
	FLocat fileLocation = new FLocat();
	fileLocation.setXlinkHref(carrier.getAbsolutePath());
	fileLocation.setLOCTYPE(Loctype.OTHER);
	fileLocation.setOTHERLOCTYPE("system file path");
	fileLocation.setXlinkTitle("original file path of the carrier");
	metsCarrier.getContent().add(fileLocation);
	carrierGroup.getContent().add(metsCarrier);
	// add structural map information:
	Fptr carrierFilePointer = new Fptr();
	carrierFilePointer.setFILEID(carrier.getAbsolutePath());
	carrierDiv.getContent().add(carrierFilePointer);
	fileSection.getContent().add(carrierGroup);
	FileGrp payloadGroup = new FileGrp();
	payloadGroup.setID("payload files");
	payloadGroup.setUSE("metadata");

	// payload div for structural map:
	Div payloadDiv = new Div();
	payloadDiv.setID("payload list");
	payloadDiv.setTYPE("payload files");
	// back to file section:
	for (File payload : payloadList) {
	    edu.harvard.hul.ois.mets.File metsPayload = new edu.harvard.hul.ois.mets.File();
	    metsPayload.setID(payload.getAbsolutePath());
	    metsPayload.setGROUPID("payload");
	    metsPayload.setCHECKSUM(DigestUtils.md5Hex(new FileInputStream(payload)));
	    metsPayload.setCHECKSUMTYPE(Checksumtype.MD5);
	    metsPayload.setMIMETYPE(Files.probeContentType(payload.toPath()));
	    metsPayload.setOWNERID(Files.getOwner(payload.toPath(), LinkOption.NOFOLLOW_LINKS).toString());
	    metsPayload.setSIZE(Files.size(payload.toPath()));
	    metsPayload.setUSE(Files.getPosixFilePermissions(payload.toPath(), LinkOption.NOFOLLOW_LINKS).toString());
	    FLocat fileLocation2 = new FLocat();
	    fileLocation2.setXlinkHref(payload.getAbsolutePath());
	    fileLocation2.setLOCTYPE(Loctype.OTHER);
	    fileLocation2.setOTHERLOCTYPE("system file path");
	    fileLocation2.setXlinkTitle("original file path of the payload");
	    metsPayload.getContent().add(fileLocation2);
	    payloadGroup.getContent().add(metsPayload);
	    // add structural map information:
	    Fptr payloadFilePointer = new Fptr();
	    payloadFilePointer.setFILEID(payload.getAbsolutePath());
	    payloadDiv.getContent().add(payloadFilePointer);
	}
	fileSection.getContent().add(payloadGroup);
	metsDocument.getContent().add(fileSection);
	// STRUCTURAL MAP:
	StructMap structuralMap = new StructMap();
	structuralMap.setID("structural map");
	Div encapsulatedFiles = new Div();
	encapsulatedFiles.setID("peripack files");
	encapsulatedFiles.setTYPE("encapsulated files");
	structuralMap.getContent().add(encapsulatedFiles);
	encapsulatedFiles.getContent().add(carrierDiv);
	encapsulatedFiles.getContent().add(payloadDiv);
	metsDocument.getContent().add(structuralMap);
	File metsFile = new File(OUTPUT_DIRECTORY + "mets.xml");
	FileOutputStream outputStream = new FileOutputStream(metsFile);
	try {
	    metsDocument.write(new MetsWriter(outputStream));
	} catch (MetsException e) {
	}
	outputStream.close();
	payloadList.add(metsFile);
	File outputFile = null;
	if (zipButton.isSelected()) {
	    outputFile = new ZipPackaging().encapsulate(carrier, payloadList);

	} else if (tarButton.isSelected()) {
	    outputFile = new TarPackaging().encapsulate(carrier, payloadList);
	}
	metsFile.delete();
	return outputFile;
    }

    @Override
    protected List<RestoredFile> restore(File outputFile) throws IOException {
	List<RestoredFile> restoredFiles = new ArrayList<RestoredFile>();
	List<String> zipExtension = new ArrayList<String>();
	zipExtension.add("zip");
	SuffixFileFilter zipFilter = new SuffixFileFilter(zipExtension);
	List<String> tarExtension = new ArrayList<String>();
	tarExtension.add("tar");
	SuffixFileFilter tarFilter = new SuffixFileFilter(tarExtension);
	if (zipFilter.accept(outputFile)) {
	    restoredFiles = new ZipPackaging().restore(outputFile);
	} else if (tarFilter.accept(outputFile)) {
	    restoredFiles = new TarPackaging().restore(outputFile);
	}
	for (RestoredFile file : restoredFiles) {
	    file.algorithm = this;
	}
	return restoredFiles;
    }

    @Override
    public String getDescription() {
	return "Creates an METS SIP (Submission Information Package).\n"
		+ "The package files are encapsulated in a zip or tar archive file.\n\n"
		+ "This is a very basic METS wrapper around the carrier and payload files. Note that"
		+ " the METS standard provides a lot more configuration possibilities than used for "
		+ "the PeriCAT created packages. We suggest to use a more specialised tool, if you need"
		+ " more complex METS files.\n\n"
		+ "For more information about METS see: http://www.loc.gov/standards/mets/ :\n"
		+ "The METS schema is a standard for encoding descriptive, administrative, "
		+ "and structural metadata regarding objects within a digital library, expressed "
		+ "using the XML schema language of the World Wide Web Consortium. The standard "
		+ "is maintained in the Network Development and MARC Standards Office of the Library "
		+ "of Congress, and is being developed as an initiative of the Digital Library Federation.";
    }

    @Override
    Scenario defineScenario() {
	Scenario scenario = new Scenario("METS packaging scenario");
	scenario.description = "This is the ideal scenario for creating a METS Submission Information Package.";
	scenario.setCriterionValue(ENCAPSULATION_METHOD, PACKAGING);
	scenario.setCriterionValue(VISIBILITY, VISIBLE);
	scenario.setCriterionValue(DETECTABILITY, DETECTABLE);
	scenario.setCriterionValue(CARRIER_RESTORABILITY, YES);
	scenario.setCriterionValue(PAYLOAD_RESTORABILITY, YES);
	scenario.setCriterionValue(CARRIER_PROCESSABILITY, NO);
	scenario.setCriterionValue(PAYLOAD_ACCESSIBILITY, NO);
	scenario.setCriterionValue(ENCRYPTION, NO);
	scenario.setCriterionValue(COMPRESSION, YES);
	scenario.setCriterionValue(VELOCITY, NO);
	scenario.setCriterionValue(STANDARDS, YES);
	return scenario;
    }

    @Override
    SuffixFileFilter configureCarrierFileFilter() {
	return new AcceptAllFilter();
    }

    @Override
    SuffixFileFilter configurePayloadFileFilter() {
	return new AcceptAllFilter();
    }

    @Override
    SuffixFileFilter configureDecapsulationFileFilter() {
	List<String> supportedFileFormats = new ArrayList<String>();
	supportedFileFormats.add("zip");
	supportedFileFormats.add("tar");
	return new SuffixFileFilter(supportedFileFormats);
    }

    @Override
    public String getName() {
	return "METS SIP";
    }

    @Override
    public boolean fulfilledTechnicalCriteria(File carrier, List<File> payloadList) {
	return carrier.isFile() || payloadList.size() > 0;
    }
}
