/*
 * FengGUI - Java GUIs in OpenGL (http://www.fenggui.org)
 * 
 * Copyright (C) 2005, 2006 FengGUI Project
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
 * Created on 2005-3-2
 * $Id: Button.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.io.IOException;
import java.util.ArrayList;

import org.fenggui.event.ActivationEvent;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.Event;
import org.fenggui.event.FocusEvent;
import org.fenggui.event.IActivationListener;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.event.IEventListener;
import org.fenggui.event.IFocusListener;
import org.fenggui.event.IKeyPressedListener;
import org.fenggui.event.IKeyReleasedListener;
import org.fenggui.event.Key;
import org.fenggui.event.KeyPressedEvent;
import org.fenggui.event.KeyReleasedEvent;
import org.fenggui.event.mouse.IMouseEnteredListener;
import org.fenggui.event.mouse.IMouseExitedListener;
import org.fenggui.event.mouse.IMousePressedListener;
import org.fenggui.event.mouse.IMouseReleasedListener;
import org.fenggui.event.mouse.MouseEnteredEvent;
import org.fenggui.event.mouse.MouseExitedEvent;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.event.mouse.MouseReleasedEvent;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * 
 * Implementation of a Button. The Button is one of the most basic Widegts to
 * interact with the user. This class extends Label which allows the Button to
 * easily render images as well as text.<br/> <br/> Note that toggle buttons
 * are not supported in the current implementation of Button.<br/> <br/>
 * 
 * 
 * A Button has three states: default, mouse-over and pressed.
 * 
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 * @dedication Terrorgruppe - Namen vergessen
 */
public class Button extends ObservableLabelWidget
{
	private ArrayList<IButtonPressedListener> buttonPressedHook = new ArrayList<IButtonPressedListener>();
	
	public static final String LABEL_DEFAULT = "default";
	public static final String LABEL_MOUSEHOVER = "mouseHover";
	public static final String LABEL_PRESSED = "pressed";
	public static final String LABEL_FOCUSED = "focused";
	public static final String LABEL_DISABLED = "disabled";
	
	private boolean pressed = false;
	private Button THIS;
	private IEventListener globalListener = null;
	
	public Button()
	{
		this((String)null);
	}

	public Button(String text)
	{
		setText(text);
		setSize(10, 10);
		buildMouseBehavior();
		buildKeyboardBehavior();
		setupTheme(Button.class);
		getAppearance().setEnabled(LABEL_MOUSEHOVER, false);
		getAppearance().setEnabled(LABEL_PRESSED, false);
		getAppearance().setEnabled(LABEL_DISABLED, false);
		getAppearance().setEnabled(LABEL_FOCUSED, false);
		THIS = this;
		setTraversable(true);
		updateMinSize();
	}
	
	public Button(InputOnlyStream stream) throws IOException, IXMLStreamableException
	{
		buildMouseBehavior();
		buildKeyboardBehavior();
		process(stream);
		getAppearance().setEnabled(LABEL_MOUSEHOVER, false);
		getAppearance().setEnabled(LABEL_PRESSED, false);
		getAppearance().setEnabled(LABEL_DISABLED, false);
		getAppearance().setEnabled(LABEL_FOCUSED, false);
		THIS = this;
		setTraversable(true);
		updateMinSize();
	}
	
	void buildKeyboardBehavior()
	{
		addFocusListener(new IFocusListener() {

			public void focusChanged(FocusEvent focusChangedEvent)
			{
				if(focusChangedEvent.isFocusGained())
				{
					getAppearance().setEnabled(LABEL_FOCUSED, true);
				}
				else
				{
					getAppearance().setEnabled(LABEL_FOCUSED, false);
				}
				
			}});
		
		addKeyPressedListener(new IKeyPressedListener() {

			public void keyPressed(KeyPressedEvent e)
			{
				if(e.getKey() == ' ' || e.getKeyClass() == Key.ENTER)
				{
					pressed();
				}
			}});
		
		addKeyReleasedListener(new IKeyReleasedListener() {

			public void keyReleased(KeyReleasedEvent e)
			{
				if(e.getKey() == ' ' || e.getKeyClass() == Key.ENTER)
				{
					released();
				}
			}});
		
	}
	
	private final void pressed()
	{
		getAppearance().setEnabled(LABEL_PRESSED, true);
		pressed = true;
	}
	
	private final void released()
	{
		if(pressed)
		{
			pressed = false;
			fireButtonPressedEvent();
			getAppearance().setEnabled(LABEL_PRESSED, false);
		}
	}
	
	void buildMouseBehavior()
	{
		addActivationListener(new IActivationListener() {

			public void widgetActivationChanged(ActivationEvent activationEvent)
			{
				getAppearance().setEnabled(LABEL_DISABLED, !activationEvent.isEnabled());
			}});
		
		addMouseEnteredListener(new IMouseEnteredListener()
		{
			public void mouseEntered(MouseEnteredEvent mouseEnteredEvent)
			{
				getAppearance().setEnabled(LABEL_MOUSEHOVER, true);
				//getAppearance().setEnabled(LABEL_PRESSED, pressed); (johannes) commented this out! Does this make sense here?					
				
			}
		});
		
		addMouseExitedListener(new IMouseExitedListener() {

			public void mouseExited(MouseExitedEvent mouseExited)
			{
				getAppearance().setEnabled(LABEL_DEFAULT, true);
				getAppearance().setEnabled(LABEL_MOUSEHOVER, false);
				getAppearance().setEnabled(LABEL_PRESSED, false);
			}});
		
		addMousePressedListener(new IMousePressedListener() {

			public void mousePressed(MousePressedEvent mousePressedEvent)
			{
				pressed();
			}});
		
		addMouseReleasedListener(new IMouseReleasedListener(){

			public void mouseReleased(MouseReleasedEvent mouseReleasedEvent)
			{
				released();
			}});
	}

	public boolean isPressed()
	{
		return pressed;
	}
	
	public void addButtonPressedListener(IButtonPressedListener l)
	{
		if (!buttonPressedHook.contains(l))
		{
			buttonPressedHook.add(l);
		}
	}
	
	public void removeButtonPressedListener(IButtonPressedListener l)
	{
		buttonPressedHook.remove(l);
	}
	
	private void fireButtonPressedEvent()
	{
		ButtonPressedEvent e = new ButtonPressedEvent(this);
		
		for(int i=0 ; i < buttonPressedHook.size(); i++)
		{
			IButtonPressedListener l = buttonPressedHook.get(i);
			l.buttonPressed(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.fenggui.Widget#addedToWidgetTree()
	 */
	@Override
	public void addedToWidgetTree() {
		super.addedToWidgetTree();
		if (getDisplay() != null) {
			globalListener = new IEventListener() {
				public void processEvent(Event event) {
					if (event instanceof MouseReleasedEvent) {
						MouseReleasedEvent mouseReleasedEvent = (MouseReleasedEvent) event;
						if (mouseReleasedEvent.getSource() != THIS) {
							pressed = false;
						}
					}
				}
			};
			getDisplay().addGlobalEventListener(globalListener);
		}
	}

	/* (non-Javadoc)
	 * @see org.fenggui.Widget#removedFromWidgetTree()
	 */
	@Override
	public void removedFromWidgetTree() {
		super.removedFromWidgetTree();
		if (getDisplay() != null) {
			if (globalListener != null){
				getDisplay().removeGlobalEventListener(globalListener);
			}
		}
	}
}
