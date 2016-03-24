/*
 * 
 */
package org.fenggui.util;

import java.util.Hashtable;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FontSAXHandler extends DefaultHandler
{
	private StringBuilder builder = null;
	private Hashtable<Character, CharacterPixmap> texHashMap = null;
	private char character = 'c';
	private int x = 0, y = 0, width = 0, height = 0, charWidth = 0;
	
	public FontSAXHandler(Hashtable<Character, CharacterPixmap> texHashMap)
	{
		this.texHashMap = texHashMap;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException 
	{
		if(builder != null) builder.append(ch, start, length);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException 
	{
		if(qName.equals("CharacterPixmap"))
		{
			x = 0;
			y = 0;
			width = 0;
			height = 0;
			charWidth = 0;
			
			x = Integer.parseInt(attributes.getValue("x"));
			y = Integer.parseInt(attributes.getValue("y"));
			width = Integer.parseInt(attributes.getValue("width"));
			height = Integer.parseInt(attributes.getValue("height"));
			charWidth = Integer.parseInt(attributes.getValue("charWidth"));
			
			
		} else if(qName.equals("character")) builder = new StringBuilder();
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException 
	{
		if(qName.equals("character"))
		{
			String s = builder.toString();
			if(s.length() > 1) throw new SAXException("The <character> element holds a string that contains more than one character!");
			character = s.charAt(0);
			builder = null;
		} 
		else if(qName.equals("CharacterPixmap"))
		{
			CharacterPixmap cp = new CharacterPixmap(null, x, y, width, height, character, charWidth);
			texHashMap.put(character, cp);
			//System.out.println(character+" "+ cp);
		} 
	}



}
