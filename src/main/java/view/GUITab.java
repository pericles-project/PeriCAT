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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class GUITab extends JScrollPane {
    private static final long serialVersionUID = 1L;
    protected final JPanel panel = new JPanel();
    protected GridBagConstraints constraints;

    public GUITab(String title) {
	getViewport().add(panel);
	panel.setLayout(new GridBagLayout());
	constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.anchor = GridBagConstraints.WEST;
	constraints.fill = GridBagConstraints.BOTH;
	constraints.weighty = 0;
	panel.add(new JLabel("<html><h1>" + title + "</h1></html>"), constraints);
	constraints.gridy++;
    }

    public void addGuiComponent(JPanel component, String title, GridBagConstraints constraints) {
	Border border = BorderFactory.createEtchedBorder();
	TitledBorder titleBorder = BorderFactory.createTitledBorder(border, "<html><h1>" + title + "</h1></html>",
		TitledBorder.CENTER, TitledBorder.TOP);
	component.setBorder(titleBorder);
	panel.add(component, constraints);
	constraints.gridy++;
    }
}
