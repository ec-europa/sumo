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
 * Created on Feb 26, 2007
 * $Id: BouncingLettersTextRenderer.java 220 2007-03-10 12:00:00Z schabby $
 */
package org.fenggui.render;

import org.fenggui.util.CharacterPixmap;

/**
 * Renders a single line of text such that the characerts jump up and down.
 * 
 * @author Johannes Schaback, last edited by $Author: schabby $, $Date: 2007-03-10 13:00:00 +0100 (Sat, 10 Mar 2007) $
 * @version $Revision: 220 $
 */
public class BouncingLettersTextRenderer implements ITextRenderer
{
	private Font font = Font.getDefaultFont(); 
	private String text = null;
	private double[] values = null;
	private double t = 0;
	
	public Font getFont()
	{
		return font;
	}

	public int getHeight()
	{
		return font.getHeight()*2;
	}

	public String getText()
	{
		return text;
	}

	public int getWidth()
	{
		return font.getWidth(text);
	}

	public void render(int x, int y, Graphics g, IOpenGL gl)
	{
		if(text == null || text.length() == 0) return;
		
    	int localX = x + g.getTranslation().getX();
    	int localY = y + g.getTranslation().getY();

        gl.enableTexture2D(true);
        
        CharacterPixmap pixmap = null;
        
        boolean init = true;
        
        for(int i = 0; i < text.length(); i++) 
        {
        	localY = y + g.getTranslation().getY() + font.getHeight()/2+  (int)(Math.sin(t+values[i])*font.getHeight()/5 + 0.5);
        	
        	final char c = text.charAt(i);
        	if(c == '\r' || c == '\f' || c =='\t') continue;
        	else if(c == '\n')
        	{
        		localY -= font.getHeight();
        		localX = x + g.getTranslation().getX();
        		continue;
        	}
	        pixmap = getFont().getCharPixMap(c);
	        
	        if(init) 
	        {
	        	ITexture tex = pixmap.getTexture();
	        	
	        	if (tex.hasAlpha()) 
	        	{
	        		gl.setTexEnvModeModulate();
	        	}
	        
	        	tex.bind();
	        	gl.startQuads();
	        	init = false;
	        }
	
	        final int imgWidth = pixmap.getWidth();
	        final int imgHeight = pixmap.getHeight();
	
	        final float endY = pixmap.getEndY();
	        final float endX = pixmap.getEndX();
	        
	        final float startX = pixmap.getStartX();
	        final float startY = pixmap.getStartY();
	
	        localY += values[i];
	        
	        gl.texCoord(startX, endY);
	        gl.vertex(localX, localY);
	
	        gl.texCoord(startX, startY);
	        gl.vertex(localX, imgHeight + localY);
	
	        gl.texCoord(endX, startY);
	        gl.vertex(imgWidth + localX, imgHeight + localY);
	
	        gl.texCoord(endX, endY);
	        gl.vertex(imgWidth + localX, localY);
	        
	        localX += pixmap.getCharWidth();
        }
        
        gl.end();
        gl.enableTexture2D(false);
        
        t = (t + 0.02) % (2*Math.PI);
	}

	public void setFont(Font font)
	{
		this.font = font;
	}

	public void setText(String text)
	{
		this.text = text;
		
		if(text == null || text.length() == 0) return;
		
		values = new double[text.length()];
		
		for(int i=0; i < values.length; i++)
		{
			//values[i] = Math.PI - Math.random()*2*Math.PI;
			values[i] = (double)i * 2*Math.PI/(double)text.length();
		}
		
	}

	/**
	 * not supported!
	 */
	public void renderCarret(int x, int y, int charIndex, ICarretRenderer carret, Graphics g, IOpenGL gl)
	{
		
	}

}
