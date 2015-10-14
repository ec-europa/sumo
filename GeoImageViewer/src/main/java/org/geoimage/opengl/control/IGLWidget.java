package org.geoimage.opengl.control;


public interface IGLWidget {

	/*
	 */
	public Object getParent();

	/**
	 * Called when the mouse enters this Widget
	 * 
	 * @param mouseEnteredEvent
	 *            event type
	 *
	public void mouseEntered(MouseEnteredEvent mouseEnteredEvent);

	/**
	 * Called when the mouse exits this Widget
	 * 
	 * @param mouseExitedEvent
	 *            event type
	 *
	public void mouseExited(MouseExitedEvent mouseExitedEvent);

	/**
	 * Called when a mouse button is pressed on this Widget.
	 * 
	 * @param mp
	 *            event type
	 *
	public void mousePressed(MousePressedEvent mp);*/

	/**
	 * Called when the mouse is moved over this Widget. Do not make
	 * computationally expensive things here because this method is constantly
	 * called by Display
	 * 
	 * @todo evaluate the idea to introduce a flag for Containers that indicate
	 *       whether the Container holds mouse-over sensitive Widgets to avoid
	 *       fining the underlying Widget on every mouse move event. #
	 * 
	 * @param displayX
	 *            the x coordinate of the mouse cursor in display coordinates
	 * @param displayY
	 *            the y coordinate of the mouse cursor in display coordinates
	 */
	public void mouseMoved(int displayX, int displayY);

	/**
	 * Called when the mouse is dragged (moved while pressing a mouse button
	 * down) over this Widget.
	 * 
	 * @todo mouseDragged is acutally only a special case of mouseMoved.
	 *       Consider to merge both events #
	 * @param mp
	 *            event type
	 *
	public void mouseDragged(MouseDraggedEvent mp);

	/**
	 * Called when a previously pressed mouse button is released on this Widget.
	 * 
	 * @param mr
	 *            event type
	 *
	public void mouseReleased(MouseReleasedEvent mr);

	/**
	 * Called when there is a mouse wheel event
	 * 
	 * @param mouseWheelEvent
	 *            event type
	 *
	public void mouseWheel(MouseWheelEvent mouseWheelEvent);

	/**
	 * Called when a key is pressed on the keyboard providing this Widget has
	 * the focus.
	 * 
	 * @param keyPressedEvent
	 *            event type
	 *
	public void keyPressed(KeyPressedEvent keyPressedEvent);

	/**
	 * Called when a previously pressed key on the keyboard is released,
	 * providing this Widget has the focus.
	 * 
	 * @param keyReleasedEvent
	 *            event type
	 *
	public void keyReleased(KeyReleasedEvent keyReleasedEvent);

	/**
	 * Called when a key is Typed.
	 * 
	 * @param keyTypedEvent
	 *            event type
	 *
	public void keyTyped(KeyTypedEvent keyTypedEvent);*/

	/*
	 */
	public void updateMinSize();

	/**
	 * Called when a widget gets the focus
	 *
	 */
	//public void focusChanged(FocusEvent focusEvent);

	public Dimension getSize();

	public Dimension getMinSize();

	public int getX();

	public int getY();

	public void setX(int x);

	public void setY(int y);

	/*
	 */
	public boolean isTraversable();

	public void setParent(Object object);

	public void addedToWidgetTree();

	public void layout();

	public void setSize(Dimension d);

	/**
	 * Checks if the widget is set to be visible or not. Doesn't check if the
	 * widget is actually drawn on screen.
	 * 
	 * @return true if the widget should be visible.
	 */
	public boolean isVisible();

	/**
	 * Sets the visibility state of the widget. If set to false the widget will
	 * not be drawn and will not receive any events but takes its space (remains
	 * in the widget-tree). Also all child elements will not be drawn or receive
	 * any events.
	 * 
	 * @param visible
	 *            True if the widget should be drawn, false otherwise.
	 */
	public void setVisible(boolean visible);

	public boolean isExpandable();

	public boolean isShrinkable();

	public void paint();

}