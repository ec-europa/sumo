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
 * $Id: InputOutputStream.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.theme.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A data stream which may do either input or output operations.
 * What we need to laod:
 * 
 * - child element of unknown type that extends/implements a certain class/interface
 *   e.g. LayoutManager in Display
 *   see  processChild(T, TypeRegister)
 * 
 * - child element of known type, that does not need to be instantiated
 *   e.g. List in ComboBox
 *   e.g. Label/Button in Window
 *   see  processChild(String, T, Class, T)
 *   
 * - list of various child elements, all implementing/exting a certain interface
 *   e.g. decorator lists
 *   see  processChildren(List, TypeRegister)
 * 
 * - list of child elements of known type
 *   e.g. <ListElement> for Lists 
 * 
 * - named instances of one specific type
 *   e.g. Color for <TextColor> and <BackgroundColor>
 *   e.g.   <SelectionUnderlay> and <MouseHoverUnderlay>
 *   
 * - attributes of various types
 *   e.g. width="123" resizable="true"
 *  
 * 
 * @see IXMLStreamable
 * @author Esa Tanskanen
 *
 */
public abstract class InputOutputStream
{
	private final List<String> warnings = new ArrayList<String>();
	
	/**
	 * Processes an attribute and returns a new value for the attribute
	 * 
	 * @param name the name of the attribute
	 * @param value the initial value of the attribute
	 * @return a new value for the attribute
	 * @throws IOException if an I/O exception is occured
	 * @throws MissingAttributeException if a new value for
	 * 		   the attribute should be returned but none can be found
	 * @throws MalformedElementException 
	 */
	public abstract int processAttribute(String name, int value) 
		throws IOException, MissingAttributeException,
			MalformedElementException;


	/**
	 * Processes an attribute and returns a new value for the attribute
	 * 
	 * @param name the name of the attribute
	 * @param value the initial value of the attribute
	 * @return a new value for the attribute
	 * @throws IOException if an I/O exception is occured
	 * @throws MissingAttributeException if a new value for
	 * 		   the attribute should be returned but none can be found
	 */
	public abstract double processAttribute(String name, double value) 
		throws IOException, MissingAttributeException,
			MalformedElementException;

	/**
	 * Processes an attribute and returns a new value for the attribute
	 * 
	 * @param name the name of the attribute
	 * @param value the initial value of the attribute
	 * @return a new value for the attribute
	 * @throws IOException if an I/O exception is occured
	 * @throws MissingAttributeException if a new value for
	 * 		   the attribute should be returned but none can be found
	 */
	public float processAttribute(String name, float value) 
		throws IOException, MissingAttributeException, MalformedElementException
	{
		return (float) processAttribute(name, (double) value);
	}


	/**
	 * Processes an attribute and returns a new value for the attribute
	 * 
	 * @param name the name of the attribute
	 * @param value the initial value of the attribute
	 * @return a new value for the attribute
	 * @throws IOException if an I/O exception is occured
	 * @throws MissingAttributeException if a new value for
	 * 		   the attribute should be returned but none can be found
	 */
	public abstract boolean processAttribute(String name, boolean value) 
		throws IOException, MissingAttributeException, MalformedElementException;


	/**
	 * Processes an attribute and returns a new value for the attribute
	 * 
	 * @param name the name of the attribute
	 * @param value the initial value of the attribute
	 * @return a new value for the attribute
	 * @throws IOException if an I/O exception is occured
	 * @throws MissingAttributeException if a new value for
	 * 		   the attribute should be returned but none can be found
	 */
	public abstract String processAttribute(String name, String value) 
		throws IOException, MissingAttributeException, MalformedElementException;



