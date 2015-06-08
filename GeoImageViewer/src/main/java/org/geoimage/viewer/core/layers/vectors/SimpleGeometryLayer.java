/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.vectors;

import java.awt.Color;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.geoimage.def.GeoTransform;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.IVectorLayer;
import org.geoimage.viewer.core.layers.AbstractLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author thoorfr
 */
public class SimpleGeometryLayer extends AbstractLayer implements IVectorLayer{

    public final static String POINT = GeometricLayer.POINT;
    public final static String POLYGON = GeometricLayer.POLYGON;
    public final static String LINESTRING = GeometricLayer.LINESTRING;
    public final static String MIXED = GeometricLayer.MIXED;
    protected boolean active = true;
    
   	
	protected String type;
    protected String name;
    protected float renderWidth = 1;
    protected Color color = new Color(1f, 1f, 1f);
    protected List<Geometry> geometries;
    private symbol displaysymbol = symbol.point;
    protected double currentThresh = 0;
    
    private static org.slf4j.Logger logger=LoggerFactory.getLogger(SimpleGeometryLayer.class);
    
    
    
    public SimpleGeometryLayer(ILayer parent,String layername, List<Geometry> geometries,String type) {
    	super.parent=parent;
        this.name = layername;
        this.geometries=geometries;
        this.type=type;
    }


    public static enum symbol {point, circle, square, triangle, cross};

 

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 
     * @param gl
     * @param cs
     * @param width
     * @param height
     * @param x
     * @param y
     */
    protected void drawPoly(GL2 gl,Coordinate[] cs,float width,float height,int x,int y,float rwidth){
    	gl.glLineWidth(rwidth);
        gl.glBegin(GL.GL_LINE_STRIP);
        for (int p = 0; p < cs.length; p++) {
        	double vx=(cs[p].x - x) / width;
        	double vy=1 - (cs[p].y - y) / height;
            gl.glVertex2d(vx,vy);
        }
       
        //close polygon
        Coordinate point = cs[0];
        gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
        gl.glEnd();
        gl.glFlush();
    }

    public void render(GeoContext context) {
        if (!context.isDirty()) {
            return;
        }
        int x = context.getX();
        int y = context.getY();
        float zoom = context.getZoom();
        float width = context.getWidth() * zoom;
        float height = context.getHeight() * zoom;
        
        GL2 gl = context.getGL().getGL2();
        float[] c = color.getColorComponents(null);
        gl.glColor3f(c[0], c[1], c[2]);

        if (geometries != null) {
                if (getType().equalsIgnoreCase(POINT)) {
                    switch (this.displaysymbol) {
                        case point: {
                            gl.glPointSize(this.renderWidth);
                            gl.glBegin(GL.GL_POINTS);
                            for (Geometry temp : geometries) {
                                Coordinate point = temp.getCoordinate();
                                gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                            }
                            gl.glEnd();
                            gl.glFlush();
                        }
                        break;
                        case circle: {
                        }
                        break;
                        case square: {
                        	//usato anche per disegnare i contorni delle detection
                            for (Geometry temp : geometries) {
                            	gl.glLineWidth(this.renderWidth);
                                Coordinate point = new Coordinate(temp.getCoordinate());
                                point.x = (point.x - x) / width;
                                point.y = 1 - (point.y - y) / height;
                                double rectwidth = 0.01;
                                gl.glBegin(GL.GL_LINE_STRIP);
                                gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
                                gl.glVertex2d(point.x - rectwidth, point.y + rectwidth);
                                gl.glVertex2d(point.x + rectwidth, point.y + rectwidth);
                                gl.glVertex2d(point.x + rectwidth, point.y - rectwidth);
                                gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
                                gl.glEnd();
                                gl.glFlush();
                            }
                        }
                        break;
                        case cross: {
                            for (Geometry temp : geometries) {
                            	gl.glLineWidth(this.renderWidth);
                                Coordinate point = new Coordinate(temp.getCoordinate());
                                point.x = (point.x - x) / width;
                                point.y = 1 - (point.y - y) / height;
                                double rectwidth = 0.01;
                                gl.glBegin(GL.GL_LINE_STRIP);
                                gl.glVertex2d(point.x - rectwidth, point.y);
                                gl.glVertex2d(point.x + rectwidth, point.y);
                                gl.glEnd();
                                gl.glBegin(GL.GL_LINE_STRIP);
                                gl.glVertex2d(point.x, point.y - rectwidth);
                                gl.glVertex2d(point.x, point.y + rectwidth);
                                gl.glEnd();
                                gl.glFlush();
                            }
                        }
                        break;
                        case triangle: {
                            for (Geometry temp : geometries) {
                            	gl.glLineWidth(this.renderWidth);
                                Coordinate point = new Coordinate(temp.getCoordinate());
                                point.x = (point.x - x) / width;
                                point.y = 1 - (point.y - y) / height;
                                double rectwidth = 0.01;
                                gl.glBegin(GL.GL_LINE_STRIP);
                                gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
                                gl.glVertex2d(point.x, point.y + rectwidth);
                                gl.glVertex2d(point.x + rectwidth, point.y - rectwidth);
                                gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
                                gl.glEnd();
                                gl.glFlush();
                            }

                        }
                        break;
                        default: {
                        }
                    }
                } else if (getType().equalsIgnoreCase(POLYGON)) {
                    for (Geometry tmp : geometries) {
                    	Polygon polygon=(Polygon)tmp;
                        if (polygon.getCoordinates().length < 1) {
                            continue;
                        }
                        float rWidth=this.renderWidth ;
                        
                        int interior=polygon.getNumInteriorRing();

                        if(interior>0){
                        	//draw external polygon
                        	LineString line=polygon.getExteriorRing();
                        	drawPoly(gl,line.getCoordinates(),width,height,x,y,rWidth);
                        	//draw holes
                        	for(int i=0;i<interior;i++){
                        		LineString line2=polygon.getInteriorRingN(i);
                        		drawPoly(gl,line2.getCoordinates(),width,height,x,y,rWidth);
                        	}
                        }else{
                        	drawPoly(gl,polygon.getCoordinates(),width,height,x,y,rWidth);
                        }
                        
                    }
                } else if (getType().equalsIgnoreCase(LINESTRING)) {
                    for (Geometry temp : geometries) {
                        if (temp.getCoordinates().length < 1) {
                            continue;
                        }
                        
                        gl.glLineWidth(this.renderWidth);
                        gl.glBegin(GL.GL_LINE_STRIP);
                        Coordinate[] cs = temp.getCoordinates();
                        for (int p = 0; p < cs.length; p++) {
                            gl.glVertex2d((cs[p].x - x) / width, 1 - (cs[p].y - y) / height);
                        }
                        gl.glEnd();
                        gl.glFlush();
                    }
                } 
            } 
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
        return getName();
    }

    public void dispose() {
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float getWidth() {
        return this.renderWidth;
    }

    public void setWidth(float width) {
        this.renderWidth = width;
    }

  

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

   
  
  


}
