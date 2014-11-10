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
 * $Id: SplitContainerExample.java 210 2007-02-21 23:25:04Z schabby $
 */

package org.fenggui.example;

import org.fenggui.Button;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.SplitContainer;
import org.fenggui.layout.StaticLayout;

/**
 * Demonstrates the usage of a <code>SplitContainer</code>.
 * 
 * @author Johannes Schaback
 */
public class SplitContainerExample implements IExample
{
	public void buildGUI(Display display)
	{
		Container cont = new Container();
		cont.setSize((int) ((double) display.getWidth() * 0.8), (int) ((double) display.getHeight() * 0.8));
		display.addWidget(cont);
		cont.setKeyTraversalRoot(true);
		
		SplitContainer centerSC = new SplitContainer(true);
		cont.addWidget(centerSC);

		SplitContainer northSC = new SplitContainer(false);
		SplitContainer southSC = new SplitContainer(false);

		Button northWestBtn = new Button("North-West");
		Button northEastBtn = new Button("North-East");

		Button southEastBtn = new Button("South-East");
		Button southWestBtn = new Button("South-West");

		centerSC.setFirstWidget(southSC);
		centerSC.setSecondWidget(northSC);

		northSC.setFirstWidget(northWestBtn);
		northSC.setSecondWidget(northEastBtn);

		southSC.setFirstWidget(southWestBtn);
		southSC.setSecondWidget(southEastBtn);

		display.layout();

		StaticLayout.center(cont, display);

	}

	public String getExampleName()
	{
		return "SplitContainer Example";
	}

	public String getExampleDescription()
	{
		return "Demonstrates a SplitContainer";
	}

}
