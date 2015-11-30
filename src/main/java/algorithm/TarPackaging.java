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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2Utils;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import model.RestoredFile;
import model.Scenario;
import view.GUIPanel;

/**
 * Tar packaging is implemented using the apache commons library.
 */
public class TarPackaging extends AbstractAlgorithm {
    public TarPackaging() {
	panel = new TarConfigurationPanel();
    }

    @Override
    public File encapsulate(File carrier, List<File> payloadFiles) throws IOException {
	String outputName = getOutputFileName(carrier);
	int dotIndex = outputName.lastIndexOf('.');
	if (dotIndex > 0) {
	    if (compression()) {
		if (gzip()) {
		    outputName = outputName.substring(0, dotIndex) + ".tgz";
		} else if (bzip()) {
		    outputName = outputName.substring(0, dotIndex) + ".tbz2";
		}
	    } else {
		outputName = outputName.substring(0, dotIndex) + ".tar";
	    }
	}
	File tarFile = new File(outputName);
	FileOutputStream outputStream = new FileOutputStream(tarFile);
	if (compression()) {
	    if (gzip()) {
		TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(
			new GzipCompressorOutputStream(new BufferedOutputStream(outputStream)));
		archiveFile(carrier, tarOutputStream);
		for (File payload : payloadFiles) {
		    archiveFile(payload, tarOutputStream);
		}
		tarOutputStream.close();
	    } else if (bzip()) {
		TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(
			new BZip2CompressorOutputStream(new BufferedOutputStream(outputStream)));
		archiveFile(carrier, tarOutputStream);
		for (File payload : payloadFiles) {
		    archiveFile(payload, tarOutputStream);
		}
		tarOutputStream.close();
	    }
	} else {
	    TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(new BufferedOutputStream(outputStream));
	    archiveFile(carrier, tarOutputStream);
	    for (File payload : payloadFiles) {
		archiveFile(payload, tarOutputStream);
	    }
	    tarOutputStream.close();
	}
	outputStream.close();
	return tarFile;
    }

    private void archiveFile(File carrier, TarArchiveOutputStream tarOutputStream)
	    throws IOException, FileNotFoundException {
	TarArchiveEntry tarEntry = new TarArchiveEntry(carrier, carrier.getName());
	tarOutputStream.putArchiveEntry(tarEntry);
	FileInputStream inputStream = new FileInputStream(carrier);
	IOUtils.copy(inputStream, tarOutputStream);
	tarOutputStream.closeArchiveEntry();
	inputStream.close();
    }

