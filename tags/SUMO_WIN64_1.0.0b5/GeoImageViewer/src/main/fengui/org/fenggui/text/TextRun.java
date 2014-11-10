/*
 * FengGUI - Java GUIs in OpenGL (http://fenggui.sf.net)
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
 * Created on 2 oct. 06
 * $Id: TextRun.java 334 2007-08-12 12:06:38Z Schabby $
 */

package org.fenggui.text;

import java.util.ArrayList;

import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.util.CharacterPixmap;
import org.fenggui.util.Dimension;
import org.fenggui.util.Point;
import org.fenggui.util.Rectangle;

/**
 * A TextRun represents some text with a specified TextStyle.  TextRuns
 * know how to word-wrap and how to render themselves onto Graphics objects.
 * 
 * @author Boris Beaulant, last edited by $Author: Schabby $, $Date: 2007-08-12 14:06:38 +0200 (So, 12 Aug 2007) $
 * @version $Revision: 334 $
 */
public class TextRun
{
	private final char[] chars;
	private final TextStyle style;

	private final Rectangle boundingRect = new Rectangle(0, 0, 0, 0);
	private final ArrayList<Substring> substrings;
	private boolean newLineFixed;


	/**
	 * TextRun constructor
	 * 
	 * @param chars The char array representation of the run text
	 * @param style The associated <code>TextStyle</code>
	 */
	public TextRun(char[] chars, TextStyle style)
	{
		super();
		this.chars = chars;
		this.style = style;

		substrings = new ArrayList<Substring>();
	}


	/**
	 * TextRun constructor
	 * 
	 * @param text The <code>String</code> repr�sentation of the run text
	 * @param style The associated <code>TextStyle</code>
	 */
	public TextRun(String text, TextStyle style)
	{
		this(text.toCharArray(), style);
	}


	/**
	 * @return The boundingRect of the <code>TextRun</code>
	 */
	public Rectangle getBoundingRect()
	{
		return boundingRect;
	}


	/**
	 * @return The TextRun's chars array
	 */
	public char[] getChars()
	{
		return chars;
	}


	/**
	 * @return The style's font
	 */
	public Font getFont()
	{
		return style.getFont();
	}


	/**
	 * Check if this TextRun contains the specified point.
	 * 
	 * @param x The x coordinate to find in the <code>TextView</code> coordinates system
	 * @param y The y coordinate to find in the <code>TextView</code> coordinates system
	 * @return <code>true</code> if the point (x, y) if orver this run,
	 *         <code>false</code> else.
	 */
	public boolean contains(int x, int y)
	{
		if (boundingRect.contains(x, y))
		{
			Substring target = null;
			for (Substring substring : substrings)
			{
				if (x >= substring.xOff && y >= substring.yOff)
				{
					target = substring;
				}
			}

			if (target != null && x < (target.xOff + getFont().getWidth(chars, target.begin, target.end))) { return true; }

		}
		return false;
	}


	/**
	 * Paint the TextRun
	 * 
	 * @param g
	 * @param xOff
	 * @param yOff
	 */
	void paint(Graphics g, int xOff, int yOff)
	{
		IOpenGL gl = g.getOpenGL();
		Font font = getFont();

		if (chars.length == 0) { return; }

		// Preload font outside glBegin and glEnd
		font.getCharPixMap('a').getTexture().bind();

		gl.enableTexture2D(true);
		gl.setTexEnvModeModulate();
		g.setColor(style.getColor());
		gl.startQuads();

		for (Substring substring : substrings)
		{
			int begin = substring.begin;
			int end = substring.end;

			int x = xOff + substring.xOff;
			int y = yOff + substring.yOff;
			CharacterPixmap charMap;

			for (int i = begin; i < end; i++)
			{
				char character = chars[i];

				charMap = font.getCharPixMap(character);

				final int imgWidth = charMap.getWidth();
				final int imgHeight = charMap.getHeight();
				final float endY = charMap.getEndY();
				final float endX = charMap.getEndX();
				final float startX = charMap.getStartX();
				final float startY = charMap.getStartY();

				gl.texCoord(startX, endY);
				gl.vertex(x, y);

				gl.texCoord(startX, startY);
				gl.vertex(x, imgHeight + y);

				gl.texCoord(endX, startY);
				gl.vertex(imgWidth + x, imgHeight + y);

				gl.texCoord(endX, endY);
				gl.vertex(imgWidth + x, y);

				x += charMap.getCharWidth();
			}
		}

		gl.end();
		gl.enableTexture2D(false);
	}


