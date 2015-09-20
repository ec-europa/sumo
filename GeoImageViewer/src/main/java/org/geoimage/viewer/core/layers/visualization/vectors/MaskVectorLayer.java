/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.visualization.vectors;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.geoimage.analysis.VDSSchema;
import org.geoimage.opengl.OpenGLContext;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.core.api.IClickable;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.layers.AttributesGeometry;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.visualization.LayerPickedData;
import org.geoimage.viewer.util.PolygonOp;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;


/**
 *
 * @author thoorfr
 */
public class MaskVectorLayer extends EditGeometryVectorLayer implements  IMask,IClickable{//,,IEditable,IMouseMove, IMouseDrag, IKeyPressed{
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(MaskVectorLayer.class);
    
    private Map<String,Boolean> intersectedMapCache=null;
    private Map<String,Boolean> includesMapCache=null;
    /**
     * 
     * @param parent
     * @param layername
     * @param type
     * @param layer
     */
    public MaskVectorLayer(ILayer parent,String layername,String type, GeometricLayer layer) {
    	super(parent,layername,type,layer);
    	intersectedMapCache=new HashMap<String,Boolean>();
    	includesMapCache=new HashMap<String,Boolean>();
        if (layer == null) {
            return;
        }
        this.glayer = layer;
        String test = glayer.getSchema('/');
        if (test.contains(VDSSchema.SIGNIFICANCE)) {
            calculateMaxMinTresh();
            threshable = true;
        }
        this.edit=false;
    }
    
    /**
     * 
     * @param imagePosition
     * @param context
     */
    public void mouseClicked(java.awt.Point imagePosition, OpenGLContext context) {
    	if(isEditable()){
	        this.selectedGeometry = null;
	        GeometryFactory gf = new GeometryFactory();
	        com.vividsolutions.jts.geom.Point p = gf.createPoint(new Coordinate(imagePosition.x, imagePosition.y));
	        for (Geometry temp : glayer.getGeometries()) {
	        	if(temp instanceof Polygon){
		        	Coordinate[] c=DistanceOp.nearestPoints(temp, p);
		        	com.vividsolutions.jts.geom.Point nearest=gf.createPoint(c[0]);
		            if (nearest.isWithinDistance(temp,5 * context.getZoom())) {
		                this.selectedGeometry = temp;
		                System.out.println(""+temp.getCoordinate().x+","+temp.getCoordinate().y);
		                LayerPickedData.put(temp, glayer.getAttributes(temp));
		                break;
		            }
	        	}   
	        }
    	}   
    }
    
    

    private void calculateMaxMinTresh() {
        minThresh = Double.MAX_VALUE;
        maxThresh = Double.MIN_VALUE;
        for (AttributesGeometry att : glayer.getAttributes()) {
            double temp = new Double("" + att.get(VDSSchema.SIGNIFICANCE));
            if (temp < minThresh) {
                minThresh = temp;
            }
            if (temp > maxThresh) {
                maxThresh = temp;
            }
        }
        currentThresh = minThresh - 0.01;
    }

    

 
    public boolean isRadio() {
        return false;
    }


    public String getDescription() {
        return getName();
    }

    public void dispose() {
    	if(glayer!=null){
    		glayer.clear();
    		glayer = null;
    	}	
    }

  
	private Boolean checkInIntersectionCache(int x, int y, int width, int height){
		return intersectedMapCache.get(new StringBuilder().append(x).append("_").append(y).append("_").append(width).append("_").append(height).toString());
	}
	private void putInIntersectionCache(int x, int y, int width, int height,Boolean intersects){
		intersectedMapCache.put(new StringBuilder().append(x).append("_").append(y).append("_").append(width).append("_").append(height).toString(),intersects);
	}
   
	private Boolean checkInIncludesCache(int x, int y, int width, int height){
		return includesMapCache.get(new StringBuilder().append(x).append("_").append(y).append("_").append(width).append("_").append(height).toString());
	}
	private void putInIncludesCache(int x, int y, int width, int height,Boolean intersects){
		includesMapCache.put(new StringBuilder().append(x).append("_").append(y).append("_").append(width).append("_").append(height).toString(),intersects);
	}
	
