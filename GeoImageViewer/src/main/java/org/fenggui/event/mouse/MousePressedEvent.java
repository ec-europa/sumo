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
 * $Id: MousePressedEvent.java 114 2006-12-12 22:06:22Z schabby $
 */
package org.fenggui.event.mouse;

import org.fenggui.IWidget;

/**
 * Holds event information concerning pressed mouse buttons.
 * 
 * @author Johannes Schaback 
 *
 */
public class MousePressedEvent extends MouseEvent
{

    private int displayX, displayY, clickCount;
    private MouseButton mouseButton = MouseButton.LEFT;
    
    /**
     * Creates a new MousePressedEvent.
     * @param mouseX the x coordinate where the click occured
     * @param mouseY the y coordinate where the click occured
     * @param mouseButton which mouse button has been pressed
     * @param clickCount count of immidiate clicks (provided by Java) for determing double clicks
     */
    public MousePressedEvent(IWidget source, int mouseX, int mouseY, MouseButton mouseButton, int clickCount) 
    {
    	super(source);
        displayX = mouseX;
        displayY = mouseY;
        this.mouseButton = mouseButton;
        this.clickCount = clickCount;
    }

    /**
     * Returns the pressed mouse button
     * @return mouse button
     */
    public MouseButton getButton() {
        return mouseButton;
    }

    /**
     * Returns the x coordinate where the mouse button has been pressed
     * @return x coordinate
     */
    public int getDisplayX() {
        return displayX;
    }

    /**
     * Returns the y coordinate where the mouse button has been pressed
     * @return y coordinate
     */
    public int getDisplayY() {
        return displayY;
    }

    public int getClickCount() {
        return clickCount;
    }



}
