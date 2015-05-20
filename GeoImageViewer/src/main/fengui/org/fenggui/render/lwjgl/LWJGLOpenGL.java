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
 * $Id: LWJGLOpenGL.java 276 2007-05-02 20:16:56Z whackjack $
 */
package org.fenggui.render.lwjgl;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.fenggui.render.IOpenGL;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.glu.GLU;

/**
 * @author oliver_carr
 *
 */
public class LWJGLOpenGL implements IOpenGL {

	/**
	 *
	 */
    protected LWJGLOpenGL() {
    }

    public void setModelMatrixMode() {
    	GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    public void setProjectionMatrixMode() {
    	GL11.glMatrixMode(GL11.GL_PROJECTION);
    }

    public void pushMatrix() {
    	GL11.glPushMatrix();
    }

    public void popMatrix() {
    	GL11.glPopMatrix();
    }

    public void loadIdentity() {
    	GL11.glLoadIdentity();
    }

    public void pushAllAttribs() {
    	GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    }

    public void popAllAttribs() {
    	GL11.glPopAttrib();
    }

    public boolean[] getBoolean(Attribute attrib) {
		int pname = getAttrib(attrib);
		ByteBuffer buf = BufferUtils.createByteBuffer(16);
		GL11.glGetBoolean(pname, buf);

		boolean[] result = new boolean[buf.capacity()];
		for(int i = 0; i < result.length; i++)
			result[i] = (buf.get() == 1);

		return result;
	}

	public double[] getDouble(Attribute attrib) {
		int pname = getAttrib(attrib);
		DoubleBuffer buf = BufferUtils.createDoubleBuffer(16);
		GL11.glGetDouble(pname, buf);

		double[] result = new double[buf.capacity()];
		for(int i = 0; i < result.length; i++)
			result[i] = buf.get();

		return result;
	}

	public float[] getFloat(Attribute attrib) {
		int pname = getAttrib(attrib);
		FloatBuffer buf = BufferUtils.createFloatBuffer(16);
		GL11.glGetFloat(pname, buf);

		float[] result = new float[buf.capacity()];
		for(int i = 0; i < result.length; i++)
			result[i] = buf.get();

		return result;
	}

	public int[] getInt(Attribute attrib) {
		int pname = getAttrib(attrib);
		IntBuffer buf = BufferUtils.createIntBuffer(16);
		GL11.glGetInteger(pname, buf);

		int[] result = new int[buf.capacity()];
		for(int i = 0; i < result.length; i++)
			result[i] = buf.get();

		return result;
	}

	public String getString(Attribute attrib) {
		int pname = getAttrib(attrib);
		return GL11.glGetString(pname);
	}

	public void enable(Attribute attrib) {
    	GL11.glEnable(getAttrib(attrib));
    }

    public void disable(Attribute attrib) {
    	GL11.glDisable(getAttrib(attrib));
    }

    public void enableLighting(boolean b) {
        if (b) GL11.glEnable(GL11.GL_LIGHTING);
        else GL11.glDisable(GL11.GL_LIGHTING);
    }

    public void enableTexture2D(boolean b) {
        if (b) GL11.glEnable(GL11.GL_TEXTURE_2D);
        else GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    public void setTexEnvModeDecal() {
        //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_DECAL);
        //GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, GL11.GL_LUMINANCE_ALPHA);
    }

    public void setTexEnvModeModulate() {
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        //GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
    }

    public void setViewPort(int x, int y, int width, int height) {
    	GL11.glViewport(x, y, width, height);
    }

    public void setOrtho(double left, double right, double bottom, double top,
            double near, double far) {
    	GL11.glOrtho(left, right, bottom, top, near, far);
    }

    public void setDepthFunctionToLEqual() { // XXX: special case function...
     	GL11.glDepthFunc(GL11.GL_LEQUAL);
    }

    public void translateZ(float z) {
    	GL11.glTranslatef(0, 0, z);
    }

    public void translateXY(int x, int y) {
    	GL11.glTranslatef(x, y, 0);
    }

	/* (non-Javadoc)
	 * @see org.fenggui.render.IOpenGL#rotate(float)
	 */
	public void rotate(float angle) {
		GL11.glRotatef(angle, 0, 0, 1);
	}

    public void end() {
    	GL11.glEnd();
    }

    public int genLists(int range) {
    	return GL11.glGenLists(range);
    }

    public void startList(int list) {
    	GL11.glNewList(list, GL11.GL_COMPILE);
    }

    public void endList() {
    	GL11.glEndList();
    }

    public void callList(int list) {
    	GL11.glCallList(list);
    }

    public void startQuads() {
    	GL11.glBegin(GL11.GL_QUADS);
    }

    public void startLines() {
    	GL11.glBegin(GL11.GL_LINES);
    }

    public void startLineStrip() {
    	GL11.glBegin(GL11.GL_LINE_STRIP);
    }

    public void startLineLoop() {
    	GL11.glBegin(GL11.GL_LINE_LOOP);
    }

    public void startTriangles() {
    	GL11.glBegin(GL11.GL_TRIANGLES);
    }

    public void startTriangleStrip() {
    	GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
    }

    public void startTriangleFan() {
    	GL11.glBegin(GL11.GL_TRIANGLE_FAN);
    }

    public void startQuadStrip() {
    	GL11.glBegin(GL11.GL_QUAD_STRIP);
    }

    public void startPoints() {
    	GL11.glBegin(GL11.GL_POINTS);
    }

    public void vertex(float x, float y) {
    	GL11.glVertex2f(x, y);
    }

    public void rect(float x1, float y1, float x2, float y2) {
    	GL11.glRectf(x1, y1, x2, y2);
    }

    public void texCoord(float x, float y) {
    	GL11.glTexCoord2f(x, y);
    }

    public void color(float red, float green, float blue, float alpha) {
    	GL11.glColor4f(red, green, blue, alpha);
    }

    public void scale(float scaleX, float scaleY) {
    	GL11.glScalef(scaleX, scaleY, 0);
    }

    //public void setTextureDecal();
    public void setupBlending() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setupStateVariables(boolean depthTestEnabled) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if(depthTestEnabled) GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_DITHER);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        //GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glEnable(GL11.GL_NORMALIZE);
        GL11.glDisable(GL11.GL_LINE_STIPPLE);


        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_DECAL);

