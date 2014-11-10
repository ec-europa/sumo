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
 * Created on Oct 8, 2006
 * $Id: MouseWheelEvent.java 244 2007-04-02 11:58:53Z bbeaulant $
 */
package org.fenggui.event.mouse;

import org.fenggui.IWidget;

public class MouseWheelEvent extends MouseEvent
{
	private boolean up;
	private int rotation;
	private int displayX;
	private int displayY;
	
	public MouseWheelEvent(IWidget source, int displayX, int displayY, boolean up, int rotation)
	{
		super(source);
		this.up = up;
		this.rotation = rotation;
		this.displayX = displayX;
		this.displayY = displayY;
	}

	public boolean wheeledUp()
	{
		return up;
	}
	
	public int getRotations()
	{
		return rotation;
	}

	/* (non-Javadoc)
	 * @see org.fenggui.event.mouse.MouseEvent#getDisplayX()
	 */
	@Override
	public int getDisplayX() 
	{
		return displayX;
	}

	/* (non-Javadoc)
	 * @see org.fenggui.event.mouse.MouseEvent#getDisplayY()
	 */
	@Override
	public int getDisplayY() 
	{
		return displayY;
	}
	
}
