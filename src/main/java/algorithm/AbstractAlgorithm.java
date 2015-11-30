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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import decisionMechanism.DistanceCalculator;
import model.RestoredFile;
import model.Scenario;
import view.GUIPanel;

/**
 * All encapsulation algorithms have to extend this abstract class.
 */
public abstract class AbstractAlgorithm {
    /** Constants to define the ideal scenario characteristics */
    protected static final int YES = 0;
    protected static final int LEFT = 0;
    protected static final int NO = 100;
    protected static final int RIGHT = 100;
    protected static final int INVISIBLE = RIGHT;
    protected static final int VISIBLE = LEFT;
    protected static final int DETECTABLE = LEFT;
    protected static final int NOT_DETECTABLE = RIGHT;
    protected static final int PACKAGING = LEFT;
    protected static final int EMBEDDING = RIGHT;

    /**
     * This {@link Scenario} describes the ideal scenario in which this
     * algorithm should be used. The {@link DistanceCalculator} will compare
     * this scenario with the user defined scenario for the decision making.
     */
    public final Scenario idealScenario;
    /**
     * This file filter accepts carrier files with supported file format.
     */
    public SuffixFileFilter carrierFileFilter;
    /**
     * This file filter accepts payload files with supported file format.
     */
    public SuffixFileFilter payloadFileFilter;
    /**
     * Accepted file types could be the result of an encapsulation with this
     * algorithm, therefore a this algorithm could be used to try to decapsulate
     * the file.
     */
    public SuffixFileFilter decapsulationFileFilter;
    /**
     * Initialise this variable to create a graphical user interface for the
     * configuration of this algorithm.
     */
    public GUIPanel panel = null;

    /**
     * Constructor
     */
    public AbstractAlgorithm() {
	this.idealScenario = defineScenario();
	this.carrierFileFilter = configureCarrierFileFilter();
	this.payloadFileFilter = configurePayloadFileFilter();
	this.decapsulationFileFilter = configureDecapsulationFileFilter();
    }

    /*
     * ****** ENCAPSULATION ***********
     */

    /**
     * Starts the encapsulation process for the passed carrier and payload
     * files.
     * 
     * @param carrier
     *            list of carrier files
     * @param payload
     *            list of payload files
     * @return list of encapsulated output files
     * @throws IOException
     */
    protected abstract File encapsulate(File carrier, List<File> payload) throws IOException;

    /**
     * Checks if carrier and payload files fulfil the technical criteria to be
     * encapsulated with the algorithm, and calls the algorithms implementation
     * of the encapsulate method afterwards.
     * 
     * @param carrier
     * @param payloadList
     * @return list of encapsulated files
     */
    public File encapsulateFiles(File carrier, List<File> payloadList) {
	if (fulfilledAllTechnicalCriteria(carrier, payloadList)) {
	    try {
		return encapsulate(carrier, payloadList);
	    } catch (IOException e) {
	    }
	}
	return null;
    }

    /*
     * ****** RESTORTATION ***********
     */

    /**
     * Reverse the execution of this algorithm to restore the original payload,
     * and if possible also the carrier files. Use {@link #decapsulate(File)}
     * instead of this function, if you want to start a real decapsulation
     * process, because it will also check if the file has the correct file
     * type.
     * 
     * @param data
     *            the encapsulated data files
     * @return decapsulated payload files, and if possible also original carrier
     *         files
     * @throws IOException
     */
    protected abstract List<RestoredFile> restore(File data) throws IOException;

    /**
     * Checks if the data has the right file type to be a result of the used
     * algorithm, and calls the algorithms implementation of the restore method.
     * 
     * @param data
     * @return a list of restored files.
     */
    public List<RestoredFile> decapsulate(File data) {
	if (decapsulationFileFilter.accept(data)) {
	    try {
		return restore(data);
	    } catch (Exception e) {
	    }
	}
	return null;
    }

    /*
     * ****** CONFIGURATION ***********
     */

    /**
     * @return Name of the algorithm.
     */
    public abstract String getName();

    /**
     * @return Description of the algorithm.
     */
    public abstract String getDescription();

