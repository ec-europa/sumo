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
 * $Id: TextArea.java 315 2007-07-28 23:10:06Z Schabby $
 */

package org.fenggui.composites;

import org.fenggui.ITextWidget;
import org.fenggui.ScrollContainer;
import org.fenggui.TextEditor;
import org.fenggui.event.ITextChangedListener;
import org.fenggui.event.TextChangedEvent;

/**
 * Implementation of an autoscroll text editor.
 * 
 * 
 * @author Boris Beaulant, last edited by $Author: Schabby $, $Date: 2007-07-29 01:10:06 +0200 (So, 29 Jul 2007) $
 * @version $Revision: 315 $
 */
public class TextArea extends ScrollContainer implements ITextWidget
{

	private TextEditor textEditor;

	/**
	 * ScrollTextEditor constructor
	 */
	public TextArea(boolean multiline)
	{
		textEditor = new TextEditor(multiline);
		textEditor.addTextChangedListener(new ITextChangedListener()
		{
			public void textChanged(TextChangedEvent textChangedEvent)
			{
				layout();
			}
		});
		setInnerWidget(textEditor);
		setSize(10, 10);
	}

	public TextArea()
	{
		this(true);
	}

	/**
	 * Returns the enclosed TextEditor
	 * @return textEditor the enclosed TextEditor
	 */
	public TextEditor getTextEditor()
	{
		return textEditor;
	}

	/**
	 * @return the text editor's text
	 */
	public String getText()
	{
		return textEditor.getText();
	}

	/**
	 * Define the textEditor's text
	 * 
	 * @param text
	 *            Text to set
	 */
	public void setText(String text)
	{
		textEditor.setText(text);
	}

	/**
	 * Append text to the end of the textEditor
	 * 
	 * @param text
	 *            Text to append
	 */
	public void appendText(String text)
	{
		textEditor.appendText(text);
	}

	/**
	 * Terminate the current line by writing the line separator string and
	 * Append text to the end of the textEditor
	 * 
	 * @param text
	 *            Text to append
	 */
	public void addTextLine(String text)
	{
		textEditor.addTextLine(text);
	}

	
}
