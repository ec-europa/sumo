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
 * $Id: CheckBox.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.io.IOException;
import java.util.ArrayList;

import org.fenggui.event.ActivationEvent;
import org.fenggui.event.IActivationListener;
import org.fenggui.event.ISelectionChangedListener;
import org.fenggui.event.SelectionChangedEvent;
import org.fenggui.event.mouse.IMousePressedListener;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * Implementation of a check box. A check box can be used to toggle
 * between two states.  
 * <br/>
 * <br/>
 * It is currently not supported to disable the check box.
 * 
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @dedication NOFX - Lower
 * @version $Revision: 327 $
 */
public class CheckBox<E> extends ObservableLabelWidget implements IToggable<E> 
{
	private ArrayList<ISelectionChangedListener> selectionChangedHook = new ArrayList<ISelectionChangedListener>();
	
    private E value = null;
    private boolean selected = false;
    
    public static final String LABEL_SELECTED = "selected";
    public static final String LABEL_DISABLED = "disabled";
    public static final String LABEL_DEFAULT = "default";
    
    /**
     * Creates a new <code>CheckBox</code> widget.
     */
    public CheckBox() 
    {
    	this("");
    }    

    void buildLogic()
    {
    	addMousePressedListener(new IMousePressedListener() {

			public void mousePressed(MousePressedEvent mousePressedEvent)
			{
				setSelected(!isSelected());
			}
    		
    	});
		
    	addActivationListener(new IActivationListener()
    		{
				public void widgetActivationChanged(ActivationEvent e)
				{
					if(e.isEnabled()) 
					{
						if(selected) getAppearance().setEnabled(LABEL_SELECTED, true);
						else getAppearance().setEnabled(LABEL_SELECTED, false);
					}
					else 
					{
						getAppearance().setEnabled(LABEL_DISABLED, true);
						getAppearance().setEnabled(LABEL_DEFAULT, false);
					}
				}
    		
    	});
    	
    	
    	
    }
    
    /**
     *     
     * Creates a new <code>CheckBox</code> widget.
     * @param text the text displayed next to the check box
     */
    public CheckBox(String text) 
    {
        super();
        
        
        buildLogic();
        
        setupTheme(CheckBox.class);
        getAppearance().setEnabled(LABEL_DEFAULT, true);
        getAppearance().setEnabled(LABEL_DISABLED, false);
        getAppearance().setEnabled(LABEL_SELECTED, false);
        
        setText(text); // does an updateMinSize()
        
    }
    
    /**
	 * Returns whether the check box is selected or not
	 * @return true if selected, false otherwise
	 */
	public boolean isSelected() 
	{
		return selected;
	}

	/**
	 * Selects or deselects this check box manually.
	 */
	public IToggable setSelected(boolean b) 
	{
		getAppearance().setEnabled(LABEL_DEFAULT, true);
		
		fireSelectionChangedEvent(this, this, b);
		
		if(b) getAppearance().setEnabled(LABEL_SELECTED, true);
		else getAppearance().setEnabled(LABEL_SELECTED, false);
		
		selected = b;
		
		return this;
	}

    /**
     * Returns the value associated with this check box.
     * @return value
     */
	public E getValue() 
	{
		return value;
	}

	/**
	 * Sets the associated value for this check box.
	 * @param value value
	 */
	public void setValue(E value) 
	{
		this.value = value;
	}

	/**
	 * Add a {@link ISelectionChangedListener} to the widget. The listener can be added only once.
	 * @param l Listener
	 */
	public void addSelectionChangedListener(ISelectionChangedListener l)
	{
		if (!selectionChangedHook.contains(l))
		{
			selectionChangedHook.add(l);
		}
	}
	
	/**
	 * Add the {@link ISelectionChangedListener} from the widget
	 * @param l Listener
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener l)
	{
		selectionChangedHook.remove(l);
	}
	
	/**
	 * Fire a {@link SelectionChangedEvent} 
	 * @param source
	 * @param t
	 * @param s
	 */
	private void fireSelectionChangedEvent(IWidget source, IToggable t, boolean s)
	{
		SelectionChangedEvent e = new SelectionChangedEvent(source, t, s);
		
		for(ISelectionChangedListener l: selectionChangedHook)
		{
			l.selectionChanged(e);
		}
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		selected = stream.processAttribute("selected", selected, false);
	}
	
}
