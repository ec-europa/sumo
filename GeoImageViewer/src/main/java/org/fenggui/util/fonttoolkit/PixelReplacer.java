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
 * Created on Jan 30, 2006
 * $Id: PixelReplacer.java 240 2007-03-28 14:19:32Z bbeaulant $
 */
package org.fenggui.util.fonttoolkit;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.image.BufferedImage;

/**
 * 
 * 
 * @todo Comment this class... #
 * 
 * @author Johannes Schaback, last edited by $Author: bbeaulant $, $Date: 2007-03-28 16:19:32 +0200 (Mi, 28 Mär 2007) $
 * @version $Revision: 240 $
 */
public class PixelReplacer extends RenderStage {

	private Paint p;
	private Color matchColor = null;
	
	public PixelReplacer(Paint paint, Color matchColor) {
		p = paint;
		this.matchColor = matchColor;
	}
	
	public void renderChar(FontMetrics fontMetrics, BufferedImage image, char c, int safetyMargin) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setPaint(p);
		for(int x=0; x<image.getWidth(); x++) {
			for(int y=0; y<image.getHeight(); y++) {
				if(INT_ARGBequals(image.getRGB(x, y), matchColor)) {
					g.fillRect(x, y, 1, 1);
				}
			}
		}
	}

	/**
	 * Compares the RGB values of the two arguments. Note that it does not
	 * compare the alpha values!
	 * 
	 * @param argb argb value from the image
	 * @param color the color to compare it with
	 * @return true if red, green and blue components are equal, false otherwise
	 */
	public static boolean INT_ARGBequals(int argb, Color color) {
		int blue = 0x000000FF & argb;
		int green = (0x0000FF00 & argb) >> 8;
		int red = (0x00FF0000 & argb) >> 16;
		//int alpha = (0xFF000000 & argb) >> 24;
		
		return color.getRed() == red &&
			color.getBlue() == blue && 
			color.getGreen() == green;
	}
	
	public static boolean INT_ARGBhasColor(int argb) {
		int blue = 0x000000FF & argb;
		int green = (0x0000FF00 & argb) >> 8;
		int red = (0x00FF0000 & argb) >> 16;
		
		return red != 0 && green != 0 && blue != 0;
	}
}
