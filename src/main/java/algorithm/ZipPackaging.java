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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import model.RestoredFile;
import model.Scenario;
/**
 * Zip packaging is implemented using the apache commons library.
 */
public class ZipPackaging extends AbstractAlgorithm {

    @Override
    public File encapsulate(File carrier, List<File> payloadList) throws IOException {
	String outputName = getOutputFileName(carrier);
	int dotIndex = outputName.lastIndexOf('.');
	if (dotIndex > 0) {
	    outputName = outputName.substring(0, dotIndex) + ".zip";
	}
	File zipFile = new File(outputName);
	try {
	    FileOutputStream outputStream = new FileOutputStream(zipFile);
	    ArchiveOutputStream archiveOutputStream = new ArchiveStreamFactory()
		    .createArchiveOutputStream(ArchiveStreamFactory.ZIP, outputStream);
	    archiveFile(archiveOutputStream, carrier);
	    for (File payload : payloadList) {
		archiveFile(archiveOutputStream, payload);
	    }
	    archiveOutputStream.close();
	    outputStream.close();
	} catch (ArchiveException e) {
	}
	return zipFile;
    }

    private void archiveFile(ArchiveOutputStream archiveOutputStream, File file)
	    throws IOException, FileNotFoundException {
	archiveOutputStream.putArchiveEntry(new ZipArchiveEntry(file.getName()));
	FileInputStream inputStream = new FileInputStream(file);
	IOUtils.copy(inputStream, archiveOutputStream);
	archiveOutputStream.closeArchiveEntry();
	inputStream.close();
    }

    @Override
    protected List<RestoredFile> restore(File zipFile) throws IOException {
	List<RestoredFile> extractedFiles = new ArrayList<RestoredFile>();
	try {
	    InputStream inputStream = new FileInputStream(zipFile);
	    ArchiveInputStream archiveInputStream = new ArchiveStreamFactory()
		    .createArchiveInputStream(ArchiveStreamFactory.ZIP, inputStream);
	    ZipArchiveEntry entry = (ZipArchiveEntry) archiveInputStream.getNextEntry();
	    while (entry != null) {
		RestoredFile extractedFile = new RestoredFile(RESTORED_DIRECTORY + entry.getName());
		OutputStream outputStream = new FileOutputStream(extractedFile);
		IOUtils.copy(archiveInputStream, outputStream);
		outputStream.close();
		extractedFiles.add(extractedFile);
		entry = (ZipArchiveEntry) archiveInputStream.getNextEntry();
	    }
	    archiveInputStream.close();
	    inputStream.close();
	} catch (ArchiveException e) {
	}
	for (RestoredFile file : extractedFiles) {
	    file.algorithm = this;
	    file.checksumValid = true;
	    file.restorationNote = "The algorithm doesn't alter these files, so they are brought to its original state. No checksum validation executed.";
	    // every file in a .zip archive is a payload file!
	    file.wasPayload = true;
	    /*
	     * Checksum validation isn't executed, because the PayloadSegment
	     * class isn't used and therewith the checksums of the original
	     * files are unknown.
	     */
	    for (RestoredFile relatedFile : extractedFiles) {
		if (file != relatedFile) {
		    file.relatedFiles.add(relatedFile);
		}
	    }
	}
	return extractedFiles;
    }

    @Override
    public String getDescription() {
	return "Creates a zip archive that includes all carrier and payload files. Zip64 is used if the file sizes exceed 4GB."
		+ "\n\nhttps://en.wikipedia.org/wiki/Zip_(file_format) :\n"
		+ ".ZIP is an archive file format that supports lossless data compression. A .ZIP file may "
		+ "contain one or more files or folders that may have been compressed. The .ZIP file format "
		+ "permits a number of compression algorithms. The format was originally created in 1989 by "
		+ "Phil Katz, and was first implemented in PKWARE, Inc.'s PKZIP utility, as a replacement "
		+ "for the previous ARC compression format by Thom Henderson. The .ZIP format is now supported "
		+ "by many software utilities other than PKZIP. Microsoft has included built-in .ZIP support "
		+ "(under the name compressed folders) in versions of Microsoft Windows since 1998. Apple has "
		+ "included built-in .ZIP support in Mac OS X 10.3 (via BOMArchiveHelper, now Archive Utility) "
		+ "and later. Most free operating systems have built in support for .ZIP in similar manners to "
		+ "Windows and Mac OS X."
		+ "\n\n.ZIP files generally use the file extensions .zip or .ZIP and the MIME media "
		+ "type application/zip. ZIP is used as a base file format by many programs, "
		+ "usually under a different name. When navigating a file system via a user "
		+ "interface, graphical icons representing .ZIP files often appear as a document or "
		+ "other object prominently featuring a zipper.";
    }

    @Override
    Scenario defineScenario() {
	Scenario scenario = new Scenario("Zip packaging scenario");
	scenario.description = "This is the ideal scenario for using the zip packaging algorithm.";
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
	return new SuffixFileFilter(supportedFileFormats);
    }

    @Override
    public String getName() {
	return "Zip packaging";
    }

    @Override
    public boolean fulfilledTechnicalCriteria(File carrier, List<File> payloadList) {
	return true;
    }
}
