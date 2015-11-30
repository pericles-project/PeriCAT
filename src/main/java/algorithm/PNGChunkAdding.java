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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.ChunkCopyBehaviour;
import ar.com.hjg.pngj.chunks.ChunkPredicate;
import ar.com.hjg.pngj.chunks.ChunksList;
import ar.com.hjg.pngj.chunks.PngChunk;
import ar.com.hjg.pngj.chunks.PngChunkTEXT;
import model.PayloadSegment;
import model.RestoredFile;
import model.Scenario;

/**
 * This technique uses the PNGJ library to embed additional information chunks into PNG files.
 *
 * Note: A(nimated)PNGs are not supported by PNGJ. A future implementation
 * should use the apache commons imaging library, when it's final.
 */
public class PNGChunkAdding extends AbstractAlgorithm {
    private static final String KEYWORD = "Pericles Metadata";

    @Override
    Scenario defineScenario() {
	Scenario scenario = new Scenario("PNG chunk adding scenario");
	scenario.description = "This is the ideal scenario for using the PNG chunk adding algorithm.";
	scenario.setCriterionValue(ENCAPSULATION_METHOD, EMBEDDING);
	scenario.setCriterionValue(VISIBILITY, INVISIBLE);
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
	supportedFormats.add("PNG");
	supportedFormats.add("png");
	return new SuffixFileFilter(supportedFormats);
    }

    @Override
    SuffixFileFilter configurePayloadFileFilter() {
	ArrayList<String> supportedFormats = new ArrayList<String>();
	supportedFormats.add("txt");
	supportedFormats.add("json");
	supportedFormats.add("xml");
	return new SuffixFileFilter(supportedFormats);
    }

    @Override
    SuffixFileFilter configureDecapsulationFileFilter() {
	return configureCarrierFileFilter();
    }

    /**
     * Create an ancillary TEXT chunk and add it to the carrier PNG. The text is
     * saved in the payload text file (txt, or JSON, or XML)
     * 
     * TODO: Problem - the library alters the original data chunks and uses
     * another compression. If it should be done in a way that the original
     * chunks stay untouched, an own parser has to be developed.
     */
    @Override
    public File encapsulate(File carrier, List<File> payloadList) throws IOException {
	File outputFile = getOutputFile(carrier);
	PngReader reader = new PngReader(carrier);
	PngWriter writer = new PngWriter(outputFile, reader.imgInfo, true);
	ChunksList chunkList = reader.getChunksList();
	writer.copyChunksFrom(chunkList, ChunkCopyBehaviour.COPY_ALL);
	for (File payload : payloadList) {
	    PayloadSegment payloadSegment = new PayloadSegment(carrier, payload, this);
	    byte[] payloadBytes = payloadSegment.getPayloadSegmentBytes();
	    PngChunkTEXT textChunk = new PngChunkTEXT(reader.imgInfo);
	    // (more than one with the same keyword is permissible)
	    textChunk.setKeyVal(KEYWORD, new String(payloadBytes));
	    writer.getChunksList().queue(textChunk);
	}
	for (int row = 0; row < reader.imgInfo.rows; row++) {
	    IImageLine line = reader.readRow();
	    writer.writeRow(line);
	}
	reader.end();
	writer.end();
	return outputFile;
    }

    @Override
    public List<RestoredFile> restore(File outputFile) throws IOException {
	List<RestoredFile> restoredFiles = new ArrayList<RestoredFile>();
	PngReader reader = new PngReader(outputFile);
	reader.end();
	ChunksList chunkList = reader.getChunksList();
	final List<PngChunkTEXT> periclesChunks = new ArrayList<PngChunkTEXT>();
	for (PngChunk chunk : chunkList.getById("tEXt")) {
	    PngChunkTEXT textChunk = (PngChunkTEXT) chunk;
	    if (textChunk.getKey().equals(KEYWORD)) {
		periclesChunks.add(textChunk);
		restoredFiles.add(restorePaylad(textChunk));
	    }
	}
	// create restored carrier file:
	RestoredFile restoredCarrier = null;
	if (periclesChunks.size() > 0) {
	    PayloadSegment payloadSegment = PayloadSegment.getPayloadSegment(periclesChunks.get(0).getVal().getBytes());
	    restoredCarrier = new RestoredFile(RESTORED_DIRECTORY + payloadSegment.getCarrierName());
	    restoredCarrier.originalFilePath = payloadSegment.getCarrierPath();
	    restoredCarrier.wasCarrier = true;
	    restoredCarrier.checksumValid = false;
	    restoredCarrier.restorationNote = "Checksum can't be valid, as the order and size of chunks is alterd. This is lossless, so the carrier is actually the same.";
	} else {
	    // No pericles chunks in carrier
	    restoredCarrier = new RestoredFile(RESTORED_DIRECTORY + outputFile.getName());
	    FileUtils.copyFile(outputFile, restoredCarrier);
	    restoredCarrier.wasCarrier = true;
	    restoredCarrier.checksumValid = true;
	    restoredCarrier.restorationNote = "There were no payload files embedded, so no checksum calculation possible.";
	    restoredCarrier.algorithm = this;
	    restoredFiles.add(restoredCarrier);
	    return restoredFiles;
	}
	// remove all pericles chunks from carrier:
	reader = new PngReader(outputFile);
	PngWriter writer = new PngWriter(restoredCarrier, reader.imgInfo, true);
	writer.copyChunksFrom(chunkList, new ChunkPredicate() {
	    @Override
	    public boolean match(PngChunk chunk) {
		for (PngChunkTEXT toBeRemovedChunk : periclesChunks) {
		    if (!chunk.equals(toBeRemovedChunk)) {
			return true;
		    }
		}
		return false;
	    }
	});
	for (int row = 0; row < reader.imgInfo.rows; row++) {
	    IImageLine line = reader.readRow();
	    writer.writeRow(line);
	}
	reader.end();
	writer.end();
	restoredFiles.add(restoredCarrier);
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

    private RestoredFile restorePaylad(PngChunkTEXT textChunk) throws IOException {
	PayloadSegment payloadSegment = PayloadSegment.getPayloadSegment(textChunk.getVal().getBytes());
	RestoredFile restoredPayload = new RestoredFile(RESTORED_DIRECTORY + payloadSegment.getPayloadName());
	FileOutputStream outputStream = new FileOutputStream(restoredPayload);
	outputStream.write(payloadSegment.getPayloadBytes());
	outputStream.close();
	restoredPayload.wasPayload = true;
	restoredPayload.originalFilePath = payloadSegment.getPayloadPath();
	restoredPayload.validateChecksum(payloadSegment.getPayloadChecksum());
	return restoredPayload;
    }

    @Override
    public String getName() {
	return "PNG chunk adding";
    }

    @Override
    public String getDescription() {
	String description = "This algorithm works on PNG carrier files and text"
		+ " payload files. It uses the JPNG library to add an arbitrary text"
		+ " chunk to the existing PNG. The payload file can be restored correctly "
		+ "in every bit. As the library has too much intelligence, it reorganize the "
		+ "PNG chunks and therefore the carrier PNG cannot be restored in every bit. "
		+ "The reorganisation is lossless, so the significant properties of the PNG won't be altered."
		+ "\nThis encapsulation algorithms doesn't support A(nimated)PNGs.";
	return description;
    }

    @Override
    public boolean fulfilledTechnicalCriteria(File carrier, List<File> payloadList) {
	return carrier.isFile() && payloadList.size() > 0;
    }
}
