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
 * Created on Feb 23, 2007
 * $Id: ITextRenderer.java 220 2007-03-10 12:00:00Z schabby $
 */
package org.fenggui.render;

/**
 * Interface that abstracts different kinds of text renderers. Text renderes get
 * a string and a font and render the text in <code>render()</code>.
 * 
 * @author Johannes Schaback, last edited by $Author: schabby $, $Date: 2007-03-10 13:00:00 +0100 (Sat, 10 Mar 2007) $
 * @version $Revision: 220 $
 */
public interface ITextRenderer
{
	public void setText(String text);
	public String getText();
	
	public void setFont(Font font);
	public Font getFont();
	
	public void render(int x, int y, Graphics g, IOpenGL gl);
	
	public void renderCarret(int x, int y, int charIndex, ICarretRenderer carret, Graphics g, IOpenGL gl);
	
	public int getWidth();
	public int getHeight();
	
}
