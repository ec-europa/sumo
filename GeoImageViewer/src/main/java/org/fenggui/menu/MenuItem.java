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
 * $Id: MenuItem.java 358 2007-09-21 16:03:59Z marcmenghin $
 */
package org.fenggui.menu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.fenggui.Item;
import org.fenggui.event.IMenuItemPressedListener;
import org.fenggui.event.MenuClosedEvent;
import org.fenggui.event.MenuItemPressedEvent;
import org.fenggui.render.BufferedTextRenderer;
import org.fenggui.render.Font;
import org.fenggui.render.ITextRenderer;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * Class that represents a menu item.
 * 
 * @todo Comment this class... #
 * 
 * @author Johannes Schaback, last edited by $Author: marcmenghin $, $Date: 2007-09-21 18:03:59 +0200 (Fr, 21 Sep 2007) $
 * @version $Revision: 358 $
 */
public class MenuItem extends Item
{
	private ArrayList<IMenuItemPressedListener> menuItemPressedHook = new ArrayList<IMenuItemPressedListener>();

	protected Menu menu = null;
	private boolean enabled = true;
	private ITextRenderer textRenderer = new BufferedTextRenderer();
	protected Map<String,Object> userData = new HashMap<String,Object>();
	
	/**
	 * Creates a new menu item.
	 * @param text the text displayed by this item.
	 */
	public MenuItem(String text)
	{
		this(text, true);
	}

	public MenuItem(String text, boolean enabled)
	{
		super(text);
		setText(text);
		setEnabled(enabled);
	}
	
	/**
	 * Loads the MenuItem from the stream
	 */
	public MenuItem(InputOnlyStream stream)
	throws IOException, IXMLStreamableException {
		super(null);
		process(stream);
	}
	
	/**
	 * Returns whether this item is enabled
	 * @return true if enabled, false otherwise
	 */
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * Enables or disables this menu item.  
	 * @param enabled true if item shall be enabled, false otherwise
	 */
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * @return <code>Menu</code> to which this <code>MenuItem</code> links
	 **/
	public Menu getMenu()
	{
		return menu;
	}

	/**
	 * Add a {@link IMenuItemPressedListener} to the widget. The listener can be added only once.
	 * @param l Listener
	 */
	public void addMenuItemPressedListener(IMenuItemPressedListener l)
	{
		if (!menuItemPressedHook.contains(l))
		{
			menuItemPressedHook.add(l);
		}
	}

	/**
	 * Add the {@link IMenuItemPressedListener} from the widget
	 * @param l Listener
	 */
	public void removeMenuItemPressedListener(IMenuItemPressedListener l)
	{
		menuItemPressedHook.remove(l);
	}

	/**
	 * Fire a {@link MenuClosedEvent} 
	 */
	public void fireMenuItemPressedEvent()
	{
		MenuItemPressedEvent e = new MenuItemPressedEvent(menu, this);

		for (IMenuItemPressedListener l : menuItemPressedHook)
		{
			l.menuItemPressed(e);
		}
	}

	@Override
	public void process(InputOutputStream stream)
	throws IOException, IXMLStreamableException {
		super.process(stream);
		
		stream.processAttribute("enabled", enabled, true);
		stream.processChild("Menu", menu, null, Menu.class);
		
		if(stream.isInputStream()) // XXX: only support read-in at the moment :(
		      textRenderer.setFont(stream.processChild("Font", textRenderer.getFont(), Font.getDefaultFont(), Font.class));
}

	@Override
	public String getText()
	{
		return textRenderer.getText();
	}

	@Override
	public void setText(String text)
	{
		textRenderer.setText(text);
	}

	public ITextRenderer getTextRenderer()
	{
		return textRenderer;
	}
	
	public void addUserData(String key, Object value) {
    this.userData.put(key, value);
  }

  public Object getUserData(String key) {
    return userData.get(key);
  }
	
}
