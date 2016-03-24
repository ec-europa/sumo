/*
 * 
 */
package org.fenggui.util.fonttoolkit;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import org.fenggui.util.Alphabet;
/**
 * Command line prototype for creating fonts that looks like from the stone age when 
 * Rainers Font Creator is done :)
 * 
 * @author Johannes, last edited by $Author: bbeaulant $, $Date: 2006-10-25 12:49:21 +0200 (Wed, 25 Oct 2006) $
 * @version $Revision: 94 $
 */
public class FontCreator 
{

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		if(args.length < 1)
		{
			printUsage();
			return;
		}

		if(args[0].equals("-list"))
		{
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			String fontNames[] = ge.getAvailableFontFamilyNames();
			for(String s: fontNames)
			{
				System.out.println(s);
			}
		}
		
		if(args[0].equals("-list"))
		{
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			String fontNames[] = ge.getAvailableFontFamilyNames();
			for(String s: fontNames)
			{
				System.out.println(s);
			}
		} 
		else if(args[0].equals("-create"))
		{
			if(args[0].length() < 6)
			{
				System.out.println("Too few arguments!");
				printUsage();
				return;
			}
			
			String fontname = args[1];
			int size = Integer.parseInt(args[2]);
			int style = 0;
			
			if(args[3].equals("plain"))
			{
				style = Font.PLAIN;
			}
			else if(args[3].equals("bold"))
			{
				style = Font.BOLD;
			}
			else if(args[3].equals("italic"))
			{
				style = Font.ITALIC;
			}
			
			boolean antiAliasing = Boolean.parseBoolean(args[4]);
			org.fenggui.render.Font f = FontFactory.renderStandardFont(new java.awt.Font(fontname,style, size),antiAliasing, Alphabet.getDefaultAlphabet());
			/*
			try {
				f.writeFontData(args[5]+".png",args[5]+".font");
			} catch (IOException e) {
				e.printStackTrace();
			}*/
		}
		
	}

	public static void printUsage()
	{
		System.out.println("FengGUI Command-Line Font Creator");
		System.out.println("Usage:");
		System.out.println("java org.fenggui.examples.FontCreator {-list | -create fontname size {plain|bold|italic} anti-aliasing name}");
		System.out.println("");
		System.out.println("-list:   lists all available AWT fonts");
		System.out.println("-create: creates a new FengGUI font");
		System.out.println("    fontame       - name of the AWT font");
		System.out.println("    size          - size of the FengGUI font (int)");
		System.out.println("    anti-aliasing - boolean whether font shall be rendered with anti-aliasing enabled");
		System.out.println("    name          - filename stem for name.png and name.font");
		System.out.println("Send questions and comments to johannes.schaback@gmail.com");
		
	}
	
}
