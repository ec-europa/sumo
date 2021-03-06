/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.geoimage.analysis.Boat;
import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.GeoTransform;
import org.geoimage.exception.GeoTransformException;
import org.geoimage.utils.PolygonOp;
import org.geoimage.viewer.core.layers.visualization.AttributesGeometry;
import org.geoimage.viewer.util.JTSUtil;
import org.geotools.data.DataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;


/**
 * This is THE class model for all Vector Data
 *
 */
public class GeometryImage implements Cloneable{


    public final static String POINT = "point";
    public final static String POLYGON = "polygon";
    public final static String LINESTRING = "linestring";
    public final static String MIXED = "mixed";

    private List<Geometry> geoms;

    private String type;
    private String name;
    private String projection;
    private FeatureCollection<?,?> featureCollection =null;

    private static Logger logger= LoggerFactory.getLogger(GeometryImage.class);


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 *
	 * @param name
	 * @param type
	 * @param geoms
	 */
	public GeometryImage(String name,String type,List<Coordinate>geoms) {
		this.type=type;
		this.name=name;

		this.geoms=new ArrayList<>();
		//attsMap=new HashMap<>();
        GeometryFactory gf = new GeometryFactory();
        for(Coordinate c:geoms){
        	AttributesGeometry att = new AttributesGeometry(new String[]{"x","y"});
        	att.set("x",c.x);
        	att.set("y",c.y);
        	Geometry gg=gf.createPoint(c);
        	put(gg, att);
        }
    }

	/**
	 *
	 * @param name
	 * @param geometries
	 */
	public GeometryImage(String name,List<Geometry>geometries) {
		this.type=geometries.get(0).getGeometryType();
		this.name=name;

		this.geoms=new ArrayList<>();

		for(Geometry g:geometries){
			AttributesGeometry att = null;

			if(g instanceof Point){
				att=new AttributesGeometry(new String[]{"x","y"});
				att.set("x",g.getCoordinate().x);
	        	att.set("y",g.getCoordinate().y);
			}else{
				att=new AttributesGeometry(new String[]{"geom"});
				att.set("geom",g.toText());
			}
			put(g, att);
		}
    }




	/**
	 *
	 * @param timeStampStart
	 * @param azimuth
	 * @param pixels
	 * @return
	 */
	public GeometryImage(String name,String type,String timeStampStart,double azimuth, Boat[] boats) {
		this.type=type;
		this.name=name;
        //GeometricLayer out = new GeometricLayer("point");
        //setName("VDS Analysis");
		geoms=new ArrayList<>();
		//attsMap=new HashMap<>();
        GeometryFactory gf = new GeometryFactory();
        long runid = System.currentTimeMillis();
        int count=0;
        for (Boat boat : boats) {
            AttributesGeometry atts = new AttributesGeometry(VDSSchema.schema);//, VDSSchema.types);
            atts.set(VDSSchema.ID, count++);
            atts.set(VDSSchema.MAXIMUM_VALUE, boat.getAllMaxValue());
            atts.set(VDSSchema.TILE_AVERAGE, boat.getAllTileAvg());
            atts.set(VDSSchema.TILE_STANDARD_DEVIATION, boat.getAllTileStd());
            atts.set(VDSSchema.THRESHOLD, boat.getAllTrhesh());
            atts.set(VDSSchema.RUN_ID, runid + "");
            atts.set(VDSSchema.NUMBER_OF_AGGREGATED_PIXELS, boat.getSize());
            atts.set(VDSSchema.ESTIMATED_LENGTH, boat.getLength());
            atts.set(VDSSchema.ESTIMATED_WIDTH, boat.getWidth());
            atts.set(VDSSchema.SIGNIFICANCE, boat.getAllSignificance());//(boat.getLength() - boat.getWidth()) / (boat.getWidth() * boat.getHeading()));
            timeStampStart=timeStampStart.replace("Z", "");
            atts.set(VDSSchema.DATE, Timestamp.valueOf(timeStampStart));
            atts.set(VDSSchema.VS, 0);
            //compute the direction of the vessel considering the azimuth of the image result is between 0 and 180 degree
            double degree = boat.getHeading() + 90 + azimuth;
            if (degree > 180) {
                degree = degree - 180;
            }
			degree = degree-90;
            atts.set(VDSSchema.ESTIMATED_HEADING, degree);
            Point p=gf.createPoint(new Coordinate(boat.getPosx(), boat.getPosy()));
            p.setUserData(atts);
            put(p);
        }
    }

