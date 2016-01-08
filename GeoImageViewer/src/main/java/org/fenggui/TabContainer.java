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
 * $Id: TabContainer.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.util.ArrayList;

import org.fenggui.event.FocusEvent;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.render.Pixmap;
import org.fenggui.util.Dimension;

/**
 * Implementation of a tab container. 
 * 
 * @author Johannes Schaback aka Schabby, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public class TabContainer extends StandardWidget implements IContainer
{
	private boolean tabOnTop;
	private ArrayList<TabItem> tabs = new ArrayList<TabItem>();
	private int activeTab = -1;
	private TabContainerAppearance appearance = null;
	
	/**
	 * Creates a TabContainer with the tabs drawn at the top.
	 */
	public TabContainer()
	{
		this(true);
	}
	
	/**
	 * Constructor of TabContainer.
	 * @param tabOnTop True if the tabs should be placed at the top of the container.<p>
	 * 				   Flase if the tabs should be placed at the bottom of the container.
	 */
	public TabContainer(boolean tabOnTop)
	{
		// Store the boolean.
		this.tabOnTop = tabOnTop;
		appearance = new TabContainerAppearance(this);
		setupTheme(TabContainer.class);
		updateMinSize();
	}
	
	
	
	public TabContainerAppearance getAppearance()
	{
		return appearance;
	}



	public void addTab(String title, Pixmap pixmap, IWidget widget)
	{
		TabItemLabel label = new TabItemLabel(this);
		label.setParent(this);
		label.setText(title);
		label.setPixmap(pixmap);
		TabItem tab = new TabItem(widget, label);
		widget.setParent(this);
		if (getDisplay() != null) {
			widget.addedToWidgetTree();
		}
		tabs.add(tab);
		
		updateMinSize();
		
		if(tabs.size() == 1) selectTab(0);
	}
	
	
	
	@Override
	public boolean isTraversable()
	{
		return true;
	}

	public void selectTab(TabItemLabel label)
	{
		for(int i = 0; i < tabs.size(); i++)
		{
			TabItem item = tabs.get(i);
			if(item.label.equals(label))
			{
				selectTab(i);
				break;
			}
		}
	}
	
	public int getChildrenCount() {
		return tabs.size();
	}
	
	public void selectTab(int index)
	{
		// if the selected index isn't valid, do nothing.
		if (index < 0 || index >= tabs.size()){
			return;
		}

		if(activeTab >= 0) 
			tabs.get(activeTab).label.getAppearance().setEnabled(TabItemLabel.LABEL_FOCUSED, false);
		
		TabItem item = tabs.get(index);
		
		item.label.getAppearance().setEnabled(TabItemLabel.LABEL_FOCUSED, true);
		activeTab = index;
		item.widget.setSize(new Dimension(getAppearance().getContentWidth(), getAppearance().getContentHeight()-item.label.getHeight()));
		item.widget.setX(0);
		if(this.tabOnTop)
		{
			item.widget.setY(0);
		}
		else
		{
			item.widget.setY(item.label.getHeight());
		}
	}
	
	public TabItemLabel getSelectedTabLabel()
	{
		return tabs.get(activeTab).label;
	}
	
	public IWidget getSelectedTabWidget()
	{
		return tabs.get(activeTab).widget;
	}
	
	private class TabItem
	{
		IWidget widget = null;
		TabItemLabel label = null;
		
		public TabItem(IWidget widget, TabItemLabel label)
		{
			this.widget = widget;
			this.label = label;
		}
	}

	@Override
    public IWidget getWidget(int x, int y) 
    {
        if(!getAppearance().insideMargin(x, y)) 
        {
            return null;
        }
        
        if(tabs.isEmpty()) return this;
        
        IWidget ret = null;
        IWidget found = this;
        
        x -= getAppearance().getLeftMargins();
        y -= getAppearance().getBottomMargins();
        
        for(TabItem item: tabs) 
        {
        	IWidget w = item.label;
            ret = w.getWidget(x-w.getX(), y-w.getY());
            
            if(ret != null) found = ret;
 
        }
        
        
        IWidget w = tabs.get(activeTab).widget;
        ret = w.getWidget(x-w.getX(), y-w.getY());
        if(ret != null)
        	found = ret;
        
        return found;
    }

	@Override
	public void layout()
	{
		int xOffset = 0;
		
		for(TabItem item: tabs)
		{
			TabItemLabel label = item.label;
			label.setSizeToMinSize();
			if(this.tabOnTop)
			{
				label.setXY(xOffset, getAppearance().getContentHeight() - label.getHeight());
			}
			else
			{
				label.setXY(xOffset, 0);
			}
			xOffset += label.getWidth();
			label.layout();
			
			IWidget widget = item.widget;
			
			widget.setX(0);
			if(this.tabOnTop)
			{
				widget.setY(0);
			}
			else
			{
				widget.setY(label.getHeight());
			}
			widget.setSize(new Dimension(getAppearance().getContentWidth(), getAppearance().getContentHeight() - label.getHeight()));
			widget.layout();
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
		//System.out.println("start "+start);
		if(start.equals(getSelectedTabWidget()))
		{
			//System.out.println("retrun "+tabs.get(activeTab).label.getText());
			return tabs.get(0).label;
			
		}
		else
		{
			int index = 0;
			for(index = 0; index < tabs.size(); index++)
			{
				if(tabs.get(index).label.equals(start)) break;
			}
			
			index++;
			
			if(index >= tabs.size()) return getParent().getNextWidget(this);
			
			return tabs.get(index).label;
		}
	}

	@Override
	public void focusChanged(FocusEvent focusEvent)
	{
		super.focusChanged(focusEvent);
		
		if(focusEvent.isFocusGained())
		{
			getDisplay().setFocusedWidget(tabs.get(activeTab).widget);
		}
	}
	
	public void addWidget(IWidget w)
	{
		addTab("No Title", null, w);
	}
	
	public void addWidget(IWidget w, int position) 
	{
		addTab("No Title", null, w);
	}

	public IWidget getPreviousWidget(IWidget start)
	{
		// XXX implement me!
		return null;
	}
	
	public class TabContainerAppearance extends DecoratorAppearance
	{
		public TabContainerAppearance(TabContainer w)
		{
			super(w);
		}

		@Override
		public Dimension getContentMinSizeHint()
		{
			int widthMax = 0;
			int heightMax = 0;
			
			for(TabItem item: tabs)
			{
				widthMax = Math.max(item.widget.getMinSize().getWidth(), widthMax);
				heightMax = Math.max(item.widget.getMinSize().getHeight(), heightMax);
			}
			
			if(!tabs.isEmpty())
			{
				heightMax += tabs.get(0).label.getMinHeight();
			}
			
			return new Dimension(widthMax, heightMax);
		}
		
		@Override
		public void paintContent(Graphics g, IOpenGL gl)
		{
			if(tabs.isEmpty()) return;
			
			for(TabItem item: tabs)
			{
				g.translate(item.label.getX(), item.label.getY());
				item.label.paint(g);
				g.translate(-item.label.getX(), -item.label.getY());
			}
			
			g.translate(tabs.get(activeTab).widget.getX(), tabs.get(activeTab).widget.getY());
			tabs.get(activeTab).widget.paint(g);
			g.translate(-tabs.get(activeTab).widget.getX(), -tabs.get(activeTab).widget.getY());
		}
	}

	@Override
	public void addedToWidgetTree()
	{
		super.addedToWidgetTree();
		for(TabItem item: tabs)
			if(item.widget != null)
				item.widget.addedToWidgetTree();
	}



	@Override
	public void removedFromWidgetTree()
	{
		super.removedFromWidgetTree();
		for(TabItem item: tabs)
			if(item.widget != null)
				item.widget.removedFromWidgetTree();
		
	}
	
	
}