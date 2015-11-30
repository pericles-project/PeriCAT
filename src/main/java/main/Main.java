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

import static main.Configuration.VERSION;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import algorithm.AbstractAlgorithm;
import controller.PeriCATControllerBuilder;

/**
 * Main Class - tool start point
 */
public class Main {
    private final static StartParameters options = new StartParameters();
    private static JCommander jCommander;

    /**
     * controller method. Takes the user input and starts the application.
     * 
     * @param args
     *            Command line start arguments.
     */
    public static void main(String[] args) {
	PeriCATControllerBuilder builder = new PeriCATControllerBuilder();
	parseCommands(args);
	handleHelpOption();
	handleProjectHelpOption();
	handleVersionOption();
	handleListAlgorithmsOption();
	handleVerboseOption(builder);
	handleAlgorithmOption(builder);
	handleCarrierOption(builder);
	handlePayloadOption(builder);
	handleScenarioOption(builder);
	handleDecapsulationOption(builder);
	handleOutputDirectory(builder);
	builder.create();
    }

    /**
     * Parses the command line arguments into the {@link StartParameters} class.
     * 
     * @param args
     */
    private static void parseCommands(String[] args) {
	jCommander = new JCommander(options);
	jCommander.setProgramName("PeriCAT");
	try {
	    jCommander.parse(args);
	} catch (ParameterException e) {
	    jCommander.usage();
	    System.out.println("Wrong arguments, can not start. Try --help");
	    System.exit(-1);
	}
    }

    /**
     * Print help and exit.
     */
    private static void handleHelpOption() {
	if (options.help) {
	    System.out
		    .println("If you execute the tool without parameter, a graphical user interface will be started.");
	    jCommander.usage();
	    System.exit(0);
	}
    }

    /**
     * Print project help and exit.
     */
    private static void handleProjectHelpOption() {
	if (options.phelp) {
	    printProjectHelpAndExit();
	}
    }

    /**
     * Print version and exit.
     */
    private static void handleVersionOption() {
	if (options.version) {
	    System.out.println("PeriCAT [Version " + VERSION + "]");
	    System.exit(0);
	}
    }

    /**
     * Print list of available algorithms and exit.
     */
    private static void handleListAlgorithmsOption() {
	if (options.list) {
	    printAvailableAlgorithms();
	    System.exit(0);
	}
    }

    /**
     * Set the tool to verbose mode.
     * 
     * @param builder
     */
    private static void handleVerboseOption(PeriCATControllerBuilder builder) {
	if (options.verbose) {
	    builder.verbose();
	}
    }

    /**
     * Use the passed algorithm.
     * 
     * @param builder
     */
    private static void handleAlgorithmOption(PeriCATControllerBuilder builder) {
	if (options.algorithmName != null) {
	    AbstractAlgorithm algorithm = getAlgorithm(options.algorithmName);
	    if (algorithm != null) {
		builder.useAlgorithm(algorithm);
		if (options.scenario != null) {
		    System.out.println("The scenario is ignored, because an algorithm name to be used was specified.");
		}
	    } else {
		System.out.println("The algorithm name \"" + options.algorithmName
			+ "\" is not in the list of possible algorithms. Check typing and try again. Use the \"--list\" option to get a list of possible algorithms.");
		System.exit(0);
	    }
	}
    }

    /**
     * Checks if the passed algorithm name is in the list of available
     * algorithms.
     * 
     * @param algorithmName
     * @return true if possible algorithm
     */
    private static AbstractAlgorithm getAlgorithm(String algorithmName) {
	for (AbstractAlgorithm algorithm : Configuration.getAlgorithms()) {
	    if (algorithm.getName().equals(algorithmName)) {
		return algorithm;
	    }
	}
	return null;
    }

    /**
     * Passes the carrier files to the tool.
     * 
     * @param builder
     */
    private static void handleCarrierOption(PeriCATControllerBuilder builder) {
	if (options.carrier != null) {
	    File carrier = new File(options.carrier);
	    if (carrier.isFile()) {
		builder.useCarrier(carrier);
	    } else {
		System.out.println("The carrier \"" + options.carrier + "\" is not a file. Check typing.");
		System.exit(0);
	    }
	}
    }

