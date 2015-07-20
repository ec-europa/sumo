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
import java.util.List;

import org.geoimage.analysis.VDSSchema;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.core.PickedData;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.IClickable;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.layers.GenericLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.util.PolygonOp;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

/**
 *
 * @author thoorfr
 */
public class MaskVectorLayer extends GenericLayer implements  IMask,IClickable{
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(MaskVectorLayer.class);
    protected Geometry currentTile=null;
    
    /**
     * 
     * @param parent
     * @param layername
     * @param type
     * @param layer
     */
    public MaskVectorLayer(ILayer parent,String layername,String type, GeometricLayer layer) {
    	super(parent,layername,type,layer);
        if (layer == null) {
            return;
        }
        this.glayer = layer;
        String test = glayer.getSchema('/');
        if (test.contains(VDSSchema.SIGNIFICANCE)) {
            calculateMaxMinTresh();
            threshable = true;
        }
    }
    

    

    private void calculateMaxMinTresh() {
        minThresh = Double.MAX_VALUE;
        maxThresh = Double.MIN_VALUE;
        for (Attributes att : glayer.getAttributes()) {
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

    

 
    /**
	 * 
	 */
	@Override
    public void mouseClicked(java.awt.Point imagePosition, int button, GeoContext context) {
        this.selectedGeometry = null;
        GeometryFactory gf = new GeometryFactory();
        Point p = gf.createPoint(new Coordinate(imagePosition.x, imagePosition.y));
        for (Geometry temp : glayer.getGeometries()) {
            //if (temp.isWithinDistance(p, 5 * context.getZoom())) {
            if (p.equalsExact(temp, 5 * context.getZoom())) {
                this.selectedGeometry = temp;
                PickedData.put(temp, glayer.getAttributes(temp));
            }
        }
    }
    
     public void render(GeoContext context) {
    	 super.render(context);
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

    public Geometry getCurrentTile() {
		return currentTile;
	}



	public void setCurrentTile(Geometry currentTile) {
		this.currentTile = currentTile;
	}
	
   
    public boolean intersects(int x, int y, int width, int height) {
        try {
            if (getType().equals("point")) {
                return false;
            }
            double[][]c={{x,y},{(x + width),y},{(x + width),(y + height)},{x, (y + height)},{x, y}};
            
            Geometry geom =(Geometry)(PolygonOp.createPolygon(c));
            this.setCurrentTile(geom);
          //for test only
           /*  try {
					SimpleShapefile.exportGeometriesToShapeFile(glayer.getGeometries(),new File("F:\\SumoImgs\\export\\aaa2.shp") ,"Polygon");
				} catch (IOException | SchemaException e) {
					e.printStackTrace();
				}*/
          /*  if(x>1790&&x<1900&&y>5800&&y<6100){
            	try {
            		List<Geometry>lg=new ArrayList<>();
            		lg.add(geom);
					SimpleShapefile.exportGeometriesToShapeFile(lg,new File("F:\\SumoImgs\\export\\gg"+x+"-"+y+".shp") ,"Polygon");
				} catch (IOException | SchemaException e) {
					e.printStackTrace();
				}
            }*/
            	if(glayer!=null){
            		for (Geometry pp : glayer.getGeometries()) {
		            	Geometry p=(Geometry) pp.clone();
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

				            		 if (e.intersects(geom)) 
				 		        			return true;
			            		}else{
			            			if (g.intersects(geom)) 
			 		        			return true;
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
			 		        			return true;
		            		}else{
		            			if (p.intersects(geom)) 
		 		        			return true;
		            		}
				            /*if(total==null)
	            				total=p;
	            			else
	            				total=total.union(p);
	            			*/
		            	} 
		            	  
		            }
            	}	
        } catch (ParseException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return false;
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

    public boolean includes(int x, int y, int width, int height) {
        try {
            if (getType().equals("point")) {
                return false;
            }
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
                    return true;
                }
            }
            return false;
        } catch (ParseException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return false;
    }

 // rasterize the mask clipped with the Rectangle scaled back to full size with an offset onto a BufferedImage
    public BufferedImage rasterize(int x,int y,int w,int h,  int offsetX, int offsetY, double scalingFactor) {
    	Rectangle rect=new Rectangle(x,y,w,h);
    	return rasterize(rect, offsetX, offsetY, scalingFactor);
    }
    // rasterize the mask clipped with the Rectangle scaled back to full size with an offset onto a BufferedImage
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
        Polygon geom = gf.createPolygon(gf.createLinearRing(coords), null);
        Graphics g2d = image.getGraphics();
        g2d.setColor(Color.WHITE);
        for (Geometry p : glayer.getGeometries()) {
            if (p.intersects(geom)) {
                int[] xPoints = new int[p.getNumPoints()];
                int[] yPoints = new int[p.getNumPoints()];
                int i = 0;
                for (Coordinate c : p.getCoordinates()) {
                    xPoints[i] = (int) ((c.x + offsetX) * scalingFactor);
                    yPoints[i++] = (int) ((c.y + offsetY) * scalingFactor);
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
        Geometry[] bufferedGeom=glayer.getGeometries().toArray(new Geometry[0]);
        
        for (int i=0;i<bufferedGeom.length;i++) {
        	//applico il buffer alla geometria
            //if(bufferingDistance>0)
            bufferedGeom[i] = EnhancedPrecisionOp.buffer(bufferedGeom[i], bufferingDistance);
        	bufferedGeom[i] = PolygonOp.removeInteriorRing(bufferedGeom[i]);
        }
        // then merge them
        List<Geometry> newgeoms = new ArrayList<Geometry>();
        List<Geometry> remove = new ArrayList<Geometry>();
        
       
        //ciclo sulle nuove geometrie
        for (Geometry g : bufferedGeom) {
            boolean isnew = true;
            remove.clear();
            for (Geometry newg : newgeoms) {
                if (newg.contains(g)) { //se newg contiene g -> g deve essere rimossa
                    isnew = false;
                    break;
                } else if (g.contains(newg)) { //se g contiene newg -> newg deve essere rimossa
                    remove.add(newg);
                }
            }
            if (isnew) {
                newgeoms.add(g);
            }
            newgeoms.removeAll(remove);
        }
        glayer.clear();

        // assign new value
        for (Geometry geom :newgeoms) {
            glayer.put(geom);
        }
        
    }
    
  
    
    public List<Geometry> getGeometries() {
        return glayer.getGeometries();
    }


}
