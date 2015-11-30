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

import static main.Configuration.LIBRARY_DIRECTORY;
import static main.Configuration.OUTPUT_DIRECTORY;
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import model.RestoredFile;
import model.Scenario;

/**
 * This technique works similar to closing credits of a movie, but for images. It is developed by Anna Eggers.
 *
 * This algorithm works on images. It extends the carrier image with additional
 * pixels and embeds the payload file into these additional pixels. Different
 * information embedding algorithms can be used in combination with this
 * algorithm for the embedding.
 * 
 * The carrier image and payload file can be restored correctly, using this
 * technique.The algorithm is able to append more than one payload file. The
 * difference to the Image-Image Frame Expanding algorithm is, that this
 * algorithm can embed all file types that are embeddable by the used embedding
 * algorithm, whereas the Image-Image algorithm expands the original carrier
 * image with the original payload image pixels.
 *
 * @author Anna Eggers
 */
public class ImageInformationEmbeddingFrame extends AbstractAlgorithm {
    private final List<File> tmpFiles = new ArrayList<File>();

    /*
     * ******* ENCAPSULATION *************
     */

    /**
     * All payload files will be added to the carrier
     */
    @Override
    public File encapsulate(File carrier, List<File> payloadList) throws IOException {
	File outputFile = appendAllPayload(carrier, payloadList);
	clearTmpFiles();
	return outputFile;
    }

    /**
     * Will create an image for each payload file, which has the same width as
     * the carrier file, in which the payload file will be embedded using the
     * LSB steganography algorithm.
     * 
     * Appends the images at the bottom of the carrier image.
     * 
     * @param carrier
	 * @param payloadList
     * @throws IOException
     */
    private File appendAllPayload(File carrier, List<File> payloadList) throws IOException {
	List<File> embeddedPayloadFiles = new ArrayList<File>();
	for (File payload : payloadList) {
	    BufferedImage payloadImage = createPayloadImage(carrier, payload);
	    embeddedPayloadFiles.add(embedPayload(payloadImage, payload));
	}
	ImageImageFrameExpanding imageAlgorithm = new ImageImageFrameExpanding();
	tmpFiles.addAll(embeddedPayloadFiles);
	return imageAlgorithm.encapsulate(carrier, embeddedPayloadFiles);
    }

