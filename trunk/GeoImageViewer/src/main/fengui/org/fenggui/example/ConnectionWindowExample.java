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
 * Created on Dec 07, 2005
 * $Id: ConnectionWindowExample.java 148 2007-01-24 18:52:11Z schabby $
 */
package org.fenggui.example;

import org.fenggui.Display;
import org.fenggui.composites.ConnectionWindow;
import org.fenggui.composites.Window;

public class ConnectionWindowExample implements IExample {

    Window filesFrame = null;
    Display display;
    
    private void buildFrame1() 
    {
    	ConnectionWindow cw = new ConnectionWindow(true);
    	cw.setX(50);
    	cw.setY(50);
    	cw.setSize(200, 250);
    	cw.layout();
    	
    	display.addWidget(cw);
    }

	public void buildGUI(Display g) {
		
		display = g;
		
		buildFrame1();
		
	}

	public String getExampleName() {
		return "Connection Window";
	}

	public String getExampleDescription() {
		return "Shows the Connection Window";
	}
    
}
