/**
 * 
 */
package org.fenggui;

import java.io.IOException;

import org.fenggui.Button;
import org.fenggui.render.Binding;
import org.fenggui.render.Pixmap;
import org.fenggui.switches.SetPixmapSwitch;

public class GameMenuButton extends Button
{
	String s = "";
	public GameMenuButton(String lowlightFile, String highlightFile)
	{
		
		getAppearance().removeAll();
		try
		{
		
			Pixmap lowlight = new Pixmap(Binding.getInstance().getTexture(lowlightFile));
			Pixmap highlight = new Pixmap(Binding.getInstance().getTexture(highlightFile));
		
			getAppearance().add(new SetPixmapSwitch(Button.LABEL_DEFAULT, lowlight));
			getAppearance().add(new SetPixmapSwitch(Button.LABEL_MOUSEHOVER, highlight));
			getAppearance().add(new SetPixmapSwitch(Button.LABEL_FOCUSED, highlight));
			
			getAppearance().setEnabled(Button.LABEL_DEFAULT, true);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public String toString()
	{
		return s;
	}
}
