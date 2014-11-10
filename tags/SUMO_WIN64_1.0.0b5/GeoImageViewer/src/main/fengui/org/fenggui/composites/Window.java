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
 * $Id: Window.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.composites;

import java.io.IOException;
import java.util.ArrayList;

import org.fenggui.Button;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.IContainer;
import org.fenggui.IWidget;
import org.fenggui.Label;
import org.fenggui.StandardWidget;
import org.fenggui.Widget;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.event.IDragAndDropListener;
import org.fenggui.event.IWindowClosedListener;
import org.fenggui.event.WindowClosedEvent;
import org.fenggui.event.mouse.MouseExitedEvent;
import org.fenggui.layout.BorderLayout;
import org.fenggui.layout.BorderLayoutData;
import org.fenggui.layout.RowLayout;
import org.fenggui.render.Binding;
import org.fenggui.theme.XMLTheme;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * Resizable, movable container with header and buttons for closing, minimizing and maximizing. It is pretty much
 * like a window in common window environments. 
 * <code>Window</code> can only be added to the Display. 
 * <br/><br/>Minimizing and maximizing windows is currently not implemented.
 * 
 * @author Johannes Schaback
 * @author Rainer Angermann
 * @author Boris Beaulant
 *
 */
public class Window extends Container implements IWindow
{
	private ArrayList<IWindowClosedListener> windowClosedHook = new ArrayList<IWindowClosedListener>();
	
	protected IContainer content = null;
	protected Container titleBar = null;
	protected Label title = null;
	protected Button closeButton = null;
	protected Button maximizeButton = null;
	protected Button minimizeButton = null;
	private boolean isShowingResizeCursors = false;
	private final Window THIS = this;
	protected IDragAndDropListener moveDnDListener = new WindowMoveDnDListenerImpl();
	protected IDragAndDropListener resizeDnDListener = new WindowResizeDnDListenerImpl();
	
	/**
	 * Creates a window with a close button (aka Dialog).
	 */
	public Window() {
		this(true, false, false, true);
	}
	
	/**
	 * Creates a window. 
	 * @param closeBtn flag whether the window shall have a close button
	 * @param maximizeBtn flag whether the window shall have a maximize button
	 * @param minimizeBtn flag whether the window shall have a minimize button
	 */
	public Window(boolean closeBtn, 
				  boolean maximizeBtn,
				  boolean minimizeBtn)
	{
		this(closeBtn, maximizeBtn, minimizeBtn, true);
	}
	
	/**
	 * Creates a new window.
	 * @param closeBtn if the window has a close button
	 * @param maximizeBtn if the window has a maximize button
	 * @param minimizeBtn if the window jas a minimize button
	 * @param autoClose if the window will be automatically closed when the closeButton is pressed or if the close method has to be called. true per default
	 */
	public Window(
			boolean closeBtn, 
			boolean maximizeBtn,
			boolean minimizeBtn,
			boolean autoClose) {
		super();

		// Build the window structure
		build(closeBtn, maximizeBtn, minimizeBtn);
		
		setupTheme(Window.class);
		
		if (autoClose) 
		{
			addWindowClosedListener(new IWindowClosedListener() 
			{
				public void windowClosed(WindowClosedEvent windowClosedEvent) 
				{
					windowClosedEvent.getWindow().close();
				}
			});
		}
	}
	
	/**
	 * Returns the <code>Container</code> that is supposed to
	 * hold the content of the <code>Window</code>.
	 * @return the content container
	 */
	public Container getContentContainer() 
	{
		return (Container) content;
	}
	
	public IContainer getIContent()
	{
		return content;
	}
	
	/**
	 * Sets the content Container. This is desirable for example, if the
	 * content Container shall be a <code>ScrollContainer</code>.
	 * @param c the new container
	 */
	public void setContentContainer(IContainer c) 
	{
		removeWidget(content);
		if(!getContent().contains(c)) addWidget(c);
		((Widget)c).setLayoutData(BorderLayoutData.CENTER);
		if(c instanceof Container) ((Container)c).setKeyTraversalRoot(false);
		updateMinSize();
		content = c;
	}
	
