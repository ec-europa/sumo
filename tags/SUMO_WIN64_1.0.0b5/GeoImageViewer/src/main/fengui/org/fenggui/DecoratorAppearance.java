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
 * Created on Dec 6, 2006
 * $Id: DecoratorAppearance.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.io.IOException;
import java.util.ArrayList;

import org.fenggui.background.Background;
import org.fenggui.border.Border;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.theme.XMLTheme;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Spacing;

/**
 * Specialized appearance definition for widgets that need decorators (mainly
 * borders and backgrounds). Decorators can be enabled and disabled to allow for
 * interaction with the mouse or keyboard.
 * 
 * @author Johannes, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public abstract class DecoratorAppearance extends SpacingAppearance
{
	private ArrayList<IDecorator> decorators = new ArrayList<IDecorator>();
	private ArrayList<Switch> switches = new ArrayList<Switch>();
	
	public void add(String label, Background background, Span spanType)
	{
		background.setLabel(label);
		background.setSpan(spanType);
		decorators.add(background);
	}

	public void add(IDecorator decorator)
	{
		decorators.add(decorator);
	}
	
	public void add(Background background)
	{
		add("default", background, Span.PADDING);
	}
	
	public void add(String label, Background background)
	{
		add(label, background, Span.PADDING);
	}
	
	public void add(String label, Border border, boolean setAsBorderSpacing)
	{
		border.setLabel(label);
		decorators.add(border);

		// we need to set a copy of the border as Spacing type in order to avoid that the
		// XMLOutputStream outputs all the border information.
		if(setAsBorderSpacing) setBorder(new Spacing(border.getTop(), border.getLeft(), border.getRight(), border.getBottom()));
	}

	public void add(Border border)
	{
		add("default", border, true);
	}

	public void add(String label, Border border)
	{
		add(label, border, true);
	}
	
	public void add(Switch sw)
	{
		switches.add(sw);
	}
	
	public DecoratorAppearance(IWidget w)
	{
		super(w);
	}

	public DecoratorAppearance(IWidget w, InputOnlyStream stream) throws IOException, IXMLStreamableException
	{
		super(w);
		this.process(stream);
	}

	@Override
	public final void paint(Graphics g, IOpenGL gl)
	{
		for(int i = 0; i < decorators.size() ; i++)
		{
			int width = getWidget().getSize().getWidth();
			int height = getWidget().getSize().getHeight();
			
			paintDecorator(decorators.get(i), g, gl, this, width, height);
		}
		
		super.paint(g, gl);
	}
	
	/**
	 * Paints the given decorator. It adjusts the size of the decorator according
	 * to the span. E.g. Span.BORDER means that the decorator span over the
	 * padding AND the border.
	 * @param d the decorators
	 * @param g the graphics object
	 * @param gl the opengl object
	 * @param app the appearance used to calculate the margins
	 * @param widgetWidth the widget if the whole widget
	 * @param widgetHeight the heigth of the whole widget
	 */
	private void paintDecorator(IDecorator d, Graphics g, IOpenGL gl, SpacingAppearance app, int widgetWidth, int widgetHeight)
	{
		if(!d.isEnabled()) return;
		
		int x = 0;
		int y = 0;
		
		if(d.getSpan() == Span.PADDING)
		{
			Spacing m = app.getMargin();
			Spacing b = app.getBorder();
			
			x += m.getLeft()   + b.getLeft();
			y += m.getBottom() + b.getBottom();
			
			widgetWidth  -= x + m.getRight() + b.getRight();
			widgetHeight -= y + m.getTop()   + b.getTop();
		}
		else if(d.getSpan() == Span.BORDER)
		{
			Spacing m = app.getMargin();
			
			x += m.getLeft();
			y += m.getBottom();
			
			widgetWidth  -= x + m.getRight();
			widgetHeight -= y + m.getTop();
		}
		
		d.paint(g, x, y, widgetWidth, widgetHeight);
	}
	
	public void setEnabled(String label, boolean enable)
	{
		for(IDecorator wrapper: decorators)
		{
			if(wrapper.getLabel().equals(label))
				wrapper.setEnabled(enable);
		}
		
		for(Switch sw: switches)
		{
			if(sw.getLabel().equals(label) && enable == sw.isReactingOnEnabled()) sw.setup(getWidget());
		}
	}
	
	@Override
	public String toString() 
	{
		String s = "";
		
		for(IDecorator wrapper: decorators)
		{
			s += "\n- "+ wrapper.toString();
		}
		
		return super.toString() + s;
	}
	
	/**
	 * Removes all switches and decorators from this appearance.
	 *
	 */
	public void removeAll()
	{
		decorators.clear();
		switches.clear();
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		if(stream.startSubcontext("decorators"))
		{
			if(stream.processAttribute("clear", false, false))
				decorators.clear();
				
			stream.processChildren(decorators, XMLTheme.TYPE_REGISTRY);

			stream.endSubcontext();
		}
		
		if(stream.startSubcontext("switches"))
		{
			if(stream.processAttribute("clear", false, false))
				switches.clear();
				
			stream.processChildren(switches, XMLTheme.TYPE_REGISTRY);

			stream.endSubcontext();
		}
		
	}


}
