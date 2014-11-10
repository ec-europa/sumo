/*
 * FengGUI - Java GUIs in OpenGL (http://fenggui.sf.net)
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
 * Created on 2 oct. 06
 * $Id: TextStyle.java 33 2006-10-05 10:57:29Z bbeaulant $
 */

package org.fenggui.text;

import org.fenggui.render.Font;
import org.fenggui.util.Color;

/**
 * Encapulates a Font and a Color
 * 
 * @author Boris Beaulant, last edited by $Author: bbeaulant $, $Date: 2006-10-05 12:57:29 +0200 (Thu, 05 Oct 2006) $
 * @version $Revision: 33 $
 */
public class TextStyle
{
	private Font font;
	private Color color;


	/**
	 * TextStyle constructor
	 *
	 * @param font
	 * @param color
	 */
	public TextStyle(Font font, Color color)
	{
		super();
		this.font = font;
		this.color = color;
	}


	/**
	 * @return color
	 */
	public Color getColor()
	{
		return color;
	}


	/**
	 * @param color to set
	 */
	public void setColor(Color color)
	{
		this.color = color;
	}


	/**
	 * @return font
	 */
	public Font getFont()
	{
		return font;
	}


	/**
	 * @param font to set
	 */
	public void setFont(Font font)
	{
		this.font = font;
	}


}
