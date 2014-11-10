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
 * Context handler that stores all data in a single namespace. Basically, it wraps
 * a hashmap. 
 * TODO: Name is bit of a misnomer. Should be something like HashMapBlackborder or so.
 *  
 * @author Esa, last edited by $Author$, $Date$
 * @version $Revision$
 */
public class GlobalContextHandler
{
	private final Map<String, IXMLStreamable> refmap = new HashMap<String, IXMLStreamable>();


	public void add(String id, IXMLStreamable obj) throws NameShadowingException
	{
		if (refmap.containsKey(id)) { throw new NameShadowingException(id); }

		refmap.put(id, obj);
	}


	public IXMLStreamable get(String id)
	{
		return refmap.get(id);
	}


	/**
	 * No subcontexts in GlobalContextHandler - does nothing
	 */
	public void startSubcontext(String name)
	{
	}


	/**
	 * No subcontexts in GlobalContextHandler - does nothing
	 */
	public void endSubcontext()
	{
	}


	public String getName(IXMLStreamable obj) {
		for (String name : refmap.keySet()) {
			if (obj == refmap.get(name)) {
				return name;
			}
		}
		
		return null;
	}
}
