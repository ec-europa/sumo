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
 * $Id: Container.java 353 2007-08-30 14:59:42Z marcmenghin $
 */
package org.fenggui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fenggui.event.FocusEvent;
import org.fenggui.layout.RowLayout;
import org.fenggui.render.Binding;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.theme.XMLTheme;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.theme.xml.MissingElementException;
import org.fenggui.util.Dimension;

/**
 * A Container is a set of Widgets. The
 * layout manager that is assigned to the Container is responsible for
 * the size and position of its content. In terms of the tree
 * data structure, a container is a node with an arbitrary number
 * of child nodes. 
 * 
 * Extends <code>StateWidget</code> because menus need to be in states as well
 * as combo boxes and both Widgets need to be containers.
 * 
 * @todo implement enabledState (defaultState) and disabledState #
 * 
 * @author Johannes Schaback
 * @dedicated NOFX - Lazy
 */
public class Container extends StandardWidget implements IContainer 
{
    private LayoutManager layoutManager = null;
    protected ArrayList<IWidget> notifyList = new ArrayList<IWidget>();
    private boolean keyTraversalRoot = false;
    private ContainerAppearance appearance = null;

    public boolean isKeyTraversalRoot()
	{
		return keyTraversalRoot;
	}

	public void setKeyTraversalRoot(boolean traversalRoot)
	{
		this.keyTraversalRoot = traversalRoot;
	}

	public void setAppearance(ContainerAppearance appearance)
	{
		this.appearance = appearance;
	}
	
	public ContainerAppearance getAppearance()
	{
		return appearance;
	}

	/**
     * Creates a new <code>Container</code>.
     */
	public Container() 
	{
		this(new RowLayout());
    }
	
	public Container(LayoutManager layoutManager) 
	{
		super();
        this.layoutManager = layoutManager;
        appearance = new ContainerAppearance(this);
        setupTheme(Container.class);
        updateMinSize();
    }
	
	@Override
	public void focusChanged(FocusEvent focusEvent)
	{
		super.focusChanged(focusEvent);
		
		if(focusEvent.isFocusGained())
		{
			int i =0;
			
			while(i < size() && !notifyList.get(i).isTraversable()) i++;
			
			if(i >= size()) return;
			
			getDisplay().setFocusedWidget(notifyList.get(i));
		}
	}
    
    @Override
  public void setSize(Dimension s) {
    super.setSize(s);
    this.layout();
  }

    /**
     * If the widget is bigger than the container, trim the
     * clip space size to the size of the container so that
     * the overlapping Widget gets clipped at the Container
     * borders. If the widget fits in the container, simply
     * set the clip space to the size of the widget. You can
     * regard this operation as a logical AND.
     * 
     * The thing is why we do the clipping here and not in 
     * Widget.display is, that Widgets are drawn in their own
     * Widget coordinate system which may have its origin 
     * outside of the Container. Because the clipping is set
     * before the border is drawn, Widgets may overdraw the border,
     * padding and spacing. It is up to the drawing routine of the
     * WidgetAdapter to prevent that.
     * 
     * @todo Widgets that are in a sub-container of this Container are
     * not clipped correctly if they overlap this container! I suggest
     * introducing a Stack for clipping planes and each Container keeps
     * his own clipping plane equations. Then, during rendering, each 
     * Container puts his own clipping equations on the stack. A helper
     * method evaluates the logical AND out of the clipping planes
     * on the stack so that nested, overlapping containers are clipped #
     * 
     * It is not alllowed to place the clipspace outside of the viewport.
     * In this case the widget would not be visible anyway. The returned flag
     * indicates exactely this. Further processing of the widget can be neglected.
     * 
     * @param g graphics
     * @param c widget
     * @return true if valid
     */
    final boolean clipWidget(Graphics g, IWidget c) {
    	
    	int startX = c.getX() < 0 ? 0 : c.getX();
    	int startY = c.getY() < 0 ? 0 : c.getY();
    	
    	Binding b = Binding.getInstance();
    	
    	if(startX >= b.getCanvasWidth() || startY >= b.getCanvasHeight())
    	{
    		return false;
    	}
    	
    	int cWidth = c.getSize().getWidth();
    	int cHeight = c.getSize().getHeight();
    	
        g.setClipSpace(
                startX,
                startY,
                c.getX() + cWidth  > getWidth()  ? getWidth()  - startX : cWidth,
                c.getY() + cHeight > getHeight() ? getHeight() - startY : cHeight);
        
        return true;
    }

