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
 * $Id: ProgressBarExample.java 264 2007-04-16 12:34:05Z bbeaulant $
 */
package org.fenggui.example;

import org.fenggui.Display;
import org.fenggui.ProgressBar;
import org.fenggui.composites.Window;
import org.fenggui.layout.StaticLayout;

/**
 * Demonstrates the usage of a progress bar.
 * 
 * @author Johannes Schaback 
 */
public class ProgressBarExample implements IExample {


    private Window filesFrame = null;
    private Display desk;

	public void buildGUI(Display d) {
		desk = d;
		
        filesFrame = new Window(true, false, false, true);
        desk.addWidget(filesFrame);
        filesFrame.setX(50);
        filesFrame.setY(50);
        filesFrame.setSize(300, 100);
        filesFrame.setTitle("Progress Bar");
        
        filesFrame.getContentContainer().setLayoutManager(new StaticLayout());
        
        final ProgressBar pb = new ProgressBar("Working");
        filesFrame.getContentContainer().addWidget(pb);
        pb.setSize(250, 20);
        pb.setShrinkable(false);
        pb.setX(25);
        pb.setY(25);

        Thread t1 = new Thread(new Runnable() {

			public void run() {
				double value = 0;
				
				// simulate work
				while(value <= 1) {
					value += Math.random()*0.1;
					pb.setValue(value);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				pb.setText("Done!");
			}});
        t1.start();
		
		desk.layout();
		
		StaticLayout.center(filesFrame, desk);
		
	}

	public String getExampleName() {
		return "Progress Bar Example";
	}

	public String getExampleDescription() {
		return "Demonstrates a Progress Bar";
	}

}
