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
 * $Id: SelectionChangedEvent.java 114 2006-12-12 22:06:22Z schabby $
 */
package org.fenggui.event;

import org.fenggui.IToggable;
import org.fenggui.IWidget;

public class SelectionChangedEvent extends Event
{

    private IToggable w = null;
    private boolean st;
    
    public SelectionChangedEvent(IWidget source, IToggable wi, boolean newState) 
    {
    	super(source);
        w = wi;
        st = newState;
    }
    
	public boolean isSelected() {
        return st;
    }
    
    public IToggable getToggableWidget() {
        return w;
    }
    
}
