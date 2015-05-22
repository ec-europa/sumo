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
 * $Id: MouseEvent.java 114 2006-12-12 22:06:22Z schabby $
 */
package org.fenggui.event.mouse;

import org.fenggui.IWidget;
import org.fenggui.event.Event;

/**
 * Base class for mouse events. It serves to make method naming in mouse event classes
 * consistent and to provide two covenience methods used for translating the 
 * mouse coordinates from global display coordinates to widgets coordinates.
 * 
 * @author Johannes Schaback aka Schabby, last edited by $Author: schabby $, $Date: 2006-12-12 23:06:22 +0100 (Tue, 12 Dec 2006) $
 * @version $Revision: 114 $
 * @see org.fenggui.Display
 */
public abstract class MouseEvent extends Event
{
	/**
	 * Creates a new <code>MouseEvent</code> object.
	 * @param source the widget that triggered the event.
	 */
	public MouseEvent(IWidget source)
	{
		super(source);
	}

	/**
	 * Returns the x coordinate of the mouse in the <code>Display</code> coordinate system.
	 * @return x coordinate
	 */
	public abstract int getDisplayX();
	
	/**
	 * Returns the y coordinate of the mouse in the <code>Display</code> coordinate system.
	 * @return y coordinate
	 */
	public abstract int getDisplayY();
	
	/**
	 * Returns the x coordinate of the mouse in the coordinate system of the given widget.
	 * @param w the widget
	 * @return x coordinates
	 */
	public int getLocalX(IWidget w)
	{
		return getDisplayX() - w.getDisplayX();
	}
	
	/**
	 * Returns the y coordinate of the mouse in the coordinate system of the given widget.
	 * @param w the widget
	 * @return y coordinates
	 */
	public int getLocalY(IWidget w)
	{
		return getDisplayY() - w.getDisplayY();
	}

}
