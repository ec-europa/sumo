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
 * Created on 2005-4-12
 * $Id: ButtonPressedEvent.java 167 2007-02-01 02:16:33Z charlierby $
 */
package org.fenggui.event;

import org.fenggui.Button;

/**
 * 
 * Event type for button pressed events.
 * 
 * @author Johannes Schaback ($Author: charlierby $)
 *
 */
public class ButtonPressedEvent extends Event
{
    private Button button;
    
    /**
     */
    public ButtonPressedEvent(Button trigger) 
    {
    	super(trigger);
        button = trigger;
    }
    
    public Button getTrigger() {
        return button;
    }
    
}
