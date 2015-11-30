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

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import controller.PeriCATController;

/**
 * This class creates a system tray icon with a menu menu and handles user
 * events, if an option of the menu is selected by the user.
 */
public final class SystemTrayIcon implements ActionListener {
    public static String TRAY_ICON_IMAGE = "/images/logo.png";

    protected TrayIcon trayIcon;
    protected PopupMenu menu;
    protected SystemTray systemTray;
    protected PeriCATController controller;

    public SystemTrayIcon(PeriCATController controller) {
	this.controller = controller;
	menu = new PopupMenu();
	trayIcon = createTrayIcon();
	if (trayIcon != null) {
	    addTayIconToSystemTray(trayIcon);
	    trayIcon.addMouseListener(new TrayMouseListener());
	}
    }

    private TrayIcon createTrayIcon() {
	URL url = this.getClass().getResource(TRAY_ICON_IMAGE);
	if (url == null) {
	    return null;
	}
	Image image = Toolkit.getDefaultToolkit().getImage(url);
	createMenu();
	TrayIcon trayIcon = new TrayIcon(image, "Pericles", menu);
	trayIcon.setImageAutoSize(true);
	return trayIcon;
    }

    MenuItem exitItem;

    private void createMenu() {
	exitItem = createMenuItem("Exit");
    }

    private MenuItem createMenuItem(String menuText) {
	MenuItem item = new MenuItem(menuText);
	item.addActionListener(this);
	menu.add(item);
	return item;
    }

    private void addTayIconToSystemTray(TrayIcon trayIcon) {
	this.systemTray = SystemTray.getSystemTray();
	try {
	    systemTray.add(trayIcon);
	} catch (AWTException e) {
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == exitItem) {
	    controller.exit();
	}
    }

    class TrayMouseListener extends MouseAdapter {
	@Override
	public void mouseClicked(MouseEvent e) {
	    if (controller.gui != null) {
		controller.gui.toFront();
	    }
	}
    }
}
