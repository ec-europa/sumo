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
 * Created on Apr 30, 2005
 * $Id: FormAttachment.java 28 2006-10-05 01:37:07Z schabby $
 */
package org.fenggui.layout;

import org.fenggui.Widget;

/**
 * Describes where to place Widgets when using FormLayout.
 * 
 * Note: offset is currently not used.
 * 
 * @author Johannes Schaback ($Author: schabby $)
 *
 */
public class FormAttachment {

    private int offset;
    private Widget attachedWidget = null;
    private int numerator=0;
    
    /**
     * Creates a new FormAttachment. 
     * @param w attaches to the Widget
     * @param offset leave gap
     */
    public FormAttachment(Widget w, int offset) {
        attachedWidget = w;
        if(attachedWidget == null) { 
        	// this took me two hours to find out about this problem! Grrr
        	System.err.println("FormAttachment Constructor Warning: The Widget you were trying to attach to is null!");
        	System.err.println("This leads to unexepected behavior in the FormLayout.");
        	System.err.println("Make sure you attach Widgets that are both initiated!");
        }
        this.offset = offset;
    }
    
    /**
     * Creates a new FormAttachment
     * @param numerator position in percent according the
     * container size in which the layouted Widget lays.
     * @param offset
     */
    public FormAttachment(int numerator, int offset) {
        this.numerator = numerator;
        this.offset = offset;
    }
    
    
    protected Widget getAttachedWidget() {
        return attachedWidget;
    }
    protected int getNumerator() {
        return numerator;
    }
    protected int getOffset() {
        return offset;
    }
    
    protected boolean isStatic() {
        return attachedWidget == null;
    }
}
