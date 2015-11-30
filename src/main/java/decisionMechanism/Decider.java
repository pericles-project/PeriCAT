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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import algorithm.AbstractAlgorithm;
import main.Configuration;
import model.Criterion;
import model.Scenario;

/**
 * This class decides which is the best algorithm to use for a given user
 * {@link Scenario} based on the calculations of the {@link DistanceCalculator}
 * s. The calculation is based on the user {@link Scenario} and the ideal
 * {@link Scenario}s of each {@link AbstractAlgorithm}.
 * 
 * This class highly depends on ONE user scenario. There is a Decider for each
 * single user scenario!
 * 
 * The {@link Decider} outputs a high score of the {@link AbstractAlgorithm}s.
 */
public class Decider {
    /**
     * The scenario setting of the user. This is a deep copy of the original
     * scenario.
     */
    public final Scenario userScenarioDeepCopy;

    /**
     * The high score is specific for the {@link #userScenarioDeepCopy} and
     * lists the {@link AbstractAlgorithm}s
     */
    private final List<DistanceCalculator> highscore = new ArrayList<DistanceCalculator>();

    /**
     * Constructor will make a deep copy of the user scenario. The algorithm
     * scenarios are always constant.
     * 
     * A {@link DistanceCalculator} will be created for each
     * {@link AbstractAlgorithm}, which keeps track of the distance from the
     * user {@link Scenario} to the algorithm {@link Scenario}.
     * 
     * @param userScenario
     */
    public Decider(Scenario userScenario) {
	this.userScenarioDeepCopy = new Scenario(userScenario);
	for (AbstractAlgorithm algorithm : Configuration.getAlgorithms()) {
	    DistanceCalculator calculator = new DistanceCalculator(userScenario, algorithm);
	    this.highscore.add(calculator);
	}
    }

    /**
     * Sorts the high score by distances before it is passed
     * 
     * @return high score
     */
    public List<DistanceCalculator> getHighscore() {
	Collections.sort(highscore);
	return highscore;
    }

    /**
     * This is called by the {@link DecisionMechanism}, if a {@link Criterion}
     * of the #userScenarioDeepCopy has changed.
     * 
     * 
     * The overall distance from the user scenario to each algorithm scenario
     * changes with + or - of the changed value.
     * 
     * @param criterion
     */
    public void updateCriterionChange(Criterion criterion) {
	String key = criterion.ID;
	int oldValue = userScenarioDeepCopy.getCriterion(key).getValue();
	int newValue = criterion.getValue();
	for (DistanceCalculator calculator : this.highscore) {
	    calculator.update(key, oldValue, newValue);
	}
	userScenarioDeepCopy.putCriterion(key, new Criterion(criterion));
    }

    /**
     * A criterion was activated or deactivated. An deactivated criterion has to
     * be excluded from calculation, and an activated has to be included.
     * 
     * @param criterionID
     * @param active
     */
    public void criterionActivationChange(String criterionID, boolean active) {
	userScenarioDeepCopy.criterionChange(criterionID, active);
	int oldValue = userScenarioDeepCopy.getCriterion(criterionID).getValue();
	if (active) {
	    for (DistanceCalculator calculator : this.highscore) {
		// The criterion was activated and its value has to be included
		// to the distance calculation:
		calculator.update(criterionID, -1, oldValue);
	    }
	} else {
	    for (DistanceCalculator calculator : this.highscore) {
		// The criterion was deactivated and its value has to be
		// excluded from the distance calculation:
		calculator.update(criterionID, oldValue, -1);
	    }
	}
    }
}
