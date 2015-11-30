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

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.RestoredFile;

/**
 * Panel that displays the restored files
 */
class DecapsulationOutputPanel extends GUIPanel {
    private static final long serialVersionUID = 1L;

    private final JList<RestoredFile> outputList = new JList<RestoredFile>(new DefaultListModel<RestoredFile>());
    private final JTextArea descriptionArea = new JTextArea();

    protected DecapsulationOutputPanel() {
	outputList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	outputList.addListSelectionListener(new SelectionListener());
	descriptionArea.setWrapStyleWord(true);
	descriptionArea.setLineWrap(true);
	descriptionArea.setEditable(false);
	descriptionArea.setText("\n\n\n\n\n");
	JScrollPane descriptionPane = new JScrollPane(descriptionArea);
	add(new JLabel("Select a decapsulated file to display further information."), constraints);
	constraints.gridy++;
	add(outputList, constraints);
	constraints.gridy++;
	add(descriptionPane, constraints);
    }

    public void updateOutputFiles(List<RestoredFile> restoredFiles) {
	for (RestoredFile file : restoredFiles) {
	    ((DefaultListModel<RestoredFile>) outputList.getModel()).addElement(file);
	}
    }

    private class SelectionListener implements ListSelectionListener {
	@Override
	public void valueChanged(ListSelectionEvent e) {
	    if (e.getSource() == outputList) {
		RestoredFile selectedFile = outputList.getSelectedValue();
		if (selectedFile == null) {
		    return;
		}
		descriptionArea.setText("Selected file: " + selectedFile.getName() + "\n\n");
		descriptionArea.append("File type: ");
		if (selectedFile.wasPayload) {
		    descriptionArea.append("payload");
		} else if (selectedFile.wasCarrier) {
		    descriptionArea.append("carrier");
		} else {
		    descriptionArea.append("unknown");
		}
		descriptionArea.append("\nFile restored correctly: " + selectedFile.checksumValid + "\n");
		descriptionArea.append("Restoration note: " + selectedFile.restorationNote + "\n");
		descriptionArea.append("Used encapsulation algorithm: " + selectedFile.algorithm + "\n");
		descriptionArea.append("Original file path: " + selectedFile.originalFilePath + "\n");
		descriptionArea.append("\nList of related files: \n");
		for (RestoredFile relatedFile : selectedFile.relatedFiles) {
		    descriptionArea.append("\t");
		    if (relatedFile.wasPayload) {
			descriptionArea.append("payload: ");
		    } else if (relatedFile.wasCarrier) {
			descriptionArea.append("carrier: ");
		    }
		    descriptionArea.append(relatedFile.getName() + "\n");
		}
		revalidate();
		repaint();
	    }
	}
    }
}
