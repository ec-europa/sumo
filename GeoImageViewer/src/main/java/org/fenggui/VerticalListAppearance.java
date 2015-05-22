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
 * Created on Dec 11, 2006
 * $Id: VerticalListAppearance.java 114 2006-12-12 22:06:22Z schabby $
 */
package org.fenggui;

import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;

public class VerticalListAppearance<E> extends DecoratorAppearance
{
	private Font font = Font.getDefaultFont();
	private VerticalList<E> vl = null;
	
	public VerticalListAppearance(VerticalList<E> w)
	{
		super(w);
		vl = w;
	}

	@Override
	public Dimension getContentMinSizeHint()
	{
		return new Dimension(0, 0);
	}

	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		int x = 0;
		int y = getContentHeight() + font.getHeight();
		
		int columnCounter = 0;
		
		g.setFont(font);
		g.setColor(Color.BLACK);
		
		for(ListItem item: vl.getItems())
		{
			if(item.isSelected())
			{
				g.setColor(Color.BLUE);
				g.drawFilledRectangle(x-5, y, vl.getColumnWidth(columnCounter), font.getHeight());
				g.setColor(Color.WHITE);
				g.drawString(item.getText(), x, y);
				g.setColor(Color.BLACK);
			}
			else
			{
				g.drawString(item.getText(), x, y);
			}
			
			y -= font.getHeight();
			
			if(y <= 0)
			{
				x += vl.getColumnWidth(columnCounter);
				columnCounter++;
				y = getContentHeight() - font.getHeight();
			}
		}
	}

	public Font getFont()
	{
		return font;
	}

	public void setFont(Font font)
	{
		this.font = font;
	}

}
