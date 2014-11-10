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
 * $Id: PixmapBackground.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.background;

import java.io.IOException;

import org.fenggui.render.Graphics;
import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;

/**
 * Draws pixmaps as the background of a widget. The <coder>PixmapBackground</code>
 * has two modes of operation:
 * <ul>
 * <li>A single pixmap that is drawn in the center or scaled to fill the widget. It can be used
 * to display a plain image as the background.</li>
 * <li>A single pixmap that is drawn in the center and eight border-pixmaps that surround the
 * pixmap in the center. Again, the pixmap in the center can be scaled to fill the residual space. This mode
 * can be used to draw sharp edges where the inner pixmap is scaled. This comes in handy for inlay-borders for
 * example.</li>
 * </ul>
 * The pixmaps are aligned as follows where the centered pixmap can fill the entire space when
 * no surrounding pixmaps are specified:
 * <pre>
 * +-------------+--------+--------------+
 * |  top-left   |  top   |   top-right  |
 * +-------------+--------+--------------+
 * |    left     | center |    right     |
 * +-------------+--------+--------------+
 * | bottom-left | bottom | bottom-right |
 * +-------------+--------+--------------+
 * </pre>
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 *
 */
public class PixmapBackground extends Background 
{
	private static final Color DEFAULT_BLENDING_COLOR = Color.WHITE;
	private Color blendingColor = DEFAULT_BLENDING_COLOR;
	private boolean scaled = false;
	private Pixmap center = null;
	private Pixmap topLeft = null; 
	private Pixmap top = null;
	private Pixmap topRight = null;
	private Pixmap right = null;
	private Pixmap bottomLeft = null;
	private Pixmap bottom = null;
	private Pixmap bottomRight = null;
	private Pixmap left = null;
	
	/**
	 * Creates a new <code>PixmapBackground</code> with only a center pixmap.
	 * 
	 * @param center the Texture to draw as the background
	 */
	public PixmapBackground(Pixmap center) 
	{
		this(center, false);
	}
	
    public PixmapBackground(InputOnlyStream stream) throws IOException, IXMLStreamableException
	{
		process(stream);
	}
	
    /**
     * Default constructor; used by the XML theme loading mechanism.
     *
     */
    public PixmapBackground()
    {
    	
    }
    
	/**
	 * Creates a new <code>PixmapBackground</code> with only a center pixmap.
	 * 
	 * @param center the Texture to draw as the background
	 * @param scaled if the texture need to be scaled
	 */
	public PixmapBackground(Pixmap center, boolean scaled) 
	{
		this(center, null, null, null, null, null, null, null, null, scaled);
	}
	
	/**
	 * Creates a new <code>PixmapBackground</code> with a centered pixmap and
	 * the surrounding "border" pixmaps.
	 * @param center the pixmap in the center
	 * @param topLeft the pixmap in the upper left corner
	 * @param top the pixmap at the top
	 * @param topRight the pixmap at the upper right corner
	 * @param right the pxiamp at the right
	 * @param bottomRight the pixmap at the lower right corner
	 * @param bottom the pixmap at the bottom
	 * @param bottomLeft the pixmap at the lower left corner
	 * @param left the pixmap to the left
	 * @param scaled flag wether the pixmap in the center shall be scaled.
	 */
	public PixmapBackground(
			Pixmap center, 
			Pixmap topLeft, 
			Pixmap top,
			Pixmap topRight, 
			Pixmap right,
			Pixmap bottomRight,
			Pixmap bottom,
			Pixmap bottomLeft,
			Pixmap left, 
			boolean scaled) 
	{
		
		this.center = center;
		this.topLeft = topLeft;
		this.top = top;
		this.topRight = topRight;
		this.right = right;
		this.bottomRight = bottomRight;
		this.bottom = bottom;
		this.bottomLeft = bottomLeft;
		this.left = left;
		
		this.scaled = scaled;
		
		if(center == null)
			throw new IllegalArgumentException("center == null");
		
		if(topLeft != null) checkIntegrity();
	}
	
