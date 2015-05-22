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
 * $Id: TableColumn.java 28 2006-10-05 01:37:07Z schabby $
 */
package org.fenggui.table;

import org.fenggui.layout.Alignment;

/**
 * Class that manages columns in tables.
 * 
 * @author Johannes Schaback
 * 
 */
public class TableColumn
{

	private ICellRenderer cellRenderer = new TextCellRenderer();
	
	/**
	 * The name (or better title) of the column which is displayed at the head
	 */
	private String name = "---";

	/**
	 * The width of the column in pixels
	 */
	private int width = -1;

	/**
	 * The width of the column relative to the table width
	 */
	private float relativeWidth = -1;

	private Alignment headingAlignment = Alignment.MIDDLE;
	private Alignment entryAlignment = Alignment.LEFT;

	/**
	 * Creates a new TableColumn
	 * 
	 * @param name
	 *            the title of the column
	 */
	protected TableColumn(String name)
	{
		this.name = name;
	}

	/**
	 * TableColumn constructor
	 *
	 * @param name
	 * @param width
	 */
	protected TableColumn(String name, int width)
	{
		this.name = name;
		setWidth(width);
	}

	/**
	 * TableColumn constructor
	 *
	 * @param name
	 * @param relativeWidth
	 */
	protected TableColumn(String name, float relativeWidth)
	{
		this.name = name;
		setRelativeWidth(relativeWidth);
	}
	
	/**
	 * Returns the name of the column
	 * 
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the width of the column in pixels
	 * 
	 * @return width the width of the column
	 */
	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
		this.relativeWidth = -1;
	}

	/**
	 * Returns the width of the column relative to the table width.
	 * 
	 * @return relativeWidth
	 */
	public float getRelativeWidth()
	{
		return relativeWidth;
	}

	/**
	 * @param relativeWidth
	 *            to define
	 */
	public void setRelativeWidth(float relativeWidth)
	{
		relativeWidth = Math.max(0, Math.min(1, relativeWidth));
		this.relativeWidth = relativeWidth;
		this.width = 0;
	}

	/**
	 * @return <code>true</code> is the relativeWidth is defined
	 *         <code>false</code> else
	 */
	public boolean isRelative()
	{
		return relativeWidth != -1;
	}

	public Alignment getHeaderAlignment()
	{
		return headingAlignment;
	}

	public void setHeaderAlignment(Alignment headingAlignment)
	{
		if (headingAlignment == null)
			return;
		this.headingAlignment = headingAlignment;
	}

	public Alignment getEntryAlignment()
	{
		return entryAlignment;
	}

	public void setEntryAlignment(Alignment valueAlignment)
	{
		if (valueAlignment == null)
			return;
		this.entryAlignment = valueAlignment;
	}

	public ICellRenderer getCellRenderer()
	{
		return cellRenderer;
	}

	public void setCellRenderer(ICellRenderer cellRenderer)
	{
		if(cellRenderer == null) throw new IllegalArgumentException("cellRenderer == null");
			
		this.cellRenderer = cellRenderer;
	}
	
	

}
