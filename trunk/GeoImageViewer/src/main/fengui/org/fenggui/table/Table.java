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
 * $Id: Table.java 284 2007-05-20 15:33:08Z schabby $
 */
package org.fenggui.table;

import org.fenggui.StandardWidget;
import org.fenggui.event.mouse.MouseDraggedEvent;
import org.fenggui.event.mouse.MouseExitedEvent;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.render.Binding;

/**
 * Implementation of a table.<br/> <br/> If you use a table within a
 * ScrollContainer you need to call <code>layout</code>
 * everytime you change your model. This notifies the
 * <code>ScrollContainer</code> to update the scroll bars.
 * 
 * 
 * 
 * 
 * @author Johannes, Rainer last edited by $Author: schabby $, $Date: 2007-05-20 17:33:08 +0200 (So, 20 Mai 2007) $
 * @version $Revision: 284 $
 * @dedication Life of Agony - Weeds
 */
public class Table extends StandardWidget
{

	private TableAppearance appearance = null;
	/**
	 * The model holding the data that the table displays.
	 */
	private ITableModel model = null;

	/**
	 * Columns of the table. No Widget, pure fabrication. Holds column related
	 * data like cell renderer, width, etc.
	 */
	private TableColumn[] columns = null;

	/**
	 * Array with the same amount of items like rows in the table. Each flag
	 * indicates whether the item is selected or now. Quite sparse usually, but
	 * used for performance reasons this way (avoids iteration).
	 */
	private boolean[] selected = null;

	/**
	 * holds the number of current selections in the table.
	 */
	private int selectionCount = 0;

	/**
	 * Indicates whether multiple selection is enabled or disabled
	 */
	private boolean multipleSelection = false;



	/**
	 * Whether the table is readonly (not selectable)
	 */
	private boolean readOnly = false;

	// both required for the headers... should go in an extra class soon

	private HeaderControl header = new HeaderControl();

	class HeaderControl
	{
		int headerY;
		int mouseX = 0;
		int columnResizeIndex = 0;
		int columnWidthBuffer = 0;
	}

	protected HeaderControl getHeader()
	{
		return header;
	}

	/**
	 * Creates a new Table.
	 */
	public Table()
	{
		super();
		appearance = new TableAppearance(this);
		
		//cellHeight = appearance.getFont().getHeight();
	}

	@Override
	public void mouseDragged(MouseDraggedEvent mp)
	{
		if(getAppearance().isTableHeadVisible()) 
		{
			if (header.columnResizeIndex > -1)
			{
				int newWidth = header.columnWidthBuffer + (mp.getDisplayX() - header.mouseX);
				int sum = getColumnWidth(header.columnResizeIndex) + getColumnWidth(header.columnResizeIndex + 1);
	
				/* make sure that the columns dont get thiner than the minimum width */
				if (newWidth < getAppearance().getColumnMinWidth() || sum - newWidth < getAppearance().getColumnMinWidth())
					return;
	
				columns[header.columnResizeIndex].setWidth(newWidth);
				columns[header.columnResizeIndex + 1].setWidth(sum - newWidth);
			}
		}
	}

	@Override
	public void mouseExited(MouseExitedEvent mouseExitedEvent)
	{
		if (header.columnResizeIndex > -1)
		{
			header.columnResizeIndex = -1;
			Binding.getInstance().getCursorFactory().getDefaultCursor().show();
		}
	}

	@Override
	public void mouseMoved(int displayX, int displayY)
	{
		if (!getAppearance().isTableHeadVisible())
		{
			return;
		}

		int widgetY = displayY - getDisplayY();

		if ((header.headerY + getAppearance().getCellHeight()) - widgetY < getAppearance().getCellHeight())
		{
			int column = isOnColumn(displayX - getDisplayX());
			if (column >= 0)
			{
				if (header.columnResizeIndex <= -1)
					Binding.getInstance().getCursorFactory().getHorizontalResizeCursor().show();
				header.columnResizeIndex = column;
				return;
			}
			else if (header.columnResizeIndex > -1)
			{
				header.columnResizeIndex = -1;
				Binding.getInstance().getCursorFactory().getDefaultCursor().show();
			}
		}
		else if (header.columnResizeIndex > -1)
		{
			header.columnResizeIndex = -1;
			Binding.getInstance().getCursorFactory().getDefaultCursor().show();
		}

	}

	@Override
	public void mousePressed(MousePressedEvent mp)
	{
		if(getAppearance().isTableHeadVisible()) 
		{
			if (header.columnResizeIndex > -1)
			{
				header.mouseX = mp.getDisplayX();
				header.columnWidthBuffer = getColumnWidth(header.columnResizeIndex);
				return;
			}
		}

		// Check if header intercept
		if (getAppearance().isTableHeadVisible() && mp.getDisplayY() - getDisplayY() >= header.headerY)
		{
			return;
		}

		// check if table is readonly
		if (readOnly)
			return;

		assertSelectionArraySize();

		int mouseY = getDisplayY() + getAppearance().getContentHeight() - mp.getDisplayY();
		mouseY += getAppearance().getCellSpacing();

		int row = (mouseY / (getAppearance().getCellHeight() + getAppearance().getCellSpacing()));

		// now that the heading is drawn the first row isn't counted!
		if (getAppearance().isTableHeadVisible())
			row--;

		// check if row is valid
		if (row < 0 || row >= selected.length) {
			// System.out.println("row not valid");
			return;
		}

		if (!selected[row])
			selectionCount++;
		else
			selectionCount--;

		if (multipleSelection)
		{
			selected[row] = !selected[row];
		}
		else
		{
			clearSelection();
			selected[row] = !selected[row];
		}
	}

