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
 * $Id: ClippingExample.java 264 2007-04-16 12:34:05Z bbeaulant $
 */
package org.fenggui.example;

import java.io.IOException;

import org.fenggui.Canvas;
import org.fenggui.Container;
import org.fenggui.DecoratorAppearance;
import org.fenggui.Display;
import org.fenggui.Label;
import org.fenggui.composites.Window;
import org.fenggui.layout.RowLayout;
import org.fenggui.render.Binding;
import org.fenggui.render.BufferedTextRenderer;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.render.ITexture;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;

/**
 * Tests the basic clipping.
 * 
 * @author Johannes Schaback ($Author: bbeaulant $)
 */
public class ClippingExample implements IExample {

    private Window frame = null;
    private Display desk;

   
    private void buildTestFrame() {

        frame = new Window(true, false, false, true);
        desk.addWidget(frame);
        frame.setX(50);
        frame.setY(50);
        frame.setTitle("Clipping Example");
        
        Label l = new Label("This text is too long" +
                " to be displayed and has to be clipped!");
        frame.getContentContainer().addWidget(l);
        
        
        Label l1 = new Label();
        l1.getAppearance().setTextRenderer(new BufferedTextRenderer());
        l1.setText("Hallo! Dies ist ein \n mehrzeilen Text, der total\ntoll ist!!!");
        
        //desk.addWidget(l1);
        l1.setXY(400, 100);
        
        try 
        {
            final ITexture tex = Binding.getInstance().getTexture("data/redCross.gif");

	        final Canvas canvas = new Canvas();
	        frame.getContentContainer().addWidget(canvas);
	        canvas.setSize(100, 100);
	        canvas.setExpandable(false);
	        canvas.setShrinkable(false);
	        DecoratorAppearance pl = new DecoratorAppearance(canvas) {
	            Color background = new Color(0, 0, 0);
	            public void paintContent(Graphics g, IOpenGL gl) {
	                g.setColor(background);
	                g.drawFilledRectangle(0, 0,canvas.getWidth(), canvas.getHeight());
	                g.setColor(1, 1, 1);
	                g.drawImage(tex, 90, 90);
	                g.drawImage(tex, -10, -10);
	                
	                g.drawImage(tex, 90, -10);
	                g.drawImage(tex, -10, 90);
	                
	                g.drawImage(tex, 45, 45);
	                
	                for(int i=0;i<10;i++) {
	                    g.setColor(
	                            (float)Math.random(),
	                            (float)Math.random(),
	                            (float)Math.random());
	                    g.drawLine((int)(Math.random()*200)-50,
	                            (int)(Math.random()*200)-50,
	                            (int)(Math.random()*200)-50,
	                            (int)(Math.random()*200)-50);
	                }
	                
	            }

				public Dimension getContentMinSizeHint()
				{
					return new Dimension(20,20);
				}
	            
	        };
	        canvas.setAppearance(pl);
	        ((Container)frame.getContentContainer()).setLayoutManager(new RowLayout(false));
        } 
        catch (IOException e) 
        {
        	e.printStackTrace();
        }

        frame.setSize(200, 200);
        frame.setExpandable(false);
        frame.setShrinkable(false);
        desk.layout();
        
    }

	public void buildGUI(Display d) {
		desk = d;
		
		buildTestFrame();
		
	}

	public String getExampleName() {
		return "Clipping Example";
	}

	public String getExampleDescription() {
		return "Demonstrates that the clipping works";
	}


}
