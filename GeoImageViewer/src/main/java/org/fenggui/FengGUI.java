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
 * $Id: FengGUI.java 357 2007-09-20 16:09:10Z marcmenghin $
 */
package org.fenggui;

import org.fenggui.composites.Window;
import org.fenggui.menu.Menu;
import org.fenggui.menu.MenuBar;
import org.fenggui.menu.MenuItem;
import org.fenggui.render.Binding;
import org.fenggui.render.ITexture;
import org.fenggui.render.Pixmap;
import org.fenggui.table.ITableModel;
import org.fenggui.table.Table;
import org.fenggui.theme.DefaultTheme;
import org.fenggui.theme.ITheme;
import org.fenggui.util.Color;

/**
 * Helper factory for creating Widgets. This class is pretty much redundant by
 * now. All it does is to add the widget to be instantiated to the given parent
 * container.<br/> 
 * <br/>
 * It is likely that this class will vanish in the future.
 * 
 * @author Johannes Schaback, last edited by $Author: marcmenghin $, $Date: 2007-09-20 18:09:10 +0200 (Do, 20 Sep 2007) $
 * @version $Revision: 357 $
 */
public class FengGUI {

	public static final String VERSION = "Alpha 10";
	
	private static ITheme theme = new DefaultTheme();
	
	/**
	 * Creates a new progress bar.
	 * @param parent the parent container 
	 * @return new progress bar
	 */
	public static ProgressBar createProgressBar(IContainer parent) {
		ProgressBar btn = new ProgressBar();
		//if(theme != null) theme.setUp(btn);
		parent.addWidget(btn);
		return btn;
	}	
	
	/**
	 * Creates a new combo box.
	 * @param parent the parent container
	 * @return new combo box
	 */
	public static ComboBox createComboBox(IContainer parent) {
		ComboBox btn = new ComboBox();
		//if(theme != null) theme.setUp(btn);
		parent.addWidget(btn);
		return btn;
	}
	
	public static Button createButton(IContainer parent) {
		Button btn = new Button();
		//if(theme != null) theme.setUp(btn);
		parent.addWidget(btn);
		return btn;
	}
	
	/**
	 * Creates a new Display object. Note that you usually have only one
	 * Display object at run-time and that the Display has no AppearanceAdapter.
	 * @param binding the OpenGL binding that is used to render the UI
	 * @return the new Display object.
	 */
	public static Display createDisplay(Binding binding) {
		Display btn = new Display(binding);
		//if(theme != null) theme.setUp(btn);
		return btn;
	}	
	
	/**
	 * Creates a new container. Uses <code>CONTAINER</code>
	 * as the appearance identifier.
	 * @param parent the parent container
	 * @return new container
	 */
	public static Container createContainer(IContainer parent) {
		Container c = new Container();
		//if(theme != null) theme.setUp(c);
		parent.addWidget(c);
		return c;
	}	
	
	/**
	 * Creates a new button.
	 * @param parent parent container
	 * @param text the text on the button
	 * @return new button
	 */
	public static Button createButton(IContainer parent, String text) 
	{
		Button btn = new Button(text);
		//if(theme != null) theme.setUp(btn);
		parent.addWidget(btn);
		return btn;
	}	
	
	public static Button createButton(String text) 
	{
		Button btn = new Button(text);
		//if(theme != null) theme.setUp(btn);
		return btn;
	}
	
	/**
	 * Creates a new button.
	 * @param parent the parent container
	 * @param image an image on the button
	 * @return new button
	 */
	public static Button createButton(IContainer parent, Pixmap image) 
	{
		Button btn = createButton(parent);
		btn.setPixmap(image);
		return btn;
	}	
	
	/**
	 * Creates a new radio button. 
	 * @param parent the parent container
	 * @param group the button group that manages the mutual exclusive selection
	 * @return the new radio button
	 */
	@SuppressWarnings("unchecked")
	public static RadioButton createRadioButton(IContainer parent, ToggableGroup group) {
		RadioButton btn = new RadioButton(group);
		//if(theme != null) theme.setUp(btn);
		parent.addWidget(btn);
		return btn;
	}

