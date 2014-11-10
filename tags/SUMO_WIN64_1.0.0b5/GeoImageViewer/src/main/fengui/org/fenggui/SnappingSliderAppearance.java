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
 * Created on Aug 1, 2007
 * $Id$
 */
package org.fenggui;

import java.io.IOException;

import org.fenggui.event.IPaintListener;
import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;

public class SnappingSliderAppearance extends DecoratorAppearance
{
	private SnappingSlider ss = null;
	
	private IPaintListener tickPainter = new TickPainter();
	private Font font = Font.getDefaultFont();
	private Color textColor = Color.BLACK;
	private Color tickColor = Color.BLACK;
	
	public SnappingSliderAppearance(SnappingSlider w)
	{
		super(w);
		ss = w;
	}

	@Override
	public Dimension getContentMinSizeHint()
	{
		if(ss.getTickLabels() != null)
		{
			int sum = 0;
			for(String s: ss.getTickLabels())
				sum += font.getWidth(s);
			
			return new Dimension(sum + 30, ss.getSliderPixmap().getHeight() + 20 + font.getHeight());
		}
		else return new Dimension(100, ss.getSliderPixmap().getHeight() + 20);
	}

	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		tickPainter.paint(g);
		
		if(ss.getSliderPixmap() == null) return;
		
		int x =(int)( (float)ss.getValue() * ((float)getContentWidth() / (float)(ss.getTicks()-1)));
		
		g.setColor(Color.WHITE);
		g.drawImage(ss.getSliderPixmap(), x - ss.getSliderPixmap().getWidth()/2, getContentHeight()/2 - ss.getSliderPixmap().getHeight()/2);
	}

	public class TickPainter implements IPaintListener
	{
		public void paint(Graphics g)
		{
			g.setColor(tickColor);
			final int y = getContentHeight()/2 + ss.getSliderPixmap().getHeight()/2;
			
			for(int i=0; i < ss.getTicks(); i++)
			{
				int x = (int)((float)i * (float)(getContentWidth() / (float)(ss.getTicks()-1)));
				
				g.drawLine(x, y-5, x, y + 5);
			}			
			
			String[] tickLabels = ss.getTickLabels();
			
			if(tickLabels == null) return;
			if(tickLabels.length != ss.getTicks())
				throw new IllegalStateException("Number of ticks and number of tick labels must be equal!");
			
			g.setFont(getFont());
			g.setColor(textColor);
			for(int i=0; i < ss.getTicks(); i++)
			{
				int x = (int)((float)i * (float)(getContentWidth() / (float)(ss.getTicks()-1)));
				
				g.drawString(tickLabels[i], x - getFont().getWidth(tickLabels[i])/2, y + 7);
			}	
			
		}
		
	}

	
	
	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		textColor = stream.processChild("TextColor", textColor, Color.BLACK, Color.class);		
		tickColor = stream.processChild("TickColor", textColor, Color.BLACK, Color.class);
		if(stream.isInputStream()) // XXX: only support read-in at the moment :(
			setFont(stream.processChild("Font", getFont(), Font.getDefaultFont(), Font.class));

	}

	public IPaintListener getTickPainter()
	{
		return tickPainter;
	}

	public void setTickPainter(IPaintListener tickPainter)
	{
		this.tickPainter = tickPainter;
	}

	public Font getFont()
	{
		return font;
	}

	public void setFont(Font font)
	{
		this.font = font;
	}

	public Color getTextColor()
	{
		return textColor;
	}

	public void setTextColor(Color textColor)
	{
		this.textColor = textColor;
	}

	public Color getTickColor()
	{
		return tickColor;
	}

	public void setTickColor(Color tickColor)
	{
		this.tickColor = tickColor;
	}
	
	
}