    /**
     * The constructor will call this function and initialise the classes
     * scenario criteria with the returned criteria.
     * 
     * Please note that the allowed values for the criteria of algorithm
     * scenarios are 0 and 100. This is necessary for a correct distance
     * calculation! Use the constants from this class.
     * 
     * @return scenario criteria for this algorithm
     */
    abstract Scenario defineScenario();

    /**
     * Will be called by the constructor. Override it to define the list of
     * supported file formats for the carrier files.
     * 
     * @return list of supported carrier file formats
     */
    abstract SuffixFileFilter configureCarrierFileFilter();

    /**
     * Will be called by the constructor. Override it to define the list of
     * supported file formats for the payload files.
     * 
     * @return list of supported payload file formats
     */
    abstract SuffixFileFilter configurePayloadFileFilter();

    /**
     * Will be called by the constructor. Override it to define the list of file
     * formats that could be the result of an encapsulation with this algorithm.
     * 
     * @return list of supported file formats for decapsulation
     */
    abstract SuffixFileFilter configureDecapsulationFileFilter();

    /**
     * This method takes the technical constraints of carrier and payload (size,
     * data format, number of files, etc.), and calculates the technical
     * oLDScenarioCriteria that are the result of these constraints for this
     * algorithm.
     * 
     * @param carrier
     * @param payloadList
     * 
     * @return TechnicalCriteria for a specific data set
     */
    public abstract boolean fulfilledTechnicalCriteria(File carrier, List<File> payloadList);

    /**
     * This method is used by the {@link model.EncapsulationData} class to add
     * algorithms to the list of possible usable algorithms, displayed in black
     * instead of red at the GUI. It calls the implemented
     * fulfilledTechnicalCriteria method of the algorithms and the file type
     * check.
     * 
     * @param carrier
     * @param payload
     * @return check if files can be used for encapsulation with this algorithm
     */
    public boolean fulfilledAllTechnicalCriteria(File carrier, List<File> payload) {
	if (carrier != null && carrier.isFile() || payload.size() > 0) {
	    return fulfilledTechnicalCriteria(carrier, payload) && fileTypesSupported(carrier, payload);
	} else {
	    return false;
	}
    }

    /**
     * Checks if the file types of carrier and payload files are in the list of
     * supported file types.
     * 
     * @return true, if file types fulfill algorithm constraints
     */
    protected boolean fileTypesSupported(File carrier, List<File> payloadList) {
	if (!carrierFileFilter.accept(new File(carrier.getPath().toLowerCase()))) {
	    return false;
	}
	for (File payload : payloadList) {
	    if (!payloadFileFilter.accept(new File(payload.getPath().toLowerCase()))) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Returns name of this algorithm
     */
    @Override
    public String toString() {
	return getName();
    }

    /*
     * ********** GETTER AND SETTER *********
     */

    protected String getOutputFileName(File file) {
	return OUTPUT_DIRECTORY + file.getName();
    }

    protected File getOutputFile(File file) {
	String outputFileName = getOutputFileName(file);
	return getRestoredCarrier(file, outputFileName);
    }

    protected String getRestoredCarrierName(File output) {
	return RESTORED_DIRECTORY + output.getName();
    }

    protected String getRestoredName(String originalName) {
	return RESTORED_DIRECTORY + originalName;
    }

    protected RestoredFile getRestoredCarrier(File output) {
	return getRestoredCarrier(output, getRestoredCarrierName(output));
    }

    protected RestoredFile getRestoredCarrier(File output, String restoredCarrierName) {
	RestoredFile restoredFile = new RestoredFile(restoredCarrierName);
	try {
	    FileUtils.copyFile(output, restoredFile);
	} catch (IOException e) {
	}
	return restoredFile;
    }

    /**
     * File filter that accepts all file types.
     */
    protected class AcceptAllFilter extends SuffixFileFilter {
	private static final long serialVersionUID = 1L;

	public AcceptAllFilter() {
	    this(new ArrayList<String>());
	}

	public AcceptAllFilter(List<String> suffixes) {
	    super(suffixes);
	}

	@Override
	public boolean accept(File file) {
	    return true;
	}
    }
}
