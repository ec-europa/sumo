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
 * $Id: ProgressBar.java 161 2007-01-28 19:01:39Z schabby $
 */
package org.fenggui;


/**
 * Horizontal progress bar Widget.
 * 
 * @author Johannes, last edited by $Author: schabby $, $Date: 2007-01-28 20:01:39 +0100 (Sun, 28 Jan 2007) $
 * @version $Revision: 161 $
 */
public class ProgressBar extends StandardWidget {

	private double value = 0.5;
	private String text="Working...";

	private ProgressBarAppearance appearance = null;

	public ProgressBar(String text)
	{
		setText(text);
		appearance = new ProgressBarAppearance(this);
		setupTheme(ProgressBar.class);
		updateMinSize();
	}
	
	public ProgressBar()
	{
		this(null);
	}
	
	public double getValue() 
	{
		return value;
	}


	public void setValue(double value) 
	{
		if(value > 1) value = 1;
		if(value < 0) value = 0;
		this.value = value;
	}
	
	public boolean isHorizontal() 
	{
		return true;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) 
	{
		this.text = text;
		updateMinSize();
	}


	public ProgressBarAppearance getAppearance()
	{
		return appearance;
	}
	
	
}
