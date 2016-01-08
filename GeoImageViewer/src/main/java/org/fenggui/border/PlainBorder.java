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
 * Created on May 3, 2005
 * $Id: PlainBorder.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.border;

import java.io.IOException;

import org.fenggui.Span;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;
import org.fenggui.util.Spacing;


/**
 * Solid line border type. The border can have arbitrary line
 * width (bottom, top, left, right) but is drawn in only one color.  
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 *
 */
public class PlainBorder extends Border {
	
    private Color color = Color.BLUE;

    /**
     * Creates a Simple Border.
     * @param c
     *          The color that in whicht the line shall appear
     * @param lineWidth
     *          Initiates all line widths with this thickness
     */
    public PlainBorder(Color c, int lineWidth) 
    {
    	this(lineWidth, lineWidth, lineWidth, lineWidth, c, true, Span.BORDER);
    }
    
    /**
     * Creates a Simple Border. The default color is blue. The default
     * line width is 1.
     *
     */
    public PlainBorder() 
    {
        setSpacing(1, 1, 1, 1);
    }    
    
    public PlainBorder(InputOnlyStream stream) throws IOException, IXMLStreamableException
    {
    	process(stream);
    }
    
    public PlainBorder(int top, int left, int right, int bottom) 
    {
        this(top, left, right, bottom, Color.BLACK, true, Span.BORDER);
    }  
    
    public PlainBorder(int top, int left, int right, int bottom, Color color, boolean enabled, Span span) 
    {
        setSpacing(top, left, right, bottom);
        this.color = color;
        setEnabled(enabled);
        setSpan(span);
    }
    
    public PlainBorder(Spacing s) 
    {
        setSpacing(s);
    }
    
    public PlainBorder(Spacing s, Color c) 
    {
    	color = c;
    	setSpacing(s);
    }
    
    /**
     * Creates a Simple Border. The default border width is 1.
     * @param c
     *          The color of the border
     */
    public PlainBorder(Color c) 
    {
        this(1, 1, 1, 1, c, true, Span.BORDER);
    }

    public PlainBorder(Color c, boolean enabled) 
    {
        this(1, 1, 1, 1, c, enabled, Span.BORDER);
    }

    public PlainBorder(Color c, Span span) 
    {
        this(1, 1, 1, 1, c, true, span);
    }
    
    /**
     * Returns the line color.
     * @return the line color
     * 
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Sets the line color.
     * @param color line color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    
    private void changeLineWidth(IOpenGL gl, int width)
    {
    	gl.end();
    	gl.lineWidth(width);
    	gl.startLines();
    }

	@Override
	public void paint(Graphics g, int localX, int localY, int width, int height)
	{

		
		IOpenGL gl = g.getOpenGL();
		gl.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());

		int globalX = localX + g.getTranslation().getX();
		int globalY = localY + g.getTranslation().getY();
		
		//System.out.println(globalX+" "+globalY+" "+width+"  "+height);
		//System.out.println(localX+" "+localY+" "+width+"  "+height+"  "+(globalY + height - getBottom()));
		
		if(getLeft() != 1 && getLeft() > 0) gl.lineWidth(getLeft());
		
		gl.startLines();
		
		// draw left line
		if(getLeft() > 0)
		{
			gl.vertex(globalX + getLeft()/2, globalY + getBottom());
			gl.vertex(globalX + getLeft()/2, globalY + height - getTop());
		}
		
		// draw bottom line
		if(getBottom() > 0)
		{
	        if(getBottom() != getLeft()) changeLineWidth(gl, getBottom());
	        
	        gl.vertex(globalX, globalY + getBottom()/2);
	        gl.vertex(globalX + width, globalY + getBottom()/2);
		}
		
		// draw rigth line
		if(getRight() > 0)
		{
			if(getRight() != getBottom()) changeLineWidth(gl, getRight());
        
        	gl.vertex(globalX + width - (getRight()+1)/2, globalY + getBottom());
        	gl.vertex(globalX + width - (getRight()+1)/2, globalY + height - getTop());
		}
		
		// draw top line
		if(getTop() > 0)
		{
			if(getTop() != getRight()) changeLineWidth(gl, getTop());
        
        	gl.vertex(globalX, globalY + height - (getTop()+1)/2);
        	gl.vertex(globalX + width, globalY + height - (getTop()+1)/2);
		}
		gl.end();
		gl.lineWidth(1);
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		if(getTop() == 0 && getLeft() == 0 && getBottom() == 0 && getRight() == 0)
		{
			setSpacing(1, 1);
		}
		
		color = stream.processChild("Color", color, Color.BLACK, Color.class);
	}
	
	
}
