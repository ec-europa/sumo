/*
 * FengGUI - Java GUIs in OpenGL (http://fenggui.dev.java.net)
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
 * $Id: ScrollBar.java 327 2007-08-11 11:20:15Z Schabby $
 */
package org.fenggui;

import java.io.IOException;

import org.fenggui.event.ActivationEvent;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IActivationListener;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.event.mouse.IMousePressedListener;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOnlyStream;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Dimension;
import org.fenggui.util.Timer;

/**
 * Implementation of a scroll bar widget. It consists of two buttons and
 * a slider.
 *
 * @see org.fenggui.Slider
 * @see org.fenggui.Button
 * 
 * @author Johannes Schaback aka Schabby, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 */
public class ScrollBar extends ObservableWidget implements IBasicContainer {

	public static final String LABEL_DEFAULT = "default";
	public static final String LABEL_DISABLED = "disabled";
	
	/**
	 * Flag indicating whether the scroll bar is horizontal or not.
	 */
	private boolean horizontal = true;
	
	/**
	 * The two buttons to move the slider
	 */
	private Button increaseBtn, decreaseBtn;
	private ScrollBarAppearance appearance = null;
	/**
	 * The silder of the scroll bar
	 */
	private Slider slider = null;
	
	private double buttonJump = 0.05;
	
	private Timer autoScrollDelay = new Timer(2, 500);
	
	private boolean enabled = true;
	
	public ScrollBar()
	{
		this(true);
	}
	
	public ScrollBar(InputOnlyStream stream) throws IOException, IXMLStreamableException
	{
		process(stream);
	}
	
	/**
	 * Creates a new ScrollBar object.
	 * 
	 * @param horizontal true if the Slider of the ScrollBar
	 * shall be moved horizontally, false otherwise. 
	 */
	public ScrollBar(boolean horizontal) 
	{
		this.horizontal = horizontal;
		
		slider = new Slider(horizontal);
		slider.setParent(this);
		
		increaseBtn = new Button();
		increaseBtn.setParent(this);
		increaseBtn.addMousePressedListener(new IMousePressedListener(){

			public void mousePressed(MousePressedEvent mousePressedEvent)
			{
				autoScrollDelay.reset();
			}});
		
		decreaseBtn = new Button();
		decreaseBtn.setParent(this);
		decreaseBtn.addMousePressedListener(new IMousePressedListener(){

			public void mousePressed(MousePressedEvent mousePressedEvent)
			{
				autoScrollDelay.reset();
			}});		
		
        increaseBtn.addButtonPressedListener(new IButtonPressedListener() {

			public void buttonPressed(ButtonPressedEvent e)
			{
				slider.setValue(slider.getValue() + buttonJump);
			}
		});

		decreaseBtn.addButtonPressedListener(new IButtonPressedListener()
		{

			public void buttonPressed(ButtonPressedEvent e)
			{
				slider.setValue(slider.getValue() - buttonJump);
			}
		});
        
		appearance = new ScrollBarAppearance(this);
		
		setTraversable(false);
		
		setupTheme(ScrollBar.class);
		getAppearance().setEnabled(LABEL_DISABLED, false);
		buildListeners();
		updateMinSize();
	}
	
	private void buildListeners() {
		addActivationListener(new IActivationListener() {

			public void widgetActivationChanged(ActivationEvent activationEvent)
			{
				boolean enabled = activationEvent.isEnabled();
				getAppearance().setEnabled(LABEL_DISABLED, !enabled);
				getAppearance().setEnabled(LABEL_DEFAULT, enabled);
				
				increaseBtn.setEnabled(enabled);
				decreaseBtn.setEnabled(enabled);
				slider.setEnabled(enabled);

			}});	
	}
	
	@Override
	public void addedToWidgetTree() 
	{
		increaseBtn.addedToWidgetTree();
		slider.addedToWidgetTree();
		decreaseBtn.addedToWidgetTree();
	}



	@Override
	public void removedFromWidgetTree() 
	{
		increaseBtn.removedFromWidgetTree();
		slider.removedFromWidgetTree();
		decreaseBtn.removedFromWidgetTree();
	}

	public Slider getSlider() {
		return slider;
	}

	public boolean isHorizontal() {
		return horizontal;
	}

	public Button getDecreaseButton() {
		return decreaseBtn;
	}

	public Button getIncreaseButton() {
		return increaseBtn;
	}

	public double getButtonJump() {
		return buttonJump;
	}