	/**
	 * Processes a child object and returns a new value for it
	 * 
	 * ALWAYS CREATES NEW INSTANCES!
	 * 
	 * @param name the name of the attribute
	 * @param value the initial value of the attribute
	 * @return a new value for the attribute
	 * @throws IOException if an I/O exception is occured
	 * @throws MissingAttributeException if a new value for
	 * 		   the attribute should be returned but none can be found
	 */
	public abstract <T extends IXMLStreamable> T processChild(String name, T value, Class<T> objectClass) 
		throws IOException, IXMLStreamableException;


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
	public float processAttribute(String name, float value, float defaultValue) 
		throws IOException, MalformedElementException
	{
		return (float) processAttribute(name, (double) value, (double) defaultValue);
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
	public int processAttribute(String name, int value, int defaultValue) 
		throws IOException, MalformedElementException
	{
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
	public double processAttribute(String name, double value, double defaultValue) 
		throws IOException, MalformedElementException {
		
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
	public boolean processAttribute(String name, boolean value, boolean defaultValue) 
		throws IOException, MalformedElementException
	{
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
	public String processAttribute(String name, String value, String defaultValue) 
		throws IOException, MalformedElementException
	{
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
	 * ALWAYS CREATES NEW INSTANCES!
	 * 
	 * @param name the name of the attribute
	 * @param value the initial value of the attribute
	 * @return a new value for the attribute
	 * @throws IOException if an I/O exception is occured
	 */
	public <T extends IXMLStreamable> T processChild(String name, T value, T defaultValue, Class<T> objectClass)
			throws IOException, IXMLStreamableException
	{ // TODO actually I would prefer to put the default value right after the normale value in the signature
		try
		{
			return processChild(name, value, objectClass);
		}
		catch (MissingElementException e)
		{
			return defaultValue;
		}
	}
/*
	public <T extends IOStreamSaveable> T process(T value, TypeRegister typeRegistry) throws IOException, IXMLStreamableException,
			MissingElementException
	{
		return (T) process(typeRegistry.getName(value.getClass()), value, value.getClass());
	}


	public <T extends IOStreamSaveable> T process(T value, T defaultValue, TypeRegister typeRegistry) throws IOException,
			IXMLStreamableException
	{
		try
		{
			return process(value, typeRegistry);
		}
		catch (MissingElementException e)
		{
			return defaultValue;
		}
	}
*/

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
	/* TEMPORARILY DISABLED; Please re-enable when needed
	public <T extends IOStreamSaveable> T process(T value, Map<String, Class<? extends T>> possibleTypeMapping)
			throws IOException, IXMLStreamableException, MissingElementException
	{
		value = process(value, null, possibleTypeMapping);

		if (value == null)
		{
			String names = getNameList(possibleTypeMapping.keySet());
			throw getMissingElementException(names);
		}

		return value;
	}
*/

	/**
	 * If a new value should be returned but none is found, the passed
	 * default value is returned.
	 * 
	 * @param name the name of the attribute
	 * @param value the initial value of the attribute
	 * @return a new value for the attribute
	 * @throws IXMLStreamableException 
	 * @throws IOException 
	 * @throws IOException if an I/O exception is occured
	 */
	/* TEMPORARILY DISABLED; Please re-enable when needed
	public <T extends IOStreamSaveable> T process(T value, T defaultValue,
			Map<String, Class<? extends T>> possibleTypeMapping) throws IOException, IXMLStreamableException,
			MissingElementException
	{
		List<T> tempList = new ArrayList<T>();
		tempList.add(value);
		processChildren(tempList, possibleTypeMapping);

		if (tempList.size() > 0)
		{
			if (tempList.size() > 1) { throw getMultipleDefinitionsException(possibleTypeMapping.keySet()); }

			return tempList.get(0);
		}

		return defaultValue;
	}
*/
	/* TEMPORARILY DISABLED; Please re-enable when needed
	public <T extends IOStreamSaveable> T processContainer(final String containerName, T value,
			final Iterable<Class<? extends T>> possibleTypes) throws IOException, IXMLStreamableException,
			MissingElementException
	{
		startSubcontext(containerName);
		value = process(value, possibleTypes);
		endSubcontext();
		return value;
	}
*/

	/* TEMPORARILY DISABLED; Please re-enable when needed
	public <T extends IOStreamSaveable> T processContainer(final String containerName, T value, final T defaultValue,
			final Iterable<Class<? extends T>> possibleTypes) throws IOException, IXMLStreamableException
	{
		startSubcontext(containerName);
		value = process(value, defaultValue, possibleTypes);
		endSubcontext();
		return value;
	}
*/
	
	/* TEMPORARILY DISABLED; Please re-enable when needed
	public <T extends IOStreamSaveable> T processContainer(final String containerName, T value,
			final Class<T> valueClass) throws IOException, IXMLStreamableException
	{
		value = processContainer(containerName, value, valueClass);

		if (value == null) { throw getMissingElementException(valueClass.getName()); }

		return value;
	}
*/

	/* TEMPORARILY DISABLED; Please re-enable when needed
	public <T extends IOStreamSaveable> T processContainer(final String containerName, T value, final T defaultValue,
			final Class<T> valueClass) throws IOException, IXMLStreamableException
	{
		startSubcontext(containerName);
		String name = getDefaultElementName(valueClass);
		value = process(name, value, valueClass, defaultValue);
		endSubcontext();
		return value;
	}
*/

	/* TEMPORARILY DISABLED; Please re-enable when needed
	public <T extends IOStreamSaveable> T process(T value, Iterable<Class<? extends T>> possibleTypes)
			throws IOException, IXMLStreamableException
	{
		value = process(value, null, possibleTypes);

		if (value == null)
		{
			String names = getNameList(possibleTypes);
			throw getMissingElementException(names);
		}

		return value;
	}
*/

	/**
	 * Processes a child element and returns a new value for the element.
	 * The name of the element is retrieved from the DefaultElementName
	 * annotation of the representing class. If a new value should be
	 * returned but none is found, the passed default value is returned.
	 * 
	 * @param name the name of the attribute
	 * @param value the initial value of the attribute
	 * @return a new value for the attribute
	 * @throws IXMLStreamableException 
	 * @throws IOException 
	 * @throws IOException if an I/O exception is occured
	 */
	/* TEMPORARILY DISABLED; Please re-enable when needed
	public <T extends IOStreamSaveable> T process(T value, T defaultValue, Iterable<Class<? extends T>> possibleTypes)
			throws IOException, IXMLStreamableException
	{
		List<T> tempList = new ArrayList<T>();
		tempList.add(value);
		processChildren(tempList, possibleTypes);

		if (tempList.size() > 0)
		{
			if (tempList.size() > 1)
			{
				String names = getNameList(possibleTypes);
				throw new ChildConstructionException("multiple definitions" + "for the element " + names + "\n\n"
						+ getParsingContext());
			}

			return tempList.get(0);
		}

		return defaultValue;
	}
*/

	/**
	 * Processes a list of child objects. The contents of the list may be
	 * modified if the modifiesValues-method returns true.
	 * 
	 * @param childName the name of the child elements
	 * @param children the list of children
	 * @param childClass the class of the children which should be processed.
	 *                   Only the elements in the children-list are taken in
	 *                   account which type is exactly the passed class.
	 *                   Also only elements with the type childClass may be
	 *                   constructed.
	 * @throws IOException if an I/O exception is occured
	 * @throws IXMLStreamableException if a processing exception is occured
	 */
	public abstract <T extends IXMLStreamable> void processChildren(
			String childName, 
			List children,
			Class<T> childClass) throws IOException, IXMLStreamableException;


	/**
	 * Processes a list of child objects. The contents of the list may be
	 * modified if the modifiesValues-method returns true.
	 * 
	 * @param children the list of children
	 * @param childClassMapping a class mapping where the keys map the element
	 *        names to the child classes
	 * @throws IOException if an I/O exception is occured
	 * @throws IXMLStreamableException if a processing exception is occured
	 */
	/* TEMPORARILY DISABLED; Please re-enable when needed
	public <T extends IOStreamSaveable> void processChildren(
			List<T> children, 
			Map<String, Class<? extends T>> childClassMapping) 
				throws IOException, IXMLStreamableException
	{
		for (String key: childClassMapping.keySet())
		{
			processChildren(key, children, childClassMapping.get(key));
		}
	}*/
	
	/**
	 * Loads a list of elements of various types. The types need to be registered
	 * in the TypeRegister.
	 */
	public abstract void processChildren(List children, TypeRegister typeRegister) 
		throws IOException, IXMLStreamableException;
	
	/**
	 * Loads a single element of unknown type. This applies to cases where you want
	 * to load a single 
	 * @param value
	 * @param typeRegister
	 * @return returns a new instance of <code>value</code>
	 * @throws IOException
	 * @throws IXMLStreamableException
	 */
	public abstract <T extends IXMLStreamable> T processChild(T value, TypeRegister typeRegister) 
		throws IOException, IXMLStreamableException;

	/**
	 * processes a child of known type. The given child must not be null. 
	 * @param <T>
	 * @param name
	 * @param value
	 * @param clazz
	 * @return returns <tt>value</tt>, but after running <tt>process()</tt> on it!
	 * @throws IOException
	 * @throws IXMLStreamableException
	 */
	public abstract <T extends IXMLStreamable> void processInherentChild(String name, T value)
		throws IOException, IXMLStreamableException;
	
	/**
	 * Processes a list of child objects. The contents of the list may be
	 * modified if the modifiesValues-method returns true.
	 * The names of the elements are retrieved from the DefaultElementName
	 * annotations of the representing classes.
	 * 
	 * @param children the list of children
	 * @param childClassMapping a class mapping where the keys map the element
	 *        names to the child classes
	 * @throws IOException if an I/O exception is occured
	 * @throws IXMLStreamableException if a processing exception is occured
	 */
	/* TEMPORARILY DISABLED; Please re-enable when needed
	public <T extends IOStreamSaveable> void processChildren(List<T> children, Iterable<Class<? extends T>> childClasses)
			throws IOException, IXMLStreamableException
	{
		for (Class<? extends T> clazz : childClasses)
		{
			String defaultName = getDefaultElementName(clazz);
			processChildren(defaultName, children, clazz);
		}
	}
*/

	public <T extends Enum> T processEnum(String name, T obj, T defaultValue, Class<T> objClass,
			StorageFormat<T, String> storageFormat) 
		throws MalformedElementException, IOException
	{
		String enumName = storageFormat.encode(obj);

		enumName = processAttribute(name, enumName, null);

		if (enumName == null) { return defaultValue; }

		T newValue = storageFormat.decode(enumName);

		if (newValue == null)
		{
			// No alignment found for that name, throw an exception

			StringBuffer buf = new StringBuffer();

			T[] values = objClass.getEnumConstants();

			for (int i = 0; i < values.length; i++)
			{
				T value = values[i];
				buf.append(value.name());

				if (i < values.length - 1)
				{
					buf.append(", ");
				}
				else if (i == values.length - 1)
				{
					buf.append(" or ");
				}
			}

			throw MalformedElementException.createDefaultMalformedAttributeException(name, buf.toString());
		}

		return newValue;
	}

	/* TEMPORARILY DISABLED; Please re-enable when needed
	public <T extends Enum> T processEnum(String name, T obj, Class<T> objClass, StorageFormat<T, String> storageFormat)
			throws MalformedElementException, IOException, MissingAttributeException
	{
		T value = processEnum(name, obj, null, objClass, storageFormat);

		if (value == null) { throw getMissingAttributeException(name); }

		return value;
	}
	 */

	/* TEMPORARILY DISABLED; Please re-enable when needed
	public <T extends Enum> T processEnumElement(String attributeName, T value, T defaultValue, Class<T> objClass,
			StorageFormat<T, String> storageFormat) throws IOException, IXMLStreamableException
	{
		String containerName = getDefaultElementName(objClass);
		startSubcontext(containerName);
		value = processEnum(attributeName, value, defaultValue, objClass, storageFormat);
		endSubcontext();
		return value;
	}
*/

	/**
	 * Retrieves the default name for the element representing the passed class
	 * from the DefaultElementName-annotation of the class
	 * 
	 * @param clazz the class which represents the element
	 * @return the default name for the element
	 * @throws NameAnnotationMissingException if the DefaultElementName
	 * 		   is not defined for that class
	 */
	/* the default element type is actually already define through the TypeRegistery 
	protected String getDefaultElementName(Class<?> clazz) throws NameAnnotationMissingException
	{
		DefaultElementName defaultNameAnn = clazz.getAnnotation(DefaultElementName.class);

		String defaultName = null;
		if (defaultNameAnn != null)
		{
			defaultName = defaultNameAnn.value();
		}
		else defaultName = clazz.getSimpleName();
		

		if (defaultName == null || defaultName.length() == 0) { throw new NameAnnotationMissingException(
				"the annotation " + DefaultElementName.class.getName() + " in the class " + clazz.getName()
						+ " is missing " + " the required name value" + "\n\n" + getParsingContext()); }

		return defaultName;
	}
	*/

	/**
	 * Returns true if this InputOutputStream may modify the values passed
	 * in the process-methods
	 * 
	 * @return true if this InputOutputStream may modify the values passed
	 * 		   in the process-methods
	 */
	public abstract boolean isInputStream();


	/**
	 * Closes both this InputOutputStream and the underlying stream
	 * (which is passed in the constructor)
	 * @throws IOException if an I/O exception is occured
	 */
	//public abstract void close() throws IOException;


	/**
	 * Used in case of an exception to output the point in the stream
	 * where the exception occured. Thus, the implementation of this method
	 * doesn't have to be too efficient.
	 * 
	 * @return the parsing context, with a marker to show which
	 * 		   part is being parsed, or null if the context can't be found out
	 */
	protected abstract String getParsingContext();

	protected String getNameList(Iterable<?> strings)
	{
		StringBuffer nameBuf = new StringBuffer();
		nameBuf.append('[');

		for (Object name : strings)
		{
			if (nameBuf.length() > 1)
			{
				nameBuf.append('|');
			}

			nameBuf.append(name.toString());
		}

		nameBuf.append(']');
		return nameBuf.toString();
	}


	/**
	 * Enters the child element with the given name. 
	 * Be aware of an ambiguity problem here:, When
	 * reading in, this method
	 * returns the FIRST child found with the given name and may omit child elements
	 * which have the same name.
	 * @param name the name of the child element
	 * @return returns whether the child element exists (true) or not (false).
	 * @throws MalformedElementException
	 */
	public abstract boolean startSubcontext(String name) throws IXMLStreamableException;


	/**
	 * Ends the current element context.
	 *
	 */
	public abstract void endSubcontext();
	
	/**
	 * Closes the stream
	 *
	 */
	public abstract void close() throws IOException;

	
	/**
	 * Returns a list of warnings caused by the previous I/O operations
	 * @return a list of warnings caused by the previous I/O operations
	 */
	public List<String> getWarnings() {
		return warnings;
	}
	
	public String getWarningsAsString() {
		StringBuilder buf = new StringBuilder();
		
		for (String warning : warnings) {
			buf.append("WARNING: ");
			buf.append(warning);
			buf.append('\n');
		}
		
		return buf.toString();
	}
	
	protected void addWarning(String warning) {
		warnings.add(warning);
	}
}