    /**
	 * Returns true as containers are always traversable. Note that
	 * the focus gets forwarded to the first widget in the container.
	 */
	public boolean isTraversable()
	{
		return true;
	}
    
/*
    @Override
	public Widget relyFocus()
	{
    	for(int i=0 ; i < size(); i++)
    	{
    		if(notifyList.get(i).relyFocus() != null) return notifyList.get(i).relyFocus();
    		System.out.println("    relyFocus: Skipping "+notifyList.get(i));
    	}
    	
    	return super.relyFocus();
	}
*/
	/**
     * Computes the minimum size by delegating the request to the
     * currently set layout manager.
     */
    @Override
    public void updateMinSize() 
    {
    	setMinSize(getAppearance().getMinSizeHint());
    	
        if(getParent() != null) getParent().updateMinSize();
    }

    /**
     * Returns the children of this container.
     * @return the children Widgets
     */
    public java.util.List<IWidget> getContent() 
    {
        return notifyList;
    }
    
    /**
     * Adds a Widget to the container.
     * @param c
     *      The Widget to be added.
     */
    public final void addWidget(IWidget c, int position) {
        
    	if (position < 0) position = 0;
    	if (position > notifyList.size()) position = notifyList.size();
    	
        if(c == null) return;
        
        if(c.equals(this)) 
        {
        	throw new IllegalArgumentException("Can't add myself! c.equals(this)");
        }
        
        if(c.equals(getParent())) 
        {
        	throw new IllegalArgumentException("Can't add my parent!");
        }
        
        if(notifyList.contains(c)) 
        {
        		System.err.println("Container.addWidget: Widget "+c+" is already in the container ("+this+")");
        }
        else 
        {
        	//if(relyFocus() == null && c.relyFocus() != null) setRelyFocus(c);
            notifyList.add(position, c);
            c.setParent(this);
            
            if(getDisplay() != null)
            	c.addedToWidgetTree();
        }
        
        updateMinSize();
    }
    
    /**
     * Reorders the children such that the given child is drawn last and therefore appears as the top child.
     * @param child the child to bring to top
     */
    public void bringToFront(IWidget child)
    {
    	if(!notifyList.contains(child)) throw new IllegalArgumentException("The given child must be in this container");
    	
    	notifyList.remove(child);
    	notifyList.add(notifyList.size(), child);
    }
    
    public void addWidget(IWidget widget)
    {
    	addWidget(widget, notifyList.size());
    }
    
    
    @Override
    public void removedFromWidgetTree()
	{
    	super.removedFromWidgetTree();
    	for(int i = 0; i < notifyList.size(); i++)
    		notifyList.get(i).removedFromWidgetTree();
	}

	@Override
	public void addedToWidgetTree()
	{
		for(int i = 0; i < notifyList.size(); i++)
    		notifyList.get(i).addedToWidgetTree();
	}

    
    
	/**
     * Sets the layout manager.
     * @param lm layout manager
     */
    public void setLayoutManager(LayoutManager lm) 
    {
        if(lm == null) return;
        layoutManager = lm;
        
        updateMinSize();
    }
    

    /**
     * Returns the currently set layout manager.
     * @return layout manager
     */
    public LayoutManager getLayoutManager() {
        return layoutManager;
    }
        
    /**
     * Layouts this Container according to his layout manager.
     */
    @Override
    public void layout() 
    {
    	// layout this container according to the min. sizes of the children
    	// and my own size. Since i know the min. size of the children and
    	// my final size, I can set the final size of my children as well.
        layoutManager.doLayout(this, notifyList);
        
        // pass layout call to my children.
        for(IWidget c: notifyList) c.layout();
    }
    
