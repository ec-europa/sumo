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
 * $Id: EventBinding.java 278 2007-05-11 16:07:38Z bbeaulant $
 */
package org.fenggui.render.jogl;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.fenggui.Display;




/**
 * Convenience class to map AWT mouse and keyboard events to FengGUI. 
 * 
 * Note that the mouse event methods in
 * Display return a boolean whether the mouse click hit a GUI component or not.
 * If you need this information, you need to implement this mapping on your own.
 *
 * @see org.fenggui.Display
 * @author Johannes Schaback
 */
public class EventBinding implements KeyListener, MouseMotionListener, MouseListener, MouseWheelListener {

    Display display = null;
    
    /**
     * Creates a new Binding.
     * @param c the JOGLUI canvas
     * @param d the FengGUI Display
     */
    public EventBinding(Component c, Display d) 
    {
        display = d;
        
        // makes FengGUI listen to tab keys
        // http://answers.google.com/answers/threadview?id=126916
        c.setFocusTraversalKeysEnabled(false);
        
        c.addMouseListener(this);
        c.addMouseMotionListener(this);
        c.addMouseWheelListener(this);
        c.addKeyListener(this);
    }
    
    /**
     * Forwards the key typed event to the Display.
     * @param e the event
     */
    public void keyTyped(KeyEvent e) 
    {
        display.fireKeyTypedEvent(e.getKeyChar());
    }

    /**
     * Forwards the key pressed event to the Display.
     * @param e the event
     */
    public void keyPressed(KeyEvent e) 
    {
        display.fireKeyPressedEvent(e.getKeyChar(), EventHelper.getKeyPressed(e));
    }

    /**
     * Forwards the key released event to the Display.
     * @param e the event
     */
    public void keyReleased(KeyEvent e) 
    {
        display.fireKeyReleasedEvent(e.getKeyChar(), EventHelper.getKeyPressed(e));
    }

    /**
     * Forwards the mouse dragged event to the Display.
     * @param e the event
     */
    public void mouseDragged(MouseEvent e)
    {
        display.fireMouseDraggedEvent(e.getX(), display.getHeight()- e.getY(), 
        		EventHelper.getMouseButton(e));
    }

    /**
     * Forwards the mouse moved event to the Display.
     * @param e the event
     */
    public void mouseMoved(MouseEvent e)
    {
        display.fireMouseMovedEvent(e.getX(), display.getHeight()-e.getY());
    }

    /**
     * Does nothing.
     * @param arg0 the event
     */
    public void mouseClicked(MouseEvent arg0) 
    {
        // does nothing... 
    }

    /**
     * Forwards the mouse pressed event to the Display.
     * @param e the event
     */
    public void mousePressed(MouseEvent e) 
    {
        display.fireMousePressedEvent(e.getX(), display.getHeight()-e.getY(), 
        		EventHelper.getMouseButton(e), e.getClickCount());
    }

    /**
     * Forwards the mouse released event to the Display.
     * @param e the event
     */
    public void mouseReleased(MouseEvent e)
    {
        display.fireMouseReleasedEvent(e.getX(), display.getHeight()-e.getY(), 
        		EventHelper.getMouseButton(e), e.getClickCount());
    }

    /**
     * Does nothing. 
     * @param e the event
     */
    public void mouseEntered(MouseEvent e) 
    {
    }

    /**
     * Does nothing. 
     * @param e the event
     */
    public void mouseExited(MouseEvent e) 
    {
    }

	/* (non-Javadoc)
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		display.fireMouseWheel(e.getX(), e.getY(), e.getWheelRotation() < 0, Math.abs(e.getWheelRotation()));
	}
    
    

}