	/**
	 * Build the window structure. Override this method if you want to change the inner widgets
	 * placement.
	 * Do not call this method. It is automaticaly call by the constructor.
	 * 
	 * @param closeBtn
	 * @param maximizeBtn
	 * @param minimizeBtn
	 */
	protected void build(boolean closeBtn, boolean maximizeBtn, boolean minimizeBtn) 
	{
		titleBar = new Container();
		this.addWidget(titleBar);
		
		setLayoutManager(new BorderLayout());
		
		content = new Container();
		((Container)content).setLayoutData(BorderLayoutData.CENTER);
		((Container)content).setKeyTraversalRoot(true);
		this.addWidget(content);
		
		titleBar.setLayoutData(BorderLayoutData.NORTH);
		
		buildTitleBar(closeBtn, maximizeBtn, minimizeBtn);
		
		setSize(100, 120);
	}	

	/**
	 * Constructs the title bar.
	 * @param closeBtn flag indicating the existence of a close button
	 * @param maximizeBtn flag indicating the existence of a maximize button
	 * @param minimizeBtn flag indicating the existence of a minimize button
	 */
	protected void buildTitleBar(
			boolean closeBtn, 
			boolean maximizeBtn,
			boolean minimizeBtn) 
	{
		titleBar.setLayoutManager(new RowLayout(true));
		
		title = new Label();
		titleBar.addWidget(title);
		title.setText("Frame");
		
		if(minimizeBtn) 
		{
			buildMinimizeButton();
		}
		
		if(maximizeBtn) 
		{
			buildMaximizeButton();
		}
		
		if(closeBtn) 
		{
			buildCloseButton();
		}	
	}

	/**
	 * Build the minimizeButton
	 */
	protected void buildMinimizeButton() {
		minimizeButton = FengGUI.createButton(titleBar, "_");
		minimizeButton.addButtonPressedListener(new IButtonPressedListener() {
			public void buttonPressed(ButtonPressedEvent e)
			{
				System.err.println("Minimize Window: Not implemented yet");
			}});	
		
	}
	
	/**
	 * Build the maximizeButton
	 */
	protected void buildMaximizeButton() {
		maximizeButton = FengGUI.createButton(titleBar);
		maximizeButton.addButtonPressedListener(new IButtonPressedListener() {
			public void buttonPressed(ButtonPressedEvent e)
			{
				System.err.println("Maximize Window: Not implemented yet");
			}});
	}
	
	/**
	 * Build the closeButton
	 */
	protected void buildCloseButton() {
		closeButton = FengGUI.createButton(titleBar);
		closeButton.setText("X");
		closeButton.addButtonPressedListener(new IButtonPressedListener() {
			public void buttonPressed(ButtonPressedEvent e)
			{
				fireWindowClosedEvent();
				//close();
			}});
		closeButton.setTraversable(false);
	}
	
	class WindowMoveDnDListenerImpl implements IDragAndDropListener 
	{
		int oldX = 0;
		int oldY = 0;
		
		public void select(int x, int y) 
		{
			oldX = x;
			oldY = y;
		}

		public void drag(int x, int y) 
		{
			// restrict dragging windows to the display
			// see http://www.fenggui.org/forum/index.php?topic=85.0
			if(x < 0 ||
				y < 0 || 
				x > getParent().getSize().getWidth() ||  
				y > getParent().getSize().getHeight()) return;

			move(x-oldX, y-oldY);
			oldX = x;
			oldY = y;
		}

		public void drop(int x, int y, IWidget dropOn) 
		{
		}

		public boolean isDndWidget(IWidget w, int x, int y) 
		{
			// If the over widget is title or titleBar it's ok to drag the window
			return w.equals(title) || w.equals(titleBar);
		}
	}

	class WindowResizeDnDListenerImpl implements IDragAndDropListener 
	{
		int oldX = 0;
		int oldY = 0;
		
		int type = -1;
		
