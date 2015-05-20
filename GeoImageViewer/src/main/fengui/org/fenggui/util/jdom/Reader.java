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
 * Created on Dec 17, 2006
 * $Id: Reader.java 154 2007-01-25 23:52:12Z schabby $
 */
package org.fenggui.util.jdom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Reader extends DefaultHandler
{
	private ArrayList<Element> stack = new ArrayList<Element>();
	private Document doc = null;
	private Locator locator = null;
	
	public Reader()
	{
		
	}
	
	public Document parse(File f) throws FileNotFoundException
	{
		return parse(new FileInputStream(f));
		
	}

	public Document parse(InputStream is)
	{
		doc = null;
		stack.clear();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try
		{
			// Set up output stream
			// out = new OutputStreamWriter(System.out, "UTF8"); 
			// Parse the input 
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(is, this);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		
		return doc;
	}

	public void setDocumentLocator(Locator l)
	{
		super.setDocumentLocator(l);
		locator = l;
		//System.out.println("LOCATOR");
		//System.out.println("ID: " + l.getSystemId() + " "+l.getColumnNumber()+" "+l.getLineNumber());
	} 

	@Override
	public void endDocument() throws SAXException
	{
		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		pop();
	}


	@Override
	public void startElement(String uri, String localName, String qName, Attributes attribs) throws SAXException
	{
		// currently processing root element
		if(doc == null)
		{
			this.doc = new Document(qName);
			stack.clear();
			stack.add(doc);
		}
		else
		{
			Element el = new Element(qName, locator.getLineNumber());
			
			for(int i = 0;i < attribs.getLength(); i++)
			{
				el.setAttribute(attribs.getQName(i), attribs.getValue(i));
			}
			
			peek().add(el);
			push(el);
		}
	}

	private void push(Element e)
	{
		stack.add(e);
	}
	
	private Element peek()
	{
		return stack.get(stack.size()-1);
	}
	

	private void pop()
	{
		stack.remove(stack.size()-1);
	}

	class MyDocumentLocator implements Locator
	{

		public String getPublicId()
		{
			System.out.println("get public ID");
			return null;
		}

		public String getSystemId()
		{
			System.out.println("get getSystemId ID");
			return null;
		}

		public int getLineNumber()
		{
			System.out.println("get getLineNumber ID");
			return 0;
		}

		public int getColumnNumber()
		{
			System.out.println("get getColumnNumber ID");
			return 0;
		}
		
	}
	
}