    /**
     * Updates the min. size and calls layout().
     * @deprecated use layout(), the min. size is kept up to date automatically
     */
    public void updateMinSizeAndLayout()
    {
    	updateMinSize();
    	layout();
    }
    
    /**
     * Removes the given Widget from this Container
     * @param c the Widget
     */
    public void removeWidget(IWidget c) 
    {
        if(c == null) return;
        if(c.equals(this)) throw new IllegalArgumentException("Cannot remove myself! "+this);
        	
        for(int i=0;i<notifyList.size();i++) 
        {
        	if(c.equals(notifyList.get(i))) 
        	{
        		notifyList.remove(i);
        		c.removedFromWidgetTree();
        		c.setParent(null);
        		
        	}
        }
        
        updateMinSize();
        
        if(getDisplay() != null) getDisplay().focusedWidgetValityCheck();
    }

    /**
     * Removes the specified
     * direct child Widgets from this Container.
     *   
     * @param list list with Widgets to be removed
     */
    public void removeWidgets(java.util.List<IWidget> list)
    {
        if(list == null) return;
        
        for(int i=0; i<list.size(); i++)
        {
        	removeWidget(list.get(i));
        }
    }

    /**
     * Removes all Widgets from this Container
     */
    public void removeAllWidgets() 
    {
        while(size() > 0) removeWidget(getWidget(0));
    }
    
    /**
     * Returns the child widget at the specified position.
     * The given position is relative to this Container.
     * @return the child widget or null if no widget has been found
     */
    public IWidget getWidget(int x, int y) 
    {
        if(!getAppearance().insideMargin(x, y)) 
        {
            return null;
        }
        
        IWidget ret = null;
        IWidget found = this;
        
        x -= getAppearance().getLeftMargins();
        y -= getAppearance().getBottomMargins();
        
        for(IWidget w: notifyList) 
        {
            ret = w.getWidget(x-w.getX(), y-w.getY());
            
            if(ret != null) found = ret;
 
        }
        
        return found;
    }

    /**
     * Puts the name of the children in a String.
     */
    @Override
    public String toString() {
        if(notifyList == null) {
            return super.toString()+" {}";
        }
        
        String s = super.toString()+" {";
        
        for(int i=0;i<notifyList.size();i++) {
            s += notifyList.get(i).getClass().getSimpleName();
            if(i < notifyList.size()-1) s += ", ";
        }
        s += "}";
        return s;
    }
    
	/**
	 * Returns the number of direct children.
	 * 
	 * @return number of children.
	 */
	public int size() {
		return notifyList.size();
	}
	
	/**
	 * Returns the child Widget with the specified index.
	 * @param index the index of the child Widget
	 * @return the child Widget
	 */
	public IWidget getWidget(int index) {
		return notifyList.get(index);
	}
	
	/**
	 * Returns all direct children.
	 * 
	 * @return children Widgets
	 */
	public Iterable<IWidget> getWidgets() 
	{
		return notifyList;
	}

	@Override
	public int getDisplayX()
	{
		return super.getDisplayX() + getAppearance().getLeftMargins();
	}


	@Override
	public int getDisplayY()
	{
		return super.getDisplayY() + getAppearance().getBottomMargins();
	}
	
	/**
	 * Layouts and sets the container to the minimum
	 * size. Calling this method is equal to calling
	 * this sequence of commands.
	 * 
	 * <code>
	 * setSizeToMinSize();
	 * layout();
	 * </code>
	 * 
	 */
	public void pack() 
	{
		setSizeToMinSize();
		layout();
	}
	
	public class ContainerAppearance extends DecoratorAppearance
	{
		Container container = null;

		public ContainerAppearance(Container w)
		{
			super(w);
			this.container = w;
		}

		@Override
		public Dimension getContentMinSizeHint()
		{
			return container.getLayoutManager().computeMinSize(container, container.getContent());
		}

