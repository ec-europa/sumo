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
 * $Id: MultiLineLabel.java 204 2007-02-15 19:24:13Z charlierby $
 */
package org.fenggui;

import java.text.BreakIterator;

/**
 * 
 *  * TODO In the long run we may want to set MultiLineLabelAppearance in Label such that
 * we do not need MultiLineLabel anymore...

 * @author Rainer Angermann
 * 
 */
public class MultiLineLabel extends Label
{

	private int maxCharactersPerLine = 50;
	private String[] text;


	public MultiLineLabel()
	{
		initializeAppearance();
		setupTheme(MultiLineLabel.class);
		updateMinSize();
	}


	/* (non-Javadoc)
	 * @see org.fenggui.Label#initializeAppearance()
	 */
	@Override
	protected void initializeAppearance()
	{
		super.setAppearance(new MultiLineLabelAppearance(this));
	}


	/* (non-Javadoc)
	 * @see org.fenggui.Label#getAppearance()
	 */
	@Override
	public MultiLineLabelAppearance getAppearance()
	{
		return (MultiLineLabelAppearance) super.getAppearance();
	}
	
	public void setAppearance(MultiLineLabelAppearance app)
	{
		super.setAppearance(app);
	}

	public String[] getTextArray()
	{
		return text;
	}


	public void setText(String text)
	{
		if (text == null) return;

		String line = wrapText(text, maxCharactersPerLine);
		this.text = line.split("\n");

		updateMinSize();
	}


	/**
	 * 
	 * @param toWrap
	 *            The string on which the line wrapping process should be done
	 * @param maxLength
	 *            The maximum length per line
	 * @return The StringBuffer with the wrapped lines
	 */
	public static String wrapText(final String toWrap, final int maxLength)
	{
		if (maxLength == 0) throw new IllegalArgumentException("maxLength must not be 0");

		StringBuffer ret = new StringBuffer();
		BreakIterator boundary = BreakIterator.getLineInstance();
		boundary.setText(toWrap);
		int realEnd = BreakIterator.DONE;
		int start = boundary.first();
		int end = boundary.next();

		int lineLength = 0;

		while (end != BreakIterator.DONE)
		{
			int charCount = end - start - 1;

			if (charCount > maxLength)
			{
				realEnd = end;
				end = start + maxLength;
			}
			String word = toWrap.substring(start, end);
			lineLength = lineLength + word.length();
			if (lineLength >= maxLength)
			{
				ret.append("\n");
				lineLength = word.length();
			}
			ret.append(word);
			if (realEnd == BreakIterator.DONE)
			{
				start = end;
				end = boundary.next();
			}
			else
			{
				start = end;
				end = realEnd;
				realEnd = BreakIterator.DONE;
			}
		}
		return ret.toString();
	}


	/**
	 * @param maxCharactersPerLine the maxCharactersPerLine to set
	 */
	public void setMaxCharactersPerLine(int maxCharactersPerLine)
	{
		this.maxCharactersPerLine = maxCharactersPerLine;
		updateMinSize();
	}


}
