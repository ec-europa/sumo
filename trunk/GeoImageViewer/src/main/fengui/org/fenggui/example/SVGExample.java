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
 * Created on Apr 10, 2007
 * $Id: SVGExample.java 324 2007-08-11 10:20:48Z Schabby $
 */
package org.fenggui.example;

import java.awt.image.BufferedImage;

import org.fenggui.Display;
import org.fenggui.Label;
import org.fenggui.render.Binding;
import org.fenggui.render.ITexture;
import org.fenggui.render.Pixmap;
import org.fenggui.util.SVGImageFactory;

public class SVGExample implements IExample
{
	
	
	public void buildGUI(Display display)
	{
		try
		{
			addSVGTiger(display, 100, 100, 50, 50);
			addSVGTiger(display, 200, 200, 100, 100);
			addSVGTiger(display, 300, 300, 150, 150);
			addSVGTiger(display, 400, 400, 250, 250);
			addSVGTiger(display, 500, 500, 300, -100);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void addSVGTiger(Display display, int width, int height, int x, int y) throws Exception
	{
		BufferedImage svg = SVGImageFactory.createSVGImage("data/xml/tiger.svg", width, height);
		ITexture texture = Binding.getInstance().getTexture(svg);
		Pixmap pixmap = new Pixmap(texture);
		Label label = new Label();
		label.setSize(width, height);
		label.setXY(x, y);
		label.setPixmap(pixmap);
		display.addWidget(label);		
	}
	
	public String getExampleDescription()
	{
		return "SVG Example";
	}

	public String getExampleName()
	{
		return "SVG Example";
	}

}
