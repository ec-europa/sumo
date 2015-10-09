/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.opengl;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.glu.GLU;

import org.fenggui.Display;

import de.lessvoid.nifty.renderer.jogl.input.JoglInputSystem;

/**
 * This class deals with the realtime status of the geolocation of the display.
 * PLEASE ONLY USE THE GETTERS AND NEVER CHANGE PARAMETERS (except setDirty()) UNLESS
 * YOU KNOW WHAT YOU DO
 * @author thoorfr
 */
public class OpenGLContext {
    private Display fDisplay;
    private int height;
    private int width;

    private boolean isDirty=false;

    private float zoom = 1;
    private int y = 0;
    private int x = 0;
    private GLContext glContext;
    private GLU glu = new GLU();

    
    
    
    
    /**
     * create an instance of GeoContext with the fenggui display
     * @param display
     */
    public OpenGLContext(Display display){
        fDisplay=display;
    }
    
    
    /**
     * gets access to the GL context to perform direct openGL operations
     * @return
     */
    public GL getGL() {
        return glContext.getGL();
    }

    /**
     * Useless, you can ignore
     * @return
     */
    public GLU getGLU() {
        return glu;
    }
    
    /**
     * return the GL context
     * @return
     */
    public GLContext getGLContext(){
        return glContext;
    }

    /**
     *
     * @return the GL Drawable
     */
    public GLDrawable getGLDrawable() {
        return glContext.getGLDrawable();

    }

    /**
     * 
     * @return the width of the GL viewport
     */
    public int getWidth() {
        return this.width;
    }

    /**
     *
     * @return the height of the GL viewport
     */
    public int getHeight() {
        return this.height;
    }

    /**
     *
     * @return the top left image x pixel in the viewport
     */
    public int getX() {
        return this.x;
    }

    /**
     * the top left image y pixel in the viewport
     * @return
     */
    public int getY() {
        return this.y;
    }

    /**
     * means [image pixel size]=zoom*[screen pixel size]
     * @return the actual zoom of the image (scale fator)
     * = 1.0 real size of the image (ie: 1 screen pixel=1 image pixel)
     * < 1.0 means image zoomed in
     * > 1.0 means zoomed out
     */
    public float getZoom() {
        return this.zoom;
    }

    /**
     * says if the context is dirty (ie has changed from last frame)
     * this is useful for performance, not to use CPU when "nothing happens"
     * @return true if the data has changed from last frame
     */
    public boolean isDirty(){
        return isDirty;
    }

    /**
     * set it to true if something has changed in the context (changing position, focus, new data etc...)
     * @param dirty
     */
    public void setDirty(boolean dirty){
        this.isDirty=dirty;
    }

    /**
     * Initializes this <code>DrawContext</code>. This method should be called at the beginning of each frame to prepare
     * the <code>DrawContext</code> for the coming render pass.
     *
     * @param glContext the <code>javax.media.opengl.GLContext</code> to use for this render pass
     */
    public void initialize(GLContext glContext) {
        this.glContext = glContext;
        
    }

    public void setX(int i) {
        x=i;
    }
    
    public void setY(int i) {
        y=i;
    }

    public void setZoom(float zoom) {
        this.zoom=zoom;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }
    
    public Display getFenguiDisplay(){
        return fDisplay;
    }

   /* public void setGeoTransform(GeoTransform geoTransform) {
        this.geoTransform = geoTransform;
    }*/
}
