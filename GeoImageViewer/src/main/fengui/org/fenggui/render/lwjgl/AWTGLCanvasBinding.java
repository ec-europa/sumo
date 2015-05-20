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
 * Created on Jan 30, 2006
 * $Id: AWTGLCanvasBinding.java 329 2007-08-11 14:54:44Z Schabby $
 */
package org.fenggui.render.lwjgl;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.fenggui.render.Binding;
import org.fenggui.render.ITexture;
import org.fenggui.render.jogl.JOGLCursorFactory;
import org.lwjgl.opengl.AWTGLCanvas;

/**
 * Alternative binding for LWJGL. 
 * It uses <code>org.lwjgl.opengl.AWTGLCanvas</code> insead of
 * <code>org.lwjgl.opengl.Display</code> to bind to.
 * 
 * @author Johannes, last edited by $Author: Schabby $, $Date: 2007-08-11 16:54:44 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 329 $
 */
public class AWTGLCanvasBinding extends Binding
{
	private AWTGLCanvas canvas = null;
	private JOGLCursorFactory cursorFactory = null;

	/**
	 * Constructs a new <code>AWTGLCanvasBinding</code> object.
	 * @param canvas the canvas
	 */
	public AWTGLCanvasBinding(final AWTGLCanvas canvas)
	{
		super(new LWJGLOpenGL());

		this.canvas = canvas;

		cursorFactory = new JOGLCursorFactory(canvas);

		canvas.addComponentListener(new ComponentListener()
		{
			public void componentResized(ComponentEvent ce)
			{
				fireDisplayResizedEvent(canvas.getWidth(), canvas.getHeight());
			}

			public void componentMoved(ComponentEvent arg0)
			{
			}

			public void componentShown(ComponentEvent arg0)
			{
			}

			public void componentHidden(ComponentEvent arg0)
			{
			}
		});
	}

	@Override
	public ITexture getTexture(InputStream is) throws IOException
	{
		return LWJGLTexture.createTexture(ImageIO.read(is));
	}

	@Override
	public ITexture getTexture(BufferedImage bi)
	{
		return LWJGLTexture.createTexture(bi);
	}

	@Override
	public int getCanvasWidth()
	{
		return canvas.getWidth();
	}

	@Override
	public int getCanvasHeight()
	{
		return canvas.getHeight();
	}

	@Override
	public JOGLCursorFactory getCursorFactory()
	{
		return cursorFactory;
	}

}
