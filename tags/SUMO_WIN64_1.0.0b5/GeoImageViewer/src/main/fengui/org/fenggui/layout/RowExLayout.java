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
 * Created on 2007-8-26
 */
package org.fenggui.layout;

import java.io.IOException;
import java.util.List;

import org.fenggui.Container;
import org.fenggui.IWidget;
import org.fenggui.LayoutManager;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Dimension;

/**
 * Layouts Widgets in a vertical or horizontal row. Distributes all usable inner space equally to
 * all grabbing child widgets. Widgets need to have the RowLayoutData set or it will be set to
 * RowLayoutData.DEFAULT. The layout is able to grab available space using weights. It is also able
 * to try and fill the space (set the size of the widget to the space it gets).
 * 
 * <p>
 * It is not intended to use this in combination with the shrinkable or expandable attributes of a
 * widget, even if this values are respected by the layout. It is recommended to set both to true
 * and only use the RowLayoutData to specify the distribution.
 * </p>
 * 
 * @author Marc Menghin
 * 
 */
public class RowExLayout extends LayoutManager
{
	private boolean horizontal = true;
	private int spacing = 0;

	/**
	 * Creates a new RowLayoutEx object. It will layout all children horizontally.
	 */
	public RowExLayout()
	{
	}

	/**
	 * Creates a new RowLayoutEx object.
	 * 
	 * @param horizontal
	 *          true if the layout manager should layout the child widgets horizontally, false
	 *          otherwise. (default = true)
	 */
	public RowExLayout(boolean horizontal)
	{
		this();
		this.horizontal = horizontal;
	}

