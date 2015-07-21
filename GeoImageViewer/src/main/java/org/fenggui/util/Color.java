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
 * Created on Apr 19, 2005
 * $Id: Color.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.util;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import org.fenggui.theme.xml.IXMLStreamable;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.theme.xml.MalformedElementException;

/**
 * Implementation of a color value (RGBA). Colors are 
 * defined by the four color components values ranging from 0 to 1: 
 * Red, Green, Blue and Alpha. <br/>
 * An alpha value of 1 means that the color is opaque. An alpha value of
 * 0 is translucent. Note that FengGUI uses float values for the color values.
 * 
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $ 
 * @version $Revision: 327 $
 * 
 */
public class Color implements IXMLStreamable
{
	private static final NumberFormat PERCENTAGE_FORMAT =
		new DecimalFormat("##%");
	
	/**
	 * The color components. Alpha's default 1 ()
	 */
	private float red, green, blue, alpha = 1f;

	/**
	 * Completely translucent color. It is like painting a wall with water :)
	 */
	public static final Color OPAQUE = new Color(0f, 0f, 0f, 0f);

	/**
	 * The color white.
	 */
	public static final Color WHITE = new Color(1f, 1f, 1f);

	/**
	 * The color black.
	 */
	public static final Color BLACK = new Color(0f, 0f, 0f);

	/**
	 * The color black with an alpha value of 0.5 
	 */
	public static final Color BLACK_HALF_OPAQUE = new Color(0f, 0f, 0f, 0.5f);

	/**
	 * The color green.
	 */
	public static final Color GREEN = new Color(0, 128, 0);

	/**
	 * The color dark green.
	 */
	public static final Color DARK_GREEN = new Color(0, 100, 0);

	/**
	 * The color light green.
	 */
	public static final Color LIGHT_GREEN = new Color(144, 238, 144);

	/**
	 * The color red.
	 */
	public static final Color RED = new Color(1f, 0f, 0f);

	/**
	 * The color light red.
	 */
	public static final Color LIGHT_RED = new Color(1f, 0.5f, 0.5f);

	/**
	 * The color dark red.
	 */
	public static final Color DARK_RED = new Color(139, 0, 0);

	/**
	 * The color blue.
	 */
	public static final Color BLUE = new Color(0f, 0f, 1);

	/**
	 * The color light blue
	 */
	public static final Color LIGHT_BLUE = new Color(173, 216, 230);

	/**
	 * The color dark blue
	 */
	public static final Color DARK_BLUE = new Color(0, 0, 139);

	/**
	 * The color yellow
	 */
	public static final Color YELLOW = new Color(1f, 1f, 0f);

	/**
	 * The color light yellow
	 */
	public static final Color LIGHT_YELLOW = new Color(255, 255, 224);

	/**
	 * The color dark yellow
	 */
	public static final Color DARK_YELLOW = new Color(0.5f, 0.5f, 0f);

	/**
	 * The color magenta
	 */
	public static final Color MAGENTA = new Color(1f, 0f, 1f);

	/**
	 * The color light magenta
	 */
	public static final Color LIGHT_MAGENTA = new Color(1f, 0.5f, 1f);

	/**
	 * The color dark magenta
	 */
	public static final Color DARK_MAGENTA = new Color(139, 0, 139);

	/**
	 * The color cyuan
	 */
	public static final Color CYAN = new Color(0f, 1f, 1f);

	/**
	 * The color light cyuan
	 */
	public static final Color LIGHT_CYUAN = new Color(224, 255, 255);

	/**
	 * The color dark cyuan
	 */
	public static final Color DARK_CYUAN = new Color(0, 139, 139);

	/**
	 * The color gray
	 */
	public static final Color GRAY = new Color(0.5f, 0.5f, 0.5f);

	/**
	 * The color light gray
	 */
	public static final Color LIGHT_GRAY = new Color(211, 211, 211);

	/**
	 * The color dark gray
	 */
	public static final Color DARK_GRAY = new Color(169, 169, 169);

	/**
	 * The color white with an alpha value of 0.5
	 */
	public static final Color WHITE_HALF_OPAQUE = new Color(1f, 1f, 1f, 0.5f);
	
	
	public static Map<String, Color> colorMap = new HashMap<String, Color>();
	
	static 
	{
		colorMap.put("blue", BLUE);
		colorMap.put("green", GREEN);
		colorMap.put("red", RED);
		colorMap.put("yellow", YELLOW);
		colorMap.put("magenta", MAGENTA);
		colorMap.put("white", WHITE);
		colorMap.put("black", BLACK);
		colorMap.put("gray", GRAY);
		colorMap.put("light blue", LIGHT_BLUE);
		colorMap.put("light yellow", LIGHT_YELLOW);
		colorMap.put("light magneta", LIGHT_MAGENTA);
		colorMap.put("light gray", LIGHT_GRAY);
	}
	

	/**
	 * Creates a new Color object.
	 * @param red the red component (0..1)
	 * @param green the green component (0..1)
	 * @param blue the blue component (0..1)
	 */
	public Color(float red, float green, float blue)
	{
		this.red = red;
		this.green = green;
		this.blue = blue;
		checkDomain();
	}


	/**
	 * Creates a new Color object.
	 * @param red the red component (0..1)
	 * @param green the green component (0..1)
	 * @param blue the blue component (0..1)
	 * @param alpha the alpha (0..1)
	 */
	public Color(float red, float green, float blue, float alpha)
	{
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
		checkDomain();
	}


	/**
	 * Copy Constructor
	 * @param value the color to copy
	 */
	public Color(Color value)
	{
		this.red = value.red;
		this.green = value.green;
		this.blue = value.blue;
	}

