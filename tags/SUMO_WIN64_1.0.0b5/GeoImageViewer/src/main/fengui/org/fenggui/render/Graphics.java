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
 * Created on Apr 18, 2005
 * $Id: Graphics.java 341 2007-08-14 18:52:31Z Schabby $
 */
package org.fenggui.render;

import org.fenggui.util.CharacterPixmap;
import org.fenggui.util.Color;
import org.fenggui.util.Point;
import org.fenggui.util.Rectangle;

/**
 * Provides a library of graphical functions for GUI classes to utilise, rather
 * than each class having to implement its own OpenGL rendering directly.
 * 
 * @todo make use of the handy '...' operator for paramters. This can allow
 * to pass an arbitray number of pixmaps to a draw routine #
 * 
 * @author Graham Briggs, last edited by $Author: Schabby $, $Date: 2007-08-14 20:52:31 +0200 (Di, 14 Aug 2007) $
 * @version $Revision: 341 $
 */
public class Graphics
{
	/**
     * The OpenGL Object
     */
    private IOpenGL gl;

    /**
     * The current font we are rendering with.
     */
    private Font font;

    /**
     * 2D Clip area - we should not render outside of the clip area.
     */
    private final Rectangle clipSpace = new Rectangle(0, 0, 10000, 10000);

    /**
     * Offset into the renderable area.
     */
    private final Point offset = new Point(0, 0);

    /**
     * Gets the current Font.
     * 
     * @return The current Font we are rendering with.
     */
    public Font getFont()
    {
        return font;
    }

    public Point getTranslation() {
    	return offset;
    }
   
    /**
     * Sets the Font.
     * 
     * @param font
     *            The new Font to set.
     */
    public void setFont(Font font)
    {
        this.font = font;
    }

    /**
     * Initialises the Graphics object.
     * 
     * @param gl
     *            The OpenGL object we will be rendering via.
     */
    public Graphics(IOpenGL gl)
    {
        this.gl = gl;
    }
    
    /**
     * Sets the clipping area according to the current offset 
     * (translation). In other words, the clipping rectangle
     * is translation dependend!!
     * 
     * @param x
     *            The x-coordinate of the left hand side of the clipping area
     * @param y
     *            The y-coordinate of the top of the clipping area
     * @param width
     *            The width of the clipping area
     * @param height
     *            The height of the clipping area
     */
    public void setClipSpace(int x, int y, int width, int height)
    {
        x += offset.getX();
        y += offset.getY();

        // setup the clip space rectangle that we keep in the Graphics instance 
        clipSpace.set(x, y, width, height);
        
        gl.setScissor(x, width, y, height);
    }

    /**
     * Draws a filled rectangle using the current colour.
     * 
     * @param x
     *            The left hand side of the rectangle
     * @param y
     *            The top of the rectangle
     * @param width
     *            The width of the rectangle
     * @param height
     *            The height of the rectangle
     */
    public void drawFilledRectangle(int x, int y, int width, int height)
    {
        x += offset.getX();
        y += offset.getY();

        gl.startQuads();
        gl.vertex(x, y);
        gl.vertex(x + width, y);
        gl.vertex(x + width, y + height);
        gl.vertex(x, y + height);
        gl.end();

    }

    /**
     * Draws a solid filled Bevel Box.
     * @param x the left hand side of the rectangle
     * @param y the right hand side of the rectangle
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @param bright the color for the right side and up side
     * @param dark the color for the right side and down side
     * @param fill the color used to fill the enclosed recangle
     */
    public void drawFilledBevelRectangle(int x, int y, int width, int height, Color bright, Color dark, Color fill)
    {
    	drawBevelRectangle(x, y, width, height, bright, dark);
    	
    	setColor(fill);
    	
    	drawFilledRectangle(x+1, y+1, width-2, height-2);
    }    
    
    /**
     * Obtain the OpenGL handle - allows custom GL rendering
     * 
     * @return The OpenGL handle
     */
    public IOpenGL getOpenGL()
    {
        return gl;
    }

