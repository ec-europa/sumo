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
 * $Id: IAppearance.java 255 2007-04-16 06:49:42Z schabby $
 */
package org.fenggui;

import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.util.Dimension;

/**
 * Base interface for appearance definitons of widgets.
 * 
 * @author Johannes, last edited by $Author: schabby $, $Date: 2007-04-16 08:49:42 +0200 (Mo, 16 Apr 2007) $
 * @version $Revision: 255 $
 */
public interface IAppearance
{
	/**
	 * Called on render-time to draw the widget.
	 * @param g graphics object
	 * @param gl opengl interface
	 */
	public void paint(Graphics g, IOpenGL gl);
	
	/**
	 * Computes the minimum size required by the widget according to its
	 * appearance definition.
	 * @return the min. size
	 */
	public Dimension getMinSizeHint();
	
}
