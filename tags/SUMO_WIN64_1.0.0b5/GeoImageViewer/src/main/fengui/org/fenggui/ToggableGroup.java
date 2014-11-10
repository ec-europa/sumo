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
 * $Id: ToggableGroup.java 206 2007-02-16 18:25:13Z charlierby $
 */
package org.fenggui;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.fenggui.event.ISelectionChangedListener;
import org.fenggui.event.SelectionChangedEvent;

/**
 * Manages toggable items. Instances of this class are usually used for radio buttons, within lists
 * and tables and shall be seen as read only objects, because the widgets in question handle the
 * <code>ToggableWidgetGroup</code> themself.<br/> 
 * <br/>
 * The number of selectable items can be specified either trough the constructor or
 * <code>setNumberOfSelectableItems</code>. For single selection, the correct value is 
 * <code>SINGLE_SELECTION</code>, while for arbitrary selection the value is 
 * <code>MULTIPLE_SELECTION</code>. To allow not more than three items to be selected at a time
 * the corresponding value is 3.
 * 
 * TODO Why dont we rename this class to 'Selection<E>' ? (johannes)
 * 
 * @author Johannes, last edited by $Author: charlierby $, $Date: 2007-02-16 19:25:13 +0100 (Fri, 16 Feb 2007) $
 * @version $Revision: 206 $
 * @dedication Jerry Cantrell - Chemical Tribe
 */
public class ToggableGroup<E> 
{
	public static int SINGLE_SELECTION = 1;
	public static int MULTIPLE_SELECTION = -1;
	
	private int numberOfSelectableItems = 1;
	
	//private ArrayList<IToggable<E>> members = new ArrayList<IToggable<E>>();
	private ArrayList<IToggable<E>> selected = new ArrayList<IToggable<E>>();
	private ArrayList<ISelectionChangedListener> selectionChangedHook = new ArrayList<ISelectionChangedListener>();
	
	/**
	 * Creates a new <code>ToggableWidgetGroup</code> instance.
	 * @param numberOfSelectableItems the maximal number of selected items at a time.
	 */
	public ToggableGroup(int numberOfSelectableItems) 
	{
		if(numberOfSelectableItems < -1) numberOfSelectableItems = -1;
		if(numberOfSelectableItems == 0) numberOfSelectableItems = 1;
		this.numberOfSelectableItems = numberOfSelectableItems;
	}

	/**
	 * Creates a new <code>ToggableWidgetGroup</code> instance.
	 * This group allows only single selection.
	 */
	public ToggableGroup() 
	{
		this(SINGLE_SELECTION);
	}
	
	/**
	 * Returns the number of maximal selectable items.
	 * @return number of selectable items
	 */
	public int getNumberOfSelectableItems() 
	{
		return numberOfSelectableItems;
	}

	/**
	 * Sets the number of maximal selectable items.
	 * 
	 * @param numberOfSelectableItems number of selectable items
	 */
	public void setNumberOfSelectableItems(int numberOfSelectableItems) 
	{
		this.numberOfSelectableItems = numberOfSelectableItems;
	}

	public void setSelected(IToggable<E> w, boolean b) 
	{
		// ignore deselections
		// ...
		if(b)
		{
			for(int i = selected.size() - 1; i >= numberOfSelectableItems - 1; i--)
			{
				IToggable<E> s = selected.get(i);
				s.setSelected(false);
				selected.remove(i);
				fireSelectionChangedEvent(null, s, false);
			}
			
			selected.add(0, w);
			
			fireSelectionChangedEvent(null, w, b);
		}
	}

	/**
	 * Returns the first currently selected item. Makes probably only sense
	 * for single selection.
	 * @return selected item or null if no item is selected
	 */
	public IToggable<E> getSelectedItem() 
	{
		if(selected.isEmpty()) return null;
		return selected.get(0);
	}
	
	/**
	 * Returns the value of the first selected item.
	 * @return value of item
	 */
	public E getSelectedValue() 
	{
		if(selected.isEmpty()) return null;
		return selected.get(0).getValue();
	}

	/**
	 * Fills a list with all values of the currently selected items.
	 * @param toBeFilled list to be filled
	 */
	public void getSelectedValues(java.util.List<E> toBeFilled) 
	{
		for(int i =0; i< selected.size();i++) 
		{
			IToggable<E> s = selected.get(i);
			toBeFilled.add(s.getValue());
		}
	}
	
	/**
	 * Returns an array of the values of all currently selected items. It requires
	 * the class of the values to instantiate the array.
	 * @param returnType the type of the values
	 * @return array
	 */
	@SuppressWarnings("unchecked")
	public E[] getSelectedValues(Class returnType)
	{
		E[] array = (E[]) Array.newInstance(returnType, selected.size());
		
		for(int i =0; i< selected.size();i++)
		{
			IToggable<E> s = selected.get(i);
			array[i] = s.getValue();
		}
		
		return array;
	}
	
	private void fireSelectionChangedEvent(IWidget source, IToggable t, boolean s)
	{
		SelectionChangedEvent e = new SelectionChangedEvent(source, t, s);
		
		for(ISelectionChangedListener l: selectionChangedHook)
		{
			l.selectionChanged(e);
		}
	}
	
	public void addSelectionChangedListener(ISelectionChangedListener l)
	{
		selectionChangedHook.add(l);
	}
	
	public void removeSelectionChangedListener(ISelectionChangedListener l)
	{
		selectionChangedHook.remove(l);
	}
	
}
