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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import edu.mit.lib.bagit.Bag;
import edu.mit.lib.bagit.Filler;
import edu.mit.lib.bagit.Loader;
import model.RestoredFile;
import model.Scenario;

/**
 * This Plug-In uses the BagIt implemenation from Richard Rodgers for integrating BagIt into PeriCAT.
 */
public class BagItPackaging extends AbstractAlgorithm {

    public BagItPackaging() {
    }

    @Override
    Scenario defineScenario() {
	Scenario scenario = new Scenario("BagIt scenario");
	scenario.description = "This is the ideal scenario to use the BagIt algorithm.";
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
	// All file formats are supported
	return new AcceptAllFilter();
    }

    @Override
    SuffixFileFilter configurePayloadFileFilter() {
	// All file formats are supported
	return new AcceptAllFilter();
    }

    @Override
    SuffixFileFilter configureDecapsulationFileFilter() {
	List<String> supportedFileFormats = new ArrayList<String>();
	supportedFileFormats.add("_BAG.zip");
	return new SuffixFileFilter(supportedFileFormats);
    }

    @Override
    public File encapsulate(File carrier, List<File> payload) throws IOException {
	String carrierName = carrier.getName();
	String bagDirectoryName = OUTPUT_DIRECTORY + carrierName + "_BAG";
	FileUtils.forceMkdir(new File(bagDirectoryName));
	File bagDirectory = new File(bagDirectoryName);
	new File(bagDirectory, "manifest-md5.txt");// manifest
	new File(bagDirectory, "tagmanifest-md5.txt"); // metadata manifest
	new File(bagDirectory, "data"); // carrier directory
	Filler filler = new Filler(bagDirectory);
	// add the DO file:
	InputStream inputStream = new FileInputStream("" + carrier);
	filler = filler.payload("" + carrier.getName(), inputStream);
	// add all metadata files:
	for (File payloadFile : payload) {
	    InputStream inputStream2 = new FileInputStream("" + payloadFile);
	    filler = filler.tag("" + payloadFile.getName(), inputStream2);
	}
	return filler.toPackage();
    }

    @Override
    public List<RestoredFile> restore(File data) throws IOException {
	// BagIt won't override an existing directory, so delete:
	FileUtils.deleteDirectory(new File(RESTORED_DIRECTORY + data.getName().split("_")[0] + "_BAG"));
	File restoredData = new File(RESTORED_DIRECTORY + data.getName());
	// Move bag to restoration directory:
	if (!data.getPath().equals(restoredData.getPath())) {
	    FileUtils.copyFile(data, restoredData);
	}
	List<RestoredFile> restoredFiles = new ArrayList<RestoredFile>();
	Loader loader = new Loader(restoredData);
	Bag bag = loader.load();
	// get carrier / digital object files:
	Map<String, String> carriersContents = bag.payloadManifest();
	for (Entry<String, String> entry : carriersContents.entrySet()) {
	    String carrierName = entry.getKey();
	    File carrierFile = new File(RESTORED_DIRECTORY + bag.bagName() + File.separator + carrierName);
	    RestoredFile restoredCarrier = new RestoredFile(RESTORED_DIRECTORY + carrierFile.getName());
	    restoredCarrier.delete();// BagIt won't override!
	    FileUtils.moveFile(carrierFile, restoredCarrier);
	    restoredCarrier.algorithm = this;
	    restoredCarrier.wasCarrier = true;
	    restoredFiles.add(restoredCarrier);
	}
	// get payload / metadata files:
	Map<String, String> payloadContents = bag.tagManifest();
	payloadContents.remove("bagit.txt");
	payloadContents.remove("bag-info.txt");
	payloadContents.remove("manifest-md5.txt");
	for (Entry<String, String> entry : payloadContents.entrySet()) {
	    String payloadName = entry.getKey();
	    File payloadFile = new File(RESTORED_DIRECTORY + bag.bagName() + File.separator + payloadName);
	    RestoredFile restoredPayload = new RestoredFile(RESTORED_DIRECTORY + payloadFile.getName());
	    restoredPayload.delete();// BagIt won't override existing files!
	    FileUtils.moveFile(payloadFile, restoredPayload);
	    restoredPayload.wasPayload = true;
	    restoredFiles.add(restoredPayload);
	}
	for (RestoredFile file : restoredFiles) {
	    file.algorithm = this;
	    file.checksumValid = true;
	    file.restorationNote = "The algorithm doesn't alter these files, so they are brought to its original state. No checksum validation executed.";
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
	return "Bag-It packaging";
    }

    @Override
    public String getDescription() {
	String description = "This is a packaging algorithm that uses the BagIt hierarchical file "
		+ "packaging format. The output is a .zip archive file. All encapsulated files can "
		+ "be restored correctly in every bit." + "\n\nhttps://en.wikipedia.org/wiki/BagIt :\n"
		+ "BagIt is a hierarchical file packaging format designed to support disk-based storage "
		+ "and network transfer of arbitrary digital content. A bag consists of a payload (the "
		+ "arbitrary content) and tags, which are metadata files intended to document the storage "
		+ "and transfer of the bag. A required tag file contains a manifest listing every file in "
		+ "the payload together with its corresponding checksum. The name, BagIt, is inspired by the "
		+ "enclose and deposit method,[1] sometimes referred to as bag it and tag it."
		+ "Bags are ideal for digital content normally kept as a collection of files. They are also "
		+ "well-suited to the export, for archival purposes, of content normally kept in database "
		+ "structures that receiving parties are unlikely to support. Relying on cross-platform "
		+ "(Windows and Unix) filesystem naming conventions, a bag's payload may include any number "
		+ "of directories and sub-directories (folders and sub-folders). A bag can specify payload "
		+ "content indirectly via a fetch.txt file that lists URLs for content that can be fetched "
		+ "over the network to complete the bag; simple parallelization (e.g. running 10 instances of "
		+ "Wget) can exploit this feature to transfer large bags very quickly. Benefits of bags " + "include "
		+ "\nWide adoption in digital libraries (e.g., the United States' Library of Congress)."
		+ "\nEasy to implement using ubiquitous and ordinary filesystem tools."
		+ "\nContent that originates as files need only be copied to the payload directory."
		+ "\nCompared to XML wrapping, content need not be encoded, saving time and storage space."
		+ "\nReceived content is ready-to-go in a familiar filesystem tree."
		+ "\nEasy to implement fast network transfer by running ordinary transfer tools in parallel.";
	return description;
    }

    @Override
    public boolean fulfilledTechnicalCriteria(File carrier, List<File> payloadList) {
	return true;
    }
}
