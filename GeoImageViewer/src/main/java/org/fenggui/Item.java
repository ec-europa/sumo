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
 * $Id: Item.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.io.IOException;

import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.IXMLStreamable;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * 
 * 
 * @todo Comment this class... #
 * 
 * @author Johannes, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public class Item implements IXMLStreamable
{
	
	private String text = null;
	private Pixmap pixmap = null;
	
	
	public Item(String text, Pixmap pixmap) {
		this.text = text;
		this.pixmap = pixmap;
	}

	public Item(String text) {
		this(text, null);
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	

	public Pixmap getPixmap() {
		return pixmap;
	}

	public void setPixmap(Pixmap pixmap) {
		this.pixmap = pixmap;
	}

	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		text = stream.processAttribute("text", text);
	}

	/* (non-Javadoc)
	 * @see org.fenggui.io.IOStreamSaveable#getUniqueName()
	 */
	public String getUniqueName() {
		return GENERATE_NAME;
	}
}
