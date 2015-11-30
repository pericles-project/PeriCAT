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
import static main.Configuration.SAVE_ICON;
import static main.Configuration.SCENARIO_DIRECTORY;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import controller.ScenarioController;
import controller.ScenarioSaverAndLoader;
import model.Scenario;

/**
 * This class shows a list of scenarios (= roles) at the encapsulation view tab.
 */
public class ScenarioSelectionPanel extends GUIPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final ScenarioTab tab;
    private final ScenarioController scenarioController;
    private final DefaultComboBoxModel<Scenario> model = new DefaultComboBoxModel<Scenario>();
    private final JComboBox<Scenario> scenarioBox = new JComboBox<Scenario>(model);
    private final JTextField nameField = new JTextField(25);
    private JTextArea scenarioDescription = new JTextArea();
    private final JButton createButton = new JButton("Create new scenario", ADD_ICON);
    private final JButton importButton = new JButton("Import scenario");
    private final JButton deleteButton = new JButton("Delete scenario", DELETE_ICON);
    private final JButton nameSaveButton = new JButton("Save name and description", SAVE_ICON);
    private Scenario selectedScenario;

    public ScenarioSelectionPanel(ScenarioTab tab, ScenarioController scenarioController) {
	this.tab = tab;
	this.scenarioController = scenarioController;
	selectedScenario = scenarioController.getScenarios().get(0);
	for (Scenario scenario : scenarioController.getScenarios()) {
	    model.addElement(scenario);
	}
	scenarioBox.setSelectedItem(selectedScenario);
	scenarioBox.addActionListener(this);
	createButton.addActionListener(this);
	importButton.addActionListener(this);
	importButton.setToolTipText("Load a scenario from file.");
	deleteButton.addActionListener(this);
	deleteButton.setToolTipText("Delete the selected scenario.");
	nameSaveButton.addActionListener(this);
	nameSaveButton.setToolTipText("Save name and description changes of the scenario to file.");
	constraints.weighty = 0;
	add(new JLabel("Select scenario, or create new scenario."), constraints);
	constraints.gridy++;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	add(scenarioBox, constraints);
	constraints.fill = GridBagConstraints.BOTH;
	constraints.gridy++;
	constraints.gridheight = 2;
	scenarioDescription = new JTextArea(selectedScenario.description);
	scenarioDescription.setLineWrap(true);
	scenarioDescription.setWrapStyleWord(true);
	add(scenarioDescription, constraints);
	constraints.gridheight = 1;
	constraints.gridx++;
	constraints.gridy = 1;
	constraints.fill = GridBagConstraints.HORIZONTAL;
	add(createButton, constraints);
	constraints.gridy++;
	add(importButton, constraints);
	constraints.gridy++;
	add(deleteButton, constraints);
	constraints.gridy++;
	constraints.gridx = 0;
	nameField.setText(selectedScenario.name);
	add(nameField, constraints);
	constraints.gridx++;
	add(nameSaveButton, constraints);
	constraints.fill = GridBagConstraints.BOTH;
	constraints.gridx = 0;
	constraints.gridy = 0;
    }

    /**
     * Get the selected scenario from the view scenario list.
     * 
     * @return the scenario which is selected at the view list
     */
    public Scenario getSelectedScenario() {
	return selectedScenario;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == scenarioBox) {
	    selectScenario((Scenario) scenarioBox.getSelectedItem());
	} else if (e.getSource() == createButton) {
	    createNewScenario();
	} else if (e.getSource() == importButton) {
	    importScenario();
	} else if (e.getSource() == deleteButton) {
	    deleteScenario();
	} else if (e.getSource() == nameSaveButton) {
	    changeScenarioName();
	}
    }

    private void selectScenario(Scenario scenario) {
	if (selectedScenario != null && selectedScenario != scenario) {
	    if (model.getSelectedItem() != scenario) {
		model.setSelectedItem(scenario);
	    }
	    selectedScenario = scenario;
	    scenarioDescription.setText(selectedScenario.description);
	    nameField.setText(selectedScenario.name);
	    tab.updateScenarioSelection();
	}
    }

    private void createNewScenario() {
	addScenario(scenarioController.createNewScenario());
    }

    private void importScenario() {
	JFrame frame = new JFrame();
	JFileChooser fileChooser = new JFileChooser();
	fileChooser.setDialogTitle("Import scenario");
	int selection = fileChooser.showOpenDialog(frame);
	if (selection == JFileChooser.APPROVE_OPTION) {
	    File selectedFile = fileChooser.getSelectedFile();
	    Scenario importedScenario = ScenarioSaverAndLoader.load(selectedFile.getAbsolutePath());
	    scenarioController.addScenario(importedScenario);
	    addScenario(importedScenario);
	}
    }

    private void addScenario(Scenario newScenario) {
	model.addElement(newScenario);
	selectScenario(newScenario);
    }

    private void deleteScenario() {
	scenarioBox.removeActionListener(this);
	model.removeElement(selectedScenario);
	scenarioController.deleteScenario(selectedScenario);
	if (model.getSize() > 0) {
	    selectScenario(scenarioBox.getItemAt(0));
	} else {
	    createNewScenario();
	}
	scenarioBox.addActionListener(this);
    }

    private void changeScenarioName() {
	String name = nameField.getText();
	if (name != null && !name.equals("")) {
	    new File(SCENARIO_DIRECTORY + selectedScenario.name).delete();
	    selectedScenario.name = name;
	}
	String description = scenarioDescription.getText();
	if (description != null && !description.equals("")) {
	    selectedScenario.description = description;
	}
	tab.updateScenarioSelection();
	ScenarioSaverAndLoader.save(selectedScenario);
    }

}