    /**
     * Renders a String of text to the display. This is done by getting the
     * texture image associated with each character in the text you wish to
     * display, and then drawing that image as a Quad via the drawImage method.
     * 
     * @param text
     *            The text to render.
     * @param x
     *            The x position of the string to render
     * @param y
     *            The y position of the string to render
     **/
    public void drawString(String text, int x, int y) {
    	
    	// sanity check (johannes)
    	if(text == null) return;
    	
    	x += offset.getX();
        y += offset.getY();

        gl.enableTexture2D(true);
        
        CharacterPixmap pixmap = null;
        
        for(int i=0;i<text.length();i++) 
        {
	        pixmap = getFont().getCharPixMap(text.charAt(i));
	        
	        if(i == 0) 
	        {
	        	ITexture tex = pixmap.getTexture();
	        	
	        	if (tex.hasAlpha()) 
	        	{
	        		gl.setTexEnvModeModulate();
	        	}
	        
	        	tex.bind();
	        	gl.startQuads();
	        }
	
	        final int imgWidth = pixmap.getWidth();
	        final int imgHeight = pixmap.getHeight();
	
	        final float endY = pixmap.getEndY();
	        final float endX = pixmap.getEndX();
	        
	        final float startX = pixmap.getStartX();
	        final float startY = pixmap.getStartY();
	
	        gl.texCoord(startX, endY);
	        gl.vertex(x, y);
	
	        gl.texCoord(startX, startY);
	        gl.vertex(x, imgHeight + y);
	
	        gl.texCoord(endX, startY);
	        gl.vertex(imgWidth + x, imgHeight + y);
	
	        gl.texCoord(endX, endY);
	        gl.vertex(imgWidth + x, y);
	        
	        x += pixmap.getCharWidth();
        }
        gl.end();
        // gl.translate(0,0,-0.1f);
        gl.enableTexture2D(false);
        // gl.translateXY(-offset.getX(), -offset.getY());    	
    }

    /**
     * Renders a String of text to the display. This is done by getting the
     * texture image associated with each character in the text you wish to
     * display, and then drawing that image as a Quad via the drawImage method.
     * 
     * @param text
     *            The text to render.
     * @param x
     *            The x position of the string to render
     * @param y
     *            The y position of the string to render
     * @param angle
     *            The angle of the rotation (in degrees)
     **/
    public void drawRotatedString(String text, int x, int y, float angle) {
    	x += offset.getX();
        y += offset.getY();
        gl.pushMatrix();
        gl.translateXY(x, y);
        gl.rotate(angle);
        drawString(text, -offset.getX(), -offset.getY());
        gl.popMatrix();
    }
    
    /**
     * Draws a scaled image.
     * 
     * @param pixmap the Pixmap to be drawn.
     * @param x the x-coordinate of the position where to draw the pixmap
     * @param y the y-coordinate of the position where to draw to pixmap
     * @param imgWidth the width of the drawn image
     * @param imgHeight the heigth of the drawn image
     */
    public void drawScaledImage(Pixmap pixmap, int x, int y, int imgWidth, int imgHeight) 
    {
        x += offset.getX();
        y += offset.getY();

        gl.enableTexture2D(true);
        
        ITexture tex = pixmap.getTexture();
        
        /*
         TODO well, this should NEVER happen here actually. Besides,
         it is time critical code around here :)
         We shall check whether the texture is null in the pixmap. However,
         fonts are generated lazily....
         */
        if (tex == null)
        {
        	throw new NullPointerException("pixmap "+pixmap+" has no texture! pixmap.getTexture() == null");
        }
        
        gl.setTexEnvModeModulate();
        
        tex.bind();

        gl.startQuads();

        final float endY = pixmap.getEndY();
        final float endX = pixmap.getEndX();
        
        final float startX = pixmap.getStartX();
        final float startY = pixmap.getStartY();

        gl.texCoord(startX, endY);
        gl.vertex(x, y);

        gl.texCoord(startX, startY);
        gl.vertex(x, imgHeight + y);

        gl.texCoord(endX, startY);
        gl.vertex(imgWidth + x, imgHeight + y);

        gl.texCoord(endX, endY);
        gl.vertex(imgWidth + x, y);
        gl.end();
        // gl.translate(0,0,-0.1f);
        gl.enableTexture2D(false);    	
    }
    
