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
 * Created on Aug 1, 2007
 * $Id$
 */
package org.fenggui;

import java.io.IOException;
import java.util.ArrayList;

import org.fenggui.event.IDragAndDropListener;
import org.fenggui.event.ISliderMovedListener;
import org.fenggui.event.SliderMovedEvent;
import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;

/**
 * Implementation of a slider that snaps to discrete ticks.
 * Currently only as vertical slider.
 * 
 * @author Johannes Schaback, last edited by $Author$, $Date$
 * @version $Revision$
 */
public class SnappingSlider extends StandardWidget
{
	private Pixmap sliderPixmap = null;
	private int ticks = 10;
	private int position = 0;
	private SnappingSliderAppearance appearance = null;
	private ArrayList<ISliderMovedListener> sliderMovedHook = new ArrayList<ISliderMovedListener>();
	private IDragAndDropListener dndListener = new SliderDnDListener();
	private String[] tickLabels = null;
	
	
	/**
	 * 
	 * @param nrOfTicks
	 */
	public SnappingSlider(int nrOfTicks)
	{
		this.ticks = nrOfTicks;
		
		appearance = new SnappingSliderAppearance(this);
		
		setupTheme(SnappingSlider.class);
		updateMinSize();
	}
	
	@Override
	public SnappingSliderAppearance getAppearance()
	{
		return appearance;
	}

	public Pixmap getSliderPixmap()
	{
		return sliderPixmap;
	}

	public void setSliderPixmap(Pixmap sliderPixmap)
	{
		this.sliderPixmap = sliderPixmap;
		updateMinSize();
	}

	public int getTicks()
	{
		return ticks;
	}

	
	public void addSliderMovedListener(ISliderMovedListener sle)
	{
		sliderMovedHook.add(sle);
	}
	
	public void removeSliderMovedListener(ISliderMovedListener sle)
	{
		sliderMovedHook.remove(sle);
	}
	
	private void fireSliderMovedListener()
	{
		SliderMovedEvent sme = new SliderMovedEvent(this);
		for(ISliderMovedListener l: sliderMovedHook)
		{
			l.sliderMoved(sme);
		}
	}

	@Override
	public void addedToWidgetTree() 
	{
		if(getDisplay() != null)
			getDisplay().addDndListener(dndListener);
	}

	@Override
	public void removedFromWidgetTree() 
	{
		if(getDisplay() != null)
			getDisplay().removeDndListener(dndListener);
	}	
	
	public int getValue()
	{
		return position;
	}
	
	public void setValue(int v)
	{
		position = v;
	}
	
	private class SliderDnDListener implements IDragAndDropListener 
	{
		private int startX = 0;

		public void select(int x, int y) 
		{
			startX = getDisplayX() + getAppearance().getLeftMargins() - (getAppearance().getContentWidth()/(getTicks()-1))/2;
		}

		public void drag(int x, int y) 
		{
			float half = ((float)getAppearance().getContentWidth() / (float)(getTicks()-1)) / 1f;
			int deltaX = x-startX;
			
			int t = deltaX / (getAppearance().getContentWidth()/(getTicks()-1));
			
			//System.out.println("drag: "+x+" "+y+" deltaX: "+deltaX+"  "+t);
			
			if(t >= 0 && t < getTicks())
			{
				int oldT = getValue();
				setValue(t);
				
				if(oldT != t)
					fireSliderMovedListener();
			}	
		}

		public void drop(int x, int y, IWidget dropOn)
		{
			//pressed = false;
		}

		public boolean isDndWidget(IWidget w, int x, int y)
		{
			x = x-getDisplayX();
			y = y-getDisplayY();
			
			int sPos =(int)((float)getValue() * (float)(getAppearance().getContentWidth()/(float)(getTicks()-1))) + getAppearance().getLeftMargins();
			
			int pixmapHeight = getSliderPixmap() == null ? 10 : getSliderPixmap().getHeight();
			int pixmapWidth  = getSliderPixmap() == null ? 10 : getSliderPixmap().getWidth();
			
			//System.out.println(" "+x+" "+y+" sPos: "+sPos);
			return getHeight()/2 - pixmapHeight/2 < y &&  getHeight()/2 + pixmapHeight/2 > y
				&& sPos - pixmapWidth/2 < x && sPos + pixmapWidth/2 > x;
		}
	}

	public String[] getTickLabels()
	{
		return tickLabels;
	}

	public void setTickLabels(String... tickLabels)
	{
		this.tickLabels = tickLabels;
		updateMinSize();
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		sliderPixmap = stream.processChild("SliderPixmap", sliderPixmap, Pixmap.class);
	}
	
	
}
