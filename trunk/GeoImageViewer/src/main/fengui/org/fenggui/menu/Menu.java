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
 * $Id: Menu.java 357 2007-09-20 16:09:10Z marcmenghin $
 */
package org.fenggui.menu;

import java.io.IOException;
import java.util.ArrayList;

import org.fenggui.Display;
import org.fenggui.IWidget;
import org.fenggui.ObservableWidget;
import org.fenggui.event.IKeyPressedListener;
import org.fenggui.event.IMenuClosedListener;
import org.fenggui.event.Key;
import org.fenggui.event.KeyPressedEvent;
import org.fenggui.event.MenuClosedEvent;
import org.fenggui.event.mouse.IMouseDraggedListener;
import org.fenggui.event.mouse.IMouseMovedListener;
import org.fenggui.event.mouse.IMousePressedListener;
import org.fenggui.event.mouse.IMouseReleasedListener;
import org.fenggui.event.mouse.MouseDraggedEvent;
import org.fenggui.event.mouse.MouseMovedEvent;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.event.mouse.MouseReleasedEvent;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * Menu widget. A menu is a popup container.
 * 
 * @author Johannes Schaback, last edited by $Author: marcmenghin $, $Date: 2007-09-20 18:09:10 +0200 (Do, 20 Sep 2007) $
 * @author Florian Köberle
 * @version $Revision: 357 $
 */
public class Menu extends ObservableWidget implements IMenuChainElement
{
	private ArrayList<IMenuClosedListener> menuClosedHook = new ArrayList<IMenuClosedListener>();

	private MenuAppearance appearance = null;

	/**
	 * Item container.
	 */
	private ArrayList<MenuItem> items = new ArrayList<MenuItem>();

	/**
	 * The currently opened submenu. Opened from within this menu.
	 */
	private IMenuChainElement nextMenu = null;

	/**
	 * Previous opened menu (or menu bar) from which this menu has been opened.
	 */
	private IMenuChainElement previousMenu = null;

	/**
	 * menu item index on which the mouse cursor hovers.
	 */
	private int mouseOverRow = -1;

	private boolean isDragging = false;

	/**
	 * Constructs a new menu.
	 *
	 */
	public Menu()
	{
		appearance = new MenuAppearance(this);
		setupTheme(Menu.class);
		updateMinSize();
		buildBehavior();
	}
	
	/**
	 * Loads the Menu from the stream
	 */
	public Menu(InputOnlyStream stream) throws IOException, IXMLStreamableException
	{
		appearance = new MenuAppearance(this);
		process(stream);
		updateMinSize();
		buildBehavior();
	}

