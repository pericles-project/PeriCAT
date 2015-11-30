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
import static main.Configuration.RESTORED_DIRECTORY;
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import model.PayloadSegment;
import model.RestoredFile;
import model.Scenario;

/**
 * This technique works similar to closing credits of a movie, but for images. It is developed by Anna Eggers.
 *
 * This algorithm works on PNG images. It extends the carrier image with
 * additional pixels, which are the pixels from the payload image. The carrier
 * image and payload file can be restored correctly, using this technique.The
 * algorithm is able to append more than one payload file. This algorithm can
 * only append image payload. For other payload files, have a look at the Image
 * Information Embedding Frame algorithm.
 *
 * @author Anna Eggers
 */
public class ImageImageFrameExpanding extends AbstractAlgorithm {

    protected final int METADATA_HEIGHT = 200;
    private final List<File> tmpFiles = new ArrayList<File>();

    /*
     * ******* ENCAPSULATION *************
     */

    /**
     * Attach all payload images to the carrier
     */
    @Override
    public File encapsulate(File carrier, List<File> payloadList) throws IOException {
	File outputFile = appendAllPayload(carrier, payloadList);
	clearTmpFiles();
	return outputFile;
    }

    /**
     * Attach all payload images to the carrier image.
     * 
     * @param carrier
     * @param payloadList
     * @return encapsulated file
     * @throws IOException
     */
    private File appendAllPayload(File carrier, List<File> payloadList) throws IOException {
	File outputFile = getOutputFile(carrier);
	FileUtils.copyFile(carrier, outputFile);
	for (File payload : payloadList) {
	    append(outputFile, payload);
	}
	return outputFile;
    }

    /**
     * Merge of only two images. This is used by the
     * {@link ImageInformationEmbeddingFrame} algorithm.
     * 
     * @param carrier
     * @param payload
     * @return encapsulated images
     * @throws IOException
     */
    public File encapsulate(File carrier, File payload) throws IOException {
	File outputFile = getOutputFile(carrier);
	FileUtils.copyFile(carrier, outputFile);
	append(outputFile, payload);
	return outputFile;
    }

    /**
     * This method appends the payload image to the bottom of the carrier image.
     * Furthermore restoration information is added.
     * 
     * @param carrier
     *            The file where the payload should be added
     * @param payload
     * @throws IOException
     */
    private void append(File carrier, File payload) throws IOException {
	BufferedImage carrierImage = ImageIO.read(carrier);
	BufferedImage payloadImage = ImageIO.read(payload);
	if (carrierImage == null || payloadImage == null) {
	    return;// "This wan't an image! Return."
	}
	BufferedImage outputImage = getOutputImage(carrierImage, payloadImage);
	BufferedImage metadataImage = getMetadataImage(carrier, payload);
	Graphics graphics = outputImage.getGraphics();
	graphics.drawImage(carrierImage, 0, 0, null);
	graphics.drawImage(payloadImage, 0, carrierImage.getHeight(), null);
	graphics.drawImage(metadataImage, 0, carrierImage.getHeight() + payloadImage.getHeight(), null);
	writeImage(carrier, outputImage);
    }

    private BufferedImage getOutputImage(BufferedImage carrierImage, BufferedImage payloadImage) {
	int outputWidth = Math.max(carrierImage.getWidth(), payloadImage.getWidth());
	int outputHeight = carrierImage.getHeight() + payloadImage.getHeight() + METADATA_HEIGHT;
	return new BufferedImage(outputWidth, outputHeight, carrierImage.getType());
    }