	private void checkIntegrity()
	{
		if(bottomLeft.getHeight() != bottom.getHeight())
			throw new IllegalArgumentException("bottomLeft.getHeight() != bottom.getHeight()");
		
		if(bottom.getHeight() != bottomRight.getHeight())
			throw new IllegalArgumentException("bottom.getHeight() != bottomRight.getHeight()");
		
		if(bottomLeft.getWidth() != left.getWidth())
			throw new IllegalArgumentException("bottomLeft.getWidth() != left.getWidth()");
		
		if(left.getWidth() != topLeft.getWidth())
			throw new IllegalArgumentException("left.getWidth() != topLeft.getWidth()");
		
		if(topLeft.getHeight() != top.getHeight())
			throw new IllegalArgumentException("topLeft.getHeight() != top.getHeight()");
		
		if(top.getHeight() != topRight.getHeight())
			throw new IllegalArgumentException("top.getHeight() != topRight.getHeight()");
		
		if(topRight.getWidth() != right.getWidth())
			throw new IllegalArgumentException("topRight.getWidth() != right.getWidth()");
		
		if(right.getWidth() != bottomRight.getWidth())
			throw new IllegalArgumentException("right.getWidth() != bottomRight.getWidth()");
	}
	

	
	@Override
	public void paint(Graphics g, int localX, int localY, int width, int height) 
	{
		g.setColor(blendingColor);

		// draw only the center
		if(topLeft == null)
		{
			if (scaled) 
			{
				g.drawScaledImage(center, localX, localY, width, height);
			} 
			else 
			{
				g.drawImage(center, localX + width/2 - center.getWidth()/2, localY + height/2-center.getHeight()/2);
			}
		}
		else // draw the "border" around the center
		{
			g.drawImage(bottomLeft, localX, localY);
			g.drawScaledImage(bottom, localX + bottomLeft.getWidth(), localY, width - left.getWidth() - right.getWidth(), bottom.getHeight());
			g.drawImage(bottomRight, localX + width - bottomRight.getWidth(), localY);
			
			g.drawScaledImage(left, localX, localY + bottomLeft.getHeight(), left.getWidth(), height - top.getHeight() - bottom.getHeight());
			
			g.drawScaledImage(right, localX + width - right.getWidth(), localY + bottomRight.getHeight(), right.getWidth(), height - topRight.getHeight() - bottomRight.getHeight());
			
			g.drawImage(topLeft, localX, localY + height - top.getHeight());
			g.drawScaledImage(top, localX + topLeft.getWidth(), localY + height - top.getHeight(), width - topRight.getWidth() - topLeft.getWidth(), topLeft.getHeight());
			g.drawImage(topRight, localX + width - topRight.getWidth(), localY + height - topRight.getHeight());

			
			if (scaled) 
			{
				g.drawScaledImage(center, localX + left.getWidth(), localY + bottomLeft.getHeight(), width - right.getWidth() - left.getWidth(), height - top.getHeight() - bottom.getHeight());
			} 
			else 
			{
				g.drawImage(center, localX + width/2 - center.getWidth()/2, localY + height/2-center.getHeight()/2);
			}
		}
		

	}

	public Color getBlendingColor()
	{
		return blendingColor;
	}

	public void setBlendingColor(Color blendingColor)
	{
		if(blendingColor == null) throw new IllegalArgumentException("blendingColor == null");
		this.blendingColor = blendingColor;
	}

	public boolean isScaled()
	{
		return scaled;
	}

	public void setScaled(boolean scaled)
	{
		this.scaled = scaled;
	}
	
	/* (non-Javadoc)
	 * @see org.fenggui.io.IOStreamSaveable#process(org.fenggui.io.InputOutputStream)
	 */
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		blendingColor = stream.processChild("BlendingColor", blendingColor, DEFAULT_BLENDING_COLOR, Color.class);
		scaled = stream.processAttribute("scaled", scaled);

		center = stream.processChild("CenterPixmap", center, Pixmap.class);
		
		left = stream.processChild("LeftPixmap", left, null, Pixmap.class);
		right = stream.processChild("RightPixmap", right, null, Pixmap.class);
		top = stream.processChild("TopPixmap", top, null, Pixmap.class);
		topLeft = stream.processChild("TopLeftPixmap", topLeft, null, Pixmap.class);
		topRight = stream.processChild("TopRightPixmap", topRight, null, Pixmap.class);
		bottom = stream.processChild("BottomPixmap", bottom, null, Pixmap.class);
		bottomLeft = stream.processChild("BottomLeftPixmap", bottomLeft, null, Pixmap.class);
		bottomRight = stream.processChild("BottomRightPixmap", bottomRight, null, Pixmap.class);
	}

}

