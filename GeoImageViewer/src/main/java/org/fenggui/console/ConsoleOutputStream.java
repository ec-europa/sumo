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
 * Created on Mar 9, 2007
 * $Id: ConsoleOutputStream.java 220 2007-03-10 12:00:00Z schabby $
 */
package org.fenggui.console;

import java.io.IOException;
import java.io.OutputStream;

import org.fenggui.render.ITextRenderer;

public class ConsoleOutputStream extends OutputStream
{
	private ITextRenderer textRenderer = null;
	
	public ConsoleOutputStream(ITextRenderer textRenderer)
	{
		this.textRenderer = textRenderer; 
	}

	@Override
	public void write(int b) throws IOException
	{
		textRenderer.setText(textRenderer.getText() + new String(new int[]{b}, 0, 1));
	}

	@Override
	public void close() throws IOException
	{
		super.close();
	}

	@Override
	public void flush() throws IOException
	{
		super.flush();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		super.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		super.write(b);
	}

	
	
}
