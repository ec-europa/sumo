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
 * $Id: ScrollBarExample.java 114 2006-12-12 22:06:22Z schabby $
 */
package org.fenggui.example;

import org.fenggui.Display;
import org.fenggui.Label;
import org.fenggui.ScrollBar;
import org.fenggui.composites.Window;
import org.fenggui.layout.*;
import org.fenggui.util.Spacing;

/**
 * 
 * Little demo app for scroll bars.
 * 
 * @author Johannes Schaback ($Author: schabby $)
 */
public class ScrollBarExample implements IExample {
	
    private Display desk;

    private void buildHorizontalScrollBarFrame() {
        
    	Window f = new Window();
    	f.setTitle("Horizontal Scroll Bar Test");
    	desk.addWidget(f);
        
    	f.setSize(200, 100);
    	f.setX(10);
    	f.setY(50);
    	
    	ScrollBar sc = new ScrollBar(true);
    	sc.setLayoutData(BorderLayoutData.SOUTH);
    	sc.getAppearance().setMargin(new Spacing(5, 5));
    	f.getContentContainer().addWidget(sc);
    	
    	f.getContentContainer().setLayoutManager(new BorderLayout());
   
    	Label l = new Label("Check out the horizontal slider!");
    	l.getAppearance().setAlignment(Alignment.MIDDLE);
    	f.getContentContainer().addWidget(l);
    	l.setLayoutData(BorderLayoutData.CENTER);
    }
    
    private void buildVerticalScrollBarFrame() {
        
    	Window f = new Window();
    	f.setTitle("Vertical Scroll Bar Test");
    	desk.addWidget(f);
        
    	f.setSize(200, 200);
    	f.setX(50);
    	f.setY(160);
    	
    	ScrollBar sc = new ScrollBar(false);
    	sc.getAppearance().setMargin(new Spacing(5, 5));
    	sc.setLayoutData(BorderLayoutData.WEST);
    	f.getContentContainer().addWidget(sc);
    	
    	Label l = new Label("Check out the vertical slider");
    	l.setLayoutData(BorderLayoutData.CENTER);
    	l.getAppearance().setAlignment(Alignment.MIDDLE);
    	f.getContentContainer().addWidget(l);
    	
    	f.getContentContainer().setLayoutManager(new BorderLayout());
    }

	public void buildGUI(Display d) {
		desk = d;
		
		buildHorizontalScrollBarFrame();
		buildVerticalScrollBarFrame();
		
		desk.layout();
	}

	public String getExampleName() {
		return "ScrollBar Example";
	}

	public String getExampleDescription() {
		return "Shows two frames with scroll bars";
	}    
    

}