	private void buildBehavior()
	{
		final IWidget thizz = this;
		
		// translated the drag event into a mouse moved event
		// so that the mouseOverIndex gets updated accordingly
		addMouseDraggedListener(new IMouseDraggedListener(){

			public void mouseDragged(MouseDraggedEvent mp)
			{
				mouseMoved(mp.getDisplayX(), mp.getDisplayY());
				isDragging = true;
			}});
		
		// set the mouseOverRow index according to the mouse position
		addMouseMovedListener(new IMouseMovedListener(){

			public void mouseMoved(MouseMovedEvent mouseMovedEvent)
			{
				isDragging = false;
				
				final int mouseY = mouseMovedEvent.getDisplayY() - getDisplayY();

				final int row = computeRow(mouseY);
				if (row != -1)
				{
					setMouseOverRow(row);
					getDisplay().setFocusedWidget(Menu.this);
				}
			}});
		
		// keypressed listener to make items navigable
		addKeyPressedListener(new IKeyPressedListener(){

			public void keyPressed(KeyPressedEvent kpe)
			{
				if(kpe.getKeyClass().equals(Key.DOWN))
				{
					setMouseOverRow((mouseOverRow + 1) % items.size());
				} 
				else if(kpe.getKeyClass().equals(Key.UP))
				{
					mouseOverRow--;
					if(mouseOverRow <= -1) mouseOverRow = items.size()-1;
					setMouseOverRow(mouseOverRow);
				}
				else if(kpe.getKeyClass().equals(Key.ENTER))
				{
					if(mouseOverRow >= 0 && mouseOverRow < items.size())
						selectItem(mouseOverRow);
				}
				else if(kpe.getKeyClass().equals(Key.LEFT))
				{
					getDisplay().setFocusedWidget((IWidget) getPreviousMenu());
					if(getPreviousMenu() instanceof MenuBar)
					{
						getPreviousMenu().keyPressed(kpe);
					}
					else
					{
						setMouseOverRow(-1);
					}
				}
				else if(kpe.getKeyClass().equals(Key.RIGHT))
				{
					if(mouseOverRow > 0 && items.get(mouseOverRow).getMenu() != null)
					{
						Menu m = items.get(mouseOverRow).getMenu();
						getDisplay().setFocusedWidget(m);
						m.setMouseOverRow(0);
					}
					else
					{
						IMenuChainElement m = getPreviousMenu();
						
						while(m.getPreviousMenu() != null)
							m = m.getPreviousMenu();
						
						if(m instanceof MenuBar)
						{
							getDisplay().setFocusedWidget((IWidget) m);
							getDisplay().getFocusedWidget().keyPressed(kpe);
						}
						
					}
				}
			}});
		
		// select item on mouse pressed
		addMousePressedListener(new IMousePressedListener(){

			public void mousePressed(MousePressedEvent mp)
			{
				int row = computeRow(mp.getLocalY(thizz));

				selectItem(row);				
			}});
		
		// when draggin, pretend that the mouse was pressed
		// to select the currently selected item
		addMouseReleasedListener(new IMouseReleasedListener(){

			public void mouseReleased(MouseReleasedEvent mr)
			{
				if (isDragging)
				{
					mousePressed(
						new MousePressedEvent(thizz, mr.getDisplayX(), mr.getDisplayY(), mr.getButton(), mr.getClickCount()));
					isDragging = false;
				}				
			}});
		
	}

	public MenuItem getItem(int index)
	{
		return items.get(index);
	}
	
	public int getItemCount()
	{
		return items.size();
	}

	public MenuItem getMenuItem(int index)
	{
		return items.get(index);
	}

	public Iterable<MenuItem> getItems()
	{
		return items;
	}

	private void displayAsPopup(Menu prev)
	{
		previousMenu = prev;
		setSizeToMinSize();

		Display display = prev.getDisplay();

		display.displayPopUp(this);
		display.layout();
	}

	/**
	 * Closes this menu and all open submenus.
	 */
	public void closeForward()
	{

		// close all sub menus first recursively
		if (nextMenu != null)
		{
			if (nextMenu.equals(this))
			{
				System.out.println(this + " " + items.get(0).getText());
			}

			nextMenu.closeForward();
			nextMenu = null;
		}
		previousMenu = null;

		if (getDisplay() != null) getDisplay().removeWidget(this);
		mouseOverRow = -1;
	}

	/**
	 * Adds a new menu item that opens the given submenu.
	 * @param submenu the submenu
	 * @param name the text used for the menu item
	 */
	public void registerSubMenu(final Menu submenu, String name)
	{
		if (submenu.equals(this)) throw new IllegalArgumentException("submenu.equals(this): circular reference!");

		MenuItem item = new MenuItem(name);
		item.menu = submenu;
		addItem(item);
		updateMinSize();
	}

	/**
	 * Sets the previous menu of the menu chain.
	 * @param previousMenu the previous menu
	 */
	protected void setPreviousMenu(IMenuChainElement previousMenu)
	{
		this.previousMenu = previousMenu;
	}

	public void closeBackward()
	{
		if (getDisplay() != null) getDisplay().removeWidget(this);

		if (previousMenu != null)
		{
			previousMenu.closeBackward();
			previousMenu = null;
		}
	}
	
