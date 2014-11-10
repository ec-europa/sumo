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
 * $Id: GridLayout.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.layout;

import java.io.IOException;
import java.util.List;

import org.fenggui.Container;
import org.fenggui.IWidget;
import org.fenggui.LayoutManager;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Dimension;

/**
 * Layouts Widgets in a Container as a grid.
 * 
 * @author Johannes, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public class GridLayout extends LayoutManager 
{
	private int rows, columns;

	public GridLayout(int rows, int columns) 
	{
		super();
		this.rows = rows;
		this.columns = columns;
	}

	@Override
	public Dimension computeMinSize(Container container, List<IWidget> content)
	{
		if(content.isEmpty()) return new Dimension(0, 0);
		
		IWidget[][] grid = buildWidgetGrid(content);
		
		int minHeight = 0;
		int minWidth = 0;
		
		for(int row = 0; row < rows; row++)
		{
			minHeight += getRowMinHeight(grid, row);
		}
		
		for(int column = 0; column < columns; column++)
		{
			minWidth += getColumnMinWidth(grid, column);
		}
		
		return new Dimension(minWidth, minHeight);
	}

	/**
	 * Returns a 2-dimensional, (rows)x(columns) matrix containing the widgets
	 * in a grid. Not that the first index iterates through the rows, the
	 * second index iterates through the columns.
	 * @param content the list which contains the widget to put in the grid
	 * @return the grid
	 */
	private IWidget[][] buildWidgetGrid(List<IWidget> content)
	{
		IWidget[][] array = new IWidget[rows][columns];
		int i = 0;
		
		for(IWidget w: content)
		{
			//System.out.println((i/columns)+", "+(i % columns)+" ---> "+w.getClass().getSimpleName());
			array[i/columns][i % columns] = w;
			i++;
			
			if(i > columns*rows)
			{
				throw new IllegalArgumentException("A "+rows+"x"+columns+" Gridlayout too small for "+
					content.size()+" widgets. Increase number of rows/columns.");
			}
		}
		//System.out.println("schluss");
		return array;
	}
	
	private int getColumnMinWidth(IWidget[][] array, int column)
	{
		int width = 0;
		
		for(int i=0; i < rows; i++)
		{
			if(array[i][column] == null) continue;
			width = Math.max(width, getValidMinWidth(array[i][column]));
		}
		
		return width;
	}
	
	private int getRowMinHeight(IWidget[][] array, int row)
	{
		int height = 0;
		
		for(int i=0; i < columns; i++)
		{
			if(array[row][i] == null) continue;
			height = Math.max(height, getValidMinHeight(array[row][i]));
		}
		
		return height;
	}
	
	
	@Override
	public void doLayout(Container container, List<IWidget> content)
	{
		IWidget[][] grid = buildWidgetGrid(content);
		
		Dimension minSize = container.getAppearance().getContentMinSizeHint();
		
		int additionalHorizontalSpace = (container.getAppearance().getContentWidth() - minSize.getWidth())/columns;
		int additionalVerticalSpace = (container.getAppearance().getContentHeight() - minSize.getHeight())/rows;
		
		// this should actually never happen
		if(additionalHorizontalSpace < 0) additionalHorizontalSpace = 0;
		if(additionalVerticalSpace < 0) additionalVerticalSpace = 0;
		
		int[] columnMinWidths = new int[columns];
		
		for(int column = 0; column < columns; column++)
		{
			columnMinWidths[column] = getColumnMinWidth(grid, column);
		}
		
		int y = container.getAppearance().getContentHeight();
		
		for(int i = 0; i < rows; i++)
		{
			int x = 0;
			int height = getRowMinHeight(grid, i) + additionalVerticalSpace;
			y -= height;
			
			for(int j = 0; j < columns; j++)
			{
				IWidget w = grid[i][j];
				if(w == null) continue;
				w.setX(x);
				w.setY(y);
				w.setSize(new Dimension(columnMinWidths[j] + additionalHorizontalSpace, height));
				//super.setValidWidth(w, );
				//super.setValidHeight(w, height);
				x += columnMinWidths[j] + additionalHorizontalSpace;
			}
		}
		
		
	}

	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		rows = stream.processAttribute("rows", rows);
		columns = stream.processAttribute("columns", columns);
	}

}
