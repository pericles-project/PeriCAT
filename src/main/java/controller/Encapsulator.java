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

import algorithm.AbstractAlgorithm;
import model.EncapsulationData;

/**
 * Static component that is responsible for the encapsulation.
 */
public class Encapsulator {

    /*
     * Static class, private constructor.
     */
    private Encapsulator() {
    }

    /**
     * If possible encapsulate dataset with selected algorithm, else encapsulate
     * with zip packaging as default.
     * 
     * @param algorithm
     * @param dataset
     * @return the encapsulated output file(s)
     */
    public static File encapsulate(EncapsulationData dataset, AbstractAlgorithm algorithm) {
	if (dataset.isPossibleAlgorithm(algorithm.getName())) {
	    printVerbose1(algorithm, dataset);
	    File outputFile = algorithm.encapsulateFiles(dataset.getCarrier(), dataset.getPayload());
	    if (outputFile != null) {
		printVerbose2(outputFile);
		dataset.addOutputFile(outputFile);
		return outputFile;
	    } else {
		printVerbose4(algorithm, dataset);
	    }
	} else {
	    printVerbose3(algorithm, dataset);
	}
	return null;
    }

    private static void printVerbose1(AbstractAlgorithm algorithm, EncapsulationData dataset) {
	if (PeriCATController.verbose) {
	    System.out.print("Using " + algorithm.getName() + " to encapsulate dataset - " + dataset.print());
	}
    }

    private static void printVerbose2(File outputFile) {
	if (PeriCATController.verbose) {
	    System.out.println("Success! Output file: \n" + outputFile.getName() + "\n");
	}
    }

    private static void printVerbose3(AbstractAlgorithm algorithm, EncapsulationData dataset) {
	if (PeriCATController.verbose) {
	    System.out.print(algorithm.getName() + " can not be applied on dataset - " + dataset.print()
		    + "\nTry another algorithm.");
	}
    }

    private static void printVerbose4(AbstractAlgorithm algorithm, EncapsulationData dataset) {
	if (PeriCATController.verbose) {
	    System.out.print("Warning - Dataset " + dataset.print() + "\n"
		    + "was not encapsulated, because of an unexpected error.\n"
		    + "No output files produces with algorithm " + algorithm.getName() + ".\n"
		    + "Try another algorithm.");
	}
    }
}