    public void drawImage(Pixmap pixmap, int x, int y) {
        x += offset.getX();
        y += offset.getY();

        gl.enableTexture2D(true);
        
        ITexture tex = pixmap.getTexture();
        
        if (tex == null)
        {
        	throw new NullPointerException("pixmap "+pixmap+" has no texture! pixmap.getTexture() == null");
        }
        
        if (tex.hasAlpha())
        {
            gl.setTexEnvModeModulate();
        }
        
        tex.bind();

        gl.startQuads();

        final int imgWidth = pixmap.getWidth();
        final int imgHeight = pixmap.getHeight();

        final float endY = pixmap.getEndY();
        final float endX = pixmap.getEndX();
        
        final float startX = pixmap.getStartX();
        final float startY = pixmap.getStartY();

        gl.texCoord(startX, endY);
        gl.vertex(x, y);

        gl.texCoord(startX, startY);
        gl.vertex(x, imgHeight + y);

        gl.texCoord(endX, startY);
        gl.vertex(imgWidth + x, imgHeight + y);

        gl.texCoord(endX, endY);
        gl.vertex(imgWidth + x, y);
        gl.end();
        // gl.translate(0,0,-0.1f);
        gl.enableTexture2D(false);
        // gl.translateXY(-offset.getX(), -offset.getY());
    }
    
    /**
     * Draws an image to the screen. The image is not scaled in any way.
     * 
     * @param tex
     *            The image to draw.
     * @param x
     *            The x-coordinate to place the image at
     * @param y
     *            The y-coordinate to place the image at
     */
    public void drawImage(ITexture tex, int x, int y)
    {
        x += offset.getX();
        y += offset.getY();

        gl.enableTexture2D(true);
        
        gl.setTexEnvModeModulate();
        
        tex.bind();

        gl.startQuads();

        int imgWidth = tex.getImageWidth();
        int imgHeight = tex.getImageHeight();

        float endY = (float) imgHeight / (float) tex.getTextureHeight();
        float endX = (float) imgWidth / (float) tex.getTextureWidth();
        float startX = 0;
        float startY = 0;

        gl.texCoord(startX, endY);
        gl.vertex(x, y);

        gl.texCoord(startX, startY);
        gl.vertex(x, imgHeight + y);

        gl.texCoord(endX, startY);
        gl.vertex(imgWidth + x, imgHeight + y);

        gl.texCoord(endX, endY);
        gl.vertex(imgWidth + x, y);
        gl.end();
        // gl.translate(0,0,-0.1f);
        gl.enableTexture2D(false);
        // gl.translateXY(-offset.getX(), -offset.getY());

    }

    