	public void setButtonJump(double buttonJump) {
		this.buttonJump = buttonJump;
	}
	
	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled){
			return;
		}
		
		this.enabled = enabled;
		increaseBtn.setEnabled(enabled);
		decreaseBtn.setEnabled(enabled);
		slider.setEnabled(enabled);
		
	}

	@Override
	public void layout()
	{
		decreaseBtn.setSizeToMinSize();
		increaseBtn.setSizeToMinSize();
		
		int contentHeight = getAppearance().getContentHeight();
		int contentWidth = getAppearance().getContentWidth();
		
		if(horizontal) 
		{
			decreaseBtn.setHeight(contentHeight);
			increaseBtn.setHeight(contentHeight);
			decreaseBtn.setXY(0, 0);
			increaseBtn.setXY(contentWidth-increaseBtn.getWidth(), 0);
			slider.setXY(decreaseBtn.getWidth(), 0);
			slider.setSize(contentWidth-(increaseBtn.getWidth() + decreaseBtn.getWidth()), contentHeight);
			slider.layout();
		}
		else 
		{
			increaseBtn.setWidth(contentWidth);
			decreaseBtn.setWidth(contentWidth);
			decreaseBtn.setXY(0, 0);
			increaseBtn.setXY(0, contentHeight-decreaseBtn.getHeight());
			slider.setXY(0, decreaseBtn.getHeight());
			slider.setSize(contentWidth, contentHeight - (decreaseBtn.getHeight() + increaseBtn.getHeight()));
			slider.layout();
		}
	}
	
	
	
	@Override
	public ScrollBarAppearance getAppearance()
	{
		return appearance;
	}



	@Override
	public IWidget getWidget(int x, int y) 
	{
		if(!getAppearance().insideMargin(x, y)) return null;
		
        x -= getAppearance().getLeftMargins();
        y -= getAppearance().getBottomMargins();
        
        if(decreaseBtn.getSize().contains(x - decreaseBtn.getX(), y - decreaseBtn.getY())) return decreaseBtn;
        if(increaseBtn.getSize().contains(x - increaseBtn.getX(), y - increaseBtn.getY())) return increaseBtn;
        if(slider.getSize().contains(x - slider.getX(), y - slider.getY())) return slider.getWidget(x - slider.getX(), y - slider.getY());
        
        return this;
	}

	public class ScrollBarAppearance extends DecoratorAppearance
	{

		public ScrollBarAppearance(ScrollBar w)
		{
			super(w);
		}

		@Override
		public Dimension getContentMinSizeHint()
		{
			if(isHorizontal()) 
			{
				int height = Math.max(getIncreaseButton().getMinHeight(), 
						Math.max(getDecreaseButton().getMinHeight(), 
								getSlider().getMinHeight()));
				return new Dimension(
						getIncreaseButton().getMinWidth()+
						getDecreaseButton().getMinWidth()+
						getSlider().getMinWidth(), height);
			}
			else 
			{
				return new Dimension(
					Math.max(getIncreaseButton().getMinWidth(), 
						Math.max(getDecreaseButton().getMinWidth(), 
						getSlider().getMinWidth())),
					getIncreaseButton().getMinHeight()+
						getDecreaseButton().getMinHeight()+
						getSlider().getMinHeight());
			}
		}

		@Override
		public void paintContent(Graphics g, IOpenGL gl)
		{
			g.translate(decreaseBtn.getX(), decreaseBtn.getY());
			decreaseBtn.paint(g);
			g.translate(-decreaseBtn.getX(), -decreaseBtn.getY());
			
			g.translate(slider.getX(), slider.getY());
			slider.paint(g);
			g.translate(-slider.getX(), -slider.getY());
			
			g.translate(increaseBtn.getX(), increaseBtn.getY());
			increaseBtn.paint(g);
			g.translate(-increaseBtn.getX(), -increaseBtn.getY());
			
			
			if(increaseBtn.isPressed() && autoScrollDelay.getState() == 1)
			{
				slider.setValue(slider.getValue() + buttonJump / 10.0);
				autoScrollDelay.setState(1);
			}
			else if(decreaseBtn.isPressed() && autoScrollDelay.getState() == 1)
			{
				slider.setValue(slider.getValue() - buttonJump / 10.0);
				autoScrollDelay.setState(1);
			}
		}
		
	}
	
	public IWidget getNextTraversableWidget(IWidget start)
	{
		return getParent().getNextTraversableWidget(this);
	}

	public IWidget getPreviousTraversableWidget(IWidget start) 
	{
		return getParent().getPreviousTraversableWidget(this);
	}	
	
	public IWidget getNextWidget(IWidget start)
	{
		return getParent().getNextWidget(this);
	}

	public IWidget getPreviousWidget(IWidget start)
	{
		return getParent().getPreviousWidget(this);
	}

	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		stream.processInherentChild("Slider", slider);
		
		if(horizontal)
		{
			stream.processInherentChild("ScrollRightButton", increaseBtn);
			stream.processInherentChild("ScrollLeftButton", decreaseBtn);
		}
		else
		{
			stream.processInherentChild("ScrollUpButton", increaseBtn);
			stream.processInherentChild("ScrollDownButton", decreaseBtn);
		}
	}
	
	
}
