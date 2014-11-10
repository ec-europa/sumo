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
 * Created on Jul 15, 2005
 * $Id: ComboBoxExample.java 264 2007-04-16 12:34:05Z bbeaulant $
 */
package org.fenggui.example;

import org.fenggui.ComboBox;
import org.fenggui.Display;
import org.fenggui.composites.Window;
import org.fenggui.layout.StaticLayout;


/**
 * Small example app to test a combo box.
 * 
 * 
 * 
 * @author Johannes Schaback 
 */
public class ComboBoxExample implements IExample {


    /**
	 * 
	 */
	private static final long serialVersionUID = 4L;
	
    private Display desk = null;

    private void buildFrame() {

        Window frame = new Window(true, false, false, true);
        desk.addWidget(frame);
        frame.setX(50);
        frame.setY(100);
        frame.setSize(300, 100);
        frame.setShrinkable(false);
        
        frame.setTitle("Pick a tea flavor...");
        
        frame.getContentContainer().setLayoutManager(new StaticLayout());
        
        ComboBox list = new ComboBox();
        frame.getContentContainer().addWidget(list);
        list.setSize(250, list.getMinHeight());
        list.setShrinkable(false);
        list.setX(25);
        list.setY(25);

        list.addItem(" Green Tea");
        list.addItem(" Chinese Tea");
        list.addItem(" English Tea");
        list.addItem(" Milk Tea");
        list.addItem(" Ginseng Tea");
        list.addItem(" Herbal Tea");
        list.addItem(" Ceylon Tea");
        list.addItem(" Black Tea");
        
        desk.layout();
    }
    

	
	public void buildGUI(Display d) {
		this.desk = d;
		
		buildFrame();
		
	}



	public String getExampleName() {
		return "ComboBox Example";
	}



	public String getExampleDescription() {
		return "Demonstrates the how to use ComboBoxes";
	}
	
	

}
