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
 * An exception thrown when the construction of a child object fails.
 * Primarly used as a wrapper exception for the numerious different
 * exceptions thrown by the reflection API.
 * 
 * @author Esa Tanskanen
 *
 */
@SuppressWarnings("serial")
public class NameShadowingException extends IXMLStreamableException
{
	public NameShadowingException(String message)
	{
		super(message);
	}


	public NameShadowingException(String message, Throwable cause)
	{
		super(message, cause);
	}


	public NameShadowingException(Throwable cause)
	{
		super(cause);
	}
}
