/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.visualization.vectors;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.geoimage.opengl.OpenGLContext;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.ITime;
import org.geoimage.viewer.core.layers.AttributesGeometry;
import org.geoimage.viewer.core.layers.GenericLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author thoorfr
 */
public class TimeVectorLayer extends GenericLayer implements ITime {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(TimeVectorLayer.class);

    private String timeColumn;
    private Date minimumDate=new Date(Long.MAX_VALUE);
    private Date maximumDate=new Date(0);
    private Vector<Geometry> renderedLayer = new Vector<Geometry>();
    private Hashtable<Geometry,Date> datesOfgeoms=new Hashtable<Geometry, Date>();
    private boolean onWork;

    public TimeVectorLayer(ILayer parent,String layername,  String type, GeometricLayer layer, String timeColumn) {
        super(parent,layername,  type, layer);
        this.timeColumn = timeColumn;
        renderedLayer.addAll(layer.getGeometries());
        for(Geometry geom:renderedLayer){
            Date date = (Date) glayer.getAttributes(geom).get(timeColumn);
            if(minimumDate.after(date))
                minimumDate = date;
            if(maximumDate.before(date))
                maximumDate = date;
            datesOfgeoms.put(geom, date);
        }
    }

    @Override
    public void render(OpenGLContext context) {
        if (minimumDate == null || maximumDate == null) {
            super.render(context);
        } else {
            if (!context.isDirty() || onWork) {
                return;
            }
            onWork=true;
            int x = context.getX(), y = context.getY();
            float zoom = context.getZoom(), width = context.getWidth() * zoom, height = context.getHeight() * zoom;
            GL2 gl = context.getGL().getGL2();
            float[] c = color.getColorComponents(null);
            gl.glColor3f(c[0], c[1], c[2]);

            if (glayer != null) {
                if (getType().equalsIgnoreCase(GeometricLayer.POINT)) {
                    gl.glPointSize(this.renderWidth);
                    gl.glBegin(GL.GL_POINTS);
                    for (Geometry temp : renderedLayer) {
                        Coordinate point = temp.getCoordinate();
                        gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                    }
                    gl.glEnd();
                    gl.glFlush();
                    if (selectedGeometry != null) {
                        gl.glPointSize(this.renderWidth * 3);
                        gl.glBegin(GL.GL_POINTS);
                        Coordinate point = selectedGeometry.getCoordinate();
                        gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                        gl.glEnd();
                        gl.glFlush();
                    }
                } else if (getType().equalsIgnoreCase(GeometricLayer.POLYGON)) {
                    for (Geometry temp : renderedLayer) {
                        if (temp.getCoordinates().length < 1) {
                            continue;
                        }
                        gl.glLineWidth(temp == selectedGeometry ? this.renderWidth * 3 : this.renderWidth);
                        gl.glBegin(GL.GL_LINE_STRIP);
                        for (Coordinate point : temp.getCoordinates()) {
                            gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                        }
                        Coordinate point = temp.getCoordinates()[0];
                        gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                        gl.glEnd();
                        gl.glFlush();

                    }
                }
            }
            onWork=false;
        }
    }

    public String getTimeColumn() {
        return timeColumn;
    }

    public void setTimeColumn(String timeColumn) {
        this.timeColumn = timeColumn;
    }

    public Date getMinimumDate() {
        return minimumDate;
    }

    public void setMinimumDate(Date minimumDate) {
        this.minimumDate = minimumDate;
        createRenderedGeometries();
    }

    public Date getMaximumDate() {
        return maximumDate;
    }

    public void setMaximumDate(Date maximumDate) {
        this.maximumDate = maximumDate;
        createRenderedGeometries();
    }

    public Date[] getDates() {
        Date[] out = new Date[2];
        out[1] = new Date(0);
        out[0] = new Date(Long.MAX_VALUE);
        for (Geometry g : glayer.getGeometries()) {
        	AttributesGeometry at=(AttributesGeometry)g.getUserData();
            Date temp = (Date) at.get(timeColumn);
            if (out[0].after(temp)) {
                out[0] = temp;
            }
            if (out[1].before(temp)) {
                out[1] = temp;
            }
        }
        return out;
    }

    private void createRenderedGeometries() {
        Vector<Geometry> templ=new Vector<Geometry>();
        for (Geometry temp : glayer.getGeometries()) {
            Date date=datesOfgeoms.get(temp);
            if (minimumDate.before(date) && maximumDate.after(date)) {
                templ.add(temp);
            }
        }
        while(onWork){
            try {
                Thread.sleep(25);
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage(),ex);
            }
        }
        onWork=true;
        renderedLayer.clear();
        renderedLayer=templ;
        onWork=false;
    }
}
