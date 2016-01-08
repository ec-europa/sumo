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
 * $Id: GradientBackground.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.background;

import java.io.IOException;

import org.fenggui.render.Graphics;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;


/**
 * Background that blends four colors set at the four corners.
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 *
 * @todo Calculate interpolation for colors. Dont use stactic
 * Color array. Rather, calc Color values from angle and position.
 * Urgent. The whole index thing is just a adhoc solution.... #
 */
public class GradientBackground extends Background
{
	/**
	 * Color of the lower right corner.
	 */
	private Color lowerLeft = Color.BLUE;

	/**
	 * Color of the lower right corner. 
	 */
	private Color lowerRight = Color.RED;

	/**
	 * Color of the upper right corner.
	 */
	private Color upperRight = Color.YELLOW;

	/**
	 * Color of upper left corner.
	 */
	private Color upperLeft = Color.WHITE;


	public GradientBackground()
	{
		this(Color.DARK_GRAY, Color.LIGHT_GRAY);
	}


	/**
	 * Creates a Blenging Background
	 * @param top color of the two upper corners.
	 * @param bottom color of the two lower corners.
	 */
	public GradientBackground(Color top, Color bottom)
	{
		this(bottom, bottom, top, top);
	}


	public GradientBackground(InputOnlyStream stream) throws IOException, IXMLStreamableException
	{
		process(stream);
	}


	/**
	 * Creates a BledingBackground.
	 * @param lowerLeft color of lower right corner
	 * @param lowerRight color of lower left corner
	 * @param upperRight color of upper right corner
	 * @param upperLeft color of upper left corner
	 */
	public GradientBackground(Color lowerLeft, Color lowerRight, Color upperRight, Color upperLeft)
	{
		this.lowerLeft = lowerLeft;
		this.lowerRight = lowerRight;
		this.upperRight = upperRight;
		this.upperLeft = upperLeft;
	}


	public Color getColor1()
	{
		return lowerLeft;
	}


	public void setColor1(Color color1)
	{
		this.lowerLeft = color1;
	}


	public Color getColor2()
	{
		return lowerRight;
	}


	public void setColor2(Color color2)
	{
		this.lowerRight = color2;
	}


	public Color getColor3()
	{
		return upperRight;
	}


	public void setColor3(Color color3)
	{
		this.upperRight = color3;
	}


	public Color getColor4()
	{
		return upperLeft;
	}


	public void setColor4(Color color4)
	{
		this.upperLeft = color4;
	}


	/* (non-Javadoc)
	 * @see joglui.background.Background#paint(joglui.binding.Binding)
	 */
	public void paint(Graphics b, int localX, int localY, int width, int height)
	{
		b.drawBlendedFilledRect(localX, localY, 
			width, height, lowerLeft, lowerRight, upperRight, upperLeft);
	}


	/* (non-Javadoc)
	 * @see org.fenggui.io.IOStreamSaveable#process(org.fenggui.io.InputOutputStream)
	 */
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		upperLeft = stream.processChild("TopLeftColor", upperLeft, Color.class);
		upperRight = stream.processChild("TopRightColor", upperRight, Color.class);
		lowerLeft = stream.processChild("BottomLeftColor", lowerLeft, Color.class);
		lowerRight = stream.processChild("BottomRightColor", lowerRight, Color.class);
	}
}
