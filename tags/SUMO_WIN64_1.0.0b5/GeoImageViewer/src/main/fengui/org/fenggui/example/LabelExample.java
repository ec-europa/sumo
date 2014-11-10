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
 * $Id: LabelExample.java 131 2006-12-18 14:41:34Z bbeaulant $
 */

package org.fenggui.example;

import java.io.IOException;

import org.fenggui.CheckBox;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.Label;
import org.fenggui.RadioButton;
import org.fenggui.RotatedLabel;
import org.fenggui.Slider;
import org.fenggui.ToggableGroup;
import org.fenggui.composites.Window;
import org.fenggui.event.ISelectionChangedListener;
import org.fenggui.event.ISliderMovedListener;
import org.fenggui.event.SelectionChangedEvent;
import org.fenggui.event.SliderMovedEvent;
import org.fenggui.layout.Alignment;
import org.fenggui.layout.BorderLayout;
import org.fenggui.layout.BorderLayoutData;
import org.fenggui.layout.GridLayout;
import org.fenggui.render.Binding;
import org.fenggui.render.ITexture;
import org.fenggui.render.Pixmap;

/**
 * Example class for Label.
 * 
 * @author Boris Beaulant, last edited by $Author: bbeaulant $, $Date: 2006-12-18 15:41:34 +0100 (Mon, 18 Dec 2006) $
 * @version $Revision: 131 $
 */
public class LabelExample implements IExample
{

	private static final long serialVersionUID = 1L;

	private Pixmap pixmap = null;

