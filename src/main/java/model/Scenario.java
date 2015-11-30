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

import static model.Criterion.CARRIER_PROCESSABILITY;
import static model.Criterion.CARRIER_RESTORABILITY;
import static model.Criterion.COMPRESSION;
import static model.Criterion.DETECTABILITY;
import static model.Criterion.ENCAPSULATION_METHOD;
import static model.Criterion.ENCRYPTION;
import static model.Criterion.PAYLOAD_ACCESSIBILITY;
import static model.Criterion.PAYLOAD_RESTORABILITY;
import static model.Criterion.STANDARDS;
import static model.Criterion.VELOCITY;
import static model.Criterion.VISIBILITY;

import java.io.File;
import java.util.Hashtable;

import algorithm.AbstractAlgorithm;

/**
 * Keeps track of lists of criteria, and their characteristics and weighting, to
 * define a scenario. The scenario can be defined by a user, or it can be the
 * ideal scenario for the use of a particular {@link AbstractAlgorithm}.
 */
public class Scenario implements Cloneable {
    /**
     * An unique identifier of the scenario which is valid during loading time.
     */
    public final String ID;
    /** Name, or role, of the scenario */
    public String name = "newScenario";
    /** Description of this scenario to be shown in GUI */
    public String description = "Add a description";
    /** Scenario File */
    public File file;

    /**
     * All criteria are saved in a hashtable with their ID as key. This ID is
     * the String which is used to save the criterion.
     */
    private final Hashtable<String, Criterion> criteriaTable = new Hashtable<String, Criterion>();

    /**
     * Constructor which initialises a new scenario with all criteria.
     * 
     * @param name
     *            name of the scenario
     */
    public Scenario(String name) {
	this.ID = getUniqueID();
	this.name = name;
	Criterion criterion1 = new Criterion(ENCAPSULATION_METHOD, "Encapsulation Method", this, "packaging",
		"embedding");
	criterion1.description = "The techniques of information encapsulate can be classified "
		+ "in packaging techniques and embedding techniques. "
		+ "Packaging means to encapsulate the information by putting them all together"
		+ " in a data container. Here all files are handled as payload. "
		+ "In contrast embedding means to encapsulate information by putting payload files "
		+ "into, or attaching them to, a carrier file.";

	Criterion criterion2 = new Criterion(VISIBILITY, "Visibility", this, "visible", "invisible");
	criterion2.description = "Visibility of an embedded payload means, that a user can see the "
		+ "payload directly, if looking at the carrier. This doesn't mean that the information "
		+ "of the payload can be accessed directly, but rather that the existance of the payload "
		+ "is obvious and not hidden.";

	Criterion criterion3 = new Criterion(DETECTABILITY, "Detectability", this, "easily detectable",
		"hardly detectable");
	criterion3.description = "Detectability means, in contrast to visibility, that the payload can be "
		+ "easily detected by humans or detection tools, but maybe not seen by supervisial observation. "
		+ "Visible payload is always detectable. Some steganography techniques can hide the "
		+ "information in a way that even computer detection is hard. "
		+ "Set this option to hard/not detectable, if the payload is secret. ";

	Criterion criterion4 = new Criterion(CARRIER_RESTORABILITY, "Carrier Restorability", this);
	criterion4.description = "After decapsulation the carrier is restored in a way that the checksums of the "
		+ "original carrier and the restored carrier are equal. "
		+ "An exception are PNG files with their loss less compression. Here it is often not possible to "
		+ "get an equal checksum, but the significant properties are kept and therewith the files are "
		+ "considered as correctly restored. ";

	Criterion criterion5 = new Criterion(PAYLOAD_RESTORABILITY, "Payload Restorability", this);
	criterion5.description = "After decapsulation the payload is restored in a way that the checksums of the "
		+ "original payload and the restored payload are equal. ";

	Criterion criterion6 = new Criterion(CARRIER_PROCESSABILITY, "Carrier Processability", this);
	criterion6.description = "The carrier file(s) can be processed normally by standard "
		+ "applications after the encapsulation process. Therefore the process doesn't "
		+ "influence the carriers structure in a way that its behaviour is affected. "
		+ "This feature is typical for most embedding techniques. "
		+ "Set the importance to high, if the carrier file(s) will be processes after the "
		+ "encapsulation process. ";

	Criterion criterion7 = new Criterion(PAYLOAD_ACCESSIBILITY, "Payload Accessibility", this);
	criterion7.description = "The payload is directly accessible, if it needs no further calculations to "
		+ "get it. It can be that it is visible that any information is embedded in a carrier, but not "
		+ "the information itself. Ih that case it needs a decapsulation process, before the user can"
		+ " access the information.";

	Criterion criterion8 = new Criterion(ENCRYPTION, "Encryption", this);
	criterion8.description = "Encryption means that the payload will only be accessible with a previously"
		+ " defined password. This increases security, but also the risk of data loss in case the "
		+ "passwort gets lost.";

	Criterion criterion9 = new Criterion(COMPRESSION, "Compression", this);
	criterion9.description = "Compression reduces the necessary disk space. Raise the importance "
		+ "of this criteria, if the scenario is disk space critical. ";

	Criterion criterion10 = new Criterion(VELOCITY, "Encapsulation Velocity", this);
	criterion10.description = "This adresses how fast an algorithm will execute the encapsulation "
		+ "procedure and is related to the mathematical term of algorithmic time complexity. "
		+ "Set the importance to high, if the scenario requires a fast calculation. ";

	Criterion criterion11 = new Criterion(STANDARDS, "Use of Standards", this);
	criterion11.description = "This feature describes if an encapsulation technique is standardised, "
		+ "or at least widely spread. Spreading lowers the risk that the information on how to "
		+ "decapsulate the information will get lost."
		+ "\nThis feature is especially important for scenarios in which the moment of decapsulation "
		+ "lies far in the future and/or " + "the person who will decapsulate the information is unknown. ";

	addCriterion(criterion1);
	addCriterion(criterion2);
	addCriterion(criterion3);
	addCriterion(criterion4);
	addCriterion(criterion5);
	addCriterion(criterion6);
	addCriterion(criterion7);
	addCriterion(criterion8);
	addCriterion(criterion9);
	addCriterion(criterion10);
	addCriterion(criterion11);
    }

