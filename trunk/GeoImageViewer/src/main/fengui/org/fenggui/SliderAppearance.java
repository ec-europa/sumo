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
 * $Id: SliderAppearance.java 226 2007-03-15 14:22:34Z bbeaulant $
 */
package org.fenggui;

import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.util.Dimension;

public class SliderAppearance extends DecoratorAppearance
{
	private Slider slider = null;
	
	public SliderAppearance(Slider w)
	{
		super(w);
		slider = w;
	}

	@Override
	public Dimension getContentMinSizeHint()
	{
		return new Dimension(30, 30);

	}

	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		// If the slider is part of a scrollbar, 
		// we don't display its button when Disabled
		if (slider.isEnabled() || !(slider.getParent() instanceof ScrollBar)) {
			Button sliderButton = slider.getSliderButton();
		
			g.translate(sliderButton.getX(), sliderButton.getY());
			sliderButton.paint(g);
			g.translate(-sliderButton.getX(), -sliderButton.getY());
		}

	}

	
}
