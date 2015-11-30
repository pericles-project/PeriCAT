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
package controller;

import java.io.File;
import java.util.List;

import algorithm.AbstractAlgorithm;

/**
 * Builder of the builder pattern for the {@link PeriCATController} of this
 * program. Takes all user configurations and configures the controller at
 * program start.
 */
public class PeriCATControllerBuilder {
    protected AbstractAlgorithm algorithm;
    protected boolean verbose = false;
    protected File carrierFile;
    protected List<File> payloadFiles;
    protected File scenarioFile;
    protected File decapsulationFile;
    protected String outputDirectory;
    protected Mode mode = Mode.GUI;

    /**
     * This enum expresses the possible modes in which PeriCAT can run.
     */
    enum Mode {
	ENCAPSULATE, DECAPSULATE, GUI, TEST
    }

    /**
     * Constructor
     */
    public PeriCATControllerBuilder() {
    }

    /**
     * Call this method at least after the builder is configured, to get the
     * {@link PeriCATController}.
     * 
     * @return the created controller
     */
    public PeriCATController create() {
	return PeriCATController.create(this);
    }

    /**
     * Enters verbose mode. The tool will print a lot of status messages during
     * this mode.
     * 
     * @return configured builder
     */
    public PeriCATControllerBuilder verbose() {
	this.verbose = true;
	return this;
    }

    /**
     * Only used at command line!
     * 
     * Specifies the algorithm to be used for the encapsulation or
     * decapsulation.
     * 
     * @param algorithm
     * @return configured builder
     */
    public PeriCATControllerBuilder useAlgorithm(AbstractAlgorithm algorithm) {
	this.algorithm = algorithm;
	return this;
    }

    /**
     * Only used at command line!
     * 
     * Use the passed carrier files for encapsulation.
     * 
     * @param carrierFile
     * @return configured builder
     */
    public PeriCATControllerBuilder useCarrier(File carrierFile) {
	this.carrierFile = carrierFile;
	this.mode = Mode.ENCAPSULATE;
	return this;
    }

    /**
     * Only used at command line!
     * 
     * Use the passed payload files for encapsulation.
     * 
     * @param payloadFiles
     * @return configured builder
     */
    public PeriCATControllerBuilder usePayload(List<File> payloadFiles) {
	this.payloadFiles = payloadFiles;
	this.mode = Mode.ENCAPSULATE;
	return this;
    }

    /**
     * Only used at command line!
     * 
     * Use the scenario file to calculate the best scored algorithm.
     * 
     * @param scenarioFile
     * @return configured builder
     */
    public PeriCATControllerBuilder useScenario(File scenarioFile) {
	this.scenarioFile = scenarioFile;
	this.mode = Mode.ENCAPSULATE;
	return this;
    }

    /**
     * Only used at command line!
     * 
     * The passed file is the file to be decapsulated.
     * 
     * @param decapsulationFile
     * @return configured builder
     */
    public PeriCATControllerBuilder useDecapsulationFile(File decapsulationFile) {
	this.decapsulationFile = decapsulationFile;
	if (this.mode != Mode.ENCAPSULATE) {
	    this.mode = Mode.DECAPSULATE;
	}
	return this;
    }

    /**
     * Use output directory.
     * 
     * @param directory
     * @return configured builder
     */
    public PeriCATControllerBuilder useOutputDirectory(String directory) {
	this.outputDirectory = directory;
	return this;
    }

    /**
     * This can be called by unit tests to get a controller without gui.
     * 
     * @return configured builder
     */
    public PeriCATControllerBuilder testMode() {
	this.mode = Mode.TEST;
	return this;
    }
}
