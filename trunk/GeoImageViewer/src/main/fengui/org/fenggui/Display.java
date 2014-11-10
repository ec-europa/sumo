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
 * Created on 2005-3-2
 * $Id: Display.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import org.fenggui.composites.Window;
import org.fenggui.event.DisplayResizedEvent;
import org.fenggui.event.Event;
import org.fenggui.event.FocusEvent;
import org.fenggui.event.IDisplayResizedListener;
import org.fenggui.event.IDragAndDropListener;
import org.fenggui.event.IEventListener;
import org.fenggui.event.Key;
import org.fenggui.event.KeyPressedEvent;
import org.fenggui.event.KeyReleasedEvent;
import org.fenggui.event.KeyTypedEvent;
import org.fenggui.event.mouse.MouseButton;
import org.fenggui.event.mouse.MouseDraggedEvent;
import org.fenggui.event.mouse.MouseEnteredEvent;
import org.fenggui.event.mouse.MouseExitedEvent;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.event.mouse.MouseReleasedEvent;
import org.fenggui.event.mouse.MouseWheelEvent;
import org.fenggui.layout.StaticLayout;
import org.fenggui.render.Binding;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.theme.XMLTheme;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * Root of the widget tree. The Display spans over the whole screen.
 * Serves also as the entry point for the event distribution in the
 * widget tree.<br/>
 * 
 * @author Johannes Schaback aka Schabby, last changed by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public class Display extends Container
{
	private ArrayList<IDragAndDropListener> dndListeners = new ArrayList<IDragAndDropListener>();
	private ArrayList<IEventListener> globalEventListener = new ArrayList<IEventListener>();

	private IWidget mouseOverWidget = this;
	private Binding binding = null;
	private boolean depthTestEnabled = false;
	
	/**
	 * Widget that is dragged (and dropped)
	 */
	//private Widget dndWidget = null;
	private IDragAndDropListener draggingListener = null;

	private IWidget focusedWidget = null;

	private Widget popupWidget = null;

	private File screenshotFile = null;

	public Display()
	{
		this(Binding.getInstance());
	}
	
	/**
	 * Constructs a new <code>Display</code> object. Note that you can have
	 * several <code>Display</code> instances but only one <code>Binding</code>.
	 * @param binding the opengl binding used to render FengGUI.
	 */
	public Display(Binding binding)
	{
		assert (binding != null);
		this.binding = binding;
		this.setY(0);
		this.setX(0);

		//setMargin(Spacing.ZERO_SPACING);
		//setPadding(Spacing.ZERO_SPACING);

		setSize(binding.getCanvasWidth(), binding.getCanvasHeight());
		setLayoutManager(new StaticLayout());
		binding.addDisplayResizedListener(new IDisplayResizedListener()
		{

			public void displayResized(DisplayResizedEvent displayResizedEvent)
			{
				setSize(displayResizedEvent.getWidth(), displayResizedEvent.getHeight());
				layout();
			}

		});
		setupTheme(Display.class);
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		setLayoutManager((LayoutManager) stream.processChild(getLayoutManager(),
			XMLTheme.TYPE_REGISTRY));
		stream.processChildren(notifyList, XMLTheme.TYPE_REGISTRY);
		for(IWidget w: notifyList)
		{
			w.setParent(this);
		}
	}

	/**
	 * Returns true as the display is the root of the widget tree.
	 */
	@Override
	public final boolean isInWidgetTree()
	{
		return true;
	}

	/**
	 * Adds the given widget as a popup widget to this display.
	 * @param pus the popup widget
	 */
	public void displayPopUp(Widget pus)
	{
		// addWidget calls setParent() and addedToWidgetTree() 
		addWidget(pus);
		popupWidget = pus;
	}

	/**
	 * Removes the widget currently set as popup widget from the display.
	 */
	public void removePopup()
	{
		// Remove the popup from widgetTree
		removeWidget(popupWidget);
		popupWidget = null;
	}

	/**
	 * Returns null because the display is the root of the widget tree. 
	 */
	public final Container getParent()
	{
		return null;
	}

	/**
	 * Returns this <code>Display</code> instance. Realize the this method
	 * marks the end of recursive <code>getDisplay</code> calls.
	 */
	@Override
	public final Display getDisplay()
	{
		return this;
	}

	/**
	 * 
	 *
	 */
	public void display()
	{
		IOpenGL opengl = binding.getOpenGL();
		opengl.pushAllAttribs();

		opengl.activateTexture(0);

		opengl.setViewPort(0, 0, binding.getCanvasWidth(), binding.getCanvasHeight());

		opengl.setModelMatrixMode();
		opengl.pushMatrix();

		opengl.loadIdentity();

		opengl.setProjectionMatrixMode();
		opengl.pushMatrix();
		opengl.loadIdentity();

		opengl.setOrtho2D(0, binding.getCanvasWidth(), 0, binding.getCanvasHeight());

		opengl.setModelMatrixMode();

		opengl.setupStateVariables(depthTestEnabled);

		//opengl.translateZ(-50);

		Graphics g = binding.getGraphics();
		g.resetTransformations();

		for (int i = 0; i < getContent().size(); i++)
		{
			IWidget c = getContent().get(i);

			if (c == null)
			{
				//TODO: bug, removing components from cont. causes null pointers here #
				System.err.println("NullPointerEx. prevention :( It is known a bug caused by multi threading!");
				continue;
			}

			opengl.pushMatrix();

			clipWidget(g, c);

			g.translate(c.getX(), c.getY());

			c.paint(g);

			g.translate(-c.getX(), -c.getY());
			opengl.popMatrix();

		}

		opengl.setProjectionMatrixMode();

		opengl.popMatrix();

		opengl.setModelMatrixMode();
		opengl.popMatrix();

		opengl.popAllAttribs();

		if (screenshotFile != null)
		{
			screenshot(opengl, getWidth(), getHeight(), screenshotFile);
			screenshotFile = null;
		}
	}

	/**
	 * @return the focused widget
	 */
	public IWidget getFocusedWidget()
	{
		return focusedWidget;
	}

	/**
	 * Sets the focused widget
	 * 
	 * @param widget the widget to receive the focus
	 */
	public void setFocusedWidget(IWidget widget)
	{
		if (focusedWidget != null && !focusedWidget.equals(widget))
		{
			FocusEvent e = new FocusEvent(focusedWidget, true);
			focusedWidget.focusChanged(e);
			fireGlobalEventListener(e);
		}

		focusedWidget = widget;

		if (widget != null)
		{
			FocusEvent e = new FocusEvent(widget, false);
			widget.focusChanged(e);
			fireGlobalEventListener(e);
		}
	}

	private boolean grandParentIsPopupWidget(IWidget w)
	{
		if(w.getParent() == null) return false;
		
		if(w.getParent().equals(popupWidget)) return true;
		
		return grandParentIsPopupWidget(w.getParent());
	}
	
	/**
	 * Triggers a mouse pressed event in joglui.
	 * @param mouseX distance of cursor to left hand side of screen
	 * @param mouseY distance of cursor to bottom of screen
	 * @param mouseButton the pressed mouse button
	 * @param clickCount indicates double click, tripple click, etc.
	 * @return true if GUI component within Display was hit, 
	 * false otherwise
	 */
	public boolean fireMousePressedEvent(int mouseX, int mouseY, MouseButton mouseButton, int clickCount)
	{
		IWidget w = getWidget(mouseX, mouseY);

		/*
		 * Exceptional case for pop up shell. Pop up shells are opened by clickling
		 * on a menu or combo box or so. If a second click does not fall in the pop up
		 * shell, the shell disappears.
		 * The popupWidget is marked to delete and will be delete after fireMousePressedEvent
		 * on the widget tree.
		 */
		IWidget toDeletePopupWidget = null;
		if (popupWidget != null    && 
			!w.equals(popupWidget) && 
			!grandParentIsPopupWidget(w))
		{
			toDeletePopupWidget = popupWidget;
		}

		// didn't hit the plain Display...
		boolean returnValue = false;

		if (!w.equals(this))
		{
			IWidget targetWidget = w;

			if (targetWidget.isTraversable() && !(targetWidget instanceof Container))
			{
				setFocusedWidget(targetWidget);
			}

			// Set the new focused widget

			MousePressedEvent e = new MousePressedEvent(w, mouseX, mouseY, mouseButton, clickCount);
			w.mousePressed(e);
			fireGlobalEventListener(e);

			for (int i = 0; i < dndListeners.size(); i++)
			{
				IDragAndDropListener dndListener = dndListeners.get(i);
				if (dndListener.isDndWidget(w, mouseX, mouseY))
				{
					dndListener.select(mouseX, mouseY);
					draggingListener = dndListener;
				}
			}

			// determine whether a frame was hit
			while (w.getParent() != null && !(w.getParent() instanceof Window))
				w = (Widget) w.getParent();

			if(w.getParent() instanceof Window && w.getParent().getParent() == this)
				bringToFront(w.getParent()); 
			
			// if yes, then re-order frame in list so that it is diplayed 
			// on top of all the others
			// @todo: crude way to bring frames to the top #
			// Done : now, windows put themselves on top of their parents
			// when their titlebar is pressed.

			returnValue = true;
		}
		else
		{
			// The user click outside all widgets, focusedWidget is set to null
			setFocusedWidget(null);
		}

		if (toDeletePopupWidget != null)
		{
			if (popupWidget.equals(toDeletePopupWidget))
			{
				removePopup();
			}
			else
			{
				// A new popup was added : only removed the 'toDeletePopupWidget'
				removeWidget(toDeletePopupWidget);
			}
		}

		return returnValue;
	}

	/**
	 * Triggers a mouse released event in FengGUI.
	 * @param mouseX distance of cursor to left hand side of screen
	 * @param mouseY distance of cursor to bottom of screen
	 * @param mouseButton the pressed mouse button
	 * @param clickCount
	 * @return true if GUI component within Display was hit, 
	 * false otherwise
	 */
	public boolean fireMouseReleasedEvent(int mouseX, int mouseY, MouseButton mouseButton, int clickCount)
	{
		IWidget w = getWidget(mouseX, mouseY);
		boolean ret = false;
		if (draggingListener != null)
		{
			draggingListener.drop(mouseX, mouseY, w);
			draggingListener = null;
			ret = true;
		}
		
		if (w.equals(this)) return ret;

		MouseReleasedEvent e = new MouseReleasedEvent(w, mouseX, mouseY, mouseButton, clickCount);
		w.mouseReleased(e);
		fireGlobalEventListener(e);

		return true;
	}

	/**
	 * Triggers a mouse dragged event.
	 * @param mouseX distance of cursor to left hand side of screen
	 * @param mouseY distance of cursor to bottom of screen
	 * @param mouseButton the pressed mouse button
	 * @return true if GUI component within Display was hit, 
	 * false otherwise
	 */
	public boolean fireMouseDraggedEvent(int mouseX, int mouseY, MouseButton mouseButton)
	{
		IWidget w = getWidget(mouseX, mouseY);

		if (draggingListener != null)
		{
			draggingListener.drag(mouseX, mouseY);
		}

		if (!mouseOverWidget.equals(w))
		{
			MouseExitedEvent exited = new MouseExitedEvent(w, mouseOverWidget);
			mouseOverWidget.mouseExited(exited);
			fireGlobalEventListener(exited);

			MouseEnteredEvent entered = new MouseEnteredEvent(w, mouseOverWidget);
			w.mouseEntered(entered);
			fireGlobalEventListener(entered);
		}
		mouseOverWidget = w;

		if (w.equals(this)) return false;

		MouseDraggedEvent e = new MouseDraggedEvent(w, mouseX, mouseY, mouseButton);
		w.mouseDragged(e);
		fireGlobalEventListener(e);

		return true;
	}

	public boolean fireMouseWheel(int mouseX, int mouseY, boolean up, int rotation)
	{
		IWidget w = getFocusedWidget();

		MouseWheelEvent e = new MouseWheelEvent(w, mouseX, mouseY, up, rotation);
		fireGlobalEventListener(e);

		if (w != null) w.mouseWheel(e);

		// if the widget under the mouse isn't the display, we mustn't send the event elsewhere.
		if (getWidget(mouseX, mouseY) == this) return false;
		
		return true;
		/* huh? What are you guys doing here? The focused widget has to receive
		 * the mouse wheel event!
		 // do not hit plain display
		 if (!w.equals(this))
		 {
		 for (Widget wi : notifyList)
		 {
		 if (wi.insideMargin(mouseX, mouseY)) wi.mouseWheel(new MouseWheelEvent(wi, up));
		 }
		 }
		 
		 return false;*/
	}

	/**
	 * Returns 0 because the display is the root of the widget tree. Realize that this
	 * method marks the end of a recursive call.
	 */
	public int getDisplayX()
	{
		return 0;
	}

	/**
	 * Returns 0 becayse the display is the root of the widget tree. Realize that this method
	 * marks the end of a recursive call.
	 */
	public int getDisplayY()
	{
		return 0;
	}

	/**
	 * Takes a screenshot and writes it in the given file.
	 * @param screenshotFile the file to store the screenshot
	 */
	public void takeScreenshot(File screenshotFile)
	{
		this.screenshotFile = screenshotFile;
	}

	private static final int TARGA_HEADER_SIZE = 18;

	/**
	 * Takes a screenshot of the current frame. This method is
	 * entirely copied from http://www.javagaming.org/forums/index.php?topic=8747.0
	 * @param gl FengGUIs opengl interface
	 * @param width the width of the screenshot
	 * @param height the height of the screenhost
	 * @param file the file where to store the screenshot
	 */
	private void screenshot(IOpenGL gl, int width, int height, File file)
	{
		try
		{
			RandomAccessFile out = new RandomAccessFile(file, "rw");
			FileChannel ch = out.getChannel();
			int fileLength = TARGA_HEADER_SIZE + width * height * 3;
			out.setLength(fileLength);
			MappedByteBuffer image = ch.map(FileChannel.MapMode.READ_WRITE, 0, fileLength);

			// write the TARGA header
			image.put(0, (byte) 0).put(1, (byte) 0);
			image.put(2, (byte) 2); // uncompressed type
			image.put(12, (byte) (width & 0xFF)); // width
			image.put(13, (byte) (width >> 8)); // width
			image.put(14, (byte) (height & 0xFF));// height
			image.put(15, (byte) (height >> 8));// height
			image.put(16, (byte) 24); // pixel size

			// go to image data position
			image.position(TARGA_HEADER_SIZE);
			// jogl needs a sliced buffer
			ByteBuffer bgr = image.slice();

			// read the BGR values into the image buffer
			gl.readPixels(0, 0, width, height, bgr);

			// close the file channel
			ch.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Triggers a mouse moved event. Note that only the Widget over
	 * which the mouse cursor is hovering will be notified by the
	 * mouse move event.
	 * @param displayX distance of cursor to left hand side of the screen
	 * @param displayY distance of cursor to bottom of the screen
	 * @return true if GUI component within Display was hit, 
	 * false otherwise
	 */
	public boolean fireMouseMovedEvent(int displayX, int displayY)
	{
		// retrieve Widget below mouse cursor
		IWidget w = getWidget(displayX, displayY);

		w.mouseMoved(displayX, displayY);

		// w points to a different Widget than before!
		if (!mouseOverWidget.equals(w))
		{
			MouseExitedEvent exited = new MouseExitedEvent(w, mouseOverWidget);
			mouseOverWidget.mouseExited(exited);
			fireGlobalEventListener(exited);

			MouseEnteredEvent entered = new MouseEnteredEvent(w, mouseOverWidget);
			w.mouseEntered(entered);
			fireGlobalEventListener(entered);
		}
		mouseOverWidget = w;
		return !w.equals(this);
	}

	public IWidget getWidget(int x, int y)
	{
		IWidget w = super.getWidget(x, y);
		if (w != null) return w;
		return this;
	}

	public boolean fireKeyPressedEvent(char keyValue, Key keyClass)
	{
		// TODO this should be removed and go as a screenshot utility
		if (keyClass == Key.F12)
		{
			System.out.println("Saving Screenshot...");
			takeScreenshot(new File(System.currentTimeMillis() + " Screenshot.tga"));
		}

		if (focusedWidget != null)
		{
			KeyPressedEvent e = new KeyPressedEvent(focusedWidget, keyValue, keyClass);
			focusedWidget.keyPressed(e);
			fireGlobalEventListener(e);
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean fireKeyReleasedEvent(char keyValue, Key keyClass)
	{
		if (focusedWidget != null)
		{
			KeyReleasedEvent e = new KeyReleasedEvent(focusedWidget, keyValue, keyClass);
			focusedWidget.keyReleased(e);
			fireGlobalEventListener(e);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean fireKeyTypedEvent(char keyValue)
	{
		if (focusedWidget != null)
		{
			KeyTypedEvent e = new KeyTypedEvent(focusedWidget, keyValue);
			focusedWidget.keyTyped(e);
			fireGlobalEventListener(e);
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Returns the currently displayed popup widget.
	 * @return popup widget
	 */
	public IWidget getPopupWidget()
	{
		return popupWidget;
	}

	/**
	 * Adds a global drag and drop listener to this display. 
	 * @param dndl the DnD listener
	 */
	public void addDndListener(IDragAndDropListener dndl)
	{
		if(!dndListeners.contains(dndl))
			dndListeners.add(dndl);
	}

	/**
	 * Removes the given drag and drop listener from this display.
	 * @param dndl the DnD listener
	 */
	public void removeDndListener(IDragAndDropListener dndl)
	{
		dndListeners.remove(dndl);
	}

	/**
	 * Fires the given event through the registered global event listeners
	 * @param event event to dispatch
	 */
	public void fireGlobalEventListener(Event event)
	{
		if (globalEventListener.isEmpty()) return;

		for (int i = 0; i < globalEventListener.size(); i++)
		{
			globalEventListener.get(i).processEvent(event);
		}
	}

	/**
	 * Adds the given global event listener to this display. The given listener
	 * will be notified upon every event except <code>MouseMovedEvent</code>.
	 * @param listener the global event listener
	 */
	public void addGlobalEventListener(IEventListener listener)
	{
		globalEventListener.add(listener);
	}

	public boolean isDepthTestEnabled()
	{
		return depthTestEnabled;
	}

	public void setDepthTestEnabled(boolean depthTestDisabled)
	{
		this.depthTestEnabled = depthTestDisabled;
	}

	/**
	 * Removes the global event listener from this display.
	 * @param listener event listener
	 */
	public void removeGlobalEventListener(IEventListener listener)
	{
		globalEventListener.remove(listener);
	}
	
	/**
	 * Checks if the <code>focusedWidget</code> is still in the widget tree
	 * an if no, sets it to null.
	 */
	protected void focusedWidgetValityCheck()
	{
		if(getFocusedWidget() != null && getFocusedWidget().getDisplay() == null)
		{
			setFocusedWidget(null);
		}
	}
	
}