    private static void handlePayloadOption(PeriCATControllerBuilder builder) {
	options.payload.addAll(options.payloadFilesWithoutFlag);
	List<File> payloadFiles = new ArrayList<File>();
	for (String payloadPath : options.payload) {
	    File payload = new File(payloadPath);
	    if (payload.isFile()) {

	    } else {
		System.out.println("The payload \"" + payloadPath + "\" is not a valid file. Check typing.");
		System.exit(0);
	    }
	}
	if (payloadFiles.size() > 1) {
	    builder.usePayload(payloadFiles);
	}
    }

    private static void handleScenarioOption(PeriCATControllerBuilder builder) {
	if (options.algorithmName == null && options.scenario != null) {
	    File scenarioFile = new File(options.scenario);
	    if (scenarioFile.isFile()) {
		builder.useScenario(scenarioFile);
	    } else {
		System.out.println("The scenario \"" + options.scenario + "\" is not a valid file.");
		System.exit(0);
	    }
	}
    }

    private static void handleDecapsulationOption(PeriCATControllerBuilder builder) {
	if (options.decapsulate != null) {
	    File decapsulationFile = new File(options.decapsulate);
	    if (decapsulationFile.isFile()) {
		builder.useDecapsulationFile(decapsulationFile);
	    } else {
		System.out.println("The file \"" + options.decapsulate
			+ "\" is not a valid file and can't be used for decapsulation.");
		System.exit(0);
	    }
	}
    }

    private static void handleOutputDirectory(PeriCATControllerBuilder builder) {
	if (options.outputDirectory != null) {
	    File outputDir = new File(options.outputDirectory);
	    if (outputDir.isDirectory()) {
		builder.useOutputDirectory(options.outputDirectory);
	    } else {
		System.out.println("The file \"" + options.outputDirectory + "\" is not a valid output directory.");
		System.exit(0);
	    }
	}
    }

    private static void printProjectHelpAndExit() {
	System.out.println("PeriCAT Version " + VERSION
		+ " - PERICLES Content Aggregation Tool. It encapsulates carrier and payload files using packaging and embedding techniques and assists with deciding which encapsulation technique to use.");
	System.exit(0);
    }

    /**
     * Print the list of available algorithms;
     */
    public static void printAvailableAlgorithms() {
	System.out.println("Available algorithms:\n");
	for (AbstractAlgorithm algorithm : Configuration.getAlgorithms()) {
	    System.out.println("\t" + algorithm.getName());
	}
    }

    private static class StartParameters {
	/* Encapsulation parameter: */
	@Parameter
	private final List<String> payloadFilesWithoutFlag = new ArrayList<String>();
	@Parameter(names = { "-c", "--carrier" }, description = "The carrier file.")
	public String carrier;
	@Parameter(names = { "-p",
		"--payload" }, description = "Add a comma separated list of payload files to be encapsulated.")
	public List<String> payload = new ArrayList<String>();
	@Parameter(names = { "-a", "--algorithm" }, description = "Algorithm name. Put the name in quotes!")
	public String algorithmName;
	@Parameter(names = { "-s",
		"--scenario" }, description = "Use a scenario file. If a scenario file is specified, then the best fitting algorithm for the scenario is used. Therefore you don't need to specify an algorithm.")
	public String scenario;
	@Parameter(names = { "-d", "--decapsulate" }, description = "Path to a file to be decapsulated.")
	public String decapsulate;
	@Parameter(names = { "-o",
		"--outputDirectory" }, description = "Output directory. Default is the PeriCAT_output directory for encapsulation and the PeriCAT_restored directory for decapsulation in the same directory as the PeriCAT.jar file.")
	public String outputDirectory;
	/* Information only parameter: */
	@Parameter(names = { "-h", "--help" }, description = "Print this message.")
	public boolean help;
	@Parameter(names = { "-ph", "--projecthelp" }, description = "Print project help information.")
	public boolean phelp;
	@Parameter(names = { "--version" }, description = "Print the version of the tool.")
	public boolean version;
	@Parameter(names = { "-l",
		"--list" }, description = "Print the list of available algorithms for information encapsulation.")
	public boolean list;
	@Parameter(names = { "-v", "--verbose" }, description = "Switch the tool to verbose mode.")
	public boolean verbose;
    }
}
