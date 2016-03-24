/*
 * 
 */
package org.fenggui.render.jogl;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import org.fenggui.render.Cursor;
import org.fenggui.render.CursorFactory;

public class JOGLCursorFactory extends CursorFactory
{
	private Component component = null;
	
	public JOGLCursorFactory(Component parent) 
	{
		component = parent;
		
		setDefaultCursor(new JOGLCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR), parent));
		setMoveCursor(new JOGLCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR), parent));
		setTextCursor(new JOGLCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.TEXT_CURSOR), parent));
		setVerticalResizeCursor(new JOGLCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.N_RESIZE_CURSOR), parent));
		setHorizontalResizeCursor(new JOGLCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.E_RESIZE_CURSOR), parent));
		setNWResizeCursor(new JOGLCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.NW_RESIZE_CURSOR), parent));
		setSWResizeCursor(new JOGLCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.SW_RESIZE_CURSOR), parent));
		setHandCursor(new JOGLCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR), parent));
		setForbiddenCursor(new JOGLCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR), parent));
	}


	@Override
	public Cursor createCursor(int xHotspot, int yHotspot, BufferedImage image) 
	{
		Toolkit tk = Toolkit.getDefaultToolkit();
		return new JOGLCursor(tk.createCustomCursor(image, new Point(xHotspot, yHotspot), null), component);
	}

}
