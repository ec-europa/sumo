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
 * $Id: DrawCharacter.java 240 2007-03-28 14:19:32Z bbeaulant $
 */
package org.fenggui.util.fonttoolkit;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Renders a character using an AWT font.
 * 
 *  
 * @author Johannes Schaback, last edited by $Author: bbeaulant $, $Date: 2007-03-28 16:19:32 +0200 (Mi, 28 Mär 2007) $
 * @version $Revision: 240 $
 */
public class DrawCharacter extends RenderStage {

	private Color renderColor = Color.RED;
	private boolean antialiasing = false;
	int[] pixel = new int[4];
	
	/**
	 * Instantiates a new <code>DrawCharacter</code> object.
	 * 
	 * @param color the color to draw the character with
	 * @param antialiasing flag indicating whether the character shall be drawn with anti-aliasing enabled
	 */
	public DrawCharacter(Color color, boolean antialiasing) 
	{
		renderColor = color;
		pixel[0] = renderColor.getRed();
		pixel[1] = renderColor.getGreen();
		pixel[2] = renderColor.getBlue();
		this.antialiasing = antialiasing;
	}
	
	/**
	 * Renders a character in the buffer.
	 */
	public void renderChar(FontMetrics fontMetrics, BufferedImage image, char c, int safetyMargin) 
	{
		Graphics2D g;
		BufferedImage tmpImage = null;
		if (antialiasing) {
			tmpImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

			g = (Graphics2D) tmpImage.getGraphics();
		
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, tmpImage.getWidth(), tmpImage.getHeight());
			g.setColor(Color.WHITE);
		
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		} else {
			g = (Graphics2D) image.getGraphics();
			g.setColor(renderColor);
		}
		
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		
//		g.setColor(Color.RED);
//		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		
		g.setFont(fontMetrics.getFont());
		
		// drawString renders the characters at the baseline!
		g.drawString(Character.toString(c), 
				0,
				fontMetrics.getMaxAscent());
		
		if (antialiasing) {
			for(int x=0; x<image.getWidth(); x++) {
				for(int y=0; y<image.getHeight(); y++) {
					pixel[3] = tmpImage.getRaster().getSample(x, y, 0);
					if (pixel[3] != 0) {
						image.getRaster().setPixel(x, y, pixel);
					}
				}
			}
		}
//		FontFactory.saveImageToDisk(image, "CharImage-"+Character.getNumericValue(c)+".png");
		//image.getHeight()/2+(fontMetrics.getDescent() + fontMetrics.getAscent())/2-fontMetrics.getDescent()
	}

}
