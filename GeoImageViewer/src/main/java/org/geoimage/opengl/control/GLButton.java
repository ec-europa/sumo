package org.geoimage.opengl.control;

import java.awt.Color;
import java.awt.Font;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.jogamp.opengl.util.awt.TextRenderer;

public class GLButton extends GLWidget{
	private String text="";
	private GL glContext=null;
	
	public GLButton(GL glContext,String textButton,Dimension size){
		super(size);
		this.text=textButton;
		this.glContext=glContext;
	}
	

	@Override
	public void paint(){
		GL2 gl=glContext.getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
	    gl.glColor3f(0.5f, 0.5f, 0.5f);
	    gl.glRecti(20,20,25,25);
		
		TextRenderer textRenderer = new TextRenderer(new Font(Font.SERIF,0 , 30));
		textRenderer.beginRendering(600,300);
		textRenderer.setColor(Color.WHITE);
		textRenderer.setSmoothing(false);
		textRenderer.draw("Hello world!!",10,10);
		textRenderer.endRendering();
		
		
	}
	
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}


	
}
