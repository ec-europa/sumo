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
 * $Id: RowLayout.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.layout;

import java.io.IOException;
import java.util.List;

import org.fenggui.Container;
import org.fenggui.LayoutManager;
import org.fenggui.IWidget;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Dimension;

/**
 * Layouts Widgets in a vertical or horizontal row.
 * The available inner size of the container is shared equally to the Widgets.
 * 
 * @author Johannes Schaback
 *
 */
public class RowLayout extends LayoutManager {
	   
    private boolean horizontal = true;
    
    public RowLayout() {
        
    }

    /**
     * Creates a new row layout. 
     * @param horizontal specifies whether the row will be
     * a vertical or horizontal in the container
     */
    public RowLayout(boolean horizontal) {
        this.horizontal = horizontal;
    }    
       
    
    public boolean isHorizontal() {
        return horizontal;
    }
    
    /* (non-Javadoc)
     * @see windowtoolkit.ILayoutManager#doLayout(windowtoolkit.Container, windowtoolkit.event.List)
     */
    public void doLayout(Container container, List<IWidget> content) 
    {
        double freeSpacePerComp = 0;
        
        int reqSpace =  horizontal ? 
        		getSumOfAllWidths(content) : getSumOfAllHeights(content);
        
        freeSpacePerComp = horizontal ? 
        		container.getAppearance().getContentWidth() : 
				container.getAppearance().getContentHeight();
		
        double expandableWidgets = 0;
        
        for(IWidget c: content) 
        	if(c.isExpandable()) expandableWidgets++;
        
        // in cases there are only non-expandable widgets in the container
        if(expandableWidgets == 0) expandableWidgets = 1;
        
        // space that can be additionally consumed by each Widget
        freeSpacePerComp =  (double) (freeSpacePerComp - reqSpace) / expandableWidgets;
        
        // uhoh, Container is actually too small
        if(freeSpacePerComp < 0 ) freeSpacePerComp = 0;
        
        int x = 0;
        int y = 0;
        
        // iterate children backwards to make them appear in the same
        // order as they have been added
        for(int i = 0; i < content.size(); i++) 
        {
        	
        	IWidget w = null;

            if(horizontal)
            {
            	w = content.get(i);
            	
            	setValidSize(w,
            		(int) freeSpacePerComp + getValidMinWidth(w),
            		container.getAppearance().getContentHeight());
            	
                w.setX(x);
                x += w.getSize().getWidth();
                w.setY(container.getAppearance().getContentHeight()/2 - w.getSize().getHeight()/2);
            } 
            else 
            {
            	w = content.get(content.size() - i - 1);
            	
            	setValidSize(w,
            		container.getAppearance().getContentWidth(),
            		(int)freeSpacePerComp + getValidMinHeight(w));

                w.setY(y);
                y += w.getSize().getHeight();
                w.setX(container.getAppearance().getContentWidth()/2 - w.getSize().getWidth()/2);
            }
        }    
       
    }
    
    private int getSumOfAllHeights(List<IWidget> content)
    {
        int sum = 0;
        for(IWidget c: content) 
        {
            sum += getValidMinHeight(c);
        }
        return sum;
    }

    

    private int getSumOfAllWidths(List<IWidget> content)
    {
        int reqW = 0;
        for(IWidget c: content)
        {
            reqW += getValidMinWidth(c);
        }
        return reqW;
    }    

    /* (non-Javadoc)
     * @see joglui.LayoutManager#updateMinSize(joglui.Container, joglui.List)
     */
    public Dimension computeMinSize(Container container, List<IWidget> content) 
    {
        int minW = 0;
        int minH = 0;
        
        // compute the min width of the container
        for(IWidget c: content) 
        {
            if(horizontal) 
            {
            	minW += getValidMinWidth(c);
            	if(minH < getValidMinHeight(c)) minH = getValidMinHeight(c);
            } 
            else 
            {
            	if(minW < getValidMinWidth(c)) minW = getValidMinWidth(c);
            	minH += getValidMinHeight(c);
            }
        }

        //System.out.println("RowLayout "+container+" "+minW +" "+minH);
        return new Dimension(minW, minH);
    }

	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		horizontal = stream.processAttribute("horizontal", horizontal);
	}

}
