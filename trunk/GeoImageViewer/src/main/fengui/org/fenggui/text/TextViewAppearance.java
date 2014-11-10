package org.fenggui.text;

import java.io.IOException;

import org.fenggui.DecoratorAppearance;
import org.fenggui.render.DirectTextRenderer;
import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.render.IOpenGL;
import org.fenggui.render.ITextRenderer;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Color;
import org.fenggui.util.Dimension;
import org.fenggui.util.Rectangle;

public class TextViewAppearance extends DecoratorAppearance
{
	private TextView view = null;
	private Color textColor = Color.BLACK;
	private ITextRenderer textRenderer = new DirectTextRenderer();	
	
	public TextViewAppearance(TextView w)
	{
		super(w);
		view = w;
	}

	public Font getFont() 
	{
		return textRenderer.getFont();
	}

	public void setFont(Font font) 
	{
		textRenderer.setFont(font);
		view.updateMinSize();
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
		return new Dimension(view.getMinWidth(), view.fullHeight);
	}

	
	@Override
	public void process(InputOutputStream stream) throws IOException,
			IXMLStreamableException {
		super.process(stream);
		
		if(stream.isInputStream()) // XXX: only support read-in at the moment :(
			setFont(stream.processChild("Font", getFont(), Font.getDefaultFont(), Font.class));
		
		textColor = stream.processChild("Color", textColor, Color.BLACK, Color.class);		
	}

	@Override
	public void paintContent(Graphics g, IOpenGL gl)
	{
		// lower left corner of character map
		int y = getContentHeight();
		int x = 0;

		x += g.getTranslation().getX();
		y += g.getTranslation().getY();

		Rectangle clipRect = new Rectangle(g.getClipSpace());
		clipRect.setX(0);
		clipRect.setY(clipRect.getY() - view.getDisplayY() - getContentHeight());

		// TODO : Display only visible runs
		for (TextRun run : view.runs)
		{
			if (run.getBoundingRect().intersect(clipRect))
			{
				run.paint(g, x, y);
			}
		}
	}

}