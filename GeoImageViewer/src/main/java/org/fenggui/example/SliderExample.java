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
 * $Id: SliderExample.java 264 2007-04-16 12:34:05Z bbeaulant $
 */
package org.fenggui.example;

import org.fenggui.Display;
import org.fenggui.Label;
import org.fenggui.Slider;
import org.fenggui.composites.Window;
import org.fenggui.event.ISliderMovedListener;
import org.fenggui.event.SliderMovedEvent;
import org.fenggui.layout.Alignment;
import org.fenggui.layout.RowLayout;
import org.fenggui.util.Spacing;

/**
 * 
 * Small example app that demonstrates a Slider Widget.
 * 
 * @author Johannes Schaback ($Author: bbeaulant $)
 */
public class SliderExample implements IExample
{

	private Display desk;

	private void buildHorizontalSliderFrame()
	{

		Window f = new Window(true, false, false, true);
		desk.addWidget(f);
		f.setTitle("Horizontal Slider Test");

		f.setSize(200, 100);
		f.setX(10);
		f.setY(50);
		f.getContentContainer().setLayoutManager(new RowLayout(false));
		Slider slider = new Slider(true);
		f.getContentContainer().addWidget(slider);
		f.getContentContainer().getAppearance().setPadding(new Spacing(10, 10));

		final Label label = new Label("Please move the slider a bit! Thanks!");
		f.getContentContainer().addWidget(label);

		slider.addSliderMovedListener(new ISliderMovedListener()
		{
			public void sliderMoved(SliderMovedEvent sliderMovedEvent)
			{
				label.setText("Slider at " + (int) (sliderMovedEvent.getPosition() * 100.0) + "% !");
			}
		});

	}

	private void buildVerticalSliderFrame()
	{

		Window f = new Window(true, false, false, true);
		desk.addWidget(f);
		f.setTitle("Vertical Slider Test");

		f.setSize(200, 150);
		f.setX(50);
		f.setY(160);

		Slider slider = new Slider(false);
		f.getContentContainer().addWidget(slider);
		slider.getAppearance().setMargin(new Spacing(0, 0, 5, 0));

		f.getContentContainer().getAppearance().setPadding(new Spacing(10, 10));
		final Label label = new Label("Please move the slider a bit!");
		f.getContentContainer().addWidget(label);
		label.getAppearance().setAlignment(Alignment.MIDDLE);
		slider.addSliderMovedListener(new ISliderMovedListener()
		{
			public void sliderMoved(SliderMovedEvent sliderMovedEvent)
			{
				label.setText("Slider at " + (int) (sliderMovedEvent.getPosition() * 100.0) + "% !");
			}
		});

	}

	public void buildGUI(Display d)
	{
		desk = d;

		buildHorizontalSliderFrame();
		buildVerticalSliderFrame();

		desk.layout();
	}

	public String getExampleName()
	{
		return "Slider Example";
	}

	public String getExampleDescription()
	{
		return "Shows two frames with Sliders";
	}

}
