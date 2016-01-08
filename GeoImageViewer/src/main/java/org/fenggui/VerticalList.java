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
 * $Id: VerticalList.java 116 2006-12-12 22:46:21Z schabby $
 */
package org.fenggui;

import java.util.ArrayList;

import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.render.Font;

/**
 * List that aligns the items vertically in several columns.
 * 
 * @author Johannes, last edited by $Author: schabby $, $Date: 2006-12-12 23:46:21 +0100 (Tue, 12 Dec 2006) $
 * @version $Revision: 116 $
 */
public class VerticalList<E> extends StandardWidget implements INotLayoutableWidget
{
	private ArrayList<ListItem<E>> items = new ArrayList<ListItem<E>>();
	private ToggableGroup<E> toggableWidgetGroup = null;

	private ArrayList<Integer> columnWidth = new ArrayList<Integer>();
	private VerticalListAppearance appearance = null;
	/**
	 * Creates a new <code>VerticalList</code> object.
	 *
	 */
	public VerticalList() 
	{
		this(1);
	}
	
	public Iterable<ListItem<E>> getItems()
	{
		return items;
	}
	
	/**
	 * Creates a new <code>List</code> object.
	 * @param selectionType number of selectable items
	 */
	public VerticalList(int selectionType) 
	{
		toggableWidgetGroup = new ToggableGroup<E>(selectionType);
		appearance = new VerticalListAppearance<E>(this);
		setupTheme(VerticalList.class);
		updateMinSize();

	}
	
	public int getColumnWidth(int i)
	{
		return columnWidth.get(i);
	}
	
	public VerticalListAppearance getAppearance()
	{
		return appearance;
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

	public int getWidth(int proposedHeight)
	{
		Font font = getAppearance().getFont();
		int x = 0;
		int y = getAppearance().getContentHeight() + font.getHeight();
		
		int currentMax = 0;
		int tmp = 0;
		
		for(ListItem item: items)
		{
			tmp = font.getWidth(item.getText());
			
			if(tmp > currentMax) currentMax = tmp;
			
			y -= font.getHeight();
			
			if(y <= 0)
			{
				x += currentMax + 10;
				currentMax = 0;
				y = getAppearance().getContentHeight() - font.getHeight();
			}
		}
		
		return x + currentMax + 10;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void mousePressed(MousePressedEvent mp) 
	{
		Font font = getAppearance().getFont();
		
		int mx = mp.getDisplayX() - getDisplayX();
		int my = mp.getDisplayY() - getDisplayY();
		
		int x = 0;
		int y = getAppearance().getContentHeight() + font.getHeight();
		
		int columnCounter = 0;

		for(ListItem item: items)
		{
			if(y <= my && y + font.getHeight() >= my &&
					x <= mx && x + columnWidth.get(columnCounter) >= mx)
			{
				toggableWidgetGroup.setSelected(item, true);
				item.setSelected(true);
			}			
			
			y -= font.getHeight();
			

			
			if(y <= 0)
			{
				x += columnWidth.get(columnCounter);
				columnCounter++;
				y = getAppearance().getContentHeight() - font.getHeight();
			}
		}
	}

	
	
	@Override
	public void layout() 
	{
		Font font = getAppearance().getFont();
		int x = 0;
		int y = getAppearance().getContentHeight() + font.getHeight();
		
		int currentMax = 0;
		int tmp = 0;
		columnWidth.clear();
		for(ListItem item: items)
		{
			tmp = font.getWidth(item.getText());
			
			if(tmp > currentMax) currentMax = tmp;
			
			y -= font.getHeight();
			
			if(y <= 0)
			{
				x += currentMax + 10;
				columnWidth.add(currentMax + 10);
				currentMax = 0;
				y = getAppearance().getContentHeight() - font.getHeight();
			}
		}
		columnWidth.add(currentMax + 10);
	}

	public void heightHint(int height) 
	{
		Font font = getAppearance().getFont();
		int x = 0;
		int y = getAppearance().getContentHeight() + font.getHeight();
		
		int currentMax = 0;
		int tmp = 0;
		
		for(ListItem item: items)
		{
			tmp = font.getWidth(item.getText());
			
			if(tmp > currentMax) currentMax = tmp;
			
			y -= font.getHeight();
			
			if(y <= 0)
			{
				x += currentMax + 10;
				currentMax = 0;
				y = getAppearance().getContentHeight() - font.getHeight();
			}
		}
		
		setMinSize(x + currentMax, height);
	}

	public void widthHint(int width) 
	{
		// ignore
	}
}
