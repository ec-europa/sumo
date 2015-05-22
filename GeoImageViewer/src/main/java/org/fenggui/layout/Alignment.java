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
 * Created on 26th October 2005
 * $Id: Alignment.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.layout;

import org.fenggui.theme.xml.DefaultElementName;
import org.fenggui.theme.xml.EncodingException;
import org.fenggui.theme.xml.StorageFormat;
import org.fenggui.util.Dimension;
import org.fenggui.util.Point;

/**
 * There are nine possible basic alignments, the four corners, the four edges,
 * and the middle. This enum also includes utility methods for aligning a point
 * within a larger box, and a box within a larger box.
 * 
 * @author Graham Briggs, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
@DefaultElementName("Alignment")
public enum Alignment
{
    TOP_LEFT(0.0, 1.0)
    {
        public Point align(Point o, Dimension s)
        {
            return new Point(o.getX(), o.getY() + s.getHeight() - 1);
        }

        public Point alignBox(Point o, Dimension s, Dimension b)
        {
            return new Point(o.getX(), o.getY() + s.getHeight() - b.getHeight());
        }
        
    },

    TOP(0.5, 1.0)
    {
        public Point align(Point o, Dimension s)
        {
            return new Point(o.getX() + (s.getWidth() - 1) / 2, o.getY() + s.getHeight() - 1);
        }

        public Point alignBox(Point o, Dimension s, Dimension b)
        {
            return new Point(o.getX() + (s.getWidth() - b.getWidth()) / 2, o.getY() + s.getHeight() - b.getHeight());
        }        
    },

    TOP_RIGHT(1.0, 1.0)
    {
        public Point align(Point o, Dimension s)
        {
            return new Point(o.getX() + s.getWidth() - 1, o.getY() + s.getHeight() - 1);
        }

        public Point alignBox(Point o, Dimension s, Dimension b)
        {
            return new Point(o.getX() + s.getWidth() - b.getWidth(), o.getY() + s.getHeight() - b.getHeight());
        }
    },

    LEFT(0.0, 0.5)
    {
        public Point align(Point o, Dimension s)
        {
            return new Point(o.getX(), o.getY() + (s.getHeight() - 1) / 2);
        }

        public Point alignBox(Point o, Dimension s, Dimension b)
        {
            return new Point(o.getX(), o.getY() + (s.getHeight() - b.getHeight()) / 2);
        }
    },

    MIDDLE(0.5, 0.5)
    {
        public Point align(Point o, Dimension s)
        {
            return new Point(o.getX() + (s.getWidth() - 1) / 2 , o.getY() + (s.getHeight() - 1) / 2);
        }

        public Point alignBox(Point o, Dimension s, Dimension b)
        {
            return new Point(o.getX() + (s.getWidth() - b.getWidth()) / 2, 
                             o.getY() + (s.getHeight() - b.getHeight()) / 2);
        }
    },

    RIGHT(1.0, 0.5)
    {
        public Point align(Point o, Dimension s)
        {
            return new Point(o.getX() + s.getWidth() - 1, o.getY() + (s.getHeight() - 1) / 2);
        }

        public Point alignBox(Point o, Dimension s, Dimension b)
        {
            return new Point(o.getX() + s.getWidth() - b.getWidth(), o.getY() + (s.getHeight() - b.getHeight()) / 2);
        }
    },

    BOTTOM_LEFT(0.0, 0.0)
    {
        public Point align(Point o, Dimension s)
        {
            return new Point(o.getX(), o.getY());
        }

        public Point alignBox(Point o, Dimension s, Dimension b)
        {
            return new Point(o.getX(), o.getY());
        }
    },

    BOTTOM(0.5, 0.0)
    {
        public Point align(Point o, Dimension s)
        {
            return new Point(o.getX() + (s.getWidth() - 1) / 2, o.getY());
        }

        public Point alignBox(Point o, Dimension s, Dimension b)
        {
            return new Point(o.getX() + (s.getWidth() - b.getWidth()) / 2, o.getY());
        }
    },

    BOTTOM_RIGHT(1.0, 0.0)
    {
        public Point align(Point o, Dimension s)
        {
            return new Point(o.getX() + s.getWidth() - 1, o.getY());
        }

        public Point alignBox(Point o, Dimension s, Dimension b)
        {
            return new Point(o.getX() + s.getWidth() - b.getWidth(), o.getY());
        }
    };
	
	public static final StorageFormat<Alignment, String> STORAGE_FORMAT =
		new StorageFormat<Alignment, String>(){

			public String encode(Alignment obj) throws EncodingException
			{
				switch(obj)
				{
				case LEFT: return "left";
				case RIGHT: return "right";
				case BOTTOM: return "bottom";
				case TOP: return "top";
				case MIDDLE: return "middle";
				case TOP_LEFT: return "top left";
				case TOP_RIGHT: return "top right";
				case BOTTOM_LEFT: return "bottom left";
				case BOTTOM_RIGHT: return "bottom right";
				default: return "left";
				}
			}

			public Alignment decode(String encodedObj) throws EncodingException
			{
				if(encodedObj.equalsIgnoreCase("left")) return LEFT;
				else if(encodedObj.equalsIgnoreCase("right")) return RIGHT;
				else if(encodedObj.equalsIgnoreCase("bottom")) return BOTTOM;
				else if(encodedObj.equalsIgnoreCase("top")) return TOP;
				else if(encodedObj.equalsIgnoreCase("middle")) return MIDDLE;
				else if(encodedObj.equalsIgnoreCase("top left")) return TOP_LEFT;
				else if(encodedObj.equalsIgnoreCase("top right")) return TOP_RIGHT;
				else if(encodedObj.equalsIgnoreCase("bottom left")) return BOTTOM_LEFT;
				else return BOTTOM_RIGHT;
			}};
	
    /**
     * How far from the left this alignment is
     */
    private double along;

    /**
     * How far from the bottom this alignment is
     */
    private double up;
    
    
    Alignment(double along, double up)
    {
        this.along = along;
        this.up = up;
    }

    /**
     * Returns how far from the left this alignment is.
     * 
     * @return A number between 0 and 1 saying how far from the bottom this
     *         alignment is.
     */
    public double fromLeft()
    {
        return along;
    }

    /**
     * Returns how far from the bottom this alignment is.
     * 
     * @return A number between 0 and 1 saying how far from the bottom this
     *         alignment is.
     */
    public double fromBottom()
    {
        return up;
    }

    /**
     * This calculates the point position for an alignment. As we will be
     * working on a screen, we will assume that the point is a 1x1 sized
     * element in order to get top and right alignments looking correctly
     * on screen.
     * 
     * @param origin
     *            The bottom-left coordinate of the containing box.
     * @param size
     *            The size of the containing box.
     * @return A 2D point for that alignment.
     */
    public abstract Point align(Point origin, Dimension size);

    public int alignX(int availableWidth, int width) {
    	return (int)(fromLeft()*availableWidth - fromLeft()*width);
    }
    
    public int alignY(int availableHeight, int height) {
    	return (int)(fromBottom()*availableHeight - fromBottom()*height);
    }
    
    /**
     * Utility method that calculates the coordinate of the bottom-left side of
     * a box that you are aligning within a larger box.
     * 
     * @param origin
     *            The bottom-left coordinate of the containing box.
     * @param size
     *            The size of the containing box.
     * @param box
     *            The size of the box you are aligning within.
     * @return The coordinate that we can start rendering at.
     */
    public abstract Point alignBox(Point origin, Dimension size, Dimension box);

}