		final int NORTH = 1;
		final int WEST = 2;
		final int SOUTH = 3;
		final int EAST = 4;
		final int SOUTH_EAST = 5;
		final int SOUTH_WEST = 6;
		final int NORTH_EAST = 7;
		final int NORTH_WEST = 8;
		
		public void select(int x, int y) 
		{
			oldX = x;
			oldY = y;
			
			int localX = x - getDisplayX();
			int localY = y - getDisplayY();
			
			if(onLeftBorder(localX, localY))
			{
				if(onBottomBorder(localX, localY))
					type = SOUTH_WEST;
				else if(onTopBorder(localX, localY))
					type = NORTH_WEST;
				else type = WEST;
			}
			else if(onRightBorder(localX, localY))
			{
				if(onBottomBorder(localX, localY))
					type = SOUTH_EAST;
				else if(onTopBorder(localX, localY))
					type = NORTH_EAST;
				else type = EAST;
			}
			else if(onBottomBorder(localX, localY))	type = SOUTH;
			else if(onTopBorder(localX, localY)) type = NORTH;
			
		}

		public void drag(int x, int y) 
		{
			boolean flagX = true;
			boolean flagY = true;
			
			switch (type)
			{
			case EAST:
				flagX = setCheckedWidth(getWidth() + x-oldX);
				flagY = setCheckedHeight(getHeight());
				break;
			case NORTH:
				flagY = setCheckedHeight(getHeight() + y - oldY);
				break;
			case SOUTH:
				flagY = setCheckedHeight(getHeight() - y + oldY);
				if(flagY) move(0, y - oldY);
				break;	
			case NORTH_EAST:
				flagX = setCheckedWidth(getWidth() + x - oldX);
				flagY = setCheckedHeight(getHeight() + y - oldY);
				break;	
			case NORTH_WEST:
				flagX = setCheckedWidth(getWidth() - x + oldX);
				if(flagX) move(x - oldX, 0);
				flagY = setCheckedHeight(getHeight() + y - oldY);
				break;		
			case SOUTH_WEST:
				flagX = setCheckedWidth(getWidth() - x + oldX);
				flagY = setCheckedHeight(getHeight() - y + oldY);
				if(flagX) move(x - oldX, 0);
				if(flagY) move(0, y - oldY);
				break;					
			case SOUTH_EAST:
				flagX = setCheckedWidth(getWidth() + x - oldX);
				flagY = setCheckedHeight(getHeight() - y + oldY);
				if(flagY) move(0, y - oldY);
				break;					
			case WEST:
				flagX = setCheckedWidth(getWidth() - x + oldX);
				if(flagX) move(x - oldX, 0);
				break;	
			default:
				break;
			}

			layout();

			if(flagX) oldX = x;
			if(flagY) oldY = y;
		}

		public void drop(int x, int y, IWidget dropOn) 
		{
			type = -1;
		}

		public boolean isDndWidget(IWidget w, int displayX, int displayY) 
		{
			if(w != THIS) return false;
			
			return isShowingResizeCursors;
		}
	}
	
	private boolean setCheckedWidth(int width)
	{
		if(width >= getMinWidth())
		{
			setWidth(width);
			return true;
		}
		else
		{
			setWidth(getMinWidth());
			return false;
		}
	}
	
	/**
	 * Make this window resizable with the mouse or not.
	 * @param b flag whether the window shall be resizable or not
	 */
	public void setResizable(boolean b)
	{
		if(!b)
		{
			if(resizeDnDListener != null)
			{
				Display d = getDisplay();
				if(d == null)
					throw new IllegalStateException("Uh, sorry, the window has to be in the widget tree if you want to disable resizing :)");
				d.removeDndListener(resizeDnDListener);
				resizeDnDListener = null;
			}
		}
		else
		{
			if(resizeDnDListener == null)
			{
				Display d = getDisplay();
				if(d == null)
					throw new IllegalStateException("Uh, sorry, the window has to be in the widget tree if you want to enable resizing :)");
				d.removeDndListener(resizeDnDListener); // first remove old instance (in case of two subsequent setResizable(true) calls)
				resizeDnDListener = new WindowResizeDnDListenerImpl();
				d.addDndListener(resizeDnDListener);
			}
		}
	}
	
