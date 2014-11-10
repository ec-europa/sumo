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
 * $Id: PixmapBorderExample.java 158 2007-01-27 01:35:46Z schabby $
 */
package org.fenggui.example;

import java.io.IOException;

import org.fenggui.Button;
import org.fenggui.Display;
import org.fenggui.PixmapDecorator;
import org.fenggui.render.Binding;
import org.fenggui.render.Pixmap;
import org.fenggui.util.Spacing;

public class PixmapDecoratorExample implements IExample {

	
	public void buildGUI(Display display) 
	{
		Button b = new Button();
		b.setSize(179, 56);
		b.getAppearance().setPadding(Spacing.ZERO_SPACING);
		b.getAppearance().setMargin(Spacing.ZERO_SPACING);
		display.addWidget(b);
		b.setXY(200, 200);
		b.getAppearance().removeAll();
		try
		{
		
			Pixmap pressedPix = new Pixmap(Binding.getInstance().getTexture("data/images/button_pressed.png"));
			Pixmap defaultPix = new Pixmap(Binding.getInstance().getTexture("data/images/button_default.png"));
			Pixmap hoverPix   = new Pixmap(Binding.getInstance().getTexture("data/images/button_highlight.png"));
		
			b.getAppearance().add(new PixmapDecorator(Button.LABEL_DEFAULT, defaultPix));
			b.getAppearance().add(new PixmapDecorator(Button.LABEL_MOUSEHOVER, hoverPix));
			b.getAppearance().add(new PixmapDecorator(Button.LABEL_PRESSED, pressedPix));
			
			b.getAppearance().setEnabled(Button.LABEL_PRESSED, false);
			b.getAppearance().setEnabled(Button.LABEL_MOUSEHOVER, false);
			b.getAppearance().setEnabled(Button.LABEL_DEFAULT, true);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public String getExampleName() {
		return "PixmapDecorator Example";
	}

	public String getExampleDescription() {
		return "Shows some buttons and other stuff";
	}	
	
}
