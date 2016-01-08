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
 * Created on Jan 16, 2007
 * $Id: DefaultTheme.java 323 2007-08-11 10:11:38Z Schabby $
 */
package org.fenggui.theme;

import java.awt.image.BufferedImage;

import org.fenggui.Button;
import org.fenggui.CheckBox;
import org.fenggui.ComboBox;
import org.fenggui.Container;
import org.fenggui.IWidget;
import org.fenggui.Label;
import org.fenggui.List;
import org.fenggui.ProgressBar;
import org.fenggui.RadioButton;
import org.fenggui.ScrollBar;
import org.fenggui.ScrollContainer;
import org.fenggui.Slider;
import org.fenggui.SnappingSlider;
import org.fenggui.Span;
import org.fenggui.SplitContainer;
import org.fenggui.TabItemLabel;
import org.fenggui.TextEditor;
import org.fenggui.VerticalList;
import org.fenggui.background.Background;
import org.fenggui.background.GradientBackground;
import org.fenggui.background.PlainBackground;
import org.fenggui.border.BevelBorder;
import org.fenggui.border.PlainBorder;
import org.fenggui.composites.Window;
import org.fenggui.console.Console;
import org.fenggui.layout.Alignment;
import org.fenggui.menu.Menu;
import org.fenggui.menu.MenuBar;
import org.fenggui.render.Binding;
import org.fenggui.render.ITexture;
import org.fenggui.render.Pixmap;
import org.fenggui.switches.SetPixmapSwitch;
import org.fenggui.switches.SetTextColorSwitch;
import org.fenggui.table.Table;
import org.fenggui.tree.Tree;
import org.fenggui.util.Color;
import org.fenggui.util.Spacing;

/**
 * The standard theme used per default in FengGUI. It does not use
 * any external resources such that it should operate savely in any
 * environment. It is designed to be fail-safe, not particularly beautiful. 
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 12:11:38 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 323 $
 */
public class DefaultTheme extends StandardTheme
{

	@Override
	public void setUp(Button b)
	{
		b.getAppearance().setTextColor(Color.BLACK);
		b.getAppearance().setAlignment(Alignment.MIDDLE);
		b.getAppearance().setPadding(new Spacing(0, 2, 2, 1));
		b.getAppearance().setMargin(new Spacing(1, 1));
		
		//Color darkBlue = new Color(174f/255f, 174f/255f, 255f/255f);
		//Color lightBlue = new Color(200f/255f, 208f/255f, 255f/255f);

		b.getAppearance().add(Button.LABEL_DEFAULT, new GradientBackground(Color.GRAY, Color.OPAQUE));
		b.getAppearance().add(Button.LABEL_MOUSEHOVER, new PlainBackground(Color.BLUE));
		
		b.getAppearance().add(Button.LABEL_DEFAULT, new PlainBorder(Color.GRAY));
		b.getAppearance().add(Button.LABEL_MOUSEHOVER, new BevelBorder(Color.DARK_GRAY, Color.LIGHT_GRAY));
		b.getAppearance().add(Button.LABEL_MOUSEHOVER, new GradientBackground(Color.GRAY, Color.OPAQUE));
		b.getAppearance().add(Button.LABEL_PRESSED, new BevelBorder(Color.LIGHT_GRAY, Color.DARK_GRAY));
		b.getAppearance().add(Button.LABEL_FOCUSED, new PlainBorder(1, 2, 2, 1,
			new Color(200, 0, 0, 0.9f), false, Span.PADDING), false);
		
		b.getAppearance().setTextColor(Color.WHITE);
	}

	@Override
	public void setUp(CheckBox b)
	{
		b.getAppearance().setTextColor(Color.BLACK);

		BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics g = bi.getGraphics();
		g.setColor(java.awt.Color.BLACK);
		g.drawRect(0, 0, 9, 9);

		BufferedImage bi1 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		g = bi1.getGraphics();
		g.setColor(java.awt.Color.BLACK);
		g.drawRect(0, 0, 9, 9);
		g.drawLine(0, 0, 9, 9);
		g.drawLine(0, 9, 9, 0);

		b.getAppearance().add(new SetPixmapSwitch(CheckBox.LABEL_DEFAULT, new Pixmap(Binding.getInstance().getTexture(bi))));
		b.getAppearance().add(new SetPixmapSwitch(CheckBox.LABEL_SELECTED, new Pixmap(Binding.getInstance().getTexture(bi1))));

		b.getAppearance().setGap(5);
	}