	public boolean isResizable()
	{
		return resizeDnDListener != null;
	}
	
	/**
	 * Make this window draggable or not 
	 * @param b whether the window should be movable with the mouse or not
	 */
	public void setMovable(boolean b)
	{
		if(!b)
		{
			if(moveDnDListener != null)
			{
				Display d = getDisplay();
				if(d == null)
					throw new IllegalStateException("Uh, sorry, the window has to be in the widget tree");
				d.removeDndListener(moveDnDListener);
			}
		}
		else
		{
			if(moveDnDListener == null)
			{
				Display d = getDisplay();
				if(d == null)
					throw new IllegalStateException("Uh, sorry, the window has to be in the widget tree");
				moveDnDListener = new WindowMoveDnDListenerImpl();
				d.addDndListener(moveDnDListener);
			}
		}
	}
	
	private boolean setCheckedHeight(int height)
	{
		if(height >= getMinHeight())
		{
			setHeight(height);
			return true;
		}
		else
		{
			setHeight(getMinHeight());
			return false;
		}
	}
	
	@Override
	public void mouseMoved(int displayX, int displayY)
	{
		if(!isResizable()) return;
		int localX = displayX - getDisplayX();
		int localY = displayY - getDisplayY();
		
		if(onLeftBorder(localX, localY))
		{
			if(onBottomBorder(localX, localY))
			{
				Binding.getInstance().getCursorFactory().getSWResizeCursor().show();
			}
			else if(onTopBorder(localX, localY))
			{
				Binding.getInstance().getCursorFactory().getNWResizeCursor().show();
			}
			else
			{
				Binding.getInstance().getCursorFactory().getHorizontalResizeCursor().show();
			}
			
			isShowingResizeCursors = true;
		}
		else if(onRightBorder(localX, localY))
		{
			if(onBottomBorder(localX, localY))
			{
				Binding.getInstance().getCursorFactory().getNWResizeCursor().show();
			}
			else if(onTopBorder(localX, localY))
			{
				Binding.getInstance().getCursorFactory().getSWResizeCursor().show();
			}
			else
			{
				Binding.getInstance().getCursorFactory().getHorizontalResizeCursor().show();
			}
			
			isShowingResizeCursors = true;
		}
		else if(onBottomBorder(localX, localY))
		{
			Binding.getInstance().getCursorFactory().getVerticalResizeCursor().show();
			isShowingResizeCursors = true;
		}
		else if(onTopBorder(localX, localY))
		{
			Binding.getInstance().getCursorFactory().getVerticalResizeCursor().show();
			isShowingResizeCursors = true;
		}
	}
	
	

	@Override
	public void mouseExited(MouseExitedEvent mouseExitedEvent)
	{
		if(isShowingResizeCursors) 
			Binding.getInstance().getCursorFactory().getDefaultCursor().show();
	}
	
	private boolean onLeftBorder(int localX, int localY)
	{
		localX += getAppearance().getLeftMargins();
		
		if(localX >= 0 && localX < getAppearance().getLeftMargins()) return true;

		return false;
	}
	
	private boolean onBottomBorder(int localX, int localY)
	{
		localY += getAppearance().getBottomMargins();
		
		if(localY >= 0 && localY < getAppearance().getBottomMargins()) return true;

		return false;
	}
	
	private boolean onRightBorder(int localX, int localY)
	{
		if(localX >= getAppearance().getContentWidth()) return true;

		return false;
	}
	
	private boolean onTopBorder(int localX, int localY)
	{
		if(localY >= getAppearance().getContentHeight()) return true;

		return false;
	}
	
	/**
	 * Sets the title of the <code>Window</code>.
	 * @param t the title
	 * @return returns this
	 */
	public Window setTitle(String t) 
	{
		title.setText(t);
		return this;
	}

	/**
	 * Returns the title of the <code>Window</code>
	 * @return the title
	 */
	public String getTitle() 
	{
		return title.getText();
	}