        //GL11.glDisable(GL11.GL_TEXTURE_GEN_S);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_NORMALIZE);
        GL11.glFrontFace(GL11.GL_CW);
        GL11.glCullFace(GL11.GL_BACK);
                // disabling textures after setting state values. They would
                // be ignored otherwise (i think)
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_TEXTURE_1D);

    }

    public void lineWidth(float width) {
    	GL11.glLineWidth(width);
    }

    public void pointSize(float size) {
    	GL11.glPointSize(size);
    }

    public void enableStipple() {
    	GL11.glEnable(GL11.GL_LINE_STIPPLE);
    }

    public void disableStipple() {
    	GL11.glDisable(GL11.GL_LINE_STIPPLE);
    }

    public void lineStipple(int stretch, short pattern) {
    	GL11.glLineStipple(stretch, pattern);
    }

    public void enableAlpha(boolean state) {
        if (state) {
            GL11.glEnable(GL11.GL_ALPHA);
        } else {
            GL11.glDisable(GL11.GL_ALPHA);
        }
    }

	public void readPixels(int x, int y, int width, int height, ByteBuffer bgr)
	{
		GL11.glReadPixels(x, y, width, height, GL12.GL_BGR ,GL11.GL_UNSIGNED_BYTE, bgr);
	}

	public void setOrtho2D(int left, int right, int bottom, int top)
	{
		GLU.gluOrtho2D(left, right, bottom, top);
	}

	public void setScissor(int x, int width, int y, int height)
	{
		GL11.glScissor(x, y, width, height);
	}

	public void activateTexture(int i)
	{
		try
		{
			GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
		}
		catch (java.lang.IllegalStateException e)
		{
			// Unsupported, ignore.
		}
	}

	public void rotate(float angle, int x, int y, int z)
	{
		GL11.glRotatef(angle, x, y, z);
	}

	private int getAttrib(Attribute attrib)
	{
		switch(attrib)
		{
			case CURRENT_COLOR:
				return GL11.GL_CURRENT_COLOR;
			case LINE_WIDTH:
				return GL11.GL_LINE_WIDTH;
			case POINT_SIZE:
				return GL11.GL_POINT_SIZE;
			default:
				return 0;
		}
	}
}
