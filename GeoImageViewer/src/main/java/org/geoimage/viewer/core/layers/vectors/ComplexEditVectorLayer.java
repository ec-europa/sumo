/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.layers.vectors;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.GeometricLayer;
import org.geoimage.viewer.core.api.ILayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author leforth
 */
public class ComplexEditVectorLayer extends SimpleEditVectorLayer {
	
	
	
    // Acts as a simple edit vector but allows additional display features
    // additional display features are stored in separate GeometricLayers
    // the class also allows more interactions such as multiple selection
    class additionalgeometries {
        private String tag;
        private Color color;
        private int lineWidth = 1;
        private String type = POINT;
        private List<Geometry> geometries;
        private boolean status;
       
        
        public additionalgeometries(String tag, Color color, int lineWidth, String type, List<Geometry> geometries, boolean status)
        {
            this.color = color;
            this.lineWidth = lineWidth;
            this.type = type;
            this.geometries = geometries;
            this.tag = tag;
            this.status = status;
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public String getTag()
        {
            return this.tag;
        }

        public Color getColor()
        {
            return this.color;
        }

        public int getLinewidth()
        {
            return this.lineWidth;
        }

        public List<Geometry> getGeometries()
        {
            return this.geometries;
        }

        public String getType()
        {
            return this.type;
        }

    };

    private List<additionalgeometries> additionalGeometries = new ArrayList<additionalgeometries>();

    public ComplexEditVectorLayer(ILayer parent,String layername, String type, GeometricLayer layer) {
        super(parent,layername, type, layer);
    }

    @Override
    public void render(GeoContext context) {
        super.render(context);
        if (!context.isDirty() || glayer == null) {
            return;
        }

        int x = context.getX(), y = context.getY();
        float zoom = context.getZoom(), width = context.getWidth() * zoom, height = context.getHeight() * zoom;
        GL2 gl = context.getGL().getGL2();
        for(additionalgeometries geometry : additionalGeometries)
        {
            // check geometries need to be displayed
            if(geometry.isStatus())
            {
                float[] c = geometry.getColor().getColorComponents(null);
                gl.glColor3f(c[0], c[1], c[2]);
                if (geometry.getType().equalsIgnoreCase(POINT)) {
                    gl.glPointSize(geometry.getLinewidth());
                    gl.glBegin(GL.GL_POINTS);
                    for (Geometry temp : geometry.getGeometries()) {
                        Coordinate point = temp.getCoordinate();
                        gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);

                    }
                    gl.glEnd();
                    gl.glFlush();
                } else if (geometry.getType().equalsIgnoreCase(POLYGON)) {
                    for (Geometry tmp : geometry.getGeometries()) {
	                    	Polygon polygon=(Polygon)tmp;
	                        if (polygon.getCoordinates().length < 1) {
	                            continue;
	                        }
	                        int interior=polygon.getNumInteriorRing();

	                        if(interior>0){
	                        	//draw external polygon
	                        	LineString line=polygon.getExteriorRing();
	                        	drawPoly(gl,line.getCoordinates(),width,height,x,y,geometry.getLinewidth());
	                        	//draw holes
	                        	for(int i=0;i<interior;i++){
	                        		LineString line2=polygon.getInteriorRingN(i);
	                        		drawPoly(gl,line2.getCoordinates(),width,height,x,y,geometry.getLinewidth());
	                        	}
	                        }else{
	                        	drawPoly(gl,polygon.getCoordinates(),width,height,x,y,geometry.getLinewidth());
	                        }
                    }
                    
                } else if (geometry.getType().equalsIgnoreCase(LINESTRING)) {
                    for (Geometry temp : geometry.getGeometries()) {
                        if (temp.getCoordinates().length < 1) {
                            continue;
                        }
                        gl.glLineWidth(geometry.getLinewidth());
                        gl.glBegin(GL.GL_LINE_STRIP);
                        for (Coordinate point : temp.getCoordinates()) {
                            gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                        }
                        gl.glEnd();
                        gl.glFlush();
                    }
                }
            }
        }
    
    }

    public void addGeometries(String geometrytag, Color color, int lineWidth, String type, List<Geometry> geometries, boolean status)
    {
        additionalGeometries.add(new additionalgeometries(geometrytag, color, lineWidth, type, geometries, status));
    }

    private int getGeometriesByTag(String geometrytag)
    {
        for(additionalgeometries geometries : additionalGeometries)
        {
            if(geometries.getTag().equals(geometrytag))
                return additionalGeometries.indexOf(geometries);
        }

        return -1;
    }

    public boolean removeGeometriesByTag(String geometrytag)
    {
        int index = getGeometriesByTag(geometrytag);
        if(index != -1)
        {
            this.additionalGeometries.remove(index);
            return true;
        }

        return false;
    }

    public boolean tagExists(String tag)
    {
        return getGeometriesByTag(tag) == -1 ? false : true;
    }

    public List<String> getGeometriestagList()
    {
        ArrayList<String> geometriestaglist = new ArrayList<String>();

        for(additionalgeometries geometries : additionalGeometries)
        {
            geometriestaglist.add(geometries.getTag());
        }

        return geometriestaglist;
    }

    public boolean getGeometriesDisplay(String geometrytag) {
        if(tagExists(geometrytag))
            return additionalGeometries.get(getGeometriesByTag(geometrytag)).isStatus();

        return false;
    }

    public void toggleGeometriesByTag(String geometrytag, boolean status) {
        if(tagExists(geometrytag))
            additionalGeometries.get(getGeometriesByTag(geometrytag)).setStatus(status);
        Platform.getGeoContext().setDirty(true);
    }

};
