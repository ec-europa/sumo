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
 * Created on Jan 18, 2007
 * $Id: StandardTheme.java 323 2007-08-11 10:11:38Z Schabby $
 */
package org.fenggui.theme;

import org.fenggui.Button;
import org.fenggui.CheckBox;
import org.fenggui.ComboBox;
import org.fenggui.Container;
import org.fenggui.IWidget;
import org.fenggui.Label;
import org.fenggui.List;
import org.fenggui.ProgressBar;
import org.fenggui.RadioButton;
import org.fenggui.ScrollBar;
import org.fenggui.ScrollContainer;
import org.fenggui.Slider;
import org.fenggui.SnappingSlider;
import org.fenggui.SplitContainer;
import org.fenggui.TabItemLabel;
import org.fenggui.TextEditor;
import org.fenggui.VerticalList;
import org.fenggui.composites.Window;
import org.fenggui.console.Console;
import org.fenggui.menu.Menu;
import org.fenggui.menu.MenuBar;
import org.fenggui.table.Table;
import org.fenggui.tree.Tree;

/**
 * Theme class to define the appearance of the standard widgets 
 * (those widgets in <code>org.fenggui</code>).
 * 
 * @author kjuytr, last edited by $Author: Schabby $, $Date: 2007-08-11 12:11:38 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 323 $
 */
public abstract class StandardTheme implements ITheme
{

	public void setUp(IWidget widget)
	{
		if (widget instanceof Button) setUp((Button) widget);
		else if (widget instanceof Label) setUp((Label) widget);
		else if (widget instanceof ProgressBar)	setUp((ProgressBar) widget);
		else if (widget instanceof ScrollBar) setUp((ScrollBar) widget);
		else if (widget instanceof Tree) setUp((Tree) widget);
		else if (widget instanceof Slider) setUp((Slider) widget);
		else if (widget instanceof MenuBar) setUp((MenuBar) widget);
		else if (widget instanceof Menu) setUp((Menu) widget);
		else if (widget instanceof Table) setUp((Table) widget);
		else if (widget instanceof TextEditor) setUp((TextEditor) widget);
		else if (widget instanceof Window) setUp((Window) widget);
		else if (widget instanceof List) setUp((List) widget);
		else if (widget instanceof ComboBox) setUp((ComboBox) widget);
		else if (widget instanceof CheckBox) setUp((CheckBox) widget);
		else if (widget instanceof SplitContainer) setUp((SplitContainer) widget);
		else if (widget instanceof VerticalList) setUp((VerticalList) widget);
		else if (widget instanceof RadioButton) setUp((RadioButton) widget);
		else if (widget instanceof TabItemLabel) setUp((TabItemLabel) widget);
		else if (widget instanceof Console) setUp((Console) widget);
		else if (widget instanceof SnappingSlider) setUp((SnappingSlider) widget);
		else  setUpUnknown(widget);
	}

	public abstract void setUp(Button w);
	public abstract void setUp(CheckBox w);
	public abstract void setUp(RadioButton w);
	public abstract void setUp(TextEditor w);
	public abstract void setUp(Tree w);
	public abstract void setUp(Table w);
	public abstract void setUp(ComboBox w);
	public abstract void setUp(ScrollBar w);
	public abstract void setUp(Label w);
	public abstract void setUp(Window w);
	public abstract void setUp(Slider w);
	public abstract void setUp(ScrollContainer w);
	public abstract void setUp(SplitContainer w);
	public abstract void setUp(ProgressBar w);
	public abstract void setUp(Container w);
	public abstract void setUp(Menu w);
	public abstract void setUp(MenuBar w);
	public abstract void setUp(List w);
	public abstract void setUp(VerticalList w);
	public abstract void setUp(TabItemLabel w);
	public abstract void setUp(Console w);
	public abstract void setUp(SnappingSlider w);
	
	public abstract void setUpUnknown(IWidget w);
	
}
