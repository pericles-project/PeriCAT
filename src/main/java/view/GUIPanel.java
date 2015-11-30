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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class GUIPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    public GridBagConstraints constraints;

    public GUIPanel() {
	setLayout(new GridBagLayout());
	constraints = new GridBagConstraints();
	constraints.anchor = GridBagConstraints.FIRST_LINE_START;
	constraints.insets = new Insets(5, 5, 5, 5);
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.weightx = 1;
	constraints.weighty = 1;
	constraints.fill = GridBagConstraints.BOTH;
    }

    protected static JButton initButton(String label, ActionListener listener, Icon icon) {
	JButton button = null;
	if (icon != null) {
	    button = new JButton(label, icon);
	} else {
	    button = new JButton(label);
	}
	button.addActionListener(listener);
	return button;
    }
}
