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
 * Created on Dec 12, 2006
 * $Id: TreeAppearance.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.tree;

import java.io.IOException;
import java.util.ArrayList;

import org.fenggui.DecoratorAppearance;
import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;

public class TreeAppearance<E> extends DecoratorAppearance
{

	private Tree<E> tree;
	private Pixmap minusIcon;
	private Pixmap plusIcon;
	private Color textColor = Color.BLACK;
	private Color selectionColor = Color.LIGHT_BLUE;
	private Font font = Font.getDefaultFont();
	public static final int ICON_OFFSET = 15;
	public static final int OFFSET = 15;
	private int counter = 0;
	
	public TreeAppearance(Tree<E> w)
	{
		super(w);
		tree = w;
	}

	@Override
	public Dimension getContentMinSizeHint()
	{
		Record<E> root = tree.getRoot();
		ITreeModel<E> model = tree.getModel();
		
		if (root == null) return new Dimension(0,0);
		ArrayList<Record<E>> stack = new ArrayList<Record<E>>();
		ArrayList<Integer> offsetStack = new ArrayList<Integer>();
		stack.add(root);
		offsetStack.add(0);
		int width = 0;
		int height = 0;
		
		while (!stack.isEmpty())
		{
			Record<E> r = stack.remove(0);
			int offset = offsetStack.remove(0);
			int recordWidth = offset + ICON_OFFSET + 3 + font.getWidth(model.getText(r.getNode()));
			if (width < recordWidth) width = recordWidth;
			
			height += font.getHeight();
			
			for (Record<E> p : r.getChildren())
			{
				stack.add(p);
				offsetStack.add(offset + OFFSET);
			}
		}

		return new Dimension(width, height);
	}

	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		if(tree.getRoot() == null) return;
		counter = 0;
		g.setColor(textColor);
		paintRecord(g, tree.getRoot(), true, false);
	}
	

	public Pixmap getMinusIcon()
	{
		return minusIcon;
	}


	public void setMinusIcon(Pixmap minusIcon)
	{
		this.minusIcon = minusIcon;
	}


	public Pixmap getPlusIcon()
	{
		return plusIcon;
	}


	public void setPlusIcon(Pixmap plusIcon)
	{
		this.plusIcon = plusIcon;
	}



	public Color getSelectionColor()
	{
		return selectionColor;
	}


	public void setSelectionColor(Color selectionColor)
	{
		this.selectionColor = selectionColor;
	}


	public Color getTextColor()
	{
		return textColor;
	}


	public void setTextColor(Color textColor)
	{
		this.textColor = textColor;
	}

	public Font getFont()
	{
		return font;
	}

	private void paintRecord(Graphics g, Record<E> node, boolean isLastOne, boolean hasSisters)
	{
		node.row = counter;
		counter++;
		ITreeModel<E> model = tree.getModel();
		int y = getContentHeight() - counter * font.getHeight();

		if (node.isSelected())
		{
			g.setColor(selectionColor);
			g.drawFilledRectangle(node.getOffset() + ICON_OFFSET + 1, y, font.getWidth(model.getText(node.getNode())) + 3, font
					.getHeight());
			g.setColor(Color.WHITE);
		}
		else g.setColor(textColor);

		g.drawString(model.getText(node.getNode()), node.getOffset() + ICON_OFFSET + 3, y);
		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(node.getOffset() + plusIcon.getWidth() / 2, y + font.getHeight() / 2, node.getOffset() + ICON_OFFSET, y
				+ font.getHeight() / 2);


		if (node.getNumberOfChildren() > 0)
		{
			g.drawLine(node.getOffset() + OFFSET + plusIcon.getWidth() / 2, y + font.getHeight() / 2, node.getOffset() + OFFSET
					+ plusIcon.getWidth() / 2, y - (node.getNumberOfChildren() * font.getHeight()) + font.getHeight() / 2);
		}


		for (int i = 0; i < node.getNumberOfChildren(); i++)
		{
			paintRecord(g, node.getChild(i), i + 1 >= node.getNumberOfChildren(), false);
		}

		if (!isLastOne)
		{
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(node.getOffset() + plusIcon.getWidth() / 2, y + font.getHeight()
					+ (font.getHeight() / 2 - minusIcon.getHeight() / 2), node.getOffset() + plusIcon.getWidth() / 2,
				getContentHeight() - counter * font.getHeight()
						- (font.getHeight() / 2 - plusIcon.getHeight() / 2));
		}

		g.setColor(Color.WHITE);

		if (node.getNumberOfChildren() > 0)
		{
			g.drawImage(minusIcon, node.getOffset(), y + (font.getHeight() / 2 - minusIcon.getHeight() / 2));
		}
		else if (node.isExpandable())
		{
			g.drawImage(plusIcon, node.getOffset(), y + (font.getHeight() / 2 - plusIcon.getHeight() / 2));
		}
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		minusIcon = stream.processChild("MinusIconPixmap", minusIcon, Pixmap.class);
		plusIcon = stream.processChild("PlusIconPixmap", plusIcon, Pixmap.class);
		textColor = stream.processChild("TextColor", textColor, Color.BLACK, Color.class);
		selectionColor = stream.processChild("SelectionColor", selectionColor, Color.LIGHT_BLUE, Color.class);
	}

	public void setFont(Font font)
	{
		this.font = font;
	}
	
	
}
