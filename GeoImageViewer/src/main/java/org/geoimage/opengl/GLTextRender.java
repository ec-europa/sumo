/*
 * 
 */
package org.geoimage.opengl;

import java.awt.*;
import java.awt.event.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import org.geoimage.viewer.core.SumoPlatform;

import com.jogamp.opengl.util.gl2.GLUT;
 

public class GLTextRender {
		 
	public class BitMapFont
	{
	  {
	    Frame frame = new Frame("BitMap Fonts");
	    GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities( glprofile );
	    GLCanvas canvas = new GLCanvas(glcapabilities);
	    //canvas.addGLEventListener(new GLTextRender());
	    frame.add(canvas);
	    frame.setSize(400, 300);
	   
	    frame.addWindowListener(new WindowAdapter()
	    {
	      public void windowClosing(WindowEvent e)
	      {
	       
	        System.exit(0);
	      }
	    });
	    frame.show();
	    canvas.requestFocus();
	   
	  }
	 
	       
	    public void display(GLDrawable gLDrawable)
	    {
	      String [] fonts = { "BitMap 9 by 15", "BitMap 8 by 13",
	            "Times Roman 10 Point ", "Times Roman 24 Point ",
	            "Helvetica 10 Point ","Helvetica 12 Point ","Helvetica 18 Point "};
	           
	      final GL2 gl = SumoPlatform.getApplication().getGeoContext().getGL().getGL2();
	      final GLUT glut = new GLUT();
	 
	      gl.glClear (GL.GL_COLOR_BUFFER_BIT);  // Set display window to color.
	      gl.glColor3f (0.0f, 0.0f, 0.0f);  // Set text e.color to black
	      gl.glMatrixMode (GL2.GL_MODELVIEW);
	      gl.glLoadIdentity();
	     
	      int x = 20, y=15;
	      for (int i=0; i<7;i++){
	            gl.glRasterPos2i(x,y); // set position
	            glut.glutBitmapString(i+2, fonts[i]);
	             y+= 20;
	      }
	    
	    }
	   
	   
	       public void displayChanged(GLDrawable gLDrawable, boolean modeChanged, boolean deviceChanged)
	    {
	    }
	   
	    public void init(GLAutoDrawable gLDrawable)
	    {
	      final GL2  gl =   gLDrawable.getContext().getGL().getGL2();
	      final GLUT glu =  new GLUT();
	           
	      gl.glMatrixMode (GL2.GL_PROJECTION); 
	      gl.glClearColor (1.0f, 1.0f, 1.0f, 0.0f);   //set background to white
	      //glu.gluOrtho2D (0.0, 200.0, 0.0, 150.0);  // define drawing area
	      //gLDrawable.addKeyListener(this);
	    }
	   
	       public void reshape(GLDrawable gLDrawable, int x, int y, int width, int height)
	    {
	     
	           
	    }
	 
	   
	    public void keyPressed(KeyEvent e)
	    {
	      if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
	        System.exit(0);
	    }
	      
	    public void keyReleased(KeyEvent e) {}
	   
	    public void keyTyped(KeyEvent e) {}
	      }

	 
}