    /**
     * Draws a scaled image, whilst clipping it correctly.
     * @deprecated use draw scaled Image(Pixmap, int, ...) instead
     * @param tex
     *            The Texture you wish to draw
     * @param x
     *            The x coordinate you wish to place it at
     * @param y
     *            The y coordinate you wish to place it at
     * @param width
     *            The width you want to scale the Texture to
     * @param height
     *            The height you want to scale the Texture to
     */
    public void drawScaledImage(ITexture tex, int x, int y, int width, int height)
    {
        x += offset.getX();
        y += offset.getY();

        // If all of the area we are rendering is outside the clip zone, then
        // don't even bother!
        /*
        if ((x < clipSpace.getX() && x + width < clipSpace.getX())
                || (y < clipSpace.getY() && y + height < clipSpace.getY())
                || (x > clipSpace.getX() + clipSpace.getWidth() && x + width > clipSpace.getX() + clipSpace.getWidth())
                || (y > clipSpace.getY() + clipSpace.getHeight() && y + height > clipSpace.getY()
                        + clipSpace.getHeight()))
        {
            return;
        }
         */
        gl.enableTexture2D(true);

        if (tex.hasAlpha())
        {
            gl.setTexEnvModeModulate();
        }

        tex.bind();

        gl.startQuads();

        // texture positions
        float startY = 0.0f; // top
        float startX = 0.0f; // left
        float endY = 1.0f; // bottom
        float endX = 1.0f; // right

        int rWidth = width;
        int rHeight = height;

        // fit into clip - both the polygon to render (x,y,x+w,y+h) AND the
        // texture (0,0->1,1)
        if (x < clipSpace.getX())
        {
            rWidth -= clipSpace.getX() - x;
            startX = (float) (clipSpace.getX() - x) / (float) width;
            x = clipSpace.getX();
        }

        if (x + rWidth > clipSpace.getX() + clipSpace.getWidth())
        {
            rWidth = clipSpace.getX() + clipSpace.getWidth() - x;
            endX = (float) rWidth / (float) width;
        }

        if (y < clipSpace.getY())
        {
            rHeight -= clipSpace.getY() - y;
            endY = (float) rHeight / (float) height;
            y = clipSpace.getY();
        }

        if (y + rHeight > clipSpace.getY() + clipSpace.getHeight())
        {
            rHeight = clipSpace.getY() + clipSpace.getHeight() - y;
            startY = (float) (height - rHeight) / (float) height;
        }

        gl.texCoord(startX, endY);
        gl.vertex(x, y);

        gl.texCoord(startX, startY);
        gl.vertex(x, rHeight + y);

        gl.texCoord(endX, startY);
        gl.vertex(rWidth + x, rHeight + y);

        gl.texCoord(endX, endY);
        gl.vertex(rWidth + x, y);
        gl.end();
        gl.enableTexture2D(false);

    }


    /**
     * Sets the current pen colour.
     * 
     * @param c The colour to set the pen to.
     */
    public void setColor(Color c)
    {
        gl.color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }

    /**
     * Sets the current pen colour (completely opaque).
     * 
     * @param red The red component of the colour
     * @param green The green component of the colour
     * @param blue The blue component of the colour
     */
    public void setColor(float red, float green, float blue)
    {
        gl.color(red, green, blue, 1);
    }
    
    
    /**
     * Sets the current pen colour, with varying opacity.
     * 
     * @param red The red component of the colour
     * @param green The green component of the colour
     * @param blue The blue component of the colour
     * @param alpha The level of opacity
     */
    public void setColor(float red, float green, float blue, float alpha)
    {
        gl.color(red, green, blue, alpha);
    }

    /**
     * Sets the width of all lines to be drawn. The width must be greater than
     * 0.0f. In antialiased mode lines with a non-integer width are possible and
     * are equivalent to rendering a filled rectangle of the given width, centered
     * on the exact line.
     * 
     * @param width The new width of lines, in pixels.
     */
    public void setLineWidth(float width)
    {
        if (width > 0.0f)
        {
            gl.lineWidth(width);
        }
    }
    
    /**
     * Sets up the stipple pattern for OpenGL. 
     * 
     * @param stretch You can stretch the stipple by an integer amount.
     * @param pattern The 16-bit bit-pattern for the stipple.
     */
    public void setLineStipple(int stretch, short pattern)
    {
        gl.enableStipple();
        gl.lineStipple(stretch, pattern);
    }
    
    /**
     * Enable and disable the stippling of lines.
     * 
     * @param enable Is stippling to be enabled?
     */
    public void setLineStipple(boolean enable)
    {
        if (enable)
        {
            gl.enableStipple();
        }
        else
        {
            gl.disableStipple();
        }
    }
    
    /**
     * Draws a line, with clipping.
     * 
     * @param x1 Starting x-coordinate
     * @param y1 Starting y-coordinate
     * @param x2 Ending x-coordinate
     * @param y2 Ending y-coordinate
     */
    public void drawLine(int x1, int y1, int x2, int y2)
    {
        x1 += offset.getX();
        y1 += offset.getY();
        x2 += offset.getX();
        y2 += offset.getY();

        gl.startLines();
        gl.vertex(x1, y1);
        gl.vertex(x2, y2);
        gl.end();
    }


