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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import decisionMechanism.DecisionMechanism;
import model.Criterion;
import model.Scenario;

public class ScenarioControllerTest {
    ScenarioController scenarioController;

    @Before
    public void setUp() {
	scenarioController = new ScenarioController(new DecisionMechanism());
    }

    @Test
    public void getScenarioTest() {
	// only the start scenario
	assertEquals(1, scenarioController.getScenarios().size());
	String id = scenarioController.getScenarios().get(0).ID;
	Scenario scenario = scenarioController.getScenario(id);
	assertNotNull(scenario);
    }

    @Test
    public void updateCriterionValueChangeTest() {
	int oldValue = 50;
	int newValue = 60;
	Scenario scenario = scenarioController.getScenarios().get(0);
	Criterion criterion = scenario.getCriterion(Criterion.CARRIER_PROCESSABILITY);
	assertNotNull(criterion);
	assertEquals(oldValue, criterion.getValue());
	criterion.setValue(newValue);
	scenarioController.updateCriterionValueChange(scenario, criterion);
	Criterion criterionNew = scenario.getCriterion(Criterion.CARRIER_PROCESSABILITY);
	assertEquals(newValue, criterionNew.getValue());
    }

    @Test
    public void deleteScenarioTest() {
	assertEquals(1, scenarioController.getScenarios().size());
	scenarioController.deleteScenario(scenarioController.getScenarios().get(0));
	// the number of scenarios should never be less than one!
	assertEquals(1, scenarioController.getScenarios().size());
	scenarioController.createNewScenario();
	assertEquals(2, scenarioController.getScenarios().size());
	scenarioController.deleteScenario(scenarioController.getScenarios().get(0));
	assertEquals(1, scenarioController.getScenarios().size());
    }

    @Test
    public void addScenarioTest() {
	Scenario scenario = new Scenario("TestScenario");
	assertEquals(1, scenarioController.getScenarios().size());
	scenarioController.addScenario(scenario);
	assertEquals(2, scenarioController.getScenarios().size());
	Scenario scenario2 = scenarioController.getScenario(scenario.ID);
	assertEquals(scenario, scenario2);
    }

    @Test
    public void criterionActivationChangeTest() {
	Scenario scenario = new Scenario("TestScenario");
	scenarioController.addScenario(scenario);
	Criterion criterion = scenario.getCriterion(Criterion.CARRIER_PROCESSABILITY);
	assertTrue(criterion.isActive());
	scenarioController.criterionActivationChange(scenario.ID, criterion.ID, false);
	assertFalse(criterion.isActive());
    }
}
