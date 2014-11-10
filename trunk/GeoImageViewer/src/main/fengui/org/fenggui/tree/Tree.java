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
 * $Id: Tree.java 171 2007-02-04 19:11:49Z schabby $
 */
package org.fenggui.tree;

import org.fenggui.ScrollContainer;
import org.fenggui.StandardWidget;
import org.fenggui.ToggableGroup;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.render.Pixmap;

/**
 * Widget to display tree a structure. The data to be displayed has to be provided by an <code>ITreeModel</code>
 * implementation. Note that at the current state of development, changes to the model require to set the entire model
 * new (setModel)/ See here http://sourceforge.net/tracker/index.php?func=detail&aid=1570998&group_id=178317&atid=884747
 * <br/> When the tree is used in a <code>ScrollContainer</code>, changes to the model may require to be commited
 * with a <code>layout</code> call in the scroll container.
 * 
 * @author Johannes Schaback aka Schabby, last edited by $Author: schabby $, $LastChangedDate: 2007-02-04 20:11:49 +0100 (Sun, 04 Feb 2007) $
 * @version $Revision: 171 $
 * @see ITreeModel
 */
public class Tree<E> extends StandardWidget
{
	private TreeAppearance appearance = null;
	/**
	 * the data model
	 */
	private ITreeModel<E> model = null;
	/**
	 * The root node of the tree
	 */
	private Record<E> root = null;
	
	private ToggableGroup<E> toggableWidgetGroup = new ToggableGroup<E>(1);



	@Override
	public void updateMinSize()
	{
		
		setMinSize(getAppearance().getMinSizeHint());
		
		if (getParent() != null && getParent() instanceof ScrollContainer)
		{
			ScrollContainer parent = (ScrollContainer) getParent();
			parent.layout();
		}
		else if (getParent() != null) getParent().updateMinSize();
	}

	/**
	 * Creates a new <code>Tree</code> instance with an empty data model.
	 * 
	 */
	public Tree()
	{
		this(null);
	}

	/**
	 * Creates a new <code>Tree</code> instance that visualized the given data model.
	 * 
	 * @param model
	 *            the data model
	 */
	public Tree(ITreeModel<E> model)
	{
		if (model != null) setModel(model);

		appearance = new TreeAppearance<E>(this);
		
		setupTheme(Tree.class);
		updateMinSize();

		if (getAppearance().getPlusIcon() == null || getAppearance().getMinusIcon() == null) 
		{ 
			throw new IllegalArgumentException(
				"plusIcon == null || minusIcon == null! Make sure you load the icons in your theme!"); 
		}
	}


	/**
	 * Returns the currently used data model.
	 * 
	 * @return the tree model
	 */
	public ITreeModel<E> getModel()
	{
		return model;
	}


	/**
	 * Sets the data model of the tree.
	 * 
	 * @param model
	 *            the data model.
	 */
	public void setModel(ITreeModel<E> model)
	{
		this.model = model;
		root = new Record<E>(model, model.getRoot());
		root.setExpandable(model.getNumberOfChildren(root.getNode()) > 0);
		updateMinSize();
	}


	@Override
	public void mousePressed(MousePressedEvent mp)
	{
		if(getModel() == null) return;
		
		Pixmap minusIcon = getAppearance().getMinusIcon();
		
		int row = (getAppearance().getContentHeight() - mp.getLocalY(this)) / getAppearance().getFont().getHeight();
		int x = mp.getLocalX(this);
		Record<E> r = findRecord(root, row);

		if (r != null)
		{
			// if clicked on the plus or minus icon
			if (x > r.getOffset() && x < r.getOffset() + minusIcon.getWidth())
			{
				if (r.getNumberOfChildren() == 0)
				{
					int n = model.getNumberOfChildren(r.getNode());
					for (int i = 0; i < n; i++)
					{
						Record<E> newRec = new Record<E>(model, model.getNode(r.getNode(), i));
						newRec.setExpandable(model.getNumberOfChildren(newRec.getNode()) > 0);
						newRec.setOffset(r.getOffset() + TreeAppearance.OFFSET);
						r.addChild(newRec);
					}
				}
				else
				{
					r.removeAllChildren();
				}
				updateMinSize();
			}
			else if (x > r.getOffset() + minusIcon.getWidth() + TreeAppearance.ICON_OFFSET
					&& x < r.getOffset() + minusIcon.getWidth() + TreeAppearance.ICON_OFFSET + getAppearance().getFont().getWidth(model.getText(r.getNode())))
			{
				toggableWidgetGroup.setSelected(r, true);
				r.setSelected(true);
				// r.isSelected = !r.isSelected;
			}
		}
	}


	private Record<E> findRecord(Record<E> node, int row)
	{
		if (node.row == row) return node;
		for (Record<E> r : node.getChildren())
		{
			Record<E> p = findRecord(r, row);
			if (p != null) return p;
		}
		return null;
	}

	


	public ToggableGroup<E> getToggableWidgetGroup()
	{
		return toggableWidgetGroup;
	}

	@Override
	public TreeAppearance getAppearance()
	{
		return appearance;
	}

	public Record<E> getRoot()
	{
		return root;
	}
}
