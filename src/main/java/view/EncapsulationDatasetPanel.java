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
package view;

import static main.Configuration.ADD_ICON;
import static main.Configuration.DELETE_ICON;
import static main.Configuration.START_ICON;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import algorithm.AbstractAlgorithm;
import model.EncapsulationData;

/**
 * The panel that displays the datasets with their carrier and payload files. It
 * provides buttons for the creation of new datasets and deletion of old ons.
 * Furthermore files can be added and removed from the datasets.
 */
class EncapsulationDatasetPanel extends GUIPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final EncapsulationTab tab;

    private final JButton carrierAddButton = initButton("Add / change carrier file", this, ADD_ICON);
    private final JButton payloadAddButton = initButton("Add payload file", this, ADD_ICON);
    private final JButton payloadRemButton = initButton("Remove payload file", this, DELETE_ICON);

    private final JButton encapsulateButton = initButton("Encapsulate", this, START_ICON);
    private final JButton createDatasetButton = initButton("Create dataset", this, ADD_ICON);
    private final JButton removeDatasetButton = initButton("Delete dataset", this, DELETE_ICON);
    private final JButton changeNameButton = initButton("Change name", this, null);
    private final JComboBox<EncapsulationData> dataList = new JComboBox<EncapsulationData>();

    private final JLabel carrierLabel = new JLabel("Add a carrier file");
    private final JList<File> payloadList = new JList<File>(new DefaultListModel<File>());
    private final JScrollPane payloadScrollPane = new JScrollPane(payloadList);

    protected EncapsulationDatasetPanel(EncapsulationTab tab) {
	this.tab = tab;
	fillDataList();
	encapsulateButton.setToolTipText("Start the encapsulation process.");
	payloadList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	constraints.gridwidth = 2;
	add(new JLabel("<html><h2>Carrier file: </h2></html>"), constraints);
	constraints.gridy++;
	add(carrierLabel, constraints);
	constraints.gridy++;
	add(carrierAddButton, constraints);
	constraints.gridy++;
	constraints.gridy++;
	constraints.gridy++;
	add(new JLabel("<html><h2>Payload files: </h2></html>"), constraints);
	constraints.gridy++;
	add(payloadScrollPane, constraints);
	constraints.gridy++;
	constraints.gridwidth = 1;
	add(payloadAddButton, constraints);
	constraints.gridx++;
	add(payloadRemButton, constraints);
	constraints.gridx += 3;
	constraints.gridy = 0;
	constraints.gridheight = 7;
	add(new JSeparator(SwingConstants.VERTICAL), constraints);
	constraints.gridheight = 1;
	constraints.gridx++;
	// general datasets
	add(new JLabel("<html><h2>Datasets: </h2></html>"), constraints);
	constraints.gridy++;
	add(dataList, constraints);
	constraints.gridy++;
	add(createDatasetButton, constraints);
	constraints.gridy++;
	add(removeDatasetButton, constraints);
	constraints.gridy++;
	add(changeNameButton, constraints);
	constraints.gridy++;
	constraints.gridy++;
	constraints.gridy++;
	add(encapsulateButton, constraints);
    }

    private void fillDataList() {
	DefaultComboBoxModel<EncapsulationData> model = new DefaultComboBoxModel<EncapsulationData>();
	dataList.setModel(model);
	for (EncapsulationData data : tab.gui.controller.datasets.get()) {
	    model.addElement(data);
	}
	if (model.getSize() > 0) {
	    model.setSelectedItem(model.getElementAt(0));
	}
	dataList.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		tab.selectedDatasetPanel.showFiles((EncapsulationData) dataList.getSelectedItem());
	    }
	});
    }

    /**
     * Returns the data set that is selected by the user.
     * 
     * @return selected data set
     */
    protected EncapsulationData getSelectedDataset() {
	EncapsulationData selectedData = (EncapsulationData) dataList.getSelectedItem();
	return selectedData;
    }

    /**
     * Called by {@link GUI} to update the displayed data set files at tool
     * start.
     */
    protected void showDatasetInformation() {
	dataList.setSelectedItem(dataList.getSelectedItem());
    }

    /**
     * Adds a data set to the view list of data sets.
     * 
     * @param data
     *            the data set to be added.
     */
    protected void addDataset(EncapsulationData data) {
	((DefaultComboBoxModel<EncapsulationData>) dataList.getModel()).addElement(data);
	dataList.setSelectedItem(data);
    }

    /**
     * Removes a data set from the view list of data sets.
     * 
     * @param data
     *            data set to be removed
     */
    protected void removeDataset(EncapsulationData data) {
	((DefaultComboBoxModel<EncapsulationData>) dataList.getModel()).removeElement(data);
    }

    public void showFiles(EncapsulationData selectedData) {
	if (selectedData == null) {
	    return;
	}
	((DefaultListModel<File>) payloadList.getModel()).removeAllElements();
	List<File> payloadPaths = selectedData.getPayload();
	File carrier = selectedData.getCarrier();
	setCarrierLabel(carrier);
	for (File path : payloadPaths) {
	    ((DefaultListModel<File>) payloadList.getModel()).addElement(path);
	}
	tab.outputPanel.refreshOutputList();
	tab.algorithmPanel.colouriseAlgorithmList(selectedData);
	this.revalidate();
	this.repaint();
    }

    /**
     * Event handler for this class.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
	EncapsulationData selectedDataset = tab.selectedDatasetPanel.getSelectedDataset();
	if (e.getSource() == carrierAddButton) {
	    changeCarrierOfSelectedDataset(selectedDataset);
	} else if (e.getSource() == payloadAddButton) {
	    addPayloadToSelectedDataset(selectedDataset);
	} else if (e.getSource() == payloadRemButton) {
	    removePayloadFromSelectedDataset(selectedDataset);
	} else if (e.getSource() == encapsulateButton) {
	    encapsulate();
	} else if (e.getSource() == createDatasetButton) {
	    createDataset();
	} else if (e.getSource() == removeDatasetButton) {
	    removeDataset();
	} else if (e.getSource() == changeNameButton) {
	    changeName();
	}
    }

    private void encapsulate() {
	EncapsulationData dataset = tab.selectedDatasetPanel.getSelectedDataset();
	AbstractAlgorithm selectedAlgorithm = tab.algorithmPanel.getSelectedAlgorithm();
	tab.gui.controller.encapsulateDataset(dataset, selectedAlgorithm);
	tab.outputPanel.refreshOutputList();
	tab.gui.decapsulationTab.decapsulationDataPanel.refreshDataList();
    }

    /**
     * Creates a dialog that asks the user for a name for a new data set, ands
     * creates this data set afterwards.
     */
    private void createDataset() {
	final JDialog dialog = new JDialog();
	dialog.setTitle("Create new dataset");
	dialog.setLayout(new GridBagLayout());
	dialog.setPreferredSize(new Dimension(400, 200));
	dialog.setLocationRelativeTo(null);
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.anchor = GridBagConstraints.LINE_START;
	constraints.insets = new Insets(5, 5, 5, 5);
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 2;
	dialog.add(new JLabel("Enter dataset name: "), constraints);
	constraints.gridy++;
	final JTextField textField = new JTextField(30);
	dialog.add(textField, constraints);
	constraints.gridy++;
	constraints.gridwidth = 1;
	JButton addButton = new JButton("Create dataset");
	addButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		tab.gui.controller.datasets.create(textField.getText());
		dialog.setVisible(false);
	    }
	});
	dialog.add(addButton, constraints);
	constraints.gridx++;
	JButton cancelButton = new JButton("Cancel");
	cancelButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		dialog.setVisible(false);
	    }
	});
	dialog.add(cancelButton, constraints);
	dialog.pack();
	dialog.setVisible(true);
    }

    private void changeName() {
	final JDialog dialog = new JDialog();
	dialog.setTitle("Change Name");
	dialog.setLayout(new GridBagLayout());
	dialog.setPreferredSize(new Dimension(400, 200));
	dialog.setLocationRelativeTo(null);
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.anchor = GridBagConstraints.LINE_START;
	constraints.insets = new Insets(5, 5, 5, 5);
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 2;
	dialog.add(new JLabel("Change dataset name: "), constraints);
	constraints.gridy++;
	final JTextField textField = new JTextField(30);
	dialog.add(textField, constraints);
	constraints.gridy++;
	constraints.gridwidth = 1;
	JButton addButton = new JButton("Save");
	addButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		EncapsulationData dataset = getSelectedDataset();
		dataset.setName(textField.getText());
		tab.gui.controller.datasets.refresh(dataset);
		dialog.setVisible(false);
	    }
	});
	dialog.add(addButton, constraints);
	constraints.gridx++;
	JButton cancelButton = new JButton("Cancel");
	cancelButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		dialog.setVisible(false);
	    }
	});
	dialog.add(cancelButton, constraints);
	dialog.pack();
	dialog.setVisible(true);
    }

    private void setCarrierLabel(File carrier) {
	if (carrier != null) {
	    carrierLabel.setText("" + carrier.toPath());
	} else {
	    carrierLabel.setText("Add a carrier file.");
	}
    }

    private void removeDataset() {
	EncapsulationData selectedDataset = getSelectedDataset();
	if (selectedDataset != null) {
	    tab.gui.controller.datasets.remove(selectedDataset);
	}
    }

    private void changeCarrierOfSelectedDataset(EncapsulationData selectedDataset) {
	File file = getCarrierFromDialogue();
	if (file != null) {
	    selectedDataset.setCarrier(file);
	    setCarrierLabel(file);
	    tab.gui.controller.datasets.refresh(selectedDataset);
	    tab.algorithmPanel.colouriseAlgorithmList(selectedDataset);
	}
    }

    private void addPayloadToSelectedDataset(EncapsulationData selectedDataset) {
	List<File> payloadFiles = getPayloadFromDialogue();
	if (payloadFiles.size() > 0) {
	    for (File file : payloadFiles) {
		selectedDataset.addPayload(file);
	    }
	    tab.gui.controller.datasets.refresh(selectedDataset);
	    tab.algorithmPanel.colouriseAlgorithmList(selectedDataset);
	}
    }

    private void removePayloadFromSelectedDataset(EncapsulationData selectedDataset) {
	File selectedFile = payloadList.getSelectedValue();
	if (selectedFile != null) {
	    selectedDataset.removePayload(selectedFile);
	    tab.gui.controller.datasets.refresh(selectedDataset);
	    removePayload(selectedFile);
	    tab.algorithmPanel.colouriseAlgorithmList(selectedDataset);
	}
    }

    private File getCarrierFromDialogue() {
	JFileChooser fileChooser = new JFileChooser("Choose carrier file to be added");
	fileChooser.setDialogTitle("Add carrier");
	fileChooser.setDialogType(JFileChooser.FILES_AND_DIRECTORIES);
	fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	fileChooser.setMultiSelectionEnabled(false);
	fileChooser.setAcceptAllFileFilterUsed(true);
	final int result = fileChooser.showDialog(null, "add");
	if (result == JFileChooser.APPROVE_OPTION) {
	    return fileChooser.getSelectedFile();
	}
	return null;
    }

    private List<File> getPayloadFromDialogue() {
	List<File> files = new ArrayList<File>();
	JFileChooser fileChooser = new JFileChooser("Choose payload files to be added");
	fileChooser.setDialogTitle("Add payload");
	fileChooser.setDialogType(JFileChooser.FILES_AND_DIRECTORIES);
	fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	fileChooser.setMultiSelectionEnabled(true);
	fileChooser.setAcceptAllFileFilterUsed(true);
	final int result = fileChooser.showDialog(null, "add");
	if (result == JFileChooser.APPROVE_OPTION) {
	    List<File> selectedFiles = new ArrayList<File>();
	    for (File selectedPath : fileChooser.getSelectedFiles()) {
		selectedFiles.add(selectedPath);
	    }
	    return selectedFiles;
	}
	return files;
    }

    private void removePayload(File file) {
	((DefaultListModel<File>) payloadList.getModel()).removeElement(file);
    }
}
