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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;

import model.RestoredFile;
import model.Scenario;

/**
 * This class uses the pdfbox library to attach files to pdfs.
 *
 * Attaches the payload file as file attachment to a pdf.
 */
public class PDFFileAttacher extends AbstractAlgorithm {

    /**
     * Note: This algorithm doesn't use the payload segment to keep the payload
     * file originally, because it will be displayed as normal file in the users
     * pdf viewer.
     */
    @Override
    public File encapsulate(File carrier, List<File> payloadList) throws IOException {
	File outputFile = copyCarrier(carrier);
	attachAll(outputFile, payloadList);
	return outputFile;
    }

    private void attachAll(File outputFile, List<File> payloadList) throws IOException {
	PDDocument document = PDDocument.load(outputFile);
	List<PDComplexFileSpecification> fileSpecifications = getFileSpecifications(document, payloadList);
	PDDocumentNameDictionary namesDictionary = new PDDocumentNameDictionary(document.getDocumentCatalog());
	PDEmbeddedFilesNameTreeNode filesTree = namesDictionary.getEmbeddedFiles();
	filesTree = new PDEmbeddedFilesNameTreeNode();
	Map<String, COSObjectable> fileMap = new HashMap<String, COSObjectable>();
	for (int i = 0; i < fileSpecifications.size(); i++) {
	    fileMap.put("PericlesMetadata-" + i, fileSpecifications.get(i));
	}
	filesTree.setNames(fileMap);
	namesDictionary.setEmbeddedFiles(filesTree);
	document.getDocumentCatalog().setNames(namesDictionary);
	try {
	    document.save(outputFile);
	} catch (COSVisitorException e) {
	}
	document.close();
    }

    private List<PDComplexFileSpecification> getFileSpecifications(PDDocument document, List<File> payloadList)
	    throws IOException {
	List<PDComplexFileSpecification> fileSpecifications = new ArrayList<PDComplexFileSpecification>();
	for (File payload : payloadList) {
	    FileInputStream inputStream = new FileInputStream(payload);
	    PDEmbeddedFile embeddedFile = new PDEmbeddedFile(document, inputStream);
	    embeddedFile.setCreationDate(new GregorianCalendar());
	    PDComplexFileSpecification fileSpecification = new PDComplexFileSpecification();
	    fileSpecification.setFile(payload.toPath().toString());
	    fileSpecification.setEmbeddedFile(embeddedFile);
	    fileSpecifications.add(fileSpecification);
	}
	return fileSpecifications;
    }

    private File copyCarrier(File carrier) throws IOException, FileNotFoundException {
	File outputFile = new File(getOutputFileName(carrier));
	byte[] carrierBytes = FileUtils.readFileToByteArray(carrier);
	FileOutputStream outputStream = new FileOutputStream(outputFile);
	outputStream.write(carrierBytes);
	outputStream.close();
	return outputFile;
    }

