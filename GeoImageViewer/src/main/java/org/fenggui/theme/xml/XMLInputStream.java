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
 * $Id: XMLInputStream.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.theme.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fenggui.util.jdom.Element;
import org.fenggui.util.jdom.Reader;

/**
 * An input stream which reads an XML presentation of an object
 * 
 * @author Esa Tanskanen, Johannes Schaback
 *
 */
public class XMLInputStream extends InputOnlyStream
{
	public static final String ATTRIBUTE_NAME = "name";
	public static final String ATTRIBUTE_REFERENCE = "ref";
	
	private Element activeElement;
	private java.io.InputStream in = null;
	
	public XMLInputStream(Element root)
	{
		this.activeElement = root;
	}

	public XMLInputStream(java.io.InputStream in) throws IOException
	{
		this.in = in;
		Reader reader = new Reader();
		Element root = reader.parse(in);
		this.activeElement = root;
	}

	/* (non-Javadoc)
	 * @see org.fenggui.io.InputOutputStream#process(java.lang.String, int)
	 */
	public int processAttribute(String name, int value) throws IOException, MissingAttributeException, MalformedElementException
	{
		String valueStr = activeElement.getAttributeValue(name);

		if (valueStr != null)
		{
			try
			{
				return Integer.parseInt(valueStr);
			}
			catch (NumberFormatException e)
			{
				throw MalformedElementException.createDefault(name, "an integer (\""+valueStr+"\")", getParsingContext());
			}
		}

		throw MissingAttributeException.createDefault(name, getParsingContext());
	}


	/* (non-Javadoc)
	 * @see org.fenggui.io.InputOutputStream#process(java.lang.String, java.lang.String)
	 */
	public String processAttribute(String name, String value) throws IOException, MissingAttributeException
	{
		String valueStr = activeElement.getAttributeValue(name);

		if (valueStr != null) { return valueStr; }

		throw MissingAttributeException.createDefault(name, getParsingContext());
	}


	/* (non-Javadoc)
	 * @see org.fenggui.io.InputOutputStream#processChildren(java.lang.String, java.util.List, java.lang.Class)
	 */
	/*
	@SuppressWarnings("unused")
	protected <T extends IOStreamSaveable> List<T> loadChildren(String childName, Class<T> childClass,
			Constructor<T> constr) throws IOException, IXMLStreamableException
	{
		List<T> children = new ArrayList<T>();
		Element thisElement = activeElement;

		for(Element kid: activeElement.getChildren())
		{
			activeElement = kid;
			
			if(!childName.equals(kid.getName())) continue;
			
			String ref = activeElement.getAttributeValue(ATTRIBUTE_REFERENCE);
			String name = activeElement.getAttributeValue(ATTRIBUTE_NAME);
			
			//System.out.println(childName);
			children.add(loadChild(null, ref, childClass, constr));
		}
		
		activeElement = thisElement;
		return children;
	}*/

	/* (non-Javadoc)
	 * @see org.fenggui.io.InputOutputStream#process(java.lang.String, float)
	 */
	public double processAttribute(String name, double value) throws IOException, MissingAttributeException,
			MalformedElementException
	{
		String valueStr = activeElement.getAttributeValue(name);

		if (valueStr != null)
		{
			try
			{
				return Double.parseDouble(valueStr);
			}
			catch (NumberFormatException e)
			{
				throw MalformedElementException.createDefault(name, "a decimal value", getParsingContext());
			}
		}

		throw MissingAttributeException.createDefault(name, getParsingContext());
	}
	
	
	public boolean processAttribute(String name, boolean value) throws IOException, MalformedElementException, MissingAttributeException
	{
		String valueStr = activeElement.getAttributeValue(name);
		
		if (valueStr == null)
			throw MissingAttributeException.createDefault(name, getParsingContext());
		
		if(valueStr.equalsIgnoreCase("true")) return true;
		else if(valueStr.equalsIgnoreCase("false")) return false;
		else throw MalformedElementException.createDefaultMalformedAttributeException(name, getParsingContext());
	}

