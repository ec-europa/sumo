/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.geoimage.def.GeoTransform;
import org.geoimage.exception.GeoTransformException;
import org.geoimage.viewer.core.api.Attributes;
import org.geotools.data.DataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * This is THE class model for all Vector Data
 * @author thoorfr
 */
public class GeometricLayer implements Cloneable{
    
    
    public final static String POINT = "point";
    public final static String POLYGON = "polygon";
    public final static String LINESTRING = "linestring";
    
    private List<Geometry> geoms;
    private List<Attributes> atts;
    private String type;
    private String name;
    private String projection;
    
    /**
     * 
     */
    public final static String MIXED = "mixed";
    private static Logger logger= LoggerFactory.getLogger(GeometricLayer.class);
    
    /**
     * Modify the GeometricLayer so the layer coordinate system matches the image coordinate system ("pixel" projection).
     * @param positions
     * @param geoTransform
     * @param projection if null use the original projection
     * @return
     * @throws GeoTransformException 
     */
    public static GeometricLayer createImageProjectedLayer(GeometricLayer positions, GeoTransform geoTransform, String projection) throws GeoTransformException {
    	long startTime = System.currentTimeMillis();
    	for(Geometry geom:positions.geoms){
            geom=geoTransform.transformGeometryPixelFromGeo(geom);
        }
    	long endTime = System.currentTimeMillis();
        System.out.println("createImageProjectedLayer  " + (endTime - startTime) +  " milliseconds.");
        return positions;
    }
    
    /**
     * Modify the GeometricLayer so the layer coordinate system matches the image coordinate system ("pixel" projection).
     */
    public static GeometricLayer createImageProjectedLayer(GeometricLayer positions, AffineTransform geoTransform) {
        //positions=positions.clone();
        for(Geometry geom:positions.geoms){
            for(Coordinate pos:geom.getCoordinates()){
                Point2D.Double temp=new Point2D.Double();
                try {
					geoTransform.inverseTransform(new Point2D.Double(pos.x, pos.y),temp);
				} catch (NoninvertibleTransformException e) {
					e.printStackTrace();
				}
                pos.x=temp.x;
                pos.y=temp.y;
            }
        }
        return positions;
    }
    
    /**
     * Modify the GeometricLayer so the layer coordinates system matches the world coordinate system (EPSG projection).
     * @throws GeoTransformException 
     */
    public static GeometricLayer createWorldProjectedLayer(GeometricLayer positions, GeoTransform geoTransform, String projection) throws GeoTransformException {
        //positions=positions.clone();
        positions.projection=projection;
        for(Geometry geom:positions.geoms){
            for(Coordinate pos:geom.getCoordinates()){
                double[] temp=geoTransform.getGeoFromPixel(pos.x, pos.y);
                pos.x=temp[0];
                pos.y=temp[1];
            }
        }
        return positions;
    }
    
