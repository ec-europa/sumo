/*
 * FengGUI - Java GUIs in OpenGL (http://www.fenggui.org)
 * 
 * Copyright (C) 2005, 2006, 2007 FengGUI Project
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
 * Created on Dec 11, 2006
 * $Id: TableAppearance.java 284 2007-05-20 15:33:08Z schabby $
 */
package org.fenggui.table;

import org.fenggui.DecoratorAppearance;
import org.fenggui.DecoratorLayer;
import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.table.Table.HeaderControl;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;

public class TableAppearance extends DecoratorAppearance
{
	private Table table = null;
	private int cellHeight = 20;
	//private int columnMinWidth = Font.getDefaultFont().getWidth("...") + OFFSET * 2;
	
	// TODO replace with cell padding (someday ;o) )
	private static final int OFFSET = 5; // space left between entry and grid
	
	private DecoratorLayer cellUnderlay = new DecoratorLayer();
	private DecoratorLayer cellOverlay = new DecoratorLayer();
	
	private int cellSpacing = 0;
	
	/**
	 * Inidicates if the grid is rendered
	 */
	private boolean gridVisible = true;


	/**
	 * Toggles drawing of table head
	 */
	private boolean tableHeaderVisible = true;

	private Color headerBackgroundColor = Color.GRAY;

	/**
	 * The grid color
	 */
	private Color gridColor = Color.GRAY;

	/**
	 * The text color
	 */
	private Color textColor = Color.BLACK;

	/**
	 * The color of the selection
	 */
	private Color selectionColor = Color.RED;

	/**
	 * The color of the table head
	 */
	private Color headTextColor = Color.GREEN;

	/**
	 * The table's font
	 */
	private Font font = Font.getDefaultFont();
	
	public TableAppearance(Table w)
	{
		super(w);
		table = w;
		
		cellHeight = font.getHeight();
	}

	@Override
	public Dimension getContentMinSizeHint()
	{
		int rowHeight = font.getHeight();
		// int minWidth = 0;

		ITableModel model = table.getModel();

		if (model == null) return new Dimension(0,0);
		/*
		 * for (int i = 0; i < model.getColumnCount(); i++) { int max = 0;
		 * for(int row = 0; row < model.getRowCount(); row++) { if(max <
		 * font.getWidth(""+model.getValue(row, i))) max =
		 * font.getWidth(""+model.getValue(row, i)); } minWidth += max; }
		 */

		int numberOfRows = model.getRowCount();

		if (tableHeaderVisible)
			numberOfRows++;

		// I set the width statically to 100 because entries may be wider than
		// allowed
		// such that they are clipped around the cell
		return new Dimension(100, numberOfRows * rowHeight + numberOfRows * cellSpacing);
	}

	
	
	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		ITableModel model = table.getModel();
		HeaderControl header = table.getHeader();
		
		if (model == null)
			return;

//		if (model.getRowCount() == 0)
//			return;

		int x = 0;
		int y = 0;

		// calculate start y
		y = getContentHeight() - cellHeight;

		// calculate scaled column widths
		// int[] scaledColWidth = new int[model.getColumnCount()];
		float freeSpace = table.getWidth();

		for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++)
		{
			freeSpace -= table.getColumnWidth(columnIndex);
		}

		g.setFont(font);

		if (isTableHeadVisible())
			y -= cellHeight + cellSpacing;

		final int lowerClipBound = g.getClipSpace().getY() - (cellHeight + cellSpacing);
		final int upperClipBound = g.getClipSpace().getY() + g.getClipSpace().getHeight();

		final int lowerContentBound = getWidget().getDisplayY();
		final int upperContentBound = lowerContentBound + (cellHeight + cellSpacing) * (model.getRowCount());		
		
		int row = (upperContentBound - upperClipBound) / (cellHeight + cellSpacing);
		
		
		if (row < 0)
		{
			row = 0;
		}

		if (row > model.getRowCount())
		{
			return;
		}		
		
		// draw entries
		y = getContentHeight() - (row + (tableHeaderVisible?2:1)) * (cellHeight + cellSpacing);
		
		while (y + lowerContentBound > lowerClipBound && row < model.getRowCount())
		{
			x = 0;

			// draw selection
			if (table.isSelected(row))
			{
				g.setColor(selectionColor);
				g.drawFilledRectangle(0, y, getContentWidth(), cellHeight);
			}

			// draw cell content
			for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++)
			{
				// draw cell underlay
				cellUnderlay.paint(g, x, y, table.getColumnWidth(columnIndex), cellHeight);				
				
				g.setColor(textColor);
				
				Object value = model.getValue(row, columnIndex);
				
				// only paint if value not null
				if (value != null) {
					ICellRenderer cellRenderer = table.getColumn(columnIndex).getCellRenderer();
					Dimension contentDimension = cellRenderer.getCellContentSize(value);
					if(contentDimension == null)
						contentDimension = new Dimension(table.getColumnWidth(columnIndex), cellHeight);
					
					int alignedX = x + table.getColumn(columnIndex).getEntryAlignment().alignX(table.getColumnWidth(columnIndex), contentDimension.getWidth());
					int alignedY = y + table.getColumn(columnIndex).getEntryAlignment().alignY(cellHeight, contentDimension.getHeight());
					cellRenderer.paint(g, value, alignedX, alignedY, table.getColumnWidth(columnIndex), cellHeight);
				}
				
				// draw cell overlay
				cellOverlay.paint(g, x, y, table.getColumnWidth(columnIndex), cellHeight);	
				
				// draw grid around cell
				if(gridVisible) {
					g.setColor(gridColor);
					
					g.drawLine(x, y, x + table.getColumnWidth(columnIndex), y);
					g.drawLine(x + table.getColumnWidth(columnIndex), y,  x + table.getColumnWidth(columnIndex), y + cellHeight);
					g.drawLine(x + table.getColumnWidth(columnIndex), y + cellHeight, x, y + cellHeight);
					g.drawLine(x, y + cellHeight, x, y);
				}
				
				x += table.getColumnWidth(columnIndex) + cellSpacing;
			}

			// draw grid
