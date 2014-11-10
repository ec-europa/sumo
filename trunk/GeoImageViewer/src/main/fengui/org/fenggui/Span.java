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
 * Created on Dec 18, 2006
 * $Id: Span.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import org.fenggui.theme.xml.EncodingException;
import org.fenggui.theme.xml.StorageFormat;

public enum Span
{

	MARGIN("margin"), PADDING("padding"), BORDER("border");
	
	private String code;
	
	private Span(String code)
	{
		this.code = code;
	}
	
	public String code()
	{
		return code;
	}
	
	public static final StorageFormat STORAGE_FORMAT = new StorageFormat<Span, String>(){

		public String encode(Span obj) throws EncodingException
		{
			return obj.code();
		}

		public Span decode(String encodedObj) throws EncodingException
		{
			if(encodedObj.equals("margin"))
				return MARGIN;
			else if(encodedObj.equals("padding"))
				return PADDING;
			else return BORDER;
		}

	};
}
