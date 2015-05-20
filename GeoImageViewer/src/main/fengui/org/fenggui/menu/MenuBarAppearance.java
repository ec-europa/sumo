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
 * $Id: MenuBarAppearance.java 357 2007-09-20 16:09:10Z marcmenghin $
 */
package org.fenggui.menu;

import java.io.IOException;

import org.fenggui.DecoratorAppearance;
import org.fenggui.DecoratorLayer;
import org.fenggui.background.Background;
import org.fenggui.background.PlainBackground;
import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;

public class MenuBarAppearance extends DecoratorAppearance
{
	private Font font = Font.getDefaultFont();
  private Color textColor = Color.BLACK;
	private int GAP = 10;
  private DecoratorLayer selectionUnderlay = new DecoratorLayer();
	private Color selectionTextColor = Color.BLACK;
    
	public MenuBarAppearance(MenuBar bar)
	{
	  super(bar);
	}
	
	/**
	 * returns always null. Will be removed soon!
	 * @return
	 * @throws Exception 
	 */
	@Deprecated
	public Background getBackground()
	{
	  //INFO: not possible anymore
	  throw new UnsupportedOperationException("This is not possible anymore");
	}

	 @Override
	 public void paintContent(Graphics g, IOpenGL gl) 
	{
		MenuBar menuBar = (MenuBar) getWidget();
		int x = 0;
		
		g.setFont(font);
		
		for(MenuBarItem item: menuBar.getMenuBarItems()) 
		{
			int itemWidth = item.getWidth();

			g.setColor(textColor);
			
			if(item.equals(menuBar.getMouseOver())) 
			{
				selectionUnderlay.paint(g, x, 0, itemWidth+ GAP, font.getHeight());

				g.setColor(selectionTextColor);
			}
			
			item.getTextRenderer().render(x+GAP/2, 0, g, gl);
			
			//g.drawString(item.getName(), x+GAP/2, 0);
			
			x += itemWidth + GAP;
		}
	}
	
	public Color getSelectionTextColor()
	{
		return selectionTextColor;
	}

	public void setSelectionTextColor(Color selectionTextColor)
	{
		this.selectionTextColor = selectionTextColor;
	}

	public DecoratorLayer getSelectionUnderlay()
	{
		return selectionUnderlay;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public Color getTextColor() {
		return textColor;
	}

	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}
	
	@Deprecated
	public void setBackground(Background background)
	{
	  this.add(background);
	}

	public int getGap()
	{
		return GAP;
	}

	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
	  super.process(stream);
		selectionTextColor = stream.processChild("SelectionTextColor", selectionTextColor, Color.BLACK, Color.class);
		selectionUnderlay = stream.processChild("SelectionUnderlay", selectionUnderlay, selectionUnderlay, DecoratorLayer.class);
    
		if(stream.isInputStream()) // XXX: only support read-in at the moment :(
      setFont(stream.processChild("Font", getFont(), Font.getDefaultFont(), Font.class));
    
		textColor = stream.processChild("Color", textColor, Color.BLACK, Color.class);
	}

	/* (non-Javadoc)
	 * @see org.fenggui.io.IOStreamSaveable#getUniqueName()
	 */
	public String getUniqueName() {
		return GENERATE_NAME;
	}

  @Override
  public Dimension getContentMinSizeHint() {
    int sum = ((MenuBar) getWidget()).getMenuBarItemCount() * GAP;
    
    for(MenuBarItem item: ((MenuBar) getWidget()).getMenuBarItems()) 
    {
      int itemWidth = font.getWidth(item.getName());
      sum += itemWidth;
    }
    
    return new Dimension(sum, font.getHeight());
  }
}
