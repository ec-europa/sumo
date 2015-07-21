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
 * Created on 2005-4-13
 * $Id: Point.java 334 2007-08-12 12:06:38Z Schabby $
 */
package org.fenggui.util;

import org.lwjgl.util.WritablePoint;

/**
 * Implementation of a point. This class is read-only.
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-12 14:06:38 +0200 (So, 12 Aug 2007) $
 * @version $Revision: 334 $
 * @see WritablePoint 
 */
public class Point
{

    int x, y;
    
    /**
     * Creates a new point.
     * 
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     */
    public Point(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Returns the x coordinate of the point
     * @return x value
     */
    public int getX() {
        return x;
    }
    
    /**
     * Returns the y coordinate of this point.
     * @return y value
     */
    public int getY() {
        return y;
    }
    
    /**
     * Returns a formated string container the x and y coordinate of this point
     * @return string
     */
    public String toString() {
        return "("+x+", "+y+")";
    }
    

	/**
	 * Sets the x coordinate of this point.
	 * 
	 * @param x
	 *            x coodinate
	 */
	public void setX(int x)
	{
		this.x = x;
	}

	/**
	 * Sets the y coordinate of this point.
	 * 
	 * @param y
	 *            y coordinate
	 */
	public void setY(int y)
	{
		this.y = y;
	}

	/**
	 * Sets the x and y coordinate of this point.
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordiate
	 */
	public void setXY(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Translates this point by the given deltas.
	 * 
	 * @param x
	 *            value to add on current x coordinate
	 * @param y
	 *            value to add on current y coordinate
	 */
	public void translate(int x, int y)
	{
		this.x += x;
		this.y += y;
	}
    
}
