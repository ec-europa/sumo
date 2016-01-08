/*
 * FengGUI - Java GUIs in OpenGL (http://www.fenggui.org)
 * 
 * Copyright (C) 2005, 2006, 2007 FengGUI Project
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
 * Created on Jan 30, 2007
 * $Id: PixmapCellRenderer.java $
 */
package org.fenggui.table;

import org.fenggui.render.Graphics;
import org.fenggui.render.Pixmap;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;

/**
 * @author Rainer Angermann
 * 
 */
public class PixmapCellRenderer implements ICellRenderer
{
	private boolean scalePixmaps = true;

	public void paint(Graphics g, Object value, int x, int y, int width, int height)
	{
		if(!(value instanceof Pixmap))
			return;
		
		Pixmap pixmap = (Pixmap)value;
		
		g.setColor(Color.WHITE);
		
		if(scalePixmaps)
			g.drawScaledImage(pixmap, x, y, width, height);
		else
			g.drawImage(pixmap, x, y);
	}

	public void setScalePixmaps(boolean scalePixmaps)
	{
		this.scalePixmaps = scalePixmaps;
	}

	public Dimension getCellContentSize(Object value)
	{
		if(value == null)
			return null;
		
		if(!(value instanceof Pixmap))
			return null;
		
		Pixmap pixmap = (Pixmap)value;
		
		return new Dimension(pixmap.getWidth(), pixmap.getHeight());
	}
}
