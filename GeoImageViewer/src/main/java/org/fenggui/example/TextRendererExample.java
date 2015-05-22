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
 * Created on Feb 25, 2007
 * $Id: TextRendererExample.java 214 2007-02-26 01:04:05Z schabby $
 */
package org.fenggui.example;

import org.fenggui.Display;
import org.fenggui.Label;
import org.fenggui.background.PlainBackground;
import org.fenggui.render.*;
import org.fenggui.util.Alphabet;
import org.fenggui.util.Color;
import org.fenggui.util.fonttoolkit.FontFactory;

public class TextRendererExample implements IExample
{

	public void buildGUI(Display display)
	{
		final String text = "from www.spiegel.de: Donnersmarck is hoping to win an\n" +
				"Oscar for his melodrama about the East German secret police, \"The\n" +
				"Lives of Others\"(\"Das Leben der Anderen\"), which is competing in\n" +
				"the category \"Best Foreign Language Film.\" The much sought-after \n" +
				"awards will be presented in Los Angeles on Feb. 25. Meanwhile, the\n" +
				"film is showing in many major US cities, where it now needs to\n" +
				"impress the members of the Academy of Motion Picture Arts and\n" +
				"Sciences who make up the jury. Donnersmarck's expectations are \n" +
				"climbing.	He's travelling around ahead of his film, from New York\n" +
				"to Los Angeles via Chicago and Dallas. \"When no one knows you,\n" +
				"\" Donnersmarck says, you have to take your film to the people.\n"+
				"\nOn Thursday, nine months after independent investigators formally cast \n" +
				"doubt on the government's acquisition of the painting, the German finance \n" +
				"ministry announced that the painting will be returned to the heirs of art\n" +
				"collector Leo Bendel, the original owner.";
		
		Label l = new Label();
		l.getAppearance().setTextRenderer(new DirectTextRenderer());
		l.setText(text);
		l.getAppearance().add(new PlainBackground(Color.WHITE));
		display.addWidget(l);
		l.updateMinSize();
		l.setSizeToMinSize();
		l.setXY(10, 50);

		Label l1 = new Label();
		l1.getAppearance().setTextRenderer(new BufferedTextRenderer());
		l1.getAppearance().add(new PlainBackground(Color.WHITE));
		l1.setText(text);
		l1.updateMinSize();
		l1.setSizeToMinSize();
		display.addWidget(l1);
		l1.setXY(l.getX() + l.getWidth()+20, 50);
		
		Label l2 = new Label();
		l2.getAppearance().setTextRenderer(new BouncingLettersTextRenderer());
		Font font = FontFactory.renderStandardFont(new java.awt.Font("Serif", java.awt.Font.PLAIN, 30), true, Alphabet.ENGLISH);
		l2.getAppearance().setFont(font);
		l2.setText("This is a line of bouncing text!");
		l2.updateMinSize();
		l2.setSizeToMinSize();
		display.addWidget(l2);
		l2.setXY(100, l.getY() + l.getHeight()+ 50);
	}

	public String getExampleDescription()
	{
		return "Text Renderer Example";
	}

	public String getExampleName()
	{
		return "Text Renderer Example";
	}

}
