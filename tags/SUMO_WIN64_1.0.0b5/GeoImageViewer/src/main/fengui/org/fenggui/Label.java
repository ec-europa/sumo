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
 * Created on 2005-3-2
 * $Id: Label.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.io.IOException;

import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * Widget for displaying a line of text or a pixmap. This widget
 * is passive and does not react on anything.
 * 
 * @author Johannes Schaback aka Schabby, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public class Label extends StandardWidget implements ILabel
{
	private Pixmap pixmap = null;
	private LabelAppearance appearance = null;

	/**
	 * Creates a new label with a given text.
	 * @param text the text
	 */
	public Label(String text)
	{
		initializeAppearance();
		setupTheme(Label.class);
		setText(text);
	}
	
	public void setAppearance(LabelAppearance appearance)
	{
		this.appearance = appearance;
	}

	/**
	 * Initialize the Label's widget appearance. Override this method to initialize own LabelAppearance
	 */
	protected void initializeAppearance()
	{
		appearance = new LabelAppearance(this);
	}


	@Override
	public LabelAppearance getAppearance()
	{
		return appearance;
	}


	public Pixmap getPixmap()
	{
		return pixmap;
	}


	public void setPixmap(Pixmap pixmap)
	{
		this.pixmap = pixmap;
		updateMinSize();
	}


	/**
	 * Creates a new empty label
	 *
	 */
	public Label()
	{
		this(null);
	}


	/* (non-Javadoc)
	 * @see org.fenggui.ITextWidget#getText()
	 */
	public String getText()
	{
		return getAppearance().getTextRenderer().getText();
	}


	/* (non-Javadoc)
	 * @see org.fenggui.ITextWidget#setText(java.lang.String)
	 */
	public void setText(String text)
	{
		getAppearance().getTextRenderer().setText(text);
		updateMinSize();
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		setText(stream.processAttribute("text", getText(), getText()));
		
		if(stream.isInputStream())  // XXX: only support read-in at the moment :(
			pixmap = stream.processChild("Pixmap", pixmap, null, Pixmap.class);
	}

}
