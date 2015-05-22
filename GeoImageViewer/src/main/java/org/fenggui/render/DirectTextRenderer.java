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
 * Created on Feb 23, 2007
 * $Id: DirectTextRenderer.java 220 2007-03-10 12:00:00Z schabby $
 */
package org.fenggui.render;

import org.fenggui.util.CharacterPixmap;

/**
 * Renders lines of text by drawing each character directly as a single
 * quad. Use this TextRenderer if you update your label (or text)
 * frequently.
 * 
 * @author Johannes Schaback, last edited by $Author: schabby $, $Date: 2007-03-10 13:00:00 +0100 (Sat, 10 Mar 2007) $
 * @version $Revision: 220 $
 */
public class DirectTextRenderer implements ITextRenderer
{
	private String text = null;
	private Font font = Font.getDefaultFont();
	private int height=-1, width = -1;
	
	public String getText()
	{
		return text;
	}

	public void render(int x, int y, Graphics g, IOpenGL gl)
	{
		if(text == null || text.length() == 0) return;
		
    	int localX = x + g.getTranslation().getX();
    	int localY = y + g.getTranslation().getY() + getHeight() - font.getHeight();

        gl.enableTexture2D(true);
        
        CharacterPixmap pixmap = null;
        
        boolean init = true;
        
        for(int i = 0; i < text.length(); i++) 
        {
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
	}

	public void setText(String text)
	{
		this.text = text;
		
		width = -1;
		height = -1;
	}

	public Font getFont()
	{
		return font;
	}

	public void setFont(Font font)
	{
		width = -1;
		height = -1;		

		this.font = font;
	}

	public int getHeight()
	{
		if(text == null || text.length() == 0) return 0;
		
		if(height == -1)
		{
			String[] split = text.split("\n");
			height = split.length * font.getHeight();
		}
		
		return height;
	}

	public int getWidth()
	{
		if(text == null || text.length() == 0) return 0;
		
		if(width == -1)
		{
			String[] split = text.split("\n");
			for(int i=0; i < split.length; i++)
			{
				width = Math.max(width, font.getWidth(split[i]));
			}
		}
		
		return width;
	}

	public void renderCarret(int x, int y, int charIndex, ICarretRenderer carret, Graphics g, IOpenGL gl)
	{
		if(text == null || text.length() == 0) return;
		
		if(charIndex < 0 || carret == null || charIndex > text.length()+1) return;
		
    	int localX = x + g.getTranslation().getX();
    	int localY = y + g.getTranslation().getY() + getHeight() - font.getHeight();

        CharacterPixmap pixmap = null;
        
        for(int i = 0; i <= charIndex; i++) 
        {
        	if(i >= text.length()) break;
        		
        	final char c = text.charAt(i);
        	if(c == '\r' || c == '\f' || c =='\t') continue;
        	else if(c == '\n')
        	{
        		localY -= font.getHeight();
        		localX = x + g.getTranslation().getX();
        		continue;
        	}
	        pixmap = getFont().getCharPixMap(c);
        
	        localX += pixmap.getCharWidth();
        }
        
        carret.render(localX - g.getTranslation().getX(), localY - g.getTranslation().getY(), g);
	}

	
}
