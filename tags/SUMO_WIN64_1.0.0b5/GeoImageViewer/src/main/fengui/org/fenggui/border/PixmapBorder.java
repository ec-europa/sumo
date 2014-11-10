/*
 * FengGUI - Java GUIs in OpenGL (http://fenggui.dev.java.net)
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
 * Created on 20th October 2005
 * $Id: PixmapBorder.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.border;

import java.io.IOException;
import java.util.List;

import org.fenggui.render.Graphics;
import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;

/**
 * Border made of 8 pixmaps. Here's a layout of a widget using PixmapBorder:
 * <pre>
 *  TOP_LEFT------TOP-----TOP_RIGHT
 *     |                      |
 *    LEFT                  RIGHT
 *     |                      |
 *  BOTTOM_LEFT--BOTTOM--BOTTOM_RIGHT  
 * </pre>
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public class PixmapBorder extends Border
{
	public static final int TOP_LEFT = 0;
	public static final int LEFT = 1;
	public static final int BOTTOM_LEFT = 2;
	public static final int BOTTOM = 3;
	public static final int BOTTOM_RIGHT = 4;
	public static final int RIGHT = 5;
	public static final int TOP_RIGHT = 6;
	public static final int TOP = 7;
	
    private Pixmap[] tex = new Pixmap[8];

    public PixmapBorder(InputOnlyStream stream) throws IOException, IXMLStreamableException
    {
    	process(stream);
    }
    
    /**
     * The color that is set just before the pixmaps are drawn. This color
     * is used to modulate the color of the pixmaps
     */
    private Color modulationColor = Color.WHITE;
    
    public Color getModulationColor() 
    {
		return modulationColor;
	}

	public void setModulationColor(Color modulationColor) 
	{
		this.modulationColor = modulationColor;
	}

	/**
	 * Creates a new PixmapBorder. Fill in the pixmaps counter clock-wise which is
	 * FengGUI convention.
	 * 
	 * @param array Contains the pixmaps for this border in the proper
	 * order. The order of the pixmaps in the array is given in the class description
	 * of PixmapBorder. Generally we suggest to directly use the static fields TOP_LEFT,
	 * TOP, TOP_RIGHT, etc. to fill the array, because we use these static fields internally
	 * as well.
	 * @throws IllegalArgumentException Thrown if the length of the array is neither 8 nor 16.
	 */
	public PixmapBorder(Pixmap[] array) throws IllegalArgumentException 
	{
    	// check mode of operation
    	if (array.length == 8)
		{
			setSpacing(array[TOP].getHeight(), array[LEFT].getWidth(), array[RIGHT].getWidth(), array[BOTTOM].getHeight());
		}
		else throw new IllegalArgumentException("PixmapBorder takes 8 pixmaps, not " + array.length);
    	
    	this.tex = array;
    }
    
	/**
	 * Creates a new PixmapBorder.
	 * 
	 * @param list Contains the pixmaps of this border in the proper order.
	 * @throws IllegalArgumentException thrown if the length of the list is 8
	 */
    public PixmapBorder(List<Pixmap> list) throws IllegalArgumentException  
    {
    	this(list.toArray(new Pixmap[list.size()]));
    }
    
    /**
     * Creates a new PixmapBorder.
     * 
     * @param leftEdge the pixmap that displays the left edge.
     * @param rightEdge the pixmap that displays the right edge.
     * @param topEdge the pixmap that displays the top edge.
     * @param bottomEdge the pixmap that displays the bottom edge
     * @param upperLeftCorner the pixmap that displays the upper left corner
     * @param upperRightCorner the pixmap that displays upper right corner
     * @param lowerLeftCorner the pixmap that displays the lower left corner
     * @param lowerRightCorner the pixmap that displays the lower right corner
     */
    public PixmapBorder(
    		Pixmap leftEdge, 
    		Pixmap rightEdge, 
    		Pixmap topEdge, 
    		Pixmap bottomEdge, 
    		Pixmap upperLeftCorner,
    		Pixmap upperRightCorner,
    		Pixmap lowerLeftCorner, 
    		Pixmap lowerRightCorner) 
    {
    	this(new Pixmap[] {upperLeftCorner, leftEdge, lowerLeftCorner, bottomEdge, lowerRightCorner, rightEdge, upperRightCorner, topEdge});
    }

	@Override
	public void paint(Graphics g, int localX, int localY, int width, int height)
	{
		g.setColor(modulationColor);

		g.drawScaledImage(tex[LEFT], 
			localX, localY + getBottom(), 
			getLeft(), height - getBottom() - getTop());
		
		g.drawScaledImage(tex[BOTTOM],
			localX + tex[BOTTOM_LEFT].getWidth(), localY,
			width - getLeft() - getRight(), getBottom());
		
		g.drawScaledImage(tex[TOP], 
			localX + getLeft(),	localY + height - getTop(),
			width - getLeft() - getRight(), getTop());
		
		g.drawScaledImage(tex[RIGHT], 
			localX + width - getRight(), localY + getBottom(),
			getRight(), height - getBottom() - getTop());
		
    	
    	g.drawImage(tex[TOP_LEFT], localX, localY + height - getTop());
    	g.drawImage(tex[BOTTOM_LEFT], localX,  localY);
		g.drawImage(tex[TOP_RIGHT], localX + width - getRight(), localY + height - getTop());
		g.drawImage(tex[BOTTOM_RIGHT], localX + width - getRight(), localY);
	}
	
	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		tex[TOP_LEFT] = stream.processChild("TopLeftPixmap", tex[TOP_LEFT], Pixmap.class);
		tex[LEFT] = stream.processChild("LeftEdgePixmap", tex[LEFT], Pixmap.class);
		tex[BOTTOM_LEFT] = stream.processChild("BottomLeftPixmap", tex[BOTTOM_LEFT], Pixmap.class);
		tex[BOTTOM] = stream.processChild("BottomEdgePixmap", tex[BOTTOM], Pixmap.class);
		tex[BOTTOM_RIGHT] = stream.processChild("BottomRightPixmap", tex[BOTTOM_RIGHT], Pixmap.class);
		tex[RIGHT] = stream.processChild("RightEdgePixmap", tex[RIGHT], Pixmap.class);
		tex[TOP_RIGHT] = stream.processChild("TopRightPixmap", tex[TOP_RIGHT], Pixmap.class);
		tex[TOP] = stream.processChild("TopEdgePixmap", tex[TOP], Pixmap.class);
		
		super.setSpacing(tex[TOP].getHeight(), tex[LEFT].getWidth(), tex[RIGHT].getWidth(), tex[BOTTOM].getHeight());
	}
	
	
}
