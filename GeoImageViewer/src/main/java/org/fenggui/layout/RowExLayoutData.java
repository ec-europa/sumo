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
 * Created on 2007-8-26
 */
package org.fenggui.layout;

/**
 * Data object to give the RowExLayout hints on how to layout a widget.
 * 
 * @author Marc Menghin
 */
public class RowExLayoutData implements ILayoutData
{
	private boolean fill = true;
	private boolean grab = false;
	private double weight = 1.0d;
	private Alignment align = Alignment.MIDDLE;

	public static final RowExLayoutData DEFAULT = new RowExLayoutData();

	public RowExLayoutData()
	{
	}

	public RowExLayoutData(boolean fill, boolean grab)
	{
		this.fill = fill;
		this.grab = grab;
	}

	public RowExLayoutData(boolean fill, boolean grab, double weight)
	{
		this(fill, grab);
		this.weight = weight;
	}

	public RowExLayoutData(boolean fill, boolean grab, double weight, Alignment align)
	{
		this(fill, grab, weight);
		this.align = align;
	}

	public boolean isFill()
	{
		return fill;
	}

	public boolean isGrab()
	{
		return grab;
	}

	public double getWeight()
	{
		return weight;
	}

	public Alignment getAlign()
	{
		return align;
	}
}
