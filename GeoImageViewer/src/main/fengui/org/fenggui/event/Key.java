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
 * $Id: Key.java 278 2007-05-11 16:07:38Z bbeaulant $
 */
package org.fenggui.event;

/**
 * Enumeration class that holds key types. 
 * Non-alpha-numerical keys all have their own class.
 * 
 * @author Johannes Schaback, last edited by $Author: bbeaulant $, $Date: 2007-05-11 18:07:38 +0200 (Fr, 11 Mai 2007) $
 * @version $Revision: 278 $
 *
 */
public enum Key {

	UNDEFINED, LETTER, DIGIT, ESCAPE,
	LEFT, RIGHT, UP, DOWN, SHIFT, CTRL, 
	ALT, ENTER, INSERT, DELETE, HOME,
	END, PAGE_UP, PAGE_DOWN, BACKSPACE, TAB,
	F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12
	
	
	
}
