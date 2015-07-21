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
 * $Id: ObservableWidget.java 353 2007-08-30 14:59:42Z marcmenghin $
 */
package org.fenggui;

import java.util.ArrayList;

import org.fenggui.event.ActivationEvent;
import org.fenggui.event.FocusEvent;
import org.fenggui.event.IActivationListener;
import org.fenggui.event.IFocusListener;
import org.fenggui.event.IKeyPressedListener;
import org.fenggui.event.IKeyReleasedListener;
import org.fenggui.event.IKeyTypedListener;
import org.fenggui.event.Key;
import org.fenggui.event.KeyPressedEvent;
import org.fenggui.event.KeyReleasedEvent;
import org.fenggui.event.KeyTypedEvent;
import org.fenggui.event.mouse.IMouseDraggedListener;
import org.fenggui.event.mouse.IMouseEnteredListener;
import org.fenggui.event.mouse.IMouseExitedListener;
import org.fenggui.event.mouse.IMouseMovedListener;
import org.fenggui.event.mouse.IMousePressedListener;
import org.fenggui.event.mouse.IMouseReleasedListener;
import org.fenggui.event.mouse.IMouseWheelListener;
import org.fenggui.event.mouse.MouseDraggedEvent;
import org.fenggui.event.mouse.MouseEnteredEvent;
import org.fenggui.event.mouse.MouseExitedEvent;
import org.fenggui.event.mouse.MouseMovedEvent;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.event.mouse.MouseReleasedEvent;
import org.fenggui.event.mouse.MouseWheelEvent;

/**
 * Widget that travels between states.
 * 
 * Every Widget can be disabled and enabled. Thus, StateWidget dictates
 * its subclasses to implement <code>getDefaultState</code> and
 * <code>getDisabledtState</code>. The default state is regardes as the
 * 'enabled' state.<br/>
 * <br/>
 * As a convention, subclasses of <code>StateWidget</code> should contain
 * at least the methods <code>getDefaultAppearnace</code> and
 * <code>getDisabledAppearance</code>. Both appearances can then be
 * access through the theme loader by 'defaultAppearance' and 
 * 'disbledAppearance'.
 * 
 * TODO Does a container really have to a StateWidget? Only for enabling/disabling?
 * 
 * @todo Comment this class... #
 * 
 * @author Johannes Schaback, last edited by $Author: marcmenghin $, $Date: 2007-08-30 16:59:42 +0200 (Do, 30 Aug 2007) $
 * @version $Revision: 353 $
 */
public abstract class ObservableWidget extends StandardWidget 
{
	private boolean enabled = true;
	private IKeyPressedListener keyTraversalListener = null;
	
	public boolean isEnabled()
	{
		return enabled;
	}

	public void setTraversable(boolean b)
	{
		if(b == isTraversable()) return;
		
		if(b)
		{
			keyTraversalListener = new IKeyPressedListener() {

				public void keyPressed(KeyPressedEvent keyPressedEvent)
				{
					if(keyPressedEvent.getKeyClass() == Key.TAB)
					{
						IWidget w = getNextTraversableWidget();
						
						Display disp = getDisplay();
						
						if(disp != null)
							disp.setFocusedWidget(w);
					}
				}};
				
			keyPressedHook.add(keyTraversalListener);
		}
		else
		{
			keyPressedHook.remove(keyTraversalListener);
			
			keyTraversalListener = null;
		}
	}

	public IWidget getNextTraversableWidget() 
	{
		return getParent().getNextTraversableWidget(this);
	}

	public IWidget getPreviousTraversableWidget() 
	{
		return getParent().getPreviousTraversableWidget(this);
	}
	
	public IWidget getNextWidget()
	{
		return getParent().getNextWidget(this);
	}

	public IWidget getPreviousWidget()
	{
		return getParent().getPreviousWidget(this);
	}
	
