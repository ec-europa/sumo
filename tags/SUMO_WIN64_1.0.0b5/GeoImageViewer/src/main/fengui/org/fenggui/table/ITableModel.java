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
 * $Id: ITableModel.java 28 2006-10-05 01:37:07Z schabby $
 */
package org.fenggui.table;

/**
 * Specifies methods used by <code>Table</code> to query data.
 * 
 * @author Johannes Schaback
 *
 */
public interface ITableModel {

	/**
	 * Returns the name of the column
	 * @param columnIndex the index of the column (starting at 0).
	 * @return the name
	 */
	public String getColumnName(int columnIndex);
	
	/**
	 * Returns the number of coumns in the table
	 * @return the number of columns
	 */
	public int getColumnCount();
	
	/**
	 * Returns the value of a cell.
	 * @param row the row of the cell
	 * @param column the column of the cell
	 * @return value
	 */
	public Object getValue(int row, int column); 
	
	/**
	 * Returns the number of rows.
	 * @return rows
	 */
	public int getRowCount();

}
