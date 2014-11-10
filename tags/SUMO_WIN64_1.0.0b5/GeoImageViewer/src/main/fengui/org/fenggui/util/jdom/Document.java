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
 * Created on Dec 17, 2006
 * $Id: Document.java 130 2006-12-18 01:49:53Z schabby $
 */
package org.fenggui.util.jdom;

public class Document extends Element
{

	public Document(String documentRootName)
	{
		super(documentRootName);
	}

	public String toXML()
	{
		StringBuilder b = new StringBuilder();
		toXML("", b);
		return b.toString();
	}
}
