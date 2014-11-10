/*
 * RoundedBorder.java
 * 
 * Created 17th October 2005
 */
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
 * $Id: RoundedBorder.java 76 2006-10-16 20:29:55Z schabby $
 */
package org.fenggui.border;

import org.fenggui.render.Graphics;
import org.fenggui.util.Color;

/**
 * Class for drawing Rectangular borders with rounded corners.
 * 
 * @author Graham Briggs, last edited by $Author: schabby $, $Date: 2006-10-16 22:29:55 +0200 (Mon, 16 Oct 2006) $
 * @version $Revision: 76 $
 */
public class RoundedBorder extends Border
{
    /**
     * The width of the border to be drawn.
     */
    private int weight;

    /**
     * The radius of each corner.
     */
    private int radius;
    
    /**
     * The colour of this border.
     */
    private Color color;

    /**
     * Create a RoundedBorder with a certain colour and line weight.
     * 
     * @param color
     *            The colour of the border.
     * @param weight
     *            The weight (width) of the border.
     */
    public RoundedBorder(Color color, int weight, int radius)
    {
        this.color = color;
        this.weight = weight;
        this.radius = radius;
        setSpacing(radius, radius, radius, radius);
    }

    public int getBottomBorderWidth()
    {
        return weight;
    }

    public int getLeftBorderWidth()
    {
        return weight;
    }

    public int getRightBorderWidth()
    {
        return weight;
    }

    public int getTopBorderWidth()
    {
        return weight;
    }

	@Override
	public void paint(Graphics g, int localX, int localY, int width, int height)
	{
        g.setColor(color);
        g.setLineWidth((int)weight);
        
        // draw a rounded rectangle
        g.drawRoundedRectangle(localX, localY, width, height, radius);
	}

}
