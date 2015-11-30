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

import static main.Configuration.SCENARIO_DIRECTORY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;

import model.Criterion;
import model.Scenario;

public class ScenarioSaverAndLoader {
    /* private constructor: static class */
    private ScenarioSaverAndLoader() {
    }

    /**
     * Saves a scenario to file. This uses the default scenario directory.
     * 
     * @param scenario
     */
    public static void save(Scenario scenario) {
	File defaultFile = new File(SCENARIO_DIRECTORY + scenario.name);
	if (!defaultFile.exists()) {
	    try {
		defaultFile.createNewFile();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	saveTo(scenario, defaultFile);
    }

    /**
     * Saves a scenario to file. The user can define where to save the scenario.
     * 
     * @param scenario
     * @param file
     */
    public static void saveTo(Scenario scenario, File file) {
	Properties properties = new Properties();
	for (Entry<String, Criterion> criterion : scenario.getCriteria().entrySet()) {
	    if (criterion.getValue().isActive()) {
		properties.put(criterion.getKey(), "" + criterion.getValue().getValue());
	    } else {
		/* The criterion is excluded from calculation */
		properties.put(criterion.getKey(), "-1");
	    }
	}
	properties.put("name", scenario.name);
	properties.put("description", scenario.description);
	try {
	    OutputStream outputStream = new FileOutputStream(file);
	    properties.store(outputStream, "scenario for PeriCAT");
	    outputStream.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Loads all scenarios which are stored in the specified directory.
     * 
     * @return returns a vector that contains all loaded scenarios
     */
    public static Vector<Scenario> loadAll() {
	File scenarioDirectory = new File(SCENARIO_DIRECTORY);
	File[] scenarioFiles = scenarioDirectory.listFiles();
	Vector<Scenario> loadedScenarios = new Vector<Scenario>();
	for (File savedScenario : scenarioFiles) {
	    Scenario loadedScenario = load(savedScenario.getAbsolutePath());
	    if (loadedScenario != null) {
		loadedScenarios.add(loadedScenario);
	    }
	}
	return loadedScenarios;
    }

    /**
     * Loads a scenario from file. This is used by the loadAll() method, and by
     * the import function.
     * 
     * @param path
     * @return scenario
     */
    public static Scenario load(String path) {
	try {
	    InputStream inputStream = new FileInputStream(path);
	    Properties properties = new Properties();
	    properties.load(inputStream);
	    if (properties.getProperty("name") == null) {
		return null;
	    }
	    inputStream.close();
	    Scenario scenario = new Scenario((String) properties.get("name"));
	    scenario.description = (String) properties.get("description");
	    properties.remove("name");
	    properties.remove("description");
	    for (Entry<Object, Object> entry : properties.entrySet()) {
		String key = (String) entry.getKey();
		String value = (String) entry.getValue();
		int criteriaValue;
		try {
		    criteriaValue = Integer.parseInt(value);
		} catch (NumberFormatException e) {
		    return null;
		}
		scenario.setCriterionValue(key, criteriaValue);
	    }
	    return scenario;
	} catch (IOException e) {
	    return null;
	}
    }
}