    /**
     * Modify the GeometricLayer so the layer coordinate system matches the image coordinate system ("pixel" projection).
     * @param positions
     * @param geoTransform
     * @param projection if null use the original projection
     * @return
     * @throws GeoTransformException
     */
    public static GeometryImage createImageProjected(GeometryImage layer, GeoTransform geoTransform, String projection) throws GeoTransformException {
    	long startTime = System.currentTimeMillis();
    	for(Geometry geom:layer.geoms){
            geom=geoTransform.transformGeometryPixelFromGeo(geom);
        }
    	long endTime = System.currentTimeMillis();
        System.out.println("createImageProjectedLayer  " + (endTime - startTime) +  " milliseconds.");
        return layer;
    }

    /**
     * Modify the GeometricLayer so the layer coordinate system matches the image coordinate system ("pixel" projection).
     */
    public static GeometryImage createImageProjected(GeometryImage layer, AffineTransform geoTransform) {
        for(Geometry geom:layer.geoms){
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
        return layer;
    }

    /**
     * Modify the GeometricLayer so the layer coordinates system matches the world coordinate system (EPSG projection).
     * @throws GeoTransformException
     */
    public static GeometryImage createWorldProjectedLayer(GeometryImage oldPositions, GeoTransform geoTransform, String projection) throws GeoTransformException {
    	GeometryImage positions=oldPositions.clone();

        //Coordinate previous=new Coordinate(0,0);
        for(Geometry geom:positions.geoms){
        	geom=geoTransform.transformGeometryGeoFromPixel(geom);
            /*for(Coordinate pos:geom.getCoordinates()){
                double[] temp=geoTransform.getGeoFromPixel(pos.x, pos.y);
                pos.x=temp[0];
                pos.y=temp[1];
            }*/
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
    public static GeometryImage createFromSimpleGeometry(final Polygon imageP,final String geoName,
    		FeatureCollection fc, final String[] schema,
    		final String[] types, boolean applayTransformation,GeoTransform transform) throws IOException{
        GeometryImage out=null;
        GeometryFactory gf=new GeometryFactory();
        if (geoName.contains("Polygon") || geoName.contains("Line")) {
                out = new GeometryImage(GeometryImage.POLYGON);
                out.setFeatureCollection(fc);

                FeatureIterator<?> fi = fc.features();
                try{
                	ThreadPoolExecutor executor = new ThreadPoolExecutor(2,Runtime.getRuntime().availableProcessors(),2, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
                	List<Callable<Object[][]>> tasks=new ArrayList<Callable<Object[][]>>();

	                while (fi.hasNext()) {
	                		final Feature f = fi.next();
	                    	Callable<Object[][]> run=new Callable<Object[][]>() {
	                    		Geometry g=(Geometry) f.getDefaultGeometryProperty().getValue();
								@Override
								public Object[][] call() {
									List<Object[]> result=java.util.Collections.synchronizedList(new ArrayList<Object[]>());
									try {
										AttributesGeometry at = new AttributesGeometry(schema);
				                        for (int i = 0; i < f.getProperties().size(); i++) {
				                        	if(f.getProperty(schema[i]).getValue()!=null)
				                        		at.set(schema[i], f.getProperty(schema[i]).getValue());
				                        }
				                      //todo:check this part -> forse non serve
				                        g=PolygonOp.removeInteriorRing(g);
				                        Geometry gbuff=g.buffer(0);
					                        if(imageP.contains(gbuff)){
					                        	Object[]o=new Object[2];
					                        	o[0]=gbuff;
				                                o[1]=at;
				                                result.add(o);
					                        }else if(imageP.intersects(gbuff)){
					                        	Geometry p2 =EnhancedPrecisionOp.intersection(imageP.buffer(1),gbuff);
					                        	p2=p2.buffer(0);
					                        	if (!p2.isEmpty()) {
					                        		for (int ii = 0; ii < p2.getNumGeometries(); ii++) {
						                                Object[]o=new Object[2];
							                        	o[0]=p2.getGeometryN(ii);
						                                o[1]=at;

						                                result.add(o);
							                        }
						                        }
					                        }
				                    } catch (Exception ex) {
				                    	logger.error(ex.getMessage(),ex);
				                    }
									return result.toArray(new Object[0][]);
								}
							};
							tasks.add(run);
	                }

                	List<Future<Object[][]>> results=executor.invokeAll(tasks);
	                executor.shutdown();

	                for(Future<Object[][]> f:results){
	                	Object o[][]=f.get();
	                	if(o!=null){
	                		for(int i=0;i<o.length;i++){
	                			Geometry g=(Geometry)o[i][0];
	                			if(applayTransformation&&transform!=null){
	                				g=transform.transformGeometryPixelFromGeo(g);
	                			}
	                			if(!g.isValid()){
	                				g=JTSUtil.repair(g);
	                			}
	                			out.put(g,(AttributesGeometry)o[i][1]);
	                		}
	                	}
	                }
                }catch(Exception e){
                	logger.error(e.getMessage(),e);
                }finally{
                	fi.close();
                }
            } else if (geoName.contains("Point")) {
                out = new GeometryImage(GeometryImage.POINT);
                FeatureIterator<?> fi = fc.features();
                try{
	                while (fi.hasNext()) {
	                    Feature f = fi.next();
	                    AttributesGeometry at = new AttributesGeometry(schema);
	                    for (int i = 0; i < f.getProperties().size(); i++) {
	                        at.set(schema[i],f.getProperty(schema[i]).getValue());
	                    }
	                    Geometry p2 = ((Geometry) (f.getDefaultGeometryProperty().getValue())).intersection(imageP);
	                    if (!p2.isEmpty()) {
	                    	if(applayTransformation&&transform!=null)
	                    		p2=transform.transformGeometryPixelFromGeo(p2);
	                        out.put(p2, at);
	                    }

	                }
                }catch(Exception e){
                	logger.error(e.getMessage(),e);
                }finally{
                	fi.close();
                }
            }
        return out;
    }

    /**
     * 
     */
    public void splitMultiPolygons(){
		List<Geometry>clone=new ArrayList<>(this.geoms);
		for(Geometry gt:clone){
			if(gt instanceof MultiPolygon){
				int n=gt.getNumGeometries();
				for(int i=0;i<n;i++){
					Polygon pp=(Polygon)gt.getGeometryN(i);
					put(pp,(AttributesGeometry)pp.getUserData());
				}
				geoms.remove(gt);
			}
		}
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
    public static GeometryImage createLayerFromFeatures(String geoName, DataStore dataStore,
    		FeatureCollection fc, final String[] schema,
    		final String[] types,boolean applayTransformation,GeoTransform transform) throws IOException{

        GeometryImage out=null;
        if (geoName.contains("Polygon") || geoName.contains("Line"))
                out = new GeometryImage(GeometryImage.POLYGON);
        else
        	out = new GeometryImage(GeometryImage.POINT);

       	out.setName(dataStore.getTypeNames()[0]);
       	//out.setFeatureSource(dataStore.getFeatureSource(dataStore.getTypeNames()[0]));
       	out.setFeatureCollection(fc);

	        FeatureIterator<?> fi = fc.features();
	        try{
	        	ThreadPoolExecutor executor = new ThreadPoolExecutor(2,Runtime.getRuntime().availableProcessors(),2, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
	        	List<Callable<Object[][]>> tasks=new ArrayList<Callable<Object[][]>>();

	            while (fi.hasNext()) {
	            		final Feature f = fi.next();
	            		Geometry g=(Geometry) f.getDefaultGeometryProperty().getValue();

	            		Callable<Object[][]> run=() -> {
	    					List<Object[]> result=java.util.Collections.synchronizedList(new ArrayList<Object[]>());
	    					try {
	    						AttributesGeometry at = new AttributesGeometry(schema);
	    				        for (int i = 0; i < f.getProperties().size(); i++) {
	    				        	if(f.getProperty(schema[i])!=null)
	    				        		at.set(schema[i], f.getProperty(schema[i]).getValue());
	    				        }
	    						for (int ii = 0; ii < g.getNumGeometries(); ii++) {
	    				            Object[]o=new Object[2];
	    				        	o[0]=g.getGeometryN(ii);
	    				            o[1]=at;
	    				            result.add(o);
	    				        }
	    				    } catch (Exception ex) {
	    				    	logger.error(ex.getMessage(),ex);
	    				    }
	    					return result.toArray(new Object[0][]);
	    				};
						tasks.add(run);
	            }

	        	List<Future<Object[][]>> results=executor.invokeAll(tasks);
	            executor.shutdown();

	            for(Future<Object[][]> future:results){
	            	Object o[][]=future.get();
	            	if(o!=null){
	            		for(int i=0;i<o.length;i++){
	            			if(applayTransformation&&transform!=null){
	                    		Geometry gg=transform.transformGeometryPixelFromGeo((Geometry)o[i][0]);
	            				out.put(gg,(AttributesGeometry)o[i][1]);
	            			}else{
	            				out.put((Geometry)o[i][0],(AttributesGeometry)o[i][1]);
	            			}
	            		}
	            	}
	            }

	        }catch(Exception e){
	        	logger.error(e.getMessage(),e);
	        }finally{
	        	fi.close();
	        }
        return out;
    }




    /**
	 *
	 * @param geoName
	 * @param dataStore  shape file
	 * @param fc
	 * @param schema
	 * @param types
	 * @return Polygons (geometry) that are the intersection between the shape file and the sar image
	 * @throws IOException
	 */
    public static GeometryImage addGeomsToLayerFromFeatures(GeometryImage layer,String geoName,
    		DataStore dataStore, FeatureCollection addFc,
    		final String[] schema, final String[] types,
    		boolean applayTransformation,GeoTransform transform) throws IOException{
        GeometryImage out=(GeometryImage)layer.clone();

	        FeatureIterator<?> fi = addFc.features();
	        try{
	        	ThreadPoolExecutor executor = new ThreadPoolExecutor(2,Runtime.getRuntime().availableProcessors(),2, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
	        	List<Callable<Object[][]>> tasks=new ArrayList<Callable<Object[][]>>();

	            while (fi.hasNext()) {
	            		final Feature f = fi.next();
	                	Callable<Object[][]> run=new Callable<Object[][]>() {
	                		Geometry g=(Geometry) f.getDefaultGeometryProperty().getValue();
							@Override
							public Object[][] call() {
								List<Object[]> result=java.util.Collections.synchronizedList(new ArrayList<Object[]>());
								try {
									AttributesGeometry at = new AttributesGeometry(schema);
			                        for (int i = 0; i < f.getProperties().size(); i++) {
			                        	if(f.getProperty(schema[i])!=null)
			                        		at.set(schema[i], f.getProperty(schema[i]).getValue());
			                        }
	                        		for (int ii = 0; ii < g.getNumGeometries(); ii++) {
		                                Object[]o=new Object[2];
			                        	o[0]=g.getGeometryN(ii);
		                                o[1]=at;
		                                result.add(o);
			                        }
			                    } catch (Exception ex) {
			                    	logger.error(ex.getMessage(),ex);
			                    }
								return result.toArray(new Object[0][]);
							}
						};
						tasks.add(run);
	            }

	        	List<Future<Object[][]>> results=executor.invokeAll(tasks);
	            executor.shutdown();


	            for(Future<Object[][]> f:results){
	            	Object o[][]=f.get();
	            	if(o!=null){
	            		for(int i=0;i<o.length;i++){
	            			if(applayTransformation&&transform!=null){
	                    		Geometry gg=transform.transformGeometryPixelFromGeo((Geometry)o[i][0]);
	            				out.put(gg,(AttributesGeometry)o[i][1]);
	            			}else{
	            				out.put((Geometry)o[i][0],(AttributesGeometry)o[i][1]);
	            			}
	            		}
	            	}
	            }

	        }catch(Exception e){
	        	logger.error(e.getMessage(),e);
	        }finally{
	        	fi.close();
	        }
        return out;
    }


    public GeometryImage(String type) {
        geoms = new ArrayList<Geometry>();
        //attsMap = new HashMap<>();
        this.type=type;
    }

    /**
     * perform a deep copy of the Geometric Layer
     * @return
     */
    @Override
    public GeometryImage clone(){
        GeometryImage out=new GeometryImage(type);
        out.name=name;
        out.projection=projection;
      //  out.featureSource=featureSource;
        out.featureCollection=featureCollection;

        for(int i=0;i<geoms.size();i++){
        	if(geoms.get(i)!=null){
        		Geometry g=(Geometry)geoms.get(i).clone();
        		out.geoms.add(i,g);
        		//out.attsMap.put(g.getSRID(),attsMap.get(i).clone());
        	}
        }
        return out;
    }

    /**
     * Clears all the data but keep schema and geometric types
     */
    public void clear(){
    	if(geoms!=null)
    		geoms.clear();
        //if(attsMap!=null)
        //	attsMap.clear();
    }

    /**
     * retrun the atributes associated with the geometry
     * @param geom
     * @return
     */
    public AttributesGeometry getAttributes(Geometry geom) {
        if(!geoms.contains(geom))
        	return null;
        return (AttributesGeometry)geom.getUserData();
    }

    /**
     *
     * @return a SHALLOW COPY of the attributes for Thread safe use
     *
    public List<AttributesGeometry> getAttributes() {
       return new ArrayList<AttributesGeometry>(attsMap.values());
    }*/

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
     * Adds a new geometry with attributes to the layer. NOTE THAT NEITHER
     * THE SCHEMA NOR the GEOMETRY TYPE ARE CHECKED
     * so you can use it in whatever way you want, at your own risks of course
     * @param geom
     * @param att
     */
    public void put(Geometry geom, AttributesGeometry att) {
    	int id=geoms.size();
    	geom.setUserData(att);
        geoms.add(id,geom);

        //attsMap.put(geom.getSRID(),att);
    }

    /**
     * Adds a geometry, with default Attributes (ie "null" for all values)
     * NO GEOMETRY TYPE CHECK
     * @param geo
     */
    public void put(Geometry geo){
       geoms.add(geo);
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
        //attsMap.remove(geom.getSRID());
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
        geom.setUserData(att);
    }

    public String[] getSchema() {
        if (geoms.size() > 0) {
        	AttributesGeometry attr=(AttributesGeometry)geoms.get(0).getUserData();
        	if(attr!=null)
        		return attr.getSchema();
        	else
        		return new String[]{};
        } else {
            return new String[]{};
        }
    }

    public String[] getSchemaTypes() {
    	String[]types=null;
        if (geoms.size() > 0) {
        	AttributesGeometry attr=(AttributesGeometry)geoms.get(0).getUserData();

        	if(attr!=null){
        		types=new String[attr.getSchema().length];
        		String[] schema=attr.getSchema();
        		for(int ii=0;ii<schema.length;ii++){
        			Class o=attr.getType(schema[ii]);
        			if(o!=null)
        				types[ii]=o.getSimpleName();

        		}
        		return types;
        	}
        }
        return new String[]{};
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
/*
	public SimpleFeatureSource getFeatureSource() {
		return featureSource;
	}

	public void setFeatureSource(SimpleFeatureSource featureSource) {
		this.featureSource = featureSource;
	}
	*/
	public FeatureCollection getFeatureCollection() {
		return featureCollection;
	}

	public void setFeatureCollection(FeatureCollection featureCollection) {
		this.featureCollection = featureCollection;
	}
}
