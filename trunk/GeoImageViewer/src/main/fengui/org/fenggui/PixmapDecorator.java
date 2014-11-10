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
 * Created on Jun 28, 2007
 * $Id$
 */
package org.fenggui;

import java.io.IOException;

import org.fenggui.render.Graphics;
import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;

public class PixmapDecorator implements IDecorator
{
	private boolean enabled = true;
	private String label = "";
	private Span span = Span.PADDING;
	private Pixmap pixmap = null;	
	
	public PixmapDecorator(String label, Span span, Pixmap pixmap)
	{
		super();
		this.label = label;
		this.span = span;
		this.pixmap = pixmap;
	}

	public PixmapDecorator(String label, Pixmap pixmap)
	{
		this(label, Span.PADDING, pixmap);
	}
	
	public String getLabel()
	{
		return label;
	}

	public Span getSpan()
	{
		return span;
	}

	public void setSpan(Span span)
	{
		this.span = span;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void paint(Graphics g, int localX, int localY, int width, int height)
	{
		if(pixmap == null || !enabled) return;
		
		int x  = localX + (int)((double)(width - pixmap.getWidth())/2d);
		int y  = localY + (int)((double)(height - pixmap.getHeight())/2d);
		
		g.setColor(Color.WHITE);
		g.drawImage(pixmap, x, y);
	}

	public void setEnabled(boolean enable)
	{
		enabled = enable;
	}

	public String getUniqueName()
	{
		return "PixmapDecorator";
	}

	public Pixmap getPixmap()
	{
		return pixmap;
	}

	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
	}

}
