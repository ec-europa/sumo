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
 * $Id: Canvas.java 161 2007-01-28 19:01:39Z schabby $
 */
package org.fenggui;

import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.util.Dimension;


/**
 * Widget for customized drawing.
 * By giving an implementation of 
 * <code>IPaintListener</code> to this class, the programmer
 * can define his own drawing routine for a Widget. Note
 * that this Widget is not state-enabled. 
 * The Canvas poses an
 * alternative to subclassing <code>Widget</code> if the programmer
 * just needs a quick way to draw some stuff in a Widget.
 * 
 * 
 * @todo Comment this class... #
 * 
 * @author Johannes Schaback, last edited by $Author: schabby $, $Date: 2007-01-28 20:01:39 +0100 (Sun, 28 Jan 2007) $
 * @version $Revision: 161 $
 */
public class Canvas extends StandardWidget {

	private DecoratorAppearance appearance = null;
	
	public Canvas()
	{
		appearance = new DefaultDectoratorAppearance(this);
		setupTheme(Canvas.class);
	}

	public DecoratorAppearance getAppearance()
	{
		return appearance;
	}

	public void setAppearance(DecoratorAppearance appearance)
	{
		this.appearance = appearance;
	}

	class DefaultDectoratorAppearance extends DecoratorAppearance
	{

		public DefaultDectoratorAppearance(IWidget w)
		{
			super(w);
		}

		@Override
		public Dimension getContentMinSizeHint()
		{
			return new Dimension(0,0);
		}

		@Override
		public void paintContent(Graphics g, IOpenGL gl)
		{
		}
		
	}
}
