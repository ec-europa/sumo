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
 * $Id:XMLTheme.java 323 2007-08-11 10:11:38Z Schabby $
 */
package org.fenggui.theme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fenggui.Button;
import org.fenggui.Canvas;
import org.fenggui.CheckBox;
import org.fenggui.ComboBox;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.IWidget;
import org.fenggui.Label;
import org.fenggui.ProgressBar;
import org.fenggui.RadioButton;
import org.fenggui.ScrollBar;
import org.fenggui.ScrollContainer;
import org.fenggui.Slider;
import org.fenggui.SnappingSlider;
import org.fenggui.SplitContainer;
import org.fenggui.StandardWidget;
import org.fenggui.TabContainer;
import org.fenggui.TabItemLabel;
import org.fenggui.TextEditor;
import org.fenggui.background.FunnyBackground;
import org.fenggui.background.GradientBackground;
import org.fenggui.background.PixmapBackground;
import org.fenggui.background.PlainBackground;
import org.fenggui.border.BevelBorder;
import org.fenggui.border.PixmapBorder;
import org.fenggui.border.PixmapBorder16;
import org.fenggui.border.PlainBorder;
import org.fenggui.border.TitledBorder;
import org.fenggui.composites.Window;
import org.fenggui.layout.BorderLayout;
import org.fenggui.layout.FormLayout;
import org.fenggui.layout.GridLayout;
import org.fenggui.layout.RowLayout;
import org.fenggui.layout.StaticLayout;
import org.fenggui.menu.Menu;
import org.fenggui.menu.MenuBar;
import org.fenggui.render.Binding;
import org.fenggui.render.Font;
import org.fenggui.render.Pixmap;
import org.fenggui.switches.SetPixmapSwitch;
import org.fenggui.table.Table;
import org.fenggui.text.TextView;
import org.fenggui.theme.xml.GlobalContextHandler;
import org.fenggui.theme.xml.IXMLStreamable;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.theme.xml.TypeRegister;
import org.fenggui.theme.xml.XMLInputStream;
import org.fenggui.tree.Tree;
import org.fenggui.util.Color;
import org.fenggui.util.jdom.Document;
import org.fenggui.util.jdom.Element;
import org.fenggui.util.jdom.Reader;

/**
 * Loads a theme from a XML file. 
 * 
 * Please note that you can switch to loading via
 * the class loader by enabling Binding.setUseClassLoader(true). 
 * 
 * @author Johannes Schaback, last edited by $Author:Schabby $, $Date:2007-08-11 12:11:38 +0200 (Sat, 11 Aug 2007) $
 * @version $Revision:323 $
 */
public class XMLTheme implements ITheme
{
	private Document document = null;
	private final List<String> warnings = new ArrayList<String>();
	private String resourcePath = null;
	private GlobalContextHandler contextHandler = null;
	
	public static final TypeRegister TYPE_REGISTRY = new TypeRegister();
	
	static
	{
		TYPE_REGISTRY.register("PixmapBackground", PixmapBackground.class);
		TYPE_REGISTRY.register("FunnyBackground", FunnyBackground.class);
		TYPE_REGISTRY.register("GradientBackground", GradientBackground.class);
		TYPE_REGISTRY.register("PlainBackground", PlainBackground.class);
		
		TYPE_REGISTRY.register("BevelBorder", BevelBorder.class);
		TYPE_REGISTRY.register("PixmapBorder", PixmapBorder.class);
		TYPE_REGISTRY.register("PixmapBorder16", PixmapBorder16.class);
		TYPE_REGISTRY.register("PlainBorder", PlainBorder.class);
		TYPE_REGISTRY.register("TitledBorder", TitledBorder.class);
		TYPE_REGISTRY.register("Window", Window.class);
		TYPE_REGISTRY.register("Menu", Menu.class);
		TYPE_REGISTRY.register("MenuBar", MenuBar.class);
		TYPE_REGISTRY.register("Button", Button.class);
		TYPE_REGISTRY.register("Canvas", Canvas.class);
		TYPE_REGISTRY.register("CheckBox", CheckBox.class);
		TYPE_REGISTRY.register("ComboBox", ComboBox.class);
		TYPE_REGISTRY.register("Container", Container.class);
		TYPE_REGISTRY.register("Display", Display.class);
		TYPE_REGISTRY.register("Label", Label.class);
		TYPE_REGISTRY.register("List", org.fenggui.List.class);
		TYPE_REGISTRY.register("ProgressBar", ProgressBar.class);
		TYPE_REGISTRY.register("RadioButton", RadioButton.class);
		TYPE_REGISTRY.register("ScrollBar", ScrollBar.class);
		TYPE_REGISTRY.register("ScrollContainer", ScrollContainer.class);
		TYPE_REGISTRY.register("SnappingSlider", SnappingSlider.class);
		TYPE_REGISTRY.register("Slider", Slider.class);
		TYPE_REGISTRY.register("SplitContainer", SplitContainer.class);
		TYPE_REGISTRY.register("TabContainer", TabContainer.class);
		TYPE_REGISTRY.register("TabItemLabel", TabItemLabel.class);
		TYPE_REGISTRY.register("TextEditor", TextEditor.class);
		TYPE_REGISTRY.register("Tree", Tree.class);
		TYPE_REGISTRY.register("Table", Table.class);
		TYPE_REGISTRY.register("TextView", TextView.class);
		
		TYPE_REGISTRY.register("Font", Font.class);
		TYPE_REGISTRY.register("Pixmap", Pixmap.class);
		TYPE_REGISTRY.register("Color", Color.class);
		
		TYPE_REGISTRY.register("PixmapSwitch", SetPixmapSwitch.class);
		
		TYPE_REGISTRY.register("GridLayout", GridLayout.class);
		TYPE_REGISTRY.register("BorderLayout", BorderLayout.class);
		TYPE_REGISTRY.register("FormLayout", FormLayout.class);
		TYPE_REGISTRY.register("RowLayout", RowLayout.class);
		TYPE_REGISTRY.register("StaticLayout", StaticLayout.class);
	}
	
