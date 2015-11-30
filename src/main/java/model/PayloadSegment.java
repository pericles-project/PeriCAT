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

import static main.Configuration.TOOL_DESCRIPTION;
import static model.PayloadSequences.END_HEADER_SEQ;
import static model.PayloadSequences.END_SEQ;
import static model.PayloadSequences.START_SEQ;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import algorithm.AbstractAlgorithm;

/**
 * A payload segment contains the restoration metadata and the payload.
 */
public class PayloadSegment {
    private Properties properties = new Properties();
    private byte[] payload;

    /**
     * A payload segment consists of: START_SEQ, restoration metadata,
     * END_HEADER_SEQ, payload bytes, END_SEQ.
     * 
     * The restoration metadata is saved in a {@link Properties} class, and
     * keeps the name of carrier and payload, as well as their checksums and the
     * name of the algorithm used to encapsulate.
     * 
     * @param carrier
     * @param payload
     * @param algorithm
     */
    public PayloadSegment(File carrier, File payload, AbstractAlgorithm algorithm) {
	String payloadChecksum = "";
	String carrierChecksum = "";
	try {
	    this.payload = FileUtils.readFileToByteArray(payload);
	    payloadChecksum += FileUtils.checksumCRC32(payload);
	    carrierChecksum += FileUtils.checksumCRC32(carrier);
	} catch (IOException e) {
	}
	properties.put("carrierName", carrier.getName());
	properties.put("carrierChecksum", carrierChecksum);
	properties.put("payloadName", payload.getName());
	properties.put("payloadChecksum", payloadChecksum);
	properties.put("algorithm", algorithm.getClass().getName());
	properties.put("carrierPath", "" + carrier.getAbsolutePath());
	properties.put("payloadPath", "" + payload.getAbsolutePath());
    }

    /**
     * This constructor is for spare algorithms with low capacity.
     * 
     * @param payload
     */
    public PayloadSegment(File payload) {
	String payloadChecksum = "";
	try {
	    this.payload = FileUtils.readFileToByteArray(payload);
	    payloadChecksum += FileUtils.checksumCRC32(payload);
	} catch (IOException e) {
	}
	properties.put("payloadName", payload.getName());
	properties.put("payloadChecksum", payloadChecksum);
    }

    /**
     * Get the restoration metadata as Properties class.
     * 
     * @return restoration metadata
     */
    public Properties getRestorationMetadata() {
	return this.properties;
    }

    /**
     * Get the restoration metadata as byte array.
     * 
     * @return restoration metadata
     */
    public byte[] getRestorationMetadataBytes() {
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	try {
	    properties.store(outputStream, "restoration metadata");
	} catch (IOException e) {
	}
	return outputStream.toByteArray();
    }

    private PayloadSegment(byte[] payload, Properties properties) {
	this.payload = payload;
	this.properties = properties;
    }

    public void addOptionalProperty(String key, String value) {
	this.properties.put(key, value);
    }

    /**
     * The payload segment consists of the restoration metadata and the payload
     * information.
     * 
     * @return payload segment as byte array
     */
    public byte[] getPayloadSegmentBytes() {
	try {
	    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	    properties.store(byteOut, TOOL_DESCRIPTION);
	    byte[] restorationMetadata = byteOut.toByteArray();
	    ByteArrayOutputStream byteSection = new ByteArrayOutputStream();
	    byteSection.write(START_SEQ);
	    byteSection.write(restorationMetadata);
	    byteSection.write(END_HEADER_SEQ);
	    byteSection.write(payload);
	    byteSection.write(END_SEQ);
	    byte[] sectionBytes = byteSection.toByteArray();
	    byteOut.close();
	    byteSection.close();
	    return sectionBytes;
	} catch (IOException e) {
	    return null;
	}
    }

    public byte[] getPayloadBytes() {
	return payload;
    }

