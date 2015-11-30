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
package view;

import static main.Configuration.ADD_ICON;
import static main.Configuration.DELETE_ICON;
import static main.Configuration.START_ICON;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import algorithm.AbstractAlgorithm;
import controller.Decapsulator;
import main.Configuration;
import model.EncapsulationData;

public class DecapsulationDataPanel extends GUIPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final DecapsulationTab tab;
    private final JButton addFileButton = initButton("Add file", this, ADD_ICON);
    private final JButton removeFileButton = initButton("Remove file", this, DELETE_ICON);
    private final JList<Path> fileList = new JList<Path>(new DefaultListModel<Path>());
    private final JScrollPane fileScrollPane = new JScrollPane(fileList);
    private final JButton decapsulateButton = initButton("Decapsulate", this, START_ICON);
    private final JCheckBox allCheckBox = new JCheckBox("Check box to decapsulate all files: ");

    protected DecapsulationDataPanel(DecapsulationTab tab) {
	this.tab = tab;
	fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	fileScrollPane.setPreferredSize(new Dimension(500, 200));
	allCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);
	decapsulateButton.setToolTipText("Decapsulate the selected encapsulated files.");
	allCheckBox.setToolTipText("Decapsulate all encapsulated files, instead of just the selected ones.");
	constraints.gridwidth = 3;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.fill = GridBagConstraints.BOTH;
	add(fileScrollPane, constraints);
	constraints.weightx = 0;
	constraints.weighty = 0;
	constraints.fill = GridBagConstraints.NONE;
	constraints.gridy++;
	// Buttons:
	constraints.gridwidth = 1;
	add(addFileButton, constraints);
	constraints.gridx++;
	add(removeFileButton, constraints);
	constraints.gridx++;
	constraints.anchor = GridBagConstraints.EAST;
	add(allCheckBox, constraints);
	constraints.gridy++;
	constraints.fill = GridBagConstraints.NONE;
	add(decapsulateButton, constraints);
    }

    /**
     * Event handler for this class.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == removeFileButton) {
	    Path file = fileList.getSelectedValue();
	    ((DefaultListModel<Path>) fileList.getModel()).removeElement(file);
	} else if (e.getSource() == addFileButton) {
	    List<File> filesToBeAdded = filesFromDialogue();
	    for (File file : filesToBeAdded) {
		((DefaultListModel<Path>) fileList.getModel()).addElement(file.toPath());
	    }
	} else if (e.getSource() == decapsulateButton) {
	    startDecapsulation(allCheckBox.isSelected());
	}
    }

    private void startDecapsulation(boolean decapsulateAllDatasets) {
	List<AbstractAlgorithm> selectedAlgorithms = tab.decapsulationAlgorithmPanel.getSelectedAlgorithms();
	if (selectedAlgorithms.size() == 0) {
	    selectedAlgorithms = Configuration.getAlgorithms();
	}
	List<Path> files;
	if (decapsulateAllDatasets) {
	    files = tab.decapsulationDataPanel.getAllData();
	} else {
	    files = tab.decapsulationDataPanel.getSelectedData();
	    if (files.size() == 0) {
		files = tab.decapsulationDataPanel.getAllData();
	    }
	}
	tab.gui.updateRestored(Decapsulator.decapsulate(selectedAlgorithms, files));
    }

    private List<File> filesFromDialogue() {
	List<File> files = new ArrayList<File>();
	final JFileChooser fileChooser = new JFileChooser("Choose files to be added");
	fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
	fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	fileChooser.setMultiSelectionEnabled(true);
	fileChooser.setAcceptAllFileFilterUsed(true);
	fileChooser.setDialogTitle("Choose files to be added");
	final int result = fileChooser.showDialog(null, "add");
	if (result == JFileChooser.APPROVE_OPTION) {
	    List<File> selectedFiles = new ArrayList<File>();
	    for (File selectedPath : fileChooser.getSelectedFiles()) {
		selectedFiles.add(selectedPath);
	    }
	    return selectedFiles;
	}
	return files;
    }

    public List<Path> getSelectedData() {
	List<Path> selectedFiles = new ArrayList<Path>();
	selectedFiles.addAll(fileList.getSelectedValuesList());
	if (selectedFiles.size() == 0) {
	    /*
	     * No files were selected. We assume that the user wants to
	     * decapsulate all files in list (which is often only one file, so
	     * why should the user select it?)
	     */
	    return getAllData();
	}
	return selectedFiles;
    }

    public List<Path> getAllData() {
	List<Path> files = new ArrayList<Path>();
	Enumeration<Path> listElements = ((DefaultListModel<Path>) fileList.getModel()).elements();
	while (listElements.hasMoreElements()) {
	    files.add(listElements.nextElement());
	}
	return files;
    }

    public void refreshDataList() {
	((DefaultListModel<Path>) fileList.getModel()).removeAllElements();
	for (EncapsulationData dataset : tab.gui.controller.datasets.get()) {
	    for (File outputFile : dataset.getOutput()) {
		((DefaultListModel<Path>) fileList.getModel()).addElement(outputFile.toPath());
	    }
	}
    }

}
