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
 * Created on Feb 21, 2007
 * $Id: SplitContainerAppearance.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.io.IOException;

import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;

public class SplitContainerAppearance extends SpacingAppearance
{
	private SplitContainer cont = null;
	private DecoratorLayer barDecorator = new DecoratorLayer();
	
	public SplitContainerAppearance(SplitContainer w)
	{
		super(w);
		cont = w;
	}

	@Override
	public Dimension getContentMinSizeHint()
	{
		int firstMinHeight = 0;
		int firstMinWidth = 0;
		
		int secondMinHeight = 0;
		int secondMinWidth = 0;
		
		IWidget firstWidget = cont.getFirstWidget();
		IWidget secondWidget = cont.getSecondWidget();
		
		if(firstWidget != null) 
		{
			//firstWidget.updateMinSize();
			
			firstMinHeight = firstWidget.getMinSize().getHeight();
			firstMinWidth = firstWidget.getMinSize().getWidth();
		}
		
		if(secondWidget != null) 
		{
			//secondWidget.updateMinSize();
			
			secondMinHeight = secondWidget.getMinSize().getHeight();
			secondMinWidth = secondWidget.getMinSize().getWidth();
		}
		
		if(cont.isHorizontal())
		{
			return new Dimension(
					Math.max(firstMinWidth, secondMinWidth), 
					firstMinHeight + cont.getBarSize() + secondMinHeight);
		}
		else
		{
			return new Dimension(
					firstMinWidth + cont.getBarSize() + secondMinWidth,
					Math.max(firstMinHeight, secondMinHeight));
		}
	}

	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		IWidget firstWidget = cont.getFirstWidget();
		IWidget secondWidget = cont.getSecondWidget();
		
		if(firstWidget != null)
		{
			g.translate(firstWidget.getX(), firstWidget.getY());
			firstWidget.paint(g);
			g.translate(-firstWidget.getX(), -firstWidget.getY());
		}
		
		if(secondWidget != null)
		{
			g.translate(secondWidget.getX(), secondWidget.getY());
			secondWidget.paint(g);
			g.translate(-secondWidget.getX(), -secondWidget.getY());
		}
		
		int width, height;
		int x, y;
		
		if(cont.isHorizontal())
		{
			width = getContentWidth();
			height = cont.getBarSize();
			x = 0;
			y = cont.getValue();
		}
		else
		{
			width = cont.getBarSize();
			height = getContentHeight();
			x = cont.getValue();
			y = 0;
		}
		
		barDecorator.paint(g, x, y, width, height);
		
		g.setColor(Color.WHITE);
		Pixmap pixmap = cont.getPixmap();
		if(pixmap != null) 
			g.drawImage(pixmap, x + width / 2 - pixmap.getWidth()/2, y + height / 2 - pixmap.getHeight()/2);
	}

	public DecoratorLayer getBarDecorator()
	{
		return barDecorator;
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		barDecorator = stream.processChild("BarDecorator", barDecorator, barDecorator, DecoratorLayer.class);
	}
	
	
}