	protected String getParsingContext()
	{
		Element parent = activeElement.getParent();
		
		if (parent != null) {
			StringBuilder parentStr = new StringBuilder();
			parent.toXML("", parentStr);
			return XMLProcessPointer.getParsingContext(parentStr.toString(),
					parent.getLineNumber(),
					activeElement.getLineNumber());
		}
		
		StringBuilder elementStr = new StringBuilder();
		activeElement.toXML("", elementStr);
		return XMLProcessPointer.getParsingContext(elementStr.toString(),
				0, 0);
	}

	public boolean startSubcontext(Element el) 
	{
		if(!el.getParent().equals(activeElement))
		{
			// this is caused by an illegal program state:
			throw new IllegalStateException("the provided element must be a child of the active element");
		}
		
		activeElement = el;
		
		return true; // child, all cool -> return true
	}
	
	/**
	 * Starts a new subcontext by matching the given element name with all child
	 * names in the active element. This method is actually insecure because it fails
	 * to recognize two children with the same name. For example
	 * <ActiveElement>
	 *      <Child width="123123"> some stuff here  </Child>
	 *      <Child width="653"> other stuff here </Child>
	 * </ActiveElement>
	 * Using startSubcontext("Child") will throw an exception due to ambiguity
	 * Thus, you better use startSubcontext(Element)
	 * 
	 * Esa's comment: This function is not to be used to load a list of children
	 * @throws MissingElementException 
	 */
	public boolean startSubcontext(String name) throws IXMLStreamableException
	{
		Element child = null;
		for(Element e: activeElement.getChildren())
		{
			if(e.getName().equals(name))
			{
				// TODO provide error location in XML file
				if(child != null)
				{
					throw new IXMLStreamableException("Ambiguity: There are at least two " +
							"children with name "+name + "\n\n" + getParsingContext());
				}
				else
				{
					child = e;
				}
			}
		}
		
		if(child == null)
		{
			return false; // child not found --> return false
		}
		
		return startSubcontext(activeElement.getChild(name));
	}


	/* (non-Javadoc)
	 * @see org.fenggui.io.InputOutputStream#endSubcontext()
	 */
	public void endSubcontext()
	{
		activeElement = activeElement.getParent();
	}

	public <T extends IXMLStreamable> T processChild(String name, T value, Class<T> clazz) 
		throws IOException, IXMLStreamableException
	{
		Element child = activeElement.getChild(name);
		
		if(child == null) throw new MissingElementException("Could not find <"+name
				+"> child element in <"+activeElement.getName()+">\n\n"
				+getParsingContext());
		
		T t = loadChild(child, clazz);
		
		return t;
	}

	@SuppressWarnings("unchecked")
	public <T extends IXMLStreamable> T processChild(T value, TypeRegister typeRegister) 
		throws IOException, IXMLStreamableException
	{
		if (typeRegister.isEmpty()) {
			throw new IXMLStreamableException("Empty TypeRegister\n\n" 
					+ getParsingContext());
		}
		
		for(Element e: activeElement.getChildren())
		{
			if(typeRegister.containsType(e.getName()))
			{
				Class<T> clazz = (Class<T>) typeRegister.getType(e.getName());
				
				T t = loadChild(e, clazz);
				
				return t;
			}
		}
		
		throw new MissingElementException("Required sub-element not found in <"
				+activeElement.getName()+">"
				+ " (expected one of: " + getNameList(typeRegister.getNames())
				+ ")\n\n" + getParsingContext());
	}

