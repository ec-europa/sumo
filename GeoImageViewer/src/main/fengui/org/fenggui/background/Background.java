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
 * $Id: Background.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.background;

import java.io.IOException;

import org.fenggui.IDecorator;
import org.fenggui.Span;
import org.fenggui.render.Graphics;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.IXMLStreamable;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * Basic class for all backgrounds. Its purpose is mainly to
 * provide the <code>paint</code> method which will be overriden
 * by specific background implementations.
 * 
 * @author Johannes Schaback
 * @version $Revision: 327 $
 */
public abstract class Background implements IXMLStreamable, IDecorator
{
	private String label = "default";
	private boolean enabled = true;
	private Span span = Span.PADDING;
	
	/**
	 * Every Background class must register itself to the type register
	 * if it should be possible to be loaded using InputOutputStream
	 */
	 //public static final TypeRegister<Background> TYPE_REGISTER =
		//new TypeRegister<Background>();
	
    /**
     * Draws the background
     * @param g the graphics context
     */
    public abstract void paint(Graphics g, int localX, int localY, int width, int height);
    

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}


	public Span getSpan()
	{
		return span;
	}


	public void setSpan(Span span)
	{
		this.span = span;
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
	
	@SuppressWarnings("unchecked")
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		span = (Span) stream.processEnum("span", span, span, Span.class, Span.STORAGE_FORMAT);
		label = stream.processAttribute("label", label, "default");
		enabled = stream.processAttribute("enabled", enabled, true);
		
	}

	public String getUniqueName() {
		return GENERATE_NAME;
	}
}