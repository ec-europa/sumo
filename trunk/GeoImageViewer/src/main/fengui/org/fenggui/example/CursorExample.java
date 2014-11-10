package org.fenggui.example;

import org.fenggui.Display;
import org.fenggui.Widget;
import org.fenggui.background.Background;
import org.fenggui.background.PlainBackground;
import org.fenggui.border.Border;
import org.fenggui.border.PlainBorder;
import org.fenggui.event.mouse.MouseEnteredEvent;
import org.fenggui.event.mouse.MouseExitedEvent;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.render.Binding;
import org.fenggui.render.Cursor;
import org.fenggui.render.CursorFactory;
import org.fenggui.render.Font;
import org.fenggui.render.Graphics;
import org.fenggui.util.Color;
import org.fenggui.util.Point;

public class CursorExample implements IExample
{

	public void buildGUI(Display display) 
	{
		CursorFactory cf = Binding.getInstance().getCursorFactory();
		
		final int x = 150;
		final int y = 150;
		
		CursorWidget textCursorWidget = new CursorWidget(cf.getTextCursor(), display, "text");
		textCursorWidget.setXY(x + 20, y + 20);
		
		CursorWidget handCursorWidget = new CursorWidget(cf.getHandCursor(), display, "hand");
		handCursorWidget.setXY(x + 40 + 50, y + 20);
		
		CursorWidget moveCursorWidget = new CursorWidget(cf.getMoveCursor(), display, "move");
		moveCursorWidget.setXY(x + 60 + 100, y + 20);
		
		CursorWidget horizontalResizeCursor = new CursorWidget(cf.getHorizontalResizeCursor(), display, "E");
		horizontalResizeCursor.setXY(x + 80 + 150, y + 20);
		
		CursorWidget verticalResizeCursor = new CursorWidget(cf.getVerticalResizeCursor(), display, "N");
		verticalResizeCursor.setXY(x + 20, y + 40 + 50);
		
		CursorWidget NWResizeCursor = new CursorWidget(cf.getNWResizeCursor(), display, "NW");
		NWResizeCursor.setXY(x + 40 + 50, y + 40 + 50);
		
		CursorWidget SWResizeCursor = new CursorWidget(cf.getSWResizeCursor(), display, "SW");
		SWResizeCursor.setXY(x + 60 + 100, y + 40 + 50);
		
		display.layout();
	}

	public String getExampleName() 
	{
		return "Cursor Example";
	}

	public String getExampleDescription() 
	{
		return "Shows Various Cursors";
	}
	
	private class CursorWidget extends Widget
	{
		private Cursor cursor = null;
		private String text = "Bla";
		private Point pressed = new Point(10, 10);
		
		private Border border = null;
		private Background background = null;
		
		public CursorWidget(Cursor cursor, Display display, String text)
		{
			this.cursor = cursor;
			setSize(50, 50);
			setExpandable(false);
			setShrinkable(false);
			display.addWidget(this);
			this.text = text;
			
			border = new PlainBorder(Color.BLACK);
			background = new PlainBackground(Color.LIGHT_GRAY);
		}

		@Override
		public void mouseEntered(MouseEnteredEvent mouseEnteredEvent) 
		{
			cursor.show();
		}

		@Override
		public void mouseExited(MouseExitedEvent mouseExitedEvent) 
		{
			Binding.getInstance().getCursorFactory().getDefaultCursor().show();
		}

		@Override
		public void paint(Graphics g) 
		{
			background.paint(g, 0, 0, getWidth(), getHeight());
			border.paint(g, 0, 0, getWidth(), getHeight());
			g.setColor(Color.BLACK);
			g.setFont(Font.getDefaultFont());
			g.drawString(text, 
					getWidth()/2 - g.getFont().getWidth(text)/2,
					getHeight()/2 -  g.getFont().getHeight()/2);
			g.setColor(Color.RED);
			g.drawLine(pressed.getX()-2, pressed.getY()-2, pressed.getX()+2,pressed.getY()+2);
			g.drawLine(pressed.getX()+2, pressed.getY()-2, pressed.getX()-2,pressed.getY()+2);
		}

		@Override
		public void mousePressed(MousePressedEvent mp) 
		{
			pressed = new Point(mp.getDisplayX() - this.getX(), mp.getDisplayY() - this.getY());
		}
		
		
	}

}
