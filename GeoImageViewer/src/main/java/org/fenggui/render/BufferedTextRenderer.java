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
 * Created on Feb 23, 2007
 * $Id: BufferedTextRenderer.java 220 2007-03-10 12:00:00Z schabby $
 */
package org.fenggui.render;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.fenggui.util.CharacterPixmap;

/**
 * Renders lines of text by pre-rendering the entire text on a single texture
 * and displaying the texture in the render() method. Use this TextRenderer
 * whenever you have a text that is never (or seldom) updated like for most
 * buttons, menu items etc.
 * 
 * @author Johannes Schaback, last edited by $Author: schabby $, $Date: 2007-03-10 13:00:00 +0100 (Sat, 10 Mar 2007) $
 * @version $Revision: 220 $
 */
public class BufferedTextRenderer implements ITextRenderer 
{
	private Font font = Font.getDefaultFont();
	private String text = null;
	private ArrayList<ArrayList<CharacterPixmap>> chars = new ArrayList<ArrayList<CharacterPixmap>>();
	private BufferedImage image = null;
	private int width = 0;
	private Pixmap completePixmap = null;
	
	public Font getFont()
	{
		return font;
	}

	public int getHeight()
	{
		return chars.size() * font.getHeight();
	}

	public String getText()
	{
		return text;
	}

	public int getWidth()
	{
		return width;
	}

	public void setFont(Font newFont)
	{
		this.font = newFont;
		setText(text); // update texture... lame trick
	}

	public void setText(String text)
	{
		this.text = text;
		//System.out.println(text);
		if(text == null || text.length() == 0) return;
		
		if(font == null) throw new NullPointerException("getFont() == null! No font set!");
		
		chars.clear();
		
		String[] lines = text.split("\n");
		
		for(int i = 0; i < lines.length; i++)
		{
			String line = lines[i].trim();
			//System.out.println(">"+line+"<");
			ArrayList<CharacterPixmap> linePixmaps = new ArrayList<CharacterPixmap>(line.length());
			int lineWidth = 0;
		
			for(int j = 0; j < line.length(); j++)
			{
				char c = line.charAt(j);
				if(c == '\t' || c == '\r' || c == '\f')
					continue;
				CharacterPixmap pixmap = font.getCharPixMap(c);
				if(pixmap == null) continue;
				linePixmaps.add(pixmap);
				lineWidth += pixmap.getCharWidth();
			}
			
			width = Math.max(lineWidth, width);
			chars.add(linePixmaps);
		}
		
		updateBufferedImage();
	}

	private void updateBufferedImage()
	{
		if(image == null || image.getWidth() < getWidth() || image.getHeight() < getHeight())
		{
			image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		}
		
		Graphics g = image.getGraphics();
		//g.clearRect(0, 0, width, getHeight());
		
		BufferedImage fontImg = font.getImage();
		
		int x = 0, y = 0;
		
		for(int lineIndex = 0; lineIndex < chars.size(); lineIndex++)
		{
			ArrayList<CharacterPixmap> line = chars.get(lineIndex);
			
			for(CharacterPixmap pixmap: line)
			{
				BufferedImage charImg = fontImg.getSubimage(pixmap.getX(), pixmap.getY(), pixmap.getWidth(), pixmap.getHeight());
				g.drawImage(charImg, x, y, null);
				x += pixmap.getWidth();
			}
			
			y += font.getHeight();
			x = 0;
		}
/*
		try
		{
			ImageIO.write(image, "png", new File(System.currentTimeMillis()+"-FONT.png"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}*/
	}

    public static int get2Fold(int fold)
	{
		int ret = 2;
		while (ret < fold)
		{
			ret *= 2;
		}
		return ret;
	}

	public void render(int x, int y, org.fenggui.render.Graphics g, IOpenGL gl)
	{
		if(image == null) return;
		
		if(completePixmap == null)
		{
			completePixmap = new Pixmap(Binding.getInstance().getTexture(image));
			//System.out.println("HMMMM: "+completePixmap.getTexture().getTextureWidth()+" "+completePixmap.getTexture().getTextureHeight()+" "+chars.size());
		}
		
		g.drawImage(completePixmap, x, y);
		
		
/*
		gl.enableTexture2D(true);
		
        if (completePixmap.getTexture().hasAlpha())
        {
            gl.setTexEnvModeModulate();
        }		
		
		g.setColor(Color.WHITE);
		completePixmap.getTexture().bind();
		gl.startQuads();
		
        final int imgWidth = completePixmap.getWidth();
        final int imgHeight = completePixmap.getHeight();		
		
        final float endY = completePixmap.getEndY();
        final float endX = completePixmap.getEndX();
        
        final float startX = completePixmap.getStartX();
        final float startY = completePixmap.getStartY();

        gl.texCoord(startX, endY);
        gl.vertex(x, y);

        gl.texCoord(startX, startY);
        gl.vertex(x, imgHeight + y);

        gl.texCoord(endX, startY);
        gl.vertex(imgWidth + x, imgHeight + y);

        gl.texCoord(endX, endY);
        gl.vertex(imgWidth + x, y);
		gl.end();
		
		gl.enableTexture2D(false);*/
	}

	/**
	 * XXX not implemented yet!
	 */
	public void renderCarret(int x, int y, int charIndex, ICarretRenderer carret, org.fenggui.render.Graphics g, IOpenGL gl)
	{
	} 
}
