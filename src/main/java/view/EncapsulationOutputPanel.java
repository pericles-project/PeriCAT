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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;

import main.Configuration;
import model.EncapsulationData;

/**
 * Panel that displays the encapsulation preferences and the output files.
 */
class EncapsulationOutputPanel extends GUIPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final JList<String> outputList = new JList<String>(new DefaultListModel<String>());
    private final EncapsulationTab tab;
    private final JLabel outputDirectoryLabel = new JLabel(Configuration.OUTPUT_DIRECTORY);
    private final JButton outputDirectoryButton = new JButton("Change output directory");

    protected EncapsulationOutputPanel(EncapsulationTab encapsulationTab) {
	this.tab = encapsulationTab;
	outputDirectoryButton.addActionListener(this);
	add(outputList, constraints);
	constraints.gridy++;
	add(new JLabel("Output directory:"), constraints);
	constraints.gridy++;
	add(outputDirectoryLabel, constraints);
	constraints.gridy++;
	add(outputDirectoryButton, constraints);
    }

    /**
     * Fills the list of output files.
     */
    protected void refreshOutputList() {
	((DefaultListModel<String>) outputList.getModel()).removeAllElements();
	List<File> outputFiles = new ArrayList<File>();
	for (EncapsulationData dataset : tab.gui.controller.datasets.get()) {
	    outputFiles.addAll(dataset.getOutput());
	}
	if (outputFiles.size() == 0) {
	    ((DefaultListModel<String>) outputList.getModel()).addElement("No data encapsulated, yet.\n");
	    return;
	}
	for (File file : outputFiles) {
	    ((DefaultListModel<String>) outputList.getModel()).addElement("" + file + "\n");
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == outputDirectoryButton) {
	    JFileChooser fileChooser = new JFileChooser("Choose output directory");
	    fileChooser.setDialogTitle("Change output directory");
	    fileChooser.setDialogType(JFileChooser.FILES_AND_DIRECTORIES);
	    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    fileChooser.setMultiSelectionEnabled(false);
	    fileChooser.setAcceptAllFileFilterUsed(true);
	    final int result = fileChooser.showDialog(null, "add");
	    if (result == JFileChooser.APPROVE_OPTION) {
		File file = fileChooser.getSelectedFile();
		if (file != null && file.isDirectory()) {
		    Configuration.OUTPUT_DIRECTORY = "" + file.toPath() + File.separator;
		    outputDirectoryLabel.setText(Configuration.OUTPUT_DIRECTORY);
		}
	    }
	}
    }
}
