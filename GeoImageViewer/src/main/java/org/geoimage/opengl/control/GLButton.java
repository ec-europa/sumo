package org.geoimage.opengl.control;

import java.awt.Color;
import java.awt.Font;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLDrawable;

import com.jogamp.opengl.util.awt.TextRenderer;

public class GLButton extends GLWidget{
	private String text="";
	private GL glContext=null;
	private int posx=0;
	private int posy=0;
	
	public GLButton(GL glContext,String textButton,Dimension size,int posx,int posy){
		super(size);
		this.text=textButton;
		this.glContext=glContext;
		this.posx=posx;
		this.posy=posy;
	}
	

	@Override
	public void paint(){
		// Compute maximum width of text we're going to draw
		TextRenderer textRenderer = new TextRenderer(new Font(Font.SERIF,0 , 18));
		
	    int maxTextWidth = (int) textRenderer.getBounds(this.text).getWidth();
	    maxTextWidth = Math.min(maxTextWidth+10,size.width);
		
		GL2 gl=glContext.getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glPolygonMode (GL2.GL_FRONT, GL2.GL_LINE_STRIP);
	    gl.glColor3f(0.5f, 0.5f, 0.5f);

	    gl.glBegin(GL2.GL_LINE_STRIP);
	    gl.glVertex2i( this.posx,this.posy);							//x1,y1
	    gl.glVertex2i( this.posx+maxTextWidth,this.posy );				//x2,y1
	    gl.glVertex2i( this.posx+maxTextWidth,this.posy +size.height); 	//x2,y2 
	    gl.glVertex2i( this.posx,this.posy+size.height);				//x1,y2
	    gl.glVertex2i( this.posx,this.posy);	
	    gl.glEnd( );
	    
	    
	    GLDrawable draw=gl.getGL().getContext().getGLDrawable();
		textRenderer.beginRendering(draw.getSurfaceWidth(),draw.getSurfaceHeight());
		textRenderer.setColor(Color.WHITE);
		textRenderer.setSmoothing(true);
		textRenderer.draw(this.text,(int)posx,(int)posy+5); //text and position
		textRenderer.flush();
		textRenderer.endRendering();
		
		gl.glFlush();
	}
	
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}


	
}
