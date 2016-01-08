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
 * Created on Jul 15, 2005
 * $Id: FontExample.java 285 2007-05-20 16:10:10Z schabby $
 */
package org.fenggui.example;

import java.awt.Paint;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.fenggui.Display;
import org.fenggui.Label;
import org.fenggui.ProgressBar;
import org.fenggui.layout.StaticLayout;
import org.fenggui.render.Font;
import org.fenggui.util.Alphabet;
import org.fenggui.util.Color;
import org.fenggui.util.fonttoolkit.AssemblyLine;
import org.fenggui.util.fonttoolkit.BinaryDilation;
import org.fenggui.util.fonttoolkit.Clear;
import org.fenggui.util.fonttoolkit.DrawCharacter;
import org.fenggui.util.fonttoolkit.FontFactory;
import org.fenggui.util.fonttoolkit.PixelReplacer;

/**
 * Displays some lines of texts in various fonts and colors.
 * 
 * @author Johannes, last edited by $Author: schabby $, $Date: 2007-05-20 18:10:10 +0200 (So, 20 Mai 2007) $
 * @version $Revision: 285 $
 */
public class FontExample implements IExample {

	private Display display;
	private ProgressBar progressBar;

	private Font loadKlingonFont() 
	{
		try {
			Font klingon = new Font("data/fonts/ST-Klinzhai.png", "data/fonts/ST-Klinzhai.font");
			return klingon;
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private Font loadStarTrekFont() 
	{
		try {
			Font klingon = new Font("data/fonts/STTNG.png", "data/fonts/STTNG.font");
			return klingon;
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private Font createCoolAWTHelveticalFont() 
	{
		java.awt.Font awtFont = new java.awt.Font("Helvetica", java.awt.Font.BOLD, 24);
		
		FontFactory ff = new FontFactory(Alphabet.getDefaultAlphabet(), awtFont);
		AssemblyLine line = ff.getAssemblyLine();
		
		Paint redYellowPaint = new java.awt.GradientPaint(0, 0, java.awt.Color.RED, 15, 15, java.awt.Color.YELLOW, true);
		Paint greenWhitePaint = new java.awt.GradientPaint(0, 0, java.awt.Color.BLACK, 15, 0, java.awt.Color.GREEN, true);
		
		line.addStage(new Clear());
		line.addStage(new DrawCharacter(java.awt.Color.WHITE, false));
		
		line.addStage(new BinaryDilation(java.awt.Color.WHITE, 3));
		
		line.addStage(new PixelReplacer(redYellowPaint, java.awt.Color.WHITE));
		line.addStage(new DrawCharacter(java.awt.Color.CYAN, false));
		
		line.addStage(new PixelReplacer(greenWhitePaint, java.awt.Color.CYAN));
		
		Font font = ff.createFont();
			
		return font;
	}
	
	private Font createCoolAWTSerifFont() 
	{
		java.awt.Font awtFont = new java.awt.Font("Serif", java.awt.Font.BOLD, 24);
		
		FontFactory ff = new FontFactory(Alphabet.getDefaultAlphabet(), awtFont);
		AssemblyLine line = ff.getAssemblyLine();
		
		Paint redYellowPaint = new java.awt.GradientPaint(0, 0, java.awt.Color.RED, 15, 15, java.awt.Color.YELLOW, true);
		
		line.addStage(new Clear());
		line.addStage(new DrawCharacter(java.awt.Color.WHITE, false));
		
		line.addStage(new BinaryDilation(java.awt.Color.BLACK, 3));
		line.addStage(new DrawCharacter(java.awt.Color.WHITE, false));
		line.addStage(new PixelReplacer(redYellowPaint, java.awt.Color.WHITE));
		
		Font font = ff.createFont();
		return font;
	}	
	
	private Font createAntiAliasedFont()
	{
		return FontFactory.renderStandardFont(new java.awt.Font("Sans", java.awt.Font.BOLD, 24), true, Alphabet.getDefaultAlphabet());
	}
	
	class GUIBuildThread extends Thread {
		public void run() {
	        try 
	        {
				sleep(1);
			} 
	        catch (InterruptedException e) {
				e.printStackTrace();
			}
			
	        final float max = 8; /// number of created fonts
	        float value = 0;
	        
			progressBar.setValue(value++ / max);
			
	        final Font defaultFont = Font.getDefaultFont();
	        
	        progressBar.setValue(value++ / max);
	        
	        final Font coolHelveticaFont = createCoolAWTHelveticalFont();
	        
	        progressBar.setValue(value++ / max);
	        
	        final Font coolSerifFont = createCoolAWTSerifFont();
	        
	        progressBar.setValue(value++ / max);
	        
	        final Font font4 = FontFactory.renderStandardFont(new java.awt.Font("Courier", java.awt.Font.PLAIN, 12));
	        
	        progressBar.setValue(value++ / max);
	        
	        final Font germanFont = FontFactory.renderStandardFont(new java.awt.Font("Sans", java.awt.Font.PLAIN, 16), true, Alphabet.GERMAN);
	        
	        progressBar.setValue(value++ / max);
	        
	        Label label1 = new Label(); display.addWidget(label1);
	        Label label2 = new Label(); display.addWidget(label2);
	        Label label3 = new Label(); display.addWidget(label3);
	        Label label4 = new Label(); display.addWidget(label4);
	        Label label5 = new Label(); display.addWidget(label5);
	        Label label6 = new Label(); display.addWidget(label6);
	        Label label7 = new Label(); display.addWidget(label7);
	        Label label8 = new Label(); display.addWidget(label8);
	        Label label9 = new Label(); display.addWidget(label9);
	        Label label10 = new Label(); display.addWidget(label10);
	        Label label11 = new Label(); display.addWidget(label11);
	        
	        label1.setText("This is a black colored line of text in the default font");
	        label1.getAppearance().setFont(defaultFont);
	        label1.getAppearance().setTextColor(Color.BLACK);
	        label1.setXY(20, 20);
	        label1.setSizeToMinSize();
	        
	        final char umlaut_ae = 0x00E4; /* <-- this hassle is only to bypass encoding problems with the SVN!
	        Of course in your own project, you can use all special characters defined in org.fenggui.util.Alphabet as usual in normal strings! */
	        label2.setText("German saying: \"Wer anderen eine Bratwurst br"+umlaut_ae+"t, braucht ein Bratwurstbratger"+umlaut_ae+"t\"");
	        label2.getAppearance().setFont(germanFont);
	        label2.getAppearance().setTextColor(Color.CYAN);
	        label2.setXY(20, label1.getY() + label1.getHeight());
	        label2.setSizeToMinSize();
	        
	        label3.setText("pre-rendered Helvetica Font... Yeah!");
	        label3.getAppearance().setFont(coolHelveticaFont);
	        label3.getAppearance().setTextColor(Color.WHITE);
	        label3.setXY(20, label2.getY() + label2.getHeight());
	        label3.setSizeToMinSize();
	        
	        label4.setText("This is the fancy, with the Font Toolkit");
	        label4.getAppearance().setFont(coolHelveticaFont);
	        label4.getAppearance().setTextColor(Color.WHITE);
	        label4.setXY(20, label3.getY() + label3.getHeight());
	        label4.setSizeToMinSize();
	        
	        label5.setText("to increase loading speed!");
	        label5.getAppearance().setFont(coolSerifFont);
	        label5.getAppearance().setTextColor(Color.WHITE);
	        label5.setXY(20, label4.getY() + label4.getHeight());
	        label5.setSizeToMinSize();
	        
	        label6.setText("Pre-rendered Fonts can be saved to a file");
	        label6.getAppearance().setFont(coolSerifFont);
	        label6.getAppearance().setTextColor(Color.WHITE);
	        label6.setXY(20, label5.getY() + label5.getHeight());
	        label6.setSizeToMinSize();
	        
	        label7.setText("Line of blue colored text in Courier.....");
	        label7.getAppearance().setFont(font4);
	        label7.getAppearance().setTextColor(Color.BLUE);
	        label7.setXY(20, label6.getY() + label6.getHeight());
	        label7.setSizeToMinSize();

	        label8.setText("This is a line of anti-aliased text!");
	        label8.getAppearance().setFont(createAntiAliasedFont());
	        label8.getAppearance().setTextColor(Color.BLACK);
	        label8.setXY(20, label7.getY() + label7.getHeight());
	        label8.setSizeToMinSize();

	        label9.setText("This is a WHITE line of anti-aliased text!");
	        label9.getAppearance().setFont(createAntiAliasedFont());
	        label9.getAppearance().setTextColor(Color.WHITE);
	        label9.setXY(20, label8.getY() + label8.getHeight());
	        label9.setSizeToMinSize();
	        
	        progressBar.setValue(value++ / max);
	        
	        label10.setText("Whatever that means!");
	        label10.getAppearance().setFont(loadKlingonFont());
	        label10.getAppearance().setTextColor(Color.BLACK);
	        label10.setXY(20, label9.getY() + label9.getHeight());
	        label10.setSizeToMinSize();
	        
	        progressBar.setValue(value++ / max);
	        
	        label11.setText("from disk loaded Star Trek font...");
	        label11.getAppearance().setFont(loadStarTrekFont());
	        label11.getAppearance().setTextColor(Color.BLACK);
	        label11.setXY(20, label10.getY() + label10.getHeight());
	        label11.setSizeToMinSize();
	        
	        progressBar.setValue(value++ / max);
	        
	        display.removeWidget(progressBar);
		}
	}
	
	public void buildGUI(Display f) {
		display = f;

		progressBar = new ProgressBar();
		display.addWidget(progressBar);
		progressBar.setText("Loading Fonts...");
		progressBar.setSize(200, 25);
		progressBar.setValue(0);
		
		StaticLayout.center(progressBar, display);
        
        GUIBuildThread t = new GUIBuildThread();
        t.start();
	}

	public String getExampleName() {
		return "Fonts Example";
	}

	public String getExampleDescription() {
		return "Shows some text in various styles";
	}

}