    private static int uniqueCounter = -1;

    /**
     * @return a unique ID for every loaded user scenario.
     */
    private static String getUniqueID() {
	uniqueCounter++;
	return "" + uniqueCounter;
    }

    /**
     * Copy constructor. Beware: The unique ID is also copied! This is because
     * the copied scenario is used to save a state of the original scenario and
     * therewith considered as the same scenario.
     * 
     * @param scenario
     *            to be copied
     */
    public Scenario(Scenario scenario) {
	this.ID = scenario.ID;
	this.name = scenario.name;
	for (Criterion criterion : scenario.getCriteria().values()) {
	    this.addCriterion(new Criterion(criterion));
	}
    }

    private void addCriterion(Criterion criterion) {
	criteriaTable.put(criterion.ID, criterion);
    }

    public Hashtable<String, Criterion> getCriteria() {
	return this.criteriaTable;
    }

    /**
     * ToString will return the name of the scenario;
     */
    @Override
    public String toString() {
	return this.name;
    }

    public void setCriterionValue(String key, int value) {
	Criterion criterion = criteriaTable.get(key);
	if (value == -1) {
	    criterion.setActive(false);
	    criterion.setValue(50);
	} else {
	    criterion.setValue(value);
	}
    }

    /**
     * @param ID
     *            of the {@link Criterion}
     * @return the criterion with the defined ID
     */
    public Criterion getCriterion(String ID) {
	return criteriaTable.get(ID);
    }

    /**
     * Add a {@link Criterion} to this Scenario.
     * 
     * @param key
     * @param criterion
     */
    public void putCriterion(String key, Criterion criterion) {
	criteriaTable.put(key, criterion);
    }

    /**
     * The activity of a criterion has changed. Update it.
     * 
     * @param criterionID
     * @param selected
     */
    public void criterionChange(String criterionID, boolean selected) {
	Criterion criterion = getCriterion(criterionID);
	criterion.setActive(selected);
    }
}
