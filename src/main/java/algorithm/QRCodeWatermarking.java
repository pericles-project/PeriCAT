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
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import model.PayloadSegment;
import model.RestoredFile;
import model.Scenario;
import view.GUIPanel;

/**
 * The ZXing library is used to implement this technique.
 *
 * This is a digital watermarking technique, that uses QR-codes. The algorithm
 * will create QR-codes from the payload files, and either ignore the carriers
 * and save the QR-codes into separate files, or use only the first payload and
 * add it on each carrier image.
 */
public class QRCodeWatermarking extends AbstractAlgorithm {
    private final JRadioButton trueEncapsulate = new JRadioButton("Add the QR-code to carrier image");
    private final JRadioButton falseEncapsulate = new JRadioButton(
	    "Create separate QR-code file for each payload file (this will ignore the carrier!)");
    private final JLabel messageLabel = new JLabel();
    private final JTextField sizeField = new JTextField(20);
    protected final JTextField xPositionField = new JTextField(20);
    protected final JTextField yPositionField = new JTextField(20);
    private final String[] fileFormats = { "png", "jpeg", "jpg", "bmp", "gif" };
    private final JComboBox<String> imageFormatBox = new JComboBox<String>(fileFormats);

    private final String ON_IMAGE = "ON_IMAGE";
    private final String SEPARATE_FILE = "SEPARATE_FILE";
    private final int DEFAULT_SIZE = 200;

    {
	createConfigurationGui();
    }

    /*
     * ****** ENCAPSULATION **********
     */

    /**
     * Starts either the creation of QR-code files from each payload file, or
     * the embedding of a QR-code created from the first payload file on each
     * carrier.
     */
    @Override
    public File encapsulate(File carrier, List<File> payloadList) throws IOException {
	String selectedImageFormat = (String) imageFormatBox.getSelectedItem();
	if (embedInCarrierFile()) {
	    return embedInCarrierFile(carrier, payloadList.get(0), selectedImageFormat);
	} else {
	    return createQrCodeFile(payloadList, selectedImageFormat, SEPARATE_FILE);
	}
    }

    /**
     * This method ignores the carrier file. It will create an own QR-code image
     * for the first payload file.
     * 
     * @param payloadList
     * @return bar code file
     * @throws IOException
     */
    private File createQrCodeFile(List<File> payloadList, String imageFormat, String usedMethod) throws IOException {
	return createBarcodeFile(payloadList.get(0), imageFormat, usedMethod);
    }

    /**
     * Creates a PNG image that contains the QR-code with the information from
     * the payload file. The image has the same name as the payload file.
     * 
     * @param payload
     * @return qr code as png image file
     * @throws IOException
     */
    private File createBarcodeFile(File payload, String imageFormat, String usedMethod) throws IOException {
	// Create restoration metadata only for the payload file to spare space.
	PayloadSegment metadata = new PayloadSegment(payload);
	metadata.addOptionalProperty("usedMethod", usedMethod);
	byte[] payloadSegment = metadata.getPayloadSegmentBytes();
	String barcodeInformation = new String(payloadSegment);
	int size = getQRCodeSize();
	String outputFileName = FilenameUtils.removeExtension(getOutputFileName(payload)) + "." + imageFormat;
	File outputFile = new File(outputFileName);
	Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
	hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
	BitMatrix byteMatrix = encodeWithQRCode(barcodeInformation, hintMap, size);
	if (byteMatrix == null) {
	    return null;
	}
	BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
	image.createGraphics();
	Graphics2D graphics = (Graphics2D) image.getGraphics();
	graphics.setColor(Color.WHITE);
	graphics.fillRect(0, 0, size, size);
	graphics.setColor(Color.BLACK);
	for (int x = 0; x < size; x++) {
	    for (int y = 0; y < size; y++) {
		if (byteMatrix.get(x, y)) {
		    graphics.fillRect(x, y, 1, 1);
		}
	    }
	}
	ImageIO.write(image, imageFormat, outputFile);
	return outputFile;
    }

