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
 * Created on Dec 7, 2006
 * $Id: ListAppearance.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.io.IOException;

import org.fenggui.layout.Alignment;
import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;

/**
 * Appearance definition for the <code>List</code> widget.
 * 
 * @author Johannes, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 * @see List
 */
public class ListAppearance extends DecoratorAppearance
{
	private Font font = Font.getDefaultFont();
	private int rowHeight = font.getHeight();
	private Color textColor = Color.BLACK;
	private Alignment alignment = Alignment.LEFT;
	private DecoratorLayer selectionUnderlay = new DecoratorLayer();
	private DecoratorLayer mouseHoverUnderlay = new DecoratorLayer();
	
	private List list = null;
	
	public ListAppearance(List w, InputOnlyStream stream) throws IOException, IXMLStreamableException
	{
		super(w, stream);
		this.process(stream);
	}
	
	public ListAppearance(List w)
	{
		super(w);
		list = w;
	}

	@Override
	public Dimension getContentMinSizeHint()
	{
		int minWidth = 100;

		if (list.isEmpty()) return new Dimension(0, 0);

		return new Dimension(minWidth, list.size() * rowHeight);
	}

	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		if (list.isEmpty())
			return;

		int lowerClipBound = g.getClipSpace().getY() - rowHeight;
		int upperClipBound = g.getClipSpace().getY() + g.getClipSpace().getHeight();

		int lowerContentBound = list.getDisplayY();
		int upperContentBound = lowerContentBound + rowHeight * list.size();

		// compute start row
		int row = (upperContentBound - upperClipBound) / rowHeight;
		if (row < 0)
		{
			row = 0;
		}

		if (row > list.size())
		{
			return;
		}

		g.setFont(font);

		int y = getContentHeight() - (row + 1) * rowHeight;
		while (y + lowerContentBound > lowerClipBound && row < list.size())
		{
			ListItem<?> item = list.getItem(row);

			if (item.isSelected())
			{
				selectionUnderlay.paint(g, 0, y, getContentWidth(), rowHeight);
			}
			else if (list.getMouseOverRow() == row)
			{
				mouseHoverUnderlay.paint(g, 0, y, getContentWidth(), rowHeight);
			}

			g.setColor(textColor);
			String s = item.getText();
			
			// alignment
			int alignedX = alignment.alignX(getContentWidth(), font.getWidth(s));
			int alignedY = alignment.alignY(rowHeight, font.getHeight());
			
			g.drawString(s, alignedX, y + alignedY);

			row++;
			y -= rowHeight;
		}

	}

	public DecoratorLayer getSelectionUnderlay()
	{
		return selectionUnderlay;
	}

	public Font getFont()
	{
		return font;
	}

	public void setFont(Font font)
	{
		this.font = font;
		rowHeight = font.getHeight();
	}

	public Color getTextColor()
	{
		return textColor;
	}

	public void setTextColor(Color textColor)
	{
		this.textColor = textColor;
	}
	

	public int getRowHeight()
	{
		return rowHeight;
	}

	public void setRowHeight(int rowHeight)
	{
		this.rowHeight = rowHeight;
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		selectionUnderlay = stream.processChild("SelectionUnderlay", selectionUnderlay, new DecoratorLayer(), DecoratorLayer.class);
		mouseHoverUnderlay = stream.processChild("MouseHoverUnderlay", mouseHoverUnderlay, new DecoratorLayer(), DecoratorLayer.class);
		
		textColor = stream.processChild("Color", textColor, Color.BLACK, Color.class);
		
		if(stream.isInputStream()) // XXX: only support read-in at the moment :(
			font = stream.processChild("Font", font, Font.getDefaultFont(), Font.class);
		
		rowHeight = stream.processAttribute("rowHeight", rowHeight, font.getHeight());
		

	}

	public DecoratorLayer getMouseHoverUnderlay()
	{
		return mouseHoverUnderlay;
	}

	/**
	 * @return the alignment
	 */
	public Alignment getAlignment()
	{
		return alignment;
	}

	/**
	 * @param alignment the alignment to set
	 */
	public void setAlignment(Alignment alignment)
	{
		this.alignment = alignment;
	}

	
}