    /**
     * Translates the offset (the origin) by a certain amount.
     * 
     * @param x How much to move the offset right.
     * @param y How much to move the offset down.
     */
    public void translate(int x, int y)
    {
        offset.translate(x, y);
    }

    /**
     * Moves the offset (the origin) back to the top left.
     */
    public void resetTransformations()
    {
        offset.setXY(0, 0);
    }

    /**
     * Returns the current clipspace.
     * 
     * @return The current clipspace.
     */
    public Rectangle getClipSpace()
    {
        return clipSpace;
    }

    /**
     * Draws a Triangle.
     * 
     * @param x1 the x value of the first point
     * @param y1 the y value of the first point
     * @param x2 the x value of the second point
     * @param y2 the y value of the second point
     * @param x3 the x value of the third point
     * @param y3 the y value of the third point
     * @param filled to fill the triangle or not
     */
    public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, boolean filled) {
    	x1 += offset.getX();
    	y1 += offset.getY();

    	x2 += offset.getX();
    	y2 += offset.getY();

    	x3 += offset.getX();
    	y3 += offset.getY();
    	
    	gl.startTriangles();
    	gl.vertex(x3, y3);
    	gl.vertex(x2, y2);
    	gl.vertex(x1, y1);
    	gl.end();
    }

    /**
     * Draws the outline of a rectangle using the currently set line width
     * and line stipple, if any. @todo Is 'Wire' really a good name for a 2D
     * graphics function? Surely we should have calls like drawRectangle and
     * drawFilledRectangle? #
     * 
     * @param x Left hand coordinate of rectangle
     * @param y Top side of rectangle
     * @param width Width of rectangle
     * @param height Height of rectangle
     */
    public void drawWireRectangle(int x, int y, int width, int height)
    {
    	// @todo This could be optimized  #
        drawLine(x, y, x + width - 1, y);
        drawLine(x, y, x, y + height - 1);
        drawLine(x + width - 1, y, x + width - 1, y + height - 1);
        drawLine(x, y + height - 1, x + width, y + height - 1);
    }

    /**
     * Draws a filled rectangle, where each corner can be a different colour. OpenGL
     * should smoothly blend each corner into the other automatically, so fancy smooth
     * shading effects are easy to perform.
     * 
     * @param x The left hand side of the rectangle.
     * @param y The top side of the rectangle.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     * @param c1 Lower left corner colour.
     * @param c2 Lower right corner colour.
     * @param c3 Upper right corner colour.
     * @param c4 Upper left corner colour.
     */
    public void drawBlendedFilledRect(int x, int y, int width, int height, Color c1, Color c2, Color c3, Color c4)
    {
        x += offset.getX();
        y += offset.getY();

        gl.startQuads();

        // lower left corner
        gl.color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha());
        gl.vertex(x, y);

        // lower right corner
        gl.color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha());
        gl.vertex(x + width, y);

        // upper right corner
        gl.color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha());
        gl.vertex(x + width, y + height);

        // upper left corner
        gl.color(c4.getRed(), c4.getGreen(), c4.getBlue(), c4.getAlpha());
        gl.vertex(x, y + height);
        gl.end();

    }


    /**
     * Draws a bevelled rectangle. @todo: Maybe bevelling should be a setting
     * that is enabled or disabled, then we can adjust other methods to provide
     * bevelling for all methods without having API bloat? In fact the same could
     * be done for fills as well. We could manage outline and fill state (off, bevel,
     * solid, blended, inset, outset, and so on) and then drawRectangle, etc, will
     * handle it automatically? #
     * 
     * @param x The left hand side of the rectangle.
     * @param y The top side of the rectangle.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     * @param bright The bright bevel colour
     * @param dark The dark bevel colour
     */
    public void drawBevelRectangle(int x, int y, int width, int height, Color bright, Color dark)
    {
        x += offset.getX();
        y += offset.getY();
        
        setColor(bright);
        
        // @todo: gl.startLineLoop would be better, but probs with blending#
        gl.startLines();
        	gl.vertex(x, y);
        	gl.vertex(x + width, y);
        	
        	gl.vertex(x + width, y);
        	gl.vertex(x + width, y+height);
        	setColor(dark);
        	gl.vertex(x + width + 1, y+height);
        	gl.vertex(x, y + height);
        	gl.vertex(x, y + height);
        	gl.vertex(x, y);
        gl.end();

    }


    /**
     * Draws a bevelled circle.
     * 
     * @param x Centre of circle, X
     * @param y Centre of circle, Y
     * @param radius Radius of circle
     * @param light Bright bevel colour
     * @param dark Dark bevel colour
     */
    public void drawBevelCircle(int x, int y, double radius, Color light, Color dark)
    {
        final double TWO_PI = Math.PI * 2.0;
        x += offset.getX();
        y += offset.getY();

        // should be light between 135 (3*pi/4) and 305 (-pi/4) degrees
        setColor(light);
        
        gl.startLineLoop();

        // therefore you should start drawing at one or the other
        gl.vertex((int) radius + x, 0 + y);

        // then do this twice for each half of the bevelled circle
        // unless you want to smoothly change bevel shading of course
        // in which case start rendering at -3pi/8 at bright bevel, and
        // blend at each vertex around to pi/8 at dark devel
        for (double d = 0; d <= TWO_PI; d = d + (TWO_PI / 18))
        {
            gl.vertex((int) (Math.cos(d) * radius + x + 0.5), (int) (Math.sin(d) * radius + y + 0.5));
        }
        gl.end();
    }
    
    public void drawPixel(int x, int y)
    {
    	
    }
    
    /**
     * Draws an arc with center x,y with radius r between angle a1 and angle a2
     * 
     * @param x
     *            The x coordinate of the centre of the arc
     * @param y
     *            The y coordinate of the centre of the arc
     * @param a1
     *            The starting angle
     * @param a2
     *            The ending angle
     * @param radius
     *            The radius to render
     * @param resolution
     *            The resolution (number of steps to the rounded corner)
     */
    public void drawRoundedCorner(int x, int y, double a1, double a2, double radius, int resolution)
    {
        if (a2 < a1)
        {
            double t = a1;
            a1 = a2;
            a2 = t;
        }

        double step = a2 - a1 / resolution;

        x += offset.getX();
        y += offset.getY();

        gl.startLineLoop();

        for (double d = a1; d <= a2; d += step)
        {
            gl.vertex((int) (Math.cos(d) * radius + x + 0.5), (int) (Math.sin(d) * radius + y + 0.5));
        }
        gl.end();
    }

    /**
     * Draws a rounded rectangle (not filled). A rounded rectange is like a
     * rectangle, with the corners smoothed. The algorithm used is quite basic.
     * 
     * @param x
     *            X coordinate of left hand side of rectangle
     * @param y
     *            Y coordinate of top of rectangle
     * @param width
     *            The width of the rectangle
     * @param height
     *            The height of the rectangle
     * @param radius
     *            The radius of the corner smoothing
     */
    public void drawRoundedRectangle(int x, int y, int width, int height, int radius)
    {
        double top = Math.PI * 1.5;
        double right = 0.0d;
        double bottom = Math.PI * 0.5;
        double left = Math.PI;

        drawRoundedCorner(x + radius, y + radius, left, top, (double) radius, 4);
        drawLine(x + radius, y, x + width - radius - 1, y);
        drawRoundedCorner(x + radius, y + radius, top, right, (double) radius, 4);
        drawLine(x + width - 1, y + radius, x + width - 1, y + height - radius - 1);
        drawRoundedCorner(x + radius, y + radius, right, bottom, (double) radius, 4);
        drawLine(x + width - radius - 1, y + height - 1, x + radius, y + height - 1);
        drawRoundedCorner(x + radius, y + radius, bottom, left, (double) radius, 4);
        drawLine(x, y + height - radius - 1, x, y + radius);
    }

}
