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
 * Created on 6 oct. 2006
 * $Id$
 */

package org.fenggui;


/**
 * Widget for displaying a line of text which can be rotate through an angle of [-90, 90] degrees.
 * This widget is passive and does not react on anything.
 * 
 * TODO : implement pixmap support
 * TODO In the long run we may want to set RotatedLabelAppearance in Label such that
 * we do not need RotatedLabel anymore
 * @author Boris Beaulant, last edited by $Author: $, $Date: $
 * @version $Revision: $
 */
public class RotatedLabel extends Label
{

	private float angle = 0;


	/**
	 * Create a RotatedLabel instance with the given angle
	 * @param angle The rotation's angle
	 */
	public RotatedLabel(float angle)
	{
		this("", angle);
	}


	/**
	 * Create a RotatedLabel instance with the given text
	 * @param text The text to display in the RotatedLabel
	 */
	public RotatedLabel(String text)
	{
		this(text, 0);
	}


	/**
	 * Create a RotatedLabel instance with the given text and angle
	 * @param text The text to display in the RotatedLabel
	 * @param angle The rotation's angle
	 */
	public RotatedLabel(String text, float angle)
	{
		super(text);
		setAngle(angle);
	}


	/* (non-Javadoc)
	 * @see org.fenggui.Label#initializeAppearance()
	 */
	@Override
	protected void initializeAppearance()
	{
		setAppearance(new RotatedLabelAppearance(this));
	}


	/* (non-Javadoc)
	 * @see org.fenggui.Label#getAppearance()
	 */
	@Override
	public RotatedLabelAppearance getAppearance()
	{
		return (RotatedLabelAppearance) super.getAppearance();
	}

	public void setAppearance(RotatedLabelAppearance a)
	{
		super.setAppearance(a);
	}
	
	/**
	 * @return Returns the rotation's angle.
	 */
	public float getAngle()
	{
		return angle;
	}


	/**
	 * Set the rotation's angle in degrees
	 * @param angle The angle to set. Angle must be [-90, 90]
	 */
	public void setAngle(float angle)
	{
		this.angle = Math.max(-90, Math.min(90, angle));

		// Precalculate trigonometric values
		double radiansAngle = Math.toRadians(this.angle);
		getAppearance().sinAngle = Math.sin(radiansAngle);
		getAppearance().cosAngle = Math.cos(radiansAngle);

		updateMinSize();
	}

}
