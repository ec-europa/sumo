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
 * Created on Dec 11, 2006
 * $Id: TextEditorAppearance.java 358 2007-09-21 16:03:59Z marcmenghin $
 */
package org.fenggui;

import java.io.IOException;

import org.fenggui.TextEditor.Selection;
import org.fenggui.event.IPaintListener;
import org.fenggui.render.BufferedTextRenderer;
import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.render.ITextRenderer;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.CharacterPixmap;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;
import org.fenggui.util.Timer;

public class TextEditorAppearance extends DecoratorAppearance
{
	private TextEditor editor = null;
	private Color textColor = Color.BLACK;
	private Color selectionColor = Color.BLUE;
	private Font font = Font.getDefaultFont(); // font is static over states
	private ITextRenderer textRenderer = new BufferedTextRenderer();
	
	private TextCursorPainter cursorPainter = new TextCursorPainter();
	
	protected boolean useBufferedTextRenderer = false;
	
	public TextEditorAppearance(TextEditor w)
	{
		super(w);
		editor = w;
	}

	@Override
	public Dimension getContentMinSizeHint()
	{
		int width = 0;
		int height = 0;

		String[] lines = editor.getTextWarped().split("\n", -1);

		if (!editor.isMultiline())
		{ // single line
			height = font.getHeight();
			if (lines.length > 0) width = editor.getFixedSize();
			else width = 20;
		}
		else
		{
			height = lines.length * font.getHeight();

			// find longest line
			if (editor.isWordWarp())
			{
			  //min width is size
			  width = 10; //this.getContentWidth();
			} else {
			  width = editor.getFixedSize();
//  			for (String s : lines)
//  			{
//  				width = Math.max(width, font.getWidth(s));
//  			}
			}
		}

		return new Dimension(width, height);
	}

	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		if(useBufferedTextRenderer)
		{
			textRenderer.render(0,0, g, gl);
		}
		else
		{
			String text = editor.getTextWarped();
			Selection selection = editor.getSelectionWarped();
			
	//		 Nothing to paint
			if ((text == null || text.length() == 0) && !editor.isInWritingState())  return;
	
			final int fontHeight = font.getHeight();
	
			// lower left corner of character map
			int y = getContentHeight() - fontHeight;
			int x = 0;
	
			x += g.getTranslation().getX();
			y += g.getTranslation().getY();
	
			// No text : draw the cursor at the beginning
			if ((text == null || text.length() == 0) && editor.isInWritingState())
			{
				g.setColor(textColor);
				paintCursor(g, x, y);
				return;
			}
	
			CharacterPixmap charMap = null;
	
			gl.enableTexture2D(true);
	
			// Bind the Font's texture outsite the glBegin and glEnd
			font.getCharPixMap(text.charAt(0)).getTexture().bind();
			
			gl.setTexEnvModeModulate();
			g.setColor(textColor);
			
			gl.startQuads();
	
			// running through text, character wise
			for (int charIndex = 0; charIndex < text.length(); charIndex++)
			{
				char character = text.charAt(charIndex);
	
				// if encounter selection, switch background and foreground color
				if (charIndex == selection.startIndex && selection.startIndex < selection.endIndex)
				{
					int tmpX = x;
					int tmpY = y;
	
					gl.end();
					gl.enableTexture2D(false);
					
					// draw the (blue) background of the selection
					gl.startQuads();
					g.setColor(selectionColor);
					gl.vertex(tmpX, tmpY);
					gl.vertex(tmpX, tmpY + fontHeight);
	
					// iterate over selected characters
					for (int k = charIndex; k < selection.endIndex && k < text.length(); k++)
					{
						char tmpChar = text.charAt(k);
	
						if (tmpChar == '\n')
						{
							gl.vertex(g.getTranslation().getX() + getContentWidth(), tmpY + fontHeight);
							gl.vertex(g.getTranslation().getX() + getContentWidth(), tmpY);
	
							tmpY -= fontHeight;
							tmpX = g.getTranslation().getX();
	
							gl.vertex(0, tmpY);
							gl.vertex(0, tmpY + fontHeight);
						}
						else
						{
							if (editor.isPasswordField()) tmpX += font.getCharPixMap('*').getCharWidth();
							else tmpX += font.getCharPixMap(tmpChar).getCharWidth();
						}
					}
	
					gl.vertex(tmpX, tmpY + fontHeight);
					gl.vertex(tmpX, tmpY);
	
					// set mode back to drawing characersd, but in WHITE up to now!
					gl.end();
					gl.enableTexture2D(true);
					gl.startQuads();
					g.setColor(Color.WHITE);
				}
	
				if (charIndex == editor.getCursorWarped() && editor.isInWritingState())
				{
					gl.end();
					paintCursor(g, x, y);
					gl.enableTexture2D(true);
					gl.startQuads();
				}
	
				if (character == '\n')
				{
					// Single line : do not show other lines
					if (!editor.isMultiline())
					{
						break;
					}
	
					x = g.getTranslation().getX();
					y -= fontHeight;
					continue;
				}
				
				// ended drawing selection, switch color back to normal
				if (charIndex == selection.endIndex)
				{
					g.setColor(textColor);
				}
	
				if (editor.isPasswordField()) charMap = font.getCharPixMap('*');
				else charMap = font.getCharPixMap(character);
	
				final int imgWidth = charMap.getWidth();
				final int imgHeight = charMap.getHeight();
				final float endY = charMap.getEndY();
				final float endX = charMap.getEndX();
				final float startX = charMap.getStartX();
				final float startY = charMap.getStartY();
	
				// now draw the actual characer :)
				
				gl.texCoord(startX, endY);
				gl.vertex(x, y);
	
				gl.texCoord(startX, startY);
				gl.vertex(x, imgHeight + y);
	
				gl.texCoord(endX, startY);
				gl.vertex(imgWidth + x, imgHeight + y);
	
				gl.texCoord(endX, endY);
				gl.vertex(imgWidth + x, y);
	
				x += charMap.getCharWidth();
			}
	
			gl.end();
			gl.enableTexture2D(false);
			
	
			// Draw the cursor even if it is at the end of the text
			if (editor.getCursorWarped() == text.length() && editor.isInWritingState())
			{
				paintCursor(g, x, y);
			}
		}
	}


	public Font getFont()
	{
		return font;
	}


	public void setFont(Font font)
	{
		this.font = font;
		if(cursorPainter != null) cursorPainter.setHeight(font.getHeight());
		editor.updateMinSize();
	}
	
	/**
	 * @return the text color
	 */
	public Color getTextColor()
	{
		return textColor;
	}


	/**
	 * Define the text color
	 * 
	 * @param textColor
	 */
	public void setTextColor(Color textColor)
	{
		this.textColor = textColor;
	}
	

	public class TextCursorPainter implements IPaintListener
	{
		private Timer timer = new Timer(2, 500);
		private int x, y;
		private int height = 17; // @todo make cursor height adjustable #

		private Color cursorColor = Color.BLACK;


		public int getX()
		{
			return x;
		}


		public void setX(int x)
		{
			this.x = x;
		}


		public int getY()
		{
			return y;
		}


		public void setY(int y)
		{
			this.y = y;
		}


		public Color getCursorColor()
		{
			return cursorColor;
		}


		public void setCursorColor(Color cursorColor)
		{
			this.cursorColor = cursorColor;
		}


		public void resetTimer()
		{
			timer.reset();
		}


		public void paint(Graphics g)
		{
			if (timer.getState() == 0)
			{
				//System.out.println(x+ " "+y);
				g.setColor(cursorColor);
				g.drawLine(x, y, x, y + height);
				//g.drawLine(x+1, y, x+1, y+height);
			}
		}


		/**
		 * returns the height of the carret.
		 * @return height of carret
		 */
		public int getHeight()
		{
			return height;
		}


		/** 
		 * sets the height of the carret.
		 */
		public void setHeight(int height)
		{
			this.height = height;
		}
		
		
	}


	/**
	 * @param cursorPainter
	 */
	public void setCursorPainter(TextCursorPainter cursorPainter)
	{
		this.cursorPainter = cursorPainter;
	}


	/**
	 * @return the cursor painter
	 */
	public TextCursorPainter getCursorPainter()
	{
		return cursorPainter;
	}
	

	private void paintCursor(Graphics g, int x, int y)
	{
		IOpenGL gl = g.getOpenGL();
		gl.enableTexture2D(false);
		cursorPainter.setX(x - g.getTranslation().getX());
		cursorPainter.setY(y - g.getTranslation().getY());
		cursorPainter.paint(g);
		
		Selection selection = editor.getSelectionWarped();
		
		if (editor.getCursorWarped() == selection.startIndex && editor.getCursorWarped() != selection.endIndex)
		{
			g.setColor(Color.WHITE);
		}
		else
		{
			g.setColor(textColor);
		}
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		textColor = stream.processChild("Color", textColor, Color.BLACK, Color.class);
		
		if(stream.isInputStream())
			font = stream.processChild("Font", font, Font.getDefaultFont(), Font.class);
	}

	public ITextRenderer getTextRenderer()
	{
		return textRenderer;
	}

	public void setTextRenderer(ITextRenderer textRendered)
	{
		this.textRenderer = textRendered;
	}

	/**
	 * @return the selectionColor
	 */
	public Color getSelectionColor()
	{
		return selectionColor;
	}

	/**
	 * @param selectionColor the selectionColor to set
	 */
	public void setSelectionColor(Color selectionColor)
	{
		this.selectionColor = selectionColor;
	}
	
	
	
}