	private int computeRow(int localY)
	{
		// y = distance from top of first menu item:
		final int y = (getHeight() - localY) - appearance.getTopMargins();
		//TODO there is some space, from which I don't know where it come from.

		if (y < 0)
		{
			return -1;
		}
		int row = y / appearance.getCellHeight();
		if (row < items.size())
		{
			return row;
		}
		else
		{
			return -1;
		}

	}


	/**
	 * Select the given item by row.
	 * @param row row of item
	 **/
	protected void selectItem(int row)
	{
		MenuItem item = items.get(row);

		if (!item.isEnabled()) return;

		item.fireMenuItemPressedEvent();

		closeBackward();
		closeForward();

		if (getDisplay() != null) getDisplay().removeWidget(this);
	}

	public void addItem(MenuItem item)
	{
		items.add(item);
		updateMinSize();
	}

	/**
	 * Set the row over which the mouse is hovering.
	 * @param row
	 **/
	protected void setMouseOverRow(int row)
	{
		if (row >= items.size())
		{
			throw new IllegalArgumentException("row does not exits");
		}
		if (row < -1)
		{
			throw new IllegalArgumentException("smaller values then 1 are not allowed");
		}
		mouseOverRow = row;
		if (row >= 0)
		{
			MenuItem item = items.get(mouseOverRow);
			Menu menu = item.menu;
	
			if (nextMenu != null) nextMenu.closeForward();
			nextMenu = menu;
			if (menu != null)
			{
				menu.setSizeToMinSize();
				menu.setX(getWidth() + this.getX());
				menu.setY(this.getY() + (items.size() - mouseOverRow) * appearance.getCellHeight() - menu.getHeight());
				menu.displayAsPopup(this);
			}
		}
		else
		{
			mouseOverRow = row;
			if (nextMenu != null) nextMenu.closeForward();
			nextMenu = null;
		}
	}

	/**
	 * @return row of menu over which mouse hovers
	 **/
	public int getMouseOverRow()
	{
		return mouseOverRow;
	}

	@Override
	public void removedFromWidgetTree()
	{
		super.removedFromWidgetTree();
		fireMenuClosedEvent();
		closeBackward();
	}

	public Iterable<MenuItem> getMenuItems()
	{
		return items;
	}


	@SuppressWarnings("unchecked")
	@Override
	public void updateMinSize()
	{
		setMinSize(appearance.getMinSizeHint());
	}

	/**
	 * @return next menu in chain
	 **/
	public IMenuChainElement getNextMenu()
	{
		return nextMenu;
	}

	/**
	 * @return previous menu in chain
	 **/
	public IMenuChainElement getPreviousMenu()
	{
		return previousMenu;
	}

	public MenuAppearance getAppearance()
	{
		return appearance;
	}

	/**
	 * Add a {@link IMenuClosedListener} to the widget. The listener can be added only once.
	 * @param l Listener
	 */
	public void addMenuClosedListener(IMenuClosedListener l)
	{
		if (!menuClosedHook.contains(l))
		{
			menuClosedHook.add(l);
		}
	}

	/**
	 * Add the {@link IMenuClosedListener} from the widget
	 * @param l Listener
	 */
	public void removeMenuClosedListener(IMenuClosedListener l)
	{
		menuClosedHook.remove(l);
	}

	/**
	 * Fire a {@link MenuClosedEvent} 
	 */
	private void fireMenuClosedEvent()
	{
		MenuClosedEvent e = new MenuClosedEvent(this);

		for (IMenuClosedListener l : menuClosedHook)
		{
			l.menuClosed(e);
		}
	}
	
	@Override
	public void process(InputOutputStream stream) 
	throws IOException, IXMLStreamableException {
		super.process(stream);
		
		stream.processChildren("Item", items, MenuItem.class);
		
		// TODO More to save?
		
		if (stream.isInputStream()) {
			updateMinSize();
			mouseOverRow = -1;
			isDragging = false;
			nextMenu = null;
			previousMenu = null;
		}
	}
}
