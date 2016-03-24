/*
 * 
 */
package org.geoimage.opengl.control;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;

public class TestFrameComponent extends JFrame implements GLEventListener{
	private GLCapabilities caps;
	private GLCanvas canvas;
	private FPSAnimator animator;
	private GLU glu;
	
	public TestFrameComponent() {
		super("test");
		/*
	     * display mode (single buffer and RGBA)
	     */
		GLProfile prof=GLProfile.getDefault();
	    caps = new GLCapabilities(prof);
	    caps.setDoubleBuffered(false);
	    System.out.println(caps.toString());
	    canvas = new GLCanvas(caps);
	    canvas.addGLEventListener(this);
	    //
	    getContentPane().add(canvas);
	}

	/**
     * Declare initial window size, position, and set frame's close behavior. Open
     * window with "hello" in its title bar. Call initialization routines.
     * Register callback function to display graphics. Enter main loop and process
     * events.
     */
	public void startTest(){
	      setSize(512, 256);
	      setLocationRelativeTo(null); // center
	      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	      setVisible(true);
	}
	
	
	@Override
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		new GLButton(gl,"Test", new Dimension(100,20),40,40).paint(); 
		
	    /*
	     * don't wait! start processing buffered OpenGL routines
	     */
	    gl.glFlush();
	}

	
	
	@Override
	public void dispose(GLAutoDrawable arg0) {
		//arg0.destroy();
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		glu = new GLU();
		
	    /* select clearing color (background) color */
	    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	 
	    /* initialize viewing values */
	    gl.glMatrixMode(GL2.GL_PROJECTION);
	    gl.glLoadIdentity();
	    gl.glOrtho(0.0, 1.0, 0.0, 1.0, -1.0, 1.0);
		
	 // Start animator (which should be a field).
        animator = new FPSAnimator(drawable, 60);
        animator.start();
	}

	@Override
	public void reshape(GLAutoDrawable drawable,int x, int y, int width, int height) {
		GL gl = drawable.getGL();
        gl.glViewport(0, 0, width, height);
	}

	
	public static void main(String[] args){
		new TestFrameComponent().startTest();
	}
	
}