		@Override
		public void paintContent(Graphics g, IOpenGL gl)
		{
	        IOpenGL opengl = g.getOpenGL();

	        List<IWidget> notifyList = container.getContent();
	        
	        for(int i = 0; i < notifyList.size(); i++) 
	        {
	        	IWidget c = notifyList.get(i);
	        	
	            // if widget lays completly outside
	            if(c.getX() > container.getWidth() || c.getY() > container.getHeight()) continue;
	            
	            //XXX the upper statement does not recognize the margins of the container!
	            
	            // if parent is a Scroll Container we have to make sure that
	            // the current clip space will NOT be overriden because the
	            // inner container is likely to be bigger than the view
	            // rectangle. The inner container would set the clip space
	            // to his size, resulting in rendering widgets from the inner
	            // container which are not in the view rectangle.
	            if(!(container.getParent() instanceof ScrollContainer)) 
	            {
	                boolean valid = container.clipWidget(g, c);
	                
	                if(!valid) return;
	            }
	            
	            opengl.pushMatrix();
	            g.translate(c.getX(), c.getY());
	                
	            c.paint(g);
	            
	            g.translate(-c.getX(), -c.getY());
	            opengl.popMatrix();
	        }
		}

	}


	/**
	 * Returns the predecessor of the given widget.
	 * @param currentWidget given widget, or null for last widget in container
	 * @return widget the previous widget
	 */
	public IWidget getPreviousWidget(IWidget currentWidget)
	{
		int i;
		if(currentWidget == null) i = size()-1;
		else i =  notifyList.indexOf(currentWidget)-1;

		if(i < 0) {
			if(isKeyTraversalRoot()) i = size()-1;
			else return getParent().getPreviousWidget(this);
		}

		return notifyList.get(i);
	}
    
    /**
     * Returns the successor of the given widget.
     * @param currentWidget the given widget, or null to return first in container
     * @return next widget
     */
    public IWidget getNextWidget(IWidget currentWidget)
    {
    	int i;
    	if(currentWidget == null) i = 0;
    	else i =  notifyList.indexOf(currentWidget)+1;
		
		if(i > size()-1) {
			if(isKeyTraversalRoot()) i = 0;
			else
			{
				if(getParent() == null) 
				{
					//System.err.println("Container.getNextWidget: parent is null and key focus probable lost!");
					return this;
				}
				else
				{
					IWidget nextWidget = getParent().getNextWidget(this);
					return nextWidget;
				}
				
			}
				
		}
		
		return notifyList.get(i);
    }
    
    /**
     * Returns the next traversable widget.
     * @param currentWidget the wiget to start searching from
     * @return next traversable widget
     */
    public IWidget getNextTraversableWidget(IWidget currentWidget)
    {
    	if(!notifyList.contains(currentWidget))
    		throw new IllegalArgumentException("currentWidget is not child of this container!");
    	
		IWidget w = getNextWidget(currentWidget);
		
		while(w != null && !w.isTraversable()) w = getNextWidget(w);
		
		return w;
    }
    
    /**
     * Returns the previous trabersable widget.
     * @param currentWidget the wiget to start searching from
     * @return previous traversable widget
     */
	public IWidget getPreviousTraversableWidget(IWidget currentWidget)
	{
    	if(!notifyList.contains(currentWidget))
    		throw new IllegalArgumentException("currentWidget is not child of this container!");
    	
		IWidget w = getPreviousWidget(currentWidget);
		
		while(w != null && !w.isTraversable()) w = getPreviousWidget(w);
		
		return w;
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		try
		{
			layoutManager = stream.processChild(layoutManager, XMLTheme.TYPE_REGISTRY);
		} catch(MissingElementException e)
		{
			// we ignore the exception intentionally, because not providing a
			// layout manger means that the default layout manager should remain in
			// place (which is the RowLayoutManager)
		}
		
		if(stream.startSubcontext("children"))
		{
			stream.processChildren(notifyList, XMLTheme.TYPE_REGISTRY);
			stream.endSubcontext();
		}
	}
}