    /**
     * Append restoration metadata. Size of carrier and payload files is added.
     * 
     * @param carrier
     * @param payload
     * @return metadata image
     * @throws IOException
     */
    protected BufferedImage getMetadataImage(File carrier, File payload) throws IOException {
	BufferedImage carrierBuffered = ImageIO.read(carrier);
	BufferedImage payloadBuffered = ImageIO.read(payload);
	BufferedImage metadataImage = new BufferedImage(
		Math.max(carrierBuffered.getWidth(), payloadBuffered.getWidth()), METADATA_HEIGHT,
		carrierBuffered.getType());
	colorizeImage(metadataImage, Color.blue.getRGB());
	File metadataImageFile = new File(OUTPUT_DIRECTORY + "metadataImage.png");
	writeImage(metadataImageFile, metadataImage);
	File metadataFile = new File(OUTPUT_DIRECTORY + "tmpMetadataText.txt");
	tmpFiles.add(metadataFile);
	PayloadSegment payloadSegment = new PayloadSegment(carrier, payload, this);
	// add height and width of carrier and payload:
	payloadSegment.addOptionalProperty("carrierWidth", "" + carrierBuffered.getWidth());
	payloadSegment.addOptionalProperty("carrierHeight", "" + carrierBuffered.getHeight());
	payloadSegment.addOptionalProperty("payloadWidth", "" + payloadBuffered.getWidth());
	payloadSegment.addOptionalProperty("payloadHeight", "" + payloadBuffered.getHeight());
	byte[] metadata = payloadSegment.getRestorationMetadataBytes();
	FileUtils.writeByteArrayToFile(metadataFile, metadata);
	OpenStegoRandomLSBSteganography lsbAlgorithm = new OpenStegoRandomLSBSteganography();
	File tmpOutputFile = lsbAlgorithm.encapsulate(metadataImageFile, metadataFile);
	tmpFiles.add(tmpOutputFile);
	metadataImage = ImageIO.read(tmpOutputFile);
	return metadataImage;
    }

    /**
     * Colorises the image. (The metadata image is blue.)
     */
    private void colorizeImage(BufferedImage image, int color) {
	for (int x = 0; x < image.getWidth(); x++) {
	    for (int y = 0; y < METADATA_HEIGHT; y++) {
		image.setRGB(x, y, color);
	    }
	}
    }

    /*
     * ******* RESTORATION *************
     */

    /**
     * Cuts away the payload from the carrier to restore the carrier. Recovers
     * the payload file. This method calls a recurse method, to recover all
     * attached payload files.
     * 
     * The encapsulated data image consists of the carrier image, the payload
     * image, and the restoration metadata image.
     */
    @Override
    public List<RestoredFile> restore(File carrier) throws IOException {
	List<RestoredFile> restoredFiles = new ArrayList<RestoredFile>();
	Properties restorationMetadata = getRestorationMetadata(carrier);
	if (payloadAttached(restorationMetadata)) {
	    restoredFiles.addAll(recursivelyRestoreAll(carrier, restorationMetadata));
	    updateRelatedFilesMetadata(restoredFiles);
	}
	clearTmpFiles();
	return restoredFiles;
    }

    /**
     * Recursively restores all payload files, and adds the last restored
     * carrier to the restored files list.
     * 
     * @param encapsulatedImages
     * @param restorationMetadata
     * @return restored files
     * @throws IOException
     */
    private List<RestoredFile> recursivelyRestoreAll(File encapsulatedImages, Properties restorationMetadata)
	    throws IOException {
	List<RestoredFile> restoredFiles = new ArrayList<RestoredFile>();
	restoredFiles.add(restorePayload(encapsulatedImages, restorationMetadata));
	RestoredFile carrier = restoreCarrier(encapsulatedImages, restorationMetadata);
	// There are more payload files, if there is further valid restoration
	// metadata attached -> one more iteration:
	Properties furtherMetadata = getRestorationMetadata(carrier);
	if (payloadAttached(furtherMetadata)) {
	    restoredFiles.addAll(recursivelyRestoreAll(carrier, furtherMetadata));
	} else { // last carrier! -> break
	    restoredFiles.add(carrier);
	}
	return restoredFiles;
    }

    /**
     * There is another payload file attached, if there is valid restoration
     * metadata.
     * 
     * @param restorationMetadata
     * @return true if another payload file is attached
     */
    private boolean payloadAttached(Properties restorationMetadata) {
	return restorationMetadata != null && restorationMetadata.size() > 0;
    }

    /**
     * This method saves the information that the encapsulated files are related
     * to each other
     * 
     * @param restoredFiles
     */
    private void updateRelatedFilesMetadata(List<RestoredFile> restoredFiles) {
	for (RestoredFile restoredFile1 : restoredFiles) {
	    for (RestoredFile restoredFile2 : restoredFiles) {
		if (restoredFile1 != restoredFile2) {
		    restoredFile1.relatedFiles.add(restoredFile2);
		}
	    }
	}
    }

