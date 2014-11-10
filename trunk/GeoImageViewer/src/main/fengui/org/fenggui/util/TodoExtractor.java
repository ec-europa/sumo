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
 * $Id: TodoExtractor.java 28 2006-10-05 01:37:07Z schabby $
 */
package org.fenggui.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Parses the source tree for 'todo' 
 * phrases with a terminating '#'
 * and outputs it in HTML code.

 * 
 * @todo Comment this class... #
 * @todo Paths used by this class (as src folder and output file) are
 * hard coded. Make this a bit more elegant #
 * @todo A possible improvement over the current way of handling
 * todo tags with a trailing dash could be do check whether the
 * comments stops or if the next line contains // comment. This would
 * render the dash obsolete #
 * 
 * @author Johannes Schaback, last edited by $Author: schabby $, $Date: 2006-10-05 03:37:07 +0200 (Thu, 05 Oct 2006) $
 * @version $Revision: 28 $
 */
public class TodoExtractor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File rootDir = new File(args[0]);
		
		try {
			
			PrintWriter out = new PrintWriter(new FileWriter("www/Todo.html", false));
			
			out.println("<html><body>\n");
			
			ArrayList<File> files = new ArrayList<File>();
			processDir(rootDir, files, out);
			
			File currentDir = null;
			
			for(File file: files) {
				if(currentDir == null) {
					currentDir = file.getParentFile();
					openTable(out, currentDir.getName());
				}
				
				if(!currentDir.equals(file.getParentFile())) {
					closeTable(out);
					currentDir = file.getParentFile();
					openTable(out, currentDir.getName());
				}
				
				processFile(file, out);
			}
			
			closeTable(out);
			out.println(
					"$Id: TodoExtractor.java 28 2006-10-05 01:37:07Z schabby $" +
					"</body></html>");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	private static void closeTable(PrintWriter out) {
		out.println("</tbody>"+ 
		        "</table>"+
		        "</td>"+
		      "</tr>"+
		    "</tbody>"+
		  "</table><br/>\n");		
	}
	
	private static void openTable(PrintWriter out, String name) {
		out.println(
				"<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" bgcolor=\"#669966\" width=\"100%\">\n"+
				"<tbody>\n"+
				"<tr>\n"+
				"<td>\n"+
				"<table width=\"100%\" border=\"0\" cellspacing=\"1\" cellpadding=\"3\">\n"+
		        "<tbody>\n"+
		        "<tr>\n"+
		        "<td colspan=\"3\"><font color=\"#ffffff\"><b>"+name+"</b></font></td>\n"+
		        "</tr>\n");		
	}
	
	public static void processDir(File dir, ArrayList<File> files, PrintWriter out) throws IOException {
		File[] subs = dir.listFiles();
		
		ArrayList<File> subDirs = new ArrayList<File>();
		
		for(int i=0;i<subs.length;i++) {
			if(subs[i].isDirectory()) {
				subDirs.add(subs[i]);
			} else if(subs[i].isFile() && isJavaFile(subs[i])) {
				files.add(subs[i]);
			}
		}
		
		for(File subDir: subDirs) {
			processDir(subDir, files, out);
		}
	}
	
	private static boolean isJavaFile(File f) {
		return f.getName().endsWith(".java") &&
			!f.getName().endsWith("TodoExtractor.java");
	}
	
	
	
	public static void processFile(File f, PrintWriter out) throws IOException {
		System.out.println("Scanning "+f.getName());
		
		BufferedReader fr = new BufferedReader(new FileReader(f));
		
		String line  = fr.readLine();
		
		boolean skipThisFile = true;
		StringBuffer sb = new StringBuffer();
		while(line != null) {
			sb.append(line+'\n');
			if(line.indexOf("@todo") != -1) skipThisFile = false;
			line  = fr.readLine();
		}
		
		if(skipThisFile) return;
		
		out.println(
		        "<tr bgcolor=\"#ffffff\" valign=\"top\">\n"+ 
		        "<td width=\"20%\"><b>"+f.getName()+"</b></td>\n"+
		        "<td>\n"+
		        "<ul>\n");
		
		int index = sb.indexOf(TODO, 0);
		int lineNumber = countLineBreaks(sb,index);
		while(index != -1) {
			String extract = sb.substring(index+TODO.length(), sb.indexOf("#", index));
			lineNumber = countLineBreaks(sb, index);
			out.println("<li>Line: "+lineNumber+": <i>"+clean(extract)+"</i></li>\n");
			index = sb.indexOf(TODO, index+TODO.length());
		}
		out.println("</ul></td>\n"+
		        "<td width=\"30\"><b>\n"+
		        "<font class=\"gray\">!</font>\n"+
		        "<font class=\"gray\">i</font>\n"+
		        "<font class=\"gray\">d</font>\n"+
		        "</b></td>\n"+
		        "</tr>\n");
		
	}

	private static int countLineBreaks(StringBuffer sb, int end) {
		int sum=1;
		int index = 0;
		index = sb.indexOf("\n", index+1);
		while(index != -1 && index < end) {
			sum++;
			index = sb.indexOf("\n", index+1);
		}
		return sum;
	}

	private static final String TODO = "todo";
	
	private static String clean(String s) {
		s = s.trim();
		
		s = s.replace("\t", "");
		s = s.replace("  * ", "");
		s = s.replace(" * ", "");
		s = s.replace("* ", "");
		s = s.replace("// ", "");
		return s;
	}
	
}
