/*
 * FengGUI - Java GUIs in OpenGL (http://www.fenggui.org)
 * 
 * Copyright (C) 2005-2007 FengGUI Project
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
 * Created on June 13, 2007
 * $Id: SVGImageFactory.java 294 2007-06-13 15:42:59Z whackjack $
 */
package org.fenggui.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

public final class SVGImageFactory
{
	
	public static BufferedImage createSVGImage(String filename, int width, int height) throws Exception
	{
		File file = new File(filename);
		FileInputStream fis = new FileInputStream(file);
		SVGUniverse universe = new SVGUniverse();
		universe.loadSVG(fis, file.toURI().toString());
		SVGDiagram diagram = universe.getDiagram(file.toURI());
		diagram.setIgnoringClipHeuristic(true);
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.transform(getAffineTransform((int)diagram.getWidth(), (int)diagram.getHeight(), width, height));
		diagram.render(g);
		g.dispose();
		return image;
	}
	
	private static AffineTransform getAffineTransform(int srcWidth, int srcHeight, int destWidth, int destHeight)
	{
		double scaleX = (double)destWidth / (double)srcWidth;
		double scaleY = (double)destHeight / (double)srcHeight;
		double scale = Math.min(scaleX, scaleY);
		return AffineTransform.getScaleInstance(scale, scale);
	}
	
}
