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
 * Created on Jul 15, 2005
 * $Id: Everything.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui.example;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.fenggui.Button;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.IWidget;
import org.fenggui.ScrollContainer;
import org.fenggui.TextEditor;
import org.fenggui.Widget;
import org.fenggui.composites.GUIInspector;
import org.fenggui.composites.MessageWindow;
import org.fenggui.composites.Window;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.DisplayResizedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.event.IDisplayResizedListener;
import org.fenggui.event.IMenuItemPressedListener;
import org.fenggui.event.MenuItemPressedEvent;
import org.fenggui.layout.BorderLayout;
import org.fenggui.layout.BorderLayoutData;
import org.fenggui.layout.RowLayout;
import org.fenggui.layout.StaticLayout;
import org.fenggui.menu.Menu;
import org.fenggui.menu.MenuBar;
import org.fenggui.menu.MenuItem;
import org.fenggui.render.Binding;
import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.theme.DefaultTheme;
import org.fenggui.theme.ITheme;
import org.fenggui.theme.XMLTheme;
import org.fenggui.theme.xml.GlobalContextHandler;
import org.fenggui.theme.xml.XMLInputStream;
import org.fenggui.theme.xml.XMLOutputStream;
import org.fenggui.util.fonttoolkit.FontFactory;

/**
 * Builds a simple frame with an ugly layout. Is mainly used to testing
 * purposes. This class will either disappear in the near future or will serves
 * as a realy simple entry point for newbies.
 * 
 * @todo Comment this class... #
 * @todo "The plates don't show up, I don't know 
 * what they are but I don't see anything." #
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public class Everything implements IExample
{
	private Display display = null;
	private boolean runAsWebstart = false;

	public Everything()
	{
		this(false);
	}

	public Everything(boolean runAsWebstart)
	{
		this.runAsWebstart = runAsWebstart;
	}

	public void registerExample(final IExample example, Menu parent, boolean activate)
	{
		MenuItem item = new MenuItem(example.getExampleName());
		parent.addItem(item);

		item.setEnabled(activate);

		item.addMenuItemPressedListener(new IMenuItemPressedListener()
		{

			public void menuItemPressed(MenuItemPressedEvent menuItemPressedEvent)
			{
				buildExampleGUIinRenderThread(example);
			}

		});

	}

	private void buildTextMenu(MenuBar menuBar)
	{
		Menu textMenu = new Menu();
		menuBar.registerSubMenu(textMenu, "Text");
		
		registerExample(new TextFieldExample(), textMenu, true);
		registerExample(new TextAreaExample(), textMenu, true);
		registerExample(new TextViewExample(), textMenu, true);
		registerExample(new MultiLineLabelExample(), textMenu, true);
		registerExample(new LabelExample(), textMenu, true);
		registerExample(new FontExample(), textMenu, true);
	}
	
	private void buildBasicWidgetsMenu(MenuBar menuBar)
	{
		Menu widgetMenu = new Menu();
		
		menuBar.registerSubMenu(widgetMenu, "Widgets");
		
		registerExample(new ListExample(), widgetMenu, true);
		registerExample(new ComboBoxExample(), widgetMenu, true);
		registerExample(new ButtonExample(), widgetMenu, true);
		registerExample(new CheckBoxExample(), widgetMenu, true);
		registerExample(new ProgressBarExample(), widgetMenu, true);
		registerExample(new SliderExample(), widgetMenu, true);
		registerExample(new RadioButtonExample(), widgetMenu, true);
		registerExample(new TableExample(), widgetMenu, true);
		registerExample(new TableExample2(), widgetMenu, true);
		registerExample(new ScrollBarExample(), widgetMenu, true);
		registerExample(new TreeExample(), widgetMenu, true);
		registerExample(new VerticalListExample(), widgetMenu, true);
	}
	
	private void buildContainersMenu(MenuBar menuBar)
	{
		Menu containerMenu = new Menu();
		
		menuBar.registerSubMenu(containerMenu, "Containers");
		registerExample(new SplitContainerExample(), containerMenu, true);
		registerExample(new ScrollContainerExample(), containerMenu, true);
		registerExample(new TabContainerExample(), containerMenu, true);
		registerExample(new ScrollContainerExample(), containerMenu, true);
	}
	
	private void buildMiscMenu(MenuBar menuBar)
	{
		Menu miscMenu = new Menu();
		
		menuBar.registerSubMenu(miscMenu, "Misc");
		registerExample(new ClippingExample(), miscMenu, true);
		registerExample(new CursorExample(), miscMenu, true);
		registerExample(new SnappingSliderExample(), miscMenu, true);
		
		Menu submen1Menu = new Menu();
		miscMenu.registerSubMenu(submen1Menu, "Submenus");
		submen1Menu.addItem(new MenuItem("Submenu Item 1", false));
		submen1Menu.addItem(new MenuItem("Submenu Item 2", false));
		submen1Menu.addItem(new MenuItem("Submenu Item 3", false));
		Menu submen2Menu = new Menu();
		submen1Menu.registerSubMenu(submen2Menu, "Another Submenu");
		submen2Menu.addItem(new MenuItem("Subsubmenu Item 1", false));
		submen2Menu.addItem(new MenuItem("Subsubmenu Item 2", false));
		submen2Menu.addItem(new MenuItem("Subsubmenu Item 3", false));
		submen2Menu.addItem(new MenuItem("Subsubmenu Item 4", false));
		submen1Menu.addItem(new MenuItem("Submenu Item 4", false));
		
		registerExample(new ConnectionWindowExample(), miscMenu, true);
		registerExample(new FPSLabelExample(), miscMenu, true);
		registerExample(new TextRendererExample(), miscMenu, true);
		registerExample(new ConsoleExample(), miscMenu, true);
		registerExample(new SVGExample(), miscMenu, true);
		registerExample(new PixmapDecoratorExample(), miscMenu, true);
		
		Menu layoutMenu = new Menu();
		miscMenu.registerSubMenu(layoutMenu, "Layouts");

		registerExample(new GridLayoutExample(), layoutMenu, true);
		registerExample(new LayoutExample(), layoutMenu, true);

		Menu decoratorMenu = new Menu();
		miscMenu.registerSubMenu(decoratorMenu, "Decorators");

		
		registerExample(new BorderTest(), decoratorMenu, true);
		registerExample(new PixmapBorderExample(), decoratorMenu, true);
		
		registerExample(new GameMenuExample(), miscMenu, true);
		
		MenuItem guiInspectorItem = new MenuItem("GUI Inspector");
		miscMenu.addItem(guiInspectorItem);
		guiInspectorItem.addMenuItemPressedListener(new IMenuItemPressedListener(){

			public void menuItemPressed(MenuItemPressedEvent menuItemPressedEvent)
			{
				GUIInspector inspector = new GUIInspector();
				inspector.layout();
				display.addWidget(inspector);
			}});
		
	}
	
	private void buildHelpMenu(MenuBar menuBar)
	{
		Menu helpMenu = new Menu();
		menuBar.registerSubMenu(helpMenu, "Help");
		MenuItem about = new MenuItem("About");
		helpMenu.addItem(about);
		
		about.addMenuItemPressedListener(new IMenuItemPressedListener() {

			public void menuItemPressed(MenuItemPressedEvent menuItemPressedEvent)
			{
				String t = "FengGUI V" + org.fenggui.FengGUI.VERSION;
				MessageWindow mw = new MessageWindow(t);
				mw.setTitle("About");
				mw.updateMinSize();
				mw.setSize(mw.getMinWidth() + 100, mw.getMinHeight());
				mw.layout();
				display.addWidget(mw);
				StaticLayout.center(mw, display);
			}
			
		});
	}
	
	private void buildThemeMenu(MenuBar menuBar)
	{
		Menu themeMenu = new Menu();
		menuBar.registerSubMenu(themeMenu, "Theme");
		
		MenuItem standardThemeItem = new MenuItem("Ugly Standard Theme");
		themeMenu.addItem(standardThemeItem);
		standardThemeItem.setEnabled(!runAsWebstart);

		MenuItem qtCurveThemeItem = new MenuItem("QtCurve");
		themeMenu.addItem(qtCurveThemeItem);
		qtCurveThemeItem.setEnabled(!runAsWebstart);

		MenuItem esaTheme = new MenuItem("Esa's Theme", false);
		themeMenu.addItem(esaTheme);

		standardThemeItem.addMenuItemPressedListener(new IMenuItemPressedListener() {

			public void menuItemPressed(MenuItemPressedEvent menuItemPressedEvent)
			{
				loadTheme(null);
			}
			
		});
		
		qtCurveThemeItem.addMenuItemPressedListener(new IMenuItemPressedListener() {
			
			public void menuItemPressed(MenuItemPressedEvent menuItemPressedEvent)
			{
				loadTheme("data/themes/QtCurve/QtCurve.xml");
			}
			
		});
		
		esaTheme.addMenuItemPressedListener(new IMenuItemPressedListener() {
			
			public void menuItemPressed(MenuItemPressedEvent menuItemPressedEvent)
			{
				loadTheme("data/themes/Esa/theme.xml");
			}
			
		});
	}
	
	
	private void buildProgramMenu(final MenuBar menuBar)
	{
		Menu programMenu = new Menu();
		menuBar.registerSubMenu(programMenu, "Program");
		MenuItem clearScreenItem = new MenuItem("Clear Display");
		programMenu.addItem(clearScreenItem);

		if(!runAsWebstart)
		{
			programMenu.addItem(buildXMLDumper());
		}
		
		MenuItem exitItem = new MenuItem("Exit");
		programMenu.addItem(exitItem);


		// register "Exit" item to quit the app
		exitItem.addMenuItemPressedListener(new IMenuItemPressedListener() {

			public void menuItemPressed(MenuItemPressedEvent menuItemPressedEvent)
			{
				quit();
			}
			
		});

		// remove everything except the MenuBar from the display
		clearScreenItem.addMenuItemPressedListener(new IMenuItemPressedListener() {

			public void menuItemPressed(MenuItemPressedEvent menuItemPressedEvent)
			{
				ArrayList<IWidget> toBeRemoved = new ArrayList<IWidget>();
				for (IWidget w : display.getWidgets())
				{
					if (!w.equals(menuBar)) toBeRemoved.add(w);
				}
				display.removeWidgets(toBeRemoved);
			}
			
		});
	}
	
	private void buildMenuBar()
	{
		final MenuBar menuBar = new MenuBar();
		display.addWidget(menuBar);

		buildProgramMenu(menuBar);
		buildBasicWidgetsMenu(menuBar);
		buildTextMenu(menuBar);
		buildContainersMenu(menuBar);
		buildMiscMenu(menuBar);
		buildThemeMenu(menuBar);
		buildHelpMenu(menuBar);
		
		// position MenuBar in Display
		menuBar.updateMinSize(); // we have not layouted anything yet...
		menuBar.setX(0);
		menuBar.setY(display.getHeight() - menuBar.getMinHeight());
		menuBar.setSize(display.getWidth(), menuBar.getMinHeight());
		menuBar.setShrinkable(false);

		// if the OpenGL screen is resized, put the MenuBar back in place
		Binding.getInstance().addDisplayResizedListener(new IDisplayResizedListener() {

			public void displayResized(DisplayResizedEvent displayResizedEvent)
			{
				menuBar.setX(0);
				menuBar.setY(displayResizedEvent.getHeight() - menuBar.getMinHeight());
				menuBar.setSize(displayResizedEvent.getWidth(), menuBar.getMinHeight());
			}
			
		});
	}

	private Font teletype = null;
	
	private MenuItem buildXMLDumper()
	{
		MenuItem xmlDump = new MenuItem("XML Dump");
		
		xmlDump.addMenuItemPressedListener(new IMenuItemPressedListener(){

			public void menuItemPressed(MenuItemPressedEvent menuItemPressedEvent)
			{
				if(teletype == null) teletype = FontFactory.renderStandardFont(new java.awt.Font("Monospace", java.awt.Font.PLAIN, 11));
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				final XMLOutputStream os = new XMLOutputStream(bos, "Display",
						new GlobalContextHandler());
				try
				{
					display.process(os);
					os.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				Window w = new Window();
				w.getContentContainer().setLayoutManager(new BorderLayout());
				ScrollContainer sc = new ScrollContainer();
				sc.setLayoutData(BorderLayoutData.CENTER);
				final TextEditor te = new TextEditor(true);
				sc.setInnerWidget(te);
				te.getAppearance().setFont(teletype);
				w.getContentContainer().addWidget(sc);
				te.setText(os.getDocument().toXML());
				
				Container btnContainer = new Container(new RowLayout(true));
				Button dumpToConsole = new Button("Print on System.out");
				Button clear = new Button("Clear");
				Button apply = new Button("Apply on Display");
				btnContainer.addWidget(dumpToConsole);
				btnContainer.addWidget(clear);
				btnContainer.addWidget(apply);
				w.getContentContainer().addWidget(btnContainer);
				btnContainer.setLayoutData(BorderLayoutData.SOUTH);
				dumpToConsole.addButtonPressedListener(new IButtonPressedListener(){
					public void buttonPressed(ButtonPressedEvent e)
					{
						System.out.println(te.getText());
					}});
				clear.addButtonPressedListener(new IButtonPressedListener(){
					public void buttonPressed(ButtonPressedEvent e)
					{
						te.setText("");
					}});
				apply.addButtonPressedListener(new IButtonPressedListener(){
					public void buttonPressed(ButtonPressedEvent e)
					{
						try
						{
							XMLInputStream xis = new XMLInputStream(new ByteArrayInputStream(te.getText().getBytes()));
							display.process(xis);
							xis.close();
							display.layout();
						}
						catch (Exception e1)
						{
							e1.printStackTrace();
						}
						//System.out.println(os.getDocument().toXML());
					}});
				
				w.setSize(500, 500);
				w.setTitle("XML Dump of Current Widget Tree");
				w.layout();
				StaticLayout.center(w, display);
				display.addWidget(w);
				//System.out.println(os.getDocument().toXML());
			}});
		return xmlDump;
	}
	
	public void quit()
	{
		System.exit(0);
	}

	private void buildExampleGUIinRenderThread(final IExample example)
	{
		Widget w = new Widget()
		{

			public void paint(Graphics g)
			{
				display.removeWidget(this);

				example.buildGUI(display);

			}
		};

		display.addWidget(w);
	}

	private void loadTheme(final String filename)
	{ 
		Widget w = new Widget()
		{

			@Override
			public void paint(Graphics g)
			{
				display.removeWidget(this);

				ITheme theme = null;

				if (filename == null) theme = new DefaultTheme();
				else
				{
					try
					{
						theme = new XMLTheme(filename);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				FengGUI.setTheme(theme);
				
				/*
				FengGUI.setUpAppearance(helpMenu);
				FengGUI.setUpAppearance(miscMenu);
				FengGUI.setUpAppearance(themeMenu);
				FengGUI.setUpAppearance(widgetMenu);
				FengGUI.setUpAppearance(programMenu);
				FengGUI.setUpAppearance(nonsenseMenu);
				FengGUI.setUpAppearance(compositesMenu);
				FengGUI.setUpAppearance(containersMenu); */
			}
		};

		display.addWidget(w);
		 
	}

	public void buildGUI(Display g)
	{
		display = g;

		buildMenuBar();

		display.layout();
	}

	public String getExampleName()
	{
		return "Test Almost Everything";
	}

	public String getExampleDescription()
	{
		return "Shows almost every Widget in FengGUI";
	}

}
