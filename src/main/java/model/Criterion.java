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
package model;

import view.ScenarioTab;

/**
 * This is the data structure for a decision criterion, as displayed in the
 * {@link ScenarioTab}.
 */
public class Criterion implements Cloneable {
    /** Collection of standard id strings */
    public static final String ENCAPSULATION_METHOD = "encapsulationMethod";
    public static final String VISIBILITY = "visibility";
    public static final String DETECTABILITY = "detectability";
    public static final String CARRIER_RESTORABILITY = "carrierRestorability";
    public static final String PAYLOAD_RESTORABILITY = "payloadRestorability";
    public static final String CARRIER_PROCESSABILITY = "carrierProcessability";
    public static final String PAYLOAD_ACCESSIBILITY = "accessibility";
    public static final String ENCRYPTION = "encryption";
    public static final String COMPRESSION = "compression";
    public static final String VELOCITY = "velocity";
    public static final String STANDARDS = "standards";

    /**
     * An active criterion is considered for the distance calculation. A user
     * can exclude a criterion. This will deactivate it.
     */
    private boolean active = true;
    /** The scenario to which the criterion belongs */
    public Scenario scenario;
    /** The identifier is used for saving purposes. It mustn't include spaces */
    public String ID;
    /** This is the name which will be shown at the GUI */
    public String name;
    /** Description of a criterion. This is for example used at tool tips. */
    public String description = "";
    /** This will be shown at the left of the GUI slider */
    public String leftLabel;
    /** This will be shown at the right of the GUI slider */
    public String rightLabel;
    /**
     * The current value of this criterion as used for the distance calculation.
     * This can be different depending on the criterion type, e.g. a slider
     * position, or a weight. The value should be between 0 and 100.
     */
    private int value = 50;

    /**
     * This constructor has a single feature. Here the value indicates how
     * important this feature is for the scenario.
     *
     * Creates a new criterion with initial value 50.
     *
     * @param ID
     * @param name
     * @param scenario
     */
    public Criterion(String ID, String name, Scenario scenario) {
	this(ID, name, scenario, 50);
    }

    /**
     * This constructor has a single feature. Here the value indicates how
     * important this feature is for the scenario.
     * 
     * Loads a criterion with the passed value.
     * 
     * @param ID
     * @param name
     * @param scenario
     * @param value
     */
    public Criterion(String ID, String name, Scenario scenario, int value) {
	this(ID, name, scenario, "important", "not important", value);
    }

    /**
     * This constructor will create a criterion with two opposing features. The
     * value indicates which of these two features is the most appropriate one
     * for a scenario.
     * 
     * Creates a new criterion with initial value 50.
     * 
     * @param ID
     * @param name
     * @param scenario
     * @param leftLabel
     * @param rightLabel
     */
    public Criterion(String ID, String name, Scenario scenario, String leftLabel, String rightLabel) {
	this(ID, name, scenario, leftLabel, rightLabel, 50);
    }

    /**
     * This constructor will create a criterion with two opposing features. The
     * value indicates which of these two features is the most appropriate one
     * for a scenario.
     * 
     * Loads a criterion with the passed value.
     * 
     * @param ID
     * @param name
     * @param scenario
     * @param leftLabel
     * @param rightLabel
     * @param value
     */
    public Criterion(String ID, String name, Scenario scenario, String leftLabel, String rightLabel, int value) {
	this.ID = ID;
	this.name = name;
	this.scenario = scenario;
	this.leftLabel = leftLabel;
	this.rightLabel = rightLabel;
	this.value = value;
    }

    /**
     * Copy constructor
     * 
     * @param criterion
     */
    public Criterion(Criterion criterion) {
	this.active = criterion.active;
	this.description = criterion.description;
	this.ID = criterion.ID;
	this.name = criterion.name;
	this.scenario = criterion.scenario;
	this.leftLabel = criterion.leftLabel;
	this.rightLabel = criterion.rightLabel;
	this.value = criterion.value;
    }

    /** @return current value of the criterion */
    public int getValue() {
	return value;
    }

    /**
     * Change the value of the criterion.
     * 
     * !Special case: if the criterion value is -1, then the criterion is
     * excluded from calculation!
     * 
     * @param value
     */
    public void setValue(int value) {
	if (value > -2 && value <= 100) {
	    this.value = value;
	}
    }

    public boolean isActive() {
	return active;
    }

    protected void setActive(boolean activate) {
	this.active = activate;
    }

    @Override
    public String toString() {
	return this.ID;
    }
}