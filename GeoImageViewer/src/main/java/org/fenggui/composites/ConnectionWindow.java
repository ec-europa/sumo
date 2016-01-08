/*
 * FengGUI - Java GUIs in OpenGL (http://www.fenggui.org)
 * 
 * Copyright (c) 2005, 2006 FengGUI Project
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details:
 * http://www.gnu.org/copyleft/lesser.html#TOC3
 * 
 * $Id: ConnectionWindow.java 264 2007-04-16 12:34:05Z bbeaulant $
 */
package org.fenggui.composites;

import org.fenggui.Button;
import org.fenggui.Container;
import org.fenggui.FengGUI;
import org.fenggui.Label;
import org.fenggui.TextEditor;
import org.fenggui.border.TitledBorder;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.layout.Alignment;
import org.fenggui.layout.FormAttachment;
import org.fenggui.layout.FormData;
import org.fenggui.layout.FormLayout;
import org.fenggui.layout.GridLayout;
import org.fenggui.util.Spacing;

/**
 * Window that contains TextFields to enter an address, port, user name
 * and password. It also has a label to notify the user about something.
 * 
 * @author Johannes Schaback, last edtir by $Author: bbeaulant $, $Date: 2007-04-16 14:34:05 +0200 (Mo, 16 Apr 2007) $
 * @version $Revision: 264 $
 *
 */
public class ConnectionWindow extends Window 
{

	private TextEditor addressTextField, portTextField, loginNameTextField, passwordTextField;
	
	private Label statusLabel;

	private Container addressContainer;
	private Container loginContainer;
	private Button connectButton, cancelButton;
	
	/**
	 * Creates a new <code>ConnectionWindow</code>.
	 * @param closeBtn if this Window has a close button
	 */
	public ConnectionWindow(boolean closeBtn) 
	{
		super(closeBtn, false, false, true);
		
		setupTheme(ConnectionWindow.class);
		
		setSize(250, 280);
		setTitle("Connection Window");
		((Container)getContentContainer()).setLayoutManager(new FormLayout());
		
		addressContainer = FengGUI.createContainer(getContentContainer());
		
		addressContainer.getAppearance().add(new TitledBorder("Address"));
		addressContainer.getAppearance().setMargin(new Spacing(1, 5));
		addressContainer.getAppearance().setPadding(new Spacing(5,5));
		FormData fd = new FormData();
		fd.left = new FormAttachment(0,0);
		fd.right = new FormAttachment(100,0);
		fd.top = new FormAttachment(100,0);
		addressContainer.setLayoutData(fd);
		addressContainer.setLayoutManager(new GridLayout(2,2));
		
		Label l1 = FengGUI.createLabel(addressContainer, "Address:");
		l1.getAppearance().setMargin(new Spacing(0, 0, 0, 5));
		
		addressTextField = FengGUI.createTextField(addressContainer);
		addressTextField.getAppearance().setMargin(new Spacing(0, 0, 0, 2));
		addressTextField.setSize(40, addressTextField.getMinHeight());
		addressTextField.setShrinkable(false);
				
		FengGUI.createLabel(addressContainer, "Port:");
		portTextField = FengGUI.createTextField(addressContainer);
		
		loginContainer = FengGUI.createContainer(getContentContainer());
		loginContainer.getAppearance().add(new TitledBorder("Login"));
		loginContainer.getAppearance().setMargin(new Spacing(1, 5));
		loginContainer.getAppearance().setPadding(new Spacing(5, 5));
		fd = new FormData();
		fd.left = new FormAttachment(0,0);
		fd.right = new FormAttachment(100,0);
		fd.top = new FormAttachment(addressContainer,0);
		loginContainer.setLayoutData(fd);
		
		loginContainer.setLayoutManager(new GridLayout(2,2));
		Label l3 = FengGUI.createLabel(loginContainer, "Name:");
		l3.getAppearance().setMargin(new Spacing(0,0,0,5));

		loginNameTextField = FengGUI.createTextField(loginContainer);
		loginNameTextField.getAppearance().setMargin(new Spacing(0,0,0,2));
		loginNameTextField.updateMinSize();
		loginNameTextField.setSize(40, loginNameTextField.getMinHeight());
		loginNameTextField.setShrinkable(false);
		
		FengGUI.createLabel(loginContainer, "Password:");
		
		passwordTextField = FengGUI.createTextField(loginContainer);
		passwordTextField.setPasswordField(true);
		
		connectButton = FengGUI.createButton(getContentContainer(), "Connect");
		connectButton.getAppearance().setMargin(new Spacing(2, 2));
		
		cancelButton = FengGUI.createButton(getContentContainer(), "Cancel");
		cancelButton.getAppearance().setMargin(new Spacing(2, 2));
		
		statusLabel = FengGUI.createLabel(getContentContainer(), "Say something...");
		statusLabel.getAppearance().setAlignment(Alignment.MIDDLE);
		
		fd = new FormData();
		fd.left = new FormAttachment(0,0);
		fd.right = new FormAttachment(100,0);
		fd.bottom = new FormAttachment(connectButton,0);
		fd.top = new FormAttachment(loginContainer,0);
		statusLabel.setLayoutData(fd);		
		
		fd = new FormData();
		fd.left = new FormAttachment(0,0);
		fd.right = new FormAttachment(50,0);
		fd.bottom = new FormAttachment(0,0);
		cancelButton.setLayoutData(fd);		
		
		fd = new FormData();
		fd.left = new FormAttachment(50,0);
		fd.right = new FormAttachment(100,0);
		fd.bottom = new FormAttachment(0, 0);
		connectButton.setLayoutData(fd);
		
		cancelButton.addButtonPressedListener(new IButtonPressedListener() {

			public void buttonPressed(ButtonPressedEvent e)
			{
				close();
			}

		});
		
		layout();
	}

	public Container getAddressContainer() {
		return addressContainer;
	}

	public TextEditor getAddressTextField() {
		return addressTextField;
	}

	public Button getCancelButton() {
		return cancelButton;
	}

	public Button getConnectButton() {
		return connectButton;
	}

	public Container getLoginContainer() {
		return loginContainer;
	}

	public TextEditor getLoginNameTextField() {
		return loginNameTextField;
	}

	public TextEditor getPasswordTextField() {
		return passwordTextField;
	}

	public TextEditor getPortTextField() {
		return portTextField;
	}

	public Label getStatusLabel() {
		return statusLabel;
	}

}
