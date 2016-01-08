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
 * $Id: MenuItemPressedEvent.java 131 2006-12-18 14:41:34Z bbeaulant $
 */
package org.fenggui.event;

import org.fenggui.menu.Menu;
import org.fenggui.menu.MenuItem;

/**
 * Class that represents the event when pressing menu items.
 * 
 * @author Johannes Schaback aka Schabby, last edited by $Author: bbeaulant $, $Date: 2006-12-18 15:41:34 +0100 (Mon, 18 Dec 2006) $
 * @version $Revision: 131 $
 */
public class MenuItemPressedEvent extends Event
{
    private MenuItem item = null;
    
    public MenuItemPressedEvent(Menu source, MenuItem i) 
    {
    	super(source);
        item = i;
    }
    
    public MenuItem getItem() 
    {
        return item;
    }
    
}