    /**
     * Create empty image with enough capacity to embed the payload
     * 
     * @param carrier
     * @param payload
     * @return image for payload
     * @throws IOException
     */
    private BufferedImage createPayloadImage(File carrier, File payload) throws IOException {
	BufferedImage carrierImage = ImageIO.read(carrier);
	byte[] payloadBytes = FileUtils.readFileToByteArray(payload);
	int neededPayloadHeight = getNeededHeight(carrierImage.getWidth(), payloadBytes.length);
	return new BufferedImage(carrierImage.getWidth(), neededPayloadHeight, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Returns the pixel height needed for an image to have enough capacity to
     * embed the payload information. The image will have the same with as the
     * carrier.
     * 
     * !! This is still a guessed value !!
     * 
     * TODO: Any help to determine the "real" needed value is appreciated.
     * 
     * @param width
     *            of the carrier
     * @param length
     *            Number of payload bytes
     * @return neededImageHeight needed height for the image to have enough
     *         capacity to embed the payload with the OpenStego
     *         algorithm.
     */
    private int getNeededHeight(int width, int length) {
	return (length * 8 / width) + 1;
    }

    /**
     * Embeds the payload information into the newly created image.
     *
     * @param payloadImage
     * @param payload
     * @return image file with mebedded payload
     * @throws IOException
     */
    private File embedPayload(BufferedImage payloadImage, File payload) throws IOException {
	OpenStegoRandomLSBSteganography lsbAlgorithm = new OpenStegoRandomLSBSteganography();
	File tmpPayloadImage = new File(payload.getName() + "_payloadImage.png");
	ImageIO.write(payloadImage, "png", tmpPayloadImage);
	File outputFile = lsbAlgorithm.encapsulate(tmpPayloadImage, payload);
	tmpPayloadImage.delete();
	return outputFile;
    }

    /*
     * ******* RESTORATION *************
     */

    /**
     * Use the restore method of the image-image algorithm. Then use restore of
     * LSB to recover the payload files.
     */
    @Override
    public List<RestoredFile> restore(File encapsulatedData) throws IOException {
	List<RestoredFile> restoredImages = divideImages(encapsulatedData);
	List<RestoredFile> restoredFiles = getPayloadFiles(restoredImages);
	restoredFiles.add(getCarrier(restoredImages));
	updateMetadata(restoredFiles);
	clearTmpFiles();
	return restoredFiles;
    }

    private List<RestoredFile> divideImages(File encapsulatedData) throws IOException {
	ImageImageFrameExpanding imageAlgorithm = new ImageImageFrameExpanding();
	return imageAlgorithm.restore(encapsulatedData);
    }

    private RestoredFile getCarrier(List<RestoredFile> restoredImages) {
	for (RestoredFile image : restoredImages) {
	    if (image.wasCarrier) {
		return image;
	    }
	}
	return null;
    }

    private List<RestoredFile> getPayloadFiles(List<RestoredFile> restoredImages) throws IOException {
	List<RestoredFile> restoredPayloadFiles = new ArrayList<RestoredFile>();
	for (RestoredFile file : restoredImages) {
	    if (file.wasPayload) {
		// The payload image is moved to the output directory, and
		// deleted after the restoration of the payload. Otherwise the
		// LSB algorithm would result in an error, as it will try to
		// recover a carrier with the same name and locate it also in
		// the restored directory.
		RestoredFile movedFile = file.copy(OUTPUT_DIRECTORY + "restored_" + file.getName());
		tmpFiles.add(movedFile);
		file.delete();
		restoredPayloadFiles.add(recover(movedFile));
	    }
	}
	return restoredPayloadFiles;
    }

    /**
     * Uses the open stego LSB algorithm to get the original payload files out
     * of the images, in which they are embedded.
     * 
     * @param payloadImage
     * @return payload
     * @throws IOException
     */
    private RestoredFile recover(RestoredFile payloadImage) throws IOException {
	OpenStegoRandomLSBSteganography algorithm = new OpenStegoRandomLSBSteganography();
	List<RestoredFile> restoredFiles = algorithm.restore(payloadImage);
	RestoredFile payload = null;
	for (RestoredFile file : restoredFiles) {
	    if (file.wasPayload) {
		payload = file;
	    } else {
		tmpFiles.add(file);
	    }
	}
	return payload;
    }

    /**
     * This method saves the information that the encapsulated files are related
     * to each other
     * 
     * @param restoredFiles
     */
    private void updateMetadata(List<RestoredFile> restoredFiles) {
	for (RestoredFile restoredFile1 : restoredFiles) {
	    restoredFile1.relatedFiles.clear();
	    restoredFile1.algorithm = this;
	    for (RestoredFile restoredFile2 : restoredFiles) {
		if (restoredFile1 != restoredFile2) {
		    restoredFile1.relatedFiles.add(restoredFile2);
		}
	    }
	}
    }

    /**
     * Deletes all temporary files.
     */
    private void clearTmpFiles() {
	for (File file : tmpFiles) {
	    file.delete();
	}
	tmpFiles.clear();
    }

    /*
     * ********** CONFIGURATION ***************
     */

    /**
     * Name of this algorithm shown in the GUI list.
     */
    @Override
    public String getName() {
	return "Image information embedding frame";
    }

    /**
     * Description that is shown in the GUI.
     */
    @Override
    public String getDescription() {
	String description = "This algorithm works on PNG images. It extends the carrier image with additional pixels and "
		+ "embeds the payload file into these additional pixels."
		+ "\nThe carrier image and payload file can be restored correctly, "
		+ "using this technique.The algorithm is able to append more than one payload file.\n"
		+ "The difference to the Image-Image Frame Expanding algorithm is, that this algorithm can embed all file types "
		+ "that are embeddable by the used embedding algorithm, whereas the Image-Image algorithm expands the original carrier "
		+ "image with the original payload image pixels.";
	if (!new File(LIBRARY_DIRECTORY + "f5.jar").isFile()) {
	    description = "WARNING: This algorithm depends on openstego, but openstego.jar doesn't exist.\n\n"
		    + description;
	}
	return description;
    }

    /**
     * PNG images are accepted.
     */
    @Override
    SuffixFileFilter configureCarrierFileFilter() {
	List<String> supportedFileFormats = new ArrayList<String>();
	supportedFileFormats.add("png");
	return new SuffixFileFilter(supportedFileFormats);
    }

    /**
     * The payload file can be of any type.
     */
    @Override
    SuffixFileFilter configurePayloadFileFilter() {
	return new AcceptAllFilter();
    }

    /**
     * PNG images are accepted.
     */
    @Override
    SuffixFileFilter configureDecapsulationFileFilter() {
	return configureCarrierFileFilter();
    }

    /**
     * The carrier has to be an image, and there has to be a minimum of one
     * carrier and one payload file.
     */
    @Override
    public boolean fulfilledTechnicalCriteria(File carrier, List<File> payloadList) {
	return carrier.isFile() && payloadList.size() > 0 && new File(LIBRARY_DIRECTORY + "openstego.jar").isFile();
    }

    /**
     * This is a restorable embedding algorithm.
     */
    @Override
    Scenario defineScenario() {
	Scenario scenario = new Scenario("Image information frame ideal scenario");
	scenario.description = "This is the ideal scenario to use the image information frame algorithm.";
	scenario.setCriterionValue(ENCAPSULATION_METHOD, EMBEDDING);
	/* just visible for images, no guaranteed invisibility */
	scenario.setCriterionValue(VISIBILITY, VISIBLE);
	scenario.setCriterionValue(DETECTABILITY, DETECTABLE);
	scenario.setCriterionValue(CARRIER_RESTORABILITY, YES);
	scenario.setCriterionValue(PAYLOAD_RESTORABILITY, YES);
	scenario.setCriterionValue(CARRIER_PROCESSABILITY, YES);
	scenario.setCriterionValue(PAYLOAD_ACCESSIBILITY, NO);
	scenario.setCriterionValue(ENCRYPTION, NO);
	scenario.setCriterionValue(COMPRESSION, NO);
	scenario.setCriterionValue(VELOCITY, NO);
	scenario.setCriterionValue(STANDARDS, NO);
	return scenario;
    }
}