	/**
	 * Resturns the labels that makes the title.
	 * @return the label
	 */
	public Label getTitleLabel() 
	{
		return title;
	}

	/**
	 * Returns the close button.
	 * @return close button
	 */
	public Button getCloseButton() 
	{
		return closeButton;
	}
	
	/**
	 * Closes this Window
	 */ 
	public void close() 
	{
		//Display.getInstance().removeDndListener(dndListener);
		((Container)getParent()).removeWidget(this);
		//fireWindowClosedEvent();
	}
	
	/**
	 * Returns the minimize button
	 * @return minimize button
	 */
	public Button getMinimizeButton() 
	{
		return minimizeButton;
	}

	/**
	 * Returns the maximize button
	 * @return maximize button
	 */
	public Button getMaximizeButton() 
	{
		return maximizeButton;
	}

	/**
	 * Returns the entire title bar.
	 * @return title bar
	 */
	public Container getTitleBar() 
	{
		return titleBar;
	}


	/**
	 * Overridden to register necessary listeners on current display.
	 */
	@Override
	public void addedToWidgetTree() 
	{
		super.addedToWidgetTree();
		getDisplay().addDndListener(moveDnDListener);
		if(resizeDnDListener != null)
			getDisplay().addDndListener(resizeDnDListener);
	}

	/**
	 * Notifies this window that it has been removed from the widget tree. It will
	 * unregister its drag and drop listeners from the display.
	 */
	@Override
	public void removedFromWidgetTree() 
	{
		super.removedFromWidgetTree();
		getDisplay().removeDndListener(moveDnDListener);
		getDisplay().removeDndListener(resizeDnDListener);
	}
	
	/**
	 * Add a {@link IWindowClosedListener} to the widget. The listener can be added only once.
	 * @param l Listener
	 */
	public void addWindowClosedListener(IWindowClosedListener l)
	{
		if (!windowClosedHook.contains(l))
		{
			windowClosedHook.add(l);
		}
	}
	
	/**
	 * Add the {@link IWindowClosedListener} from the widget
	 * @param l Listener
	 */
	public void removeWindowClosedListener(IWindowClosedListener l)
	{
		windowClosedHook.remove(l);
	}
	
	/**
	 * Fire a {@link WindowClosedEvent} 
	 */
	private void fireWindowClosedEvent()
	{
		WindowClosedEvent e = new WindowClosedEvent(this);
		
		for(IWindowClosedListener l: windowClosedHook)
		{
			l.windowClosed(e);
		}
	}

	/**
	 * Reads in appearance related parameters from streams (e.g. XML).
	 */
	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		setExpandable(stream.processAttribute("expandable", isExpandable(), true));
		setShrinkable(stream.processAttribute("shrinkable", isShrinkable(), true));
		setWidth(stream.processAttribute("width", getWidth(), 10));
		setHeight(stream.processAttribute("height", getHeight(), 10));
		setMinSize(stream.processAttribute("minWidth", getMinWidth(), 50),stream.processAttribute("minHeight", getMinHeight(), 50));
		setX(stream.processAttribute("x", getX(), 10));
		setY(stream.processAttribute("y", getY(), 10));
		
		setTitle(stream.processAttribute("title", getTitle(), "No Title"));
		
		stream.processInherentChild("TitleLabel", this.title);
		
		stream.processInherentChild("TitleBar", this.titleBar);

		
		if(closeButton != null)
			stream.processInherentChild("CloseButton", this.closeButton);
		
		if(minimizeButton != null)
			stream.processInherentChild("MinimizeButton", this.minimizeButton);
		
		if(maximizeButton != null)
			stream.processInherentChild("MaximizeButton", this.maximizeButton);
		
		if(stream.startSubcontext("content"))
		{
			//System.out.println(content.getClass().getCanonicalName());
			content = (IContainer) stream.processChild((StandardWidget)content, XMLTheme.TYPE_REGISTRY);
			content.setParent(this);
			stream.endSubcontext();
		}
		
		if(stream.startSubcontext("Appearance"))
		{
			getAppearance().process(stream);
			stream.endSubcontext();
		}
	}
	
	
}
