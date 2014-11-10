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
 * $Id: LeftTitleBarWindow.java 264 2007-04-16 12:34:05Z bbeaulant $
 */

package org.fenggui.contrib;

import org.fenggui.Container;
import org.fenggui.FengGUI;
import org.fenggui.RotatedLabel;
import org.fenggui.background.PlainBackground;
import org.fenggui.border.PlainBorder;
import org.fenggui.composites.Window;
import org.fenggui.layout.Alignment;
import org.fenggui.layout.BorderLayout;
import org.fenggui.layout.BorderLayoutData;
import org.fenggui.layout.RowLayout;
import org.fenggui.util.Color;

/**
 * A window with the title bar on the left (without title ;) )
 * 
 * @author Boris Beaulant
 */
public class LeftTitleBarWindow extends Window
{

	/**
	 * Creates a new LeftBarWindow on the display.
	 */
	public LeftTitleBarWindow()
	{
		super();
	}


	/**
	 * Creates a new LeftBarWindow on the display.
	 * 
	 * @param closeBtn
	 * @param maximizeBtn
	 * @param minimizeBtn
	 */
	public LeftTitleBarWindow(boolean closeBtn, boolean maximizeBtn, boolean minimizeBtn, boolean autoClose)
	{
		super(closeBtn, maximizeBtn, minimizeBtn, autoClose);

		setupTheme(Window.class);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fenggui.composites.Window#build(boolean, boolean, boolean)
	 */
	@Override
	protected void build(boolean closeBtn, boolean maximizeBtn, boolean minimizeBtn)
	{
		titleBar = FengGUI.createContainer(this);

		setLayoutManager(new BorderLayout());

		content = new Container();
		((Container)content).setLayoutData(BorderLayoutData.CENTER);
		addWidget(content);

		titleBar.setLayoutData(BorderLayoutData.WEST);

		buildTitleBar(closeBtn, maximizeBtn, minimizeBtn);

		setSize(100, 120);
		getAppearance().add(new PlainBackground(new Color(1, 1, 1, 0.8f)));
		getAppearance().add(new PlainBorder(Color.GRAY));
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fenggui.composites.Window#buildTitleBar(boolean, boolean,
	 *      boolean)
	 */
	@Override
	protected void buildTitleBar(boolean closeBtn, boolean maximizeBtn, boolean minimizeBtn)
	{
		titleBar.setLayoutManager(new RowLayout(false));

		if (closeBtn)
		{
			buildCloseButton();
		}

		if (minimizeBtn)
		{
			buildMinimizeButton();
		}

		if (maximizeBtn)
		{
			buildMaximizeButton();
		}

		// Create a vertical Label
		title = new RotatedLabel(90);
		title.getAppearance().setAlignment(Alignment.BOTTOM);
		titleBar.addWidget(title);
	}

}
