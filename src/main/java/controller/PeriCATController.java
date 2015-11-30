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

import static main.Configuration.VERSION;

import java.io.File;

import algorithm.AbstractAlgorithm;
import controller.PeriCATControllerBuilder.Mode;
import decisionMechanism.DecisionMechanism;
import decisionMechanism.DistanceCalculator;
import main.Configuration;
import main.PeriCAT;
import model.EncapsulationData;
import model.Scenario;
import view.GUI;
import view.SystemTrayIcon;

/**
 * This controller controls the whole interaction of the framework components.
 * 
 * It lists datasets and algorithms, asks the {@link DistanceCalculator} which
 * algorithm is the best to use for the given {@link Scenario} and technical
 * constraints (file numbers, file sizes, etc.). At least the controller calls
 * the {@link Encapsulator} with carrier, payload and algorithm, to start the
 * encapsulation process, and the {@link Decapsulator} to start restoring
 * processes.
 * 
 * The controller also manages the user interfaces ({@link GUI}) and forwards
 * their events to the proper classes.
 */
public class PeriCATController {
    /** The graphical user interface */
    public GUI gui;
    /**
     * This option sets the whole framework to verbose mode. Verbose state
     * messages will be printed to standard out.
     */
    public static boolean verbose = false;
    /**
     * The selected algorithm is the one to use for encapsulation, if the
     * useSelectedAlgorithm option is true.
     */
    private AbstractAlgorithm selectedAlgorithm;
    /**
     * Class that manages the scenarios / roles
     */
    public ScenarioController scenarioController;
    /**
     * Class that manages the decision making
     */
    public DecisionMechanism decisionMechanism;
    /**
     * Class that manages the datasets.
     */
    public DatasetController datasets;

    /**
     * An PeriCATController can only be initialised with its factory method
     * {@link PeriCATController#create(PeriCATControllerBuilder)}
     */
    private PeriCATController() {
    }

    /**
     * Factory method that takes an {@link PeriCATControllerBuilder} to
     * initialise the PeriCATController.
     * 
     * @param builder
     *            The builder of the implemented builder design pattern
     * @return initialised PeriCATController
     */
    public final static PeriCATController create(PeriCATControllerBuilder builder) {
	PeriCATController controller = new PeriCATController();
	PeriCATController.verbose = builder.verbose;
	if (builder.mode == Mode.ENCAPSULATE) {
	    Configuration.OUTPUT_DIRECTORY = builder.outputDirectory;
	    Configuration.createEncapsulationConstants();
	    Configuration.createProjectDirectories();
	    encapsulateAndExit(builder);
	} else if (builder.mode == Mode.DECAPSULATE) {
	    Configuration.RESTORED_DIRECTORY = builder.outputDirectory;
	    Configuration.createEncapsulationConstants();
	    Configuration.createProjectDirectories();
	    decapsulateAndExit(builder);
	} else if (builder.mode == Mode.GUI) {
	    Configuration.createEncapsulationConstants();
	    Configuration.createProjectDirectories();
	    initialiseFramework(controller);
	    startGUI(controller);
	} else if (builder.mode == Mode.TEST) {
	    initialiseFramework(controller);
	}
	return controller;
    }

    /**
     * Command line arguments have specified parameters for encapsulation. The
     * API is called to execute the command, and the tool exists afterwards.
     * 
     * @param builder
     */
    private static void encapsulateAndExit(PeriCATControllerBuilder builder) {
	if (builder.algorithm != null) {
	    PeriCAT.encapsulate(builder.carrierFile, builder.payloadFiles, builder.algorithm);
	} else if (builder.scenarioFile != null) {
	    PeriCAT.encapsulate(builder.carrierFile, builder.payloadFiles, builder.scenarioFile);
	} else {
	    System.out.println("You have to specify the algorithm to be used, or pass an scenario file.");
	}
	System.exit(0);
    }