	@Override
	public boolean isTraversable()
	{
		return keyTraversalListener != null;
	}

	public void setEnabled(boolean enabled)
	{
		if (this.enabled == enabled){
			// No need to (des)activate the same widget twice or more 
			return;
		}
		
		this.enabled = enabled;
		
		ActivationEvent e = new ActivationEvent(this, enabled);
		
		for(IActivationListener l: activationHook)
		{
			l.widgetActivationChanged(e);
		}
	}

	
	private ArrayList<IActivationListener> activationHook = new ArrayList<IActivationListener>(0);
	private ArrayList<IMouseEnteredListener> mouseEnteredHook = new ArrayList<IMouseEnteredListener>(0);
	private ArrayList<IMouseMovedListener> mouseMovedHook = new ArrayList<IMouseMovedListener>(0);
	private ArrayList<IMouseExitedListener> mouseExitedHook = new ArrayList<IMouseExitedListener>(0);
	private ArrayList<IMousePressedListener> mousePressedHook = new ArrayList<IMousePressedListener>(0);
	private ArrayList<IMouseReleasedListener> mouseReleasedHook = new ArrayList<IMouseReleasedListener>(0);
	private ArrayList<IFocusListener> focusGainedHook = new ArrayList<IFocusListener>(0);
	private ArrayList<IMouseDraggedListener> mouseDraggedHook = new ArrayList<IMouseDraggedListener>(0);
	private ArrayList<IMouseWheelListener> mouseWheeledHook = new ArrayList<IMouseWheelListener>(0);
	private ArrayList<IKeyPressedListener> keyPressedHook = new ArrayList<IKeyPressedListener>(0);
	private ArrayList<IKeyReleasedListener> keyReleasedHook = new ArrayList<IKeyReleasedListener>(0);
	private ArrayList<IKeyTypedListener> keyTypedHook = new ArrayList<IKeyTypedListener>(0);

	
	public void addKeyReleasedListener(IKeyReleasedListener l)
	{
		keyReleasedHook.add(l);
	}
	
	public void removeKeyReleasedListener(IKeyReleasedListener l)
	{
		keyReleasedHook.remove(l);
	}
	
	public void addKeyPressedListener(IKeyPressedListener l)
	{
		keyPressedHook.add(l);
	}
	
	public void removeKeyPressedListener(IKeyPressedListener l)
	{
		keyPressedHook.remove(l);
	}
	
	public void addKeyTypedListener(IKeyTypedListener l)
	{
		keyTypedHook.add(l);
	}
	
	public void removeKeyTypedListener(IKeyTypedListener l)
	{
		keyTypedHook.remove(l);
	}
	
	public void addMouseDraggedListener(IMouseDraggedListener l)
	{
		mouseDraggedHook.add(l);
	}

	public void removeMouseDraggedListener(IMouseDraggedListener l)
	{
		mouseDraggedHook.remove(l);
	}
	
	public void addMouseMovedListener(IMouseMovedListener l)
	{
		mouseMovedHook.add(l);
	}

	public void removeMouseMovedListener(IMouseMovedListener l)
	{
		mouseMovedHook.remove(l);
	}
	
	public void addMouseReleasedListener(IMouseReleasedListener l)
	{
		mouseReleasedHook.add(l);
	}

	public void removeMouseReleasedListener(IMouseReleasedListener l)
	{
		mouseReleasedHook.remove(l);
	}
	
	public void addMousePressedListener(IMousePressedListener l)
	{
		mousePressedHook.add(l);
	}

	public void removeMousePressedListener(IMousePressedListener l)
	{
		mousePressedHook.remove(l);
	}
	
	public void addMouseExitedListener(IMouseExitedListener l)
	{
		mouseExitedHook.add(l);
	}

	public void removeMouseExitedListener(IMouseExitedListener l)
	{
		mouseExitedHook.remove(l);
	}
	
