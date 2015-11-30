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
package algorithm;

import static main.Configuration.OUTPUT_DIRECTORY;
import static main.Configuration.RESTORED_DIRECTORY;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

/**
 * Includes code which has to be executed in every algorithm test.
 */
public abstract class AbstractAlgorithmTest {

    @Before
    public void setUp() {
	File testDir1 = new File(RESTORED_DIRECTORY);
	File testDir2 = new File(OUTPUT_DIRECTORY);
	if (!testDir1.exists()) {
	    try {
		FileUtils.forceMkdir(testDir1);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	if (!testDir2.exists()) {
	    try {
		FileUtils.forceMkdir(testDir2);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}

    }

    /**
     * Delete the test directory after each algorithm test. This prevents that
     * the resulting test files influence other algorithm tests.
     */
    @After
    public void deleteTestDirectory() {
	File testDir1 = new File(RESTORED_DIRECTORY);
	File testDir2 = new File(OUTPUT_DIRECTORY);
	try {
	    FileUtils.deleteDirectory(testDir1);
	    FileUtils.deleteDirectory(testDir2);
	} catch (IOException e) {
	    System.err.println("Failt do delete test directories!");
	    e.printStackTrace();
	}
    }
}
