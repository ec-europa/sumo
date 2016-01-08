/*
 * FengGUI - Java GUIs in OpenGL (http://www.fenggui.org)
 * 
 * Copyright (c) 2005, 2006, 2007 FengGUI Project
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
 * Created on Jan 30, 2007
 * $Id: TableExample2.java $
 */

package org.fenggui.example;

import java.io.IOException;

import org.fenggui.Button;
import org.fenggui.CheckBox;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.ScrollContainer;
import org.fenggui.background.PlainBackground;
import org.fenggui.composites.Window;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.event.ISelectionChangedListener;
import org.fenggui.event.SelectionChangedEvent;
import org.fenggui.layout.Alignment;
import org.fenggui.layout.BorderLayout;
import org.fenggui.layout.BorderLayoutData;
import org.fenggui.layout.GridLayout;
import org.fenggui.render.Binding;
import org.fenggui.render.ITexture;
import org.fenggui.render.Pixmap;
import org.fenggui.table.ITableModel;
import org.fenggui.table.PixmapCellRenderer;
import org.fenggui.table.Table;
import org.fenggui.table.TableColumn;
import org.fenggui.util.Alphabet;
import org.fenggui.util.Color;
import org.fenggui.util.Spacing;

/**
 * Example class for tables.
 * 
 * 
 * 
 * @author Rainer Angermann, last edited by $Author: charlierby $, $Date: 2007-01-30 $
 * @version $Revision: 131 $
 */
public class TableExample2 implements IExample
{

	private static final long serialVersionUID = 1L;

	private Window window = null;
	private Display desk;

	@SuppressWarnings("unchecked")
	private void buildTableFrame()
	{

		window = FengGUI.createDialog(desk, "Table Test");
		window.setX(50);
		window.setY(50);
		window.setSize(400, 300);

		final ScrollContainer sc = new ScrollContainer();
		sc.setLayoutData(BorderLayoutData.CENTER);
		window.getContentContainer().addWidget(sc);
		window.getContentContainer().setLayoutManager(new BorderLayout());

		final Table table = new Table();
		sc.setInnerWidget(table);

		table.setModel(new MyTableModel());
		table.getAppearance().setGridColor(Color.LIGHT_GRAY);
		table.getAppearance().setCellHeight(20);
		table.getAppearance().setCellSpacing(20);
		
		// change the cell renderer for the first column
		TableColumn column0 = table.getColumn(0);
		column0.setCellRenderer(new PixmapCellRenderer());
		
		// set cell underlay
		table.getAppearance().getCellUnderlay().add(new PlainBackground(Color.BLACK_HALF_OPAQUE));
		
		// set entry aligment for column1
		table.getColumn(1).setEntryAlignment(Alignment.MIDDLE);
		
		Container buttons = new Container();
		buttons.setLayoutData(BorderLayoutData.SOUTH);
		buttons.setLayoutManager(new GridLayout(3, 2));
		window.getContentContainer().addWidget(buttons);

		Button createTableModelButton = new Button("Generate a New Table Model");
		createTableModelButton.getAppearance().setMargin(new Spacing(0, 5));
		buttons.addWidget(createTableModelButton);

		CheckBox multiSelection = new CheckBox("Multiselection");
		buttons.addWidget(multiSelection);

		Button updateTableModel = new Button("Update Table Model");
		updateTableModel.getAppearance().setMargin(new Spacing(0, 5));
		buttons.addWidget(updateTableModel);

		CheckBox drawGrid = new CheckBox("Draw Grid");
		drawGrid.setSelected(true);
		buttons.addWidget(drawGrid);

		Button setRelativeWidth = new Button("Set Relative width");
		setRelativeWidth.getAppearance().setMargin(new Spacing(0, 5));
		buttons.addWidget(setRelativeWidth);

		CheckBox drawHeader = new CheckBox("Draw Table Header");
		drawHeader.setSelected(true);
		buttons.addWidget(drawHeader);

		multiSelection.addSelectionChangedListener(new ISelectionChangedListener()
		{

			public void selectionChanged(SelectionChangedEvent selectionChangedEvent)
			{
				table.setMultipleSelection(selectionChangedEvent.isSelected());
			}

		});

		drawGrid.addSelectionChangedListener(new ISelectionChangedListener()
		{

			public void selectionChanged(SelectionChangedEvent selectionChangedEvent)
			{
				table.getAppearance().setGridVisible(selectionChangedEvent.isSelected());
			}

		});

		drawHeader.addSelectionChangedListener(new ISelectionChangedListener()
		{

			public void selectionChanged(SelectionChangedEvent selectionChangedEvent)
			{
				table.getAppearance().setHeaderVisible(selectionChangedEvent.isSelected());
				table.updateMinSize();
				sc.layout();
			}

		});

		createTableModelButton.addButtonPressedListener(new IButtonPressedListener()
		{

			public void buttonPressed(ButtonPressedEvent e)
			{
				table.setModel(new MyTableModel());
				table.updateMinSize();
				sc.layout();
			}

		});

		updateTableModel.addButtonPressedListener(new IButtonPressedListener()
		{

			public void buttonPressed(ButtonPressedEvent e)
			{
				((MyTableModel) table.getModel()).update();
				
				// change the cell renderer for the first column
				TableColumn column0 = table.getColumn(0);
				column0.setCellRenderer(new PixmapCellRenderer());
				
				table.updateMinSize();
				sc.layout();
			}

		});

		setRelativeWidth.addButtonPressedListener(new IButtonPressedListener()
		{

			public void buttonPressed(ButtonPressedEvent e)
			{
				table.setColumnWidth(0, 0.1f);
				table.setColumnWidth(1, 0.1f);
				table.setColumnWidth(2, 0.5f);
				table.setColumnWidth(3, 0.3f);
			}

		});
	}

	public void buildGUI(Display g)
	{
		desk = g;

		buildTableFrame();

		desk.layout();
	}

	public static String generateRandomString()
	{
		int length = (int) (Math.random() * 10) + 5;
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < length; i++)
			sb.append(Alphabet.getDefaultAlphabet().getAlphabet()[(int) (Math.random() * Alphabet.getDefaultAlphabet()
					.getAlphabet().length)]);

		return sb.toString();
	}

	class MyTableModel implements ITableModel
	{
		Object[][] matrix = null;

		public MyTableModel()
		{
			update();
		}

		public void update()
		{
			ITexture texture;
			try
			{
				texture = Binding.getInstance().getTexture("data/smiley.png");
			}
			catch (IOException e)
			{
				throw new RuntimeException("Unable to open image data/smiley.png");
			}
			matrix = new Object[20][4];
			for(int i = 0; i < 20; i++) {
				matrix[i][0] = new Pixmap(texture);
				matrix[i][1] = new String("column1 - row"+i);
				matrix[i][2] = new String("column2 - row"+i);
				matrix[i][3] = new String("column3 - row"+i);
			}
		}

		public String getColumnName(int columnIndex)
		{
			return "COLUMN" + columnIndex;
		}

		public int getColumnCount()
		{
			return matrix[0].length;
		}

		public Object getValue(int row, int column)
		{
			return matrix[row][column];
		}

		public int getRowCount()
		{
			return matrix.length;
		}

		public void clear()
		{
			// TODO implement
		}

		public Object getValue(int row)
		{
			// not used in this example
			return null;
		}
	}

	public String getExampleName()
	{
		return "Table Example (Advanced)";
	}

	public String getExampleDescription()
	{
		return "Demonstrates the Table Widget including the usage of cell renderers";
	}

}
