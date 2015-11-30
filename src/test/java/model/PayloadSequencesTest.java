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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PayloadSequencesTest {

    @Test
    public void isSequenceStartTest() {
	byte[] seq = "STARTSEQ".getBytes();
	byte[] testdata1 = "STARTSEQ12354".getBytes();
	byte[] testdata2 = "test123STARTSEQ".getBytes();
	byte[] testdata3 = "testadsfasdSTARTSEQkdasdfhu".getBytes();
	byte[] testdata4 = "emptysequence".getBytes();

	int indexFound = -1;
	for (int i = 0; i < testdata1.length; i++) {
	    if (PayloadSequences.isSequenceStart(testdata1, seq, i)) {
		indexFound = i;
	    }
	}
	assertEquals(0, indexFound);
	indexFound = -1;
	for (int i = 0; i < testdata2.length; i++) {
	    if (PayloadSequences.isSequenceStart(testdata2, seq, i)) {
		indexFound = i;
	    }
	}
	assertEquals(7, indexFound);
	indexFound = -1;
	for (int i = 0; i < testdata3.length; i++) {
	    if (PayloadSequences.isSequenceStart(testdata3, seq, i)) {
		indexFound = i;
	    }
	}
	assertEquals(11, indexFound);
	indexFound = -1;
	for (int i = 0; i < testdata4.length; i++) {
	    if (PayloadSequences.isSequenceStart(testdata4, seq, i)) {
		indexFound = i;
	    }
	}
	assertEquals(-1, indexFound);
    }
}
