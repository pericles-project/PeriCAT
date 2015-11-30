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
package model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import algorithm.AbstractAlgorithm;
import main.Configuration;

/**
 * This class contains everything that is needed for an encapsulation. The
 * carrier files (digital object), the payload (metadata) and the algorithm to
 * be used;
 */
public class EncapsulationData implements Encapsulateable {
    private String name = "";
    private File carrier;
    private final List<File> payload = new ArrayList<File>();
    /** List with the encapsulated output files, filled after process: */
    private final List<File> encapsulatedData = new ArrayList<File>();
    /** List of all algorithms with matching technical criteria: */
    public final Set<AbstractAlgorithm> possibleAlgorithms = new HashSet<AbstractAlgorithm>();

    /**
     * Constructor for a new dataset.
     * 
     * @param carrier
     *            carrier file
     * @param payload
     *            List of payload files.
     * @param name
     *            Unique name of the dataset.
     */
    public EncapsulationData(File carrier, List<File> payload, String name) {
	this.carrier = carrier;
	for (File payloadFile : payload) {
	    if (payloadFile.isFile()) {
		this.payload.add(payloadFile);
	    }
	}
	updatePossibleAlgorithms();
	this.name = name;
    }

    /**
     * Constructor for a new empty dataset.
     * 
     * @param name
     *            Unique name of the dataset.
     */
    public EncapsulationData(String name) {
	this.name = name;
    }

    /**
     * @return Name of this dataset.
     */
    public String getName() {
	return this.name;
    }

    public void setName(String name) {
	this.name = name;
    }

    /**
     * @return Carrier file belonging to this dataset.
     */
    @Override
    public File getCarrier() {
	return this.carrier;
    }

    /**
     * @return List of payload files belonging to this dataset.
     */
    @Override
    public List<File> getPayload() {
	return this.payload;
    }

    public List<File> getOutput() {
	return encapsulatedData;
    }

    public void addOutputFile(File outputFile) {
	if (outputFile != null && outputFile.isFile()) {
	    encapsulatedData.add(outputFile);
	}
    }

    /**
     * Set the carrier file of the dataset.
     * 
     * @param carrier
     *            file to be added.
     */
    public void setCarrier(File carrier) {
	if (carrier != null && carrier.isFile()) {
	    this.carrier = carrier;
	    updatePossibleAlgorithms();
	}
    }

    /**
     * Add a payload file to the dataset.
     * 
     * @param payload
     *            file to be added.
     */
    public void addPayload(File payload) {
	if (payload.isFile()) {
	    this.payload.add(payload);
	}
	updatePossibleAlgorithms();
    }

    /**
     * Remove a payload file from this dataset.
     * 
     * @param file
     *            file to be removed.
     */
    public void removePayload(File file) {
	payload.remove(file);
	updatePossibleAlgorithms();
    }

    /**
     * {@link #toString()} will return the name of this dataset.
     */
    @Override
    public String toString() {
	return name;
    }

    public String print() {
	String fileString = "";
	fileString += "Name: " + name + "\n";
	fileString += "Carrier file: \n";
	fileString += "\t" + carrier.getAbsolutePath() + "\n";
	fileString += "Payload files: \n";
	for (File file : this.payload) {
	    fileString += "\t" + file.getAbsolutePath() + "\n";
	}
	fileString += "\n";
	return fileString;
    }

    /**
     * If a carrier or payload file is added or removed the list of possible
     * usable algorithms can change. This method will update the list.
     * 
     * Afterwards the GUI has to be updated
     */
    private void updatePossibleAlgorithms() {
	possibleAlgorithms.clear();
	for (AbstractAlgorithm algorithm : Configuration.getAlgorithms()) {
	    if (algorithm.fulfilledAllTechnicalCriteria(carrier, payload)) {
		possibleAlgorithms.add(algorithm);
	    }
	}
    }

    /**
     * This method checks if an algorithm can be used for encapsulation, based
     * on the algorithm name. It is especially useful, if "contains" cannot be
     * used, because the algorithm instance differs.
     * 
     * @param algorithmName
     * @return true, if technical criteria are fulfilled to use the algorithm on
     *         this dataset
     */
    public boolean isPossibleAlgorithm(String algorithmName) {
	for (AbstractAlgorithm algorithm : possibleAlgorithms) {
	    if (algorithm.getName().equals(algorithmName)) {
		return true;
	    }
	}
	return false;
    }
}
