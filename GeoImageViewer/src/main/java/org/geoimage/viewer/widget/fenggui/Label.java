/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.widget.fenggui;

import org.fenggui.ILabel;
import org.fenggui.LabelAppearance;
import org.fenggui.ObservableLabelWidget;
import org.fenggui.render.Pixmap;

/**
 *
 * @author thoorfr
 */
public class Label extends ObservableLabelWidget implements ILabel{
        private Pixmap pixmap = null;
	private LabelAppearance appearance = null;

	/**
	 * Creates a new label with a given text.
	 * @param text the text
	 */
	public Label(String text)
	{
		initializeAppearance();
		setupTheme(Label.class);
		setText(text);
	}
	
	public void setAppearance(LabelAppearance appearance)
	{
		this.appearance = appearance;
	}

	/**
	 * Initialize the Label's widget appearance. Override this method to initialize own LabelAppearance
	 */
	protected void initializeAppearance()
	{
		appearance = new LabelAppearance(this);
	}


	@Override
	public LabelAppearance getAppearance()
	{
		return appearance;
	}


	public Pixmap getPixmap()
	{
		return pixmap;
	}


	public void setPixmap(Pixmap pixmap)
	{
		this.pixmap = pixmap;
		updateMinSize();
	}


	/**
	 * Creates a new empty label
	 *
	 */
	public Label()
	{
		this(null);
	}


	/* (non-Javadoc)
	 * @see org.fenggui.ITextWidget#getText()
	 */
	public String getText()
	{
		return getAppearance().getTextRenderer().getText();
	}


	/* (non-Javadoc)
	 * @see org.fenggui.ITextWidget#setText(java.lang.String)
	 */
	public void setText(String text)
	{
		getAppearance().getTextRenderer().setText(text);
		updateMinSize();
	}

    
}
