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
 * $Id: ScrollContainerExample.java 264 2007-04-16 12:34:05Z bbeaulant $
 */
package org.fenggui.example;

import org.fenggui.*;
import org.fenggui.background.PlainBackground;
import org.fenggui.composites.Window;
import org.fenggui.render.Font;
import org.fenggui.util.Alphabet;
import org.fenggui.util.Color;
import org.fenggui.util.fonttoolkit.FontFactory;

/**
 * Small example app that uses <code>ScrollContainer</code>s.
 *  
 * @author Johannes Schaback 
 */
public class ScrollContainerExample implements IExample {
	
    private Display desk;


    private void buildFrame() {

        final Window filesFrame = new Window(true, false, false, true);
        desk.addWidget(filesFrame);
        filesFrame.setX(50);
        filesFrame.setY(50);
        filesFrame.setSize(300, 300);
        filesFrame.setTitle("ScrollContainer");

        ScrollContainer sc = new ScrollContainer();
        filesFrame.setContentContainer(sc);
        
        Label l = new Label("This text is far too long to" +
        		" be displayed in one single line. But fortunately, Johannes " +
        		"implemented scrolling so that we all can enjoy the pleasure" +
        		" to read this text!   Ahhhh. I'm getting sick of these colors.");
        sc.setInnerWidget(l);
        
        Font serif = FontFactory.renderStandardFont(new java.awt.Font("Times", java.awt.Font.PLAIN, 45), true, Alphabet.getDefaultAlphabet());
        l.getAppearance().setFont(serif);
        
        l.getAppearance().setTextColor(Color.WHITE);
        l.getAppearance().add(new PlainBackground(Color.DARK_GREEN));
        
    }

    
	public void buildGUI(Display d) {
		
		desk = d;
		buildFrame();
		
        desk.layout();
	}

	public String getExampleName() {
		return "ScrollContainer Example";
	}

	public String getExampleDescription() {
		return "Shows a ScrollContainer";
	}

}
