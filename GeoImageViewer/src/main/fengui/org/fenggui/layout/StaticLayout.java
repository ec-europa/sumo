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
 * Created on 2005-3-27
 * $Id: StaticLayout.java 327 2007-08-11 11:20:15Z Schabby $
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
 * The layout manager for the static layout. The static layout leaves
 * all positions and size as they are. 
 * The developer ha complete control
 * over the layout this way. However, except for the Display, 
 * it is recommended to not use the static layout.
 * 
 * @author Johannes Schaback
 * @dedication NOFX - The Decline
 */
public class StaticLayout extends LayoutManager
{

	/**
	 * Does nothing!
	 */
    public void doLayout(Container container, List<IWidget> content) {
    	/*
        for(Widget w: content) {
        	w.setValidHeight(w.getMinHeight());
        	w.setValidWidth(w.getMinWidth());
        }*/
    }

    /**
     * Sets the x and y coordinate of the specified Widget so that
     * it is in the center of the specified Container.
     * @param widget the Widget to center
     * @param container the parent of the Widget
     */
    public static void center(IWidget widget, Container container) 
    {
    	widget.setX((container.getAppearance().getContentWidth()/2)-widget.getSize().getWidth()/2);
    	widget.setY((container.getAppearance().getContentHeight()/2)-widget.getSize().getHeight()/2);
    }

	public Dimension computeMinSize(Container container, List<IWidget> content)
	{
		return new Dimension(container.getMinSize());
	}

	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
	}

}
