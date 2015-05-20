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
public class EnumFormat<T extends Enum> implements StorageFormat<T, String>
{
	protected final Class<T> _enumClass;


	public EnumFormat(Class<T> enumClass)
	{
		_enumClass = enumClass;
	}


	/* (non-Javadoc)
	 * @see org.fenggui.io.StorageFormat#encode(java.lang.Object)
	 */
	public String encode(T obj) throws EncodingException
	{
		return encodeName(obj.name());
	}


	/* (non-Javadoc)
	 * @see org.fenggui.io.StorageFormat#decode(java.lang.Object)
	 */
	public T decode(String encodedObj)
	{
		String name = decodeName(encodedObj);

		T[] values = _enumClass.getEnumConstants();

		for (T value : values)
		{
			if (value.name().equals(name)) { return value; }
		}

		return null;
	}


	protected String encodeName(String name) throws EncodingException
	{
		return name;
	}


	protected String decodeName(String encodedName)
	{
		return encodedName;
	}


	protected boolean equalityOperation(String enumName, String decodedName)
	{
		return enumName.equals(decodedName);
	}
}
