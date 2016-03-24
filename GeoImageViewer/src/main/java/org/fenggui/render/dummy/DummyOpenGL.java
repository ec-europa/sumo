/*
 * 
 */
package org.fenggui.render.dummy;

import java.nio.ByteBuffer;

import org.fenggui.render.IOpenGL;

public class DummyOpenGL implements IOpenGL {

	
	
	public void setModelMatrixMode() {
		// does nothing! It's a dummy implementation
		
	}

	public void setProjectionMatrixMode() {
		// does nothing! It's a dummy implementation
		
	}

	public void pushMatrix() {
		// does nothing! It's a dummy implementation
		
	}

	public void popMatrix() {
		// does nothing! It's a dummy implementation
		
	}

	public void loadIdentity() {
		// does nothing! It's a dummy implementation
		
	}

	public void pushAllAttribs() {
		// does nothing! It's a dummy implementation
		
	}

	public void popAllAttribs() {
		// does nothing! It's a dummy implementation
		
	}

	public void enableTexture2D(boolean b) {
		// does nothing! It's a dummy implementation
		
	}

	public void setTexEnvModeDecal() {
		// does nothing! It's a dummy implementation
		
	}

	public void setTexEnvModeModulate() {
		// does nothing! It's a dummy implementation
		
	}

	public void setViewPort(int x, int y, int width, int height) {
		// does nothing! It's a dummy implementation
		
	}
	
	public void setDepthFunctionToLEqual() {
		// does nothing! It's a dummy implementation
		
	}

	public void translateZ(float z) {
		// does nothing! It's a dummy implementation
		
	}

	public void translateXY(int x, int y) {
		// does nothing! It's a dummy implementation
		
	}

	public void rotate(float angle) {
		// does nothing! It's a dummy implementation
		
	}
	
	public void end() {
		// does nothing! It's a dummy implementation
		
	}

	public void startQuads() {
		// does nothing! It's a dummy implementation
		
	}

	public void startLines() {
		// does nothing! It's a dummy implementation
		
	}

	public void startLineStrip() {
		// does nothing! It's a dummy implementation
		
	}

	public void startLineLoop() {
		// does nothing! It's a dummy implementation
		
	}

	public void startTriangles() {
		// does nothing! It's a dummy implementation
		
	}

	public void startTriangleStrip() {
		// does nothing! It's a dummy implementation
		
	}

	public void startQuadStrip() {
		// does nothing! It's a dummy implementation
		
	}

	public void startPoints() {
		
	}
	
	public void vertex(float x, float y) {
		// does nothing! It's a dummy implementation
		
	}

	public void texCoord(float x, float y) {
		// does nothing! It's a dummy implementation
		
	}

	public void color(float red, float green, float blue, float alpha) {
		// does nothing! It's a dummy implementation
		
	}

	public void scale(float scaleX, float scaleY) {
		// does nothing! It's a dummy implementation
		
	}

	public void setupBlending() {
		// does nothing! It's a dummy implementation
		
	}

	public void enableLighting(boolean b) {
		// does nothing! It's a dummy implementation
		
	}

	public void setupStateVariables(boolean disableDepthTest) {
		// does nothing! It's a dummy implementation
		
	}

	public void lineWidth(float width) {
		// does nothing! It's a dummy implementation
		
	}

	public void enableStipple() {
		// does nothing! It's a dummy implementation
		
	}

	public void disableStipple() {
		// does nothing! It's a dummy implementation
		
	}

	public void lineStipple(int stretch, short pattern) {
		// does nothing! It's a dummy implementation
		
	}

	public void enableAlpha(boolean state) {
		// does nothing! It's a dummy implementation
		
	}

	public void readPixels(int x, int y, int width, int height, ByteBuffer bgr) {
		// does nothing! It's a dummy implementation
		
	}

	public void setOrtho2D(int x, int width, int y, int height) {
		// does nothing! It's a dummy implementation
		
	}

	public void setScissor(int x, int width, int y, int height) {
		// does nothing! It's a dummy implementation
		
	}

	public void activateTexture(int i)
	{
		// does nothing! It's a dummy implementation
	}

	public void rotate(float angle, int x, int y, int z)
	{
		// does nothing! It's a dummy implementation
	}

	public void disable(Attribute attrib) {
		// does nothing! It's a dummy implementation
		
	}

	public void enable(Attribute attrib) {
		// does nothing! It's a dummy implementation
		
	}

	public void rect(float x1, float y1, float x2, float y2) {
		// does nothing! It's a dummy implementation
	}

	public boolean[] getBoolean(Attribute attrib) {
		// TODO Auto-generated method stub
		return null;
	}

	public double[] getDouble(Attribute attrib) {
		// TODO Auto-generated method stub
		return null;
	}

	public float[] getFloat(Attribute attrib) {
		// TODO Auto-generated method stub
		return null;
	}

	public int[] getInt(Attribute attrib) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getString(Attribute attrib) {
		// TODO Auto-generated method stub
		return null;
	}

	public void pointSize(float size) {
		
	}

	public void startTriangleFan()
	{
		// TODO Auto-generated method stub
		
	}

	public void callList(int list)
	{
		// TODO Auto-generated method stub
		
	}

	public void endList()
	{
		// TODO Auto-generated method stub
		
	}

	public int genLists(int range)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public void startList(int list)
	{
		// TODO Auto-generated method stub
		
	}

}
