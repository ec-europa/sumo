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
 * $Id: FPSLabel.java 214 2007-02-26 01:04:05Z schabby $
 */
package org.fenggui.contrib;

import org.fenggui.Label;
import org.fenggui.render.Graphics;

public class FPSLabel extends Label
{
	private long end = System.currentTimeMillis() + 1000;
	private int frameCounter = 0;
	
	@Override
	public void paint(Graphics g)
	{
		frameCounter++;
		
		long the_mooooooooment = System.currentTimeMillis();
		
		if(end <= the_mooooooooment)
		{
			setText(frameCounter+" fps");
			updateMinSize();
			setSizeToMinSize();
			end = the_mooooooooment + 1000;
			frameCounter = 0;
		}
		
		super.paint(g);
	}

	
	
}
