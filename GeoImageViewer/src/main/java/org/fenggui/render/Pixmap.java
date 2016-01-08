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
 * $Id: Pixmap.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.render;

import java.io.IOException;

import org.fenggui.theme.XMLTheme;
import org.fenggui.theme.xml.IXMLStreamable;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;


/**
 * A pixmap describes a rectangular area on a texture. The idea
 * behind it is that multiple pixmaps can share the same
 * texture to make the use of
 * texture space more efficient. 
 * A pixmap can also span over the whole
 * texture of course.<br/>
 * <br/>
 * Note thate in contrast to the widget space, the origin of a pixmap
 * is at the upper left corner! That makes it easier to define pixmaps
 * on textures, because the textures are usually created with external
 * tools which regard the image origin in the upper left corner. 
 * 
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public class Pixmap implements IXMLStreamable
{

	/**
	 * The texture that holds the pixmap
	 */
	private ITexture texture  = null;
	
	/**
	 * position of the pixmap in the texture
	 */
	private int x, y;
	
	/**
	 * the size of the pixmap
	 */
	private int width, height;
	
	/**
	 * Creates a new Pixmap that covers the complete image that is hold
	 * in the texture.
	 * @param tex the texture
	 */
	public Pixmap(ITexture tex) 
	{
		this(tex, 0, 0, tex.getImageWidth(), tex.getImageHeight());
	}
	
	public Pixmap(InputOnlyStream stream) throws IOException, IXMLStreamableException 
	{
		process(stream);
	}
	
	/**
	 * Creates a new Pixmap that covers an area of the given texture.
	 * @param texture the text
	 * @param x the x coordinate of the origin of the Pixmap in the Texture
	 * @param y the y coordinate of the origin of the Pixmap in the Texture
	 * @param width the width of the Pixmap
	 * @param height the height of the Pixmap
	 */
	public Pixmap(ITexture texture, int x, int y, int width, int height) 
	{
		this.texture = texture;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		setTexture(texture);
	}

	public ITexture getTexture() 
	{
		return texture;
	}

	public int getX() 
	{
		return x;
	}

	public int getY() 
	{
		return y;
	}

	
	
	/**
	 * Returns the height of the Pixmap in pixels.
	 * @return height
	 */
	public int getHeight() 
	{
		return height;
	}

	/**
	 * Returns the width of the Pixmap in pixels.
	 * @return width
	 */
	public int getWidth() 
	{
		return width;
	}

	public String toString() 
	{
		return "Pixmap pos: "+x+", "+y+" size: "+width+", "+height;
	}
	
	protected void setTexture(ITexture texture) 
	{
		this.texture = texture;
	}

	/**
	 * Returns the x coordinate of the right hand side of the pixmap
	 * in texture coordinates
	 * @return x coordinate of right hand side
	 */
	public float getEndX() 
	{
		return (float) (width+x) / (float) texture.getTextureWidth();
	}

	/**
	 * Returns the y coordinate of the top of the pixmap
	 * in texture coordinates
	 * @return y coordinate of right hand side
	 */
	public float getEndY() 
	{
		return (float) (height+y) / (float) texture.getTextureHeight();
	}

	/**
	 * Returns the x coordinate of this pixmap on the texture.
	 * @return xcoordinate
	 */
	public float getStartX() 
	{
		return (float) x / (float) texture.getTextureWidth();
	}

	/**
	 * Returns the y coordinate of this pixmap on the texture.
	 * @return y coordinate
	 */
	public float getStartY() 
	{
		return (float) y / (float) texture.getTextureHeight();
	}
	
	/* (non-Javadoc)
	 * @see org.fenggui.io.IOStreamSaveable#process(org.fenggui.io.InputOutputStream)
	 */
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException {
		//texture = stream.process(texture, the used texture class);
		
		// i dont know the exact type of the text (LWJGL or JOGL)
		if(stream.isInputStream())
			texture = (ITexture) stream.processChild(texture, XMLTheme.TYPE_REGISTRY);
		
		x = stream.processAttribute("x", x);
		y = stream.processAttribute("y", y);
		width = stream.processAttribute("width", width);
		height = stream.processAttribute("height", height);
	}

	/* (non-Javadoc)
	 * @see org.fenggui.io.IOStreamSaveable#getUniqueName()
	 */
	public String getUniqueName() {
		return GENERATE_NAME;
	}
}
