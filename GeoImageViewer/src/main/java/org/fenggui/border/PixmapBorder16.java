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
 * $Id: PixmapBorder16.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.border;

import java.io.IOException;
import java.util.List;

import org.fenggui.render.Graphics;
import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;

/**
 * This border type uses 16 pixmaps to draw the border. The pixmaps are arranged as follows:
 * 
 * <pre>
 *      TOP_LEFT-----UPPER_TOP_LEFT_JUNC-----TOP-----UPPER_TOP_RIGHT_JUNC-----TOP_RIGHT
 *          |                                                                    |
 * LOWER_TOP_LEFT_JUNC                                                 LOWER_TOP_RIGHT_JUNC
 *          |                                                                    |
 *        LEFT                                                                 RIGHT
 *          |                                                                    |
 * UPPER_BOTTOM_LEFT_JUNC                                            UPPER_BOTTOM_RIGHT_JUNC
 *          |                                                                    |
 *    BOTTOM_LEFT--LOWER_BOTTOM_LEFT_JUNC--BOTTOM--LOWER_BOTTOM_RIGHT_JUNC--BOTTOM_RIGHT  
 * </pre>
 * 
 * Pixmaps number 2, 7, 8 and D are scaled in the x-axis (2, D) or in the y-axis
 * (7,8) in order to create arbitrary sized borders. One limitation with this
 * system is that it is very difficult to create a border around an object that
 * is smaller than 4w by 4h in size (where w,h are the width and height of the
 * pixmap components). This could be fixed by scaling the border down for small
 * sizes.<br/>
 * <br/>
 * In an alternative mode of operation, the pixmaps that make up an edge are merged. The
 * three pixmaps that make up one edge are regarded as one pixmap that is strechted
 * accordingly. Concretely, that means that the pixmaps 
 * 5, 7, 9 are drawn as one as well as 6, 8, A and 1, 2, 3 and C, D, E. 
 * 
 * @author Graham Briggs, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public class PixmapBorder16 extends Border
{
    /**
     * The additional border Pixmaps. 
     */
    private Pixmap[] tex = null;

    /**
     * The color that is set just before the pixmaps are drawn. This color
     * is used to modulate the color of the pixmaps
     */
    private Color modulationColor = Color.WHITE;
    
    public Color getModulationColor() {
		return modulationColor;
	}

	public void setModulationColor(Color modulationColor) {
		this.modulationColor = modulationColor;
	}

	/**
	 * Creates a new PixmapBorder.
	 * 
	 * @param array Contains the pixmaps for this border in the proper
	 * order. According to the names of the pixmap in the above figure, the
	 * order is the following:
	 * 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, A, B, C, D, E, F. In the alternative mode of
	 * operation the order of the pixmaps in the are is 0, 2, 4, 7, 8, B, D, F.
	 * @throws IllegalArgumentException Thrown if the length of the array is neither 8 nor 16.
	 */
	public PixmapBorder16(Pixmap[] array) throws IllegalArgumentException {
    	
    	// check mode of operation
		if (array.length == 16)
		{
			setSpacing(array[2].getHeight(), array[7].getWidth(), array[8].getWidth(), array[13].getHeight());
		}
		else throw new IllegalArgumentException(
				"Wrong numbers of Pixmaps! Either 8 or 16 Pixmaps can be specified, not " + array.length);
    	 
    	
    	tex = array;
    	
    }
    
	/**
	 * Creates a new PixmapBorder.
	 * 
	 * @param list Contains the pixmaps of this border in the proper order.
	 * @throws IllegalArgumentException thrown if the length of the list is neither
	 * 8 nor 16
	 */
    public PixmapBorder16(List<Pixmap> list) throws IllegalArgumentException  
    {
    	this(list.toArray(new Pixmap[list.size()]));
    }
    
    /**
     * Default constructor; used by the XML theme loading mechanism.
     *
     */
    public PixmapBorder16()
    {
    	
    }
    
    /**
     * Creates a new PixmapBorder.
     * 
     * @param left the pixmap that displays the left edge.
     * @param right the pixmap that displays the right edge.
     * @param top the pixmap that displays the top edge.
     * @param bottom the pixmap that displays the bottom edge
     * @param topleft the pixmap that displays the upper left corner
     * @param topright the pixmap that displays upper right corner
     * @param bottomleft the pixmap that displays the lower left corner
     * @param bottomright the pixmap that displays the lower right corner
     */
    public PixmapBorder16(Pixmap left, Pixmap right, Pixmap top, Pixmap bottom, Pixmap topleft, Pixmap topright, Pixmap bottomleft, Pixmap bottomright) {
    	this(new Pixmap[] {topleft, top, topright, left, right, bottomleft, bottom, bottomright});
    }
    
	@Override
	public void paint(Graphics g, int localX, int localY, int width, int height)
	{
		Pixmap left        = null;
		Pixmap right       = null;
		Pixmap top         = null;
		Pixmap bottom      = null;
		Pixmap topleft     = null;
		Pixmap topright    = null;
		Pixmap bottomleft  = null;
		Pixmap bottomright = null;
		
		g.setColor(modulationColor);
		

		topleft                		= tex[0];
		Pixmap topJunctionLeft 		= tex[1];
		top 						= tex[2];
		Pixmap topJunctionRight 	= tex[3];
		topright 					= tex[4];
		Pixmap leftJunctionTop 		= tex[5];
		Pixmap rightJunctionTop 	= tex[6];
		left 						= tex[7];
		right 						= tex[8];
		Pixmap leftJunctionBottom	= tex[9];
		Pixmap rightJunctionBottom	= tex[10]; // A
		bottomleft 					= tex[11]; // B
		Pixmap bottomJunctionLeft	= tex[12]; // C
		bottom 						= tex[13]; // D
		Pixmap bottomJunctionRight 	= tex[14]; // E
		bottomright 				= tex[15]; // F
		
		g.drawImage(topJunctionLeft, 
			localX + getLeft(), localY + height - getTop());
		
		g.drawScaledImage(top, 
			localX + getLeft() + topJunctionLeft.getWidth(), localY + height - getTop(), 
			width - getLeft() - getRight() - topJunctionLeft.getWidth() - rightJunctionTop.getWidth(), getTop());

		g.drawImage(topJunctionRight, 
			localX + width - getRight() - topJunctionRight.getWidth(), localY + height - getTop());
		
		g.drawImage(leftJunctionTop, 
			localX, localY + height - getTop() - leftJunctionTop.getHeight());
		
		g.drawImage(rightJunctionTop, 
			localX + width - getRight(), localY + height - getTop() - rightJunctionTop.getHeight());
		
		g.drawScaledImage(left, 
			localX, localY + getBottom() + leftJunctionBottom.getHeight(), 
			getLeft(), height - getBottom() - getTop() - leftJunctionTop.getHeight() - leftJunctionBottom.getHeight());
		
		g.drawScaledImage(right, 
			localX + width - getRight(), localY + getBottom() + rightJunctionBottom.getHeight(), 
			getRight(), height - getBottom() - getTop() - rightJunctionBottom.getHeight() - rightJunctionTop.getHeight());
		
		g.drawImage(leftJunctionBottom, 
			localX, localY + getBottom());
		
		g.drawImage(rightJunctionBottom, 
			localX + width - getRight(), localY + getBottom());
		
		// bottom left corner
		
		g.drawImage(bottomJunctionLeft, 
			localX + getLeft(), localY);
		
		
		g.drawScaledImage(bottom, 
			localX + getLeft() + bottomJunctionLeft.getWidth(), localY, 
			width - getLeft() - getRight() - bottomJunctionLeft.getWidth() - bottomJunctionRight.getWidth(), getBottom());
		
		g.drawImage(bottomJunctionRight, 
			localX - getRight() + width - bottomJunctionRight.getWidth(), localY);
			
	
    	
    	g.drawImage(topleft, localX, localY + height - getTop());
    	g.drawImage(bottomleft, localX,  localY);
		g.drawImage(topright, localX + width - getRight(), localY + height - getTop());
		g.drawImage(bottomright, localX + width - getRight(), localY);
	}

	/**
	 * XML streaming not supported yet for PixmapBorde16
	 */
	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		//FIXME implement me
	}
	
	
}