//			if (gridVisible)
//			{
//				g.setColor(gridColor);
//				g.drawLine(0, y, x, y);
//			}

			row++;
			y -= (cellHeight + cellSpacing);
		}

		// draw table head
		if (tableHeaderVisible)
		{
			// calc display y relative to widget coordinates
			header.headerY = g.getClipSpace().getHeight() - cellHeight - table.getY();

			g.setColor(headerBackgroundColor);
			g.drawFilledRectangle(0, header.headerY, getContentWidth(), font.getHeight());

			x = 0;
			g.setColor(headTextColor);

			for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++)
			{
				String s = model.getColumnName(columnIndex);
				int columnWidth = table.getColumnWidth(columnIndex);

				s = font.confineLength(s, columnWidth - OFFSET);

				int entryOffset = OFFSET + table.getColumn(columnIndex).getHeaderAlignment().alignX(columnWidth - OFFSET, font.getWidth(s));

				g.setFont(font);
				g.drawString(s, x + entryOffset, header.headerY);
				x += columnWidth + cellSpacing;
			}
		}

		// draw grid
//		if (gridVisible)
//		{
//			// draw vertical grid lines
//			g.setColor(gridColor);
//			x = 0;
//			for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++)
//			{
//				g.drawLine(x, 0, x, getContentHeight());
//				x += table.getColumnWidth(columnIndex);
//			}
//
//			g.drawLine(x, 0, x, getContentHeight());
//
//			// draw top horizontal grid line
//			g.drawLine(0, y + cellHeight - 1, getContentWidth(), y + cellHeight - 1);
//		}

	}
	

	/**
	 * @param gridColor
	 *            The gridColor to set.
	 */
	public void setGridColor(Color gridColor)
	{
		this.gridColor = gridColor;
	}

	/**
	 * @param textColor
	 *            The textColor to set.
	 */
	public void setTextColor(Color textColor)
	{
		this.textColor = textColor;
	}


	/**
	 * @param font
	 *            The font to set.
	 */
	public void setFont(Font font)
	{
		this.font = font;
		cellHeight = font.getHeight();
		getWidget().updateMinSize();
	}

	public Font getFont()
	{
		return font;
	}

	public Color getHeadTextColor()
	{
		return headTextColor;
	}

	public void setHeadTextColor(Color headTextColor)
	{
		this.headTextColor = headTextColor;
	}

	public Color getSelectionColor()
	{
		return selectionColor;
	}

	public void setSelectionColor(Color selectionColor)
	{
		this.selectionColor = selectionColor;
	}

	public Color getGridColor()
	{
		return gridColor;
	}

	public Color getTextColor()
	{
		return textColor;
	}

	public int getCellHeight()
	{
		return cellHeight;
	}

	public void setCellHeight(int cellHeight)
	{
		this.cellHeight = cellHeight;
	}

	/**
	 * @param gridVisible
	 *            The gridVisible to set.
	 */
	public void setGridVisible(boolean gridVisible)
	{
		this.gridVisible = gridVisible;
	}
	

	public boolean isTableHeadVisible()
	{
		return tableHeaderVisible;
	}
	


	/**
	 * @param drawTableHead
	 *            The drawTableHead to set.
	 */
	public void setHeaderVisible(boolean drawTableHead)
	{
		this.tableHeaderVisible = drawTableHead;
		getWidget().updateMinSize();
	}

	/**
	 * @return the cellSpacing
	 */
	public int getCellSpacing()
	{
		return cellSpacing;
	}

	/**
	 * @param cellSpacing the cellSpacing to set
	 */
	public void setCellSpacing(int cellSpacing)
	{
		this.cellSpacing = cellSpacing;
		getWidget().updateMinSize();
	}

	/**
	 * @return the cellOverlay
	 */
	public DecoratorLayer getCellOverlay()
	{
		return cellOverlay;
	}

	/**
	 * @return the cellUnderlay
	 */
	public DecoratorLayer getCellUnderlay()
	{
		return cellUnderlay;
	}

	/**
	 * @return the headerBackgroundColor
	 */
	public Color getHeaderBackgroundColor()
	{
		return headerBackgroundColor;
	}

	/**
	 * @param headerBackgroundColor the headerBackgroundColor to set
	 */
	public void setHeaderBackgroundColor(Color headerBackgroundColor)
	{
		this.headerBackgroundColor = headerBackgroundColor;
	}

	public int getColumnMinWidth()
	{
		return 15;
	}


}
