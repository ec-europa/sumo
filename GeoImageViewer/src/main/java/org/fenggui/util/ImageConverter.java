/*
 * FengGUI - Java GUIs in OpenGL (http://www.fenggui.org)
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
 * Created on Aug 11, 2007
 * $Id$
 */
package org.fenggui.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

public class ImageConverter
{
	
	public final static ComponentColorModel COLOR_MODEL = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8,8,8,8}, true, false, ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
	
	public static BufferedImage createGlCompatibleAwtImage(int width, int height)
	{
       	WritableRaster raster      = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 4, null);
       	BufferedImage convertImage = new BufferedImage(COLOR_MODEL, raster, false, new Hashtable());
		return convertImage;
	}
	
	public static boolean isGlCompatibleAwtImage(BufferedImage img)
	{
		if(img.getColorModel() != COLOR_MODEL) return false;
		if(!(img.getRaster().getDataBuffer() instanceof DataBufferByte)) return false;
		if(img.getRaster().getDataBuffer().getNumBanks() != 4) return false;
		return true;
	}
	
	public static boolean isPowerOf2(int value)
	{
		int i = 2;
        while (i < value) i *= 2;
        if(i == value) return true;
        return false; 
	}
	
	/**
	 * Returns the closest (greater or equals) power of 2 to the given value.
	 * @param value the value
	 * @return power of 2
	 */
	public static int powerOf2(int value)
	{
		if(value < 3) throw new IllegalArgumentException("value must be greater or equals 3!");
		int i = 2;
        while (i < value) i *= 2;
        return i; 
	}
	
	/**
	 * Converts the given image to a gl compatible format if necessary and returns the data in the format GL_RGBA as GL_UNSIGNED_BYTE.
	 * @param awtImage the image to be converted to an byte buffer
	 * @return nio buffer
	 */
	public static ByteBuffer convert(BufferedImage awtImage)
	{
		if(!isGlCompatibleAwtImage(awtImage))
		{
			BufferedImage convertImage = createGlCompatibleAwtImage(awtImage.getWidth(), awtImage.getHeight());
	        // copy the source image into the produced image
	        Graphics g = convertImage.getGraphics();
	        g.setColor(new Color(0f, 0f, 0f, 0f));
	        g.fillRect(0, 0, awtImage.getWidth(), awtImage.getHeight());
	        g.drawImage(awtImage, 0, 0, null);
	        awtImage = convertImage;
		}
		
        // build a byte buffer from the temporary image 
        // that be used by OpenGL to produce a texture.
        byte[] data = ((DataBufferByte) awtImage.getRaster().getDataBuffer()).getData(); 
        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(data.length); 
        imageBuffer.order(ByteOrder.nativeOrder()); 
        imageBuffer.put(data, 0, data.length); 
        imageBuffer.flip();
        
        return imageBuffer; 
	}
	
	/**
	 * Converts the given image to a gl compatible format if necessary and returns the data in the format GL_RGBA as GL_UNSIGNED_BYTE
	 * such that the width and height are a power of 2. The image is place in the upper left corner.
	 * @param awtImage the image to be converted to an byte buffer
	 * @return nio buffer
	 */
	public static ByteBuffer convertPowerOf2(BufferedImage awtImage)
	{
		if(!isGlCompatibleAwtImage(awtImage) || !isPowerOf2(awtImage.getWidth()) || !isPowerOf2(awtImage.getHeight()))
		{
			int width = powerOf2(awtImage.getWidth());
			int height = powerOf2(awtImage.getHeight());
			BufferedImage convertImage = createGlCompatibleAwtImage(width, height);
	        // copy the source image into the produced image
	        Graphics g = convertImage.getGraphics();
	        g.setColor(new Color(0f, 0f, 0f, 0f));
	        g.fillRect(0, 0, width, height);
	        g.drawImage(awtImage, 0, 0, null);
	        awtImage = convertImage;
		}
		
        // build a byte buffer from the temporary image 
        // that be used by OpenGL to produce a texture.
        byte[] data = ((DataBufferByte) awtImage.getRaster().getDataBuffer()).getData(); 
        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(data.length); 
        imageBuffer.order(ByteOrder.nativeOrder()); 
        imageBuffer.put(data, 0, data.length); 
        imageBuffer.flip();
        
        return imageBuffer; 
	}	
	

}