    /**
     * This method parses the bytes of an encapsulated file from least to first
     * for the least payload section of the file. Beware that the parentCarrier
     * can contain additional payload sections!
     * 
     * This method will return null, if no payload sections are left and
     * otherwise a {@link PayloadSegment} class which contains the restored
     * payload and the restoration metadata.
     * 
     * @param encapsulatedBytes
     * 
     * @return PayloadSegment class, or null if no payload section available
     */
    public static PayloadSegment getPayloadSegment(byte[] encapsulatedBytes) {
	try {
	    byte[] payloadSection = null;
	    int endIndex = -1;
	    for (int index = encapsulatedBytes.length - 1; index >= 0; index--) {
		if (PayloadSequences.isEndSequence(encapsulatedBytes, index)) {
		    endIndex = index; // first byte of end sequence
		} else if (PayloadSequences.isStartSequence(encapsulatedBytes, index)) {
		    if (endIndex == -1) {
			return null;
		    }
		    // The payload section bytes are after the start sequence
		    // and before the end sequence:
		    payloadSection = Arrays.copyOfRange(encapsulatedBytes, index + START_SEQ.length, endIndex);
		    break;
		}
	    }
	    if (payloadSection == null) {
		// no payload section in file.
		return null;
	    }
	    // Now get the restoration metadata bytes out of the payload section
	    // bytes:
	    byte[] restorationMetadataBytes = null;
	    byte[] payloadBytes = null;
	    for (int index = 0; index < payloadSection.length - 1; index++) {
		if (PayloadSequences.isEndHeader(payloadSection, index)) {
		    restorationMetadataBytes = Arrays.copyOfRange(payloadSection, 0, index - 1);
		    payloadBytes = Arrays.copyOfRange(payloadSection, index + END_HEADER_SEQ.length,
			    payloadSection.length);
		}
	    }
	    Properties properties = new Properties();
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(restorationMetadataBytes);
	    properties.load(inputStream);
	    return new PayloadSegment(payloadBytes, properties);
	} catch (IOException e) {
	}
	return null;
    }

    /**
     * Copy the encapsulation file to the restoration directory first, to be
     * sure that you won't touch the file in the encapsulation output directory.
     * 
     * @param encapsulatedData
     * @return altered carrier file...
     */
    public static byte[] removeLeastPayloadSegment(byte[] encapsulatedData) {
	try {
	    int endIndex = -1;
	    for (int index = encapsulatedData.length - 1; index >= 0; index--) {
		if (PayloadSequences.isEndSequence(encapsulatedData, index)) {
		    endIndex = index;
		} else if (PayloadSequences.isStartSequence(encapsulatedData, index)) {
		    if (endIndex == -1) {
			return null;
		    }
		    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		    byteOut.write(Arrays.copyOfRange(encapsulatedData, 0, index));
		    if (endIndex + END_SEQ.length > encapsulatedData.length) {
			byte[] afterPayload = Arrays.copyOfRange(encapsulatedData, endIndex + END_SEQ.length,
				encapsulatedData.length - 1);
			byteOut.write(afterPayload);
		    }
		    byte[] dataWithRemovedPayload = byteOut.toByteArray();
		    byteOut.close();
		    return dataWithRemovedPayload;
		}
	    }
	} catch (IOException e) {
	}
	return null;
    }

    /**
     * Get the obligatory restoration metadata value: original name of the
     * carrier file.
     * 
     * @return carrier name
     */
    public String getCarrierName() {
	return properties.getProperty("carrierName");
    }

    /**
     * Get the obligatory restoration metadata value: original name of the
     * payload file.
     * 
     * @return payload name
     */
    public String getPayloadName() {
	return properties.getProperty("payloadName");
    }

    /**
     * Get the obligatory restoration metadata value: checksum of the original
     * carrier file.
     * 
     * @return carrier checksum
     */
    public String getCarrierChecksum() {
	return properties.getProperty("carrierChecksum");
    }

    /**
     * Get the obligatory restoration metadata value: checksum of the original
     * payload file.
     * 
     * @return payload checksum
     */
    public String getPayloadChecksum() {
	return properties.getProperty("payloadChecksum");
    }

    /**
     * Get the obligatory restoration metadata value: class name of the
     * algorithm used for the encapsulation
     * 
     * @return encapsulation algorithm class name
     */
    public String getAlgorithmClassName() {
	return properties.getProperty("algorithm");
    }

    /**
     * 
     * @return original path of the carrier file
     */
    public String getCarrierPath() {
	return properties.getProperty("carrierPath");
    }

    /**
     * 
     * @return original path of the payload file
     */
    public String getPayloadPath() {
	return properties.getProperty("payloadPath");
    }
}
