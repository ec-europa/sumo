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
 * Created on Apr 29, 2005
 * $Id: Border.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.border;

import java.io.IOException;

import org.fenggui.IDecorator;
import org.fenggui.Span;
import org.fenggui.render.Graphics;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Spacing;

/**
 * Base class for all borders.
 * 
 * @author Johannes Schaback aka Schabby, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public abstract class Border extends Spacing implements IDecorator
{

	private String label = "default";
	private boolean enabled = true;
	private Span span = Span.BORDER;
	
	/**
	 * Every Background class must register itself to the type register
	 * if it should be possible to be loaded using InputOutputStream
	 */
	//public static final TypeRegister<Border> TYPE_REGISTER =
	//	new TypeRegister<Border>();
	

	public Border()
	{

	}

	public Span getSpan()
	{
		return span;
	}

	public void setSpan(Span span)
	{
		this.span = span;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}


	public void setLabel(String label)
	{
		this.label = label;
	}


	public boolean isEnabled()
	{
		return enabled;
	}
	
	public String getLabel()
	{
		return label;
	}
	

	public Border(int top, int left, int right, int bottom)
	{
		super(top, left, right, bottom);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		label = stream.processAttribute("label", label, "default");
		enabled = stream.processAttribute("enabled", enabled, true);
		span = (Span) stream.processEnum("span", span, Span.BORDER, Span.class, Span.STORAGE_FORMAT);
	}

	
	public abstract void paint(Graphics g, int localX, int localY, int width, int height);

	public String getUniqueName() {
		return GENERATE_NAME;
	}
}
