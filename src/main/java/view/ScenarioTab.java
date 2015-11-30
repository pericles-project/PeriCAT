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

import model.Criterion;

/**
 * Experimental state!
 * 
 * This is called to create a new scenario.
 */
public class ScenarioTab extends GUITab {
    private static final long serialVersionUID = 1L;

    protected final ScenarioSelectionPanel selectionPanel;
    protected final ScenarioQuestionnairePanel questionnairePanel;
    protected final ScenarioHighScorePanel highScorePanel;
    protected final ScenarioBackPanel backPanel;
    protected final GUI gui;

    public ScenarioTab(GUI gui) {
	super("Scenarios");
	this.gui = gui;
	selectionPanel = new ScenarioSelectionPanel(this, gui.controller.scenarioController);
	questionnairePanel = new ScenarioQuestionnairePanel(this);
	highScorePanel = new ScenarioHighScorePanel(this);
	backPanel = new ScenarioBackPanel(this);
	addGuiComponent(selectionPanel, "Select Scenario", constraints);
	constraints.weighty = 0.9;
	addGuiComponent(highScorePanel, "High Score", constraints);
	constraints.weighty = 0;
	addGuiComponent(backPanel, "Back", constraints);
	// The questionnaire should be placed at the right of the other
	// components:
	constraints.gridy = 1;
	constraints.gridx = 1;
	constraints.gridheight = 4;// 4 other components (select,name,high,back)
	addGuiComponent(questionnairePanel, "Edit Scenario", constraints);
	updateScenarioSelection();
    }

    protected void updateCriterionChange(Criterion criterion) {
	gui.controller.scenarioController.updateCriterionValueChange(selectionPanel.getSelectedScenario(), criterion);
	highScorePanel.updateCriterionChange();
    }

    protected void updateScenarioSelection() {
	questionnairePanel.updatePanel();
	highScorePanel.updatePanel();
	this.repaint();
	this.revalidate();
    }
}