    /**
     * Creates the BitMatrix for the QR-Code
     * 
     * @param barcodeInformation
     * @param hintMap
     * @param size
     * @return bit matrix
     */
    private BitMatrix encodeWithQRCode(String barcodeInformation,
	    Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap, int size) {
	try {
	    return new QRCodeWriter().encode(barcodeInformation, BarcodeFormat.QR_CODE, size, size, hintMap);
	} catch (Exception e) {
	    displayMessage(e.getMessage());
	}
	return null;
    }

    /**
     * This method will create a QR-code from the information of the payload
     * file, and add them to each carrier image.
     * 
     * @param carrier
     * @param payload
     * @return qr code
     * @throws IOException
     */
    private File embedInCarrierFile(File carrier, File payload, String imageFormat) throws IOException {
	List<File> payloadList = new ArrayList<>();
	payloadList.add(payload);
	File barcode = createQrCodeFile(payloadList, "png", ON_IMAGE);
	return writeQRCodeOnImage(barcode, carrier, imageFormat);
    }

    /**
     * This algorithm writes a QR-code image on a carrier image.
     * 
     * @param qrCodeFile
     * @param carrierFile
     * @param imageFormat
     * @return image file with qr code on top of carrier
     * @throws IOException
     */
    private File writeQRCodeOnImage(File qrCodeFile, File carrierFile, String imageFormat) throws IOException {
	BufferedImage barcode = ImageIO.read(qrCodeFile);
	BufferedImage carrier = ImageIO.read(carrierFile);
	if (barcode.getWidth() > carrier.getWidth() || barcode.getHeight() > carrier.getHeight()) {
	    displayMessage(
		    "The QR-code is to big to add it to the carrier image. Try again with a lower QR-code size!");
	    qrCodeFile.delete();
	    return null;
	}
	Graphics graphics = carrier.getGraphics();
	graphics.drawImage(barcode, getXPosition(), getYPosition(), null);
	String outputFileName = FilenameUtils.removeExtension(getOutputFileName(carrierFile)) + "." + imageFormat;
	File outputFile = new File(outputFileName);
	ImageIO.write(carrier, imageFormat, outputFile);
	qrCodeFile.delete();
	return outputFile;
    }

    /**
     * Determines if the QR-code should be saved to a separate file, or embedded
     * on the carrier.
     * 
     * @return a boolean, if embedding should be used
     */
    private boolean embedInCarrierFile() {
	return trueEncapsulate.isSelected();
    }

    /*
     * ****** RESTORATION **********
     */

