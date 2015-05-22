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
 * $Id: TabItemLabel.java 337 2007-08-12 12:40:32Z Schabby $
 */
package org.fenggui;

import org.fenggui.event.FocusEvent;
import org.fenggui.event.IFocusListener;
import org.fenggui.event.IKeyPressedListener;
import org.fenggui.event.Key;
import org.fenggui.event.KeyPressedEvent;
import org.fenggui.event.mouse.IMouseEnteredListener;
import org.fenggui.event.mouse.IMouseExitedListener;
import org.fenggui.event.mouse.IMousePressedListener;
import org.fenggui.event.mouse.MouseButton;
import org.fenggui.event.mouse.MouseEnteredEvent;
import org.fenggui.event.mouse.MouseExitedEvent;
import org.fenggui.event.mouse.MousePressedEvent;


/**
 * Special label used for tabs in <code>TabContainer</code>s. It is a special widget
 * so that it can be adjusted through themes.
 * 
 * @author Johannes Schaback aka Schabby, last edited by $Author: Schabby $, $Date: 2007-08-12 14:40:32 +0200 (So, 12 Aug 2007) $
 * @version $Revision: 337 $
 */
public class TabItemLabel extends ObservableLabelWidget
{
	private TabContainer tabContainer = null;
	
	public static final String LABEL_DEFAULT = "default";
	public static final String LABEL_MOUSE_HOVER = "mouseHover";
	public static final String LABEL_FOCUSED = "active";
	
	public TabItemLabel(TabContainer tabContainer)
	{
		this.tabContainer = tabContainer;
		
		setupTheme(TabItemLabel.class);
		buildBehavior();
		setTraversable(true);
		
		getAppearance().setEnabled(LABEL_DEFAULT, true);
		getAppearance().setEnabled(LABEL_FOCUSED, false);
		getAppearance().setEnabled(LABEL_MOUSE_HOVER, false);
		
	}

	void buildBehavior()
	{
		final TabItemLabel THIZZ = this;
		
		addMouseEnteredListener(new IMouseEnteredListener() 
		{

			public void mouseEntered(MouseEnteredEvent mouseEnteredEvent)
			{
				getAppearance().setEnabled(LABEL_MOUSE_HOVER, true);
			}
		
		});
		
		addMouseExitedListener(new IMouseExitedListener() {

			public void mouseExited(MouseExitedEvent mouseExited)
			{
				getAppearance().setEnabled(LABEL_MOUSE_HOVER, false);
				/*
				if(!THIZZ.equals(tabContainer.getSelectedTabLabel()))
					getAppearance().setEnabled(LABEL_ACTIVE, false);
					*/
			}
			
		});
		
		addMousePressedListener(new IMousePressedListener() {
			public void mousePressed(MousePressedEvent mousePressedEvent)
			{
				if(mousePressedEvent.getButton() == MouseButton.LEFT)
					tabContainer.selectTab(THIZZ);
				else
					if(getDisplay() != null)
						getDisplay().setFocusedWidget(null);
			}
		});
		
		addKeyPressedListener(new IKeyPressedListener(){

			public void keyPressed(KeyPressedEvent k)
			{
				if(k.getKey() == ' ' || k.getKeyClass() == Key.ENTER)
				{
					tabContainer.selectTab(THIZZ);
					getDisplay().setFocusedWidget(tabContainer.getSelectedTabWidget());
				}
			}});
		
		addFocusListener(new IFocusListener(){

			public void focusChanged(FocusEvent f)
			{
				if(f.isFocusGained())
				{
					getAppearance().setEnabled(LABEL_FOCUSED, true);
				}
				else
				{
					if(!THIZZ.equals(tabContainer.getSelectedTabLabel()))
						getAppearance().setEnabled(LABEL_FOCUSED, false);
				}
			}});
	}
	
}
