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

import static main.Configuration.DELETE_ICON;
import static main.Configuration.SAVE_ICON;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import controller.ScenarioSaverAndLoader;
import model.Criterion;
import model.Scenario;

public class ScenarioQuestionnairePanel extends GUIPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    final int MIN = 0;
    final int MAX = 100;
    private final ScenarioTab tab;
    private final JButton saveButton = new JButton("Save", SAVE_ICON);
    private final JButton saveToButton = new JButton("Save to...", SAVE_ICON);
    private final JButton resetButton = new JButton("Reset scenario");

    /**
     * This scenario is initialised with the values from the selected scenario.
     * If the user wants to reset changes, the values from this scenario will be
     * loaded back to the selected scenario.
     */
    private Scenario resetScenario;

    public ScenarioQuestionnairePanel(ScenarioTab tab) {
	this.tab = tab;
	constraints.anchor = GridBagConstraints.WEST;
	saveButton.addActionListener(this);
	saveButton.setToolTipText("Save the scenario to file. It will be stored at the tools scenarios directory.");
	saveToButton.addActionListener(this);
	saveToButton.setToolTipText("Save the scenario to a specific file.");
	resetButton.addActionListener(this);
	resetButton.setToolTipText("Load the values from last save of the scenario.");
    }

    /**
     * Updates the questionnaire in case the selected scenario changed, or the
     * scenario name changed.
     */
    public void updatePanel() {
	removeAll();
	saveResetCopy();
	drawGUI();
	this.repaint();
	this.revalidate();
    }

    /**
     * Makes a deep copy of the selected scenario to be able to reset changes.
     */
    private void saveResetCopy() {
	resetScenario = new Scenario(tab.selectionPanel.getSelectedScenario());
    }

    /**
     * Creates the graphical user interface. This method defines the look and
     * feel of the scenario questionnaire.
     */
    private void drawGUI() {
	constraints.gridwidth = 3;
	add(new JLabel("<html><h2>Scenario: " + tab.selectionPanel.getSelectedScenario().name + "</h2></html>"),
		constraints);
	constraints.gridy++;
	add(new JLabel("Consider the tool tips for a description of the features."), constraints);
	constraints.gridy++;
	List<Criterion> sliderCriteria = new ArrayList<Criterion>();
	List<Criterion> weightCriteria = new ArrayList<Criterion>();
	for (Entry<String, Criterion> entry : tab.selectionPanel.getSelectedScenario().getCriteria().entrySet()) {
	    Criterion criterion = entry.getValue();
	    if (criterion.leftLabel.equals("important")) {
		/*
		 * The interface looks better, if there is a distinction between
		 * the weighting criteria and the two feature criteria
		 */
		weightCriteria.add(criterion);
	    } else {
		sliderCriteria.add(criterion);
	    }
	}
	addQuestionCategory(constraints, "Weight between the two features:");
	addCriteria(constraints, sliderCriteria);
	addQuestionCategory(constraints, "Weight the following features:");
	addCriteria(constraints, weightCriteria);
	constraints.gridy++;
	add(resetButton, constraints);
	constraints.gridy++;
	add(saveButton, constraints);
	constraints.gridx++;
	add(saveToButton, constraints);
	constraints.gridx = 0;
	constraints.gridy = 0;
    }

    private void addQuestionCategory(GridBagConstraints constraints, String title) {
	constraints.gridwidth = 4;
	this.add(new JLabel("<html><h2>" + title + "</h2></html>"), constraints);
	constraints.gridwidth = 1;
	constraints.gridy++;
    }

    /**
     * Adds a list of criteria to the scenario questionnaire.
     * 
     * @param constraints
     * @param criteria
     */
    private void addCriteria(GridBagConstraints constraints, List<Criterion> criteria) {
	for (Criterion criterion : criteria) {
	    GUICriterion guiCriterion = new GUICriterion(criterion);
	    constraints.gridx = 0;
	    add(guiCriterion.nameLabel, constraints);
	    constraints.gridx++;
	    constraints.fill = GridBagConstraints.NONE;
	    constraints.anchor = GridBagConstraints.EAST;
	    add(guiCriterion.excludeButton, constraints);
	    constraints.fill = GridBagConstraints.HORIZONTAL;
	    constraints.gridx = 0;
	    constraints.gridy++;
	    constraints.gridwidth = 3;
	    add(guiCriterion.description, constraints);
	    constraints.gridwidth = 1;
	    constraints.gridy++;
	    constraints.gridwidth = 2;
	    add(guiCriterion.slider, constraints);
	    add(guiCriterion.excludedLabel, constraints);
	    constraints.gridy++;
	    add(new JSeparator(SwingConstants.HORIZONTAL), constraints);
	    constraints.gridwidth = 1;
	    constraints.gridy++;
	}
	constraints.gridy++;
	constraints.gridx = 0;
    }

    private final String excludeToolTip = "<html><p width=\"300\">Click this toggle button to exclude the criterion from the calculation.\n"
	    + "This is recommended for criteria which are irrelevant for a scenario.</p></html>";

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == saveToButton) {
	    JFrame frame = new JFrame();
	    JFileChooser fileChooser = new JFileChooser();
	    fileChooser.setDialogTitle("Save the scenario");
	    int selection = fileChooser.showSaveDialog(frame);
	    if (selection == JFileChooser.APPROVE_OPTION) {
		File file = fileChooser.getSelectedFile();
		resetScenario = new Scenario(tab.selectionPanel.getSelectedScenario());
		ScenarioSaverAndLoader.saveTo(tab.selectionPanel.getSelectedScenario(), file);
	    }
	} else if (e.getSource() == saveButton) {
	    resetScenario = new Scenario(tab.selectionPanel.getSelectedScenario());
	    ScenarioSaverAndLoader.save(tab.selectionPanel.getSelectedScenario());
	} else if (e.getSource() == resetButton) {
	    resetScenarioChanges();
	}
    }

    /**
     * The reset button was pressed. This method will load the old values of the
     * scenario, which are saved in the #resetScenario, back to the
     * #tab.selectedScenario.
     */
    private void resetScenarioChanges() {
	for (Criterion criterion : resetScenario.getCriteria().values()) {
	    tab.selectionPanel.getSelectedScenario().setCriterionValue(criterion.ID, criterion.getValue());
	}
	updatePanel();
    }

    /**
     * GUI representation of a criterion.
     */
    class GUICriterion implements ActionListener, ChangeListener {
	public Criterion criterion;
	public JLabel nameLabel;
	public JLabel leftLabel;
	public JLabel rightLabel;
	public JTextArea description;
	public JSlider slider;
	public JToggleButton excludeButton = new JToggleButton(DELETE_ICON);
	public final JLabel excludedLabel = new JLabel(
		"<html><font color='gray'>excluded from the calculation</gray></html>");

	public GUICriterion(Criterion criterion) {
	    int value = criterion.getValue();
	    if (value == -1) {
		/* The slider position of excluded criteria is 50 = middle */
		value = 50;
	    }
	    slider = new JSlider(JSlider.HORIZONTAL, MIN, MAX, value);
	    this.criterion = criterion;
	    this.nameLabel = new JLabel("<html><h2>" + criterion.name + "</h2></html>");
	    this.description = new JTextArea(criterion.description);
	    this.description.setLineWrap(true);
	    this.description.setWrapStyleWord(true);
	    this.leftLabel = new JLabel(criterion.leftLabel);
	    this.rightLabel = new JLabel(criterion.rightLabel);
	    this.excludeButton.addActionListener(this);
	    this.excludeButton.setSelected(!criterion.isActive());
	    setVisibility();
	    this.excludeButton.setToolTipText(excludeToolTip);
	    Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
	    labelTable.put(new Integer(0), leftLabel);
	    labelTable.put(new Integer(100), rightLabel);
	    slider.setLabelTable(labelTable);
	    slider.addChangeListener(this);
	    slider.setMinorTickSpacing(25);
	    slider.setMajorTickSpacing(50);
	    slider.setPaintTicks(true);
	    slider.setPaintLabels(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == excludeButton) {
		String scenarioID = tab.selectionPanel.getSelectedScenario().ID;
		tab.gui.controller.scenarioController.criterionActivationChange(scenarioID, criterion.ID,
			!excludeButton.isSelected());
		setVisibility();
		tab.highScorePanel.updateCriterionChange();
	    }
	}

	private void setVisibility() {
	    if (criterion.isActive()) {
		slider.setVisible(true);
		excludedLabel.setVisible(false);
	    } else {
		slider.setVisible(false);
		excludedLabel.setVisible(true);
	    }
	}

	/**
	 * Detects slider changes and initiates the adaption of the decision
	 * mechanism.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
	    JSlider slider = (JSlider) e.getSource();
	    if (!slider.getValueIsAdjusting()) {
		criterion.setValue(slider.getValue());
		tab.updateCriterionChange(criterion);
	    }
	}
    }
}
