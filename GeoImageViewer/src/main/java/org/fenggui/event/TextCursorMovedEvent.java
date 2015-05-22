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
 * $Id: KeyEvent.java 47 2006-10-08 23:00:10Z schabby $
 */
package org.fenggui.event;

import org.fenggui.Widget;

public class TextCursorMovedEvent extends Event
{


	// Constants defining direction of move.
	public static final int DOWN = 0;
	public static final int UP = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;

	private int direction;
	private int newIndex;
	private boolean select;

	public TextCursorMovedEvent(Widget source, int direction, int newIndex, boolean select)
	{
		super(source);
		this.direction = direction;
		this.newIndex = newIndex;
		this.select = select;
	}

	public int getDirection()
	{
		return direction;
	}

	public int getNewIndex()
	{
		return newIndex;
	}

	public boolean isSelect() {
		return select;
	}

}