	@Override
	public void setUp(RadioButton b)
	{
		b.getAppearance().setGap(5);
		b.getAppearance().setTextColor(Color.BLACK);
		b.getAppearance().setAlignment(Alignment.LEFT);

		BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics g = bi.getGraphics();
		g.setColor(java.awt.Color.BLACK);
		g.drawOval(0, 0, 9, 9);

		BufferedImage bi1 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		g = bi1.getGraphics();
		g.setColor(java.awt.Color.GREEN);
		g.fillOval(0, 0, 9, 9);
		g.setColor(java.awt.Color.BLACK);
		g.drawOval(0, 0, 9, 9);

		b.setPixmap(new Pixmap(Binding.getInstance().getTexture(bi)));
		b.getAppearance().add(new SetPixmapSwitch(RadioButton.LABEL_DEFAULT, b.getPixmap()));
		b.getAppearance().add(new SetPixmapSwitch(RadioButton.LABEL_SELECTED, new Pixmap(Binding.getInstance().getTexture(bi1))));
	}

	@Override
	public void setUp(TextEditor te)
	{
		te.getAppearance().add(new PlainBackground(Color.WHITE));
		te.getAppearance().add(new PlainBorder(Color.DARK_GRAY));
		te.getAppearance().setTextColor(Color.BLACK);
		te.getAppearance().getCursorPainter().setCursorColor(Color.BLACK);		
	}

