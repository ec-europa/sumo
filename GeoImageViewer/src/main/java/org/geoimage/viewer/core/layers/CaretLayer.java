/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.api.ILayer;

/**
 *
 * @author thoorfr
 *
 * not used anymore
 *
 */
public class CaretLayer extends AbstractLayer {

    private boolean active=true;
    private int width;
    private int height;

    public CaretLayer(IImageLayer parent) {
        this.width = parent.getImageReader().getWidth();
        this.height = parent.getImageReader().getHeight();
        super.setParent((ILayer) parent);
    }

    public String getName() {
        return "Caret";
    }

    public void setName(String name) {

    }

    public void render(GeoContext context) {
        GL2 gl = context.getGL().getGL2();
        gl.glColor3f(1, 1, 1);
        gl.glLineWidth(1.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        float zoom=context.getZoom();
        gl.glVertex2f(context.getX() / (1f*width),1-context.getY() / (1f*height));
        gl.glVertex2f((context.getX() + zoom*context.getWidth()) / width,1- context.getY() / (1f*height));
        gl.glVertex2f((context.getX() + zoom*context.getWidth()) / width,1- (context.getY() + zoom*context.getHeight()) / height);
        gl.glVertex2f(context.getX() / (1f*width),1-(context.getY() + zoom*context.getHeight()) / height);
        gl.glEnd();
        gl.glFlush();


    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRadio() {
        return false;
    }

    public String getDescription() {
        return "Position of the view in the Image";
    }

    public void dispose() {
        //nothing to do
        return;
    }
}
