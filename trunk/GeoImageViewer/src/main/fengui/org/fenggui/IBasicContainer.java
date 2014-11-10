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
 * Created on Nov 10, 2006
 * $Id: IBasicContainer.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import org.fenggui.theme.xml.IXMLStreamable;

/**
 * Meant for widgets that contain a fixed number of other widgets and layout
 * them according to a fixed rule.
 * 
 * @author Johannes, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 * @dedication Frank Sinatra - Fly Me To The Moon
 */
public interface IBasicContainer extends IWidget, IXMLStreamable
{
	public IWidget getNextWidget(IWidget start);
	
	public IWidget getPreviousWidget(IWidget start);
	
	public IWidget getNextTraversableWidget(IWidget start);

	public IWidget getPreviousTraversableWidget(IWidget start);
	
	public void layout();

}
