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
 * $Id: ButtonExample.java 264 2007-04-16 12:34:05Z bbeaulant $
 */
package org.fenggui.example;

import org.fenggui.Button;
import org.fenggui.CheckBox;
import org.fenggui.Display;
import org.fenggui.Label;
import org.fenggui.RadioButton;
import org.fenggui.TextEditor;
import org.fenggui.ToggableGroup;
import org.fenggui.composites.Window;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.event.IWindowClosedListener;
import org.fenggui.event.WindowClosedEvent;
import org.fenggui.event.mouse.IMousePressedListener;
import org.fenggui.event.mouse.IMouseReleasedListener;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.event.mouse.MouseReleasedEvent;

public class ButtonExample implements IExample {

	Display display = null;



	@SuppressWarnings("unchecked")
	public void buildGUI(Display d) {
		display = d;
		
		final Label l = new Label("This is a plain label.");
		l.setX(50);
		l.setY(10);
		l.setSizeToMinSize();
		display.addWidget(l);
		
		Button b = new Button("This is a simple button");
		b.setX(50);
		b.setY(30);		
		b.setSizeToMinSize();

		display.addWidget(b);
		
		CheckBox cb = new CheckBox("Here we got a check box");
		cb.setX(50);
		cb.setY(60);
		cb.setSizeToMinSize();
		display.addWidget(cb);
		
		Window w = new Window(true, false, false, true);
		w.setTitle("Test!!");
		w.setX(150);
		w.setY(200);
		w.setSize(200, 200);
		
		RadioButton rb = new RadioButton("This is a radio button", new ToggableGroup());
		rb.setX(50);
		rb.setY(90);
		rb.setSizeToMinSize();
		display.addWidget(rb);
		
		TextEditor textArea = new TextEditor();
		textArea.setXY(300, 100);
		textArea.setText("asd gadyugadgaysjd guasg dasd gasd\n" +
				"a usdyags dags duyagsudy gasuyd gasug duyasdg \n" +
				"a kdaks dasjkg djhas gjhas dgjhas gdjhas gash d\n" +
				"ajs gdjhdask gdasj gjd gas gdjasd asj gasjh das hd \n" +
				"as dajjh s jhas jash dghas gdhjasg djhasg das d");
		textArea.setSizeToMinSize();
		display.addWidget(textArea);
		
		display.layout();
		
		b.addButtonPressedListener(new IButtonPressedListener() {

			public void buttonPressed(ButtonPressedEvent e)
			{
				System.out.println("Button Pressed");
			}});

		b.addMouseReleasedListener(new IMouseReleasedListener() {

			public void mouseReleased(MouseReleasedEvent mouseReleasedEvent)
			{
				System.out.println("Mouse Released");
			}});
		
		b.addMousePressedListener(new IMousePressedListener() {

			public void mousePressed(MousePressedEvent mousePressedEvent)
			{
				System.out.println("Mouse Pressed");
			}});
	}

	public String getExampleName() {
		return "Button Example";
	}

	public String getExampleDescription() {
		return "Shows some buttons and other stuff";
	}	
	
}
