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
 * Created on Jan 31, 2006
 * $Id: Convolution.java 28 2006-10-05 01:37:07Z schabby $
 */
package org.fenggui.util.fonttoolkit;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 * Performs a kernel convulition on the character image.
 * 
 * 
 * Normalization can darken the image.
 * 
 * @author Johannes Schaback, last edited by $Author: schabby $, $Date: 2006-10-05 03:37:07 +0200 (Thu, 05 Oct 2006) $
 * @version $Revision: 28 $
 */
public class Convolution extends RenderStage {
	
	private Kernel kernel = null;
	private boolean normalize = false;
	
	public Convolution(Kernel kernel, boolean normalize) {
		this.kernel = kernel;
		this.normalize = normalize;
	}
	
	public void renderChar(FontMetrics fontMetrics, BufferedImage image, char c, int safetyMargin) {
		double[][] red = new double[image.getWidth()][image.getHeight()];
		double[][] green = new double[image.getWidth()][image.getHeight()];
		double[][] blue = new double[image.getWidth()][image.getHeight()];
		double[][] alpha = new double[image.getWidth()][image.getHeight()];
		
		ColorModel cm = image.getColorModel();
		
		for(int x=kernel.getWidth()/2; x < image.getWidth()-kernel.getWidth()/2; x++) {
			for(int y=kernel.getHeight()/2; y < image.getHeight()-kernel.getHeight()/2; y++) {
				
				for(int j=0 ; j<kernel.getWidth(); j++) {
					for(int k=0; k<kernel.getHeight(); k++) {
						
						int rgb = image.getRGB(
								x+j-kernel.getWidth()/2,
								y+k-kernel.getHeight()/2);
						
						double value = kernel.getValue(j, k);
						
						red[x][y] += cm.getRed(rgb)*value;
						green[x][y] += cm.getGreen(rgb)*value;
						blue[x][y] += cm.getBlue(rgb)*value;
						alpha[x][y] += cm.getAlpha(rgb)*value;
					}
				}
			}
		}
		
		if(normalize) {
			normalize(red, green, blue, alpha);
		}
		
		Graphics2D g = image.createGraphics();
		Clear.clear(g, image.getWidth(), image.getHeight());
		
		
		for(int x=0; x < image.getWidth(); x++) {
			for(int y=0; y < image.getHeight(); y++) {
				if(red[x][y] > 255) red[x][y] = 255;
				if(green[x][y] > 255) green[x][y] = 255;
				if(blue[x][y] > 255) blue[x][y] = 255;
				if(alpha[x][y] > 255) alpha[x][y] = 255;
				
				if(red[x][y] < 0) red[x][y] = 0;
				if(green[x][y] < 0) green[x][y] = 0;
				if(blue[x][y] < 0) blue[x][y] = 0;
				if(alpha[x][y] < 0) alpha[x][y] = 0;
				
				// @todo optimize drawing the image with image.setRGB #
				Color color = new Color((int)red[x][y], (int)green[x][y], (int)blue[x][y], (int)alpha[x][y]);
				g.setColor(color);
				g.drawLine(x, y, x, y);
			}
		}
		
	}

	private void normalize(double[][] red, double[][] green, double[][] blue, double[][] alpha) {
		
		double max = 0;
		double min = 0;
		
		for(int x=0; x < red.length; x++) {
			for(int y=0; y < red[0].length; y++) {
				
				max = Math.max(red[x][y], max);
				max = Math.max(green[x][y], max);
				max = Math.max(blue[x][y], max);
				max = Math.max(alpha[x][y], max);
				
				min = Math.min(red[x][y], min);
				min = Math.min(green[x][y], min);
				min = Math.min(blue[x][y], min);
				min = Math.min(alpha[x][y], min);
			}
		}
		
		
		
		double scale = 255.0/(max-min);
		
		//System.out.println("max: "+max+"  "+min+"\t"+scale);
		
		for(int x=0; x < red.length; x++) {
			for(int y=0; y < red[0].length; y++) {
				
				//System.out.println("prev "+red[x][y]);
				
				red[x][y] = red[x][y]*scale - min;
				green[x][y] = green[x][y]*scale - min;
				blue[x][y] = blue[x][y]*scale - min;
				alpha[x][y] = alpha[x][y]*scale - min;
				
				//System.out.println("after "+red[x][y]);
				
			}
		}

	}
	
}
