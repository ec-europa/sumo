/*
 * FengGUI - Java GUIs in OpenGL (http://www.fenggui.org)
 *
 * Copyright (c) 2005, 2006 FengGUI Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details:
 * http://www.gnu.org/copyleft/lesser.html#TOC3
 *
 * Created on Apr 19, 2005
 * $Id: JOGLOpenGL.java 329 2007-08-11 14:54:44Z Schabby $
 */
package org.fenggui.render.jogl;

import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.fenggui.render.IOpenGL;


/**
 * The implementation of the OpenGL interface for JOGL.
 *
 * @author Johannes, Graham, last edited by $Author: Schabby $, $Date: 2007-08-11 16:54:44 +0200 (Sa, 11 Aug 2007) $
 */
public class JOGLOpenGL implements IOpenGL {

    private GL2 gl;
    private GLU glu;

    /**
     * Creates a new <code>JOGLOpenGL</code> instance.
     * @param gl the JOGL binding
     */
    protected JOGLOpenGL(GL gl)
    {
        this.gl = gl.getGL2();
        glu = new GLU();
    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#setModelMatrixMode()
     */
    public void setModelMatrixMode() {
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#setProjectionMatrixMode()
     */
    public void setProjectionMatrixMode() {
        gl.glMatrixMode(GL2.GL_PROJECTION);

    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#pushMatrix()
     */
    public void pushMatrix() {
        gl.glPushMatrix();

    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#popMatrix()
     */
    public void popMatrix() {
        gl.glPopMatrix();
    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#loadIdentity()
     */
    public void loadIdentity() {
        gl.glLoadIdentity();
    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#pushAllAttribs()
     */
    public void pushAllAttribs() {
        gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#popAllAttribs()
     */
    public void popAllAttribs() {
        gl.glPopAttrib();
    }

    /* (non-Javadoc)
	 * @see org.fenggui.render.IOpenGL#getBoolean(org.fenggui.render.IOpenGL.Attribute)
	 */
	public boolean[] getBoolean(Attribute attrib) {
		int pname = getAttrib(attrib);
		ByteBuffer buf = ByteBuffer.allocateDirect(16);
		gl.glGetBooleanv(pname, buf);

		boolean[] result = new boolean[buf.capacity()];
		for(int i = 0; i < result.length; i++)
			result[i] = (buf.get() == 1);

		return result;
	}

	/* (non-Javadoc)
	 * @see org.fenggui.render.IOpenGL#getDouble(org.fenggui.render.IOpenGL.Attribute)
	 */
	public double[] getDouble(Attribute attrib) {
		int pname = getAttrib(attrib);
		DoubleBuffer buf = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder()).asDoubleBuffer();
		gl.glGetDoublev(pname, buf);

		double[] result = new double[buf.capacity()];
		for(int i = 0; i < result.length; i++)
			result[i] = buf.get();

		return result;
	}

	/* (non-Javadoc)
	 * @see org.fenggui.render.IOpenGL#getFloat(org.fenggui.render.IOpenGL.Attribute)
	 */
	public float[] getFloat(Attribute attrib) {
		int pname = getAttrib(attrib);
		FloatBuffer buf = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder()).asFloatBuffer();
		gl.glGetFloatv(pname, buf);

		float[] result = new float[buf.capacity()];
		for(int i = 0; i < result.length; i++)
			result[i] = buf.get();

		return result;
	}

	/* (non-Javadoc)
	 * @see org.fenggui.render.IOpenGL#getInt(org.fenggui.render.IOpenGL.Attribute)
	 */
	public int[] getInt(Attribute attrib) {
		int pname = getAttrib(attrib);
		IntBuffer buf = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder()).asIntBuffer();
		gl.glGetIntegerv(pname, buf);

		int[] result = new int[buf.capacity()];
		for(int i = 0; i < result.length; i++)
			result[i] = buf.get();

		return result;
	}

	/* (non-Javadoc)
	 * @see org.fenggui.render.IOpenGL#getString(org.fenggui.render.IOpenGL.Attribute)
	 */
	public String getString(Attribute attrib) {
		int pname = getAttrib(attrib);
		return gl.glGetString(pname);
	}

	public void enable(Attribute attrib) {
    	gl.glEnable(getAttrib(attrib));
    }

    public void disable(Attribute attrib) {
    	gl.glDisable(getAttrib(attrib));
    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#enableTexture2D()
     */
    public void enableTexture2D(boolean b) {
        if(b) gl.glEnable(GL_TEXTURE_2D);
        else gl.glDisable(GL_TEXTURE_2D);
    }

    public void enableLighting(boolean b) {
        if(b) gl.glEnable(GL2.GL_LIGHTING);
        else gl.glDisable(GL2.GL_LIGHTING);
    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#setViewPort(int, int, int, int)
     */
    public void setViewPort(int x, int y, int width, int height)
    {
        gl.glViewport(x, y, width, height);
    }


    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#setDepthFunctionToLess()
     */
    public void setDepthFunctionToLEqual() {
        gl.glDepthFunc(GL_LEQUAL);

    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#translate(int, int, int)
     */
    public void translateZ(float z) {
        gl.glTranslatef(0, 0, z);
    }

	/* (non-Javadoc)
	 * @see org.fenggui.render.IOpenGL#rotate(double)
	 */
	public void rotate(float angle) {
		gl.glRotatef(angle, 0, 0, 1);
	}

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#setTextureDecal()
     */
    public void setTextureDecal() {
        //gl.glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#end()
     */
    public void end() {
        gl.glEnd();
    }

    public int genLists(int range) {
    	return gl.glGenLists(range);
    }

    public void startList(int list) {
    	gl.glNewList(list, GL2.GL_COMPILE);
    }

    public void endList() {
    	gl.glEndList();
    }

    public void callList(int list) {
    	gl.glCallList(list);
    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#startQuads()
     */
    public void startQuads() {
        gl.glBegin(GL2.GL_QUADS);
    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#startLines()
     */
    public void startLines() {
        gl.glBegin(GL_LINES);

    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#startTriangles()
     */
    public void startTriangles() {
        gl.glBegin(GL_TRIANGLES);

    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#startTriangleStrip()
     */
    public void startTriangleStrip() {
        gl.glBegin(GL_TRIANGLE_STRIP);

    }

    /*
     * (non-Javadoc)
     */
    public void startTriangleFan() {
    	gl.glBegin(GL_TRIANGLE_FAN);
    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#startQuadStrip()
     */
    public void startQuadStrip() {
        gl.glBegin(GL2.GL_QUAD_STRIP);

    }

    public void startPoints() {
    	gl.glBegin(GL_POINTS);
    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#vertex(float, float)
     */
    public void vertex(float x, float y) {
       gl.glVertex2f(x, y);
    }

    public void rect(float x1, float y1, float x2, float y2) {
    	gl.glRectf(x1, y1, x2, y2);
    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#texCoord(float, float)
     */
    public void texCoord(float x, float y) {
        gl.glTexCoord2f(x, y);
    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#color(float, float, float)
     */
    public void color(float red, float green, float blue, float alpha) {
        gl.glColor4f(red, green, blue, alpha);
    }

    /* (non-Javadoc)
     * @see joglui.binding.OpenGL#scale(float, float)
     */
    public void scale(float scaleX, float scaleY) {
        gl.glScalef(scaleX, scaleY, 0);
    }

    /* (non-Javadoc)
     * @see joglui.render.OpenGL#setTexEnvModeDecal()
     */
    public void setTexEnvModeDecal() {
        //gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_DECAL);
        //gl.glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_COLOR, GL_LUMINANCE_ALPHA);
    }

    /* (non-Javadoc)
     * @see joglui.render.OpenGL#setTexEnvModeModulate()
     */
    public void setTexEnvModeModulate() {
        gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);

    }

    /* (non-Javadoc)
     * @see joglui.render.OpenGL#translateXY(int, int)
     */
    public void translateXY(int x, int y) {
        gl.glTranslatef(x, y, 0);
    }

    public void setupBlending() {
        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setupStateVariables(boolean depthTestEnabled) {

        //TODO many of these commands are redundant

        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        if(depthTestEnabled) gl.glDisable(GL_DEPTH_TEST);

        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glDisable(GL2.GL_FOG);
        gl.glDisable(GL.GL_DITHER);

        gl.glEnable(GL.GL_SCISSOR_TEST);

        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glDisable(GL2.GL_LINE_STIPPLE);


        //gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
        //gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);

        //gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        //gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);




        gl.glTexEnvf(GL2.GL_TEXTURE_ENV,GL2.GL_TEXTURE_ENV_MODE, GL2.GL_DECAL);

        //gl.glDisable(GL_TEXTURE_GEN_S);
        gl.glDisable(GL_CULL_FACE);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glFrontFace(GL_CW);
        gl.glCullFace(GL_BACK);
                // disabling textures after setting state values. They would
                // be ignored otherwise (i think)
        gl.glDisable(GL_TEXTURE_2D);
        gl.glDisable(GL2.GL_TEXTURE_1D);

        //gl.glTranslatef(0.375f, 0.375f, 0);
    }

    public void startLineStrip() {
        gl.glBegin(GL.GL_LINE_STRIP);
    }

    public void startLineLoop() {
        gl.glBegin(GL.GL_LINE_LOOP);
    }

    public void enableStipple()
    {
        gl.glEnable(GL2.GL_LINE_STIPPLE);
    }

    public void disableStipple()
    {
        gl.glDisable(GL2.GL_LINE_STIPPLE);
    }

    public void lineStipple(int stretch, short pattern)
    {
        gl.glLineStipple(stretch, pattern);
    }

    public void lineWidth(float width)
    {
        gl.glLineWidth(width);
    }

    public void pointSize(float size)
	{
		gl.glPointSize(size);
	}

    public void enableAlpha(boolean state)
    {
        if (state)
        {
            gl.glEnable(GL.GL_ALPHA);
        }
        else
        {
            gl.glDisable(GL.GL_ALPHA);
        }
    }

	public void readPixels(int x, int y, int width, int height, ByteBuffer bgr) {
		gl.glReadPixels(x, y, width, height, GL2.GL_BGR,
				GL.GL_UNSIGNED_BYTE, bgr);
	}

	public void setOrtho2D(int left, int right, int bottom, int top)
	{
		glu.gluOrtho2D(left, right, bottom, top);
	}

	public void setScissor(int x, int width, int y, int height)
	{
		gl.glScissor(x, y, width, height);
	}

	public void activateTexture(int i)
	{
		gl.glActiveTexture(GL.GL_TEXTURE0 + i);
	}

	public void rotate(float angle, int x, int y, int z)
	{
		gl.glRotated(angle, x, y, z);
	}

	private int getAttrib(Attribute attrib)
	{
		switch(attrib)
		{
			case CURRENT_COLOR:
				return GL2.GL_CURRENT_COLOR;
			case LINE_WIDTH:
				return GL_LINE_WIDTH;
			case POINT_SIZE:
				return GL_POINT_SIZE;
			default:
				return 0;
		}
	}
}