    public boolean intersects(int x, int y, int width, int height) {
        Boolean intersectLandCache=checkInIntersectionCache(x, y, width, height);
        if(intersectLandCache!=null)
        	return intersectLandCache.booleanValue();

    	boolean intersectLand=false;
        try {
            if (getType().equals("point")) {
                return false;
            }
            
            double[][]c={{x,y},{(x + width),y},{(x + width),(y + height)},{x, (y + height)},{x, y}};
            
            Geometry geom =(Geometry)(PolygonOp.createPolygon(c));
        
            	if(glayer!=null){
            		for (int idx=0;idx<glayer.getGeometries().size()&&intersectLand==false;idx++) {
		            	Geometry p=(Geometry) glayer.getGeometries().get(idx);
		            	if(p instanceof MultiPolygon){
		            		MultiPolygon mp=(MultiPolygon)p;
		            		for(int i=0;i<mp.getNumGeometries();i++){
			             		Geometry g=mp.getGeometryN(i);
			             		if(!g.isValid()){
			            			 Coordinate[] cs=g.getCoordinates();
				            		 List<Coordinate>lcs=new ArrayList<Coordinate>();
				            		 lcs.addAll(Arrays.asList(cs));
				            		 lcs.add(cs[0]);
				            		 GeometryFactory builder = new GeometryFactory();
				            		 Polygon e=builder.createPolygon(lcs.toArray(new Coordinate[0]));

				            		 if (e.intersects(geom)) {
				            			 intersectLand= true;
				            		 }		
			            		}else{
			            			if (g.intersects(geom)) 
			            				intersectLand=true;
			            		}
		            		}	
		            	}else{
		            		if(!p.isValid()){
		            			 Coordinate[] cs=p.getCoordinates();
			            		 List<Coordinate>lcs=new ArrayList<Coordinate>();
			            		 lcs.addAll(Arrays.asList(cs));
			            		 lcs.add(cs[0]);
			            		 GeometryFactory builder = new GeometryFactory();
			            		 Polygon e=builder.createPolygon(lcs.toArray(new Coordinate[0]));

			            		 if (e.intersects(geom)) 
			            			 intersectLand=true;
		            		}else{
		            			if (p.intersects(geom)) 
		            				intersectLand=true;
		            		}
		            	} 
		            	  
		            }
            	}	
        } catch (ParseException ex) {
            logger.error(ex.getMessage(), ex);
        }
        putInIntersectionCache( x,  y,  width,  height, intersectLand);
        return intersectLand;
    }
	

  

   
    
