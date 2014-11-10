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
 * Created on 2005-3-26
 * $Id: BorderLayout.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.layout;

import java.io.IOException;
import java.util.List;

import org.fenggui.Container;
import org.fenggui.IWidget;
import org.fenggui.LayoutManager;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Dimension;

/**
 * Works similar to the well known BorderLayout in AWT/Swing.
 * 
 * @author Johannes Schaback
 *
 */
public class BorderLayout extends LayoutManager 
{
    /* (non-Javadoc)
     * @see windowtoolkit.ILayoutManager#doLayout(windowtoolkit.Container, windowtoolkit.event.List)
     */
    public void doLayout(Container container, List<IWidget> content) {
        int widthWest = 0; 
        int widthEast = 0;

        int heightNorth = 0;
        int heightSouth = 0;
                
        for(IWidget c: content) {
            BorderLayoutData bld = (BorderLayoutData) c.getLayoutData();
            
            if(bld == null)
            {
                /*
                 // we do not send this warning anymore because it interfered
                  // with popups when the borderlayout was set on the
                  // display @todo think if that's wise #
                System.err.println("The container ("+container+") has an BorderLayout, but this child ("+c+") " +
                        "does not have a BorderLayoutData value set as its layout data!");
                */
                continue;
            }
            
            // Running through all widgets to successively retrieve the
            // values for heightNorth, heightSouth, widthEast and widthWest.
            if(bld.getValue() == BorderLayoutData.NORTH_VALUE)
            {
                heightNorth = getValidMinHeight(c);
            } 
            else if(bld.getValue() == BorderLayoutData.SOUTH_VALUE) 
            {
                heightSouth = getValidMinHeight(c);
            } 
            else if(bld.getValue() == BorderLayoutData.EAST_VALUE) 
            {
                widthEast = getValidMinWidth(c);
            } 
            else if(bld.getValue() == BorderLayoutData.WEST_VALUE) 
            {
                widthWest = getValidMinWidth(c);
            }
        }
        
        for(IWidget w: content) 
        {
            BorderLayoutData bld = (BorderLayoutData) w.getLayoutData();
            
            if(bld == null) 
            {
            	continue;
            }
            
            if(bld.getValue() == BorderLayoutData.NORTH_VALUE) 
            {
                w.setX(widthWest);
                w.setY(container.getAppearance().getContentHeight() - heightNorth);
                setValidSize(w, 
                	container.getAppearance().getContentWidth() - widthWest - widthEast,
                	getValidMinHeight(w));
            } 
            else if(bld.getValue() == BorderLayoutData.SOUTH_VALUE) 
            {
                w.setX(widthWest);
                w.setY(0);
                setValidSize(w,
                	container.getAppearance().getContentWidth() - widthWest - widthEast,
                	getValidMinHeight(w));
            } 
            else if(bld.getValue() == BorderLayoutData.EAST_VALUE) {
                w.setX(container.getAppearance().getContentWidth() - getValidMinWidth(w));
                w.setY(0);
                setValidSize(w,
                	getValidMinWidth(w),
                	container.getAppearance().getContentHeight());
            } 
            else if(bld.getValue() == BorderLayoutData.WEST_VALUE) {
                w.setX(0);
                w.setY(0);
                setValidSize(w,
                	getValidMinWidth(w),
                	container.getAppearance().getContentHeight());
            } 
            else  
            { // CENTER
                w.setX(widthWest);
                w.setY(heightSouth);
                setValidSize(w,
                	container.getAppearance().getContentWidth() - widthEast - widthWest,
                	container.getAppearance().getContentHeight() - heightSouth-heightNorth);
            }
        }        
        
        //widthCenter = 
        
    }

    /* (non-Javadoc)
     * @see windowtoolkit.ILayoutManager#getMinHeight(windowtoolkit.event.List)
     */
    public int getMinHeight(List<IWidget> content)
    {
        int leftColumn = 0;
        int rightColumn = 0;
        int middleColumn = 0;
        
        for(IWidget c: content)
        {
            BorderLayoutData bld = (BorderLayoutData) c.getLayoutData();
            if(bld == null) continue;
            
            if(bld.getValue() == BorderLayoutData.CENTER_VALUE ||
            		bld.getValue() == BorderLayoutData.NORTH_VALUE ||
            		bld.getValue() == BorderLayoutData.SOUTH_VALUE)
            {
            	middleColumn += getValidMinHeight(c);
            }
            else if(bld.getValue() == BorderLayoutData.EAST_VALUE)
            {
            	rightColumn = getValidMinHeight(c);
            }
            else if(bld.getValue() == BorderLayoutData.WEST_VALUE)
            {
            	leftColumn = getValidMinHeight(c);
            }
        }
        
        return Math.max(leftColumn, Math.max(rightColumn, middleColumn));
    }
    
    /* (non-Javadoc)
     * @see windowtoolkit.ILayoutManager#getMinWidth(windowtoolkit.event.List)
     */
    public int getMinWidth(List<IWidget> content)
    {
        int leftColumn = 0;
        int rightColumn = 0;
        int middleColumn = 0;
        
        for(IWidget c: content)
        {
            BorderLayoutData bld = (BorderLayoutData) c.getLayoutData();
            if(bld == null) continue;
            
            if(bld.getValue() == BorderLayoutData.CENTER_VALUE ||
            		bld.getValue() == BorderLayoutData.NORTH_VALUE ||
            		bld.getValue() == BorderLayoutData.SOUTH_VALUE)
            {
            	if(middleColumn < getValidMinWidth(c)) middleColumn = getValidMinWidth(c);
            }
            else if(bld.getValue() == BorderLayoutData.EAST_VALUE)
            {
            	rightColumn = getValidMinWidth(c);
            }
            else if(bld.getValue() == BorderLayoutData.WEST_VALUE)
            {
            	leftColumn = getValidMinWidth(c);
            }
        }
        
        return leftColumn + middleColumn + rightColumn;    
    }

	public Dimension computeMinSize(Container container, List<IWidget> content)
	{
		return new Dimension(getMinWidth(content), getMinHeight(content));
	}

	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
	}

    
}
