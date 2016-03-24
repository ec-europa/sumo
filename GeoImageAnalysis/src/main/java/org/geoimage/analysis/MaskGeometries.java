/*
 * 
 */
package org.geoimage.analysis;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.xml.resolver.apps.resolver;
import org.geoimage.utils.PolygonOp;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.algorithm.locate.IndexedPointInAreaLocator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Location;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.geom.prep.PreparedPoint;
import com.vividsolutions.jts.geom.prep.PreparedPolygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class MaskGeometries {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(MaskGeometries.class);
	private List<Geometry> maskGeometries;
	private String maskName=null;
	private Map<String,Boolean> intersectedMapCache=null;
    private Map<String,Boolean> includesMapCache=null;

	public List<Geometry> getMaskGeometries() {
		return maskGeometries;
	}
	public void setMaskGeometries(List<Geometry> maskGeometries) {
		this.maskGeometries = maskGeometries;
	}



    public String getMaskName() {
		return maskName;
	}
	public void setMaskName(String maskName) {
		this.maskName = maskName;
	}
	public MaskGeometries(String maskName,List<Geometry> maskGeometries) {
    	this.maskName=maskName;
    	this.maskGeometries=maskGeometries;
    	intersectedMapCache=new HashMap<String,Boolean>();
    	includesMapCache=new HashMap<String,Boolean>();
	}




	private Boolean checkInIncludesCache(int x, int y, int width, int height){
		return includesMapCache.get(new StringBuilder().append(x).append("_").append(y).append("_").append(width).append("_").append(height).toString());
	}
	private void putInIncludesCache(int x, int y, int width, int height,Boolean intersects){
		includesMapCache.put(new StringBuilder().append(x).append("_").append(y).append("_").append(width).append("_").append(height).toString(),intersects);
	}


	private Boolean checkInIntersectionCache(int x, int y, int width, int height){
		return intersectedMapCache.get(new StringBuilder().append(x).append("_").append(y).append("_").append(width).append("_").append(height).toString());
	}
	private void putInIntersectionCache(int x, int y, int width, int height,Boolean intersects){
		intersectedMapCache.put(new StringBuilder().append(x).append("_").append(y).append("_").append(width).append("_").append(height).toString(),intersects);
	}


	 /**
     *
     */
    public boolean includes(int x, int y, int width, int height) {
      /*   if (getType().equals("point")) {
             return false;
         }*/

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
            for (Geometry p : maskGeometries) {
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
     * check if the layer contains the geometry
     * @param g
     * @return
     */
    public boolean contains(Geometry g) {
        for (Geometry p : maskGeometries) {
            if (p.contains(g)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(int x, int y) {
        GeometryFactory gf = new GeometryFactory();
        Point geom = gf.createPoint(new Coordinate(x, y));
        for (Geometry p : maskGeometries) {
            if (p.contains(geom)) {
                return true;
            }
        }
        return false;
    }


    public boolean intersects(int x, int y, int width, int height) {
        Boolean intersectLandCache=checkInIntersectionCache(x, y, width, height);
        if(intersectLandCache!=null)
        	return intersectLandCache.booleanValue();

    	boolean intersectLand=false;
        try {

            double[][]c={{x,y},{(x + width),y},{(x + width),(y + height)},{x, (y + height)},{x, y}};

            Geometry geom =(Geometry)(PolygonOp.createPolygon(c));
            GeometryFactory builder = new GeometryFactory();

            	if(maskGeometries!=null&&!maskGeometries.isEmpty()){
            		for (int idx=0;idx<maskGeometries.size()&&intersectLand==false;idx++) {
		            	Geometry p=(Geometry) maskGeometries.get(idx);


	            		/*for(int i=0;i<p.getNumGeometries();i++){
	            			Geometry g=p.getGeometryN(i);

	            			if(!g.isValid()){*/
	            				//TODO: change this part for performance
		            			/* Coordinate[] cs=g.getCoordinates();
			            		 List<Coordinate>lcs=new ArrayList<Coordinate>();
			            		 lcs.addAll(Arrays.asList(cs));
			            		 lcs.add(cs[0]);

			            		Geometry e=builder.createPolygon(lcs.toArray(new Coordinate[0]));
	            				*/
	            	/*			Geometry g2=g.buffer(0);

			            		 if (g2.intersects(geom)) {
			            			 intersectLand= true;
			            		 }
		            		}else{
		            			if (g.intersects(geom))
		            				intersectLand=true;
		            		}
	            		}*/



		            	if(p instanceof MultiPolygon){
		            		MultiPolygon mp=(MultiPolygon)p;
		            		for(int i=0;i<mp.getNumGeometries();i++){
			             		Geometry g=mp.getGeometryN(i);
			             		if(!g.isValid()){
			            			 Coordinate[] cs=g.getCoordinates();
				            		 List<Coordinate>lcs=new ArrayList<Coordinate>();
				            		 lcs.addAll(Arrays.asList(cs));
				            		 lcs.add(cs[0]);
				            		 Geometry e=builder.createPolygon(lcs.toArray(new Coordinate[0]));
				            		 e=e.buffer(0);

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
			            		 Geometry e=builder.createPolygon(lcs.toArray(new Coordinate[0]));
			            		 e=e.buffer(0);
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
     * rasterize the mask clipped with the Rectangle scaled back to full size with an offset onto a BufferedImage
     */
    public int[] getRasterDataMask(int x,int y,int w,int h,  int offsetX, int offsetY, double scalingFactor) {
    	Rectangle rect=new Rectangle(x,y,w,h);
        BufferedImage bi = this.rasterize(rect, offsetX, offsetY, scalingFactor);
        Raster rastermask=bi.getData();
    	int[] maskdata=rastermask.getSamples(0, 0, rastermask.getWidth(), rastermask.getHeight(), 0,(int[])null);
    	//int[] maskdata=rasterMaskJTS(rect, offsetX, offsetY, scalingFactor);
    	return maskdata;
    }

    /**
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @param offsetX
     * @param offsetY
     * @param scalingFactor
     * @param output
     * @return true if the raster is saved
     */
    public boolean saveRaster(int x,int y,int w,int h,  int offsetX, int offsetY, double scalingFactor,File output){
    	Rectangle rect=new Rectangle(x,y,w,h);
//        BufferedImage rastermask = this.rasterizeJTS(rect, offsetX, offsetY, scalingFactor);
        BufferedImage rastermask = this.rasterize(rect, offsetX, offsetY, scalingFactor);

        try {
			ImageIO.write(rastermask, "bmp", output);
			return true;
		} catch (IOException e) {
			logger.warn(e.getMessage());
			return false;
		}

    }

    public boolean saveRaster(BufferedImage rastermask ,File output){
        try {
			ImageIO.write(rastermask, "bmp", output);
			return true;
		} catch (IOException e) {
			logger.warn(e.getMessage());
			return false;
		}

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
        for (Geometry p : maskGeometries) {
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

    /**
     * rasterize the mask clipped with the Rectangle scaled back to full size with an offset onto a BufferedImage
     */
    public BufferedImage rasterizeJTS(Rectangle rect, int offsetX, int offsetY, double scalingFactor) {

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

        for (Geometry p : maskGeometries) {
            if (p.intersects(geom)) {
            	Geometry pg=p.intersection(geom).buffer(0);
            	//Coordinate[] coordsInter=gg.getCoordinates();
            	//Polygon pg=gf.createPolygon(coordsInter);

            	for(int x=0;x<rect.width;x++){
            		for(int y=0;y<rect.height;y++){
            			Point point=gf.createPoint(new Coordinate(rect.x+x,rect.y+y));
            			if(pg.contains(point)){
		            		try{
		            			image.setRGB(x,y, Color.WHITE.getRGB());
		            		}catch(Exception e){
		            			logger.error(e.getMessage()+"  x:"+x+"  y:"+y);
		            		}
            			}
	            	}
	            }
	        }
        }
        return image;

    }

    /**
     * rasterize the mask clipped with the Rectangle scaled back to full size with an offset onto a BufferedImage
     */
    public int[] rasterMaskJTS(Rectangle rect, int offsetX, int offsetY, double scalingFactor) {

    	// create the buffered image of the size of the Rectangle
        int[] mask = new int[rect.width* rect.height];
        GeometryFactory gf = new GeometryFactory();

        // define the clipping region in full scale
        Coordinate[] coords = new Coordinate[]{
            new Coordinate((int) (((double) rect.getMinX() / scalingFactor)), (int) (((double) rect.getMinY() / scalingFactor))),
            new Coordinate((int) (((double) rect.getMaxX() / scalingFactor)), (int) (((double) rect.getMinY() / scalingFactor))),
            new Coordinate((int) (((double) rect.getMaxX() / scalingFactor)), (int) (((double) rect.getMaxY() / scalingFactor))),
            new Coordinate((int) (((double) rect.getMinX() / scalingFactor)), (int) (((double) rect.getMaxY() / scalingFactor))),
            new Coordinate((int) (((double) rect.getMinX() / scalingFactor)), (int) (((double) rect.getMinY() / scalingFactor))),};

        Polygon geom = gf.createPolygon(gf.createLinearRing(coords));
        PreparedPolygon ppol=new PreparedPolygon(geom);

        int numPix=rect.width*rect.height;
        for (Geometry p : maskGeometries) {
            if (ppol.intersects(p)) {
            	Geometry pg=p.intersection(geom).buffer(0);
            	IndexedPointInAreaLocator locator=new IndexedPointInAreaLocator(pg);


            	int x=0;
            	int y=0;

            	for(int ii=0;ii<numPix;ii++){
            		if(ii%rect.width==0){
            			x=0;
            			y++;
            		}
        			//Point point=gf.createPoint(new Coordinate(rect.x+x,rect.y+y));
        			//PreparedPoint ppoint=new PreparedPoint(point);
        			//if(ppoint.within(pg)){
            		int loc=locator.locate(new Coordinate(rect.x+x,rect.y+y));
            		if(loc==Location.INTERIOR||loc==Location.BOUNDARY)
	            		try{
	            			mask[x]=1;
	            		}catch(Exception e){
	            			logger.warn(e.getMessage()+"  x:"+x+"  y:"+y);
	            		}
        			}
	            }
	        //}
        }
        return mask;

    }
}
