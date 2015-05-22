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
 * Created on 2005-3-1
 * $Id: LayoutExample.java 309 2007-07-04 22:01:56Z Schabby $
 */
package org.fenggui.example;

import org.fenggui.Button;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.Label;
import org.fenggui.composites.Window;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.layout.BorderLayoutData;
import org.fenggui.layout.FormAttachment;
import org.fenggui.layout.FormData;
import org.fenggui.layout.FormLayout;
import org.fenggui.layout.RowLayout;
import org.fenggui.util.Spacing;


/**
 * Small example app that plays around with some basic layouts.
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-07-05 00:01:56 +0200 (Do, 05 Jul 2007) $
 * @version $Revision: 309 $
 */
public class LayoutExample implements IExample 
{
    private Display desk;
    
    private Window smallTestFrame;
    
    private void buildFormLayoutWindow() 
    {
        smallTestFrame = new Window(true, false, false, true);
        desk.addWidget(smallTestFrame);
        smallTestFrame.setX(50);
        smallTestFrame.setY(50);
        smallTestFrame.setSize(250, 150);
        //smallTestFrame.doLayout();
        smallTestFrame.setTitle("Form Layout");
        
        Button b1 = new Button("Upper Left");
        smallTestFrame.getContentContainer().addWidget(b1);
        FormData fd = new FormData();
        fd.left = new FormAttachment(0, 0);
        fd.top = new FormAttachment(100, 0);
        b1.setLayoutData(fd);
        
        Button b2 = new Button("Upper Right");
        smallTestFrame.getContentContainer().addWidget(b2);
        fd = new FormData();
        fd.right = new FormAttachment(100, 0);
        fd.top = new FormAttachment(100, 0);
        b2.setLayoutData(fd);
        
        Button b3 = new Button("Lower Right");
        smallTestFrame.getContentContainer().addWidget(b3);
        fd = new FormData();
        fd.right = new FormAttachment(100, 0);
        fd.bottom = new FormAttachment(0, 0);
        b3.setLayoutData(fd);
        
        Button b4 = new Button("Switch Resizable");
        smallTestFrame.getContentContainer().addWidget(b4);
        fd = new FormData();
        fd.left = new FormAttachment(0, 0);
        fd.bottom = new FormAttachment(0, 0);
        b4.setLayoutData(fd);
        
        Button b5 = new Button("Upper Center");
        smallTestFrame.getContentContainer().addWidget(b5);
        fd = new FormData();
        fd.left = new FormAttachment(b1, 0);
        fd.top = new FormAttachment(100, 0);
        fd.right = new FormAttachment(b2, 0);
        b5.setLayoutData(fd);
        
        b4.addButtonPressedListener(new IButtonPressedListener()
		{

			public void buttonPressed(ButtonPressedEvent e)
			{
				smallTestFrame.setResizable(!smallTestFrame.isResizable());
            }
        });
        
        smallTestFrame.getContentContainer().setLayoutManager(new FormLayout());
        
        smallTestFrame.layout();
    }
    
    private void buildBorderLayoutWindow() 
    {
        final Window filesFrame = new Window(true, false, false, true);
        desk.addWidget(filesFrame);
        filesFrame.setX(280);
        filesFrame.setY(230);
        filesFrame.setTitle("Border Layout");
        filesFrame.setSize(200, 100);
        filesFrame.getContentContainer().setLayoutManager(new org.fenggui.layout.BorderLayout());
        
        final Label label = new Label("This is a label in the north");
        filesFrame.getContentContainer().addWidget(label);
        
        label.setLayoutData(BorderLayoutData.NORTH);
        
        Label westLabel = new Label("West");
        filesFrame.getContentContainer().addWidget(westLabel);
        westLabel.setLayoutData(BorderLayoutData.WEST);
        
        Button button = new Button("Show Big Frame");
        filesFrame.getContentContainer().addWidget(button);
        button.setLayoutData(BorderLayoutData.SOUTH);
        
        Label eastLabel = new Label("East");
        filesFrame.getContentContainer().addWidget(eastLabel);
        eastLabel.setLayoutData(BorderLayoutData.EAST);
        
        button.addButtonPressedListener(new IButtonPressedListener()
		{

			public void buttonPressed(ButtonPressedEvent e)
			{
                label.setText("The answer to the world and everything is: "+System.currentTimeMillis());
                
                filesFrame.getContentContainer().layout(); // to re-center the label
                desk.addWidget(smallTestFrame);
            }
            
        });
        
        filesFrame.layout();
    }
        
    private void buildRowLayoutWindow()
    {
        final Window flowFrame = new Window(true, false, false, true);
        flowFrame.setTitle("Vertical Row Layout");
        desk.addWidget(flowFrame);
        flowFrame.setX(100);
        flowFrame.setY(230);
        Container cont = flowFrame.getContentContainer();
        cont.setLayoutManager(new RowLayout(false));
        cont.addWidget(new Button("One Button"));
        
        Button b = new Button("Can't Expand");
        cont.addWidget(b);
        b.setSizeToMinSize();
        b.setExpandable(false);
        
        Button b3 = new Button("Button With Margin");
        cont.addWidget(b3);
        b3.getAppearance().setMargin(new Spacing(20, 10));
        Button b4 = new Button("Button With Padding");
        cont.addWidget(b4);
        b4.getAppearance().setPadding(new Spacing(20,20));
        
        cont.addWidget(new Button("Button at Bottom"));
        
        flowFrame.pack();
    }

	public void buildGUI(Display d) {
		desk = d;
		
		buildBorderLayoutWindow();
		buildRowLayoutWindow();
		buildFormLayoutWindow();
		
		desk.layout();
		
	}

	public String getExampleName() {
		return "Various Layouts";
	}

	public String getExampleDescription() {
		return "Shows some layouts";
	}

}

