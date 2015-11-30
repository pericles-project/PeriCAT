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
package controller;

import java.io.File;
import java.util.Vector;

import algorithm.AbstractAlgorithm;
import decisionMechanism.DecisionMechanism;
import model.Criterion;
import model.Scenario;
import view.ScenarioQuestionnairePanel;

/**
 * This class manages the user {@link Scenario}s, which include the predefined
 * scenario examples, and user created scenarios.
 * 
 * Any scenario changes (new, delete, change) will be announced to the
 * {@link DecisionMechanism} to calculate which is the best
 * {@link AbstractAlgorithm} to be used for the encapsulation in a
 * {@link Scenario}.
 */
public class ScenarioController {
    /** The list of all loaded user scenarios */
    private final Vector<Scenario> scenarios = new Vector<Scenario>();
    /** The decision mechanism has to be updated in case of scenario changes */
    private final DecisionMechanism decisionMechanism;

    /**
     * The constructor will load all user scenarios saved in the scenarios
     * directory, as well as the predefined scenarios.
     * 
     * @param decisionMechanism
     */
    public ScenarioController(DecisionMechanism decisionMechanism) {
	this.decisionMechanism = decisionMechanism;
	createNewScenario();
    }

    public void loadSavedScenarios() {
	for (Scenario scenario : ScenarioSaverAndLoader.loadAll()) {
	    if (this.getScenario(scenario.ID) == null) {
		addScenario(scenario);
	    }
	}
    }

    /**
     * Returns the {@link Scenario} with the specified ID
     * 
     * @param ID
     * @return scenario
     */
    public Scenario getScenario(String ID) {
	for (Scenario listedScenario : scenarios) {
	    if (listedScenario.ID.equals(ID)) {
		return listedScenario;
	    }
	}
	return null;
    }

    /**
     * This will be called by the {@link ScenarioQuestionnairePanel} in case the
     * user changed the value of a scenario criterion. This method will update
     * the {@link DecisionMechanism} to adjust the distance calculation and
     * therewith the high score.
     * 
     * @param scenario
     * @param criterion
     */
    public void updateCriterionValueChange(Scenario scenario, Criterion criterion) {
	decisionMechanism.updateScenario(scenario.ID, criterion);
    }

    /**
     * Deletes a scenario from the list of scenarios. This will also delete the
     * scenario file, if existing.
     * 
     * @param scenario
     */
    public void deleteScenario(Scenario scenario) {
	if (scenarios.size() == 1) {
	    // Number of scenarios should never be less than one!
	    createNewScenario();
	}
	decisionMechanism.deleteScenario(scenario.ID);
	File scenarioFile = scenario.file;
	if (scenarioFile != null && scenarioFile.exists()) {
	    scenarioFile.delete();
	}
	scenarios.remove(scenario);
	if (scenarios.size() == 0) {
	    createNewScenario();
	}
    }

    /**
     * @return A list of all scenarios
     */
    public Vector<Scenario> getScenarios() {
	if (scenarios.size() == 0) {
	    createNewScenario();
	}
	return this.scenarios;
    }

    /**
     * Creates a new {@link Scenario} and adds it to the scenarios list.
     * 
     * @return the new created scenario
     */
    public Scenario createNewScenario() {
	Scenario newScenario = new Scenario(getUniqueName("NewScenario", 0));
	if (PeriCATController.verbose) {
	    System.out.println("Created new scenario: " + newScenario.name);
	}
	return addScenario(newScenario);
    }

    /**
     * Add a {@link Scenario} to the scenarios list, and update the
     * {@link DecisionMechanism}
     * 
     * @param scenario
     * @return the added scenario
     */
    public Scenario addScenario(Scenario scenario) {
	scenarios.add(scenario);
	decisionMechanism.addScenario(scenario);
	return scenario;
    }

    /*
     * Adds a number at the end of the name, if the name is already used.
     * Increases the number, until a unique name is found.
     */
    private String getUniqueName(String suggestedName, int iteration) {
	for (Scenario scenario : scenarios) {
	    if (iteration == 0 && scenario.name.equals(suggestedName)) {
		return getUniqueName(suggestedName, ++iteration);
	    } else if (iteration > 0 && scenario.name.equals(suggestedName + iteration)) {
		return getUniqueName(suggestedName, ++iteration);
	    }
	}
	if (iteration == 0) {
	    return suggestedName;
	} else {
	    return suggestedName + iteration;
	}
    }

    /**
     * Changes the active status of a criterion. Informs the decision mechanism
     * only if the active status really changed.
     * 
     * @param scenarioID
     * @param criterionID
     * @param active
     */
    public void criterionActivationChange(String scenarioID, String criterionID, boolean active) {
	Scenario scenario = getScenario(scenarioID);
	if (active != scenario.getCriterion(criterionID).isActive()) {
	    scenario.criterionChange(criterionID, active);
	    decisionMechanism.criterionActivationChange(scenarioID, criterionID, active);
	}
    }
}
