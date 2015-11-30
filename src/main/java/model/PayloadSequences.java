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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * Collection of useful static methods and constants to be used by the
 * encapsulation algorithms and the {@link PayloadSegment}.
 */
public class PayloadSequences {
    /* static class, private constructor */
    private PayloadSequences() {
    }

    /*
     * Standard order for encapsulating payload bytes into a carrier file:
     * 
     * [CARRIER BYTES]([START_SEQ][RESTORATION_METADATA][END_HEADER_SEQ][PAYLOAD
     * FILE BYTES][END_SEQ]*These can be repeated for more than one payload
     * file)[MAYBE CARRIER BYTES]
     */

    /**
     * This sequence can be used to indicate where the encapsulated payload in a
     * carrier file starts.
     */
    public static final byte[] START_SEQ = "<!--PERI_CAT".getBytes();
    /**
     * This sequence can be used to indicate where the encapsulated payload in a
     * carrier file ends.
     */
    public static final byte[] END_SEQ = "PERI_CAT_END-->".getBytes();
    /**
     * The end header sequence can be used to put encapsulation metadata, as for
     * example the payload file name, before the payload file bytes. It
     * indicates where these encapsulation metadata ends.
     */
    public static final byte[] END_HEADER_SEQ = "END_HEADER_SEQ".getBytes();

    /**
     * Checks if data[index] is the first byte of START_SEQ
     * 
     * @param data
     *            normally the encapsulated carrier file as byte array
     * @param index
     *            assumed start of START_SEQ
     * @return result of check
     */
    public static boolean isStartSequence(byte[] data, int index) {
	return isSequenceStart(data, START_SEQ, index);
    }

    /**
     * Checks if data[index] is the first byte of END_SEQ
     * 
     * @param data
     *            normally the encapsulated carrier file as byte array
     * @param index
     *            assumed start of END_SEQ
     * @return result of check
     */
    public static boolean isEndSequence(byte[] data, int index) {
	return isSequenceStart(data, END_SEQ, index);
    }

    /**
     * Checks if data[index] is the first byte of END_HEADER_SEQ
     * 
     * @param data
     *            normally the encapsulated carrier file as byte array
     * @param index
     *            assumed start of END_HEADER_SEQ
     * @return result of check
     */
    public static boolean isEndHeader(byte[] data, int index) {
	return isSequenceStart(data, END_HEADER_SEQ, index);
    }

    /**
     * Checks if data[index] is the first byte of a byte sequence
     * 
     * @param data
     *            normally the encapsulated carrier file as byte array
     * @param sequence
     *            that maybe starts
     * @param index
     *            assumed start of a byte sequence
     * @return result of check
     */
    public static boolean isSequenceStart(byte[] data, byte[] sequence, int index) {
	if (data.length < index + sequence.length) {
	    return false; // There can't be a start sequence
	}
	for (int i = 0; i < sequence.length; i++) {
	    if (sequence[i] != data[index + i]) {
		return false;
	    }
	}
	return true;
    }

    /**
     * The trick here is that the index (of a byte array) is on the first byte
     * of sequence. The new index will be on the last byte of the (start / end /
     * etc) sequence, as this method is often called from within a for loop, so
     * the index will be increased by the loop and point to the first byte after
     * the sequence afterwards. If the method not called from within a for loop,
     * the index has to be increased by 1 manually to get the first byte of the
     * following content.
     * 
     * @param index
     *            index of first byte of a byte sequence
     * @param sequence
     *            the byte sequence to be skipped
     * @return index of last byte of a byte sequence
     */
    public static int skipSequence(int index, byte[] sequence) {
	return index + sequence.length - 1;
    }

    /**
     * Encapsulation metadata is stored between the START_SEQ and
     * END_HEADER_SEQ. It is a serialised {@link Properties} file.
     * 
     * @param properties
     * @return byte array representation of preferences
     */
    public static byte[] getEncapsulationMetadataBytes(Properties properties) {
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	try {
	    properties.store(outputStream, null);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return outputStream.toByteArray();
    }

    /**
     * @param data
     * @return Properties class form byte array
     */
    public static Properties getEncapsulationMetadataProperties(byte[] data) {
	Properties properties = new Properties();
	try {
	    properties.load(new ByteArrayInputStream(data));
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return properties;
    }
}
