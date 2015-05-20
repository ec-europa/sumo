package org.fenggui.render;

import java.awt.image.BufferedImage;

public abstract class CursorFactory 
{
	private Cursor defaultCursor = null;
	private Cursor moveCursor = null;
	private Cursor textCursor = null;
	private Cursor horizontalResizeCursor = null;
	private Cursor verticalResizeCursor = null;
	private Cursor NWResizeCursor = null;
	private Cursor SWResizeCursor = null;
	private Cursor handCursor = null;
	private Cursor forbiddenCursor = null;
	
	public abstract Cursor createCursor(int xHotspot, int yHotspot, BufferedImage image);

	public Cursor getDefaultCursor() {
		return defaultCursor;
	}

	public void setDefaultCursor(Cursor defaultCursor) {
		this.defaultCursor = defaultCursor;
	}

	public Cursor getHandCursor() {
		return handCursor;
	}

	public void setHandCursor(Cursor handCursor) {
		this.handCursor = handCursor;
	}

	public Cursor getHorizontalResizeCursor() {
		return horizontalResizeCursor;
	}

	public void setHorizontalResizeCursor(Cursor horizontalResizeCursor) {
		this.horizontalResizeCursor = horizontalResizeCursor;
	}

	public Cursor getMoveCursor() {
		return moveCursor;
	}

	public void setMoveCursor(Cursor moveCursor) {
		this.moveCursor = moveCursor;
	}

	public Cursor getNWResizeCursor() {
		return NWResizeCursor;
	}

	public void setNWResizeCursor(Cursor resizeCursor) {
		NWResizeCursor = resizeCursor;
	}

	public Cursor getSWResizeCursor() {
		return SWResizeCursor;
	}

	public void setSWResizeCursor(Cursor resizeCursor) {
		SWResizeCursor = resizeCursor;
	}

	public Cursor getTextCursor() {
		return textCursor;
	}

	public void setTextCursor(Cursor textCursor) {
		this.textCursor = textCursor;
	}

	public Cursor getVerticalResizeCursor() {
		return verticalResizeCursor;
	}

	public void setVerticalResizeCursor(Cursor verticalResizeCursor) {
		this.verticalResizeCursor = verticalResizeCursor;
	}

	public Cursor getForbiddenCursor() {
		return forbiddenCursor;
	}

	public void setForbiddenCursor(Cursor forbiddenCursor) {
		this.forbiddenCursor = forbiddenCursor;
	}
	
}
