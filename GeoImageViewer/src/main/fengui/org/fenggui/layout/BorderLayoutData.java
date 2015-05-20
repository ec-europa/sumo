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
 * Created on May 18, 2005
 * $Id: BorderLayoutData.java 28 2006-10-05 01:37:07Z schabby $
 */
package org.fenggui.layout;

/**
 * Type to give the border layout manager a clue how to
 * layout its children. The possible values are
 * <ul>
 * <li>Use <code>NORTH</code> to align a Widget at the top of the parent Container.</li>
 * <li>Use <code>SOUTH</code> to align a Widget at the bottom of the parent Container.</li>
 * <li>Use <code>EAST</code> to align a Widget at the right hand side of the parent Container.</li>
 * <li>Use <code>WEST</code> to align a Widget at the left hand side of the parent Container.</li>
 * <li>Use <code>CENTER</code> to place a Widget in the middle of the parent Container.</li>
 * </ul>
 * @see org.fenggui.layout.BorderLayout
 * @see org.fenggui.Container
 * 
 * @todo make this BorderLayoutData class an enum! #
 * 
 * @author Johannes Schaback
 */
public class BorderLayoutData implements ILayoutData {
    
    public BorderLayoutData(int v) {
        value = v;
    }
    
    protected static final int NORTH_VALUE = 0;
    protected static final int WEST_VALUE = 1;
    protected static final int EAST_VALUE = 2;
    protected static final int SOUTH_VALUE = 3;
    protected static final int CENTER_VALUE = 4;

    public static final BorderLayoutData NORTH = new BorderLayoutData(NORTH_VALUE);
    public static final BorderLayoutData WEST = new BorderLayoutData(WEST_VALUE);
    public static final BorderLayoutData SOUTH = new BorderLayoutData(SOUTH_VALUE);
    public static final BorderLayoutData CENTER = new BorderLayoutData(CENTER_VALUE);
    public static final BorderLayoutData EAST = new BorderLayoutData(EAST_VALUE);
    
    
    private int value = NORTH_VALUE;
    
    public int getValue() {
        return value;
    }
    
}
