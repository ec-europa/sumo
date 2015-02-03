/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Point;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.geoimage.java2d.util.Positioning;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.IClickable;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.IMouseMove;
import org.geoimage.viewer.core.api.ILayerManager;
import org.geoimage.viewer.widget.PositionDialog;
import org.geotools.referencing.GeodeticCalculator;

/**
 *
 * @author thoorfr
 */
public class PositionLayer extends AbstractLayer implements  IMouseMove, IClickable {

    private IImageLayer parent;
    private boolean active = true;
    private Point imagePosition;
    private PositionDialog pd;
    private Point initPosition = null;
    private Point endPosition = null;

    public PositionLayer(IImageLayer layer) {
        this.pd = new PositionDialog(Frame.getFrames()[0], false, this);
        this.pd.setVisible(true);
        super.init(parent);
    }

    public String getName() {
        return "Position";
    }

    public void setName(String name) {
    }

    public void render(GeoContext context) {
        if (initPosition == null) {
            return;
        }
        int x = context.getX(), y = context.getY();
        float zoom = context.getZoom(), width = context.getWidth() * zoom, height = context.getHeight() * zoom;
        GL2 gl = context.getGL().getGL2();
        gl.glLineWidth(1);
        float[] c = Color.GREEN.getColorComponents(null);
        gl.glColor3f(c[0], c[1], c[2]);
        gl.glBegin(GL.GL_LINE_STRIP);
        gl.glVertex2d((initPosition.x - x) / width, 1 - (initPosition.y - y) / height);
        if (endPosition == null) {
            gl.glVertex2d((imagePosition.x - x) / width, 1 - (imagePosition.y - y) / height);
        } else {
            gl.glVertex2d((endPosition.x - x) / width, 1 - (endPosition.y - y) / height);
        }
        gl.glEnd();
        gl.glFlush();
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.pd.setVisible(active);
    }

    public boolean isRadio() {
        return false;
    }

   

    public String getDescription() {
        return "Gives the positon of the mouse in the image";
    }

    public void dispose() {
        pd.setVisible(false);
        pd.dispose();
    }

    public void mouseMoved(Point imagePosition, GeoContext context) {
        this.imagePosition = imagePosition;
        if (active) {
            if (initPosition != null) {
            	GeodeticCalculator gc=Positioning.computeDistance(Platform.getCurrentImageLayer().getImageReader().getGeoTransform(),initPosition,endPosition,imagePosition);
    	        //pd.setDistance(""+Math.sqrt((init[0]-end[0])*(init[0]-end[0])+(init[1]-end[1])*(init[1]-end[1]))+" Meters");
    	        pd.setDistance("" + (float)(Math.round(gc.getOrthodromicDistance()*1000))/1000 + " Meters");

            }
            pd.setImagePosition(imagePosition);
        }
    }

    public void mouseClicked(Point imagePosition, int button, GeoContext context) {
        if (pd.getCheckDistance()) {
            if (initPosition == null) {
                initPosition = imagePosition;
            } else if (endPosition == null) {
                endPosition = imagePosition;

                GeodeticCalculator gc=Positioning.computeDistance(Platform.getCurrentImageLayer().getImageReader().getGeoTransform(),initPosition,endPosition,imagePosition);
    	        //pd.setDistance(""+Math.sqrt((init[0]-end[0])*(init[0]-end[0])+(init[1]-end[1])*(init[1]-end[1]))+" Meters");
    	        pd.setDistance("" + (float)(Math.round(gc.getOrthodromicDistance()*1000))/1000 + " Meters");

            } else {
                initPosition = null;
                endPosition = null;
                pd.setDistance("NA");
            }
        }
    }

   
}
