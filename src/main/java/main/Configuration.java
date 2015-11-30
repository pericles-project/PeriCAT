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

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.io.FileUtils;

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

/**
 * This class provides configuration options: paths to the project directory and
 * configuration files, paths to images, and other configuration parameter.
 */
public class Configuration {
    /* private constructor: static class */
    private Configuration() {
    }

    /** Version number of the tool */
    public static final String VERSION = "1.0";
    /** General short description text to be encapsulated */
    public static final String TOOL_DESCRIPTION = "The PeriCAT - PERICLES Content Aggregation Tool, version " + VERSION
	    + ", was used to encapsulate these information.";
    /** Directory for test data. This is for the unit tests. */
    public static final String TEST_DIRECTORY = "src" + File.separator + "test" + File.separator + "resources"
	    + File.separator;

    /** The directory where the projects directory will be created. */
    public static String WORKING_DIRECTORY = getCurrentJarFolder() + File.separator + "test_directory" + File.separator;
    /** The result files of encapsulation processes will be stored here */
    public static String OUTPUT_DIRECTORY = WORKING_DIRECTORY + "output" + File.separator;
    /** If encapsulated files are restored, they will be stored here */
    public static String RESTORED_DIRECTORY = WORKING_DIRECTORY + "restored" + File.separator;
    /** Directory where the scenarios or roles are stored */
    public static String SCENARIO_DIRECTORY = WORKING_DIRECTORY + "scenarios" + File.separator;
    /** Directory in which external libraries are stored */
    public static String LIBRARY_DIRECTORY = getCurrentJarFolder() + File.separator + "PeriCAT_libs" + File.separator;

    /** Image icon, for add buttons */
    public static final Icon ADD_ICON = new ImageIcon(getImage("/images/add.png"));
    /** Image icon for delete buttons */
    public static final Icon DELETE_ICON = new ImageIcon(getImage("/images/delete.png"));
    /** Image icon for save buttons */
    public static final Icon SAVE_ICON = new ImageIcon(getImage("/images/disk.png"));
    /** Image icon for buttons that start a process */
    public static final Icon START_ICON = new ImageIcon(getImage("/images/control_play_blue.png"));

    private static List<AbstractAlgorithm> algorithms = new ArrayList<AbstractAlgorithm>();

    /**
     * Configures the constants of the projects directories.
     */
    public static void createEncapsulationConstants() {
	WORKING_DIRECTORY = getCurrentJarFolder() + File.separator;
	if (OUTPUT_DIRECTORY == null || !new File(OUTPUT_DIRECTORY).isDirectory()) {
	    OUTPUT_DIRECTORY = WORKING_DIRECTORY + "PeriCAT_output" + File.separator;
	}
	if (RESTORED_DIRECTORY == null || !new File(RESTORED_DIRECTORY).isDirectory()) {
	    RESTORED_DIRECTORY = WORKING_DIRECTORY + "PeriCAT_restored" + File.separator;
	}
	SCENARIO_DIRECTORY = WORKING_DIRECTORY + "PeriCAT_scenarios" + File.separator;
	LIBRARY_DIRECTORY = WORKING_DIRECTORY + "PeriCAT_libs" + File.separator;
	createProjectDirectories();
    }

    /**
     * Load an image icon to be displayed on a button.
     * 
     * @param path
     * @return image for a button
     */
    public static Image getImage(String path) {
	URL url = Configuration.class.getResource(path);
	if (url == null) {
	    return null;
	}
	return Toolkit.getDefaultToolkit().getImage(url);
    }

    /**
     * Creates the project directories. To be called on tool start.
     */
    public static void createProjectDirectories() {
	try {
	    FileUtils.forceMkdir(new File(OUTPUT_DIRECTORY));
	    FileUtils.forceMkdir(new File(RESTORED_DIRECTORY));
	    FileUtils.forceMkdir(new File(SCENARIO_DIRECTORY));
	    FileUtils.forceMkdir(new File(LIBRARY_DIRECTORY));
	} catch (IOException e) {
	}
    }

    /**
     * Gets the location of the .jar, to figure out where to create the project
     * directory.
     * 
     * @return File with path to jar directory
     */
    private static File getCurrentJarFolder() {
	try {
	    File file = new File(Configuration.class.getProtectionDomain().getCodeSource().getLocation().toURI())
		    .getParentFile();
	    if (file.exists() && file.isDirectory()) {
		return file;
	    }
	} catch (URISyntaxException e) {
	    System.err.println(
		    "Exception while getting the locatio of the .jar. for the creation of the projects directory.");
	}
	return new File("./");
    }

    /**
     * Get the list of available algorithms
     *
     * Add here further algorithms, and comment out the "not yet finished"
     * algorithms.
     * 
     * @return algorithms for information encapsulation
     */
    public static List<AbstractAlgorithm> getAlgorithms() {
	if (algorithms.size() == 0) {
	    algorithms.add(new TextInformationFrame());
	    algorithms.add(new OpenStegoRandomLSBSteganography());
	    algorithms.add(new F5Steganography());
	    algorithms.add(new BagItPackaging());
	    algorithms.add(new PNGChunkAdding());
	    algorithms.add(new JPEGTextAdding());
	    algorithms.add(new PDFFileAttacher());
	    algorithms.add(new ZipPackaging());
	    algorithms.add(new TarPackaging());
	    algorithms.add(new OaiOreSubmissionInformationPackage());
	    algorithms.add(new MetsSubmissionInformationPackage());
	    algorithms.add(new QRCodeWatermarking());
	    algorithms.add(new ImageImageFrameExpanding());
	    algorithms.add(new ImageInformationEmbeddingFrame());
	}
	return algorithms;
    }
}
