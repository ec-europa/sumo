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
 * $Id: ListItem.java 136 2006-12-31 18:25:58Z schabby $
 */
package org.fenggui;

import org.fenggui.render.Pixmap;


/**
 * Item used in lists. It is only
 * used as a raw data item.
 * 
 * @todo Pixmap is not drawn yet #
 * 
 * @author Johannes, last edited by $Author: schabby $, $Date: 2006-12-31 19:25:58 +0100 (Sun, 31 Dec 2006) $
 * @version $Revision: 136 $
 */
public class ListItem<E> extends Item implements IToggable<E>
{
	private E value = null;
	private boolean isSelected = false;

	/**
	 * Creates a new list item.
	 * @param text the text shown in this item
	 * @param pixmap the pixmap display in this item, can be null
	 * @param value the arbitrary value associated with this item
	 */
	public ListItem(String text, Pixmap pixmap, E value) 
	{
		super(text, pixmap);
		this.value = value;
	}	
	
	public ListItem(String text, E value) 
	{
		this(text, null, value);
	}

	public ListItem(String text, Pixmap pixmap) 
	{
		this(text, pixmap, null);
	}	
	
	public ListItem(String text)
	{
		this(text, null, null);
	}
	
	public ListItem() 
	{
		this(null, null, null);
	}
	
	public boolean isSelected() 
	{
		return isSelected;
	}

	public ListItem<E> setSelected(boolean b) 
	{
		this.isSelected = b;
		return this;
	}

	public E getValue() 
	{
		return value;
	}

	public void setValue(E value) 
	{
		this.value = value;
	}	
}
