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
 * Created on May 20, 2005
 * $Id: BevelBorder.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.border;

import java.io.IOException;

import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.theme.xml.DefaultElementName;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;

/**
 * Bevel border type.
 * 
 * @author Johannes, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
@DefaultElementName("BevelBorder")
public class BevelBorder extends Border 
{
    private Color elevated = Color.LIGHT_GRAY;
    private Color lowered = Color.DARK_GRAY;

    /**
     * Creates a new <code>BevelBorder</code> object. The spacing is
     * set to 1 per default.
     *
     */
    public BevelBorder() 
    {
        setSpacing(1, 1, 1, 1);
    }
    
    /**
     * Creates a new <code>BevelBorder</code> object with the given colors.
     * @param elevated the color of the ascending edges
     * @param lowered the color of the descending edges
     */
    public BevelBorder(Color elevated, Color lowered) 
    {
        this.elevated = elevated;
        this.lowered = lowered;
        setSpacing(1, 1, 1, 1);
    }

	@Override
	public void paint(Graphics g, int localX, int localY, int width, int height)
	{
		
		IOpenGL gl = g.getOpenGL();
		
		g.setColor(lowered);
		
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
			gl.vertex(globalX + getLeft()/2, globalY - getBottom() + height);
		}
		
		g.setColor(elevated);
		
		// draw bottom line
		if(getBottom() > 0)
		{
	        if(getBottom() != getLeft()) changeLineWidth(gl, getBottom());
	        
	        gl.vertex(globalX, globalY + getBottom()/2);
	        gl.vertex(globalX - getLeft() + width + getRight(), globalY + getBottom()/2);
		}
		
		// draw rigth line
		if(getRight() > 0)
		{
			if(getRight() != getBottom()) changeLineWidth(gl, getRight());
        
        	gl.vertex(globalX - getLeft() + width + getRight()/2, globalY + getBottom());
        	gl.vertex(globalX - getLeft() + width + getRight()/2, globalY - getBottom() + height);
		}
		
		g.setColor(lowered);
		
		// draw top line
		if(getTop() > 0)
		{
			if(getTop() != getRight()) changeLineWidth(gl, getTop());
        
        	gl.vertex(globalX, globalY - getBottom() + height + getTop()/2);
        	gl.vertex(globalX - getLeft() + width + getRight(), globalY - getBottom() + height + getTop()/2);
		}
		gl.end();
		gl.lineWidth(1);
	}
	
    private void changeLineWidth(IOpenGL gl, int width)
    {
    	gl.end();
    	gl.lineWidth(width);
    	gl.startLines();
    }

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		elevated = stream.processChild("ElevatedColor", elevated, Color.class);
		lowered = stream.processChild("LoweredColor", lowered, Color.class);
	}
	
}
