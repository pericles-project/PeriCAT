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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import algorithm.AbstractAlgorithm;

/**
 * Simple data structure without logic to store restored files with some
 * additional information. These additional information are optional and will be
 * added by the {@link AbstractAlgorithm#restore} method. They will be displayed
 * to the user.
 */
public class RestoredFile extends File {
    private static final long serialVersionUID = 1L;

    public RestoredFile(String pathname) {
	super(pathname);
    }

    /** Was the file a carrier or a payload? */
    public boolean wasPayload = false;
    public boolean wasCarrier = false;

    /** Was the file restored correctly? */
    public boolean checksumValid = false;

    /** Optional explanation why a checksum isn't valid */
    public String restorationNote = "";

    /** The path to the original file */
    public String originalFilePath = "";

    /** The used algorithm for encapsulation/decapsulation: */
    public AbstractAlgorithm algorithm = null;

    /** The files that were encapsulated together with this file: */
    public List<RestoredFile> relatedFiles = new ArrayList<RestoredFile>();

    /**
     * Pass the checksum string from the restoration metadata to validate if
     * this file was restored correctly
     * 
     * @param checksum
     * @return validity of the restoration
     * @throws IOException
     */
    public boolean validateChecksum(String checksum) throws IOException {
	checksumValid = checksum.equals("" + FileUtils.checksumCRC32(this));
	return checksumValid;
    }

    /**
     * Moves this file to another location
     * 
     * @param destination
     * @return A copy of this file at another location.
     * @throws IOException
     */
    public RestoredFile copy(String destination) throws IOException {
	RestoredFile movedFile = new RestoredFile(destination);
	FileUtils.moveFile(this, movedFile);
	movedFile.wasPayload = this.wasPayload;
	movedFile.wasCarrier = this.wasCarrier;
	movedFile.checksumValid = this.checksumValid;
	movedFile.restorationNote = this.restorationNote;
	movedFile.originalFilePath = this.originalFilePath;
	movedFile.algorithm = this.algorithm;
	movedFile.relatedFiles = this.relatedFiles;
	return movedFile;
    }

    /**
     * ToString will print the files path
     */
    @Override
    public String toString() {
	return "" + this.toPath();
    }
}
