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
 * Created on Mar 10, 2007
 * $Id: PwdCommand.java 220 2007-03-10 12:00:00Z schabby $
 */
package org.fenggui.console;

import java.io.PrintStream;

public class PwdCommand implements ICommand
{

	public void execute(PrintStream out, Console source, String[] args)
	{
		out.println(source.getCurrentDir().getAbsolutePath());
	}

	public String getCommand()
	{
		return "pwd";
	}

}
