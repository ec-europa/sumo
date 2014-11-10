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
 * Created on Dec 12, 2006
 * $Id: SetTextColorSwitch.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.switches;

import java.io.IOException;

import org.fenggui.FengGUI;
import org.fenggui.IWidget;
import org.fenggui.LabelAppearance;
import org.fenggui.StandardWidget;
import org.fenggui.Switch;
import org.fenggui.theme.XMLTheme;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;

public class SetTextColorSwitch extends Switch
{
	Color c = null;
	LabelAppearance t = null;
	
	public SetTextColorSwitch(String label, Color colorToSet)
	{
		super(label);
		c = colorToSet;
	}

	@Override
	public void setup(IWidget w)
	{
		// kinda ugly casting, I know. But I think this should work in 99% of all cases.
		((LabelAppearance)((StandardWidget) w).getAppearance()).setTextColor(c);
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		c = (Color) stream.processChild(c, XMLTheme.TYPE_REGISTRY);
	}

	
}