	public void addMouseEnteredListener(IMouseEnteredListener l)
	{
		mouseEnteredHook.add(l);
	}
	
	public void removeMouseEnteredListener(IMouseEnteredListener l)
	{
		mouseEnteredHook.remove(l);
	}
	
	public void addFocusListener(IFocusListener l)
	{
		focusGainedHook.add(l);
	}

	public void removeFocusListener(IFocusListener l)
	{
		focusGainedHook.remove(l);
	}
	
	
	public void addMouseWheelListener(IMouseWheelListener l)
	{
		mouseWheeledHook.add(l);
	}
	
	public void removeMouseWheelListener(IMouseWheelListener l)
	{
		mouseWheeledHook.remove(l);
	}
	
	public void mouseEntered(MouseEnteredEvent mouseEnteredEvent)
	{
		if(!enabled || !isVisible()) return;
			
		for(IMouseEnteredListener l: mouseEnteredHook)
		{
			l.mouseEntered(mouseEnteredEvent);
		}
	}
	
	public void addActivationListener(IActivationListener l)
	{
		activationHook.add(l);
	}

	public void removeActivationListener(IActivationListener l)
	{
		activationHook.remove(l);
	}
	
	public void mouseExited(MouseExitedEvent mouseExitedEvent)
	{
	  if(!enabled || !isVisible()) return;
		
		for(IMouseExitedListener l: mouseExitedHook)
		{
			l.mouseExited(mouseExitedEvent);
		}
	}


	public void mousePressed(MousePressedEvent mousePressedEvent)
	{
	  if(!enabled || !isVisible()) return;
		
		for(IMousePressedListener l: mousePressedHook)
		{
			l.mousePressed(mousePressedEvent);
		}
	}


	public void mouseMoved(int displayX, int displayY)
	{
		if(mouseMovedHook.isEmpty() || !enabled || !isVisible()) return;
		
		MouseMovedEvent e = new MouseMovedEvent(null, displayX, displayY);
		
		for(IMouseMovedListener l: mouseMovedHook)
		{
			l.mouseMoved(e);
		}
	}


	public void mouseDragged(MouseDraggedEvent mouseDraggedEvent)
	{
		if(!enabled || !isVisible()) return;
		
		for(IMouseDraggedListener l: mouseDraggedHook)
		{
			l.mouseDragged(mouseDraggedEvent);
		}
	}


	public void mouseReleased(MouseReleasedEvent mouseReleasedEvent)
	{
	  if(!enabled || !isVisible()) return;
		
		for(IMouseReleasedListener l: mouseReleasedHook)
		{
			l.mouseReleased(mouseReleasedEvent);
		}
	}


	public void keyPressed(KeyPressedEvent keyPressedEvent)
	{
	  if(!enabled || !isVisible()) return;
		
		for(IKeyPressedListener l: keyPressedHook)
		{
			l.keyPressed(keyPressedEvent);
		}
	}


	public void keyReleased(KeyReleasedEvent keyReleasedEvent)
	{
	  if(!enabled || !isVisible()) return;
		
		for(IKeyReleasedListener l: keyReleasedHook)
		{
			l.keyReleased(keyReleasedEvent);
		}
	}
	
	public void keyTyped(KeyTypedEvent keyTypedEvent)
	{
	  if(!enabled || !isVisible()) return;
		
		for(IKeyTypedListener l: keyTypedHook)
		{
			l.keyTyped(keyTypedEvent);
		}
	}
	
	public void focusChanged(FocusEvent focusGainedEvent)
	{
	  if(!enabled || !isVisible()) return;
		
		for(IFocusListener l: focusGainedHook)
		{
			l.focusChanged(focusGainedEvent);
		}
	}

	public void mouseWheel(MouseWheelEvent e)
	{
	  if(!enabled || !isVisible()) return;
		
		for(IMouseWheelListener l: mouseWheeledHook)
		{
			l.mouseWheel(e);
		}
	}
}
