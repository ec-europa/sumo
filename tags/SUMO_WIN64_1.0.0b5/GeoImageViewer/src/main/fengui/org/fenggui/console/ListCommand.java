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
 * Created on Mar 9, 2007
 * $Id: ListCommand.java 220 2007-03-10 12:00:00Z schabby $
 */
package org.fenggui.console;

import java.io.File;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;

public class ListCommand implements ICommand
{


	public String getCommand()
	{
		return "ls";
	}

	public void execute(PrintStream out, Console source, String[] args)
	{
		File[] list = new File(source.getCurrentDir().getAbsolutePath()+"/.").listFiles();
		
		for(File file: list)
		{
			if(file.canRead())
				out.print('R');
			else
				out.print('-');
			
			if(file.canWrite())
				out.print('W');
			else
				out.print('-');

			Calendar c = Calendar.getInstance();
			c.setTime(new Date(file.lastModified()));
			
			out.print(" "+
				ensureLength(c.get(Calendar.DAY_OF_MONTH), 2, '0')+"-"+
				ensureLength(c.get(Calendar.MONTH), 2, '0')+"-"+
				ensureLength(c.get(Calendar.YEAR), 2, '0')+" ");
			
			int size = (int)file.length();
			if(size < 1000)
				out.print(ensureLength((int)size, 3, ' ')+" B ");
			else
			{
				if(size/Math.pow(2, 10) < 1000)
					out.print(ensureLength((int)(size/Math.pow(2, 10)), 3, ' ')+" KB");
				else
				{
					if(size/Math.pow(2, 20) < 1000)
						out.print(ensureLength((int)(size/Math.pow(2, 20)), 3, ' ')+" MB");
					else
					{
						if(size/Math.pow(2, 30) < 1000)
							out.print(ensureLength((int)(size/Math.pow(2, 30)), 3, ' ')+" GB");
					}
				}
			}
			out.println(" "+file.getName());
		}
		
		
	}

	private String ensureLength(int d, int length, char add)
	{
		String s = ""+d;
		
		while(s.length() < length) s = add + s;
		
		return s;
	}
}