	/**
	 * Creates a new radio button.
	 * @param parent the parent container
	 * @param text the text displayed on the radio button
	 * @return the new radio button
	 */
	public static RadioButton createRadioButton(IContainer parent, String text) {
		RadioButton btn = createRadioButton(parent, (ToggableGroup)null);
		btn.setText(text);
		return btn;
	}	
	
	/**
	 * Creates a new radio button.
	 * @param parent the parent container
	 * @param text the text displayed beside the radio button
	 * @param group the button group that manages the mutual exclusive selection
	 * @return the new radio button
	 */
	@SuppressWarnings("unchecked")
	public static RadioButton createRadioButton(IContainer parent, String text, ToggableGroup group) {
		RadioButton btn = createRadioButton(parent, group);
		btn.setText(text);
		btn.setRadioButtonGroup(group);
		return btn;
	}	
	
	/**
	 * Creates a new check box.
	 * @param parent the parent container
	 * @return the new check box
	 */
	public static CheckBox createCheckBox(IContainer parent) {
		CheckBox btn = new CheckBox();
		//if(theme != null) theme.setUp(btn);
		parent.addWidget(btn);
		return btn;
	}	
	
	/**
	 * Creates a new check box. 
	 * @param parent the parent container
	 * @param text the text displayed at the side of the check box
	 * @return the new check box
	 */
	public static CheckBox createCheckBox(IContainer parent, String text) {
		CheckBox btn = createCheckBox(parent);
		btn.setText(text);
		return btn;
	}	
	
	/**
	 * Creates a new label.
	 * @param parent the parent container
	 * @return new label
	 */
	public static Label createLabel(IContainer parent) {
		Label l = new Label();
		//setUpAppearance(l);
		parent.addWidget(l);
		return l;
	}
	

	/**
	 * Creates a new multiline label
	 * @return Returns a new multiline label
	 */
	public static MultiLineLabel createMultiLineLabel(IContainer parent) {
		MultiLineLabel l = new MultiLineLabel();
		//setUpAppearance(l);
		parent.addWidget(l);
		return l;
	}
	
	
	/**
	 * Creates a new item for list Widgets.
	 * @param parent the parent container
	 * @return new list item
	 */
	@SuppressWarnings("unchecked")
	public static ListItem createListItem(List parent) {
		ListItem btn = new ListItem();
		parent.addItem(btn);
		return btn;
	}	
	
	/**
	 * Creates a new menu bar.
	 * @param parent the parent container
	 * @return new menu bar
	 */
	public static MenuBar createMenuBar(IContainer parent) {
		MenuBar menuBar = new MenuBar();
		//if(theme != null) theme.setUp(menuBar);
		parent.addWidget(menuBar);
		return menuBar;
	}	
	
	/**
	 * Creates a new menu associated with a menu bar.
	 * @param parent the parent MenuBar
	 * @return new menu
	 */
	public static Menu createMenu(Display parent, boolean display) {
		Menu menu = new Menu();
		//if(theme != null) theme.setUp(menu);
		if(display) parent.addWidget(menu);
		return menu;
	}	
	
	/**
	 * Creates a new menu associated with a menu bar.
	 * @param parent the parent MenuBar
	 * @return new menu
	 */ 
	public static Menu createMenu(Menu parent, String name, boolean display) {
		Menu menu = createMenu(parent.getDisplay(), display);
		parent.registerSubMenu(menu, name);
		return menu;
	}	
	
	public static Menu createMenu(MenuBar parent, String name, boolean display) {
		Menu menu = createMenu(parent.getDisplay(), display);
		parent.registerSubMenu(menu, name);
		return menu;
	}
		
	/**
	 * Creates a new menu item in a menu.
	 * @param parent the parent menu.
	 * @return new menu item
	 */
	public static MenuItem createMenuItem(Menu parent, String name) {
		MenuItem item = new MenuItem(name);
		parent.addItem(item);
		item.getTextRenderer().setFont(parent.getAppearance().getFont());
		return item;
	}
	
	
	/**
	 * Creates a new list Widget.
	 * @param parent the parent container
	 * @return new list
	 */
	public static List createList(IContainer parent) 
	{
		List btn = new List();
		//if(theme != null) theme.setUp(btn);
		parent.addWidget(btn);
		return btn;
	}	
	
