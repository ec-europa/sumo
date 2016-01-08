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
 * Created on Dec 8, 2006
 * $Id: Switch.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.io.IOException;

import org.fenggui.theme.xml.IXMLStreamable;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;

public abstract class Switch implements IXMLStreamable
{
	private String label = "default";
	private boolean reactingOnEnabled = true; 
	
	public Switch(String label)
	{
		this.label = label;
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public boolean isReactingOnEnabled()
	{
		return reactingOnEnabled;
	}

	public abstract void setup(IWidget widget);
	
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		label = stream.processAttribute("label", label, label);
		reactingOnEnabled = stream.processAttribute("reactingOnEnabled", reactingOnEnabled, reactingOnEnabled);
	}

	public String getUniqueName() {
		return GENERATE_NAME;
	}

	public void setReactingOnEnabled(boolean reactingOnEnabled)
	{
		this.reactingOnEnabled = reactingOnEnabled;
	}
	
	
}
