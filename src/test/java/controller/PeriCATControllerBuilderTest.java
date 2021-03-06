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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import controller.PeriCATControllerBuilder.Mode;
import main.Configuration;
import main.TestDataProvider;

public class PeriCATControllerBuilderTest {

    PeriCATControllerBuilder builder;
    PeriCATController controller;
    File bmpFile;
    File jpgFile;
    File txtFile;

    @Before
    public void setUp() {
	builder = new PeriCATControllerBuilder();
	bmpFile = TestDataProvider.BMP_FILE;
	jpgFile = TestDataProvider.JPG_FILE;
	txtFile = TestDataProvider.TXT_FILE;
    }

    @Test
    public void encapsulationModeTest() {
	assertEquals(Mode.GUI, builder.mode);
	builder.verbose();
	assertEquals(Mode.GUI, builder.mode);
	builder.useOutputDirectory(Configuration.TEST_DIRECTORY);
	assertEquals(Mode.GUI, builder.mode);
	builder.useCarrier(bmpFile);
	assertEquals(Mode.ENCAPSULATE, builder.mode);
	builder.useDecapsulationFile(jpgFile);
	// stay in encaps mode
	assertEquals(Mode.ENCAPSULATE, builder.mode);
    }

    @Test
    public void encapsulationModeTest2() {
	assertEquals(Mode.GUI, builder.mode);
	List<File> payloadFiles = new ArrayList<File>();
	payloadFiles.add(bmpFile);
	payloadFiles.add(jpgFile);
	builder.usePayload(payloadFiles);
	assertEquals(Mode.ENCAPSULATE, builder.mode);
    }

    @Test
    public void decapsulationModeTest() {
	assertEquals(Mode.GUI, builder.mode);
	builder.useDecapsulationFile(bmpFile);
	assertEquals(Mode.DECAPSULATE, builder.mode);
    }
}