	public Color(InputOnlyStream stream) throws IOException, IXMLStreamableException
	{
		process(stream);
	}
	
	/**
	 * Creates a new Color object
	 * @param red the red component (0...255)
	 * @param green the green component (0...255)
	 * @param blue the blue component (0...255)
	 * @param alpha the alpah component (0...255)
	 */
	public Color(int red, int green, int blue, int alpha) {
		this.red =  red / 255.0f;
		this.green = green / 255.0f;
		this.blue = blue / 255.0f;
		this.alpha = alpha / 255.0f;
		checkDomain();
	}
	
	/**
	 * Creates a new Color object
	 * @param red the red component (0...255)
	 * @param green the green component (0...255)
	 * @param blue the blue component (0...255)
	 */
	public Color(int red, int green, int blue) {
		this.red =  red / 255.0f;
		this.green = green / 255.0f;
		this.blue = blue / 255.0f;
		checkDomain();
	}

	/**
	 * Creates a random color with alpha set to 1.
	 * @return new color with randomly chosen RGB values.
	 */
	public static Color random()
	{
		return new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
	}


	/**
	 * Makes sure that the color component values are in the range of 0..1.
	 * If they are not, they are set to the closest, valid value.
	 */
	private void checkDomain()
	{
		if (red > 1f) red = 1f;
		if (green > 1f) green = 1f;
		if (blue > 1f) blue = 1f;
		if (alpha > 1f) alpha = 1f;

		if (red < 0f) red = 0f;
		if (green < 0f) green = 0f;
		if (blue < 0f) blue = 0f;
		if (alpha < 0f) alpha = 0f;
	}


	/**
	 * Returns a darker variant of this color. Subtracts 0.2 from each
	 * component
	 * @return new darker color
	 */
	public Color darker()
	{
		return darker(0.2f);
	}


	/**
	 * Returns a brigher variant of this color. Adds 0.2 to each color
	 * component
	 * @return new brighter color
	 */
	public Color brighter()
	{
		return brighter(0.2f);
	}


	/**
	 * Returns a darker variant of this color.
	 * @param step the value to subtract from the color components (not alpha)
	 * @return new darker color
	 */
	public Color darker(float step)
	{
		return new Color(red - step, green - step, blue - step, alpha);
	}


	/**
	 * Return a brighter variant of this color.
	 * @param step the value to add to all color components (not alpha)
	 * @return new brigher color
	 */
	public Color brighter(float step)
	{
		return new Color(red + step, green + step, blue + step, alpha);
	}


	/**
	 * Returns the red component of this color value
	 * @return red component
	 */
	public float getRed()
	{
		return red;
	}


	/**
	 * Returns the green component of this color value
	 * @return green component
	 */
	public float getGreen()
	{
		return green;
	}


	/**
	 * Returns the blue component of the 
	 * @return blue component
	 */
	public float getBlue()
	{
		return blue;
	}

	public boolean equals(Color c)
	{
		return alpha == c.alpha && blue == c.blue && red == c.red && green == c.green;
	}


	/**
	 * Returns the alpha value of this color value.
	 * @return alpha value
	 */
	public float getAlpha()
	{
		return alpha;
	}

	private void set(Color c)
	{
		this.red = c.red;
		this.blue = c.blue;
		this.green = c.green;
		this.alpha = c.alpha;
	}

	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		// The value might be stored as value="r g b a"
		
		if (stream.isInputStream())
		{
			
			String rgbaStr = stream.processAttribute("rgba", "", null);
			if (rgbaStr != null && !rgbaStr.equals(""))
			{
				String[] s = rgbaStr.split(",");
				
				if(s.length != 4) throw new MalformedElementException("the rgba attribute requires 4 elements, not "+s.length+"!");
				
				red = (float)Integer.parseInt(s[0].trim())/255f;
				green = (float)Integer.parseInt(s[1].trim())/255f;
				blue = (float)Integer.parseInt(s[2].trim())/255f;
				alpha = (float)Integer.parseInt(s[3].trim())/255f;
				return;
			}
			
			String value = stream.processAttribute("value", "blue", "no value");
			
			if(!value.equals("no value"))
			{
				Color color = colorMap.get(value);
				
				if (color == null) {
					throw new MalformedElementException("Color value "+value+
							" not recognized!");
				}
				
				set(color);
				return;
			}
		}
		else // if writing out!
		{
			for (String key: colorMap.keySet()) 
			{
				if (this.equals(colorMap.get(key))) 
				{
					stream.processAttribute("value", key);
					return;
				}
			}
		}
		
		// If it's not, the color components should be stored separately
		red = stream.processAttribute("red", red);
		green = stream.processAttribute("green", green);
		blue = stream.processAttribute("blue", blue);
		alpha = stream.processAttribute("alpha", alpha, 1.0f);
	}
	
	public String toString()
	{
		return red+", "+green+", "+blue+", "+alpha;
	}
	
	
	public boolean equalsIgnoreAlpha(Color c) {
		return blue == c.blue && red == c.red && green == c.green;
	}

	/* (non-Javadoc)
	 * @see org.fenggui.io.IOStreamSaveable#getUniqueName()
	 */
	public String getUniqueName() {
		for (String key: colorMap.keySet()) 
		{
			Color c = colorMap.get(key);
			
			if (this.equals(c)) 
			{
				return null;
			}
			
			if (this.equalsIgnoreAlpha(c)) 
			{
				return PERCENTAGE_FORMAT.format(alpha) + " opaque " +
					key;
			}
		}
		
		// TODO Maybe generate some extra names
		
		return GENERATE_NAME;
	}
}