	/**
	 * Created a new RowLayoutEx object.
	 * 
	 * @param horizontal
	 *          true if the layout manager should layout the child widgets horizontally, false
	 *          otherwise. (default = true)
	 * @param spacing
	 *          The spacing that should be used between child widgets (default = 0).
	 */
	public RowExLayout(boolean horizontal, int spacing)
	{
		this(horizontal);
		this.spacing = spacing;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fenggui.LayoutManager#computeMinSize(org.fenggui.Container, java.util.List)
	 */
	@Override
	public Dimension computeMinSize(Container container, List<IWidget> content)
	{
		int minW = 0;
		int minH = 0;

		// compute the min width of the container
		for (IWidget c : content)
		{
			if (horizontal)
			{
				minW += (getValidMinWidth(c) + this.spacing);
				int height = getValidMinHeight(c);
				if (minH < height) minH = height;
			}
			else
			{
				int width = getValidMinWidth(c);
				if (minW < width) minW = width;
				minH += (getValidMinHeight(c) + this.spacing);
			}
		}

		if (horizontal)
		{
			if (minW > 0)
			{
				minW -= this.spacing;
			}
		}
		else
		{
			if (minH > 0)
			{
				minH -= this.spacing;
			}
		}

		// System.out.println("RowLayoutEx "+container+" "+minW +" "+minH);
		return new Dimension(minW, minH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fenggui.LayoutManager#doLayout(org.fenggui.Container, java.util.List)
	 */
	@Override
	public void doLayout(Container container, List<IWidget> content)
	{
		int leftSpace;

		if (horizontal)
		{
			leftSpace = container.getAppearance().getContentWidth() - computeFixedSpace(content);
		}
		else
		{
			leftSpace = container.getAppearance().getContentHeight() - computeFixedSpace(content);
		}
		computeIndividualSpace(container, content, leftSpace);
		placeWidgets(container, content);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fenggui.theme.xml.IXMLStreamable#process(org.fenggui.theme.xml.InputOutputStream)
	 */
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		horizontal = stream.processAttribute("horizontal", horizontal);
		spacing = stream.processAttribute("spacing", spacing);
	}

	/**
	 * Places the widgets in a direction based on there size.
	 * 
	 * @param widgets
	 *          widgets to place.
	 */
	private void placeWidgets(Container container, List<IWidget> widgets)
	{
		int currentPos = 0;

		if (horizontal)
		{
			for (IWidget widget : widgets)
			{
				RowExLayoutData widgetData = (RowExLayoutData) widget.getLayoutData();
				if (widgetData == null)
				{
					// System.err.println("No layout data set to widget (" + widget.toString() + ")");
					// no layout data, use default
					widgetData = RowExLayoutData.DEFAULT;
				}
				widget.setX(currentPos);
				widget.setY(placeAligned(widgetData.getAlign(), container.getAppearance().getContentHeight(), widget
						.getSize().getHeight()));
				currentPos += widget.getSize().getWidth() + spacing;
			}
		}
		else
		{
			for (int i = (widgets.size() - 1); i >= 0; i--)
			{
				IWidget widget = widgets.get(i);
				RowExLayoutData widgetData = (RowExLayoutData) widget.getLayoutData();
				if (widgetData == null)
				{
					// System.err.println("No layout data set to widget (" + widget.toString() + ")");
					// no layout data, use default
					widgetData = RowExLayoutData.DEFAULT;
				}
				widget.setX(placeAligned(widgetData.getAlign(), container.getAppearance().getContentWidth(), widget
						.getSize().getWidth()));
				widget.setY(currentPos);
				currentPos += widget.getSize().getHeight() + spacing;
			}
		}
	}

	/**
	 * Places a thing correctly aligned in the space.
	 * 
	 * @param align
	 * @param space
	 * @param size
	 * @return
	 */
	private int placeAligned(Alignment align, int space, int size)
	{
		if (align == Alignment.LEFT)
		{
			return 0;
		}
		else if (align == Alignment.RIGHT)
		{
			return space - size;
		}
		else
		{
			return (space - size) / 2;
		}
	}

	/**
	 * Calculates the space all widgets need.
	 * 
	 * @param widgets
	 *          a list of widgets
	 * @return the calculated size of all widgets
	 */
	private int computeFixedSpace(List<IWidget> widgets)
	{
		int result = 0;

		for (IWidget widget : widgets)
		{
			RowExLayoutData widgetData = (RowExLayoutData) widget.getLayoutData();
			if (widgetData == null)
			{
				// System.err.println("No layout data set to widget (" + widget.toString() + ")");
				// no layout data, use default
				widgetData = RowExLayoutData.DEFAULT;
			}

			if (!widgetData.isGrab())
			{
				if (horizontal)
				{
					result += widget.getMinSize().getWidth();
				}
				else
				{
					result += widget.getMinSize().getHeight();
				}
			}
		}

		result = result + (this.spacing * (widgets.size() - 1));

		return result;
	}

	/**
	 * Calculates the max. weight in a direction.
	 * 
	 * @param widgets
	 *          widgets to use for calculation
	 * @return the max. weight in a direction.
	 */
	private double computeMaxWeight(List<IWidget> widgets)
	{
		double result = 0.0d;

		for (IWidget widget : widgets)
		{
			RowExLayoutData widgetData = (RowExLayoutData) widget.getLayoutData();
			if (widgetData == null)
			{
				// System.err.println("No layout data set to widget (" + widget.toString() + ")");
				// no layout data, use default
				widgetData = RowExLayoutData.DEFAULT;
			}

			if (widgetData.isGrab())
			{
				result += widgetData.getWeight();
			}
		}

		return result;
	}

	/**
	 * distributes the spaceLeft to each widget that is able to expand.
	 * 
	 * @param widgets
	 *          widgets to use for calculation.
	 * @param spaceLeft
	 *          the space that is left for distribution
	 * @param maxWeight
	 *          the max. weight from all widgets
	 */
	private void computeIndividualSpace(Container container, List<IWidget> widgets, int spaceLeft)
	{
		double maxWeight = computeMaxWeight(widgets);
		double sizeUnified = spaceLeft / maxWeight;

		for (IWidget widget : widgets)
		{
			RowExLayoutData widgetData = (RowExLayoutData) widget.getLayoutData();
			if (widgetData == null)
			{
				// System.err.println("No layout data set to widget (" + widget.toString() + ")");
				// no layout data, use default
				widgetData = RowExLayoutData.DEFAULT;
			}

			if (widgetData.isGrab())
			{
				if (horizontal)
				{
					this.setValidSize(widget, (int) ((sizeUnified * widgetData.getWeight())), widget.getMinSize()
							.getHeight());
				}
				else
				{
					this.setValidSize(widget, widget.getMinSize().getWidth(), (int) ((sizeUnified * widgetData
							.getWeight())));
				}
			}
			else
			{
				this.setValidSize(widget, widget.getMinSize().getWidth(), widget.getMinSize().getHeight());
			}

			if (horizontal)
			{
				if (widgetData.isFill())
				{
					this.setValidHeight(widget, container.getAppearance().getContentHeight());
				}
			}
			else
			{
				if (widgetData.isFill())
				{
					this.setValidWidth(widget, container.getAppearance().getContentWidth());
				}
			}

		}
	}
}
