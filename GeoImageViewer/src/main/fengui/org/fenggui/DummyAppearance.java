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
 * $Id: DummyAppearance.java 114 2006-12-12 22:06:22Z schabby $
 */
package org.fenggui;

import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.util.Dimension;

public class DummyAppearance implements IAppearance
{

	public Dimension getMinSizeHint()
	{
		return null;
	}

	public void paint(Graphics g, IOpenGL gl)
	{
	}


}
