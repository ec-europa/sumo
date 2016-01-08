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
 * Created on Dec 7, 2006
 * $Id: ComboBoxAppearance.java 161 2007-01-28 19:01:39Z schabby $
 */
package org.fenggui;

import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.render.Pixmap;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;

public class ComboBoxAppearance extends DecoratorAppearance
{
	private ComboBox box = null;
	
	public ComboBoxAppearance(ComboBox w)
	{
		super(w);
		box = w;
	}

	@Override
	public Dimension getContentMinSizeHint()
	{
		int pixmapWidth=0, pixmapHeight = 0;
		
		if(box.getPixmap() != null)
		{
			pixmapHeight = box.getPixmap().getHeight();
			pixmapWidth  = box.getPixmap().getWidth();
		}
		
		Dimension d = new Dimension(
			box.getLabel().getMinWidth() + pixmapWidth,
			Math.max(box.getLabel().getMinHeight(), pixmapHeight));
		
		return d;
	}

	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		Label label= box.getLabel();
		Pixmap pixmap = box.getPixmap();
		
		g.translate(label.getX(), label.getY());
		label.paint(g);
		g.translate(-label.getX(), -label.getY());
		
		if(pixmap != null)
		{
			g.setColor(Color.WHITE);
			g.drawImage(
				pixmap, getContentWidth()-pixmap.getWidth(), 
				getContentHeight()/2 - pixmap.getHeight()/2);
		}
	}

	
}
