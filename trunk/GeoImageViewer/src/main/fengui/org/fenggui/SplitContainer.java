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
 * Created on Jan 31, 2006
 * $Id: SplitContainer.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.io.IOException;

import org.fenggui.event.FocusEvent;
import org.fenggui.event.IDragAndDropListener;
import org.fenggui.event.mouse.MouseEnteredEvent;
import org.fenggui.event.mouse.MouseExitedEvent;
import org.fenggui.render.Binding;
import org.fenggui.render.Pixmap;
import org.fenggui.theme.XMLTheme;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Dimension;

/**
 * Container that separates two elements either vertically or horizontically. The size
 * of the elements can be adjusted by moving the separator. 
 * 
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public class SplitContainer extends StandardWidget implements IContainer
{
	private Pixmap pixmap = null;
	
	private boolean horizontal = true;
	private IWidget firstWidget = null;
	private IWidget secondWidget = null;
	private int barSize = 10;

	private SplitContainerDndListener dndListener = null;
	private SplitContainerAppearance appearance = null;
	
	/**
	 * Indicates the left/lower edge of the slider
	 */
	private int value = -1;
	
	/**
	 * Creates a new SplitContainer object.
	 * @param horizontal true if split horizontally (i.e. slider is movable up and downwards), false otherwise
	 */
	public SplitContainer(boolean horizontal) 
	{
		this.horizontal = horizontal;
		dndListener = new SplitContainerDndListener(this);
		
		appearance = new SplitContainerAppearance(this);
		setupTheme(SplitContainer.class);
		updateMinSize();
	}
	
	public SplitContainer() 
	{
		this(true);
	}
	
	

	public SplitContainerAppearance getAppearance()
	{
		return appearance;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getBarSize() {
		return barSize;
	}

	public void setBarSize(int barSize) 
	{
		this.barSize = barSize;
	}

	public IWidget getFirstWidget() {
		return firstWidget;
	}

	public void setFirstWidget(Widget firstWidget) 
	{
		this.firstWidget = firstWidget;
		
		if(this.firstWidget != null)
		{
			this.firstWidget.removedFromWidgetTree();
			this.firstWidget.setParent(null);
			if(getDisplay() != null) getDisplay().focusedWidgetValityCheck();			
		}
		
		if(firstWidget != null)
		{
			firstWidget.setParent(this);
			firstWidget.addedToWidgetTree();
		}
			
		updateMinSize();
	}

	public IWidget getSecondWidget() {
		return secondWidget;
	}

	public void setSecondWidget(Widget secondWidget) 
	{
		this.secondWidget = secondWidget;
		
		if(this.secondWidget != null)
		{
			this.secondWidget.removedFromWidgetTree();
			this.secondWidget.setParent(null);
			if(getDisplay() != null) getDisplay().focusedWidgetValityCheck();			
		}		
		
		if(secondWidget != null)
		{
			secondWidget.setParent(this);
			secondWidget.addedToWidgetTree();
		}
		
		updateMinSize();
	}

	
	@Override
	public void addedToWidgetTree() 
	{
		getDisplay().addDndListener(dndListener);
		
		if(firstWidget != null) firstWidget.addedToWidgetTree();
		if(secondWidget != null) secondWidget.addedToWidgetTree();
	}

	@Override
	public void removedFromWidgetTree() 
	{
		Display d = getDisplay();
		if(d != null)
			d.removeDndListener(dndListener);
		
		if(firstWidget != null) firstWidget.addedToWidgetTree();
		if(secondWidget != null) secondWidget.addedToWidgetTree();
	}

	public boolean isHorizontal() {
		return horizontal;
	}
	
	private int keepSliderInRange(int newValue)
	{
		int contentHeight = getAppearance().getContentHeight();
		int contentWidth  = getAppearance().getContentWidth();
		
		if(horizontal)
		{
			int firstMinHeight = firstWidget != null ? firstWidget.getMinSize().getHeight() : 0;
			int secondMinHeight = secondWidget != null ? secondWidget.getMinSize().getHeight() : 0;
			
			if(newValue < firstMinHeight) 
				return firstMinHeight;
			
			if(newValue > (contentHeight - secondMinHeight - barSize)) 
				return contentHeight - secondMinHeight - barSize;
		}
		else
		{
			int firstMinWidth = firstWidget != null ? firstWidget.getMinSize().getWidth() : 0;
			int secondMinWidth = secondWidget != null ? secondWidget.getMinSize().getWidth() : 0;
			
			if(newValue < firstMinWidth) 
				return firstMinWidth;
			
			if(newValue > (contentWidth - secondMinWidth - barSize)) 
				return contentWidth - secondMinWidth - barSize;
			
		}
		
		return newValue;
	}
	
	class SplitContainerDndListener implements IDragAndDropListener 
	{

		int oldValue = -1;
		
		private SplitContainer thizz = null;
		
		public SplitContainerDndListener(SplitContainer mom)
		{
			thizz = mom;
		}
		
		public boolean isDndWidget(IWidget w, int x, int y) 
		{
			return w.equals(thizz);
		}

		public void select(int displayX, int displayY) 
		{
			if(horizontal)
				oldValue = displayY;
			else
				oldValue = displayX;
		}

		public void drag(int displayX, int displayY) 
		{
			if(horizontal) 
			{
				value += (displayY - oldValue);
				oldValue = displayY;
			}
			else
			{
				value += (displayX - oldValue);
				oldValue = displayX;
			}	 
			
			value = keepSliderInRange(value);
			
			layout();
		}

		public void drop(int x, int y, IWidget droppedOn) 
		{
			
		}
		
	}
	
	
	@Override
	public IWidget getWidget(int x, int y) 
	{
        if(!getAppearance().insideMargin(x, y)) 
        {
            return null;
        }
        
        x -= getAppearance().getLeftMargins();
        y -= getAppearance().getBottomMargins();
        
        if(firstWidget != null && firstWidget.getSize().contains(x - firstWidget.getX(), y - firstWidget.getY())) 
        	return firstWidget.getWidget(x - firstWidget.getX(), y - firstWidget.getY());
        
        if(secondWidget != null && secondWidget.getSize().contains(x - secondWidget.getX(), y - secondWidget.getY())) 
        	return secondWidget.getWidget(x - secondWidget.getX(), y - secondWidget.getY());
        
        return this;
	}

	public Pixmap getPixmap() {
		return pixmap;
	}

	public void setPixmap(Pixmap pixmap) 
	{
		this.pixmap = pixmap;
		
		updateMinSize();
	}

	@Override
	public void layout() 
	{
		int contentHeight = getAppearance().getContentHeight();
		int contentWidth  = getAppearance().getContentWidth();
		
		if(horizontal)
		{
			if(value < 0) value = (contentHeight - barSize) / 2;
			
			if(firstWidget != null)
			{
				firstWidget.setX(0);
				firstWidget.setY(0);
				firstWidget.setSize(new Dimension(contentWidth, value));
			}
			
			if(secondWidget != null)
			{
				secondWidget.setX(0);
				secondWidget.setY(value + barSize);
				secondWidget.setSize(new Dimension(contentWidth, contentHeight - value - barSize));
			}
		}
		else // vertical slider
		{
			if(value < 0) value = (contentWidth - barSize) / 2;
			
			if(firstWidget != null)
			{
				firstWidget.setX(0);
				firstWidget.setY(0);
				firstWidget.setSize(new Dimension(value, contentHeight));
			}
			
			if(secondWidget != null)
			{
				secondWidget.setX(value + barSize);
				secondWidget.setY(0);
				secondWidget.setSize(new Dimension(contentWidth - value - barSize, contentHeight));
			}
		}
		
		if(firstWidget != null) firstWidget.layout();
		if(secondWidget != null) secondWidget.layout();
	}
	
	@Override
	public void mouseEntered(MouseEnteredEvent mouseEnteredEvent) 
	{
		if(horizontal)
			Binding.getInstance().getCursorFactory().getVerticalResizeCursor().show();
		else
			Binding.getInstance().getCursorFactory().getHorizontalResizeCursor().show();
	}

	@Override
	public void mouseExited(MouseExitedEvent mouseExitedEvent) 
	{
		Binding.getInstance().getCursorFactory().getDefaultCursor().show();
	}



	public void addWidget(IWidget w)
	{
		if(firstWidget == null) 
		{
			firstWidget = w;
			return;
		}
		
		secondWidget = w;
	}
	
	public void addWidget(IWidget w, int position) 
	{
		if(position <= 0) {
			firstWidget = w;
			return;
		}
		if (position >= 1) {
			secondWidget = w;
			return;
		}
	}

	public IWidget getNextTraversableWidget(IWidget start) 
	{
		if(start.equals(firstWidget) && secondWidget.isTraversable())
			return secondWidget;
		else return getParent().getNextTraversableWidget(this);
	}

	public IWidget getPreviousTraversableWidget(IWidget start) 
	{
		if(start.equals(secondWidget) && firstWidget.isTraversable())
			return firstWidget;
		else return getParent().getPreviousTraversableWidget(this);
	}	
	
	public IWidget getNextWidget(IWidget start)
	{
		if(start.equals(firstWidget))
			return secondWidget;
		else return getParent().getNextWidget(this);
	}
	
	public IWidget getPreviousWidget(IWidget start)
	{
		if(start.equals(secondWidget))
			return firstWidget;
		else return getParent().getNextWidget(this);
	}




	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		if(getFirstWidget() instanceof StandardWidget || getFirstWidget() == null)
		{
			if(stream.startSubcontext("firstWidget"))
			{
				firstWidget =(StandardWidget) stream.processChild((StandardWidget)firstWidget, XMLTheme.TYPE_REGISTRY);
				stream.endSubcontext();
			}
		}
		
		if(getSecondWidget() instanceof StandardWidget || getSecondWidget() == null)
		{
			if(stream.startSubcontext("secondWidget"))
			{
				secondWidget =(StandardWidget) stream.processChild((StandardWidget)secondWidget, XMLTheme.TYPE_REGISTRY);
				stream.endSubcontext();
			}
		}
		
		if(isHorizontal())
			setPixmap(stream.processChild("HorizontalPixmap", getPixmap(), null, Pixmap.class));
		else
			setPixmap(stream.processChild("VerticalPixmap", getPixmap(), null, Pixmap.class));
		
	}

	@Override
	public boolean isTraversable()
	{
		return true;
	}

	@Override
	public void focusChanged(FocusEvent focusEvent)
	{
		super.focusChanged(focusEvent);
		
		if(focusEvent.isFocusGained())
		{
			getDisplay().setFocusedWidget(firstWidget);
		}
	}
	
}
