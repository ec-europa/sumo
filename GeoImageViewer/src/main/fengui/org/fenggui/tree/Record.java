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
 * Created on Dec 12, 2006
 * $Id: Record.java 114 2006-12-12 22:06:22Z schabby $
 */
package org.fenggui.tree;

import java.util.ArrayList;

import org.fenggui.IToggable;

public class Record<E> implements IToggable<E>
{

	private E node = null;
	private ArrayList<Record<E>> children = new ArrayList<Record<E>>();
	public int row = 0;
	private boolean isExpandable = false;
	private boolean isSelected = false;
	private ITreeModel<E> model = null;
	private int offset = 0;
	
	public Record(ITreeModel<E> model, E node)
	{
		this.node = node;
		this.model = model;
		if (node == null) throw new IllegalArgumentException("node == null");
	}


	public int getOffset()
	{
		return offset;
	}

	public Record<E> getChild(int index)
	{
		return children.get(index);
	}
	
	public void removeAllChildren()
	{
		children.clear();
	}
	
	public void addChild(Record<E> r)
	{
		children.add(r);
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}


	public void setExpandable(boolean isExpandable)
	{
		this.isExpandable = isExpandable;
	}

	public int getNumberOfChildren()
	{
		return children.size();
	}

	public Iterable<Record<E>> getChildren()
	{
		return children;
	}


	public boolean isExpandable()
	{
		return isExpandable;
	}


	public E getNode()
	{
		return node;
	}


	public boolean isSelected()
	{
		return isSelected;
	}


	public IToggable setSelected(boolean b)
	{
		isSelected = b;
		return this;
	}


	public E getValue()
	{
		return node;
	}


	public String getText()
	{
		return model.getText(node);
	}
}
