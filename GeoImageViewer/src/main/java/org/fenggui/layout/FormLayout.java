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
 * $Id: FormLayout.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.fenggui.Container;
import org.fenggui.IWidget;
import org.fenggui.LayoutManager;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Dimension;

/**
 * Implementation of FormLayout Manager. It behaves similar to the
 * SWT FormLayout Manager.<br/>
 * A form layout is not able to compute a valid minimum size! Thus, <code>pack</code>
 * and <code>setSizeToMinSize</code> on containers having a FormLayout assigned will
 * not be layouted correctly.
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 */
public class FormLayout extends LayoutManager
{
	
    private ArrayList<IWidget> order = new ArrayList<IWidget>();
    private Hashtable<IWidget, IWidget> sorted = new Hashtable<IWidget, IWidget>();
	public boolean debug = false;
    
    public FormLayout() {
    }
    
    /**
     * Widgets that have a relative position are appended to the
     * order list. It is made sure, that the Widget on which the
     * actual Widget depends is BEFORE him in the order list. 
     * @param w the actual Widget
     * @param offset ???
     */
    private void sort(IWidget w, String offset) 
    {
        try 
        {
            //if(debug) System.out.println(offset+"in sort for "+w+", is sorted: "+sorted.containsKey(w));
            if(sorted.containsKey(w)) 
            {
            	if(debug) System.out.println("oh, "+w+" is already in the order list");
            	return;
            }
            
            //if(debug) System.out.println("sort: "+w);
            
            FormData fd = (FormData) w.getLayoutData();
            
            /*
             * The static components do not need to be processed.
             * thus, they won't be sorted.
             */ 
            if(fd.allStatic()) 
            {
            	if(debug) System.out.println("all static for "+w+", abort");
                return;
            }
            
            if(fd.left != null && !fd.left.isStatic()) 
            {
                //System.out.println(offset+"going in LEFT!");
                sort(fd.left.getAttachedWidget(), offset+"  ");
            } 
            
            if(fd.right != null && !fd.right.isStatic()) 
            {
                //System.out.println(offset+"going in RIGHT!");
                sort(fd.right.getAttachedWidget(), offset+"  ");
            } 
            
            if(fd.top != null && !fd.top.isStatic()) 
            {
                //System.out.println(offset+"going in TOP!");
                sort(fd.top.getAttachedWidget(), offset+"  ");
            } 
            
            if(fd.bottom != null && !fd.bottom.isStatic()) 
            {
                //System.out.println(offset+"going in BOTTOM!");
                sort(fd.bottom.getAttachedWidget(), offset+"  ");
            }
            
            order.add(w);
            sorted.put(w, w);
        } 
        catch(StackOverflowError soe) 
        {
            System.err.println("There seems to be a cyclic dependency " +
                    "for widget "+w+"\n"+soe.getMessage());
            System.exit(-1);
        }
    }
    
    
    
