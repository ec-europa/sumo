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
 * $Id: FontFactory.java 331 2007-08-11 15:20:05Z Schabby $
 */
package org.fenggui.util.fonttoolkit;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.fenggui.util.Alphabet;
import org.fenggui.util.CharacterPixmap;
import org.fenggui.util.ImageConverter;

/**
 * Utility class to create FengGUI fonts out of AWT fonts. Several <code>RenderStage<code>
 * objects need to be registered in the <code>FontFactory</code>s assembly line. Each stage is
 * then responsible for a processing step to render a character or modify its appearance.
 * 
 * 
 * @see RenderStage
 * @see AssemblyLine
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 17:20:05 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 331 $
 */
public class FontFactory {

	private Alphabet alphabet = null;
	private Font font = null;
	private FontMetrics fontMetrics = null;
	private int squarePixel = 0;
	final int safetyMargin = 4;
	private AssemblyLine assemblyLine = new AssemblyLine();

	/**
	 * Returns the AssemblyLine used to render characters.
	 * @return assembly line
	 */
	public AssemblyLine getAssemblyLine() 
	{
		return assemblyLine;
	}

	/**
	 * Contructs a new FontFactory object.
	 * @param alphabet the alphabet over which the FengGUI font shall be created. The font will contain
	 * all characters that are in the alphabet
	 * @param font the AWT font used to render the FengGUI font
	 */
	public FontFactory(Alphabet alphabet, Font font) 
	{
		this.alphabet = alphabet;
		this.font = font;
		createFontMetrics();
	}

	private void createFontMetrics() 
	{
		BufferedImage baseImage = new BufferedImage(1, 1,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) baseImage.getGraphics();
		fontMetrics = g.getFontMetrics(font);
	}

	/**
	 * Creates the FengGUI font out of the AWT font. It uses the previously set up AssemblyLine
	 * to render and modify each character in the alphabet. 
	 * @return FengGUI font
	 */
	public org.fenggui.render.Font createFont() 
	{
		ArrayList<BufferedImage> charImages = new ArrayList<BufferedImage>();

		BufferedImage bi = null;
		
		for (char c : alphabet.getAlphabet()) 
		{
			if(c != ' ') // if processed character not the blank
			{
				// generate BufferedImage that is big enough to display the character
				bi = new BufferedImage(
					fontMetrics.charWidth(c) + safetyMargin*(font.getSize()/2), 
					fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent() + safetyMargin,
					BufferedImage.TYPE_INT_ARGB);
			} 
			else 
			{
				bi = new BufferedImage(fontMetrics.stringWidth(" "), fontMetrics.getMaxAscent()+fontMetrics.getMaxDescent(), BufferedImage.TYPE_INT_ARGB);
			}
			
			assemblyLine.execute(fontMetrics, bi, c, safetyMargin);
			
			if(c != ' ') 
			{
				bi = cropImage(bi, c);
			}
			
			squarePixel += bi.getWidth() * bi.getHeight();
			
			charImages.add(bi);
		}

		org.fenggui.render.Font font = buildCharTexture(charImages);

		return font;
	}
	
	private BufferedImage cropImage(BufferedImage bi, char c) 
	{
		ColorModel cm = bi.getColorModel();

		int startX = 0;
		int endX = fontMetrics.charWidth(c);

		// coming from the right, going to the left
		for (int x = fontMetrics.charWidth(c); x < bi.getWidth(); x++) 
		{
			boolean hasAlpha = false;
			
			for (int y = 0; y < bi.getHeight(); y++) 
			{
				if (cm.getAlpha(bi.getRGB(x, y)) != 0) 
				{
					hasAlpha = true;
					endX = x+1;
					break;
				}
			}
			
			if(!hasAlpha) break;
		}
		
		//System.out.println(c+" StartX: "+startX+", endX: "+endX);
		/*
		if(c == ' ')
		{
			endX = fontMetrics.charWidth(' ');
			startX = 0;
		}
		*/

		//endX++;
		//endX++;
		
		// large fonts need extra space
		//while(endX - startX < fontMetrics.charWidth(c)) endX++;
		
		BufferedImage cropped = new BufferedImage(endX - startX, fontMetrics.getMaxAscent()+fontMetrics.getMaxDescent(),
				bi.getType());
		cropped.getGraphics().drawImage(bi, -startX, 0, null);

		return cropped;
	}

	private org.fenggui.render.Font buildCharTexture(ArrayList<BufferedImage> charImages) 
	{
		Hashtable<Character, CharacterPixmap> hashtable = new Hashtable<Character, CharacterPixmap>();
		int length = ImageConverter.powerOf2((int) Math.sqrt(squarePixel));

		BufferedImage bi = ImageConverter.createGlCompatibleAwtImage(length, length);
		Graphics2D g = bi.createGraphics();
		Clear.clear(g, bi.getWidth(), bi.getHeight());

		int x = 0;
		int y = 0;

		int counter = 0;

		for (BufferedImage charImage : charImages) 
		{
			if (charImage.getWidth() + x > bi.getWidth()) 
			{
				y += fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();
				x = 0;
			}

			int xValue = x;
			int yValue = y;
			
			CharacterPixmap cp = new CharacterPixmap(
					null, xValue, yValue, 
					charImage.getWidth(), fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent(), 
					alphabet.getAlphabet()[counter], fontMetrics.stringWidth(""
							+ alphabet.getAlphabet()[counter]));

			hashtable.put(cp.getCharacter(), cp);

			g.drawImage(charImage, xValue, yValue, null);
			
			x += charImage.getWidth();
			counter++;
		}

		//saveImageToDisk(bi, "test.png");

		return new org.fenggui.render.Font(bi, hashtable, fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent());

	}

	/**
	 * Saves the given image to a PNG file.
	 * @param bi the image to be saved
	 * @param filename the file where to save the PNG in
	 */
	public static void saveImageToDisk(BufferedImage bi, String filename) 
	{
		String ending = filename.substring(filename.length() - 3, 
				filename.length());
		try 
		{
			ImageIO.write(bi, ending, new File(filename));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a standard FengGUI font from the given AWT font.
	 * @param awtFont the AWT-Font that shall be used to render the FengGUI font
	 * @return FengGUI font
	 */
	public static org.fenggui.render.Font renderStandardFont(java.awt.Font awtFont)
	{
		return renderStandardFont(awtFont, Alphabet.getDefaultAlphabet());
	}
	
	/**
	 * Creates a standard FengGUI font from a given alphabet.
	 * @param awtFont the awt-font that shall be used to render the FengGUI font
	 * @param alphabet the alphabet which contains the characters to render
	 * @return new FengGUI font
	 */
	public static org.fenggui.render.Font renderStandardFont(java.awt.Font awtFont, Alphabet alphabet)
	{
		return renderStandardFont(awtFont, false, alphabet);
		
	}
	
	/**
	 * Creates a new FengGUI font out of an awt font.
	 * @param awtFont the awt font used to render the characters on the texture
	 * @param antiAliasing flag indicating whether antialiasing shall be enabled for rendering
	 * @param alphabet the charcters to render
	 * @return FengGUI font
	 */
	public static org.fenggui.render.Font renderStandardFont(java.awt.Font awtFont, boolean antiAliasing, Alphabet alphabet)
	{
		FontFactory ff = new FontFactory(alphabet, awtFont);
		AssemblyLine line = ff.getAssemblyLine();
		
		line.addStage(new Clear());
		line.addStage(new DrawCharacter(java.awt.Color.WHITE, antiAliasing));

		org.fenggui.render.Font f = ff.createFont();		

		return f;
		
	}
}
