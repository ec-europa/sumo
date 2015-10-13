package org.geoimage.opengl.control;

import org.fenggui.render.Graphics;

public class GLWidget implements IGLWidget{
	private Dimension size    = new Dimension(10, 10);
	private Dimension minSize = new Dimension(10, 10);

	private boolean shrinkable = true;
	private boolean expandable = true;
	private boolean visible = true;
	
	
	
	public GLWidget(Dimension size){
		this.size=size;
	}

	public void setMinSize(Dimension minSize) {
		this.minSize = minSize;
	}

	public void setShrinkable(boolean shrinkable) {
		this.shrinkable = shrinkable;
	}

	public void setExpandable(boolean expandable) {
		this.expandable = expandable;
	}

	@Override
	public Object getParent() {
		return null;
	}

	@Override
	public void mouseMoved(int displayX, int displayY) {
		
	}
	/*
	@Override
	public void mouseEntered(MouseEnteredEvent mouseEnteredEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseExitedEvent mouseExitedEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MousePressedEvent mp) {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public void mouseDragged(MouseDraggedEvent mp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseReleasedEvent mr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheel(MouseWheelEvent mouseWheelEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyPressedEvent keyPressedEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyReleasedEvent keyReleasedEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyTypedEvent keyTypedEvent) {
		// TODO Auto-generated method stub
		
	}*/

	@Override
	public void updateMinSize() {
		// TODO Auto-generated method stub
		
	}

	/*
	@Override
	public void focusChanged(FocusEvent focusEvent) {
		// TODO Auto-generated method stub
		
	}*/

	@Override
	public Dimension getSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimension getMinSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setX(int x) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setY(int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isTraversable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setParent(Object object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addedToWidgetTree() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void layout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSize(Dimension d) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVisible(boolean visible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isExpandable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isShrinkable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void paint() {
		
	}

}
