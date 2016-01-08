/*
 * FengGUI - Java GUIs in OpenGL (http://fenggui.sf.net)
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
 * Created on 2 oct. 06
 * $Id: TextView.java 342 2007-08-20 11:03:14Z marcmenghin $
 */

package org.fenggui.text;

import java.io.IOException;
import java.util.ArrayList;

import org.fenggui.ITextWidget;
import org.fenggui.ObservableWidget;
import org.fenggui.ScrollContainer;
import org.fenggui.event.ITextChangedListener;
import org.fenggui.event.TextChangedEvent;
import org.fenggui.event.mouse.IMousePressedListener;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.render.Font;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;
import org.fenggui.util.Point;

/**
 * An alternative TextView (non-editable) that allows font colors, sytles,
 * and word-wrap.
 * 
 * @author Boris Beaulant, last edited by $Author: marcmenghin $, $Date: 2007-08-20 13:03:14 +0200 (Mo, 20 Aug 2007) $
 * @version $Revision: 342 $
 */
public class TextView extends ObservableWidget implements ITextWidget
{
	private int minWidth = 10;
		
	private ArrayList<ITextChangedListener> textChangedHook = new ArrayList<ITextChangedListener>();

	protected ArrayList<TextRun> runs;
	private TextStyle defaulStyle;

	private Point nextDrawPoint = new Point(0, 0);
	private Dimension scratchDimension = new Dimension(0, 0);

	protected int fullHeight; // TODO the full height should be calculated on the fly

	private TextViewAppearance appearance = null;

	/**
	 * TextView constructor
	 */
	public TextView()
	{
		appearance = new TextViewAppearance(this);
		runs = new ArrayList<TextRun>();
		setupTheme(TextView.class);
		defaulStyle = new TextStyle(appearance.getFont(),appearance.getTextColor());
	}

	@Override
	public void process(InputOutputStream stream) throws IOException,	
		IXMLStreamableException {
		setText(stream.processAttribute("text", getText(), getText()));
		super.process(stream);
	}

	void buildLogic()
	{
		addMousePressedListener(new IMousePressedListener()
		{

			public void mousePressed(MousePressedEvent mp)
			{
				// (johannes) isnt that the same as getAppearance().insideMargin(...)?
				int x = mp.getDisplayX() - getDisplayX() - getAppearance().getPadding().getLeft();
				int y = mp.getDisplayY() - getDisplayY() - getAppearance().getPadding().getBottom()
						- getAppearance().getContentHeight();

				TextRun run = getRun(x, y);
				if (run != null)
				{
					System.out.println("Click on : " + new String(run.getChars()));
				}
			}
		});
	}

	/**
	 * @return defaulStyle
	 */
	public TextStyle getDefaulStyle()
	{
		return defaulStyle;
	}

	/**
	 * Define the default TextView style
	 * 
	 * @param style
	 */
	public void setStyle(TextStyle style)
	{
		defaulStyle = style;
	}

	/**
	 * Define the default TextView font
	 * 
	 * @param font
	 */
	public void setFont(Font font)
	{
		defaulStyle.setFont(font);
	}

	/**
	 * Define the default TextView text color
	 * 
	 * @param color
	 */
	public void setTextColor(Color color)
	{
		defaulStyle.setColor(color);
	}

	/**
	 * @return the minWidth
	 */
	public int getMinWidth() {
		return minWidth;
	}

	/**
	 * @param minWidth a d�fnir
	 */
	public void setMinWidth(int minWidth) {
		this.minWidth = minWidth;
	}

	/**
	 * Append styled text
	 * 
	 * @param run
	 */
	public void appendText(TextRun run)
	{
		runs.add(run);
		if (getParent() != null)
		{
			prepare(run);
		}
		processTextChanged(new String(run.getChars()));
	}
	
	/**
	 * Append styled text
	 * 
	 * @param text
	 * @param style
	 */
	public void appendText(String text, TextStyle style)
	{
		if (text.length() != 0)
		{
			appendText(new TextRun(text, style));
		}
	}

