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
 * $Id: ChangeDirectoryCommand.java 220 2007-03-10 12:00:00Z schabby $
 */
package org.fenggui.console;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class ChangeDirectoryCommand implements ICommand
{

	public void execute(PrintStream out, Console source, String[] args)
	{
		if(args.length <= 1)
		{
			out.println("Usage: cd [directory]");
			return;
		}
		
		String f = source.getCurrentDir().getAbsolutePath() + "/"+ args[1];
		File file = new File(f);
		try
		{
			file = new File(file.getCanonicalPath());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		if(!file.exists())
		{
			out.println(f+" does not exist");
			return;
		}
		
		
		if(!file.isDirectory())
		{
			out.println(f+" is not a directory!");
			return;
		}

		source.setCurrentDir(file);
	}

	public String getCommand()
	{
		return "cd";
	}

}
