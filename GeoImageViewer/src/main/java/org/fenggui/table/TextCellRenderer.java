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
 * $Id: TextCellRenderer.java 184 2007-02-07 01:14:49Z charlierby $
 */
package org.fenggui.table;

import org.fenggui.layout.Alignment;
import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;

/**
 * Class that renders text in cells.
 * 
 * @author Johannes Schaback
 * 
 */
public class TextCellRenderer implements ICellRenderer
{
	private Font font = Font.getDefaultFont();
	private Color textColor = Color.WHITE;
	private Alignment alignment = Alignment.LEFT;
	
	public void paint(Graphics g, Object value, int x, int y, int width, int height)
	{
		String s = value.toString();
		
		s = font.confineLength(s, width - 4);

		int entryOffset = 4 + alignment.alignX(width - 4, font.getWidth(s));

		g.setFont(font);
		g.setColor(textColor);
		g.drawString(s, x + entryOffset, y);
	}

	/**
	 * @param font the font to set
	 */
	public void setFont(Font font)
	{
		this.font = font;
	}

	/**
	 * @param textColor the textColor to set
	 */
	public void setTextColor(Color textColor)
	{
		this.textColor = textColor;
	}

	public Dimension getCellContentSize(Object value)
	{
		if(value == null)
			return null;
		
		String s = value.toString();
		
		return new Dimension(font.getWidth(s), font.getHeight());
	}
}
