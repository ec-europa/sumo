/*
 * 
 */
package org.fenggui.menu;

import org.fenggui.render.BufferedTextRenderer;
import org.fenggui.render.ITextRenderer;


public class MenuBarItem 
{
	private Menu menu = null;
	private ITextRenderer textRenderer = new BufferedTextRenderer();
	private int width = 0;
	
	
	protected MenuBarItem(Menu menu, String name)
	{
		this.menu = menu;
		textRenderer.setText(name);
		width = textRenderer.getWidth();
	}
	
	public Menu getMenu()
	{
		return menu;
	}


	public String getName()
	{
		return textRenderer.getText();
	}
	
	public ITextRenderer getTextRenderer()
	{
		return textRenderer;
	}

	public int getWidth()
	{
		return width;
	}
	
	
}
