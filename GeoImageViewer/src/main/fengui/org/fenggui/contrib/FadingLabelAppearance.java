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
 * $Id: FadingLabelAppearance.java 335 2007-08-12 12:30:24Z Schabby $
 */
package org.fenggui.contrib;

import org.fenggui.ILabel;
import org.fenggui.LabelAppearance;
import org.fenggui.layout.Alignment;
import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.render.Pixmap;
import org.fenggui.util.Color;

/**
 * Label appearance that allows to fade labels by adjusting a global alpha value.
 * 
 * @author Rainer Angermann, last edited by $Author: Schabby $, $Date: 2007-08-12 14:30:24 +0200 (So, 12 Aug 2007) $
 * @version $Revision: 335 $
 */
public class FadingLabelAppearance extends LabelAppearance
{
	private float alpha = 1.0f;
	private ILabel label = null;
	
	public FadingLabelAppearance(ILabel w)
	{
		super(w);
		label = w;
	}

	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		Color color = getTextColor();
		g.setColor(color.getRed(), color.getGreen(), color.getBlue(), alpha);
		
		int x = 0;
    	int y = 0;
    	int width = 0;
    	int height = 0;
    	
    	if(label == null)
    		return;
    	
    	Pixmap pixmap = label.getPixmap();
    	String text   = label.getText();
    	int gap = getGap();
    	Font font = getFont();
    	Alignment alignment = getAlignment();
    	
    	if(pixmap != null) 
    	{
    		width = pixmap.getWidth();
    		height = pixmap.getHeight();
    		if(text != null && text.length() > 0) width += gap;
    	}
    	else if(text == null) return;
    	
    	if(text != null)
    	{
    		width += font.getWidth(text);
    		height = Math.max(height, font.getHeight());
    	}
    	
    	x = alignment.alignX(getContentWidth(), width);
    	
        if(pixmap != null) 
        {        	
            g.setColor(Color.WHITE.getRed(), Color.WHITE.getBlue(), Color.WHITE.getGreen(), alpha);
            y = alignment.alignY(getContentHeight(), pixmap.getHeight());
        	g.drawImage(pixmap, x, y);
        	x += pixmap.getWidth( ) +gap;
        }
    	
        if(text != null && text.length() > 0)
        {
            g.setFont(font);
            if(color != null) 
            	g.setColor(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            
            y = alignment.alignY(getContentHeight(), font.getHeight());
            g.drawString(text, x, y);
        }
	}

	public float getAlpha()
	{
		return alpha;
	}

	public void setAlpha(float alpha)
	{
		this.alpha = alpha;
	}
}
