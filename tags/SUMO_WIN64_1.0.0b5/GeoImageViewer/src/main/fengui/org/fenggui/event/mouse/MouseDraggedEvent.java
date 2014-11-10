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
 * Created on 2005-4-10
 * $Id: MouseDraggedEvent.java 114 2006-12-12 22:06:22Z schabby $
 */
package org.fenggui.event.mouse;

import org.fenggui.IWidget;


/**
 * 
 * Event type used when the user dragged the mouse of
 * a Widget.
 * 
 * @author Johannes Schaback ($Author: schabby $)
 */
public class MouseDraggedEvent extends MouseEvent
{
    
    private int displayX, displayY;
    private MouseButton mouseButton = MouseButton.LEFT;
    
    public MouseDraggedEvent(IWidget source, int x, int y, MouseButton mouseButton) 
    {
    	super(source);
        this.displayX = x;
        this.displayY = y;
        this.mouseButton = mouseButton;
    }

    public MouseButton getButton() {
        return mouseButton;
    }

    public int getDisplayX() {
        return displayX;
    }

    public int getDisplayY() {
        return displayY;
    }
    
  
    
}
