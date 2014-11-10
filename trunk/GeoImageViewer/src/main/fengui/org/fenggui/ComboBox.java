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
 * $Id: ComboBox.java 350 2007-08-29 08:22:24Z charlierby $
 */
package org.fenggui;

import java.io.IOException;

import org.fenggui.event.ISelectionChangedListener;
import org.fenggui.event.SelectionChangedEvent;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * A list that comes in a popup menu. 
 * 
 * @author Johannes, last edited by $Author: charlierby $, $Date: 2007-08-29 10:22:24 +0200 (Mi, 29 Aug 2007) $
 * @version $Revision: 350 $
 */
public class ComboBox<E> extends StandardWidget implements IBasicContainer
{
	private Pixmap pixmap = null;
	private Label label;
	private ComboBoxAppearance appearance = null;
	private List<E> list = null;
	private ScrollContainer popupContainer = null;
	/**
	 * Sets the pixmap drawn at the right side the combo box.
	 * @param pixmap the pixmap
	 */
	public void setPixmap(Pixmap pixmap) 
	{
		this.pixmap = pixmap;
		updateMinSize();
	}
	
	public ScrollContainer getPopupContainer()
	{
		return popupContainer;
	}

	public ComboBoxAppearance getAppearance()
	{
		return appearance;
	}

	/**
	 * Returns the pixmap.
	 * @return pixmap
	 */
	public Pixmap getPixmap() {
		return pixmap;
	}

	/**
	 * Returns the label used to display the current selection.
	 * @return label
	 */
	public Label getLabel()
	{
		return label;
	}

	/**
	 * Returns the popup list that appears when the user clicks on the combo box.
	 * @return popup list
	 */
	public List<E> getList() 
	{
		return list;
	}

	/**
	 * Creates a new <code>ComboBox</code> object.
	 * 
	 */
	public ComboBox() 
	{
		appearance = new ComboBoxAppearance(this);
		
		label = new Label();
		label.setParent(this);
		label.setText("Space holder....");
		
		popupContainer = new ScrollContainer();
		
		list = new List<E>(ToggableGroup.SINGLE_SELECTION);
		popupContainer.setInnerWidget(list);
		
		addSelectionChangedListener(getPopupHandler());
		appearance = new ComboBoxAppearance(this);
		setupTheme(ComboBox.class);
        updateMinSize();
	}
	
	private ISelectionChangedListener getPopupHandler()
	{
		return new ISelectionChangedListener(){

			public void selectionChanged(SelectionChangedEvent e) {
				
				// dont listen to de-select events!
				if(!e.isSelected()) return;
				
				if (getDisplay() != null)
				{
					getDisplay().removePopup();
				}
				
				getLabel().setText(list.getToggableWidgetGroup().getSelectedItem().getText());
			}
		};
	}
	
	public void addSelectionChangedListener(ISelectionChangedListener l)
	{
		list.getToggableWidgetGroup().addSelectionChangedListener(l);
	}
	
	public void removeSelectionChangedListener(ISelectionChangedListener l)
	{
		list.getToggableWidgetGroup().removeSelectionChangedListener(l);
	}
	
	@Override
	public void layout() 
	{
		int height = getAppearance().getContentHeight();
		int width  = getAppearance().getContentWidth();
		
		int pixmapWidth = 0;
		
		if(pixmap != null) pixmapWidth = pixmap.getWidth();
		
		label.setSize(width - pixmapWidth, height);
		
		label.setXY(0, 0);
		
	}

	@Override
	public void updateMinSize() 
	{
		setMinSize(getAppearance().getMinSizeHint());
		
		if(getParent() != null) getParent().updateMinSize();
	}

	
	/**
	 * Adds a new item to the popup list
	 * @param item new item
	 */
	public void addItem(ListItem<E> item) 
	{
		if(list.isEmpty()) {
			list.addItem(item);
			// Set the new first item as 'SelectedItem'
			list.setSelectedIndex(0, true);
		} else {
			list.addItem(item);
		}
		
	}
	
	/**
	 * Manually selects the item specified by the given string.
	 * @param s the string
	 */
	public void setSelected(String s)
	{
		for(ListItem<E> item: list.getItems())
		{
			if(s.equals(item.getText()))
			{
				setSelected(item);
				break;
			}
		}
	}
	
	/**
	 * Manually selects the given item.
	 * @param item the item
	 */
	public void setSelected(IToggable<E> item)
	{
		if(!list.getItems().contains(item)) return;
		
		//label.setText(item.getText());
		list.getToggableWidgetGroup().setSelected(item, true);
	}
	
	/**
	 * Manually selects the given item index
	 * @param index The index of the item to select
	 * @param selected <code>true</code> to select, <code>false</code> else.
	 */
	public void setSelectedIndex(int index, boolean selected)
	{
		list.setSelectedIndex(index, selected);
	}
	
	/**
	 * Adds a new item to the popup list.
	 * @param s text of item
	 */
	public void addItem(String s) 
	{
		addItem(new ListItem<E>(s));
	}
	
	/**
	 * Opens the popup menu
	 */
	private void openPopup() 
	{
		list.updateMinSize();
		list.setSizeToMinSize();
		
		final int displayY = getDisplayY();
		final int displayX = getDisplayX();
		
		final int horMargins = popupContainer.getAppearance().getLeftMargins() + 
			popupContainer.getAppearance().getRightMargins();

		final int verMargins = popupContainer.getAppearance().getTopMargins() + 
			popupContainer.getAppearance().getBottomMargins();

		if(displayY - list.getHeight() < 0)
			popupContainer.setHeight(displayY + verMargins);
		else
			popupContainer.setHeight(list.getHeight() + verMargins);

		popupContainer.setWidth(Math.max(list.getWidth(), getWidth()) + horMargins);
		
		popupContainer.layout();
		
		popupContainer.setX(displayX);
		popupContainer.setY(displayY - popupContainer.getHeight());
		
		// the click is processed in org.fenggui.Display afterwards and
		// will place the Frame with the Combo Box inside to the
		// first position in the content list. We have to wait to pass
		// this event and then display the popup.
		
		Thread t = new Thread() {
			public void run() {
				try {sleep(50);} catch (InterruptedException e) {}
				getDisplay().displayPopUp(popupContainer);
			}
		};
			
		t.start();
	}

	@Override
	public void mousePressed(MousePressedEvent mousePressedEvent) 
	{
		if (list == null || !list.isInWidgetTree()) {
			openPopup();
		}
	}

	public IWidget getNextTraversableWidget(IWidget start) 
	{
		return getParent().getNextTraversableWidget(this);
	}

	public IWidget getPreviousTraversableWidget(IWidget start) 
	{
		return getParent().getPreviousTraversableWidget(this);
	}	
	
	public IWidget getNextWidget(IWidget start)
	{
		return getParent().getNextWidget(this);
	}

	public IWidget getPreviousWidget(IWidget start)
	{
		return getParent().getPreviousWidget(this);
	}



	@SuppressWarnings("unchecked")
	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		stream.processInherentChild("List", list);
		stream.processInherentChild("Label", label);
		stream.processInherentChild("PopupContainer", popupContainer);
		
		addSelectionChangedListener(getPopupHandler());
		
		setPixmap(stream.processChild("Pixmap", getPixmap(), null, Pixmap.class));
	}
	
	/**
	 * @return Returns the currently selected value of the ComboBox
	 */
	public String getSelectedValue() {
		if(label != null) {
			return label.getText();
		}
		
		return null;
	}
}
