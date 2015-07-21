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
 * Created on Jan 18, 2007
 * $Id: DecoratorLayer.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.io.IOException;
import java.util.ArrayList;

import org.fenggui.render.Graphics;
import org.fenggui.theme.XMLTheme;
import org.fenggui.theme.xml.IXMLStreamable;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * Class to make decorators usable for overlay effects such as selections bars in menus und
 * tables.
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public class DecoratorLayer implements IXMLStreamable
{
	private ArrayList<IDecorator> decorators = new ArrayList<IDecorator>();
	
	public DecoratorLayer(InputOnlyStream stream) throws IOException, IXMLStreamableException
	{
		process(stream);
	}
	
	public DecoratorLayer(IDecorator d)
	{
		decorators.add(d);
	}
	
	public DecoratorLayer(IDecorator... array)
	{
		for(IDecorator d: array) decorators.add(d);
	}
	
	public DecoratorLayer(java.util.List<IDecorator> list)
	{
		decorators.addAll(list);
	}
	
	public void paint(Graphics g, int x, int y, int width, int height)
	{
		for(IDecorator deco: decorators)
		{
			if(!deco.isEnabled()) continue;
			
			deco.paint(g, x, y, width, height);
		}
	}

	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		stream.processChildren(decorators, XMLTheme.TYPE_REGISTRY);
	}
	
	public void add(IDecorator d)
	{
		decorators.add(d);
	}
	
	public void clear()
	{
		decorators.clear();
	}

	/* (non-Javadoc)
	 * @see org.fenggui.io.IOStreamSaveable#getUniqueName()
	 */
	public String getUniqueName() {
		return GENERATE_NAME;
	}
}
