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

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import algorithm.AbstractAlgorithm;
import controller.PeriCATController;
import main.Configuration;

/**
 * GUI panel that displays the list of available algorithms for decapsulation.
 */
class DecapsulationAlgorithmPanel extends GUIPanel {
    private static final long serialVersionUID = 1L;
    private final JList<AbstractAlgorithm> algorithmList = new JList<AbstractAlgorithm>(
	    new DefaultListModel<AbstractAlgorithm>());
    private final JTextArea descriptionArea = new JTextArea();

    protected DecapsulationAlgorithmPanel() {
	constraints.gridwidth = 2;
	descriptionArea.setText("Select all algorithms that might have been used for the information encapsulation. "
		+ "\nThe tool will check each algorithm and use the right algorithm for the restoration of"
		+ " the payload and carrier files, if available and possible.\n"
		+ "Select only one algorithm, if you know which one was used.");
	descriptionArea.setEditable(false);
	JScrollPane descriptionPane = new JScrollPane(descriptionArea);
	add(descriptionPane, constraints);
	constraints.gridwidth = 1;
	constraints.weightx = 0.5;
	constraints.gridy++;
	algorithmList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	fillAlgorithmList(Configuration.getAlgorithms());
	add(algorithmList, constraints);
    }

    /**
     * Fills the algorithm list with the algorithms provided by the
     * {@link PeriCATController}
     * 
     * @param algorithms
     */
    protected void fillAlgorithmList(List<AbstractAlgorithm> algorithms) {
	((DefaultListModel<AbstractAlgorithm>) algorithmList.getModel()).removeAllElements();
	for (AbstractAlgorithm algorithm : algorithms) {
	    ((DefaultListModel<AbstractAlgorithm>) algorithmList.getModel()).addElement(algorithm);
	}
    }

    protected List<AbstractAlgorithm> getSelectedAlgorithms() {
	return algorithmList.getSelectedValuesList();
    }
}
