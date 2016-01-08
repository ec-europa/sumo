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
 * $Id: List.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.io.IOException;
import java.util.ArrayList;

import org.fenggui.event.mouse.MouseExitedEvent;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * Implementation of a vertical list of items with each item be selectable. The
 * items used in this list are <code>ListItems</code> rather than widgets.
 * 
 * 
 * 
 * @author Johannes, last edited by $Author: Schabby $, $Date: 2006/09/18
 *         12:38:46 $
 * @version $Revision: 327 $
 * @dedication The Offspring - Defy You
 */
public class List<E> extends StandardWidget
{
	private ToggableGroup<E> toggableWidgetGroup = null;
	private ArrayList<ListItem<E>> items = new ArrayList<ListItem<E>>();
	private int mouseOverRow = -1;
	private ListAppearance appearance = null;
	
	/**
	 * Creates a new list
	 * 
	 */
	public List()
	{
		this(1);
	}

	/**
	 * Creates a new List object.
	 * 
	 * @param selectionType
	 */
	public List(int selectionType)
	{
		toggableWidgetGroup = new ToggableGroup<E>(selectionType);
		
		appearance = new ListAppearance(this);
		setupTheme(List.class);
		updateMinSize();
	}


	public ToggableGroup<E> getToggableWidgetGroup()
	{
		return toggableWidgetGroup;
	}


	public void addItem(ListItem<E> li)
	{
		items.add(li);
		updateMinSize();
	}

	public void addItem(String text)
	{
		addItem(new ListItem<E>(text));
	}

	public ListItem<E> getItem(int row)
	{
		return items.get(row);
	}

	public void removeItem(int row)
	{
		removeItem(items.get(row));
	}

	public void removeItem(ListItem<E> item)
	{
		items.remove(item);
		updateMinSize();
	}
	
	public void clear()
	{
		items.clear();
		updateMinSize();
	}

	@Override
	public void mousePressed(MousePressedEvent mp)
	{
		int mouseY = mp.getDisplayY() - getDisplayY();

		int row = (getAppearance().getContentHeight() - mouseY) / getAppearance().getRowHeight();

		setSelectedIndex(row, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		if(stream.startSubcontext("listItems"))
		{
			stream.processChildren("ListItem", items, ListItem.class);
			stream.endSubcontext();
		}
		updateMinSize();
	}

	@Override
	public void mouseMoved(int displayX, int displayY)
	{
		// TODO cache getDisplayY()!!
		int mouseY = displayY - getDisplayY();

		mouseOverRow = (getAppearance().getContentHeight() - mouseY) / getAppearance().getRowHeight();
	}

	@Override
	public void mouseExited(MouseExitedEvent mouseExitedEvent)
	{
		mouseOverRow = -1;
	}

	public boolean isEmpty()
	{
		return items.isEmpty();
	}

	public int size()
	{
		return items.size();
	}

	@SuppressWarnings("unchecked")
	public void setSelectedIndex(int index, boolean selected)
	{
		if (index < 0 || index >= items.size())
			return;

		ListItem item = items.get(index);
		toggableWidgetGroup.setSelected(item, selected);
		item.setSelected(selected);
	}

	public ArrayList<ListItem<E>> getItems()
	{
		return items;
	}

	public int getMouseOverRow()
	{
		return mouseOverRow;
	}

	public ListAppearance getAppearance()
	{
		return appearance;
	}
	
	
}
