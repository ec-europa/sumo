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
 * Created on Mar 9, 2007
 * $Id: ConsoleAppearance.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.console;

import java.io.IOException;

import org.fenggui.DecoratorAppearance;
import org.fenggui.render.DirectTextRenderer;
import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.render.ICarretRenderer;
import org.fenggui.render.IOpenGL;
import org.fenggui.render.ITextRenderer;
import org.fenggui.render.LineCarretRenderer;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;
import org.fenggui.util.Timer;

public class ConsoleAppearance extends DecoratorAppearance
{
	private ITextRenderer textRenderer = new DirectTextRenderer();
	private ITextRenderer promtRenderer = new DirectTextRenderer();
	private ICarretRenderer carretRenderer = null;
	private Console widget = null;
	private Timer carretTimer = new Timer(2, 400);
	private Color textColor = Color.BLACK;
	
	public ConsoleAppearance(Console w)
	{
		super(w);
		this.widget = w;
		
		textRenderer.setFont(Font.getDefaultFont());
		promtRenderer.setFont(Font.getDefaultFont());
		carretRenderer = new LineCarretRenderer(Font.getDefaultFont().getHeight());
	}

	@Override
	public Dimension getContentMinSizeHint()
	{
		return null;
	}

	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		g.setColor(textColor);
		promtRenderer.render(0, 0, g, gl);
		if(carretTimer.getState() == 0 && widget.hasFocus())
			promtRenderer.renderCarret(0, 0, widget.getCarretIndex()-1, carretRenderer, g, gl);
		textRenderer.render(0, promtRenderer.getHeight(), g, gl);
	}

	public ITextRenderer getTextRenderer()
	{
		return textRenderer;
	}

	public ITextRenderer getPromtRenderer()
	{
		return promtRenderer;
	}

	public void setCarretRenderer(ICarretRenderer carretRenderer)
	{
		this.carretRenderer = carretRenderer;
	}

	public Console getWidget()
	{
		return widget;
	}

	public Timer getCarretTimer()
	{
		return carretTimer;
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		textColor = stream.processChild("Color", textColor, Color.BLACK, Color.class);
		
		if(stream.isInputStream())
			setFont(stream.processChild("Font", null, Font.getDefaultFont(), Font.class));		
	}

	public void setFont(Font f)
	{
		textRenderer.setFont(f);
		promtRenderer.setFont(f);
	}
	
	public Font getFont()
	{
		return textRenderer.getFont();
	}
}