    @Override
    public List<RestoredFile> restore(File originalPdf) throws IOException {
	RestoredFile copiedPdf = getRestoredCarrier(originalPdf);
	List<RestoredFile> restoredFiles = new ArrayList<RestoredFile>();
	PDDocument document = PDDocument.load(copiedPdf);
	PDDocumentNameDictionary namesDictionary = new PDDocumentNameDictionary(document.getDocumentCatalog());
	PDEmbeddedFilesNameTreeNode filesTree = namesDictionary.getEmbeddedFiles();
	if (filesTree != null) {
	    int i = 0;
	    while (true) {
		PDComplexFileSpecification fileSpecification = (PDComplexFileSpecification) filesTree
			.getValue("PericlesMetadata-" + i);
		if (fileSpecification == null) {
		    break;
		}
		File oldAttachedFile = new File(fileSpecification.getFile());
		RestoredFile restoredPayload = new RestoredFile(RESTORED_DIRECTORY + oldAttachedFile.getName());
		PDEmbeddedFile embeddedFile = fileSpecification.getEmbeddedFile();
		InputStream inputStream = embeddedFile.createInputStream();
		FileOutputStream outputStream = new FileOutputStream(restoredPayload);
		IOUtils.copy(inputStream, outputStream);
		removeBuggyLineEnding(restoredPayload);
		restoredPayload.wasPayload = true;
		restoredPayload.checksumValid = true;
		restoredPayload.restorationNote = "Checksum wasn't calculated, because this algorithm isn't using restoration metadata. The original payload file survives the encapsulation with this algorithm.";
		restoredFiles.add(restoredPayload);
		i++;
	    }
	}
	document.close();
	copiedPdf.wasCarrier = true;
	copiedPdf.checksumValid = false;
	copiedPdf.restorationNote = "Checksum can't be valid, because attached payload files can't be removed from carrier.";
	restoredFiles.add(copiedPdf);
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

    /**
     * PDFBox adds a new line with ^M at the end of the restored payload file.
     * This method removes the buggy line.
     * 
     * @param restoredPayload
     */
    private void removeBuggyLineEnding(File restoredPayload) throws IOException {
	byte[] data = FileUtils.readFileToByteArray(restoredPayload);
	FileUtils.writeByteArrayToFile(restoredPayload, Arrays.copyOfRange(data, 0, data.length - 2), false);
    }

    @Override
    Scenario defineScenario() {
	Scenario scenario = new Scenario("PDF file attaching scenario");
	scenario.description = "This is the ideal scenario for using the PDF file attacher algorithm.";
	scenario.setCriterionValue(ENCAPSULATION_METHOD, EMBEDDING);
	scenario.setCriterionValue(VISIBILITY, VISIBLE);
	scenario.setCriterionValue(DETECTABILITY, DETECTABLE);
	scenario.setCriterionValue(CARRIER_RESTORABILITY, NO);
	scenario.setCriterionValue(PAYLOAD_RESTORABILITY, YES);
	scenario.setCriterionValue(CARRIER_PROCESSABILITY, YES);
	scenario.setCriterionValue(PAYLOAD_ACCESSIBILITY, YES);
	scenario.setCriterionValue(ENCRYPTION, NO);
	scenario.setCriterionValue(COMPRESSION, NO);
	scenario.setCriterionValue(VELOCITY, NO);
	scenario.setCriterionValue(STANDARDS, YES);
	return scenario;
    }

    @Override
    SuffixFileFilter configureCarrierFileFilter() {
	ArrayList<String> supportedFormats = new ArrayList<String>();
	supportedFormats.add("pdf");
	supportedFormats.add("PDF");
	return new SuffixFileFilter(supportedFormats);
    }

    @Override
    SuffixFileFilter configurePayloadFileFilter() {
	return new AcceptAllFilter();
    }

    @Override
    SuffixFileFilter configureDecapsulationFileFilter() {
	return configureCarrierFileFilter();
    }

    @Override
    public String getName() {
	return "PDF file attaching";
    }

    @Override
    public String getDescription() {
	String description = "This algorithm uses Apache PDFBox for the standard way of attaching "
		+ "any kind of files to PDF documents. If the encapsulated PDF is opened in a PDF "
		+ "reader, the reader will notify the user of the attached file, if it supports "
		+ "the view of attached files.\nThe payload file can be restored correctly in every "
		+ "bit. It seems to be that the used library doesn't support the removing of the attached "
		+ "files from the carrier, so the carrier can not be restored to its original state with"
		+ "the PeriCAT tool, but with PDF editors which support this feature.\n"
		+ "Don't use this algorithm on PDF documents that already keep PERICLES metadata!";
	return description;
    }

    /**
     * The dataset shouldn't contain more than one carrier
     */

    @Override
    public boolean fulfilledTechnicalCriteria(File carrier, List<File> payloadList) {
	return carrier.isFile() && payloadList.size() > 0;
    }
}
