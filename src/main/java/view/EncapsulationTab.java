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

import algorithm.AbstractAlgorithm;
import model.EncapsulationData;

public class EncapsulationTab extends GUITab {
    private static final long serialVersionUID = 1L;
    protected final EncapsulationDatasetPanel selectedDatasetPanel;
    protected final EncapsulationAlgorithmPanel algorithmPanel;
    protected final EncapsulationOutputPanel outputPanel;
    protected GUI gui;

    public EncapsulationTab(GUI gui) {
	super("Information Encapsulation");
	this.gui = gui;
	selectedDatasetPanel = new EncapsulationDatasetPanel(this);
	algorithmPanel = new EncapsulationAlgorithmPanel(this);
	outputPanel = new EncapsulationOutputPanel(this);
	addGuiComponent(selectedDatasetPanel, "Datasets", constraints);
	addGuiComponent(algorithmPanel, "Encapsulation Algorithms", constraints);
	constraints.weighty = 1;
	addGuiComponent(outputPanel, "Output Files", constraints);
    }

    public void addDataset(EncapsulationData data) {
	selectedDatasetPanel.addDataset(data);
    }

    public void removeDataset(EncapsulationData data) {
	selectedDatasetPanel.removeDataset(data);
    }

    public void showDatasetInformation() {
	selectedDatasetPanel.showDatasetInformation();
    }

    public void setSelectedAlgorithm(AbstractAlgorithm selectedAlgorithm) {
	algorithmPanel.setSelectedAlgorithm(selectedAlgorithm);
    }

    public void refreshGUI(EncapsulationData dataset) {
	selectedDatasetPanel.showFiles(dataset);
    }

    public AbstractAlgorithm getSelectedAlgorithm() {
	return algorithmPanel.getSelectedAlgorithm();
    }
}
