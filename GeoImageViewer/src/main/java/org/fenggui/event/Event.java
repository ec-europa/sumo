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
 * $Id: Event.java 114 2006-12-12 22:06:22Z schabby $
 */
package org.fenggui.event;

import org.fenggui.IWidget;

/**
 * Base class for all events.
 * 
 * @author Johannes Schaback aka Schabby, last edited by $Author: schabby $, $Date: 2006-12-12 23:06:22 +0100 (Tue, 12 Dec 2006) $
 * @version $Revision: 114 $
 */
public class Event
{
	private IWidget source = null;
	
	/**
	 * Creates a new <code>Event</code> object.
	 * @param source the source from which the event was emitted
	 */
	public Event(IWidget source)
	{
		this.source = source;
	}
	
	/**
	 * Returns the widet that emitted the event.
	 * @return the widget
	 */
	public IWidget getSource()
	{
		return source;
	}
}