	@SuppressWarnings("unchecked")
	public <T extends IXMLStreamable> void processChildren(String childName, List children, Class<T> childClass) 
		throws IOException, IXMLStreamableException
	{
		for(Element e: activeElement.getChildren())
		{
			if(e.getName().equals(childName))
			{
				T t = loadChild(e, childClass);
				children.add(t);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void processChildren(List children, TypeRegister typeRegister) 
		throws IOException, IXMLStreamableException
	{
		ArrayList<IXMLStreamable> list = new ArrayList<IXMLStreamable>();
		
		for(Element e: activeElement.getChildren())
		{
			if(typeRegister.containsType(e.getName()))
			{
				Class<IXMLStreamable> clazz = (Class<IXMLStreamable>) typeRegister.getType(e.getName());
				
				if (clazz == null) {
					addWarning("The XML element " + e.getName() +
							" was treated as a subcontext");
					startSubcontext(e);
					processChildren(children, typeRegister);
					endSubcontext();
				}
				else {
					IXMLStreamable t = loadChild(e, clazz);
					list.add(t);
				}
			}
		}
		
		children.addAll(list);
	}

	protected String getActiveElementName()
	{
		return activeElement.getName();
	}

	public <T extends IXMLStreamable> void processInherentChild(String name, T value) 
		throws IOException, IXMLStreamableException
	{
		if(value == null)
			throw new IllegalArgumentException("value must not be null!");
		
		// using getChild(name) is insecure again, actually
		// but must cases of processInherentChild use a very peculiar element name 
		// (e.g. CloseButton)
		Element e = activeElement.getChild(name);
		
		if(e == null)
			throw new MissingElementException("Missing <"+name+"> in <"+activeElement.getName()+">");
		
		startSubcontext(e);
		
		value.process(this);
		
		endSubcontext();
		
	}

	@SuppressWarnings("unchecked")
	private <T extends IXMLStreamable> T handleRef(Class<T> childClass) throws ChildConstructionException
	{
		String ref = activeElement.getAttributeValue(ATTRIBUTE_REFERENCE);
		
		IXMLStreamable childObj = get(ref);

		if (childObj == null)
		{
			throw new ChildConstructionException("the element " + getActiveElementName()
				+ " refers to an another element '" + ref + "', but that element can't be found "
				+ "in the context" + "\n\n" + getParsingContext() + "\n");
		}

		if (!childClass.isInstance(childObj))
		{
			throw new ChildConstructionException("the element "
				+ getActiveElementName() + " refers to an another element of type " + childObj.getClass().getName()
				+ ", but the type of the referenced object should be " + childClass.getName() + "\n\n"
				+ getParsingContext() + "\n");
		}
		
		return (T) get(ref); // default refYype is call-by-reference
	}

	/**
	 * Loads a child element. If the passed reference is not null,
	 * the child element is searched from the active context.
	 * 
	 * @param childIdentifier the name of the new child
	 * @param reference the name of the element this element refers to, or null
	 * @param childClass the class of the child element
	 * @param constr the constructor which should be used to produce a new child
	 *        element
	 * @return a child element
	 * @throws ChildConstructionException if the child element can't be
	 *         constructed, the element to which the reference points to can't
	 *         be found or the reference points to an element of incompatible
	 *         type
	 * @throws NameShadowingException if the name of the new child element
	 *         would shadow an existing name and name shadowing is turned off
	 */
	
	protected <T extends IXMLStreamable> T loadChild(Element el, 
			Class<T> childClass) 
		throws IXMLStreamableException, NameShadowingException, IOException
	{
		startSubcontext(el);
		
		T child = null;

		if (activeElement.getAttributeValue(ATTRIBUTE_REFERENCE) != null)
		{
			child = handleRef(childClass);
		}
		else
		{
			child = constructObject(childClass);
		}

		if (activeElement.getAttributeValue(ATTRIBUTE_NAME) != null)
		{
			handleName(child);
		}
		
		endSubcontext();
		return child;
	}

	private void handleName(IXMLStreamable child) throws NameShadowingException
	{
		try
		{
			put(activeElement.getAttributeValue(ATTRIBUTE_NAME), child);
		}
		catch (NameShadowingException e)
		{
			throw new NameShadowingException("the name of the element " + getActiveElementName()
					+ " shadows the previously declared name " + activeElement.getAttributeValue(ATTRIBUTE_NAME) + "\n\n" + getParsingContext()
					+ "\n");
		}
	}
	
	@Override
	public void close() throws IOException
	{
		in.close();
	}


	
}