	public XMLTheme(String xmlThemeFile) throws IOException, IXMLStreamableException
	{
		/* extract path to xml file */
		if(xmlThemeFile.indexOf('/') != -1)
			this.resourcePath = xmlThemeFile.substring(0, xmlThemeFile.lastIndexOf('/')+1);
		else
			this.resourcePath = xmlThemeFile.substring(0, xmlThemeFile.lastIndexOf('\\')+1);
		
		/* parse the XML file in out JDOM */
		Reader r = new Reader();
		document = r.parse(Binding.getInstance().getResource(xmlThemeFile));
		
		/* will throw an exception if the bnding has not been initialized yet
		   see http://www.jmonkeyengine.com/jmeforum/index.php?topic=4483.15 */
		Binding.getInstance();
		
		Element el = document.getChild("FengGUI:init");
		
		if(el != null)
		{
		
			XMLInputStream xis = new XMLInputStream(el);
			xis.setResourcePath(resourcePath);
			List<IXMLStreamable> contents = new ArrayList<IXMLStreamable>();
			xis.processChildren(contents, XMLTheme.TYPE_REGISTRY);
			handleWarnings(xis);
			contextHandler = xis.getContextHandler();
		}
	}

	public Document getRoot()
	{
		return document;
	}

	
	@SuppressWarnings("unchecked")
	private String findSupertype(IWidget w)
	{
		Class clazz = w.getClass();
		String s = null;
		while(s == null && !clazz.equals(Object.class))
		{
			s = XMLTheme.TYPE_REGISTRY.getName(clazz);
			clazz = clazz.getSuperclass();
		}
		return s;
	}
	
	public void setUp(IWidget widget)
	{
		if(!(widget instanceof StandardWidget))
		{
			throw new IllegalArgumentException(
				"widget "+widget.getClass().getCanonicalName()+" is not a StandardWidget!");
		}
		
		StandardWidget w = (StandardWidget) widget;
		
		String type = findSupertype(widget);
		
		if(type == null)
		{
			System.err.println("Warning: "+widget.getClass().getCanonicalName()+" is not registered in org.theme.XMLTheme.TYPE_REGISTRY");
			return;
		}
		
		Element el = document.getChild(type);
		
		if(el == null)
		{
			System.err.println("Warning: <"+type+"> could not be found in theme definition file");
			return;
		}
		
		XMLInputStream xis = new XMLInputStream(el);
		xis.setContextHandler(contextHandler);
		xis.setResourcePath(resourcePath);
		
		try
		{
			w.process(xis);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		catch (IXMLStreamableException e)
		{
			throw new RuntimeException(e);
		}
		
		handleWarnings(xis);
	}
	
	public List<String> getWarnings() {
		return warnings;
	}
	
	private void handleWarnings(InputOutputStream stream) {
		String warningsStr = stream.getWarningsAsString().trim();
		if(warningsStr.length() > 0)
			System.out.println(warningsStr);
		warnings.addAll(stream.getWarnings());
	}
}
