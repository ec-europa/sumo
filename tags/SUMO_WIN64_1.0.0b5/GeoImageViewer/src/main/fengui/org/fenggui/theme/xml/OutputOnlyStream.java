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
 * $Id: OutputOnlyStream.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.theme.xml;

import java.io.IOException;


/**
 * A data stream which may only do output (serialization) operations.
 * 
 * @author Esa Tanskanen
 *
 */
public abstract class OutputOnlyStream extends InputOutputStream
{
	private static final double DOUBLE_EQUALITY_IMPRECISION = 0.00001;
	
	private final boolean dontSaveDefaultValues;
	
	private final GlobalContextHandler contextHandler;


	public OutputOnlyStream(boolean saveDefaultValues,
			GlobalContextHandler contextHandler)
	{
		this.contextHandler = contextHandler;
		this.dontSaveDefaultValues = !saveDefaultValues;
	}
	
	@Override
	public boolean startSubcontext(String name)
	{
		contextHandler.startSubcontext(name);
		return true;
	}
	
	
	@Override
	public void endSubcontext()
	{
		contextHandler.endSubcontext();
	}
	
	
	protected void putObject(String name, IXMLStreamable object)
	throws NameShadowingException {
		contextHandler.add(name, object);
	}
	
	
	public IXMLStreamable getObject(String qualifiedName) {
		return contextHandler.get(qualifiedName);
	}
	
	
	public String getQualifiedName(IXMLStreamable object) {
		return contextHandler.getName(object);
	}


	public boolean saveDefaultValues()
	{
		return !dontSaveDefaultValues;
	}
	
	/* (non-Javadoc)
	 * @see org.fenggui.io.InputOutputStream#modifiesValues()
	 */
	@Override
	public boolean isInputStream()
	{
		return false;
	}


	/**
	 * Processes an attribute and returns a new value for the attribute
	 * If a new value should be returned but none is found, the passed
	 * default value is returned.
	 * 
	 * @param name the name of the attribute
	 * @param value the initial value of the attribute
	 * @return a new value for the attribute
	 * @throws IOException if an I/O exception is occured
	 */
	@Override
	public int processAttribute(String name, int value, int defaultValue) throws IOException, MalformedElementException
	{
		if (dontSaveDefaultValues && value == defaultValue) {
			return value;
		}
		
		try
		{
			return processAttribute(name, value);
		}
		catch (MissingAttributeException e)
		{
			return defaultValue;
		}
	}


	/**
	 * Processes an attribute and returns a new value for the attribute
	 * If a new value should be returned but none is found, the passed
	 * default value is returned.
	 * 
	 * @param name the name of the attribute
	 * @param value the initial value of the attribute
	 * @return a new value for the attribute
	 * @throws IOException if an I/O exception is occured
	 */
	@Override
	public double processAttribute(String name, double value, double defaultValue) throws IOException, MalformedElementException
	{
		if (dontSaveDefaultValues
				&& value > defaultValue - DOUBLE_EQUALITY_IMPRECISION
				&& value < defaultValue + DOUBLE_EQUALITY_IMPRECISION) {
			return value;
		}
		
		try
		{
			return processAttribute(name, value);
		}
		catch (MissingAttributeException e)
		{
			return defaultValue;
		}
	}


	/**
	 * Processes an attribute and returns a new value for the attribute
	 * If a new value should be returned but none is found, the passed
	 * default value is returned.
	 * 
	 * @param name the name of the attribute
	 * @param value the initial value of the attribute
	 * @return a new value for the attribute
	 * @throws IOException if an I/O exception is occured
	 */
	@Override
	public boolean processAttribute(String name, boolean value, boolean defaultValue) throws IOException,
			MalformedElementException
	{
		if (dontSaveDefaultValues && value == defaultValue) {
			return value;
		}
		
		try
		{
			return processAttribute(name, value);
		}
		catch (MissingAttributeException e)
		{
			return defaultValue;
		}
	}


	/**
	 * Processes an attribute and returns a new value for the attribute
	 * If a new value should be returned but none is found, the passed
	 * default value is returned.
	 * 
	 * @param name the name of the attribute
	 * @param value the initial value of the attribute
	 * @return a new value for the attribute
	 * @throws IOException if an I/O exception is occured
	 */
	@Override
	public String processAttribute(String name, String value, String defaultValue) throws IOException, MalformedElementException
	{
		if (dontSaveDefaultValues && value.equals(defaultValue)) {
			return value;
		}
		
		try
		{
			return processAttribute(name, value);
		}
		catch (MissingAttributeException e)
		{
			return defaultValue;
		}
	}
}
