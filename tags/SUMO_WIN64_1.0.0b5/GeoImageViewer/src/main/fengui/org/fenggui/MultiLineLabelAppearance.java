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
 * Created on Dec 11, 2006
 * $Id: MultiLineLabelAppearance.java 205 2007-02-15 20:18:04Z charlierby $
 */
package org.fenggui;

import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.util.Dimension;

public class MultiLineLabelAppearance extends LabelAppearance
{
	
	private MultiLineLabel label = null;
	
	public MultiLineLabelAppearance(MultiLineLabel w)
	{
		super(w);
		label = w;
	}

	@Override
	public Dimension getContentMinSizeHint()
	{
		String[] text = label.getTextArray();
		
		if (text == null)
			return new Dimension(0, 0);

		int width = 0;
		for (String s : text) {
			int length = getFont().getWidth(s);
			if (width < length)
				width = length;
		}

		int height = text.length * getFont().getHeight();

		return new Dimension(width, height);
	}

	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		String[] text = label.getTextArray();
		if(text != null && text.length > 0)
        {
            g.setFont(getFont());
            
            if(getTextColor() != null) g.setColor(getTextColor());
            
            int x = 0;
            int y = getAlignment().alignY(getContentHeight(), text.length * getFont().getHeight());
            
            for(int i = text.length - 1; i >= 0; i--) 
            {
            	String toDraw = text[i];
            	if(toDraw.length() > 0) 
            	{
            		x = getAlignment().alignX(getContentWidth(), getFont().getWidth(toDraw));
            		g.drawString(toDraw, x, y);
            		y += getFont().getHeight();
            	}
            }
        }
	}

	
	
}