    /**
     * Command line arguments have specified parameters for decapsulation. The
     * API is called to execute the command, and the tool exists afterwards.
     * 
     * @param builder
     */
    private static void decapsulateAndExit(PeriCATControllerBuilder builder) {
	if (builder.algorithm != null) {
	    PeriCAT.decapsulate(new File(builder.outputDirectory), builder.algorithm);
	} else {
	    PeriCAT.decapsulate(new File(builder.outputDirectory));
	}
	System.exit(0);
    }

    /**
     * No command line arguments were passed. The graphical user interface will
     * be executed and all controller will be initialised.
     * 
     * @param controller
     */
    private static void initialiseFramework(PeriCATController controller) {
	if (PeriCATController.verbose) {
	    printVerboseWelcome(controller);
	    PeriCATController.verbose = false;
	    controller.datasets = new DatasetController(controller);
	    controller.decisionMechanism = new DecisionMechanism();
	    controller.scenarioController = new ScenarioController(controller.decisionMechanism);
	    PeriCATController.verbose = true;
	} else {
	    controller.datasets = new DatasetController(controller);
	    controller.decisionMechanism = new DecisionMechanism();
	    controller.scenarioController = new ScenarioController(controller.decisionMechanism);
	}
    }

    /**
     * Starts the graphical user interface. This is not executed in test mode.
     * 
     * @param controller
     */
    private static void startGUI(PeriCATController controller) {
	controller.gui = new GUI(controller);
	new SystemTrayIcon(controller);
    }

    private static void printVerboseWelcome(PeriCATController controller) {
	System.out.println("Welcome to the PeriCAT version " + VERSION
		+ " - PERICLES Content Aggregation Tool - a framework for information encapsulation.\n");
	System.out.println("## Configuration ##");
	System.out.println("\tVerbose: " + PeriCATController.verbose + "\n");
	System.out.println("## Available Algorithms ##");
	for (AbstractAlgorithm algorithm : Configuration.getAlgorithms()) {
	    System.out.println("\t" + algorithm.getName());
	}
	System.out.println("\nStarting the graphical user interface...\n");
    }

    /* ******* ALGORITHMS: ******* */
    /**
     * The selected algorithm is the one that is used for encapsulation of all
     * datasets, if the option is chosen to encapsulate all datasets with the
     * same algorithm.
     * 
     * @return selected algorithm
     */
    public AbstractAlgorithm getSelectedAlgorithm() {
	return selectedAlgorithm;
    }

    /**
     * Sets the selected algorithm to be used for encapsulation, if the
     * "use selected algorithm"-option is enabled.
     * 
     * @param algorithm
     */
    public void setSelectedAlgorithm(AbstractAlgorithm algorithm) {
	this.selectedAlgorithm = algorithm;
	gui.setSelectedAlgorithm(algorithm);
    }

    /**
     * Get an algorithm based on its name.
     * 
     * @param algorithmName
     * @return algorithm
     */
    public AbstractAlgorithm getAlgorithm(String algorithmName) {
	for (AbstractAlgorithm algorithm : Configuration.getAlgorithms()) {
	    if (algorithm.getName().equals(algorithmName)) {
		return algorithm;
	    }
	}
	return null;
    }

    /* ******* ENCAPSULATION: ******* */

    public void encapsulateAllDatasets(AbstractAlgorithm selectedAlgorithm) {
	for (EncapsulationData dataset : datasets.get()) {
	    encapsulateDataset(dataset, selectedAlgorithm);
	}
    }

    public void encapsulateDataset(EncapsulationData dataset, AbstractAlgorithm algorithm) {
	Encapsulator.encapsulate(dataset, algorithm);
    }

    /* ******* EXIT: ******* */
    /**
     * Exit PeriCAT.
     */
    public void exit() {
	if (verbose) {
	    System.out.println("Exit the framework. Good bye.");
	}
	System.exit(0);
    }
}