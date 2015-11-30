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
package main;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestDataProviderTest {
    @Test
    public void testFilesExistingTest() {
	assertTrue(TestDataProvider.TXT_FILE.isFile());
	assertTrue(TestDataProvider.TXT_FILE_2.isFile());
	assertTrue(TestDataProvider.TXT_FILE_3.isFile());
	assertTrue(TestDataProvider.XML_FILE.isFile());
	assertTrue(TestDataProvider.PDF_FILE.isFile());
	assertTrue(TestDataProvider.PNG_FILE.isFile());
	assertTrue(TestDataProvider.PNG_FILE_2.isFile());
	assertTrue(TestDataProvider.PS_FILE.isFile());
	assertTrue(TestDataProvider.JPG_FILE.isFile());
	assertTrue(TestDataProvider.JPG_FILE_2.isFile());
	assertTrue(TestDataProvider.JPG_FILE_3.isFile());
	assertTrue(TestDataProvider.PS_FILE.isFile());
	assertTrue(TestDataProvider.BMP_FILE.isFile());
    }
}