	/**
	 * Prepares this run of text for word-wrapped stylized display, breaking it
	 * down into discrete Substrings that can be drawn easily.
	 * 
	 * @param xMax the wrapping point (width of the view)
	 * @param scratchDimension is filled in based xMax
	 * @param point the Point contains the x, y offset that this TextRun starts
	 *            at (top left corner). The values in <code>point</code> get replaced with the Point that the next
	 *            TextRun should begin at.
	 */
	void prepare(int xMax, Dimension scratchDimension, Point point)
	{
		Font font = getFont();

		substrings.clear();

		int x = point.getX();
		int y = point.getY() - font.getHeight();
		if (xMax <= 0)
		{
			xMax = 600;
		}

		int xOff = x;
		int yOff = y;

		int height = Math.max(scratchDimension.getHeight(), -yOff);

		int begin = 0;
		int len = chars.length;
		int end = 0;

		int width = 0;
		do
		{
			if (chars[begin] == '\n')
			{
				begin++;
				if (!newLineFixed)
				{
					height += font.getHeight();
					yOff = -height;
					xOff = 0;
				}
				else
				{
					newLineFixed = false;
				}
				if (begin == chars.length)
				{
					width = 0;
					break;
				}
			}
			end = findEnd(begin, xOff, xMax);
			width = xOff + font.getWidth(chars, begin, end);

			substrings.add(new Substring(begin, end, xOff, yOff));

			xOff = 0;
			if (end != len)
			{
				height += font.getHeight();
				yOff = -height;
				begin = end;
			}
		} while (end != len);

		scratchDimension.setWidth(xMax);
		scratchDimension.setHeight(Math.max(height, scratchDimension.getHeight()));

		boundingRect.setX(0);
		boundingRect.setY(-scratchDimension.getHeight());
		boundingRect.setWidth(scratchDimension.getWidth());
		boundingRect.setHeight(point.getY() + scratchDimension.getHeight());

		point.setX(width);
		point.setY(yOff + font.getHeight()); // (+ font.getHeight() because point is the top left corner)
	}


	/**
	 * Find the index of last last character of the substring that can be drawn
	 * before having to wrap.
	 * 
	 * @param begin the offset of the first character
	 * @param xOff the X position that the substring starts at
	 * @param xMax the wrapping point
	 * @return the index of the last character that can be drawn before wrapping
	 */
	private int findEnd(int begin, int xOff, int xMax)
	{
		Font font = getFont();
		int end = begin;
		int width = xOff;
		while (end != chars.length)
		{
			char endChar = chars[end];

			// TODO: Manage unknow characters
			int endCharWidth = font.getWidth(endChar);

			if (width + endCharWidth <= xMax && (chars[end] != '\n'))
			{
				width += endCharWidth;
				end++;
			}
			else
			{
				break;
			}
		}

		if (end == chars.length) { return end; }

		if (chars[end] == '\n')
		{
			newLineFixed = true;
			return end;
		}

		// back up to the last space
		int oldEnd = end;
		while (end > begin)
		{
			if (chars[end - 1] == ' ')
			{
				return end; // here it is
			}
			else
			{
				end--;
			}
		}

		int newEnd = xOff == 0 ? oldEnd : end;

		// The new end must be higher than begin
		return newEnd != begin ? newEnd : newEnd + 1;
	}

}

/**
 * Represents a substring that is ready to be drawn. Has an x and y screen
 * offset (xOff and yOff), an offset into the chars array (begin) and length.
 */
class Substring
{
	public int begin;
	public int end;
	public int xOff;
	public int yOff;


	/**
	 * @param begin starting index into the chars array
	 * @param end endding index into the chars array
	 * @param xOff xOffset to start drawing
	 * @param yOff yOffset to start drawing
	 */
	public Substring(int begin, int end, int xOff, int yOff)
	{
		this.begin = begin;
		this.end = end;
		this.xOff = xOff;
		this.yOff = yOff;
	}
}
