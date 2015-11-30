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
import java.util.Map.Entry;

import algorithm.AbstractAlgorithm;
import model.Criterion;
import model.Scenario;

/**
 * This class calculates the distance between the criteria of two scenarios.
 */
public class DistanceCalculator implements Comparable<DistanceCalculator> {

    /** The compared algorithm */
    public final AbstractAlgorithm algorithm;
    /** The sum of all criteria distances between the two scenarios */
    private int overallDistance = -1;

    /**
     * Constructor
     *
     * Calculates the initial #overallDistance from the #userScenarioDeepCopy to
     * the #algorithmScenario.
     * 
     * Therefore the eucledian distance for each criterion is calculated an the
     * distances from all criteria summed up.
     *
     * Ensures that the same criterion of each scenario is used for the
     * calculation, and not a different criterion.
     * 
     * @param userScenario
     * @param algorithm
     */
    public DistanceCalculator(Scenario userScenario, AbstractAlgorithm algorithm) {
	this.algorithm = algorithm;
	overallDistance = 0;
	Hashtable<String, Criterion> algorithmCriteria = algorithm.idealScenario.getCriteria();
	for (Entry<String, Criterion> userCriterion : userScenario.getCriteria().entrySet()) {
	    String key = userCriterion.getKey();
	    Criterion criterion = userCriterion.getValue();
	    // Exclude inactive criteria from calculation
	    if (criterion.isActive()) {
		overallDistance += eucledianDistance(criterion.getValue(), algorithmCriteria.get(key).getValue());
	    }
	}
    }

    /**
     * A criterion of the original user scenario has changed. The calculation
     * needs to be adjusted.
     * 
     * new overall distance = old overall distance - old criterion distance +
     * new criterion distance
     * 
     * This prevents a recalculation of all criterion distances!
     * 
     * @param key
     * @param oldValue
     * @param newValue
     * 
     */
    public void update(String key, int oldValue, int newValue) {
	int algorithmValue = algorithm.idealScenario.getCriterion(key).getValue();
	if (oldValue == -1) {
	    // the criterion was activated, so its value has to be included in
	    // the calculation
	    overallDistance += eucledianDistance(algorithmValue, newValue);
	} else if (newValue == -1) {
	    // the criterion was deactivated, its value has to be excluded from
	    // calculation
	    overallDistance -= eucledianDistance(algorithmValue, oldValue);
	} else { // just a value change
	    int oldEuclid = eucledianDistance(algorithmValue, oldValue);
	    int newEuclid = eucledianDistance(algorithmValue, newValue);
	    overallDistance = overallDistance - oldEuclid + newEuclid;
	}
    }

    /**
     * This method calculates the eucledian distance of two criteria.
     * 
     * @param a
     * @param b
     * @return sqrt((a-b)^2)
     */
    public static int eucledianDistance(int a, int b) {
	if (a > b) {
	    return a - b;
	} else if (b > a) {
	    return b - a;
	} else {
	    return 0;
	}
    }

    @Override
    public String toString() {
	return "[Distance: " + this.overallDistance + "] " + this.algorithm.getName();
    }

    @Override
    public int compareTo(DistanceCalculator o) {
	return new Integer(this.overallDistance).compareTo(o.overallDistance);
    }
}