    /**
     * Input an image file, look for a QR-code, recreate the payload file.
     */
    @Override
    public List<RestoredFile> restore(File qrCodeImage) throws IOException {
	List<RestoredFile> restoredFiles = new ArrayList<RestoredFile>();
	BufferedImage bufferedQrCode = ImageIO.read(qrCodeImage);
	LuminanceSource luminance = new BufferedImageLuminanceSource(bufferedQrCode);
	BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(luminance));
	QRCodeReader reader = new QRCodeReader();
	try {
	    Result result = reader.decode(bitmap);
	    PayloadSegment payloadSegment = PayloadSegment.getPayloadSegment(result.getText().getBytes());
	    String payloadName = payloadSegment.getPayloadName();
	    RestoredFile payloadFile = new RestoredFile(RESTORED_DIRECTORY + payloadName);
	    FileOutputStream out = new FileOutputStream(payloadFile);
	    out.write(payloadSegment.getPayloadBytes());
	    out.close();
	    payloadFile.algorithm = this;
	    payloadFile.wasPayload = true;
	    payloadFile.wasCarrier = false;
	    payloadFile.validateChecksum(payloadSegment.getPayloadChecksum());
	    if (payloadSegment.getRestorationMetadata().getProperty("usedMethod").equals(ON_IMAGE)) {
		RestoredFile carrier = restoreCarrier(qrCodeImage);
		payloadFile.relatedFiles.add(carrier);
		carrier.relatedFiles.add(payloadFile);
		restoredFiles.add(carrier);
	    }
	    restoredFiles.add(payloadFile);
	} catch (NotFoundException | ChecksumException | FormatException e) {
	    e.printStackTrace();
	}
	return restoredFiles;
    }

    private RestoredFile restoreCarrier(File qrCodeImage) {
	RestoredFile carrier = getRestoredCarrier(qrCodeImage);
	carrier.wasCarrier = true;
	carrier.wasPayload = false;
	carrier.algorithm = this;
	carrier.checksumValid = false;
	carrier.restorationNote = "Carrier can't be restored.";
	return carrier;
    }

    /*
     * **** CONFIGURATION ****************
     */

    /**
     * Creates the GUI that is used to configure this algorithm.
     */
    private void createConfigurationGui() {
	initButtons();
	panel = new GUIPanel();
	panel.setLayout(new GridBagLayout());
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.anchor = GridBagConstraints.NORTHWEST;
	constraints.gridwidth = 2;
	panel.add(new JLabel("<html><h2>QR-code options</h2></html>"), constraints);
	constraints.gridy++;
	constraints.gridwidth = 1;
	panel.add(new JLabel("QR-code size: "), constraints);
	constraints.gridx++;
	sizeField.setText("200");
	panel.add(sizeField, constraints);
	constraints.gridwidth = 2;
	constraints.gridx = 0;
	constraints.gridy++;
	constraints.gridy++;
	constraints.gridx = 0;
	constraints.gridwidth = 1;
	panel.add(new JLabel("Select output file format:"), constraints);
	constraints.gridx++;
	panel.add(imageFormatBox, constraints);
	constraints.gridx = 0;
	constraints.gridy++;

	// separate file:
	constraints.gridwidth = 2;
	panel.add(falseEncapsulate, constraints);
	constraints.gridy++;
	constraints.gridy++;

	// add to carrier:
	panel.add(trueEncapsulate, constraints);
	constraints.gridy++;

	JTextArea infoArea = new JTextArea();
	infoArea.setText("" + "\nPlease note: "
		+ "\n- The QR-code will be created from the first payload file, if the dataset has more than one payload files, and added to each carrier file. "
		+ "All other payload files will be ignored, if this option is selected."
		+ "\n- Carrier files need to be one of the following file types:" + "\n\tpng, jpeg, jp2, jpg, bmp, gif"
		+ "\n- All carrier files of other file formats are ignored."
		+ "\n- The QR-code pixel size should exceed the carrier image size in no dimension."
		+ "\n- If the size of the selected payload file is too big to create a QR-code from it, you will get an error message."
		+ "\n\n" + "Options for adding the QR-code to the carrier images:");
	infoArea.setEditable(false);
	infoArea.setLineWrap(true);
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.weightx = 1;
	panel.add(infoArea, constraints);
	constraints.fill = GridBagConstraints.NONE;
	constraints.gridy++;
	panel.add(new JLabel("On which image position should the barcode be added?"), constraints);
	constraints.gridy++;
	constraints.gridwidth = 1;
	panel.add(new JLabel("X :"), constraints);
	constraints.gridx++;
	xPositionField.setText("0");
	panel.add(xPositionField, constraints);
	constraints.gridx = 0;
	constraints.gridy++;
	panel.add(new JLabel("Y :"), constraints);
	constraints.gridx++;
	yPositionField.setText("0");
	panel.add(yPositionField, constraints);
	constraints.gridx = 0;
	constraints.gridy++;
	constraints.gridy++;
	panel.add(messageLabel, constraints);
    }

    /**
     * Change the text of a GUI JLabel to display state messages to the user.
     * 
     * @param message
     */
    private void displayMessage(String message) {
	messageLabel.setText("<html><h3>" + message + "</h3></html>");
    }

    /**
     * Initialises the GUI buttons.
     */
    private void initButtons() {
	ButtonGroup encapsulateGroup = new ButtonGroup();
	encapsulateGroup.add(trueEncapsulate);
	encapsulateGroup.add(falseEncapsulate);
	trueEncapsulate.setSelected(true);
	messageLabel.setForeground(Color.red);
    }

    /**
     * Configures which technique to use.
     * 
     * @param encapsulate
     *            true: A qr-code image is created from the first payload file
     *            and embedded on each carrier file.
     * 
     *            false: A qr-code image is created from each payload file and
     *            stored in an own file. The carriers are ignored.
     */
    public void setCarrierEncapsulation(boolean encapsulate) {
	trueEncapsulate.setSelected(encapsulate);
	falseEncapsulate.setSelected(!encapsulate);
    }

    /**
     * This is a visible watermarking algorithm. If the QR-code is added to a
     * carrier image, the carrier can't be restored.
     */
    @Override
    Scenario defineScenario() {
	Scenario scenario = new Scenario("QR-code scenario");
	scenario.description = "This is the ideal scenario for creating a QR-code.";
	scenario.setCriterionValue(ENCAPSULATION_METHOD, EMBEDDING);
	scenario.setCriterionValue(VISIBILITY, VISIBLE);
	scenario.setCriterionValue(DETECTABILITY, DETECTABLE);
	scenario.setCriterionValue(CARRIER_RESTORABILITY, NO);
	scenario.setCriterionValue(PAYLOAD_RESTORABILITY, YES);
	scenario.setCriterionValue(CARRIER_PROCESSABILITY, YES);
	scenario.setCriterionValue(PAYLOAD_ACCESSIBILITY, NO);
	scenario.setCriterionValue(ENCRYPTION, NO);
	scenario.setCriterionValue(COMPRESSION, YES);
	scenario.setCriterionValue(VELOCITY, NO);
	scenario.setCriterionValue(STANDARDS, YES);
	return scenario;
    }

    /**
     * Only image files can be used as carrier, but this algorithm allows
     * non-image files to be in the dataset. All carrier files that aren't an
     * image will be ignored for the encapsulation.
     */
    @Override
    SuffixFileFilter configureCarrierFileFilter() {
	return new AcceptAllFilter();
    }

    /**
     * Payload can be of any type.
     */
    @Override
    SuffixFileFilter configurePayloadFileFilter() {
	return new AcceptAllFilter();
    }

    /**
     * The algorithm only outputs images.
     */
    @Override
    SuffixFileFilter configureDecapsulationFileFilter() {
	List<String> supportedFileFormats = new ArrayList<String>();
	supportedFileFormats.add("jpg");
	supportedFileFormats.add("jpeg");
	supportedFileFormats.add("png");
	supportedFileFormats.add("bmp");
	supportedFileFormats.add("gif");
	return new SuffixFileFilter(supportedFileFormats);
    }

    /**
     * Name of the algorithm in the GUI list.
     */
    @Override
    public String getName() {
	return "QR-code watermarking";
    }

    /**
     * This description will be shown in the GUI.
     */
    @Override
    public String getDescription() {
	String description = "This algorithm creates QR-codes from the payload information.\n"
		+ "It can be configured to either create a QR-code file for each payload file,"
		+ " and ignore the carrier; Or create a QR-code from the first payload file, and "
		+ "add it on each carrier image.\n"
		+ "All carrier files that aren't images, will be ingored by this algorithm.";
	return description;
    }

    /**
     * A carrier file is not needed, but there has to be a minimum of one
     * payload file.
     */
    @Override
    public boolean fulfilledTechnicalCriteria(File carrier, List<File> payloadList) {
	return payloadList.size() == 1;
    }

    /**
     * Get the x-position of the carrier image, where the QR-code should be
     * added.
     * 
     * @return x-position
     */
    private int getXPosition() {
	try {
	    int size = Integer.parseInt(xPositionField.getText());
	    return size;
	} catch (Exception e) {
	    return 0;
	}
    }

    /**
     * Get the y-position of the carrier image, where the QR-code should be
     * added.
     * 
     * @return y position
     */
    private int getYPosition() {
	try {
	    int size = Integer.parseInt(yPositionField.getText());
	    return size;
	} catch (Exception e) {
	    return 0;
	}
    }

    /**
     * Get the size of the QR-code. This is configure by the user, and has a
     * default of 200*200.
     * 
     * @return QR-code size
     */
    private int getQRCodeSize() {
	try {
	    int size = Integer.parseInt(sizeField.getText());
	    return size;
	} catch (Exception e) {
	    return DEFAULT_SIZE;
	}
    }
}
