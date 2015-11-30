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

import model.RestoredFile;

public class DecapsulationTab extends GUITab {
    private static final long serialVersionUID = 1L;
    protected final DecapsulationAlgorithmPanel decapsulationAlgorithmPanel;
    public final DecapsulationDataPanel decapsulationDataPanel;
    protected final DecapsulationOutputPanel decapsulationOutputPanel;
    protected final GUI gui;

    public DecapsulationTab(GUI gui) {
	super("Information Decapsulation");
	this.gui = gui;
	decapsulationDataPanel = new DecapsulationDataPanel(this);
	decapsulationAlgorithmPanel = new DecapsulationAlgorithmPanel();
	decapsulationOutputPanel = new DecapsulationOutputPanel();
	addGuiComponent(decapsulationAlgorithmPanel, "Algorithms for decapsulation", constraints);
	addGuiComponent(decapsulationDataPanel, "Files to be decapsulated", constraints);
	addGuiComponent(decapsulationOutputPanel, "Restored Files", constraints);
    }

    public void updateOutputFiles(List<RestoredFile> restoredFiles) {
	decapsulationOutputPanel.updateOutputFiles(restoredFiles);
    }
}
