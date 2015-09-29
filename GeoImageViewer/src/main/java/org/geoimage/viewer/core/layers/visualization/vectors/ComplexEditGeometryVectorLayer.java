/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.layers.visualization.vectors;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geoimage.opengl.GL2ShapesRender;
import org.geoimage.opengl.OpenGLContext;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.api.IComplexVectorLayer;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.layers.GeometricLayer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author Pietro Argentieri
 * this class extends the EditGeometryLayer to manage "additional geometries"
 *   
 * 
 */
public class ComplexEditGeometryVectorLayer extends EditGeometryVectorLayer implements IComplexVectorLayer {
public static final String ARTEFACTS_AMBIGUITY_TAG="artefactsambiguities";
public static final String AZIMUTH_AMBIGUITY_TAG="azimuthambiguities";
public static final String AMBIGUITY_TAG="All ambiguities";
public static final String DETECTED_PIXELS_TAG="detectedpixels";
public static final String TRESHOLD_PIXELS_AGG_TAG="thresholdaggregatepixels";
public static final String TRESHOLD_PIXELS_TAG="thresholdclippixels";
	
	
    // Acts as a simple edit vector but allows additional display features
    // additional display features are stored in separate GeometricLayers
    // the class also allows more interactions such as multiple selection
    public class Additionalgeometries {
        private String tag;
        private Color color;
        private int lineWidth = 1;
        private String type = GeometricLayer.POINT;
        private List<Geometry> geometries;
        private boolean status;
       
        
        public Additionalgeometries(String tag, Color color, int lineWidth, String type, List<Geometry> geometries, boolean status)
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

    private Map<String,Additionalgeometries> additionalGeometriesMap = new HashMap<String,Additionalgeometries>();

    public ComplexEditGeometryVectorLayer(ILayer parent,String layername, String type, GeometricLayer layer) {
        super(parent,layername, type, layer);
    }

    @Override
    public void render(OpenGLContext context) {
        super.render(context);
        
        
        if (!context.isDirty() || glayer == null||SumoPlatform.isBatchMode()) {
            return;
        }

        int x = context.getX(), y = context.getY();
        float zoom = context.getZoom(), width = context.getWidth() * zoom, height = context.getHeight() * zoom;

        for(Additionalgeometries geometry : additionalGeometriesMap.values()){
            // check geometries need to be displayed
            if(geometry.isStatus()){
                if (geometry.getType().equalsIgnoreCase(GeometricLayer.POINT)) {
                	GL2ShapesRender.renderPolygons(context,width,height,geometry.getGeometries(),geometry.getLinewidth(),geometry.getColor());

                } else if (geometry.getType().equalsIgnoreCase(GeometricLayer.POLYGON)) {
                    for (Geometry tmp : geometry.getGeometries()) {
                    	List<Geometry>gs=new ArrayList<>();
	                    if(tmp instanceof MultiPolygon){
	                    	MultiPolygon mp=(MultiPolygon)tmp;
	                    	for (int ig=0;ig<mp.getNumGeometries();ig++) {
	                    		gs.add(mp.getGeometryN(ig));
	                    	}
	                    }else{
	                    	gs.add(tmp);
	                    }
	                    for (Geometry g : gs) {
	                    	Polygon polygon=(Polygon)g;
		                        if (polygon.getCoordinates().length < 1) {
		                            continue;
		                        }
		                        GL2ShapesRender.drawPoly(context,polygon.getCoordinates(),width,height,x,y,geometry.getLinewidth(),color);
		                        /*int interior=polygon.getNumInteriorRing();
	
		                        if(interior>0){
		                        	//draw external polygon
		                        	LineString line=polygon.getExteriorRing();
		                        	GL2ShapesRender.drawPoly(context,line.getCoordinates(),width,height,x,y,geometry.getLinewidth(),color);
		                        	//draw holes
		                        	for(int i=0;i<interior;i++){
		                        		LineString line2=polygon.getInteriorRingN(i);
		                        		GL2ShapesRender.drawPoly(context,line2.getCoordinates(),width,height,x,y,geometry.getLinewidth(),color);
		                        	}
		                        }else{
		                        	GL2ShapesRender.drawPoly(context,polygon.getCoordinates(),width,height,x,y,geometry.getLinewidth(),color);
		                        }*/
	                    }
                    }
                    
                } else if (geometry.getType().equalsIgnoreCase(GeometricLayer.LINESTRING)) {
                    for (Geometry temp : geometry.getGeometries()) {
                        if (temp.getCoordinates().length < 1) {
                            continue;
                        }
                    	GL2ShapesRender.renderPolygon(context,width,height,temp.getCoordinates(),geometry.getLinewidth(),color);
                    }
                }
            }
        }
    }

    public void addGeometries(String geometrytag, Color color, int lineWidth, String type, List<Geometry> geometries, boolean status)
    {
    	additionalGeometriesMap.put(geometrytag,new Additionalgeometries(geometrytag, color, lineWidth, type, geometries, status));
    }

    public Additionalgeometries getGeometriesByTag(String geometrytag)
    {
        return additionalGeometriesMap.get(geometrytag);
    }

    public boolean removeGeometriesByTag(String geometrytag)
    {
    	try{
    		additionalGeometriesMap.remove(geometrytag);
    		return true;
    	}catch(Exception e){
    		return false;
    	}	
    }

    public boolean tagExists(String tag)
    {
        return getGeometriesByTag(tag) == null ? false : true;
    }

    public List<String> getGeometriestagList()
    {
    	List<String> keys=new ArrayList<>();
    	keys.addAll(additionalGeometriesMap.keySet());
        return keys;
    }

    public boolean getGeometriesDisplay(String geometrytag) {
        if(tagExists(geometrytag)){
        	Additionalgeometries add=getGeometriesByTag(geometrytag);
            return add.isStatus();
        }    
        return false;
    }

    public void toggleGeometriesByTag(String geometrytag, boolean status) {
        if(tagExists(geometrytag))
            additionalGeometriesMap.get(geometrytag).setStatus(status);
        SumoPlatform.getApplication().getGeoContext().setDirty(true);
    }

};
