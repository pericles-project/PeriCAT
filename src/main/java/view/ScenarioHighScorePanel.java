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

import static main.Configuration.START_ICON;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import algorithm.AbstractAlgorithm;
import decisionMechanism.Decider;
import decisionMechanism.DistanceCalculator;
import model.Criterion;
import model.Scenario;

public class ScenarioHighScorePanel extends GUIPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    private Decider decider;
    private final DefaultListModel<DistanceCalculator> listModel = new DefaultListModel<DistanceCalculator>();
    private final JList<DistanceCalculator> highscoreList = new JList<DistanceCalculator>(listModel);
    private final JButton encapsulationButton = new JButton("Use selected algorithm", START_ICON);
    private final ScenarioTab tab;
    private final JTextArea descriptionArea = new JTextArea();
    private final ListColouriser colouriser = new ListColouriser();

    public ScenarioHighScorePanel(ScenarioTab tab) {
	this.tab = tab;
	descriptionArea.setWrapStyleWord(true);
	descriptionArea.setLineWrap(true);
	descriptionArea.setEditable(false);
	highscoreList.addListSelectionListener(new SelectionListener());
	highscoreList.setCellRenderer(colouriser);
	updatePanel();
	encapsulationButton.addActionListener(this);
	encapsulationButton.setToolTipText(
		"Switch to the encapsulation tab, and preselect the chosen technique for encapsulation. This won't start the encapsulation process.");
	constraints.fill = GridBagConstraints.BOTH;
	constraints.weighty = 0;
	JTextArea informationText = new JTextArea(
		"The algorithm with the lowest criteria distance to the scenario is the recommended one.\n"
			+ "A grayed algorithm means that at least one criterion of this algorithm has the "
			+ "maximum distance to the corresponding user scenario criterion. Therefore such "
			+ "algorithm is not fully recommended, but still useable.\n");
	informationText.setEditable(false);
	informationText.setLineWrap(true);
	informationText.setWrapStyleWord(true);
	add(informationText, constraints);
	constraints.gridy++;
	add(highscoreList, constraints);
	constraints.gridy++;
	constraints.fill = GridBagConstraints.NONE;
	add(encapsulationButton, constraints);
	constraints.fill = GridBagConstraints.BOTH;
	constraints.gridy++;
	add(new JLabel("Algorithm description:"), constraints);
	constraints.gridy++;
	constraints.weighty = 0.8;
	add(descriptionArea, constraints);
	constraints.weighty = 0;
    }

    /**
     * A criterion value of the user scenario has changed. Therewith the
     * distances were newly calculated. The map has to be resorted!
     */
    protected void updateCriterionChange() {
	updateList();
    }

    protected void updatePanel() {
	Scenario scenario = tab.selectionPanel.getSelectedScenario();
	this.decider = tab.gui.controller.decisionMechanism.getDecider(scenario.ID);
	updateList();
    }

    private void updateList() {
	listModel.removeAllElements();
	for (DistanceCalculator calculator : decider.getHighscore()) {
	    listModel.addElement(calculator);
	}
	if (tab.gui.encapsulationTab != null) {
	    tab.gui.encapsulationTab.algorithmPanel.updateAlgorithmList();
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	/*
	 * Switches the tab to encapsulation tab, and selects the correct
	 * algorithm
	 */
	if (e.getSource() == encapsulationButton) {
	    if (highscoreList.getSelectedValue() != null) {
		tab.gui.tabbedPane.setSelectedIndex(0);
		tab.gui.controller.setSelectedAlgorithm(highscoreList.getSelectedValue().algorithm);
	    }
	}
    }

    private class SelectionListener implements ListSelectionListener {
	@Override
	public void valueChanged(ListSelectionEvent e) {
	    if (e.getSource() == highscoreList && !e.getValueIsAdjusting()
		    && highscoreList.getSelectedValue() != null) {
		AbstractAlgorithm algorithm = highscoreList.getSelectedValue().algorithm;
		if (algorithm == null) {
		    return;
		}
		descriptionArea.setText("Algorithm name: " + algorithm.getName() + "\n\n");
		descriptionArea.append(algorithm.getDescription() + "\n\n");
		descriptionArea.setCaretPosition(0);
	    }
	}
    }

    /**
     * If the user moves a criterion to the opposite of the algorithm criterion,
     * then the algorithm should be shown as gray in the list.
     */
    class ListColouriser extends DefaultListCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index,
		boolean isSelected, boolean cellHasFocus) {
	    Component algorithmEntry = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    Scenario algorithmScenario = ((DistanceCalculator) value).algorithm.idealScenario;
	    Scenario selectedScenario = tab.selectionPanel.getSelectedScenario();
	    algorithmEntry.setForeground(Color.black);

	    for (Entry<String, Criterion> entry : selectedScenario.getCriteria().entrySet()) {
		Criterion criterion = entry.getValue();
		if (criterion.isActive()) {
		    String key = entry.getKey();
		    int criterionDistance = DistanceCalculator
			    .eucledianDistance(algorithmScenario.getCriterion(key).getValue(), criterion.getValue());
		    if (criterionDistance == 100) { // max distance -> gray
			algorithmEntry.setForeground(Color.GRAY);
		    }
		}
	    }
	    return algorithmEntry;
	}
    }
}