    /**
     * check if the layer contains the geometry
     * @param g
     * @return
     */
    public boolean contains(Geometry g) {
        for (Geometry p : glayer.getGeometries()) {
            if (p.contains(g)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(int x, int y) {
        if (getType().equals(GeometricLayer.POINT)) {
            return false;
        }
        GeometryFactory gf = new GeometryFactory();
        Point geom = gf.createPoint(new Coordinate(x, y));
        for (Geometry p : glayer.getGeometries()) {
            if (p.contains(geom)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 
     */
    public boolean includes(int x, int y, int width, int height) {
         if (getType().equals("point")) {
             return false;
         }

    	 Boolean includesLandCache=checkInIncludesCache(x, y, width, height);
         if(includesLandCache!=null)
         	return includesLandCache.booleanValue();
   
         try {
            WKTReader wkt = new WKTReader();
            StringBuilder polyStr=new StringBuilder("POLYGON((" )
            						.append(x).append(" ")
            						.append(y).append(",")
            						.append((x + width)).append(" ")
            						.append(y).append(",")
            						.append((x + width)).append(" ")
            						.append((y + height)).append(",")
            						.append(x).append(" ")
            						.append((y + height)).append(",")
            						.append(x).append(" ")
            						.append(y).append("))");
            
            Geometry geom = wkt.read(polyStr.toString());
            for (Geometry p : glayer.getGeometries()) {
                if (geom.within(p)) {
                	putInIncludesCache(x, y, width, height, true);
                    return true;
                }
            }
        } catch (ParseException ex) {
            logger.error(ex.getMessage(), ex);
        }
        putInIncludesCache(x, y, width, height, false);
        return false;
    }

    /**
     * rasterize the mask clipped with the Rectangle scaled back to full size with an offset onto a BufferedImage
     */
    public BufferedImage rasterize(int x,int y,int w,int h,  int offsetX, int offsetY, double scalingFactor) {
    	Rectangle rect=new Rectangle(x,y,w,h);
    	return rasterize(rect, offsetX, offsetY, scalingFactor);
    }
    /**
     * rasterize the mask clipped with the Rectangle scaled back to full size with an offset onto a BufferedImage
     */
    public BufferedImage rasterize(Rectangle rect, int offsetX, int offsetY, double scalingFactor) {

    	// create the buffered image of the size of the Rectangle
        BufferedImage image = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_BYTE_BINARY);
        GeometryFactory gf = new GeometryFactory();
        
        // define the clipping region in full scale
        Coordinate[] coords = new Coordinate[]{
            new Coordinate((int) (((double) rect.getMinX() / scalingFactor)), (int) (((double) rect.getMinY() / scalingFactor))),
            new Coordinate((int) (((double) rect.getMaxX() / scalingFactor)), (int) (((double) rect.getMinY() / scalingFactor))),
            new Coordinate((int) (((double) rect.getMaxX() / scalingFactor)), (int) (((double) rect.getMaxY() / scalingFactor))),
            new Coordinate((int) (((double) rect.getMinX() / scalingFactor)), (int) (((double) rect.getMaxY() / scalingFactor))),
            new Coordinate((int) (((double) rect.getMinX() / scalingFactor)), (int) (((double) rect.getMinY() / scalingFactor))),};
        
        Polygon geom = gf.createPolygon(gf.createLinearRing(coords));
        
        Graphics g2d = image.getGraphics();
        
        g2d.setColor(Color.WHITE);
        for (Geometry p : glayer.getGeometries()) {
            if (p.intersects(geom)) {
                int[] xPoints = new int[p.getNumPoints()];//build array for x coordinates
                int[] yPoints = new int[p.getNumPoints()];//build array for y coordinates
                int i = 0;
                for (Coordinate c : p.getCoordinates()) {
                    xPoints[i] = (int) ((c.x + offsetX) * scalingFactor);
                    yPoints[i] = (int) ((c.y + offsetY) * scalingFactor);
                    i++;
                }
                g2d.fillPolygon(xPoints, yPoints, p.getNumPoints());
            }
        }
        g2d.dispose();
        return image;
        
    }

    public Area getShape(int width, int height) {
        Area maskArea = new Area();
        
        Rectangle rect = new Rectangle(0, 0, width,height);//reader.getWidth(), reader.getHeight());

        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coords = new Coordinate[]{
            new Coordinate((int) rect.getMinX(), (int) rect.getMinY()),
            new Coordinate((int) rect.getMaxX(), (int) rect.getMinY()),
            new Coordinate((int) rect.getMaxX(), (int) rect.getMaxY()),
            new Coordinate((int) rect.getMinX(), (int) rect.getMaxY()),
            new Coordinate((int) rect.getMinX(), (int) rect.getMinY()),};
        Polygon geom = gf.createPolygon(gf.createLinearRing(coords), null);
        for (Geometry p : glayer.getGeometries()) {
            if (p.intersects(geom)) {
                int[] xPoints = new int[p.getNumPoints()];
                int[] yPoints = new int[p.getNumPoints()];
                int i = 0;
                for (Coordinate c : p.getCoordinates()) {
                    xPoints[i] = (int) (c.x);
                    yPoints[i++] = (int) (c.y);
                }
                maskArea.add(new Area(new java.awt.Polygon(xPoints, yPoints, p.getNumPoints())));
            }
        }
        return maskArea;
    }

   
    
    /**
     * create the new buffered layer
     */
    public void buffer(double bufferingDistance) {
        List<Geometry> bufferedGeom=glayer.getGeometries();
        
        try {
			bufferedGeom=parallelBuffer(bufferedGeom, bufferingDistance);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
        
        // then merge them
        List<Geometry> newgeoms = new ArrayList<Geometry>();
        List<Geometry> remove = new ArrayList<Geometry>();
        if(bufferingDistance>0){
	        //ciclo sulle nuove geometrie
	        for (Geometry buffGeom : bufferedGeom) {
	            boolean isnew = true;
	            remove.clear();
	            for (Geometry newg : newgeoms) {
	                if (newg.contains(buffGeom)) { //se newg contiene g -> g deve essere rimossa
	                    isnew = false;
	                    break;
	                } else if (buffGeom.contains(newg)) { //se g contiene newg -> newg deve essere rimossa
	                    remove.add(newg);
	                }
	            }
	            if (isnew) {
	                newgeoms.add(buffGeom);
	            }
	            newgeoms.removeAll(remove);
	        }
	        glayer.clear();
	
	        // assign new value
	        for (Geometry geom :newgeoms) {
	            glayer.put(geom);
	        }
        }
        
       /* 
        for (int i=0;i<bufferedGeom.length;i++) {
        	//applico il buffer alla geometria
        	/*if(bufferedGeom[i] instanceof Polygon){
        		bufferedGeom[i] = ((Polygon)bufferedGeom[i]).getExteriorRing();
        	}else if(bufferedGeom[i] instanceof MultiPolygon){
        		Geometry g=bufferedGeom[i].getGeometryN(0);
        		for(int ii=1;ii<((MultiPolygon)bufferedGeom[i]).getNumGeometries();ii++){
        			g=g.union(bufferedGeom[i].getGeometryN(ii));
        		}	
        		bufferedGeom[i] = g;
        	}else{
        		bufferedGeom[i] =PolygonOp.removeInteriorRing(bufferedGeom[i]);
        	}*/
       /* 	bufferedGeom[i] =PolygonOp.removeInteriorRing(bufferedGeom[i]);
        	bufferedGeom[i]=bufferedGeom[i].buffer(0);
            bufferedGeom[i] = EnhancedPrecisionOp.buffer(bufferedGeom[i], bufferingDistance);
           
        }
        // then merge them
        List<Geometry> newgeoms = new ArrayList<Geometry>();
        List<Geometry> remove = new ArrayList<Geometry>();
        
        if(bufferingDistance>0){
	        //ciclo sulle nuove geometrie
	        for (Geometry buffGeom : bufferedGeom) {
	            boolean isnew = true;
	            remove.clear();
	            for (Geometry newg : newgeoms) {
	                if (newg.contains(buffGeom)) { //se newg contiene g -> g deve essere rimossa
	                    isnew = false;
	                    break;
	                } else if (buffGeom.contains(newg)) { //se g contiene newg -> newg deve essere rimossa
	                    remove.add(newg);
	                }
	            }
	            if (isnew) {
	                newgeoms.add(buffGeom);
	            }
	            newgeoms.removeAll(remove);
	        }
	        glayer.clear();
	
	        // assign new value
	        for (Geometry geom :newgeoms) {
	            glayer.put(geom);
	        }
        }else{
        	for (Geometry geom :bufferedGeom) {
	            glayer.put(geom);
	        }
        }*/
    }
    
    
    
    class ParallelBuffer implements Callable<Geometry> {
    	private Geometry bufferedGeom;
    	private double bufferingDistance=0;
		/**
		 * 
		 * @param 
		 * @param 
		 */
		ParallelBuffer(Geometry bufferedGeom,double bufferingDistance) {
			this.bufferedGeom=bufferedGeom;
			this.bufferingDistance=bufferingDistance;
		}

		@Override
		public Geometry call() {
	        	//applico il buffer alla geometria
	        	/*if(bufferedGeom instanceof Polygon){
	        		bufferedGeom = ((Polygon)bufferedGeom).getExteriorRing();
	        	}else if(bufferedGeom instanceof MultiPolygon){
	        		Geometry g=bufferedGeom.getGeometryN(0);
	        		for(int ii=1;ii<((MultiPolygon)bufferedGeom).getNumGeometries();ii++){
	        			g=g.union(bufferedGeom.getGeometryN(ii));
	        		}	
	        		bufferedGeom = g;
	        	}else{
	        		bufferedGeom =PolygonOp.removeInteriorRing(bufferedGeom);
	        	}*/
				//
				bufferedGeom =PolygonOp.removeInteriorRing(bufferedGeom);
			    if(!bufferedGeom.isValid()){
			    	//System.out.println(Arrays.toString(bufferedGeom.getCoordinates()));
			    	PrecisionModel pm=new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
				    GeometryFactory gf = new GeometryFactory(pm);
				    Coordinate[]cc=new Coordinate[bufferedGeom.getCoordinates().length+1];
				    for(int i=0;i<bufferedGeom.getCoordinates().length;i++){
				    	cc[i]=bufferedGeom.getCoordinates()[i];
				    }
				    cc[cc.length-1]=cc[0];
			    	bufferedGeom=gf.createPolygon(cc);
			    }
	            bufferedGeom =bufferedGeom.buffer(bufferingDistance);
			return bufferedGeom;
		}
	}
    
    
    
    private List<Geometry> parallelBuffer(List<Geometry> bufferedGeom,double bufferDistance)throws InterruptedException, ExecutionException {
		int processors = Runtime.getRuntime().availableProcessors();
		ThreadPoolExecutor executor = new ThreadPoolExecutor(2, processors, 5000, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());

		List<Callable<Geometry>> tasks = new ArrayList<Callable<Geometry>>();
		for (int i=0;i<bufferedGeom.size();i++) {
			 tasks.add(new ParallelBuffer(bufferedGeom.get(i),bufferDistance));
		}
		List<Future<Geometry>> results = executor.invokeAll(tasks);
		executor.shutdown();

		List<Geometry> geoms = new ArrayList<Geometry>();
		for (Future<Geometry> result : results) {
			List<Geometry> l = Arrays.asList(result.get());
			geoms.addAll(l);
		}

		return geoms;
	}
    
    
  
    
    public List<Geometry> getGeometries() {
        return glayer.getGeometries();
    }





}
