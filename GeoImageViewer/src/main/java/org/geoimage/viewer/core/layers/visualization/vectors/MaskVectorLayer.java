/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.visualization.vectors;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.geoimage.analysis.VDSSchema;
import org.geoimage.opengl.OpenGLContext;
import org.geoimage.utils.PolygonOp;
import org.geoimage.viewer.core.GeometryImage;
import org.geoimage.viewer.core.api.IClickable;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.api.ilayer.IMask;
import org.geoimage.viewer.core.layers.visualization.AttributesGeometry;
import org.geoimage.viewer.core.layers.visualization.LayerPickedData;
import org.geoimage.viewer.util.JTSUtil;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import com.vividsolutions.jts.operation.distance.DistanceOp;


/**
 *
 * @author thoorfr
 */
public class MaskVectorLayer extends EditGeometryVectorLayer implements  IMask,IClickable{
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(MaskVectorLayer.class);
	public static final int COASTLINE_MASK=0;
	public static final int ICE_MASK=1;
	public static final int WINDFARM_MASK=2;
	public static final int GENERIC=99;

	//set default
	private int  maskType=COASTLINE_MASK;


    public int getMaskType() {
		return maskType;
	}

	public void setMaskType(int maskType) {
		this.maskType = maskType;
	}

	/**
     *
     * @param parent
     * @param layername
     * @param geomType
     * @param masktype coastline, ice or windfarm
     * @param layer
     */
    public MaskVectorLayer(ILayer parent,String layername,String geomType,int maskType, GeometryImage layer) {
    	super(parent,layername,geomType,layer);
    	this.maskType=maskType;

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
        for (Geometry geoms : glayer.getGeometries()) {
        	AttributesGeometry att=(AttributesGeometry) geoms.getUserData();
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
        if (getType().equals(GeometryImage.POINT)) {
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
       // if(bufferingDistance>0){
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
      //  }
    }


    /**
     *
     * @author argenpo
     *
     */
    private class ParallelBuffer implements Callable<Geometry> {
    	private Geometry bufferedGeom;
    	private double bufferingDistance=0;

    	/**
		 *
		 * @param
		 * @param
		 */

    	private ParallelBuffer(Geometry bufferedGeom,double bufferingDistance) {
			this.bufferedGeom=bufferedGeom;
			this.bufferingDistance=bufferingDistance;
		}

		@Override
		public Geometry call() {
				bufferedGeom =PolygonOp.removeInteriorRing(bufferedGeom);
			    if(!bufferedGeom.isValid()){
			    	bufferedGeom=JTSUtil.repair(bufferedGeom);
			    	//System.out.println(Arrays.toString(bufferedGeom.getCoordinates()));
			    	/*PrecisionModel pm=new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
				    GeometryFactory gf = new GeometryFactory(pm);
				    Coordinate[]cc=new Coordinate[bufferedGeom.getCoordinates().length+1];
				    for(int i=0;i<bufferedGeom.getCoordinates().length;i++){
				    	cc[i]=bufferedGeom.getCoordinates()[i];
				    }
				    cc[cc.length-1]=cc[0];
			    	bufferedGeom=gf.createPolygon(cc);*/
			    }
	            bufferedGeom =BufferOp.bufferOp(bufferedGeom,bufferingDistance,BufferParameters.CAP_SQUARE,BufferParameters.DEFAULT_QUADRANT_SEGMENTS);


			return bufferedGeom;
		}
	}


    /**
     *
     * @param bufferedGeom
     * @param bufferDistance
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
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
