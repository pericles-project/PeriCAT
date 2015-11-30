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
package decisionMechanism;

import java.util.Hashtable;

import controller.ScenarioController;
import model.Criterion;
import model.Scenario;

/**
 * Main controlling class of all decision calculations.
 * 
 * The mechanism has to be updated by the {@link ScenarioController} in case a
 * {@link Scenario} is added, deleted, or has changed.
 * 
 * The mechanism keeps for every user/role {@link Scenario} (this includes not
 * the ideal algorithm scenarios) a {@link Decider}, which manages the scenario
 * specific calculations and provides a high score.
 */
public class DecisionMechanism {

    /**
     * This table keeps a {@link Decider} for each user {@link Scenario}. The
     * key String is the user scenarios ID.
     */
    private final Hashtable<String, Decider> deciderTable = new Hashtable<String, Decider>();

    /**
     * Constructor of the decision mechanism.
     */
    public DecisionMechanism() {
    }

    /**
     * This should be called by the {@link ScenarioController}, if a new
     * {@link Scenario} was created. The mechanism will create a new
     * {@link Decider} for the scenario.
     * 
     * @param scenario
     */
    public void addScenario(Scenario scenario) {
	Decider decider = new Decider(scenario);
	deciderTable.put(scenario.ID, decider);
    }

    /**
     * This should be called by the {@link ScenarioController}, if a
     * {@link Scenario} was deleted. This will delete the corresponding
     * {@link Decider}.
     * 
     * @param scenarioID
     */
    public void deleteScenario(String scenarioID) {
	deciderTable.remove(scenarioID);
    }

    /**
     * This should be called by the {@link ScenarioController}, if a
     * {@link Criterion} of a {@link Scenario} with the passed scenario ID has
     * changed. This will adjust the distances from the user scenario to each
     * algorithm scenario.
     * 
     * @param scenarioID
     *            the ID of the user {@link Scenario}
     * @param changedCriterion
     */
    public void updateScenario(String scenarioID, Criterion changedCriterion) {
	deciderTable.get(scenarioID).updateCriterionChange(changedCriterion);
    }

    public Decider getDecider(String scenario) {
	return deciderTable.get(scenario);
    }

    /**
     * This should be called by the {@link ScenarioController}, if a
     * {@link Criterion} of a {@link Scenario} with the passed scenario ID has
     * changed its activation status.
     * 
     * A criterion was activated or deactivated. An deactivated criterion has to
     * be excluded from calculation, and an activated has to be included.
     * 
     * @param scenarioID
     * @param criterionID
     * @param active
     */
    public void criterionActivationChange(String scenarioID, String criterionID, boolean active) {
	Decider decider = deciderTable.get(scenarioID);
	decider.criterionActivationChange(criterionID, active);
    }
}