	/**
	 * Append new text
	 * 
	 * @param text
	 */
	public void appendText(String text)
	{
		appendText(text, defaulStyle);
	}

	/**
	 * Add new styled text line
	 * 
	 * @param text
	 * @param style
	 */
	public void addTextLine(String text, TextStyle style)
	{
		if (runs.isEmpty())
		{
			appendText(text, style);
		}
		else
		{
			appendText(new StringBuilder().append('\n').append(text).toString(), style);
		}
	}

	/**
	 * Add new text line
	 * 
	 * @param text
	 */
	public void addTextLine(String text)
	{
		addTextLine(text, defaulStyle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fenggui.ITextWidget#getText()
	 */
	public String getText()
	{
		StringBuilder sb = new StringBuilder();
		for (TextRun run : runs)
		{
			sb.append(run.getChars());
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fenggui.ITextWidget#setText(java.lang.String)
	 */
	public void setText(String text)
	{
		runs.clear();
		appendText(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fenggui.Widget#layout()
	 */
	@Override
	public void layout()
	{
		int oldHeight = fullHeight;
		prepareAll();
		if (oldHeight != fullHeight){
			getParent().layout();
		} else {
			super.layout();
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.fenggui.Widget#addedToWidgetTree()
	 */
	public void addedToWidgetTree() {
		super.addedToWidgetTree();
		prepareAll();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fenggui.Widget#updateMinSize()
	 */
	@Override
	public void updateMinSize()
	{
		setMinSize(getAppearance().getMinSizeHint());

		if (getParent() != null && getParent() instanceof ScrollContainer)
		{
			((ScrollContainer) getParent()).layout();
		}
		else if (getParent() != null)
		{
			getParent().updateMinSize();
		}
	}

	/**
	 * Get the TextRun at location (x, y)
	 * 
	 * @param x
	 * @param y
	 * @return A <code>TextRun</code> or <code>null</code>
	 */
	public TextRun getRun(int x, int y)
	{
		for (TextRun run : runs)
		{
			if (run.contains(x, y)) { return run; }
		}
		return null;
	}

	/**
	 * Prepares all of the TextRuns for rendering.
	 */
	private void prepareAll()
	{
		scratchDimension.setSize(0, 0);
		nextDrawPoint.setX(0);
		nextDrawPoint.setY(0);
		for (TextRun run : runs)
		{
			prepare(run);
		}
	}

	/**
	 * Prepare a TextRun to be displayed. This means, if word-wrapping is
	 * necessary, the TextRun will break itself down into individual lines.
	 */
	private void prepare(TextRun run)
	{
		int width = Math.max(minWidth,	getAppearance().getContentWidth());
		run.prepare(width, scratchDimension, nextDrawPoint);
		fullHeight = scratchDimension.getHeight();
	}

	/**
	 * Emit the text change signal
	 */
	private void processTextChanged(String text)
	{
		updateMinSize();

		fireTextChangedEvent(text);
	}

	@Override
	public TextViewAppearance getAppearance()
	{
		return appearance;
	}

	/**
	 * Add a {@link ITextChangedListener} to the widget. The listener can be added only once.
	 * @param l Listener
	 */
	public void addSelectionChangedListener(ITextChangedListener l)
	{
		if (!textChangedHook.contains(l))
		{
			textChangedHook.add(l);
		}
	}

	/**
	 * Add the {@link ITextChangedListener} from the widget
	 * @param l Listener
	 */
	public void removeSelectionChangedListener(ITextChangedListener l)
	{
		textChangedHook.remove(l);
	}

	/**
	 * Fire a {@link TextChangedEvent} 
	 */
	private void fireTextChangedEvent(String text)
	{
		TextChangedEvent e = new TextChangedEvent(this, text);

		for (ITextChangedListener l : textChangedHook)
		{
			l.textChanged(e);
		}
		
		if(getDisplay() != null)
			getDisplay().fireGlobalEventListener(e);
	}

}
