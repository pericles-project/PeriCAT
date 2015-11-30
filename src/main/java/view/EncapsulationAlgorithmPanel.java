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

import java.awt.Color;
import java.awt.Component;
import java.util.Enumeration;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import algorithm.AbstractAlgorithm;
import algorithm.ZipPackaging;
import decisionMechanism.Decider;
import decisionMechanism.DistanceCalculator;
import model.EncapsulationData;
import model.Scenario;

/**
 * GUI panel that displays the list of scored algorithms, and a description
 * about the algorithms and their score.
 */
class EncapsulationAlgorithmPanel extends GUIPanel {
    private static final long serialVersionUID = 1L;

    private final DefaultListModel<DistanceCalculator> listModel = new DefaultListModel<DistanceCalculator>();
    private final JList<DistanceCalculator> algorithmList = new JList<DistanceCalculator>(listModel);
    private final JTextArea descriptionArea = new JTextArea();
    private final JScrollPane configurationPanel = new JScrollPane();
    private final EncapsulationTab tab;
    private final ListColouriser colouriser;

    protected EncapsulationAlgorithmPanel(EncapsulationTab tab) {
	this.tab = tab;
	colouriser = new ListColouriser();
	algorithmList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	algorithmList.addListSelectionListener(new SelectionListener());
	algorithmList.setCellRenderer(colouriser);
	updateAlgorithmList();
	constraints.weighty = 0;
	constraints.gridwidth = 2;
	add(new JLabel("Note: Red algortihms can't be applied on the given data."), constraints);
	constraints.gridy++;
	add(algorithmList, constraints);
	constraints.gridy++;
	add(new JLabel("Algorithm description: "), constraints);
	constraints.gridy++;
	constraints.weighty = 1;
	descriptionArea.setWrapStyleWord(true);
	descriptionArea.setLineWrap(true);
	descriptionArea.setEditable(false);
	add(descriptionArea, constraints);
	constraints.gridy++;
	configurationPanel.setViewportView(new JLabel("Algorithm configuration panel"));
	add(configurationPanel, constraints);
    }

    /**
     * This method will ask for the high score of the currently selected user
     * scenario, and be able to display the algorithms with their corresponding
     * distances. The colouriser ensures that non-usable algorithms will be
     * displayed in red.
     */
    protected void updateAlgorithmList() {

	Scenario selectedScenario = tab.gui.scenarioTab.selectionPanel.getSelectedScenario();
	Decider decider = tab.gui.controller.decisionMechanism.getDecider(selectedScenario.ID);

	listModel.removeAllElements();

	for (DistanceCalculator calculator : decider.getHighscore()) {
	    listModel.addElement(calculator);
	}
    }

    /**
     * This method will change the colour of the presented algorithms based on
     * if they can be used for the selected dataset or not.
     * 
     * @param selectedData
     */
    public void colouriseAlgorithmList(EncapsulationData selectedData) {
	revalidate();
	repaint();
    }

    /**
     * This class is used as cell renderer by the algorithms JList. It colours
     * the entries depending on whether the entry algorithm is in the list of
     * possibly usable algorithms of the currently selected encapsulation
     * dataset.
     */
    class ListColouriser extends DefaultListCellRenderer {
	private static final long serialVersionUID = 1L;

	public ListColouriser() {
	}

	@Override
	public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index,
		boolean isSelected, boolean cellHasFocus) {
	    Component algorithmEntry = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    EncapsulationData selectedData = tab.selectedDatasetPanel.getSelectedDataset();
	    if (selectedData != null
		    && selectedData.possibleAlgorithms.contains(((DistanceCalculator) value).algorithm)) {
		algorithmEntry.setForeground(Color.black);
	    } else {
		algorithmEntry.setForeground(Color.red);
	    }
	    return algorithmEntry;
	}
    }

    private class SelectionListener implements ListSelectionListener {
	@Override
	public void valueChanged(ListSelectionEvent e) {
	    if (e.getSource() == algorithmList && !e.getValueIsAdjusting()
		    && algorithmList.getSelectedValue() != null) {
		AbstractAlgorithm algorithm = algorithmList.getSelectedValue().algorithm;
		if (algorithm == null) {
		    return;
		}
		descriptionArea.setText("Algorithm name: " + algorithm.getName() + "\n\n");
		descriptionArea.append(algorithm.getDescription() + "\n\n");
		descriptionArea.setCaretPosition(0);
		tab.gui.controller.setSelectedAlgorithm(algorithm);
		if (algorithm.panel != null) {
		    configurationPanel.setViewportView(algorithm.panel);
		} else {
		    configurationPanel.setViewportView(new JLabel("Algorithm configuration panel"));
		}
	    }
	}
    }

    public void setSelectedAlgorithm(AbstractAlgorithm selectedAlgorithm) {
	if (selectedAlgorithm == null) {
	    return;
	}
	Enumeration<DistanceCalculator> calculatorIterator = listModel.elements();
	while (calculatorIterator.hasMoreElements()) {
	    DistanceCalculator calculator = calculatorIterator.nextElement();
	    if (calculator.algorithm.getName().equals(selectedAlgorithm.getName())) {
		algorithmList.setSelectedValue(calculator, true);
		return;
	    }
	}
    }

    public AbstractAlgorithm getSelectedAlgorithm() {
	if (algorithmList.getSelectedValue() == null) {
	    /*
	     * No algorithm is selected, but the user pressed the encapsulate
	     * button. Select zip packaging as default algorithm, because it
	     * will work for all data sets.
	     */
	    return tab.gui.controller.getAlgorithm(new ZipPackaging().getName());
	}
	return algorithmList.getSelectedValue().algorithm;
    }

    public AbstractAlgorithm getFirstAlgorithm() {
	return listModel.get(0).algorithm;
    }
}
