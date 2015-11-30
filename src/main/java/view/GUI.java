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

import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import algorithm.AbstractAlgorithm;
import controller.PeriCATController;
import model.EncapsulationData;
import model.RestoredFile;

/**
 * Main GUI class. Provides access to the {@link PeriCATController} for the
 * other view components.
 */
public class GUI extends JFrame {
    private static final long serialVersionUID = 1L;
    protected final PeriCATController controller;
    protected final JTabbedPane tabbedPane = new JTabbedPane();;

    protected final EncapsulationTab encapsulationTab;
    protected final DecapsulationTab decapsulationTab;
    protected ScenarioTab scenarioTab;

    public GUI(final PeriCATController controller) {
	this.controller = controller;
	setTitle("PeriCAT - PERICLES Content Aggregation Tool");
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	addWindowListener(new java.awt.event.WindowAdapter() {
	    @Override
	    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		controller.exit();
	    }
	});
	setExtendedState(Frame.MAXIMIZED_BOTH);
	scenarioTab = new ScenarioTab(this);
	encapsulationTab = new EncapsulationTab(this);
	decapsulationTab = new DecapsulationTab(this);
	tabbedPane.addTab("Information Encapsulation", encapsulationTab);
	tabbedPane.addTab("Information Decapsulation", decapsulationTab);
	tabbedPane.addTab("Scenario", scenarioTab);
	this.add(tabbedPane);
	encapsulationTab.showDatasetInformation();
	this.setMinimumSize(new Dimension(200, 200));
	this.setVisible(true);
    }

    /**
     * A data set was added to the tool.The data set view panel has to be
     * updated.
     * 
     * @param data
     */
    public void addDataset(EncapsulationData data) {
	encapsulationTab.addDataset(data);
    }

    /**
     * A data set was removed from tool. The data set view panel has to be
     * updated.
     * 
     * @param data
     */
    public void removeDataset(EncapsulationData data) {
	encapsulationTab.removeDataset(data);
    }

    public void updateRestored(List<RestoredFile> restoredFiles) {
	if (restoredFiles == null) {
	    return;
	}
	if (PeriCATController.verbose) {
	    System.out.println("The following files are restored: ");
	    for (File file : restoredFiles) {
		System.out.println("\t" + file.getAbsolutePath());
	    }
	}
	decapsulationTab.updateOutputFiles(restoredFiles);
    }

    public void setSelectedAlgorithm(AbstractAlgorithm selectedAlgorithm) {
	encapsulationTab.setSelectedAlgorithm(selectedAlgorithm);
    }

    public void refreshGUI(EncapsulationData dataset) {
	encapsulationTab.refreshGUI(dataset);
    }
}
