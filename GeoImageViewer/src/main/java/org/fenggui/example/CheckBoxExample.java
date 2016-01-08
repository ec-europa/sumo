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
 * $Id: CheckBoxExample.java 264 2007-04-16 12:34:05Z bbeaulant $
 */
package org.fenggui.example;

import org.fenggui.Button;
import org.fenggui.CheckBox;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.Label;
import org.fenggui.composites.Window;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.event.ISelectionChangedListener;
import org.fenggui.event.SelectionChangedEvent;
import org.fenggui.layout.RowLayout;
import org.fenggui.util.Spacing;

/**
 * Example for testing the <code>CheckBox</code> Widget.
 * 
 * @todo Comment this class... #
 * 
 * @author Johannes Schaback, last edited by $Author: bbeaulant $, $Date: 2007-04-16 14:34:05 +0200 (Mo, 16 Apr 2007) $
 * @version $Revision: 264 $
 */
public class CheckBoxExample implements IExample
{

	Window frame = null;
	Display desk;

	private void buildFrame()
	{

		frame = new Window(true, false, false, true);
		desk.addWidget(frame);
		frame.setX(50);
		frame.setY(50);
		frame.setTitle("Fun With Check Boxes");

		((Container)frame.getContentContainer()).setLayoutManager(new RowLayout(false));
		((Container)frame.getContentContainer()).getAppearance().setPadding(new Spacing(0, 5));

		Button b = new Button("Book Flight!");
		b.setSizeToMinSize();
		b.setExpandable(false);
		frame.getContentContainer().addWidget(b);

		final CheckBox<Integer> berlin = new CheckBox<Integer>("London");
		berlin.setValue(133);
		frame.getContentContainer().addWidget(berlin);

		final CheckBox<Integer> paris = new CheckBox<Integer>("Paris");
		paris.setValue(431);
		frame.getContentContainer().addWidget(paris);

		final CheckBox<Integer> london = new CheckBox<Integer>("Berlin");
		london.setValue(234);
		frame.getContentContainer().addWidget(london);

		final CheckBox<Integer> newYork = new CheckBox<Integer>("New York");
		newYork.setValue(1022);
		frame.getContentContainer().addWidget(newYork);

		final CheckBox<Integer> munich = new CheckBox<Integer>("Munich");
		munich.setValue(200);
		frame.getContentContainer().addWidget(munich);

		final CheckBox<Integer> sydney = new CheckBox<Integer>("Sydney");
		sydney.setValue(2134);
		frame.getContentContainer().addWidget(sydney);

		final CheckBox<Integer> sanFrancisco = new CheckBox<Integer>("San Francisco");
		sanFrancisco.setValue(1523);
		frame.getContentContainer().addWidget(sanFrancisco);

		final Label label = new Label("Please select the cities you want to visit!");
		label.getAppearance().setMargin(new Spacing(10, 10));
		frame.getContentContainer().addWidget(label);

		berlin.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent selectionChangedEvent)
			{
				if (selectionChangedEvent.isSelected()) label.setText("A good choice!!!");
				else label.setText("A bad choice!!!");
			}
		});

		b.addButtonPressedListener(new IButtonPressedListener()
		{
			public void buttonPressed(ButtonPressedEvent e)
			{
				int sum = 0;

				if (london.isSelected()) sum += london.getValue();
				if (paris.isSelected()) sum += paris.getValue();
				if (berlin.isSelected()) sum += berlin.getValue();
				if (newYork.isSelected()) sum += newYork.getValue();
				if (munich.isSelected()) sum += munich.getValue();
				if (sydney.isSelected()) sum += sydney.getValue();
				if (sanFrancisco.isSelected()) sum += sanFrancisco.getValue();

				label.setText("Price: $" + sum);
			}
		});

		frame.pack();
	}

	public void buildGUI(Display f)
	{
		desk = f;

		buildFrame();
	}

	public String getExampleName()
	{
		return "CheckBox Exmaple";
	}

	public String getExampleDescription()
	{
		return "CheckBoxes are so handy!";
	}

}
