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
 * $Id: ObservableLabelWidget.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.io.IOException;

import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * 
 * State-enabled Widget that helps drawing text and
 * images.
 * 
 * @todo Comment this class... #
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public class ObservableLabelWidget extends ObservableWidget implements ILabel
{
    private Pixmap pixmap = null;	
    private LabelAppearance appearance = null;
    
	public ObservableLabelWidget()
	{
		super();
		appearance = new LabelAppearance(this);
	}

	public LabelAppearance getAppearance()
	{
		return appearance;
	}

	/**
	 * @return Returns the text.
	 */
	public String getText() 
	{
		return getAppearance().getTextRenderer().getText();
	}

	/**
	 * @param text The text to set.
	 */
	public void setText(String text) 
	{
		getAppearance().getTextRenderer().setText(text);
		updateMinSize();
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

	@Override
	public void updateMinSize()
	{
		setMinSize(getAppearance().getMinSizeHint());
		
		if(getParent() != null) getParent().updateMinSize();
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		setText(stream.processAttribute("text", getText(), getText()));
		
		if(stream.isInputStream()) // XXX: only support read-in at the moment :(
		{
			setPixmap(stream.processChild("Pixmap", pixmap, null, Pixmap.class));
		}
	}

	
}
