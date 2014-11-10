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

import org.fenggui.Display;
import org.fenggui.Label;
import org.fenggui.SnappingSlider;
import org.fenggui.composites.Window;
import org.fenggui.event.ISliderMovedListener;
import org.fenggui.event.SliderMovedEvent;
import org.fenggui.layout.RowLayout;
import org.fenggui.util.Spacing;

/**
 * Tests the basic clipping.
 * 
 * @author Johannes Schaback ($Author: bbeaulant $)
 */
public class SnappingSliderExample implements IExample {

    private Window frame = null;
    private Display desk;

   
    private void buildTestFrame() {

        frame = new Window(true, false, false, true);
        desk.addWidget(frame);
        frame.setX(150);
        frame.setY(150);
        frame.setTitle("Snapping Slider Example");
        frame.setSize(500, 200);
        
        frame.getContentContainer().setLayoutManager(new RowLayout(false));
        
        SnappingSlider ss = new SnappingSlider(10);
        ss.setTickLabels("sucks", "aweful", "bad", "soso", "okay", "moderate", "nice", "good", "wonderful", "excellent");
        frame.getContentContainer().addWidget(ss);
        ss.getAppearance().setMargin(new Spacing(25,25));
        
        final Label l = new Label("Please drag the slider");
        l.getAppearance().setMargin(new Spacing(15,15));
        frame.getContentContainer().addWidget(l);
        
        ss.addSliderMovedListener(new ISliderMovedListener(){

			public void sliderMoved(SliderMovedEvent sliderMovedEvent)
			{
				l.setText("value: "+sliderMovedEvent.getPosition());
			}});
        
        desk.layout();
    }

	public void buildGUI(Display d) 
	{
		desk = d;
		
		buildTestFrame();
	}

	public String getExampleName() 
	{
		return "SnappingSlider Example";
	}

	public String getExampleDescription() {
		return "SnappingSlider Example";
	}


}