    /* (non-Javadoc)
     * @see joglui.LayoutManager#doLayout(joglui.Container, joglui.List)
     */
    public void doLayout(Container container, List<IWidget> content) 
    {
        
    	final int innerWidth = container.getAppearance().getContentWidth();
    	final int innerHeight = container.getAppearance().getContentHeight();
    	
        // only process static widgets
        for(IWidget w: content) 
        {
            
            // reset widget to smallest size
            w.setSize(new Dimension(getValidMinWidth(w), getValidMinHeight(w)));
            
            // retrieve form data
            FormData fd = (FormData) w.getLayoutData();
            if(fd == null) continue;
            
            /* 
             * First we resolve the static relations. That means the
             * coordinates (e.g. new FormAttachment(32, 0) for 32%).
             * Then we reorder the components so that the components
             * that heavily depend on the position and size of others
             * are processed at the end.
             */
            
            if (fd.left != null) 
            {
                if(fd.left.isStatic()) 
                {
                    w.setX((innerWidth * fd.left.getNumerator()) / 100);
                    w.setX(w.getX() + fd.left.getOffset());
                } 
                else sort(w, "");
            }
            
            if(fd.right != null) 
            {
                if(fd.right.isStatic()) 
                {

                    if(fd.left != null && fd.left.isStatic()) 
                    {
                    	setValidWidth(w, ((innerWidth*fd.right.getNumerator())/100)-w.getX());
                    } 
                    else 
                    {
                        // no left constraint, simply move widget to the right
                        w.setX(((innerWidth*fd.right.getNumerator())/100) - w.getSize().getWidth()); 
                        w.setX(w.getX() + fd.right.getOffset());
                    }
                } 
                else sort(w, "");
            }

            if(fd.bottom != null) 
            {
            	if(debug) System.out.println("fd.bottom: "+w.getClass().getSimpleName()+" "+fd.bottom.isStatic());
                
            	if(fd.bottom.isStatic()) 
            	{
                    w.setY(((innerHeight*fd.bottom.getNumerator())/100));
                    w.setY(w.getY() + fd.bottom.getOffset());
                } 
            	else sort(w, "");
            }                
            
            if(fd.top != null) 
            { 
                if(fd.top.isStatic()) 
                {
                    // static position
                    if(fd.bottom != null && fd.bottom.isStatic())
                    	setValidHeight(w, innerHeight-((innerHeight*fd.top.getNumerator())/100));
                    else 
                    {
                        //System.out.println("top, without bottom: "+w+"||"+w.getMinHeight());
                        w.setY((innerHeight*fd.top.getNumerator())/100-w.getSize().getHeight());
                        w.setY(w.getY() + fd.top.getOffset());
                    }
                } 
                else sort(w, "");
            }
            
        }
        
        // this time, process only the widgets with relative positions
        // to other widgets, but do not screw the previous set
        // positions (the static ones).
        for(IWidget w: order) 
        {
            
        	if(debug) System.out.println("processing in order : "+w);
        	
            // retrieve form data
            FormData fd = (FormData) w.getLayoutData();
            if(fd == null) continue;
            
            if (fd.left != null && !fd.left.isStatic()) 
            {
                int rightSideOfAttachedWidget = fd.left.getAttachedWidget().getX() + fd.left.getAttachedWidget().getWidth() + fd.left.getOffset();
                
                if(fd.right == null) 
                	w.setX(rightSideOfAttachedWidget);
                else 
                {
                	setValidWidth(w, (w.getX() + w.getSize().getWidth()) - rightSideOfAttachedWidget);
                    w.setX(rightSideOfAttachedWidget);
                    
                }
            }
            
            if (fd.right != null && !fd.right.isStatic()) 
            {
                int leftSideofAttachedWidget = fd.right.getAttachedWidget().getX() + fd.right.getOffset();
                
                if(fd.left == null)
                    w.setX(leftSideofAttachedWidget - w.getSize().getWidth());
                else 
                {
                	setValidWidth(w, leftSideofAttachedWidget - w.getX());
                }
            }
            
            if (fd.bottom != null && !fd.bottom.isStatic()) 
            {
                int topSideOfAttachedWidget = fd.bottom.getAttachedWidget().getY() + 
                	fd.bottom.getAttachedWidget().getHeight() + fd.bottom.getOffset();
                
                if(fd.top == null) 
                {
                    w.setY(topSideOfAttachedWidget);
                    if(debug) System.out.println("upSideOfAttachedWidget 1: "+w);
                } 
                else 
                {
                	setValidHeight(w, (w.getY() + w.getSize().getHeight()) - topSideOfAttachedWidget);
                    w.setY(topSideOfAttachedWidget);
                    if(debug) System.out.println("upSideOfAttachedWidget 2: "+w);
                }
            }            
            
            if (fd.top != null && !fd.top.isStatic()) 
            {
            	int bottomSideOfAttachedWidget = fd.top.getAttachedWidget().getY() + fd.top.getOffset();
                if(fd.bottom == null)
                    w.setY(bottomSideOfAttachedWidget - w.getSize().getHeight());
                else 
                {
                	setValidHeight(w, bottomSideOfAttachedWidget-w.getY());
                }
            }
            
            //System.out.println("Layouted "+w);
        }
        sorted.clear();
        order.clear();
    }

	public Dimension computeMinSize(Container container, List<IWidget> list)
	{
    	// I can not calculate the min size in an exact manner...
    	// make sure that it is at least as big that 
    	// the biggest Widget fits in.
    	
    	int width = 0;
    	int height = 0;
    	
    	for(IWidget w: list) {
    		width = Math.max(getValidMinWidth(w), width);
    		height = Math.max(getValidMinHeight(w), height);
    	}
    	
		return new Dimension(width, height);
	}

	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
	}




}
