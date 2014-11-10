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
 * $Id: RadioButtonExample.java 264 2007-04-16 12:34:05Z bbeaulant $
 */
package org.fenggui.example;

import org.fenggui.Button;
import org.fenggui.Display;
import org.fenggui.Label;
import org.fenggui.RadioButton;
import org.fenggui.ToggableGroup;
import org.fenggui.composites.Window;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.layout.Alignment;
import org.fenggui.layout.RowLayout;
import org.fenggui.layout.StaticLayout;
import org.fenggui.util.Spacing;

/**
 * Exmaple class for demonstrating the use of Radio Buttons.
 * 
 * @todo Comment this class... #
 * 
 * @author Johannes Schaback, last edited by $Author: bbeaulant $, $Date: 2007-04-16 14:34:05 +0200 (Mo, 16 Apr 2007) $
 * @version $Revision: 264 $
 */
public class RadioButtonExample implements IExample {
	
	private Window filesFrame = null;
    private Display desk;


    private void buildFrame() {

        filesFrame = new Window(true, false, false, true);
        filesFrame.setX(50);
        filesFrame.setY(50);
        filesFrame.setSize(250, 400);
        filesFrame.setTitle("Radio Buttons");
        filesFrame.getContentContainer().setLayoutManager(new RowLayout(false));
        filesFrame.getContentContainer().getAppearance().setPadding(new Spacing(10, 10));
        Button btn = new Button("Apply");
        filesFrame.getContentContainer().addWidget(btn);
        btn.updateMinSize();
        btn.setSizeToMinSize();
        btn.setExpandable(false);
        
        filesFrame.getContentContainer().addWidget(new Label("How many legs do bugs have?"));
        
        final ToggableGroup<String> group = new ToggableGroup<String>();
        
        RadioButton<String> threeLegs = new RadioButton<String>("Three legs", group, "Nope!");
        filesFrame.getContentContainer().addWidget(threeLegs);
        
        RadioButton<String> fourLegs = new RadioButton<String>("Four legs", group, "Close!");
        filesFrame.getContentContainer().addWidget(fourLegs);
        
        RadioButton<String> oneLeg = new RadioButton<String>("One leg", group, "Wrong!");
        filesFrame.getContentContainer().addWidget(oneLeg);
        
        RadioButton<String> nineLegs = new RadioButton<String>("Nine legs", group, "Not quite right");
        filesFrame.getContentContainer().addWidget(nineLegs);
        
        RadioButton<String> noLegs =  new RadioButton<String>("No legs", group, "Not really!");
        filesFrame.getContentContainer().addWidget(noLegs);
        
        RadioButton<String> halfLegs = new RadioButton<String>("3 1/2 legs", group, "Hmmmmmmm... no!");
        filesFrame.getContentContainer().addWidget(halfLegs);
        
        final Label label = new Label("Make your choice!");
        filesFrame.getContentContainer().addWidget(label);
        label.getAppearance().setAlignment(Alignment.MIDDLE);
                
        btn.addButtonPressedListener(new IButtonPressedListener()
		{
			public void buttonPressed(ButtonPressedEvent e)
			{
                if(group.getSelectedItem() != null)
                    label.setText(group.getSelectedValue()+" Please try again");
                else label.setText("Please make your choice!");
            }});
        
        filesFrame.pack();
        desk.addWidget(filesFrame);
        StaticLayout.center(filesFrame, desk);
    }

	public void buildGUI(Display d) {
		desk = d;
		
		buildFrame();
	}

	public String getExampleName() {
		return "RadioButton Example";
	}

	public String getExampleDescription() {
		return "Frame with RadioButtons";
	}

}
