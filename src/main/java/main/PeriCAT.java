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
package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import algorithm.AbstractAlgorithm;
import algorithm.BagItPackaging;
import algorithm.F5Steganography;
import algorithm.ImageImageFrameExpanding;
import algorithm.ImageInformationEmbeddingFrame;
import algorithm.JPEGTextAdding;
import algorithm.MetsSubmissionInformationPackage;
import algorithm.OaiOreSubmissionInformationPackage;
import algorithm.OpenStegoRandomLSBSteganography;
import algorithm.PDFFileAttacher;
import algorithm.PNGChunkAdding;
import algorithm.QRCodeWatermarking;
import algorithm.TarPackaging;
import algorithm.TextInformationFrame;
import algorithm.ZipPackaging;
import controller.Decapsulator;
import controller.Encapsulator;
import controller.ScenarioSaverAndLoader;
import decisionMechanism.Decider;
import model.EncapsulationData;
import model.Scenario;

/**
 * Wrapper API around the PeriCAT - PERICLES Content Aggregation Tool This class
 * provides an interface to be used by other programs.
 */
public class PeriCAT {
    public static final AbstractAlgorithm TEXT_INFORMATION_FRAME = new TextInformationFrame();
    public static final AbstractAlgorithm RANDOM_LSB_STEGANOGRAPHY = new OpenStegoRandomLSBSteganography();
    public static final AbstractAlgorithm F5_STEGANOGRAPHY = new F5Steganography();
    public static final AbstractAlgorithm BAG_IT_PACKAGING = new BagItPackaging();
    public static final AbstractAlgorithm PNG_CHUNK_ADDING = new PNGChunkAdding();
    public static final AbstractAlgorithm JPEG_TEXT_ADDING = new JPEGTextAdding();
    public static final AbstractAlgorithm PDF_FILE_ATTACHER = new PDFFileAttacher();
    public static final AbstractAlgorithm ZIP_PACKAGING = new ZipPackaging();
    public static final AbstractAlgorithm TAR_PACKAGING = new TarPackaging();
    public static final AbstractAlgorithm OAI_ORE_SIP = new OaiOreSubmissionInformationPackage();
    public static final AbstractAlgorithm METS_SIP = new MetsSubmissionInformationPackage();
    public static final AbstractAlgorithm QR_CODE = new QRCodeWatermarking();
    public static final AbstractAlgorithm IMAGE_IMAGE_FRAME = new ImageImageFrameExpanding();
    public static final AbstractAlgorithm IMAGE_INFORMATION_FRAME = new ImageInformationEmbeddingFrame();

    /**
     * Encapsulates files with the passed algorithm.
     * 
     * @param carrierFile
     * @param payloadFiles
     * @param algorithm
     * @return encapsulated file
     */
    public static File encapsulate(File carrierFile, List<File> payloadFiles, AbstractAlgorithm algorithm) {
	EncapsulationData dataset = new EncapsulationData(carrierFile, payloadFiles, "PeriCAT-API");
	return Encapsulator.encapsulate(dataset, algorithm);
    }

    /**
     * Encapsulates files with a suggested algorithm, based on the scenario
     * passed as scenario file.
     * 
     * @param carrierFile
     * @param payloadFiles
     * @param scenarioFile
     * @return encapsulated file
     */
    public static File encapsulate(File carrierFile, List<File> payloadFiles, File scenarioFile) {
	EncapsulationData dataset = new EncapsulationData(carrierFile, payloadFiles, "PeriCAT-API");
	Scenario scenario = ScenarioSaverAndLoader.load("" + scenarioFile.toPath());
	Decider decider = new Decider(scenario);
	AbstractAlgorithm algorithm = decider.getHighscore().get(0).algorithm;
	return Encapsulator.encapsulate(dataset, algorithm);
    }

    /**
     * Decapsulates a file with the passed algorithm.
     * 
     * @param file
     * @param algorithm
     * @return decapsulated files, or null;
     */
    public static List<File> decapsulate(File file, AbstractAlgorithm algorithm) {
	List<File> outputFiles = new ArrayList<File>();
	for (File restoredFile : Decapsulator.decapsulate(algorithm, file.toPath())) {
	    outputFiles.add(restoredFile);
	}
	return outputFiles;
    }

    /**
     * Decapsulates a file; the algorithm for decapsulation will be guessed.
     *
     * @param file
     * @return decapsulated files, or null;
     */
    public static List<File> decapsulate(File file) {
	List<AbstractAlgorithm> algorithms = new ArrayList<AbstractAlgorithm>();
	algorithms.addAll(Configuration.getAlgorithms());
	List<File> outputFiles = new ArrayList<File>();
	for (File restoredFile : Decapsulator.decapsulate(algorithms, file.toPath())) {
	    outputFiles.add(restoredFile);
	}
	return outputFiles;
    }
}
