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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class ScenarioBackPanel extends GUIPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    private final JButton backButton = new JButton("Back to encapsulation tab");
    private final ScenarioTab tab;

    public ScenarioBackPanel(ScenarioTab tab) {
	this.tab = tab;
	backButton.addActionListener(this);
	backButton.setToolTipText("Switch back to the encapsulation tab.");
	add(backButton, constraints);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == backButton) {
	    tab.gui.tabbedPane.setSelectedIndex(0);
	}
    }
}
