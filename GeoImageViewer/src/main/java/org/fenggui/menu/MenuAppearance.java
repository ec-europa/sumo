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
 * $Id: MenuAppearance.java 356 2007-09-20 08:40:12Z marcmenghin $
 */
package org.fenggui.menu;

import java.io.IOException;

import org.fenggui.DecoratorAppearance;
import org.fenggui.DecoratorLayer;
import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;

public class MenuAppearance extends DecoratorAppearance
{
	private Menu menu = null; 
	private DecoratorLayer decoratorUnderlay = new DecoratorLayer();
	
	/**
	 * height of menu items.
	 */
    private int cellHeight = 20;
    
    /**
     * Font used to render the text of menu items.
     */
	private Font font = Font.getDefaultFont();
	
	/**
	 * Color to render the text of menu items
	 */
	private Color textColor = Color.BLACK;

	private Color textSelectionColor = Color.BLACK;

	/**
	 * Color to draw the text of disabled menu items
	 */
	private Color disabledColor = Color.GRAY;

	
	public MenuAppearance(Menu w)
	{
		super(w);
		menu =w ;
	}

	@Override
	public Dimension getContentMinSizeHint()
	{
		int minWidth = 0;
		
		for(MenuItem item: menu.getItems()) 
		{
			int w = font.getWidth(item.getText())+10;
			
			if(minWidth < w) 
			{
				minWidth = w;
			}
		}
		
		return new Dimension(minWidth+10, cellHeight*menu.getItemCount());
	}

	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		if(menu.getItemCount() == 0) return;
		
		int y = getContentHeight()-cellHeight;
		
        g.setFont(font);
         
		for(int row = 0; row < menu.getItemCount();row++) {
			
			MenuItem item = menu.getMenuItem(row);
			
			if(menu.getMouseOverRow() == row) 
			{
				decoratorUnderlay.paint(g, 0, y-cellHeight/7, getContentWidth(), cellHeight);
			}
			
			if(item.isEnabled()) 
			{
				if(menu.getMouseOverRow() == row)
					g.setColor(textSelectionColor);
				else g.setColor(textColor);
			} 			
			else 
				g.setColor(disabledColor);
			
			if(item.menu != null) 
			{
				int tx = getContentWidth();
				g.drawTriangle(tx-5, y+2, tx-5, y+12, tx-2, y+7, true);
			}
			
			item.getTextRenderer().render(3, y, g, gl);
			
			y -= cellHeight;
		}
	}

    /**
     * Returns color used to draw the text of disabled menu items.
     * @return color
     */
	public Color getDisabledColor() {
		return disabledColor;
	}

	
	public void setDisabledColor(Color disabledColor) {
		this.disabledColor = disabledColor;
	}

	public Color getTextColor() {
		return textColor;
	}


	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}


	public int getCellHeight() 
	{
		return cellHeight;
	}


	public void setCellHeight(int cellHeight) {
		this.cellHeight = cellHeight;
	}


	public Font getFont() {
		return font;
	}


	public void setFont(Font font) {
		this.font = font;
	}

	public DecoratorLayer getSelectionUnderlay()
	{
		return decoratorUnderlay;
	}

	public Color getTextSelectionColor()
	{
		return textSelectionColor;
	}

	public void setTextSelectionColor(Color textSelectionColor)
	{
		this.textSelectionColor = textSelectionColor;
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		cellHeight = stream.processAttribute("cellHeight", cellHeight, cellHeight);
		disabledColor = stream.processChild("DisabledTextColor", disabledColor, disabledColor, Color.class);
		
		if(stream.isInputStream()) // XXX: only support read-in at the moment :(
		      setFont(stream.processChild("Font", getFont(), Font.getDefaultFont(), Font.class));

		textColor = stream.processChild("Color", textColor, Color.class);
		textSelectionColor = stream.processChild("SelectionTextColor", textSelectionColor, textSelectionColor, Color.class);
		decoratorUnderlay = stream.processChild("SelectionUnderlay", decoratorUnderlay, decoratorUnderlay, DecoratorLayer.class);
	}

	
}
