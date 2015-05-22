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
 * $Id: Alphabet.java 285 2007-05-20 16:10:10Z schabby $
 */
package org.fenggui.util;


/**
 * An Alphabet contains all characters of a certain 
 * natural language.
 * 
 * An alphabet contains usually not more then 100 characters (all letters
 * including lower and upper case and digits, punctuation, etc.). Alphabets
 * that have several thousand characters (e.g. Chinese) are not renderable
 * with FengGUI at the current state.<br/>
 * 
 * @todo Comment this class... #
 * @todo add more languages #
 * 
 * @author Johannes Schaback, last edited by $Author: schabby $, $Date: 2007-05-20 18:10:10 +0200 (So, 20 Mai 2007) $
 * @version $Revision: 285 $
 */
public class Alphabet
{

	public static final Alphabet ENGLISH = new Alphabet(); 
	
	/* see http://www.geocities.com/click2speak/unicode/chars_de.html */
	public static final Alphabet GERMAN = new Alphabet(new char[] {0x00C4, 0x00D6, 0x00DC, 0x00E4, 0x00F6, 0x00FC, 0x00DF});
	
	/* see http://www.geocities.com/click2speak/unicode/chars_fr.html */
	public static final Alphabet FRENCH = new Alphabet(new char[] {0x00C0, 
																   0x00C2, 
																   0x00C6, 0x00C8, 0x00C9, 0x00CA, 0x00CB, 0x00CE, 0x00CF, 0x00D4, 0x0152, 0x00D9,
																   0x00DB, 0x00DC, 0x0178, 0x00C7,
																   0x00E0, 0x00E2, 0x00E6, 0x00E8, 0x00E9, 0x00EA, 0x00EB, 0x00EE, 0x00EF, 0x00F4,
																   0x0153, 0x00F9, 0x00FB, 0x00FC, 0x00FF, 0x00E7});
	 public static final Alphabet ESTONIAN = new Alphabet(new char[]{
	            0x00F6, 0x00E4, 0x00F5, 0x00FC,
	            0x00D6, 0x00C4, 0x00D5, 0x00DC
	    });	
	/**
	 * Default Alphabet. It can be set by user through the setDefaultAlphabet method
	 */
	private static Alphabet defaultAlphabet = ENGLISH;
	
	/**
	 * @return The default Alphabet
	 */
	public static Alphabet getDefaultAlphabet()
	{
		return defaultAlphabet;
	}
	
	/**
	 * Set the default Alphabet
	 * @param alphabet
	 */
	public static void setDefaultAlphabet(Alphabet alphabet)
	{
		defaultAlphabet = alphabet;
	}
	
	/**
	 * Creates a standard character set alphabet (English).
	 */
	public Alphabet()
	{
		
	}
	
	private char[] alphabet = 
		(	"abcdefghijklmnopqrstuvwxyz" + 
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ" + 
			"`1234567890-=" + 
			"~!@#$%^&*()_+" + 
			"[]{}\\|" + 
			":;\"'" + 
			"<>,.?/ ").toCharArray();


	/**
	 * Creates a standard character set (English) and adds the
	 * given additional chars to the alphabet. 
	 * 
	 * @param additionalChars Additional chars to the basic alphabet
	 */
	public Alphabet(char[] additionalChars)
	{
		String a = new String(additionalChars);
		a += new String(alphabet);
		alphabet = a.toCharArray();
	}


	/**
	 * @return return the Alphabet
	 */
	public char[] getAlphabet()
	{
		return alphabet;
	}


	/**
	 * Returns whether the specified character is in this alphabet.
	 * @param c the character
	 * @return true if it's in the alphabet, false otherwise
	 */
	public boolean valid(char c)
	{
		for (char p : alphabet)
			if (c == p) return true;

		return false;
	}

}