	/**
	 * Creates a new label.
	 * @param parent parent container
	 * @param text the text the label shall display.
	 * @return new label
	 */
	public static Label createLabel(IContainer parent, String text) {
		Label btn = new Label();
		//if(theme != null) theme.setUp(btn);
		btn.setText(text);
		parent.addWidget(btn);
		return btn;
	}	

	public static Label createLabel(String text) 
	{
		Label btn = new Label(text);
		//if(theme != null) theme.setUp(btn);
		return btn;
	}
	
	/**
	 * Creates a new label.
	 * @param parent the parent container
	 * @param image the image to be displayed
	 * @return new label
	 */
	public static Label createLabel(IContainer parent, ITexture image) {
		Label btn = createLabel(parent);
		btn.setPixmap(new Pixmap(image));
		return btn;
	}
	
	/**
	 * Creates a new label.
	 * @param parent the parent container
	 * @param pixmap the image to be displayed
	 * @return new label
	 */
	public static Label createLabel(IContainer parent, Pixmap pixmap) {
		Label btn = createLabel(parent);
		btn.setPixmap(pixmap);
		return btn;
	}
	
	/**
	 * Creates a new label. 
	 * @param parent the parent container
	 * @param text the text to be dislayed
	 * @param image the image to be displayed
	 * @return new label
	 */
	public static Label createLabel(IContainer parent, String text, ITexture image) {
		Label label = createLabel(parent, text);
		label.setPixmap(new Pixmap(image));
		return label;
	}	
	
	/**
	 * Creates a new label.
	 * @param parent the parent container
	 * @param text the text to drawn with the label
	 * @param textColor the color of the text of the label
	 * @return new label
	 */
	public static Label createLabel(IContainer parent, String text, Color textColor) {
		Label label = createLabel(parent, text);
		label.getAppearance().setTextColor(textColor);
		return label;
	}	
	
	/**
	 * Creates a new window.
	 * @param parent the Display
	 * @param closeBtn whether the window has close button or not
	 * @param maxBtn whether the window has a maximize button or not
	 * @param minBtn whether the windows has a minimizae button or not
	 * @return new window
	 */
	public static Window createWindow(Display parent, boolean closeBtn, boolean maxBtn, boolean minBtn, boolean autoclose) {
		Window frame = new Window(closeBtn, maxBtn, minBtn, autoclose);
		//if(theme != null) theme.setUp(frame);
		parent.addWidget(frame);
		return frame;		
	}
	
	/**
	 * Creates a new a new frame. A frame is a window with a close button,
	 * a minimize button and a maximize button.
	 * @param parent the Display
	 * @param text the title of the frame
	 * @return the new frame
	 */
	public static Window createFrame(Display parent, String text, boolean autoclose) {
		Window frame = new Window(true, true, true, autoclose);
		//if(theme != null) theme.setUp(frame);
		frame.setTitle(text);
		parent.addWidget(frame);
		return frame;
	}	
	
	/**
	 * Creates a new Dialog. A Dialog is a Window that only has a close
	 * button.
	 * @param parent the Display
	 * @return new Dialog
	 */
	public static Window createDialog(Display parent) {
		Window frame = new Window(true, false, false, true);
		//if(theme != null) theme.setUp(frame);
		parent.addWidget(frame);
		return frame;
	}	
	
	/**
	 * Creates a new Dialog. A Dialog is a Window that has only a close button.
	 * @param parent the Display
	 * @param title the title of the Dialog
	 * @return new Dialog
	 */
	public static Window createDialog(Display parent, String title) {
		Window frame = createDialog(parent);
		frame.setTitle(title);
		return frame;
	}	
	
	/**
	 * Creates a new Slider.
	 * @param parent the parent container
	 * @param horizontal whether the Slider lays horizontal or vertical
	 * @return new Slider
	 */
	public static Slider createSlider(IContainer parent, boolean horizontal) {
		Slider s = new Slider(horizontal);
		//if(theme != null) theme.setUp(s);
		parent.addWidget(s);
		return s;
	}	

	/**
	 * Creates a new ScrollContainer.
	 * @param parent the parent container.
	 * @return new ScrollContainer
	 */
	public static ScrollContainer createScrollContainer(IContainer parent) {
		ScrollContainer c = new ScrollContainer();
		//if(theme != null) theme.setUp(c);
		parent.addWidget(c);
		return c;
	}
	