	@SuppressWarnings("unchecked")
	private void buildLabelFrame(Display display)
	{

		Window window = new Window();
		window.setTitle(getExampleName());
		window.setX(50);
		window.setY(300);
		window.setSize(300, 200);
		window.getContentContainer().setLayoutManager(new BorderLayout());
		display.addWidget(window);

		final Label label = new Label("Label's text");
		label.setLayoutData(BorderLayoutData.CENTER);
		window.getContentContainer().addWidget(label);

		Container buttons = new Container();
		buttons.setLayoutData(BorderLayoutData.EAST);
		buttons.setLayoutManager(new GridLayout(8, 1));
		window.getContentContainer().addWidget(buttons);

		CheckBox pixmapCheckBox = new CheckBox("Pixmap");
		buttons.addWidget(pixmapCheckBox);

		CheckBox gapCheckBox = new CheckBox("Gap (10px)");
		buttons.addWidget(gapCheckBox);

		final ToggableGroup<Alignment> alignGroup = new ToggableGroup<Alignment>();

		RadioButton<Alignment> leftRadioButton = new RadioButton<Alignment>("Left", alignGroup, Alignment.LEFT);
		leftRadioButton.setSelected(true);
		buttons.addWidget(leftRadioButton);

		RadioButton<Alignment> middleRadioButton = new RadioButton<Alignment>("Middle", alignGroup, Alignment.MIDDLE);
		buttons.addWidget(middleRadioButton);

		RadioButton<Alignment> rightRadioButton = new RadioButton<Alignment>("Right", alignGroup, Alignment.RIGHT);
		buttons.addWidget(rightRadioButton);

		RadioButton<Alignment> topRadioButton = new RadioButton<Alignment>("Top", alignGroup, Alignment.TOP);
		buttons.addWidget(topRadioButton);

		RadioButton<Alignment> bottomRadioButton = new RadioButton<Alignment>("Bottom", alignGroup, Alignment.BOTTOM);
		buttons.addWidget(bottomRadioButton);

		RadioButton<Alignment> topLeftRadioButton = new RadioButton<Alignment>("Top Left", alignGroup,
				Alignment.TOP_LEFT);
		buttons.addWidget(topLeftRadioButton);

		pixmapCheckBox.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent selectionChangedEvent)
			{
				if (selectionChangedEvent.isSelected())
				{
					label.setPixmap(pixmap);
				}
				else
				{
					label.setPixmap(null);
				}
			}
		});

		gapCheckBox.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent selectionChangedEvent)
			{
				if (selectionChangedEvent.isSelected())
				{
					label.getAppearance().setGap(10);
				}
				else
				{
					label.getAppearance().setGap(0);
				}
			}
		});

		alignGroup.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent selectionChangedEvent)
			{
				label.getAppearance().setAlignment(alignGroup.getSelectedValue());
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void buildRotatedLabelFrame(Display display)
	{

		Window window = new Window();
		window.setTitle("RotatedLabel Exemple");
		window.setX(50);
		window.setY(50);
		window.setSize(300, 200);
		window.getContentContainer().setLayoutManager(new BorderLayout());
		display.addWidget(window);

		final RotatedLabel rotatedLabel = new RotatedLabel("RotatedLabel's text");
		rotatedLabel.setLayoutData(BorderLayoutData.CENTER);
		window.getContentContainer().addWidget(rotatedLabel);

		Container buttons = new Container();
		buttons.setLayoutData(BorderLayoutData.EAST);
		buttons.setLayoutManager(new GridLayout(8, 1));
		window.getContentContainer().addWidget(buttons);

		final Label labelAngle = new Label("Angle :");
		buttons.addWidget(labelAngle);

		Slider angleSlider = new Slider(true);
		angleSlider.setValue(0.5);
		buttons.addWidget(angleSlider);

		final ToggableGroup<Alignment> alignGroup = new ToggableGroup<Alignment>();

		RadioButton<Alignment> leftRadioButton = new RadioButton<Alignment>("Left", alignGroup, Alignment.LEFT);
		leftRadioButton.setSelected(true);
		buttons.addWidget(leftRadioButton);

		RadioButton<Alignment> middleRadioButton = new RadioButton<Alignment>("Middle", alignGroup, Alignment.MIDDLE);
		buttons.addWidget(middleRadioButton);

		RadioButton<Alignment> rightRadioButton = new RadioButton<Alignment>("Right", alignGroup, Alignment.RIGHT);
		buttons.addWidget(rightRadioButton);

		RadioButton<Alignment> topRadioButton = new RadioButton<Alignment>("Top", alignGroup, Alignment.TOP);
		buttons.addWidget(topRadioButton);

		RadioButton<Alignment> bottomRadioButton = new RadioButton<Alignment>("Bottom", alignGroup, Alignment.BOTTOM);
		buttons.addWidget(bottomRadioButton);

		RadioButton<Alignment> topLeftRadioButton = new RadioButton<Alignment>("Top Left", alignGroup,
				Alignment.TOP_LEFT);
		buttons.addWidget(topLeftRadioButton);

		angleSlider.addSliderMovedListener(new ISliderMovedListener()
		{
			public void sliderMoved(SliderMovedEvent sliderMovedEvent)
			{
				float angle = (float) (sliderMovedEvent.getPosition() - 0.5) * 180;
				labelAngle.setText("Angle : " + angle);
				rotatedLabel.setAngle(angle);
			}
		});

		alignGroup.addSelectionChangedListener(new ISelectionChangedListener()
		{

			public void selectionChanged(SelectionChangedEvent selectionChangedEvent)
			{
				rotatedLabel.getAppearance().setAlignment(alignGroup.getSelectedValue());
			}
		});
	}

	public void buildGUI(Display display)
	{
		try
		{
			ITexture texture = Binding.getInstance().getTexture("data/redCross.gif");
			pixmap = new Pixmap(texture, 1, 0, 14, 15);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		buildLabelFrame(display);
		buildRotatedLabelFrame(display);

		display.layout();
	}

	/* (non-Javadoc)
	 * @see org.fenggui.example.IExample#getExampleDescription()
	 */
	public String getExampleDescription()
	{
		return "Label Example";
	}

	/* (non-Javadoc)
	 * @see org.fenggui.example.IExample#getExampleName()
	 */
	public String getExampleName()
	{
		return "Label Example";
	}

}
