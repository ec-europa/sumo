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
 * Created on Feb 3, 2007
 * $Id: GUIInspector.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.composites;

import java.io.ByteArrayOutputStream;

import org.fenggui.Button;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.IWidget;
import org.fenggui.Label;
import org.fenggui.SplitContainer;
import org.fenggui.StandardWidget;
import org.fenggui.border.PlainBorder;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.event.ISelectionChangedListener;
import org.fenggui.event.SelectionChangedEvent;
import org.fenggui.layout.BorderLayout;
import org.fenggui.layout.BorderLayoutData;
import org.fenggui.layout.GridLayout;
import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.GlobalContextHandler;
import org.fenggui.theme.xml.XMLOutputStream;
import org.fenggui.tree.ITreeModel;
import org.fenggui.tree.Tree;
import org.fenggui.util.Color;

public class GUIInspector extends Window
{
	private SplitContainer splitContainer = new SplitContainer(false);
	private Tree<IWidget> tree = new Tree<IWidget>();
	private TextArea textArea = new TextArea();
	private Label sizeLabel = new Label();
	private Label minSizeLabel = new Label();
	private Label positionLabel = new Label();
	private Label resizeLabel = new Label();
	
	public GUIInspector()
	{
		super(true, false, false, true);
		
		
		setContentContainer(splitContainer);
		Container treeContainer = new Container(new BorderLayout()); 
		treeContainer.addWidget(tree);
		tree.setLayoutData(BorderLayoutData.CENTER);
		splitContainer.setFirstWidget(treeContainer);
		splitContainer.setValue(100);
		Button updateButton = new Button("Update");
		treeContainer.addWidget(updateButton);
		updateButton.setLayoutData(BorderLayoutData.SOUTH);
		
		Container labelContainer = new Container(new GridLayout(2, 4));
		labelContainer.addWidget(new Label("Size:"));
		labelContainer.addWidget(sizeLabel);
		labelContainer.addWidget(new Label("MinSize:"));
		labelContainer.addWidget(minSizeLabel);
		labelContainer.addWidget(new Label("Position:"));
		labelContainer.addWidget(positionLabel);
		labelContainer.addWidget(new Label("resize:"));
		labelContainer.addWidget(resizeLabel);
		
		Container dataContainer = new Container(new BorderLayout());
		dataContainer.addWidget(labelContainer);
		labelContainer.setLayoutData(BorderLayoutData.NORTH);
		textArea.setLayoutData(BorderLayoutData.CENTER);
		dataContainer.addWidget(textArea);
		splitContainer.setSecondWidget(dataContainer);
		tree.getAppearance().add(new PlainBorder(Color.BLACK));
		
		setupTheme(GUIInspector.class);
		setTitle("GUI Inspector");
		setSize(400, 400);
		updateButton.addButtonPressedListener(new IButtonPressedListener(){

			public void buttonPressed(ButtonPressedEvent e)
			{
				update();
			}});
		
		tree.getToggableWidgetGroup().addSelectionChangedListener(new ISelectionChangedListener(){

			public void selectionChanged(SelectionChangedEvent selectionChangedEvent)
			{
				if(!selectionChangedEvent.isSelected()) return;
				IWidget source = (IWidget) selectionChangedEvent.getToggableWidget().getValue();
				setupDetails(source);
			}});
		
	}
	
	private void setupDetails(IWidget w)
	{
		minSizeLabel.setText(w.getMinSize()+"");
		positionLabel.setText(w.getX()+", "+w.getY());
		resizeLabel.setText(w.isExpandable()+" "+w.isShrinkable());
		sizeLabel.setText(w.getSize()+"");
		textArea.setText(w.toString());
		
		if(w instanceof StandardWidget)
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final XMLOutputStream os = new XMLOutputStream(bos, w.getClass().
					getSimpleName(), new GlobalContextHandler());
			try
			{
				((StandardWidget)w).process(os);
				os.close();
				textArea.appendText("\n\n"+os.getDocument().toXML());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	private void update()
	{
		tree.setModel(new WidgetTreeModel());
		layout();
	}
	
	class WidgetTreeModel implements ITreeModel<IWidget>
	{

		public IWidget getNode(IWidget parent, int index)
		{
			if(parent instanceof Container || parent instanceof Display)
			{
				return ((Container)parent).getWidget(index);
			}

			return null;
		}

		public int getNumberOfChildren(IWidget node)
		{
			if(node instanceof Container || node instanceof Display)
			{
				return ((Container)node).size();
			}
			
			return 0;
		}

		public Pixmap getPixmap(IWidget node)
		{
			return null;
		}

		public IWidget getRoot()
		{
			return getDisplay();
		}

		public String getText(IWidget node)
		{
			return node.getClass().getSimpleName();
		}
		
	}
}
