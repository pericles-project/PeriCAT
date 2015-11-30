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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import algorithm.AbstractAlgorithm;
import algorithm.QRCodeWatermarking;
import model.RestoredFile;

/**
 * The {@link Decapsulator} reverses the information encapsulation process. The
 * encapsulated files will be decapsulated with the output of restored files. In
 * best case this will recover all original files. This is not always possible.
 */
public class Decapsulator {

    /**
     * Decapsulation with one file and unknown algorithm.
     * 
     * @param algorithms
     * @param file
     * @return restored files
     */
    public static List<RestoredFile> decapsulate(List<AbstractAlgorithm> algorithms, Path file) {
	List<Path> files = new ArrayList<Path>();
	files.add(file);
	return decapsulate(algorithms, files);
    }

    /**
     * Decapsulation with known algorithm and one file.
     * 
     * @param algorithm
     * @param file
     * @return restored files
     */
    public static List<RestoredFile> decapsulate(AbstractAlgorithm algorithm, Path file) {
	List<Path> files = new ArrayList<Path>();
	files.add(file);
	return decapsulate(algorithm, files);
    }

    /**
     * Decapsulation with known algorithm.
     * 
     * @param algorithm
     * @param files
     * @return restored files
     */
    public static List<RestoredFile> decapsulate(AbstractAlgorithm algorithm, List<Path> files) {
	List<AbstractAlgorithm> algorithms = new ArrayList<AbstractAlgorithm>();
	algorithms.add(algorithm);
	return decapsulate(algorithms, files);
    }

    /**
     * This function tries to used the passed algorithms to decapsulate the
     * files and returns the restored files, if successful.
     * 
     * @param algorithms
     *            used for decapsulation
     * @param files
     *            the carrier(s) and payload(s) are encapsulated in these files
     * @return decapsulated files
     */
    public static List<RestoredFile> decapsulate(List<AbstractAlgorithm> algorithms, List<Path> files) {
	printVerbose1(files);
	if (files.size() == 0) {
	    System.out.println("Add a file for decapsulation!");
	    return null;
	}
	List<RestoredFile> allRestoredFiles = new ArrayList<RestoredFile>();
	for (Path encapsulatedFiles : files) {
	    /*
	     * find for each file the right algorithm, break after decapsulation
	     * success.
	     */
	    for (AbstractAlgorithm algorithm : algorithms) {
		printVerbose3(algorithm, encapsulatedFiles);
		try {
		    List<RestoredFile> restoredFiles = new ArrayList<RestoredFile>();
		    restoredFiles.addAll(algorithm.decapsulate(encapsulatedFiles.toFile()));
		    printVerbose2(restoredFiles);
		    if (restoredFiles.size() == 1) {
			// QR code algorithm can return only 1 file
			RestoredFile restoredFile = restoredFiles.get(0);
			if (restoredFile.algorithm instanceof QRCodeWatermarking) {
			    allRestoredFiles.add(restoredFile);
			}
		    } else if (restoredFiles.size() > 1) {
			/*
			 * Decapsulation success! So don't try the other
			 * algorithms and break out of the algorithm loop.
			 */
			allRestoredFiles.addAll(restoredFiles);
			break;
		    }
		} catch (Exception e) {
		    if (PeriCATController.verbose) {
			System.out.println("Decapsulation failed. This was probably not the right algorithm.");
		    }
		}
	    }
	}
	return allRestoredFiles;
    }

    private static void printVerbose1(List<Path> selectedFiles) {
	if (PeriCATController.verbose) {
	    System.out.println("Starting decapsulation for " + selectedFiles.size() + " file(s)...");
	}
    }

    private static void printVerbose2(List<RestoredFile> restoredFiles) {
	if (PeriCATController.verbose) {
	    if (restoredFiles.size() == 1) {
		RestoredFile restoredFile = restoredFiles.get(0);
		if (restoredFile.algorithm instanceof QRCodeWatermarking) {
		    System.out.println("...success! Could restore the file " + restoredFile.getPath());
		} else {
		    System.out.println("...decapsulation not successful, try another algorithm.");
		}
	    } else if (restoredFiles.size() > 1) {
		System.out.println("...success! Could restore " + restoredFiles.size() + " files!");
	    } else {
		System.out.println("...decapsulation not successful, try another algorithm.");
	    }
	}
    }

    private static void printVerbose3(AbstractAlgorithm algorithm, Path file) {
	if (PeriCATController.verbose) {
	    System.out.println(
		    "...trying algorithm " + algorithm.getName() + " for decapsulation of file " + file + " ...");
	}
    }
}
