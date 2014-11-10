/*
 * FengGUI - Java GUIs in OpenGL (http://www.fenggui.org)
 * 
 * Copyright (c) 2005, 2006 FengGUI Project
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
 * $Id: CharacterPixmap.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.util;

import java.io.IOException;

import org.fenggui.render.ITexture;
import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.theme.xml.MalformedElementException;

/**
 * A character pixmap is especially designed for describing
 * characters on textures.<br/> 
 *  
 * 
 * @todo Comment this class... #
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public class CharacterPixmap extends Pixmap 
{
	private static final int DEFAULT_CHAR_WIDTH = 10;
	
	private static final String ATTR_CHARACTER = "char";
	private static final String ATTR_CHAR_WIDTH = "char-width";
	
	private char character;
	private int charWidth = DEFAULT_CHAR_WIDTH;
	
	public CharacterPixmap(ITexture texture, int x, int y, int width, int height, char c, int charWidth) {
		super(texture, x, y, width, height);
		this.charWidth = charWidth;
		character = c;
	}
	
	public char getCharacter() {
		return character;
	}

	public int getCharWidth() {
		return charWidth;
	}

	public void toXML(String blankOffset, StringBuilder buffer)
	{
		buffer.append(blankOffset);
		buffer.append("<CharacterPixmap");
		
		buffer.append(" x=\""+getX()+"\"");
		buffer.append(" y=\""+getY()+"\"");
		
		buffer.append(" width=\""+getWidth()+"\"");
		buffer.append(" height=\""+getHeight()+"\"");
		
		buffer.append(" charWidth=\""+getCharWidth()+"\">\n");
		
		buffer.append(blankOffset+"   ");
		buffer.append("<character><![CDATA["+character);
		buffer.append("]]></character>\n");
		buffer.append(blankOffset+"</CharacterPixmap>\n\n");
	}
	
	/* (non-Javadoc)
	 * @see org.fenggui.io.IOStreamSaveable#process(org.fenggui.io.InputOutputStream)
	 */
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		String charStr = stream.processAttribute(ATTR_CHARACTER, character + "");

		if (charStr.length() != 1) { throw MalformedElementException.createDefaultMalformedAttributeException(ATTR_CHARACTER, "a single character"); }

		character = charStr.charAt(0);
		charWidth = stream.processAttribute(ATTR_CHAR_WIDTH, charWidth, DEFAULT_CHAR_WIDTH);
	}
}
