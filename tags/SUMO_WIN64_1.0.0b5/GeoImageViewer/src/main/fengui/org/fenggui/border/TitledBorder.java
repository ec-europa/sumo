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
 * $Id: TitledBorder.java 161 2007-01-28 19:01:39Z schabby $
 */
package org.fenggui.border;

import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.util.Color;

/**
 * Border that displays a line of text at the top.
 *  
 * @author Johannes Schaback, last edited by $Author: schabby $, $Date: 2007-01-28 20:01:39 +0100 (Sun, 28 Jan 2007) $
 * @version $Revision: 161 $
 */
public class TitledBorder extends Border {

	private String title = "";
	private Color color = Color.GRAY, textColor = Color.BLACK;
	private Font font = null;
	
	/**
	 * Constructs a new <code>TitledBorder</code> without text.
	 *
	 */
	public TitledBorder()
	{
		this(Font.getDefaultFont(), "");
	}

	public TitledBorder(String title)
	{
		this(Font.getDefaultFont(), title, Color.BLACK);
	}

	public TitledBorder(Font font, String title)
	{
		this(font, title, Color.BLACK);
	}


	public TitledBorder(Font font, String title, Color textColor)
	{
		super(font.getHeight(), 1, 1, 1);
		this.font = font;
		setTitle(title);
		this.textColor = textColor;
	}


	public Color getColor()
	{
		return color;
	}


	public void setColor(Color color)
	{
		this.color = color;
	}


	public String getTitle()
	{
		return title;
	}


	public void setTitle(String title)
	{
		this.title = title;
	}

	public Font getFont()
	{
		return font;
	}

	public Color getTextColor()
	{
		return textColor;
	}


	public void setTextColor(Color textColor)
	{
		this.textColor = textColor;
	}


	@Override
	public void paint(Graphics g, int localX, int localY, int width, int height)
	{
		g.setColor(textColor);
		g.setFont(font);
		g.drawString(title, localX + 10, localY + height - font.getHeight());
		
		g.setColor(color);

		final int OFFSET = 2;
		
		// left
		g.setLineWidth(1);
		g.drawLine(
			localX, localY, 
			localX, localY + height - font.getHeight()/2-OFFSET);

		// right
		g.drawLine(
			localX + width - getRight(), localY,
			localX + width - getRight(), localY + height - font.getHeight()/2-OFFSET + 1);

		// top
		g.drawLine(
			localX, localY + height - font.getHeight()/2-OFFSET, 
			localX + 5, localY + height - font.getHeight()/2-OFFSET);
		
		g.drawLine(
			localX + font.getWidth(title) + 15, localY + height - font.getHeight()/2-OFFSET, 
			localX + getLeft() + width, localY + height - font.getHeight()/2-OFFSET);
		
		// bottom
		g.drawLine(
			localX, localY, 
			getLeft() + width + getRight(), localY);

		// set line width back to default
		g.setLineWidth(1);
	}

}
