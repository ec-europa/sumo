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
 * $Id: IOStreamSaveable.java 195 2007-02-10 19:15:17Z eptanska $
 */
package org.fenggui.theme.xml;

import java.io.IOException;

/**
 * Indicates that the object can be saved and loaded by using the
 * InputOutputStream system. All objects which implement the interface
 * should have a constructor which takes a single InputOnlyStream.
 * 
 * @author Esa Tanskanen
 *
 */
public interface IXMLStreamable
{
	public static final String GENERATE_NAME = "--generate-name--";
	
	/**
	 * Either serializes or deserializes the data contained by the object
	 * by calling the processing methods of the passed InputOutputStream.
	 * 
	 * @param stream the stream used by the serialization or deserialization
	 * @throws IOException thrown if an I/O exception occurs during the operation
	 * @throws IXMLStreamableException if the input/output operations fail
	 */
	void process(InputOutputStream stream) throws IOException, IXMLStreamableException;
	
	/**
	 * Returns an unique name for this object, null if no name should be
	 * given to this object or GENERATE_NAME, if the name can be freely chosen
	 * @return an unique name, null or GENERATE_NAME
	 */
	String getUniqueName();
}
