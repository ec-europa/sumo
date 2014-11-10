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
 * $Id: WindowClosedEvent.java 56 2006-10-09 16:14:49Z schabby $
 */
package org.fenggui.event;

import org.fenggui.composites.Window;

/**
 * Class that represents the event of closing a window.
 * 
 * @author Schabby, last edited by $Author: schabby $, $Date: 2006-10-09 18:14:49 +0200 (Mon, 09 Oct 2006) $
 * @version $Revision: 56 $
 */
public class WindowClosedEvent extends Event
{

	private Window window = null;
	
	public WindowClosedEvent(Window w) 
	{
		super(w);
		window = w;
	}

	public Window getWindow() {
		return window;
	}
	
}