    /**
     * This method returns the restoration metadata as {@link Properties} class.
     * The restoration metadata contains among others the height and width of
     * the original carrier and payload images, which is necessary to restore
     * them.
     * 
     * @param encapsulatedData
     * @return Should return null, if no valid restoration data is available!
     * @throws IOException
     */
    protected Properties getRestorationMetadata(File encapsulatedData) {
	try {
	    File embeddedRestorationMetadata = restoreRestorationMetadataImage(encapsulatedData);
	    OpenStegoRandomLSBSteganography lsbAlgorithm = new OpenStegoRandomLSBSteganography();
	    List<RestoredFile> restoredFiles = lsbAlgorithm.restore(embeddedRestorationMetadata);
	    tmpFiles.addAll(restoredFiles);
	    RestoredFile restorationMetadata = getRestorationMetadataFile(restoredFiles);
	    if (restorationMetadata == null) {
		return null;
	    }
	    InputStream inputStream = new FileInputStream(restorationMetadata);
	    Properties metadata = new Properties();
	    metadata.load(inputStream);
	    inputStream.close();
	    return metadata;
	} catch (Exception e) {
	}
	return null;
    }

    /**
     * Returns the image in which the restoration metadata is embedded. This
     * image is appended at the end of the encapsulated data image.
     * 
     * The restoration metadata image contains the restoration metadata file,
     * which was embedded with the lsb steganography algorithm and has to be
     * restored afterwards.
     * 
     * @param encapsulatedData
     * @return image with embedded restoration data
     * @throws IOException
     */
    protected File restoreRestorationMetadataImage(File encapsulatedData) {
	try {
	    BufferedImage encapsulatedImage = ImageIO.read(encapsulatedData);
	    if (encapsulatedImage.getHeight() < METADATA_HEIGHT) {
		return null;
	    }
	    BufferedImage restorationMetadataBuffered = encapsulatedImage.getSubimage(0,
		    encapsulatedImage.getHeight() - METADATA_HEIGHT, encapsulatedImage.getWidth(), METADATA_HEIGHT);
	    File embeddedRestorationMetadata = new File("tmpRestorationMetadata.png");
	    tmpFiles.add(embeddedRestorationMetadata);
	    writeImage(embeddedRestorationMetadata, restorationMetadataBuffered);
	    return embeddedRestorationMetadata;
	} catch (IOException e) {
	    return null;
	}
    }

    /**
     * The payload file of the restored files is the restoration metadata file.
     * 
     * @param restoredFiles
     * @return the restoration metadata file. Should return null in case there
     *         is no restoration metadata file.
     */
    protected RestoredFile getRestorationMetadataFile(List<RestoredFile> restoredFiles) {
	if (restoredFiles.size() != 2) {
	    return null;
	}
	RestoredFile restorationMetadata = null;
	if (restoredFiles.get(0).wasPayload) {
	    restorationMetadata = restoredFiles.get(0);
	} else {
	    restorationMetadata = restoredFiles.get(1);
	}
	return restorationMetadata;
    }

    /**
     * Get the carrier image from the encapsulated data.
     * 
     * @param encapsulatedImage
     * @param restorationMetadata
     * @return carrier image
     * @throws IOException
     */
    protected RestoredFile restoreCarrier(File encapsulatedImage, Properties restorationMetadata) throws IOException {
	BufferedImage encapsulatedImageBuffered = ImageIO.read(encapsulatedImage);
	int carrierWidth = Integer.parseInt(restorationMetadata.getProperty("carrierWidth"));
	int carrierHeight = Integer.parseInt(restorationMetadata.getProperty("carrierHeight"));
	BufferedImage carrierBuffered = encapsulatedImageBuffered.getSubimage(0, 0, carrierWidth, carrierHeight);
	String originalCarrierPath = restorationMetadata.getProperty("carrierPath");
	RestoredFile carrier = new RestoredFile(RESTORED_DIRECTORY + Paths.get(originalCarrierPath).getFileName());
	writeImage(carrier, carrierBuffered);
	carrier.wasCarrier = true;
	carrier.wasPayload = false;
	carrier.checksumValid = false;
	carrier.originalFilePath = originalCarrierPath;
	carrier.algorithm = this;
	carrier.restorationNote = "It is not possible to get exactly the same checksum, because of internal lossless PNG compression mechanisms.";
	return carrier;
    }

