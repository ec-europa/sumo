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
 * Created on Jan 30, 2006
 * $Id: EventHelper.java 211 2007-02-22 02:25:13Z schabby $
 */
package org.fenggui.render.jogl;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.fenggui.event.Key;
import org.fenggui.event.mouse.MouseButton;

/**
 * 
 * Maps from <code>java.awt.KeyEvent</code> to 
 * <code>org.fenggui.event.key.Key</code>
 * 
 * @todo Not all keys are correctly mapped yet!! #
 * 
 * @author Johannes Schaback, last edited by $Author: schabby $, $Date: 2007-02-22 03:25:13 +0100 (Thu, 22 Feb 2007) $
 * @version $Revision: 211 $
 */
public class EventHelper {

	
	public static MouseButton getMouseButton(MouseEvent me) {
		switch(me.getButton()) {
			case MouseEvent.BUTTON1:
				return MouseButton.LEFT;
			case MouseEvent.BUTTON2:
				return MouseButton.MIDDLE;
			case MouseEvent.BUTTON3:
				return MouseButton.RIGHT;
			case MouseEvent.MOUSE_WHEEL:
				return MouseButton.WHEEL;
			default:
				return MouseButton.LEFT;
		}
	}
	
	public static Key getKeyPressed(KeyEvent ke) {
		
		Key keyClass;
		
        switch(ke.getKeyCode()) 
        {
        case KeyEvent.VK_BACK_SPACE:
        	keyClass = Key.BACKSPACE;
            break;
        case KeyEvent.VK_ENTER:
        	keyClass = Key.ENTER;
            break;
        case KeyEvent.VK_ESCAPE: 
        	keyClass = Key.ESCAPE;
            break;
        case KeyEvent.VK_DELETE: 
        	keyClass = Key.DELETE;
            break;
        case KeyEvent.VK_UP:
        	keyClass = Key.UP;
        	break;
        case KeyEvent.VK_RIGHT:
        	keyClass = Key.RIGHT;
            break;
        case KeyEvent.VK_LEFT:
        	keyClass = Key.LEFT;
            break;
        case KeyEvent.VK_DOWN:
        	keyClass = Key.DOWN;
            break;
        case KeyEvent.VK_END:
        	keyClass = Key.END;
        	break;
        case KeyEvent.VK_HOME:
        	keyClass = Key.HOME;
        	break;
        case KeyEvent.VK_SHIFT:
        	keyClass = Key.SHIFT;
            break;
        case KeyEvent.VK_ALT:
        	keyClass = Key.ALT;
            break;
        case KeyEvent.VK_CONTROL:
        	keyClass = Key.CTRL;
            break;
        case KeyEvent.VK_INSERT:
        	keyClass = Key.INSERT;
            break;
        case KeyEvent.VK_TAB:
        	keyClass = Key.TAB;
            break;            
        case KeyEvent.VK_F12:
        	keyClass = Key.F12;
            break;
        case KeyEvent.VK_F11:
        	keyClass = Key.F11;
            break;
        case KeyEvent.VK_F10:
        	keyClass = Key.F10;
            break;
        case KeyEvent.VK_F9:
        	keyClass = Key.F9;
            break;
        case KeyEvent.VK_F8:
        	keyClass = Key.F8;
            break;
        case KeyEvent.VK_F7:
        	keyClass = Key.F7;
            break;
        case KeyEvent.VK_F6:
        	keyClass = Key.F6;
            break;
        case KeyEvent.VK_F5:
        	keyClass = Key.F5;
            break;
        case KeyEvent.VK_F4:
        	keyClass = Key.F4;
            break;
        default:
        	if("1234567890".indexOf(ke.getKeyChar()) != -1) {
        		keyClass = Key.DIGIT;
        	} else { 
        		// @todo must not necessarily be a letter!! #
        		keyClass = Key.LETTER;
        	}
        	break;
    	}
		
        return keyClass;
		
	}
	
}
