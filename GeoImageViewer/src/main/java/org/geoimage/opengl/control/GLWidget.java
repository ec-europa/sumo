/*
 * 
 */
package org.geoimage.opengl.control;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

public class GLWidget implements IGLWidget{
	
	protected Dimension size    = new Dimension(10, 10);
	protected Dimension minSize = new Dimension(10, 10);

	protected boolean shrinkable = true;
	protected boolean expandable = true;
	protected boolean visible = true;
	
	
	
	public GLWidget(Dimension size){
		this.size=size;
		setCamera();
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
	
	
	private void setCamera() {
        GLU glu=new GLU();
        GL2 gl2=GLU.getCurrentGL().getGL2();
        
		// Change to projection matrix.
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();
        glu.gluOrtho2D(0,gl2.getContext().getGLDrawable().getWidth(),0,
        		gl2.getContext().getGLDrawable().getHeight());
	
        // Change back to model view matrix.
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glLoadIdentity();
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
