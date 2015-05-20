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
 * Created on Feb 2, 2007
 * $Id: InputOnlyStream.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.theme.xml;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class InputOnlyStream extends InputOutputStream
{
	private GlobalContextHandler contextHandler = new GlobalContextHandler(); 
	private String resourcePath = null;

	public String getResourcePath()
	{
		return resourcePath;
	}

	public void setResourcePath(String resourcePath)
	{
		this.resourcePath = resourcePath;
	}
	
	public IXMLStreamable get(String name)
	{
		return contextHandler.get(name);
	}
	
	public void put(String name, IXMLStreamable s) throws NameShadowingException
	{
		contextHandler.add(name, s);
	}
	
	/**
	 * Retrieves the constructor having an InputOutputStream as argument or the
	 * default constructor from the given class.
	 * @param <T>
	 * @param clazz
	 * @return the constructor
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public <T extends IXMLStreamable> T constructObject(Class<T> clazz)
	throws IXMLStreamableException, IOException {
		try {
			try
			{
				Constructor<T> constr =
					clazz.getConstructor(InputOnlyStream.class);
				return constr.newInstance(this);
			}
			catch (NoSuchMethodException e)
			{
				addWarning("Used the default constructor for class "
						+ clazz.getName());
				
				T instance = clazz.newInstance();
				instance.process(this);
				return instance;
			}
		}
		catch (IllegalAccessException e) {
			throw new ChildConstructionException(e.getMessage(), e);
		}
		catch (InstantiationException e) {
			e.printStackTrace();
			throw new ChildConstructionException(e.getMessage(), e);
		}
		catch (InvocationTargetException e) {
			throw new ChildConstructionException(e.getMessage(), e);
		}
	}
	
	@Override
	public boolean isInputStream()
	{
		return true;
	}
	
	

	public void setContextHandler(GlobalContextHandler contextHandler)
	{
		this.contextHandler = contextHandler;
	}

	public GlobalContextHandler getContextHandler()
	{
		return contextHandler;
	}	
}
