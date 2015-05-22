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
 * Created on Apr 30, 2005
 * $Id: FunnyBackground.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.background;

import java.io.IOException;

import org.fenggui.render.Graphics;
import org.fenggui.theme.xml.DefaultElementName;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;


/**
 * Draws a background used for debugging purposes. This class
 * will vanish soon.
 * 
 * @author Johannes Schaback ($Author: Schabby $)
 */
@DefaultElementName("FunnyBackground")
public class FunnyBackground extends Background
{

	public FunnyBackground()
	{
	}


	public FunnyBackground(InputOnlyStream stream) throws IOException, IXMLStreamableException
	{
		process(stream);
	}


	public void paint(Graphics g, int localX, int localY, int width, int height)
	{
		g.setColor(Color.RED);
		g.drawWireRectangle(localX, localY, width, height);
		g.setColor(Color.BLUE);
		g.drawWireRectangle(localX + 1, localY + 1, width - 2, height - 2);
		g.setColor(Color.YELLOW);
		g.drawWireRectangle(localX + 2,  localY + 2, width - 4, height - 4);
	}


	/* (non-Javadoc)
	 * @see org.fenggui.io.IOStreamSaveable#process(org.fenggui.io.InputOutputStream)
	 */
	public void process(InputOutputStream stream) {
		// Nothing to save
	}
}