	@Override
	public void setUp(Tree l)
	{
		l.getAppearance().add(new PlainBackground(Color.WHITE));

		BufferedImage bi = new BufferedImage(9, 9, BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics g = bi.getGraphics();
		g.setColor(java.awt.Color.WHITE);
		g.fillRect(0, 0, 9, 9);
		g.setColor(java.awt.Color.GRAY);
		g.drawRect(0, 0, 8, 8);
		g.setColor(java.awt.Color.BLACK);
		g.drawLine(5 - 1, 3 - 1, 5 - 1, 7 - 1);
		g.drawLine(3 - 1, 5 - 1, 7 - 1, 5 - 1);
		l.getAppearance().setPlusIcon(new Pixmap(Binding.getInstance().getTexture(bi)));

		bi = new BufferedImage(9, 9, BufferedImage.TYPE_INT_ARGB);
		g = bi.getGraphics();
		g.setColor(java.awt.Color.WHITE);
		g.fillRect(0, 0, 9, 9);
		g.setColor(java.awt.Color.GRAY);
		g.drawRect(0, 0, 8, 8);
		g.setColor(java.awt.Color.BLACK);
		// g.drawLine(5, 3, 5, 7);
		g.drawLine(3 - 1, 5 - 1, 7 - 1, 5 - 1);
		l.getAppearance().setMinusIcon(new Pixmap(Binding.getInstance().getTexture(bi)));
		
	}

	@Override
	public void setUp(Table w)
	{
	}

	@Override
	public void setUp(ComboBox b)
	{
		b.getAppearance().add(new PlainBorder(Color.DARK_GRAY));

		BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics g = bi.getGraphics();
		g.setColor(java.awt.Color.RED);
		g.drawString("\\/", 2, 10);

		ITexture tex = Binding.getInstance().getTexture(bi);

		b.setPixmap(new Pixmap(tex));

		b.getList().getAppearance().add(new GradientBackground(Color.WHITE, Color.WHITE_HALF_OPAQUE, Color.WHITE_HALF_OPAQUE, Color.WHITE));
		b.getPopupContainer().getAppearance().add(new PlainBorder(Color.GRAY));
	}

	@Override
	public void setUp(ScrollBar l)
	{
		if(l.isHorizontal()) 
		{
			l.getIncreaseButton().setText(">>");
			l.getDecreaseButton().setText("<<");
		} 
		else 
		{
			l.getIncreaseButton().setText(" /\\ ");
			l.getDecreaseButton().setText(" \\/ ");
		}
		
		l.getIncreaseButton().getAppearance().setMargin(Spacing.ZERO_SPACING);
		l.getDecreaseButton().getAppearance().setMargin(Spacing.ZERO_SPACING);
		
		l.getIncreaseButton().getAppearance().removeAll();
		l.getDecreaseButton().getAppearance().removeAll();

		l.getIncreaseButton().getAppearance().add(Button.LABEL_DEFAULT, new PlainBorder(Color.GRAY));
		l.getDecreaseButton().getAppearance().add(Button.LABEL_DEFAULT, new PlainBorder(Color.GRAY));

		l.getIncreaseButton().getAppearance().add(Button.LABEL_MOUSEHOVER, new PlainBorder(Color.RED));
		l.getDecreaseButton().getAppearance().add(Button.LABEL_MOUSEHOVER, new PlainBorder(Color.RED));
						
		
		l.getIncreaseButton().getAppearance().add(
			new SetTextColorSwitch(Button.LABEL_MOUSEHOVER, Color.RED));
		l.getDecreaseButton().getAppearance().add(
			new SetTextColorSwitch(Button.LABEL_MOUSEHOVER, Color.RED));

		l.getIncreaseButton().getAppearance().add(
			new SetTextColorSwitch(Button.LABEL_DEFAULT, Color.BLACK));
		l.getDecreaseButton().getAppearance().add(
			new SetTextColorSwitch(Button.LABEL_DEFAULT, Color.BLACK));
		
		l.getSlider().getSliderButton().getAppearance().add(Button.LABEL_MOUSEHOVER, new PlainBorder(Color.RED, false));
		
		l.getIncreaseButton().getAppearance().setEnabled(Button.LABEL_MOUSEHOVER, false);
		l.getDecreaseButton().getAppearance().setEnabled(Button.LABEL_MOUSEHOVER, false);
			
	}

	@Override
	public void setUp(Label l)
	{
		l.getAppearance().setTextColor(Color.BLACK);
	}

	@Override
	public void setUp(Window w)
	{
		w.getAppearance().add(new PlainBackground(Color.WHITE_HALF_OPAQUE));
		w.getAppearance().add(new PlainBorder(Color.BLUE, 3));

		if(w.getCloseButton() != null)
		{
			w.getCloseButton().setText("X");
			w.getCloseButton().getAppearance().setMargin(new Spacing(2, 2));
			w.getCloseButton().updateMinSize();
			w.getCloseButton().setSizeToMinSize();
			w.getCloseButton().setShrinkable(false);
			w.getCloseButton().setExpandable(false);
		}
		
		if(w.getMaximizeButton() != null)
		{
			w.getMaximizeButton().setText("O");
			w.getMaximizeButton().getAppearance().setMargin(new Spacing(2, 2));
			w.getMaximizeButton().updateMinSize();
			w.getMaximizeButton().setSizeToMinSize();
			w.getMaximizeButton().setShrinkable(false);
			w.getMaximizeButton().setExpandable(false);
		}

		if(w.getMinimizeButton() != null)
		{
			w.getMinimizeButton().setText("_");
			w.getMinimizeButton().getAppearance().setMargin(new Spacing(2, 2));
			w.getMinimizeButton().updateMinSize();
			w.getMinimizeButton().setSizeToMinSize();
			w.getMinimizeButton().setShrinkable(false);
			w.getMinimizeButton().setExpandable(false);
		}
		
		
		w.getTitleLabel().getAppearance().setPadding(new Spacing(0, 5, 0, 0));
		w.getTitleLabel().getAppearance().setTextColor(Color.WHITE);

		w.getTitleBar().getAppearance().add(new GradientBackground(Color.BLUE, Color.LIGHT_BLUE));
	
	}

	@Override
	public void setUp(Slider l)
	{
		Background defaultbg = new PlainBackground(Color.WHITE_HALF_OPAQUE);
		Background disabledbg = new PlainBackground(Color.BLACK_HALF_OPAQUE);
		
		l.getSliderButton().getAppearance().removeAll();
		l.getSliderButton().getAppearance().setMargin(Spacing.ZERO_SPACING);
		l.getSliderButton().getAppearance().add(defaultbg);
		l.getSliderButton().getAppearance().add(new PlainBorder(Color.LIGHT_BLUE));
		
		l.getAppearance().add(Slider.LABEL_DISABLED, disabledbg);

		if(l.isHorizontal())
		{
			PlainBorder b = new PlainBorder(1, 0, 0, 1, Color.GRAY, true, Span.BORDER);
			b.setSpan(Span.PADDING);
			l.getAppearance().add(b);
		}
		else
		{
			PlainBorder b = new PlainBorder(0, 1, 1, 0, Color.GRAY, true, Span.BORDER);
			b.setSpan(Span.PADDING);
			l.getAppearance().add(b);
		}
			
	}

	@Override
	public void setUp(ScrollContainer w)
	{
	}

	@Override
	public void setUp(ProgressBar l)
	{
		l.getAppearance().setTextColor(Color.BLACK);
		l.getAppearance().setBorder(new PlainBorder(Color.GRAY));
		l.getAppearance().setProgressBarColor(Color.LIGHT_BLUE);
	}

	@Override
	public void setUp(Container w)
	{
	}

	@Override
	public void setUpUnknown(IWidget w)
	{
	}

	@Override
	public void setUp(Menu m)
	{
		m.getAppearance().add(new GradientBackground(Color.WHITE, Color.WHITE_HALF_OPAQUE, Color.WHITE_HALF_OPAQUE, Color.WHITE));
		m.getAppearance().add(new PlainBorder(Color.GRAY));
		m.getAppearance().setTextColor(Color.BLACK);
		m.getAppearance().setDisabledColor(Color.GRAY);
		m.getAppearance().setTextSelectionColor(Color.BLACK);
		m.getAppearance().getSelectionUnderlay().add(new GradientBackground(Color.LIGHT_GRAY, Color.LIGHT_GRAY, Color.WHITE_HALF_OPAQUE, Color.WHITE));
		m.getAppearance().getSelectionUnderlay().add(new PlainBorder(1, 0, 0, 1, Color.LIGHT_GRAY, true, Span.BORDER));
	}

	@Override
	public void setUp(MenuBar mn)
	{
		mn.getAppearance().setBackground(new GradientBackground(Color.LIGHT_GRAY, Color.WHITE_HALF_OPAQUE, Color.WHITE_HALF_OPAQUE, Color.WHITE));
		mn.getAppearance().setTextColor(Color.BLACK);
		mn.getAppearance().setSelectionTextColor(Color.WHITE);
		mn.getAppearance().getSelectionUnderlay().add(new PlainBackground(Color.LIGHT_BLUE));
		mn.getAppearance().getSelectionUnderlay().add(new PlainBorder(0, 1, 1, 0, Color.GRAY, true, Span.BORDER));
	}

	@Override
	public void setUp(List l)
	{
		l.getAppearance().setTextColor(Color.BLACK);
		l.getAppearance().getMouseHoverUnderlay().add(new GradientBackground(Color.LIGHT_GRAY, Color.LIGHT_GRAY, Color.WHITE_HALF_OPAQUE, Color.WHITE));
		l.getAppearance().getMouseHoverUnderlay().add(new PlainBorder(1, 0, 0, 1, Color.LIGHT_GRAY, true, Span.BORDER));
		l.getAppearance().getSelectionUnderlay().add(new PlainBackground(Color.LIGHT_BLUE));
	}

	@Override
	public void setUp(VerticalList b)
	{
		b.getAppearance().add(new PlainBorder(Color.BLACK));
		b.getAppearance().add(new PlainBackground(Color.WHITE));		
	}

	@Override
	public void setUp(TabItemLabel b)
	{
		b.getAppearance().add(new PlainBorder(Color.BLACK));
		b.getAppearance().setTextColor(Color.BLACK);
		b.getAppearance().setPadding(new Spacing(1, 5));
		
		b.getAppearance().add(TabItemLabel.LABEL_DEFAULT, new PlainBackground(Color.LIGHT_GRAY));
		b.getAppearance().add(TabItemLabel.LABEL_MOUSE_HOVER, new PlainBackground(Color.LIGHT_BLUE));
		b.getAppearance().add(TabItemLabel.LABEL_FOCUSED, new PlainBackground(Color.WHITE));
			
	}

	@Override
	public void setUp(SplitContainer w)
	{
		w.getAppearance().getBarDecorator().add(new PlainBackground(Color.LIGHT_GRAY));
		w.getAppearance().getBarDecorator().add(new PlainBorder(Color.GRAY));
	}

	@Override
	public void setUp(Console w)
	{
		w.getAppearance().add(new PlainBackground(Color.WHITE));
		w.getAppearance().add(new PlainBorder(Color.BLACK));
		w.getAppearance().setPadding(new Spacing(5, 5, 5, 5));
	}

	@Override
	public void setUp(SnappingSlider w)
	{
		BufferedImage bi = new BufferedImage(14, 30, BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics g = bi.getGraphics();
		g.setColor(java.awt.Color.RED);
		int[] xPoints = new int[]{0,  0,  14, 14, 7};
		int[] yPoints = new int[]{10, 30, 30, 10, 0};
		g.fillPolygon(xPoints, yPoints, xPoints.length);
		//g.fillRect(0, 0, 9, 9);
/*		g.setColor(java.awt.Color.GRAY);
		g.drawRect(0, 0, 8, 8);
		g.setColor(java.awt.Color.BLACK);
		g.drawLine(5 - 1, 3 - 1, 5 - 1, 7 - 1);
		g.drawLine(3 - 1, 5 - 1, 7 - 1, 5 - 1); */
		w.setSliderPixmap(new Pixmap(Binding.getInstance().getTexture(bi)));
	}
	
}
