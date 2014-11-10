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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Esa Tanskanen
 *
 */
public class TypeRegister
{
	private final Map<String, Class<? extends IXMLStreamable>> namedTypes =
		new HashMap<String, Class<? extends IXMLStreamable>>();

	public void register(String name, Class<? extends IXMLStreamable> type)
	{
		namedTypes.put(name, type);
	}
	
	public boolean isEmpty() {
		return namedTypes.isEmpty();
	}

	/*
	public Iterable<Class<? extends IOStreamSaveable>> getTypes()
	{
		return namedTypes.values();
	}
	*/
	
	public String getName(Class<? extends IXMLStreamable> clazz)
	{
		for(String s: namedTypes.keySet())
		{
			if(clazz.equals(namedTypes.get(s))) return s;
		}
		// has to return null so that we get to know if the given
		// Class is not registered
		return null;
	}

	public Iterable<String> getNames()
	{
		return namedTypes.keySet();
	}
	
	public Class<? extends IXMLStreamable> getType(String name)
	{
		return namedTypes.get(name);
	}
	
	public boolean containsType(String name)
	{
		return namedTypes.containsKey(name);
	}
	
	/*
	public void process(InputOutputStream stream, List<IOStreamSaveable> children) throws IOException, IXMLStreamableException
	{
		stream.processChildren(children, namedTypes);
		stream.processChildren(children, defaultTypes);
	}
*/

	/*
	@SuppressWarnings("unchecked")
	public <T extends IOStreamSaveable> T process(InputOutputStream stream, T value, T defaultValue) throws IOException, IXMLStreamableException
	{
		T first = (T) stream.process(value, defaultValue, namedTypes);
		T second = (T) stream.process(value, defaultValue, defaultTypes);

		if (first != null)
		{
			if (second != null) { throw stream.getMultipleDefinitionsException(getNames()); }

			return first;
		}

		if (second != null) { return second; }

		throw stream.getMissingElementException(getNames());
	}
	*/

	/*
	public <T extends IOStreamSaveable> T processContainer(String containerName, InputOutputStream stream, T value, T defaultValue)
			throws IOException, IXMLStreamableException
	{
		stream.startSubcontext(containerName);
		T ret = process(stream, value, defaultValue);
		stream.endSubcontext();
		return ret;
	}
	*/

/*
	public void processChildren(InputOutputStream stream, List<? extends IOStreamSaveable> values) throws IOException, IXMLStreamableException
	{
		// I modified this  (Johannes)
		//stream.processChildren(values, defaultTypes);
		//stream.processChildren(values, namedTypes);
	}
*/
/*
	public void processContainer(String containerName, InputOutputStream stream, List<? extends IOStreamSaveable> values) throws IOException,
			IXMLStreamableException
	{
		stream.startSubcontext(containerName);
		//processChildren(stream, values);
		stream.endSubcontext();
	}
	*/

/*
	private Iterable<Object> getNames()
	{
		List<Object> names = new ArrayList<Object>();
		//names.addAll(defaultTypes);
		names.addAll(namedTypes.keySet());
		return names;
	}
*/
/*
	public void process(InputOutputStream stream, List list)
	{
		
	}
*/
}
