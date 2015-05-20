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
 * $Id: SliderMovedEvent.java 322 2007-08-11 10:03:15Z Schabby $
 */
package org.fenggui.event;

import org.fenggui.Slider;
import org.fenggui.SnappingSlider;

/**
 * Class that represents the event of moving a slider.
 * 
 * @author Johannes Schaback aka Schabby, last edited by $Author: Schabby $, $Date: 2007-08-11 12:03:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 322 $
 */
public class SliderMovedEvent extends Event
{
	public SliderMovedEvent(Slider slider) 
	{
		super(slider);
	}

	public SliderMovedEvent(SnappingSlider slider) 
	{
		super(slider);
	}
	
	
	public double getPosition() 
	{
		if(getSource() instanceof Slider)
			return ((Slider)getSource()).getValue();
		else 
			return ((SnappingSlider)getSource()).getValue();
	}
	
}
