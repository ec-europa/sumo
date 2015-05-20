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
 * $Id: ProgressBarAppearance.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.io.IOException;

import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;

public class ProgressBarAppearance extends DecoratorAppearance
{
	private ProgressBar bar = null;
	
	private Font font = Font.getDefaultFont();
	private Color progressBarColor = Color.BLUE;
	private Color textColor = Color.BLACK;
	
	public ProgressBarAppearance(ProgressBar w)
	{
		super(w);
		bar = w;
	}

	@Override
	public Dimension getContentMinSizeHint()
	{
		return new Dimension(0, 0);
	}

	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		
		g.setColor(progressBarColor);
		g.drawFilledRectangle(0, 0, 
				(int)((double)getContentWidth()*bar.getValue()),
				getContentHeight());
		
		String s = bar.getText();
		if(s != null && s.length() > 0) {
			g.setColor(textColor);
			g.setFont(getFont());
			g.drawString(s, 
					(getContentWidth()-font.getWidth(s))/2,
					getContentHeight()/2-font.getHeight()/2);
		}
	}

	public Color getProgressBarColor() {
		return progressBarColor;
	}

	public void setProgressBarColor(Color progressBarColor) {
		this.progressBarColor = progressBarColor;
	}

	public Color getTextColor() {
		return textColor;
	}

	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) 
	{
		this.font = font;
		bar.updateMinSize();
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		if(stream.isInputStream()) // XXX: only support read-in at the moment :(
			setFont(stream.processChild("Font", getFont(), Font.getDefaultFont(), Font.class));

		textColor        = stream.processChild("Color", textColor, Color.WHITE, Color.class);
		progressBarColor = stream.processChild("ProgressBarColor", textColor, Color.BLUE, Color.class);
	}
	
	
	
}
