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
 * Created on Feb 25, 2007
 * $Id: FPSLabelExample.java 214 2007-02-26 01:04:05Z schabby $
 */
package org.fenggui.example;

import org.fenggui.Display;
import org.fenggui.contrib.FPSLabel;
import org.fenggui.render.Font;
import org.fenggui.util.Alphabet;
import org.fenggui.util.fonttoolkit.FontFactory;

public class FPSLabelExample implements IExample
{

	public void buildGUI(Display display)
	{
		FPSLabel label = new FPSLabel();
		display.addWidget(label);
		label.setXY(display.getWidth() - 90, 10);
		
		Font font = FontFactory.renderStandardFont(new java.awt.Font("Serif", java.awt.Font.PLAIN, 24), true, Alphabet.ENGLISH);
		label.getAppearance().setFont(font);
	}

	public String getExampleDescription()
	{
		return "FPS Label";
	}

	public String getExampleName()
	{
		return "FPS Label";
	}

}
