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
import java.util.ArrayList;

import org.fenggui.Canvas;
import org.fenggui.Display;
import org.fenggui.background.PlainBackground;
import org.fenggui.border.PixmapBorder;
import org.fenggui.border.PixmapBorder16;
import org.fenggui.render.Binding;
import org.fenggui.render.ITexture;
import org.fenggui.render.Pixmap;
import org.fenggui.util.Color;

public class PixmapBorderExample implements IExample {

	@SuppressWarnings("unchecked")
	public void buildGUI(Display display) 
	{
		Canvas leftCont = new Canvas();
		display.addWidget(leftCont);
		
		leftCont.setSize(150, 150);
		leftCont.setX(50);
		leftCont.setY(50);
		leftCont.getAppearance().add(new PlainBackground(Color.RED));
		
		Canvas rightCont = new Canvas();
		display.addWidget(rightCont);
		rightCont.setSize(150, 150);
		rightCont.setX(250);
		rightCont.setY(50);
		rightCont.getAppearance().add(new PlainBackground(Color.RED));
		
		Canvas veryRightCont = new Canvas();
		display.addWidget(veryRightCont);
		veryRightCont.setSize(150, 150);
		veryRightCont.setX(450);
		veryRightCont.setY(50);
		veryRightCont.getAppearance().add(new PlainBackground(Color.RED));
		
		try
		{
			ITexture tex = Binding.getInstance().getTexture("data/PixmapBorderRed.png");
			
			// mapping pixmaps on the texture. Note that the origin for pixmaps is
			// in the upper left corner!!!
			Pixmap upperLeftCorner = new Pixmap(tex, 0, 0, 13, 13);
			Pixmap upperRightCorner = new Pixmap(tex, 12, 0, 13, 13);
			Pixmap lowerLeftCorner = new Pixmap(tex, 0, 12, 13, 13);
			Pixmap lowerRightCorner = new Pixmap(tex, 12, 12, 13, 13);
			
			Pixmap leftEdge = new Pixmap(tex, 0, 12, 13, 1);
			Pixmap rightEdge = new Pixmap(tex, 12, 12, 13, 1);
			Pixmap topEdge = new Pixmap(tex, 12, 0, 1, 13);
			Pixmap bottomEdge = new Pixmap(tex, 12, 12, 1, 13);
			
			PixmapBorder border = new PixmapBorder(
					leftEdge,
					rightEdge,
					topEdge,
					bottomEdge,
					upperLeftCorner,
					upperRightCorner,
					lowerLeftCorner,
					lowerRightCorner);
			
			leftCont.getAppearance().add(border);
			
			ITexture thinBorderTex = Binding.getInstance().getTexture("data/PixmapBorderRed1.png");
			
			upperLeftCorner = new Pixmap(thinBorderTex, 0, 0, 17, 17);
			upperRightCorner = new Pixmap(thinBorderTex, 25, 0, 17, 17);
			lowerLeftCorner = new Pixmap(thinBorderTex, 0, 18, 17, 17);
			lowerRightCorner = new Pixmap(thinBorderTex, 25, 18, 17, 17);
			
			leftEdge = new Pixmap(thinBorderTex, 0, 17, 17, 1);
			rightEdge = new Pixmap(thinBorderTex, 25, 17, 17, 1);
			topEdge = new Pixmap(thinBorderTex, 18, 0, 2, 17);
			bottomEdge = new Pixmap(thinBorderTex, 17, 18, 1, 17);
			
			border = new PixmapBorder(
					leftEdge,
					rightEdge,
					topEdge,
					bottomEdge,
					upperLeftCorner,
					upperRightCorner,
					lowerLeftCorner,
					lowerRightCorner);
			
			rightCont.getAppearance().add(border);
			
			//////// 16 slices /////////
			ITexture sixteenSlicesTex = Binding.getInstance().getTexture("data/images/PixmapBorderExample.png");
			
			upperLeftCorner = new Pixmap(sixteenSlicesTex, 0, 0, 11, 11);
			upperRightCorner = new Pixmap(sixteenSlicesTex, 66, 0, 11, 11);
			lowerLeftCorner = new Pixmap(sixteenSlicesTex, 0, 88, 11, 11);
			lowerRightCorner = new Pixmap(sixteenSlicesTex, 66, 88, 11, 11);
			
			leftEdge = new Pixmap(sixteenSlicesTex, 0, 44, 11, 11);
			rightEdge = new Pixmap(sixteenSlicesTex, 66, 44, 11, 11);
			topEdge = new Pixmap(sixteenSlicesTex, 30, 0, 11, 11);
			bottomEdge = new Pixmap(sixteenSlicesTex, 34, 88, 11, 11);
			
			Pixmap upperLeftJunction = new Pixmap(sixteenSlicesTex, 0, 22, 11, 11);
			Pixmap lowerLeftJunction = new Pixmap(sixteenSlicesTex, 0, 66, 11, 11);
			
			Pixmap upperRightJunction = new Pixmap(sixteenSlicesTex, 65, 22, 11, 11);
			Pixmap lowerRightJunction = new Pixmap(sixteenSlicesTex, 67, 66, 11, 11);
			
			Pixmap topLeftJunction = new Pixmap(sixteenSlicesTex, 16, 0, 11, 11);
			Pixmap topRightJunction = new Pixmap(sixteenSlicesTex, 46, 0, 11, 11);
			
			Pixmap bottomLeftJunction = new Pixmap(sixteenSlicesTex, 18, 88, 11, 11);
			Pixmap bottomRightJunction = new Pixmap(sixteenSlicesTex, 48, 88, 11, 11);
			
			ArrayList<Pixmap> list = new ArrayList<Pixmap>();
			
			list.add(upperLeftCorner); // 0
			list.add(topLeftJunction); // 1
			list.add(topEdge); // 2
			list.add(topRightJunction); // 3
			list.add(upperRightCorner); // 4
			
			list.add(upperLeftJunction); // 5
			list.add(upperRightJunction); // 6
			list.add(leftEdge); // 7
			list.add(rightEdge); // 8
			
			list.add(lowerLeftJunction); // 9
			list.add(lowerRightJunction); // A

			list.add(lowerLeftCorner); // B
			list.add(bottomLeftJunction); // C
			list.add(bottomEdge); // D
			list.add(bottomRightJunction); // E
			list.add(lowerRightCorner); // F
			
			PixmapBorder16 sixteenSlicesBorder = new PixmapBorder16(list);
			veryRightCont.getAppearance().add(sixteenSlicesBorder);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}

	public String getExampleName() {
		return "PixmapBorder Example";
	}

	public String getExampleDescription() {
		return "Shows some buttons and other stuff";
	}	
	
}
