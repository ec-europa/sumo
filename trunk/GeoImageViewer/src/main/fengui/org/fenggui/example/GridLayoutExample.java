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
 * $Id: GridLayoutExample.java 114 2006-12-12 22:06:22Z schabby $
 */
package org.fenggui.example;

import org.fenggui.Button;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.border.PlainBorder;
import org.fenggui.layout.GridLayout;
import org.fenggui.layout.StaticLayout;
import org.fenggui.util.Color;

/**
 * Demonstrates the Layout Manager for grid layouts.
 * 
 * @todo Comment this class... #
 * 
 * @author Johannes Schaback, last edited by $Author: schabby $, $Date: 2006-12-12 23:06:22 +0100 (Tue, 12 Dec 2006) $
 * @version $Revision: 114 $
 */
public class GridLayoutExample implements IExample {

    private Display display;


	public void buildGUI(Display g) {
		display = g;

    	Container c = new Container();
    	c.setXY(10, 10);
    	c.setLayoutManager(new GridLayout(3, 3));
    	c.getAppearance().add(new PlainBorder(Color.GRAY));
    	
    	c.addWidget(new Button("Button 1"));
    	c.addWidget(new Button("Button 2"));
    	c.addWidget(new Button("Button 3"));
    	c.addWidget(new Button("Button 4"));
    	c.addWidget(new Button("Button 5"));
    	c.addWidget(new Button("Button 6"));
    	c.addWidget(new Button("Button 7"));
    	c.addWidget(new Button("Button 8"));
    	c.addWidget(new Button("Button 9"));
    	
    	display.addWidget(c);
    	
    	c.pack();
    	System.out.println(c.getWidget(3));
    	StaticLayout.center(c, display);
	}

	public String getExampleName() {
		return "Grid Layout Example";
	}

	public String getExampleDescription() {
		return "Shows a Container with a GridLayout manager";
	}


}
