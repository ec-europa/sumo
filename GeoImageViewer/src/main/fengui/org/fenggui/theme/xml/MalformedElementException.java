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
 * $Id: ComboBox.java 116 2006-12-12 22:46:21Z schabby $
 */
package org.fenggui.theme.xml;

/**
 * @author Esa Tanskanen
 *
 */
@SuppressWarnings("serial")
public class MalformedElementException extends IXMLStreamableException
{
	public MalformedElementException(String message)
	{
		super(message);
	}


	public MalformedElementException(String message, Throwable cause)
	{
		super(message, cause);
	}


	public MalformedElementException(Throwable cause)
	{
		super(cause);
	}
	
	public static MalformedElementException createDefaultMalformedAttributeException(String name, String content) throws MalformedElementException
	{
		throw new MalformedElementException("Malformed attribute "+name+", "+content);
	}
	


	/**
	 * Returns an MalformedElementException with a suitable error message.
	 * 
	 * @param attribute the name of the malformed element
	 * @return an MissingElementException with a suitable message
	 */
	public static MalformedElementException createDefault(String name, String valueFormatDescrption, String parsingContext)
	{
		return new MalformedElementException("the element " + name + " should be " + valueFormatDescrption + "\n\n"	+ parsingContext);
	}
}