	public void setSelected(int index, boolean b)
	{
		assertModel();
		assertSelectionArraySize();

		// sanity check
		if (index < 0 || index >= selected.length)
			return;

		if (multipleSelection)
		{
			// if a value is switched
			if (selected[index] != b)
			{

				selected[index] = b;

				if (b)
					selectionCount++;
				else
					selectionCount--;
			}
		}
		else
		{
			clearSelection();
			selected[index] = b;
			selectionCount = 1;
		}
	}

	public void setModel(ITableModel m)
	{
		columns = new TableColumn[m.getColumnCount()];

		selected = new boolean[m.getRowCount()];

		for (int i = 0; i < columns.length; i++)
		{
			columns[i] = new TableColumn(m.getColumnName(i));
		}

		model = m;
		
		updateMinSize();
	}

	public ITableModel getModel()
	{
		return model;
	}

	public boolean isSelected(int row)
	{
		assertModel();

		if (row >= 0 && row < selected.length)
			return selected[row];
		else
			return false;
	}

	public int getSelectionCount()
	{
		return selectionCount;
	}

	public void distributeColumnWidthsEqually()
	{
		if (model == null)
			throw new IllegalStateException("The table has no model!");

		for (int i = 0; i < columns.length; i++)
		{
			columns[i].setWidth(getAppearance().getContentWidth() / columns.length);
		}
	}

	/**
	 * Define the width in pixel a the specified column
	 * 
	 * @param columnIndex
	 *            Index of the column
	 * @param widthInPixel
	 *            Width in pixel
	 */
	public void setColumnWidth(int columnIndex, int widthInPixel)
	{
		getColumn(columnIndex).setWidth(widthInPixel);
	}

	/**
	 * Define relative width (in %) to the specified column
	 * 
	 * @param columnIndex
	 *            Index of the column
	 * @param relativeWidth
	 *            Relative width [0, 1]
	 */
	public void setColumnWidth(int columnIndex, float relativeWidth)
	{
		getColumn(columnIndex).setRelativeWidth(relativeWidth);
	}

	/**
	 * @return Returns the multipleSelection.
	 */
	public boolean isMultipleSelection()
	{
		return multipleSelection;
	}

	/**
	 * @param multipleSelection
	 *            The multipleSelection to set.
	 */
	public void setMultipleSelection(boolean multipleSelection)
	{
		this.multipleSelection = multipleSelection;
	}

	/**
	 * Deselects all entries in the table
	 * 
	 */
	public void clearSelection()
	{
		for (int i = 0; i < selected.length; i++)
		{
			selected[i] = false;
		}
	}



	@Override
	public void layout()
	{
		if (model != null && columns.length > 0 && getColumnWidth(0) == -1)
		{
			distributeColumnWidthsEqually();
		}
	}

	/**
	 * Returns the index of the first row that is selected. Should be used for
	 * getting the index when using single selection. The actual row can then be
	 * retrieved by getting the value of the index in the table model.
	 * 
	 * @return value
	 */
	public int getSelection()
	{
		assertModel();

		for (int i = 0; i < selected.length; i++)
		{
			if (selected[i] == true)
				return i;
		}
		return -1;
	}


	/**
	 * @param readOnly
	 *            The readOnly to set.
	 */
	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
	}

	public TableColumn getColumn(int columnIndex)
	{
		assertModel();

		return columns[columnIndex];
	}


	private int isOnColumn(int x)
	{
		double sum = 0;
		for (int col = 0; col < columns.length - 1; col++)
		{
			sum += getColumnWidth(col) + getAppearance().getCellSpacing();
			if (Math.abs(sum - x) < 5)
				return col;
		}

		return -1;
	}

	private void assertSelectionArraySize()
	{
		if (selected.length == model.getRowCount())
			return;

		boolean[] newSelected = new boolean[model.getRowCount()];

		for (int i = 0; i < selected.length && i < newSelected.length; i++)
		{
			newSelected[i] = selected[i];
		}

		this.selected = newSelected;
	}

	public TableAppearance getAppearance()
	{
		return appearance;
	}

	private void assertModel()
	{
		if (model == null)
			throw new IllegalStateException("No table model set!");
	}
	
	public int getColumnWidth(int columnIndex)
	{
		TableColumn column = columns[columnIndex];
		if (column != null)
		{
			if (column.isRelative()) 
			{ 
				return (int) (getAppearance().getContentWidth() * column.getRelativeWidth()); 
			}
			
			return column.getWidth();
		}
		return 0;
	}


}