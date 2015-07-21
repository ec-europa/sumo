package org.fenggui;

import java.io.IOException;

import org.fenggui.layout.Alignment;
import org.fenggui.render.DirectTextRenderer;
import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.render.ITextRenderer;
import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;

/**
 * 
 * 
 * @author Johannes Schaback, last edited by $Author: Schabby $, $Date: 2007-08-11 13:20:15 +0200 (Sa, 11 Aug 2007) $
 * @version $Revision: 327 $
 * @dedication Frank Sinatra - Bad Leroy Brown
 */
public class LabelAppearance extends DecoratorAppearance
{
	private Color textColor = Color.BLACK;
	private ILabel label = null;
	private int gap = 5;
	private Alignment alignment = Alignment.LEFT;
	private ITextRenderer textRenderer = new DirectTextRenderer();
	
	public LabelAppearance(ILabel w)
	{
		super(w);
		this.label = w;
	}

	public Alignment getAlignment()
	{
		return alignment;
	}

	public void setAlignment(Alignment alignment)
	{
		this.alignment = alignment;
	}

	public int getGap()
	{
		return gap;
	}

	public void setGap(int gap)
	{
		this.gap = gap;
	}

	public Font getFont() 
	{
		return textRenderer.getFont();
	}

	public void setFont(Font font) 
	{
		textRenderer.setFont(font);
		label.updateMinSize();
	}

	public Color getTextColor() 
	{
		return textColor;
	}

	public void setTextColor(Color textColor)
	{
		this.textColor = textColor;
	}

	@Override
	public Dimension getContentMinSizeHint()
	{
		int width = 0;
		int height = 0;

		final String text = textRenderer.getText();
		final Pixmap pixmap = label.getPixmap();
		
		if (text != null && text.length() > 0)
		{
			width = textRenderer.getWidth();
			height = textRenderer.getHeight();
		}

		if (pixmap != null)
		{
			width += pixmap.getWidth();
			if (text != null && text.length() > 0) width += gap;
			height = Math.max(pixmap.getHeight(), height);
		}

		return new Dimension(width, height);
	}

	
	/**
	 * Updates the standard min. size for labels.
	 * @param font the font of the widget
	 * @param gap the gap between text and pixmap
	 * @param text the text of the widget
	 * @param pixmap the pixmap 
	 */
	/*
	public static void updateMinInnerSize(Widget toBeSet, Font font, int gap, String text, Pixmap pixmap)
	{
		int width = 0;
		int height = 0;

		if (text != null && text.length() > 0)
		{
			width = font.getWidth(text);
			height = font.getHeight();
		}

		if (pixmap != null)
		{
			width += pixmap.getWidth();
			if (text != null && text.length() > 0) width += gap;
			height = Math.max(pixmap.getHeight(), height);
		}

		toBeSet.setInnerMinSize(width, height);
	}*/

	public static Dimension getContentSizeHint(Font font, int gap, String text, Pixmap pixmap)
	{
		int width = 0;
		int height = 0;

		if (text != null && text.length() > 0)
		{
			width = font.getWidth(text);
			height = font.getHeight();
		}

		if (pixmap != null)
		{
			width += pixmap.getWidth();
			if (text != null && text.length() > 0) width += gap;
			height = Math.max(pixmap.getHeight(), height);
		}

		return new Dimension(width, height);
	}



	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		int x = 0;
		int y = 0;
		int width = 0;
		int height = 0;

		Pixmap pixmap = label.getPixmap();
		String text = label.getText();
		
		int contentWidth = getContentWidth();
		int contentHeight = getContentHeight();
		
		if (pixmap != null)
		{
			width = pixmap.getWidth();
			height = pixmap.getHeight();
			if (text != null && text.length() > 0) width += gap;
		}
		else if (text == null) return;

		if (text != null)
		{
			width += textRenderer.getWidth();
			height = Math.max(height, textRenderer.getHeight());
		}

		x = alignment.alignX(contentWidth, width);

		if (pixmap != null)
		{
			g.setColor(Color.WHITE);
			y = alignment.alignY(contentHeight, pixmap.getHeight());
			g.drawImage(pixmap, x, y);
			x += pixmap.getWidth() + gap;
		}

		if (text != null && text.length() > 0)
		{
			//g.setFont(font);
			if (textColor != null) g.setColor(textColor);
			y = alignment.alignY(contentHeight, textRenderer.getHeight());
			//g.drawString(text, x, y);
			textRenderer.render(x, y, g, gl);
		}
	}
	

	/**
	 * Paints a label. This method exists because several widgets require to draw
	 * lable like content (e.g. cell renderers in tables).
	 * @param g the graphics object
	 * @param font the font used to render text
	 * @param alignment the aligniment says where to place the content
	 * @param color modulation color for the text
	 * @param gap gap between text and pixmap
	 * @param pixmap pixmap (can be null)
	 * @param text text (can be null)
	 */
	public static void paint(Graphics g, Font font, Alignment alignment, 
			Color color, int gap, Pixmap pixmap,
			String text, int contentWidth, int contentHeight)
	{

		int x = 0;
		int y = 0;
		int width = 0;
		int height = 0;

		if (pixmap != null)
		{
			width = pixmap.getWidth();
			height = pixmap.getHeight();
			if (text != null && text.length() > 0) width += gap;
		}
		else if (text == null) return;

		if (text != null)
		{
			width += font.getWidth(text);
			height = Math.max(height, font.getHeight());
		}

		x = alignment.alignX(contentWidth, width);

		if (pixmap != null)
		{
			g.setColor(Color.WHITE);
			y = alignment.alignY(contentHeight, pixmap.getHeight());
			g.drawImage(pixmap, x, y);
			x += pixmap.getWidth() + gap;
		}

		if (text != null && text.length() > 0)
		{
			g.setFont(font);
			if (color != null) g.setColor(color);
			y = alignment.alignY(contentHeight, font.getHeight());
			g.drawString(text, x, y);
		}
	}



	@Override
	public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
	{
		super.process(stream);
		
		gap = stream.processAttribute("gap", gap, 5);
		alignment = stream.processEnum("alignment", alignment, Alignment.LEFT, Alignment.class, Alignment.STORAGE_FORMAT);
		
		if(stream.isInputStream()) // XXX: only support read-in at the moment :(
			setFont(stream.processChild("Font", getFont(), Font.getDefaultFont(), Font.class));
		
		textColor = stream.processChild("Color", textColor, Color.BLACK, Color.class);
	}

	public ITextRenderer getTextRenderer()
	{
		return textRenderer;
	}

	public void setTextRenderer(ITextRenderer textRenderer)
	{
		textRenderer.setFont(this.textRenderer.getFont());
		textRenderer.setText(this.textRenderer.getText());
		this.textRenderer = textRenderer;
	}



	
}