	/**
	 * Creates a new TextArea.
	 * @param parent the parent container
	 * @return new TextArea
	 */
	public static TextEditor createTextArea(IContainer parent) {
		TextEditor c = new TextEditor();
		//if(theme != null) theme.setUp(c);
		parent.addWidget(c);
		return c;
	}	
	
	/**
	 * Creates a new TextArea.
	 * @param parent the parent container
	 * @param text the text inside of the TextArea
	 * @return new TextArea
	 */
	public static TextEditor createTextArea(IContainer parent, String text) {
		TextEditor c = createTextArea(parent);
		c.setText(text);
		return c;
	}	
	
	/**
	 * Creates a new plain Canvas.
	 * @param parent the parent container
	 * @return new Canvas
	 */
	public static Canvas createCanvas(IContainer parent) {
		Canvas w = new Canvas();
		//if(theme != null) theme.setUp(w);
		parent.addWidget(w);
		return w;
	}	
	
	/**
	 * Creates a new plain Widget.
	 * @param parent the parent container
	 * @return new Widget
	 */
	public static IWidget createWidget(IContainer parent) {
		Widget w = new Widget();
		//if(theme != null) theme.setUp(w);
		parent.addWidget(w);
		return w;
	}	
	
	/**
	 * Creates a new ScrollBar.
	 * @param parent the parent container
	 * @param horizontal whether the ScrollBar is horizontal or vertical
	 * @return new ScrollBar
	 */
	public static ScrollBar createScrollBar(IContainer parent, boolean horizontal) {
		ScrollBar c = new ScrollBar(horizontal);
		//if(theme != null) theme.setUp(c);
		parent.addWidget(c);
		return c;
	}

	/**
	 * Creates a new Table.
	 * @param parent the parent container
	 * @return new Table
	 */
	public static Table createTable(IContainer parent) {
		Table table = new Table();
		//if(theme != null) theme.setUp(table);
		parent.addWidget(table);
		return table;
	}

	/**
	 * Creates a new Table.
	 * @param parent the parent container
	 * @param model the model used to provide the table with data
	 * @return new Table
	 */
	public static Table createTable(IContainer parent, ITableModel model) {
		Table table = createTable(parent);
		table.setModel(model);
		return table;
	}
	
	/**
	 * Creates a new TextField.
	 * @param parent parent container
	 * @return new text field
	 */
	public static TextEditor createTextField(IContainer parent) {
		TextEditor tf = new TextEditor(false);
		//if(theme != null) theme.setUp(tf);
		parent.addWidget(tf);
		return tf;
	}
	
	/**
	 * Creates a new TextField.
	 * @param parent parent container
	 * @param text text set withing text field
	 * @return new text field
	 */
	public static TextEditor createTextField(IContainer parent, String text) {
		TextEditor td = createTextField(parent);
		td.setText(text);
		return td;
	}
	
	public static SplitContainer createSplitContainer(IContainer parent, boolean horizontal)
	{
		SplitContainer sc = new SplitContainer(horizontal);
		//if(theme != null) theme.setUp(sc);
		parent.addWidget(sc);
		return sc;
	}
	
	public static SplitContainer createSplitContainer(boolean horizontal)
	{
		SplitContainer sc = new SplitContainer(horizontal);
		//if(theme != null) theme.setUp(sc);
		return sc;
	}
	
	/**
	 * Creates a new ViewPort.
	 * @param parent the parent container
	 * @return new view port
	 */
	public static ViewPort createViewPort(IContainer parent) {
		ViewPort p = new ViewPort();
		//if(theme != null) theme.setUp(p);
		parent.addWidget(p);
		return p;
	}

	public static VerticalList createVerticalList()
	{
		VerticalList v = new VerticalList();
		//setUpAppearance(v);
		return v;
	}
	
	public static ITheme getTheme() {
		return theme;
	}

	public static void setTheme(ITheme theme) {
		FengGUI.theme = theme;
	}

	/**
	 * Sets up the the appearance of a Widget. It first calls <code>initAppearance</code>
	 * and then applies the theme (if available).
	 * 
	 * @param toBeSetUp the Widget to be set up
	 * @return the Widget
	 */
	public static IWidget setUpAppearance(Widget toBeSetUp) {
		if(theme != null) theme.setUp(toBeSetUp);
		return toBeSetUp;
	}
}
