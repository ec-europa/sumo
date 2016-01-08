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
 * Created on October 25th 2005
 * $Id: Spacing.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.util;

import java.io.IOException;

import org.fenggui.theme.xml.DefaultElementName;
import org.fenggui.theme.xml.IXMLStreamable;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * This class defines four 1D size attributes, known as left, right, top and
 * bottom. This is a utility class for the layout and rendering system that is
 * utilising CSS style attributes for widgets, such as padding, borders, margins
 * and so on. As we are using CSS style spacing, we must be aware that cunning
 * layouts are often achieved by having negative values for various sides,
 * depending on the container setup.
 * 
 * 
 * Box has to catch if the spacing is changing (setBorder, setPadding, setMargin)
 * thus you have to create a new instance for a new spacing and use one
 * of these methods.
 * 
 * @author Graham Briggs, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
@DefaultElementName("Spacing")
public class Spacing implements IXMLStreamable
{
    private int top;

    private int left;

    private int right;

    private int bottom;

    public Spacing(InputOnlyStream stream) throws IOException, IXMLStreamableException
    {
    	this.process(stream);
    }
    
    /**
     * Default constructor will create a Spacing with zero spacing in all
     * directions.
     */
    public Spacing()
    {
        this(0, 0, 0, 0);
    }

    /**
     * This defines a spacing by two figures, where the first figure is the
     * spacing at the top and bottom, and the second figure is the spacing to
     * the left and right.
     * 
     * @param topbottom
     *            The top and bottom spacing.
     * @param leftright
     *            The left and right spacing.
     */
    public Spacing(int topbottom, int leftright)
    {
        this(topbottom, leftright, leftright, topbottom);
    }

    private void checkIntegrity(int value)
    {
    	if(value < 0) throw new IllegalArgumentException("spacing parameter < 0");
    }
    
    /**
     * Creates a new Spacing with the provides figures.
     * 
     * @param top
     *            The top spacing or sizing.
     * @param left
     *            The left spacing or sizing.
     * @param right
     *            The right spacing or sizing.
     * @param bottom
     *            The bottom spacign or sizing.
     */
    public Spacing(int top, int left, int right, int bottom)
    {
    	checkIntegrity(top);
    	checkIntegrity(left);
    	checkIntegrity(right);
    	checkIntegrity(bottom);
    	
        this.top = top;
        this.left = left;
        this.right = right;
        this.bottom = bottom;
    }

    public Spacing(Spacing c)
    {
    	setSpacing(c);
    }
    
    /**
     * Returns the top spacing or size.
     * 
     * @return The top spacing or size.
     */
    public int getTop()
    {
        return top;
    }

    /**
     * Returns the left spacing or size.
     * 
     * @return The left spacing or size.
     */
    public int getLeft()
    {
        return left;
    }

    /**
     * Returns the right spacing or size.
     * 
     * @return The right spacing or size.
     */
    public int getRight()
    {
        return right;
    }
    
    /**
     * Returns the bottom spacing or size.
     *
     * @return The bottom spacing or size.
     */
    public int getBottom()
    {
        return bottom;
    }
    
    public int getLeftPlusRight()
    {
    	return left + right;
    }
    
    public int getBottomPlusTop()
    {
    	return bottom + top;
    }
    
    /**
     * Sets the spacing.
     * 
     * @param top The spacing at the top.
     * @param left The spacing to the left.
     * @param right The spacing to the right.
     * @param bottom The spacing to the bottom.
     */
    protected void setSpacing(int top, int left, int right, int bottom)
    {
        this.top = top;
        this.left = left;
        this.right = right;
        this.bottom = bottom;
    }
    
    /**
     * This defines a spacing by two figures, where the first figure is the
     * spacing at the top and bottom, and the second figure is the spacing to
     * the left and right.
     * 
     * 
     * 
     * @param topbottom
     *            The top and bottom spacing.
     * @param leftright
     *            The left and right spacing.
     *            
     *
     *            
     */
    protected void setSpacing(int topbottom, int leftright) {
    	this.left = this.right = leftright;
    	this.bottom = this.top = topbottom;
    }
    
    protected void setSpacing(Spacing s) {
    	left = s.left;
    	right = s.right;
    	top = s.top;
    	bottom = s.bottom;
    }
    
    public static final Spacing ZERO_SPACING = new Spacing(0, 0, 0, 0);
    
    public String toString() {
    	return "(l: "+left+", r: "+right+", t: "+top+", b: "+bottom+")";
    }
	
	/* (non-Javadoc)
	 * @see org.fenggui.io.IOStreamSaveable#process(org.fenggui.io.InputOutputStream)
	 */
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		if(!stream.isInputStream() && top == left && left == right && right == bottom)
		{
			stream.processAttribute("all", left, 0);
			return;
		}
		
		top = left = right = bottom = stream.processAttribute("all", left, 0);
		
		// the attribute all="?" set the values already
		if(top != 0) return;
		
		top = stream.processAttribute("top", top, 0);
		bottom = stream.processAttribute("bottom", bottom, 0);
		left = stream.processAttribute("left", left, 0);
		right = stream.processAttribute("right", right, 0);
	}

	/* (non-Javadoc)
	 * @see org.fenggui.io.IOStreamSaveable#getUniqueName()
	 */
	public String getUniqueName() {
		return GENERATE_NAME;
	}
}
