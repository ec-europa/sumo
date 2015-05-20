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
 * $Id: RadioButton.java 161 2007-01-28 19:01:39Z schabby $
 */
package org.fenggui;

import java.util.ArrayList;

import org.fenggui.event.ISelectionChangedListener;
import org.fenggui.event.SelectionChangedEvent;
import org.fenggui.event.mouse.IMousePressedListener;
import org.fenggui.event.mouse.MousePressedEvent;

/**
 * Implementation of a radio button which usually represents a single option 
 * out of a set of options where only one option can be selected. 
 *
 * 
 * @author Johannes Schaback, last edited by $Author: schabby $, $Date: 2007-01-28 20:01:39 +0100 (Sun, 28 Jan 2007) $
 * @version $Revision: 161 $
 */
public class RadioButton<E> extends ObservableLabelWidget implements IToggable<E> 
{
    private ToggableGroup<E> radioButtonGroup = null;
    private E value = null;
    private boolean selected = false;
    private ArrayList<ISelectionChangedListener> selectionChangedHook = new ArrayList<ISelectionChangedListener>();

    public static final String LABEL_SELECTED = "selected";
    public static final String LABEL_DEFAULT = "default";
    public static final String LABEL_DISABLED = "disabled";

    public RadioButton(String text, ToggableGroup<E> group, E data) 
    {
        setRadioButtonGroup(group);
    	
    	setValue(data);
    	
    	buildLogic();
    	
		setupTheme(RadioButton.class);
		getAppearance().setEnabled(LABEL_DEFAULT, true);
		getAppearance().setEnabled(LABEL_DISABLED, false);
		getAppearance().setEnabled(LABEL_SELECTED, false);
		setText(text);
    }
    
    void buildLogic()
    {
    	addMousePressedListener(new IMousePressedListener(){

			public void mousePressed(MousePressedEvent mousePressedEvent)
			{
				setSelected(true);
			}});
    }
    
	public RadioButton(String text, ToggableGroup<E> group)  
    {
    	this(text, group, null);
    }
    
	public RadioButton(ToggableGroup<E> group)  
    {
    	this(null, group, null);
    }    

    public RadioButton()  
    {
    	this("", null, null);
    }  
	
    public RadioButton(String text)  
    {
    	this(text, null, null);
    }    
    
    public boolean isSelected() 
    {
        return selected;
    }
    
    private void fireSelectionChangedEvent(boolean b)
    {
    	SelectionChangedEvent e = new SelectionChangedEvent(this, this, b);
    	for(ISelectionChangedListener l: selectionChangedHook)
    	{
    		l.selectionChanged(e);
    	}
    }
    
    public RadioButton setSelected(boolean s) 
    {
    	if(s) 
    	{
    		radioButtonGroup.setSelected(this, true);
    		fireSelectionChangedEvent(s);
    		getAppearance().setEnabled(LABEL_DEFAULT, false);
    		getAppearance().setEnabled(LABEL_SELECTED, true);
    	} 
    	else 
    	{
    		getAppearance().setEnabled(LABEL_SELECTED, false);
    		getAppearance().setEnabled(LABEL_DEFAULT, true);
    		fireSelectionChangedEvent(s);
    	}
    	
    	selected = s;
        return this;
    }

    public ToggableGroup<E> getRadioButtonGroup() 
    {
        return radioButtonGroup;
    }

    public void setRadioButtonGroup(ToggableGroup<E> radioButtonGroup) 
    {
        this.radioButtonGroup = radioButtonGroup;
        if(isSelected()) radioButtonGroup.setSelected(this, isSelected());
    }
	
    public E getValue() 
    {
		return value;
	}

	public void setValue(E value) 
	{
		this.value = value;
	}	
	
	public void addSelectionChangedListener(ISelectionChangedListener l)
	{
		selectionChangedHook.add(l);
	}
	
}
