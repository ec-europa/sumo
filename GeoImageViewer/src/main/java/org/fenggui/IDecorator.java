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
 * Created on Dec 6, 2006
 * $Id: IDecorator.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import org.fenggui.render.Graphics;
import org.fenggui.theme.xml.IXMLStreamable;

/**
 * Decorators are graphics routines to beautify widgets, such as borders and backgrounds.
 * Decorators can be enabled and disabled which means that they are drawn or not. They
 * can also share the same label (state label) for a widget such that they get enabled and
 * disabled collectively. This way, widgets can disabled and enable groups of decorators
 * in their behavior routines (e.g. for mouse hover effects, or to draw a button differently
 * when pressed).
 *  
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public interface IDecorator extends IXMLStreamable
{
	public boolean isEnabled();
	
	public String getLabel();
	public Span getSpan();
	
	public void paint(Graphics g, int localX, int localY, int width, int height);

	public void setEnabled(boolean enable);
	
}
