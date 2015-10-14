package org.geoimage.opengl.control;

import java.awt.Color;
import java.awt.Font;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

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
		GL2 gl=glContext.getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glPolygonMode (GL2.GL_FRONT, GL2.GL_LINE_STRIP);
	    gl.glColor3f(0.5f, 0.5f, 0.5f);
	    gl.glRecti(this.posx,this.posy,80,20);
		
		
		TextRenderer textRenderer = new TextRenderer(new Font(Font.SERIF,0 , 20));
		textRenderer.beginRendering(700,300);//size
		textRenderer.setColor(Color.WHITE);
		textRenderer.setSmoothing(false);
		textRenderer.draw(this.text,this.posx,this.posy); //text and position
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