    @Override
    protected List<RestoredFile> restore(File tarFile) throws IOException {
	List<RestoredFile> extractedFiles = new ArrayList<RestoredFile>();
	FileInputStream inputStream = new FileInputStream(tarFile);
	if (GzipUtils.isCompressedFilename(tarFile.getName())) {
	    TarArchiveInputStream tarInputStream = new TarArchiveInputStream(
		    new GzipCompressorInputStream(new BufferedInputStream(inputStream)));
	    TarArchiveEntry entry = tarInputStream.getNextTarEntry();
	    while (entry != null) {
		RestoredFile outputFile = new RestoredFile(RESTORED_DIRECTORY + entry.getName());
		if (entry.isDirectory()) {
		    if (!outputFile.exists()) {
			outputFile.mkdirs();
		    }
		} else if (entry.isFile()) {
		    FileOutputStream outputFileStream = new FileOutputStream(outputFile);
		    IOUtils.copy(tarInputStream, outputFileStream);
		    outputFileStream.close();
		}
		extractedFiles.add(outputFile);
		entry = tarInputStream.getNextTarEntry();
	    }
	    tarInputStream.close();
	} else if (BZip2Utils.isCompressedFilename(tarFile.getName())) {
	    TarArchiveInputStream tarInputStream = new TarArchiveInputStream(
		    new BZip2CompressorInputStream(new BufferedInputStream(inputStream)));
	    TarArchiveEntry entry = tarInputStream.getNextTarEntry();
	    while (entry != null) {
		RestoredFile outputFile = new RestoredFile(RESTORED_DIRECTORY + entry.getName());
		if (entry.isDirectory()) {
		    if (!outputFile.exists()) {
			outputFile.mkdirs();
		    }
		} else if (entry.isFile()) {
		    FileOutputStream outputFileStream = new FileOutputStream(outputFile);
		    IOUtils.copy(tarInputStream, outputFileStream);
		    outputFileStream.close();
		}
		extractedFiles.add(outputFile);
		entry = tarInputStream.getNextTarEntry();
	    }
	    tarInputStream.close();
	} else {
	    TarArchiveInputStream tarInputStream = new TarArchiveInputStream(new BufferedInputStream(inputStream));
	    TarArchiveEntry entry = tarInputStream.getNextTarEntry();
	    while (entry != null) {
		RestoredFile outputFile = new RestoredFile(RESTORED_DIRECTORY + entry.getName());
		if (entry.isDirectory()) {
		    if (!outputFile.exists()) {
			outputFile.mkdirs();
		    }
		} else if (entry.isFile()) {
		    FileOutputStream outputFileStream = new FileOutputStream(outputFile);
		    IOUtils.copy(tarInputStream, outputFileStream);
		    outputFileStream.close();
		}
		extractedFiles.add(outputFile);
		entry = tarInputStream.getNextTarEntry();
	    }
	    tarInputStream.close();
	}
	for (RestoredFile file : extractedFiles) {
	    file.algorithm = this;
	    file.checksumValid = true;
	    file.restorationNote = "The algorithm doesn't alter these files, so they are brought to its original state. No checksum validation executed.";
	    // All files in a .tar file are payload files:
	    file.wasPayload = true;
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
	return "Creates a tar archive that includes all carrier and payload files."
		+ "\n\nhttp://en.wikipedia.org/wiki/Tar_%28computing%29 :\n"
		+ "In computing, tar is a computer software utility for collecting "
		+ "many files into one archive file for distribution or backup purposes. "
		+ "The name is derived from tape archive, as it was originally developed "
		+ "to write data to sequential I/O devices. The archive data sets created "
		+ "by tar contain various file system parameters, such as time stamps, "
		+ "ownership, file access permissions, and directory organization. "
		+ "The file structure to store this information was later standardized in "
		+ "POSIX.1-1988 and later POSIX.1-2001 and became a format supported by most "
		+ "modern file archiving systems.";
    }

    @Override
    Scenario defineScenario() {
	Scenario scenario = new Scenario("Tar packaging scenario");
	scenario.description = "This is the ideal scenario for using the tar packaging algorithm.";
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
	supportedFileFormats.add("tar");
	supportedFileFormats.add("gz");
	supportedFileFormats.add("tgz");
	supportedFileFormats.add("tbz2");
	supportedFileFormats.add("tbz");
	supportedFileFormats.add("bz2");
	return new SuffixFileFilter(supportedFileFormats);
    }

    @Override
    public String getName() {
	return "Tar packaging";
    }

    @Override
    public boolean fulfilledTechnicalCriteria(File carrier, List<File> payloadList) {
	return true;
    }

    private boolean compression() {
	return ((TarConfigurationPanel) panel).trueCompressionButton.isSelected();
    }

    private boolean gzip() {
	return ((TarConfigurationPanel) panel).gzipCompressionButton.isSelected();
    }

    private boolean bzip() {
	return ((TarConfigurationPanel) panel).bzip2CompressionButton.isSelected();
    }

    class TarConfigurationPanel extends GUIPanel {
	private static final long serialVersionUID = 1L;
	protected final JRadioButton trueCompressionButton = new JRadioButton("true");
	protected final JRadioButton falseCompressionButton = new JRadioButton("false");
	protected final JRadioButton gzipCompressionButton = new JRadioButton("Gzip (.tgz)");
	protected final JRadioButton bzip2CompressionButton = new JRadioButton("BZip2 (.tbz2)");

	public TarConfigurationPanel() {
	    add(new JLabel("<html><h2>Tar archiving options</h2></html>"), constraints);
	    constraints.gridy++;
	    add(new JLabel("Compress the archive:"), constraints);
	    constraints.gridx++;
	    ButtonGroup buttonGroupCompression = new ButtonGroup();
	    buttonGroupCompression.add(trueCompressionButton);
	    buttonGroupCompression.add(falseCompressionButton);
	    trueCompressionButton.setSelected(true);
	    add(trueCompressionButton, constraints);
	    constraints.gridx++;
	    add(falseCompressionButton, constraints);
	    constraints.gridx = 0;
	    constraints.gridy++;
	    add(new JLabel("If compression is enable, choose a technique:"), constraints);
	    constraints.gridx++;
	    ButtonGroup optionsGroup = new ButtonGroup();
	    optionsGroup.add(gzipCompressionButton);
	    add(gzipCompressionButton, constraints);
	    constraints.gridy++;
	    optionsGroup.add(bzip2CompressionButton);
	    add(bzip2CompressionButton, constraints);
	    constraints.gridy++;
	    gzipCompressionButton.setSelected(true);
	    constraints.gridx = 0;
	}
    }
}
