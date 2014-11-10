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
 * Created on Dec 8, 2006
 * $Id$
 */
package org.fenggui.switches;

import java.io.IOException;

import org.fenggui.IPixmapWidget;
import org.fenggui.IWidget;
import org.fenggui.Switch;
import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * TODO rename to PixmapSwitch
 * 
 * @author Johannes Schaback, last edited by $Author$, $Date$
 * @version $Revision$
 */
public class SetPixmapSwitch extends Switch
{
	private Pixmap pixmap = null;
	
	public SetPixmapSwitch(InputOnlyStream stream) throws IOException, IXMLStreamableException
	{
		super("not set yet");
		process(stream);
	}
	
	public SetPixmapSwitch(String label, Pixmap pixmap)
	{
		super(label);
		this.pixmap = pixmap;
	}

	@Override
	public void setup(IWidget widget)
	{
		try 
		{
			IPixmapWidget pw = (IPixmapWidget) widget;
			pw.setPixmap(pixmap);
		} 
		catch(ClassCastException ex)
		{
			System.err.println("widget is not of type IPixmapWidget!");
			ex.printStackTrace();
		}
	}

	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		// we want to treat the <PixmapSwitch> element as a
		// Pixmap context, thus we directly call new Pixmap(stream) here
		if(pixmap == null)
			pixmap = new Pixmap((InputOnlyStream)stream);
		else
			pixmap.process(stream);
	}

}
