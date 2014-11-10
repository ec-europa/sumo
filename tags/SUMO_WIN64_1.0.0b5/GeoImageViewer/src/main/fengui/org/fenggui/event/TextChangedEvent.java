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
 * $Id: TextChangedEvent.java 139 2007-01-08 19:42:25Z thewordnerd $
 */

package org.fenggui.event;

import org.fenggui.ITextWidget;
import org.fenggui.IWidget;

/**
 * Event type for text editor's text change events.
 *
 * @author Boris Beaulant, last edited by $Author: thewordnerd $, $Date: 2007-01-08 20:42:25 +0100 (Mon, 08 Jan 2007) $
 * @version $Revision: 139 $
 */
public class TextChangedEvent extends Event
{
	private ITextWidget trigger;
	private String text;
	
	/**
	 * TextChanged constructor
	 */
	public TextChangedEvent(ITextWidget trigger, String text) 
	{
		super((IWidget)trigger);
		this.trigger = trigger;
		this.text = text;
	}

	/**
	 * @return trigger
	 */
	public ITextWidget getTrigger() {
		return trigger;
	}


	/**
	 * @return text of change
	**/
	public String getText() {
		return text;
	}

}