    /**
     * Get the payload image, in which the payload file is embedded, from the
     * encapsulated data.
     * 
     * @param encapsulatedImage
     * @param restorationMetadata
     * @return payload image
     * @throws IOException
     */
    protected RestoredFile restorePayload(File encapsulatedImage, Properties restorationMetadata) throws IOException {
	BufferedImage encapsulatedImageBuffered = ImageIO.read(encapsulatedImage);
	int payloadWidth = Integer.parseInt(restorationMetadata.getProperty("payloadWidth"));
	int payloadHeight = Integer.parseInt(restorationMetadata.getProperty("payloadHeight"));
	int carrierHeight = Integer.parseInt(restorationMetadata.getProperty("carrierHeight"));
	BufferedImage payloadBuffered = encapsulatedImageBuffered.getSubimage(0, carrierHeight, payloadWidth,
		payloadHeight);
	RestoredFile payload = new RestoredFile(
		RESTORED_DIRECTORY + Paths.get(restorationMetadata.getProperty("payloadPath")).getFileName());
	writeImage(payload, payloadBuffered);
	payload.wasPayload = true;
	payload.wasCarrier = false;
	// The restored file attributes are not important here,
	// as this is not the real payload (which is embedded in this payload
	// holding image)
	return payload;
    }

    /**
     * Write a buffered output image to the output file
     * 
     * @param output
     * @param outputImage
     * @throws IOException
     */
    private void writeImage(File output, BufferedImage outputImage) throws IOException {
	if (!output.exists()) {
	    output.createNewFile();
	}
	ImageIO.write(outputImage, "png", output);
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
     * ********** CONFIGURATION ************
     */

    /**
     * Name of this algorithm to be shown in the GUI list.
     */
    @Override
    public String getName() {
	return "Image-image frame expanding";
    }

    /**
     * Returns the description of this algorithm to be shown in the user
     * interfaces.
     */
    @Override
    public String getDescription() {
	String description = "This algorithm currently works only on PNG images. It extends the carrier image with additional pixels, which are the pixels"
		+ "from the payload image. The carrier image and payload file can be restored correctly, "
		+ "using this technique.The algorithm is able to append more than one payload file.\n"
		+ "This algorithm can only append image payload. For other payload files, have a look at the Image Information Embedding Frame algorithm.";
	if (!new File(LIBRARY_DIRECTORY + "f5.jar").isFile()) {
	    description = "WARNING: This algorithm depends on openstego, but openstego.jar doesn't exist.\n\n"
		    + description;
	}
	return description;
    }

    /**
     * This is a restorable embedding algorithm.
     */
    @Override
    Scenario defineScenario() {
	Scenario scenario = new Scenario("Image image frame ideal scenario");
	scenario.description = "This is the ideal scenario to use the image image frame algorithm.";
	scenario.setCriterionValue(ENCAPSULATION_METHOD, EMBEDDING);
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

    /**
     * All carrier and payload file have to be images.
     */
    @Override
    SuffixFileFilter configureCarrierFileFilter() {
	List<String> supportedFileFormats = new ArrayList<String>();
	supportedFileFormats.add("png");
	return new SuffixFileFilter(supportedFileFormats);
    }

    /**
     * All carrier and payload file have to be images.
     */
    @Override
    SuffixFileFilter configurePayloadFileFilter() {
	return configureCarrierFileFilter();
    }

    /**
     * This algorithm outputs only images.
     */
    @Override
    SuffixFileFilter configureDecapsulationFileFilter() {
	return configureCarrierFileFilter();
    }

    /**
     * All files have to be images, and there has to be a minimum of one carrier
     * and one payload file.
     */
    @Override
    public boolean fulfilledTechnicalCriteria(File carrier, List<File> payloadList) {
	return carrier.isFile() && payloadList.size() > 0 && new File(LIBRARY_DIRECTORY + "openstego.jar").isFile();
    }
}
