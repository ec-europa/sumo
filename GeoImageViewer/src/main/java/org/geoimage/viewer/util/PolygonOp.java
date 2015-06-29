package org.geoimage.viewer.util;

import java.util.ArrayList;
import java.util.List;

import org.geoimage.def.GeoTransform;
import org.geoimage.exception.GeoTransformException;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;

public class PolygonOp {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(PolygonOp.class);
	
	
	/**
	 * 
	 * @param geom
	 * @return
	 */
	public static Geometry removeInteriorRing(Geometry geom){
		PrecisionModel pm=new PrecisionModel(1);
	    GeometryFactory gf = new GeometryFactory(pm);
		Geometry buff=geom;
        if (buff instanceof Polygon && ((Polygon) buff).getNumInteriorRing() > 0) {
        	LineString p=((Polygon) buff).getExteriorRing();
        	buff = gf.createPolygon(p.getCoordinates());
        }
        return buff;
	}
	
	/**
	 * 
	 * @param xs
	 * @return
	 * @throws ParseException 
	 */
	public static Polygon createPolygon(double[]...xs) throws ParseException{
		StringBuilder builder =new StringBuilder("POLYGON((");
		
		for(int i=0;i<xs.length;i++){
			double x[]=xs[i];
			if(i<(xs.length-1))
				builder=builder.append(x[0]).append(" ").append(x[1]).append(",");
			else
				builder=builder.append(x[0]).append(" ").append(x[1]).append("))");
		}
		
		Polygon imageP = (Polygon) new WKTReader().read(builder.toString());
		return imageP;
	}
	
	/**
	 * 
	 * @param xs
	 * @return
	 * @throws ParseException 
	 */
	public static Polygon createPolygon(int[]...xs) throws ParseException{
		StringBuilder builder =new StringBuilder("POLYGON((");
		
		for(int i=0;i<xs.length;i++){
			int x[]=xs[i];
			if(i<(xs.length-1))
				builder=builder.append(x[0]).append(" ").append(x[1]).append(",");
			else
				builder=builder.append(x[0]).append(" ").append(x[1]).append("))");
		}
		
		Polygon imageP = (Polygon) new WKTReader().read(builder.toString());
		return imageP;
	}
	
	
	
	  /**
     * 
     * @param polygons
     * @return
     */
	public static  List<Geometry> mergeCascadePolygons(List<Geometry> polygons,double buffer) {
    	List <List<Geometry>>intersectedGeom =new ArrayList<List<Geometry>>();
    	List <Geometry>alreadySelected =new ArrayList<Geometry>();
    	for (int i = 0; i < polygons.size(); i++) {
    		
    		Geometry a = polygons.get(i);
    		List <Geometry> l=new ArrayList<Geometry>();
    		l.add(a);
    		
    		alreadySelected.add(a);
    		
    		for (int j = i + 1; j < polygons.size();j++) {
    			final Geometry b = polygons.get(j);
    	        try{
			        if (a.intersects(b)) {
			        	l.add(b);
			        	alreadySelected.add(b);
			        }
    			}catch(Exception e){
    				logger.warn(e.getMessage());
    			}
    	    }
    		if(l.size()>1||!alreadySelected.contains(l.get(0)))
    			intersectedGeom.add(l);
    	}
    	GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();
    	for(List<Geometry> ll:intersectedGeom){
    		if(ll.size()>1){
    			try{
    				
    				//10 e' un valore determinato solo da diversi test ...
    				if(buffer>10){
						for(int i=0;i<ll.size();i++){
							Geometry g=ll.get(i);
							ll.set(i,g.buffer(-buffer/2));
						}
    				}
    				CascadedPolygonUnion cascadeU=new CascadedPolygonUnion(ll);
    				Geometry union=cascadeU.union();
    				
    	            if (union instanceof Polygon && ((Polygon) union).getNumInteriorRing() > 0) {
    	            	LineString p=((Polygon) union).getExteriorRing();
    	            	union = factory.createPolygon(p.getCoordinates());
    	            }
    				polygons.add(union);
    				
	    		}catch(Exception e){
	    			logger.warn(e.getMessage());
	    			
	    			for(Geometry g:ll){
	    				polygons.add(g);
	    			}
				}
    		}else{
   				polygons.add(ll.get(0));
    		}
    	}
    	
    	
    	return polygons;
    }
	
	/**
	 * 
	 * @param polygons
	 * @return
	 */
    public static List<Geometry> mergePolygons(List<Geometry> polygons) {
    	boolean done;
    	do {
    	done = true;
    	for (int i = 0; i < polygons.size(); i++) {
    		Geometry a = polygons.get(i);
    	    for (int j = i + 1; j < polygons.size();) {
    	        final Geometry b = polygons.get(j);
    	        if (a.intersects(b)) {
    	        	
    	            polygons.set(i, (Polygon) a.union(b));
    	            a = polygons.get(i);
    	            polygons.remove(j);
    	            done = false;
    	        }
    	        else {
    	            j++;
    	        }
    	    }
    	}
    	} while (!done);
    	
    	return polygons;
    }
    /**
	 * 
	 * @param polygons
	 * @return
	 */
    public static List<Geometry> symDifference(List<Geometry> polygons) {
    	boolean done;
    	do {
    	done = true;
    	for (int i = 0; i < polygons.size(); i++) {
    		Geometry a = polygons.get(i);
    	    for (int j = i + 1; j < polygons.size();) {
    	        final Geometry b = polygons.get(j);
    	        if (a.intersects(b)) {
    	        	
    	            polygons.set(i, (Polygon) a.symDifference(b));
    	            a = polygons.get(i);
    	            polygons.remove(j);
    	            done = false;
    	        }
    	        else {
    	            j++;
    	        }
    	    }
    	}
    	} while (!done);
    	
    	return polygons;
    }
}
