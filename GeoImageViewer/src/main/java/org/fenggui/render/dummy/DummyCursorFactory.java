/*
 * 
 */
package org.fenggui.render.dummy;

import java.awt.image.BufferedImage;

import org.fenggui.render.Cursor;
import org.fenggui.render.CursorFactory;

public class DummyCursorFactory extends CursorFactory
{// TODO Check this class!!!

	@Override
	public Cursor createCursor(int xHotspot, int yHotspot, BufferedImage image) {
		return null;
	}

	@Override
	public Cursor getDefaultCursor() {
		return null;
	}

	@Override
	public Cursor getHandCursor() {
		return null;
	}

	@Override
	public Cursor getMoveCursor() {
		return null;
	}

	@Override
	public Cursor getTextCursor() {
		return null;
	}

	@Override
	public Cursor getHorizontalResizeCursor() {
		return null;
	}

	@Override
	public Cursor getVerticalResizeCursor() {
		return null;
	}

	@Override
	public Cursor getNWResizeCursor() {
		return null;
	}

	@Override
	public Cursor getSWResizeCursor() {
		return null;
	}

}
