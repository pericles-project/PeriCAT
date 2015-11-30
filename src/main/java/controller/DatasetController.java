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

import java.util.ArrayList;
import java.util.List;

import model.EncapsulationData;

public class DatasetController {
    private final List<EncapsulationData> datasets = new ArrayList<EncapsulationData>();
    private final PeriCATController controller;

    /**
     * Constructor of the dataset controller. At start a default dataset will be
     * created. For this purpose the verbose messages are temporary switched
     * off.
     * 
     * @param controller
     */
    public DatasetController(PeriCATController controller) {
	this.controller = controller;
	if (PeriCATController.verbose) {
	    PeriCATController.verbose = false;
	    create("new Dataset");
	    PeriCATController.verbose = true;
	} else {
	    create("new Dataset");
	}
    }

    /**
     * Get a dataset based on its name
     * 
     * @param datasetName
     * @return dataset
     */
    public EncapsulationData get(String datasetName) {
	for (EncapsulationData dataset : datasets) {
	    if (dataset.getName().equals(datasetName)) {
		return dataset;
	    }
	}
	return null;
    }

    /**
     * Removes a dataset from the program.
     * 
     * @param dataset
     */
    public void remove(EncapsulationData dataset) {
	if (datasets.size() == 1) {
	    // number of datasets should never be less than 1!
	    create("new Dataset");
	}
	datasets.remove(dataset);
	if (controller.gui != null) {
	    controller.gui.removeDataset(dataset);
	}
	if (PeriCATController.verbose) {
	    System.out.println("Removed dataset " + dataset.getName() + "\n");
	}
    }

    /**
     * Create a new empty {@link EncapsulationData} dataset.
     *
     * @param dataName
     *            Name of the dataset (as displayed in GUI)
     * @return the created data set
     */
    public EncapsulationData create(String dataName) {
	EncapsulationData dataset = new EncapsulationData(getUniqueName(dataName, 0));
	datasets.add(dataset);
	if (controller.gui != null) {
	    controller.gui.addDataset(dataset);
	}
	if (PeriCATController.verbose) {
	    System.out.println("Created new dataset " + dataset.getName() + "\n");
	}
	return dataset;
    }

    public void refresh(EncapsulationData dataset) {
	if (controller.gui != null) {
	    controller.gui.refreshGUI(dataset);
	}
    }

    /*
     * Adds a number at the end of the name, if the name is already used.
     * Increases the number, until a unique name is found.
     */
    private String getUniqueName(String suggestedName, int iteration) {
	for (EncapsulationData dataset : datasets) {
	    if (iteration == 0 && dataset.getName().equals(suggestedName)) {
		return getUniqueName(suggestedName, ++iteration);
	    } else if (iteration > 0 && dataset.getName().equals(suggestedName + iteration)) {
		return getUniqueName(suggestedName, ++iteration);
	    }
	}
	if (iteration == 0) {
	    return suggestedName;
	} else {
	    return suggestedName + iteration;
	}
    }

    public void addAll(List<EncapsulationData> datasets) {
	this.datasets.addAll(datasets);
    }

    /**
     * Get the list of datasets.
     * 
     * @return All datasets of the program.
     */
    public List<EncapsulationData> get() {
	return datasets;
    }
}