    /**
	 * 
	 * @param imageP poligono creato con i punti di riferimento dell'immagine
	 * @param geoName
	 * @param dataStore  shape file
	 * @param fc
	 * @param schema
	 * @param types
	 * @return Polygons (geometry) that are the intersection between the shape file and the sar image
	 * @throws IOException
	 */
    public static GeometricLayer createFromSimpleGeometry(final Polygon imageP, String geoName, DataStore dataStore, FeatureCollection fc, final String[] schema, final String[] types) throws IOException{
        GeometricLayer out=null;
        if (geoName.contains("Polygon") || geoName.contains("Line")) {
                out = new GeometricLayer(GeometricLayer.POLYGON);
                out.setName(dataStore.getTypeNames()[0]);
                FeatureIterator<?> fi = fc.features();
                try{
                	ThreadPoolExecutor executor = new ThreadPoolExecutor(2,Runtime.getRuntime().availableProcessors(),2, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
                	List<Callable<Object[]>> tasks=new ArrayList<Callable<Object[]>>();
                	
	                while (fi.hasNext()) {
	                		final Feature f = fi.next();//(Feature)ff[idx];
	                    	Callable<Object[]> run=new Callable<Object[]>() {
	                    		Geometry g=(Geometry) f.getDefaultGeometryProperty().getValue();
								@Override
								public Object[] call() {
									Object[] result=new Object[2];
									try {
										Attributes at = Attributes.createAttributes(schema, types);
				                        for (int i = 0; i < f.getProperties().size(); i++) {
				                            at.set(schema[i], f.getProperty(schema[i]).getValue());
				                        }
				                       // g=TopologyPreservingSimplifier.simplify(g,0.0005);
				                        
				                        //buffer(0) is used to avoid intersection errors 
				                        Geometry p2 =EnhancedPrecisionOp.intersection(imageP.buffer(0),g);
				                        
				                        if(!p2.isEmpty()){
			                                if(p2.getGeometryType().equalsIgnoreCase("MULTIPOLYGON")){
						                    	for (int i = 0; i < p2.getNumGeometries(); i++) {
						                            if (!p2.getGeometryN(i).isEmpty()) {
						                                result[0]=p2.getGeometryN(i);
						                                result[1]=at;
						                            }
						                        }
			                                }else{
					                        	result[0]=p2;
				                                result[1]=at;
			                                }	
				                        }	
				                    } catch (Exception ex) {
				                    	logger.error(ex.getMessage(),ex);
				                    }
									return result;
								}
							};
							tasks.add(run);
	                }
                	
                	List<Future<Object[]>> results=executor.invokeAll(tasks);
	                executor.shutdown();
	                
	                
	                for(Future<Object[]> f:results){
	                	Object o[]=f.get();
	                	if(o[0]!=null){
	                		out.put((Geometry)o[0],(Attributes)o[1]);
	                	}	
	                }
	                
                }catch(Exception e){
                	logger.error(e.getMessage(),e);
                }finally{
                	fi.close();
                }   
                //out.put(imageP, Attributes.createAttributes(schema, types));
            } else if (geoName.contains("Point")) {
                out = new GeometricLayer(GeometricLayer.POINT);
                FeatureIterator<?> fi = fc.features();
                try{
	                out.setName(dataStore.getTypeNames()[0]);
	                while (fi.hasNext()) {
	                    Feature f = fi.next();
	                    Attributes at = Attributes.createAttributes(schema, types);
	                    for (int i = 0; i < f.getProperties().size(); i++) {
	                        at.set(schema[i],f.getProperty(schema[i]).getValue());
	                    }
	                    Geometry p2 = ((Geometry) (f.getDefaultGeometryProperty().getValue())).intersection(imageP);
	                    if (!p2.isEmpty()) {
	                        out.put(p2, at);
	                    }
	
	                }
	            }finally{
	            	fi.close();
	            }  
            }
        return out;
    }
    

    public GeometricLayer(String type) {
        geoms = new ArrayList<Geometry>();
        atts = new ArrayList<Attributes>();
        this.type=type;
    }
    
    /**
     * perform a deep copy of the Geometric Layer
     * @return
     */
    @Override
    public GeometricLayer clone(){
        GeometricLayer out=new GeometricLayer(type);
        out.name=name;
        for(int i=0;i<geoms.size();i++){
            out.geoms.add(i,(Geometry)geoms.get(i).clone());
            out.atts.add(i,atts.get(i).clone());
        }
        return out;
    }

    /**
     * Clears all the data but keep schema and geometric types
     */
    public void clear(){
        geoms.clear();
        atts.clear();
    }
    
    /**
     * retrun the atributes associated with the geometry
     * @param geom
     * @return
     */
    public Attributes getAttributes(Geometry geom) {
        int i = geoms.indexOf(geom);
        if(i<0) return null;
        return atts.get(i);
    }

    /**
     *
     * @return a SHALLOW COPY of the attributes for Thread safe use
     */
    public List<Attributes> getAttributes() {
       return new ArrayList<Attributes>(atts);
    }

    /**
     * 
     * @return the list of geometries (NOT COPIED so NOT THREAD-SAFE)
     */
    public List<Geometry> getGeometries() {
        return geoms;
    }
    
    /**
     * 
     * @return the type of the geometry, one of the static field of the class
     */
    public String getGeometryType(){
        return this.type;
    }

    /**
     * return the schema example: getSchema(':')="name:age:position"
     * @param separator
     * @return
     */
    public String getSchema(char separator) {
        StringBuilder out = new StringBuilder();
        for (String att : getSchema()) {
            out.append(att).append(separator);
        }
        if (out.toString().equals("")) {
            return out.toString();
        } else {
            return out.substring(0, out.length() - 1);
        }
    }

    /**
     * PLEASE DO NOT USE, AT YOUR OWN RISKS
     * @param name
     * @param type
     * @return
     */
    public boolean addColumn(String name, String type){
        for(Attributes att:atts){
            att.addColumn(name, type);
        }
        return true;
    }
    
    /**
     * 
     * @return the types of the schema. @see Attributes
     */
    public String[] getSchemaTypes(){
          if (atts.size() > 0) {
            return atts.get(0).getTypes();
        } else {
            return new String[]{};
        }
    }

    /**
     * The types of the schema. @see Attributes
     * @param separator
     * @return
     */
    public String getSchemaTypes(char separator) {
        String out = "";
        for (String att : getSchemaTypes()) {
            out += att + separator;
        }
        if (out.equals("")) {
            return out;
        } else {
            return out.substring(0, out.length() - 1);
        }
    }

    /**
     * Adds a new geometry with attributes to the layer. NOTE THAT NEITHER 
     * THE SCHEMA NOR the GEOMETRY TYPE ARE CHECKED
     * so you can use it in whatever way you want, at your own risks of course
     * @param geom
     * @param att
     */
    public void put(Geometry geom, Attributes att) {
        geoms.add(geom);
        atts.add(att);
    }

    /**
     * Adds a geometry, with default Attributes (ie "null" for all values)
     * NO GEOMETRY TYPE CHECK
     * @param geo
     */
    public void put(Geometry geo){
        this.put(geo, Attributes.createAttributes(getSchema(), getSchemaTypes()));
    }

    /**
     * Removes one geometry. The Attributes will be removed accordingly
     * @param geom
     */
    public void remove(Geometry geom) {
        if (!geoms.contains(geom)) {
            return;
        }
        int i = geoms.indexOf(geom);
        geoms.remove(i);
        atts.remove(i);
    }

    /**
     * replace the geometry "oldGeometry" with "newGeometry"
     * @param oldGeometry
     * @param newGeometry
     */
    public void replace(Geometry oldGeometry, Geometry newGeometry) {
        geoms.set(geoms.indexOf(oldGeometry), newGeometry);
    }

    public void setAttribute(Geometry geom, String att, Object value) {
        if (!geoms.contains(geom)) {
            return;
        }
        int i = geoms.indexOf(geom);
        atts.get(i).set(att, value);
    }

    public String[] getSchema() {
        if (atts.size() > 0) {
            return atts.get(0).getSchema();
        } else {
            return new String[]{};
        }
    }

    public String getName() {
        return name;
    }

    public void setGeometryType(String type) {
        this.type=type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }
}
