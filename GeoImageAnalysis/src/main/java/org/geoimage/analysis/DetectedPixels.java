/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.analysis;

import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 *
 * @author thoorfr
 */
public class DetectedPixels {
	public class BoatPixel {

	    public int x;
	    public int y;
	    public int value;
	    public double tileAvg;
	    public double tileStd;
	    public double threshold;
	    public int band;
	    public int id;

	    public BoatPixel(int x, int y, int value, double tileAvg, double tileStd, double threshold, int band) {
	        this.x = x;
	        this.y = y;
	        this.value = value;
	        this.tileAvg = tileAvg;
	        this.tileStd = tileStd;
	        this.threshold = threshold;
	        this.band = band;
	    }
	}
    private Map<String, BoatPixel> allDetectedPixels = new HashMap<String, BoatPixel>();
    private Map<String, BoatPixel> aggregatedPixels = new HashMap<String, BoatPixel>();
    private Map<Integer, List<int[]>> aggregatedBoats = new HashMap<Integer, List<int[]>>();
   // private Boat[] boatArray = null;
    // size of the search window in meters
    private final int SEARCHWINDOW = 50;
   
    private double pixsam;//range
	private double pixrec;//azimuth
    private int searchwindowWidth = 1;
    private int searchwindowHeight = 1;
    
    ArrayList<BoatConnectedPixelMap> listboatneighbours = new ArrayList<BoatConnectedPixelMap>();

    private Logger logger= LoggerFactory.getLogger(DetectedPixels.class);

    
    public double getPixsam() {
		return pixsam;
	}


	public void setPixsam(double pixsam) {
		this.pixsam = pixsam;
	}
    
   
    /**
     * 
     * @param gir
     */
    public DetectedPixels(double pixsam,double pixrec) {
        // get the image pixel size
        this.pixsam = pixsam;
        this.pixrec = pixrec;

        // calculate search window size using pixel size
        if (SEARCHWINDOW > pixsam) {
            this.searchwindowWidth = (int) (SEARCHWINDOW / pixsam);
        }
        if (SEARCHWINDOW > pixrec) {
            this.searchwindowHeight = (int) (SEARCHWINDOW / pixrec);
        }
    }

    
    /**
     * 
     * @param x
     * @param y
     * @param value
     * @param tileAvg
     * @param tileStd
     * @param threshold
     * @param band
     */
    public void add(int x, int y, int value, double tileAvg, double tileStd, double threshold, int band) {
        BoatPixel boatPixel = new BoatPixel(x, y, value, tileAvg, tileStd, threshold, band);
        StringBuilder point=new StringBuilder("").append(x).append(" ").append(y);
        allDetectedPixels.put(point.toString(), boatPixel);

    }

    /**
     *  add the detectedpixels
     * @param pixels
     */
    public void addAll(DetectedPixels pixels) {
    	Map<String, BoatPixel> BoatPixel = pixels.getDetectedPixels();
    	Collection<BoatPixel> boats = BoatPixel.values();
    	StringBuilder point=new StringBuilder();
        for (BoatPixel boat:boats) {
        	point.setLength(0);
        	point=point.append(boat.x).append(" ").append(boat.y);
            allDetectedPixels.put(point.toString(), boat);
        }
    }
    /**
     * 
     * @param pixels
     */
    // marge the detectedpixels
    public void merge(DetectedPixels pixels) {
    	Map<String, BoatPixel> BoatPixel = pixels.getDetectedPixels();
    	Collection<BoatPixel> boats = BoatPixel.values();
    	StringBuilder position = new StringBuilder();
        for (BoatPixel boat:boats) {
        	position.setLength(0);
            // do not add if position already exists
            position = position.append(boat.x).append(" ").append(boat.y); 
            //if (allDetectedPixels.get(position.toString()) == null) {
            	//if the value is already in the map it will be replaced 
                allDetectedPixels.put(position.toString(), boat);
            //}
        }
    }

/*    public Boat[] getBoats() {
        return boatArray;
    }
*/
    private Map<String, BoatPixel> getDetectedPixels() {
        return allDetectedPixels;
    }
    
    /**
     * 
     * @param x
     * @param y
     * @param id
     */
    private void aggregate(int x, int y, int id) {
        int[] seed = {x, y, id};
        ArrayList<int[]> list = new ArrayList<int[]>();
        list.add(seed);
        while (list.size() > 0) {
            int[] p = list.remove(0);
            int xx = p[0];
            int yy = p[1];
            int idd = p[2];
            for (int i = xx - this.searchwindowWidth; i < xx + this.searchwindowWidth + 1; i++) {
                for (int j = yy - this.searchwindowHeight; j < yy + this.searchwindowHeight + 1; j++) {
                	String key = new StringBuilder().append(i).append(" ").append(j).toString();
                    BoatPixel pixel = allDetectedPixels.get(key);
                    if (pixel != null && !(i == xx && j == yy)) {
                        if (aggregatedPixels.get(key) == null) {
                            pixel.id=idd;
                            List<int[]> agBoat = aggregatedBoats.get(idd);
                            if (agBoat == null) {
                                agBoat = new ArrayList<int[]>();
                            }
                            agBoat.add(new int[]{i, j});
                            aggregatedBoats.put(idd, agBoat);
                            aggregatedPixels.put(key, pixel);
                            list.add(new int[]{i, j, idd});
                        }
                    }
                }
            }
        }
    }
    
    public void agglomerate() {
    	BoatPixel[] boats = allDetectedPixels.values().toArray(new BoatPixel[0]);
        int id = -1;
        for (BoatPixel boat:boats) {
            List<int[]> agBoat = new ArrayList<int[]>();
            int x = boat.x;
            int y = boat.y;
            String position = new StringBuilder().append(x).append(" ").append(y).toString();
            agBoat.add(new int[]{x, y});
            if (aggregatedPixels.get(position) == null) {
                boat.id=++id;
                aggregatedBoats.put(id, agBoat);
                aggregatedPixels.put(position, boat);
                aggregate(x, y, id);
            }
        }
    //computeBoatsAttributes();
    }


    /**
     * 
     * @param pixels
     * @param imagemap
     * @param imagedata
     * @param thresholdaggregate
     * @param position
     * @param neighboursdistance
     * @param tilesize
     * @param rastermask
     * @return
     */
    public boolean checkNeighbours(List<int[]> pixels, int[] imagemap, 
    		int[][] imagedata, 
    		double[][] thresholdaggregate, 
    		int[] position, int neighboursdistance, 
    		int tilesize, 
    		Raster rastermask) {
    	
        int numberofbands = thresholdaggregate.length;
        // touches land flag
        boolean result = false;
        // create pixels list
        ArrayList<int[]> localpixels = new ArrayList<int[]>();
        // add the pixel to the local list
        localpixels.add(new int[]{position[0], position[1]});
        // mark this pixel as checked
        imagemap[position[0] + position[1] * tilesize] = 1;
        // search for all connected neighbours
        while (!localpixels.isEmpty()) {
            // get pixel from local list
            int[] localpixel = localpixels.get(0);
            // remove pixel from local list
            localpixels.remove(localpixel);
            // check neighbouring pixels
            for (int i = localpixel[0] - neighboursdistance; i < localpixel[0] + neighboursdistance + 1; i++) {
                for (int j = localpixel[1] - neighboursdistance; j < localpixel[1] + neighboursdistance + 1; j++) {
                    // check neighbour is within tile
                    if ((i < 0) || (i >= tilesize) || (j < 0) || (j >= tilesize)) {
                        continue;
                    }

                    // check if pixel has already been checked
                    if (imagemap[i + j * tilesize] == 0) {
                        // mark as checked
                        imagemap[i + j * tilesize] = 1;
                        // check if pixel is in sea
                        if ((rastermask == null) || (rastermask.getSample(i, j, 0) == 0)) {

                        	
                        	boolean aggregated = false;
                            boolean clipped = false;
                            int value = 0;
                            
                            //calculate the max pixel value for all bands --> Here we use only one value!!!! 
                            for (int band = 0; band < numberofbands; band++) {
                                int pixelvalue = imagedata[band][i + tilesize * j];

                                // check aggregate threshold
                                if (pixelvalue > thresholdaggregate[band][0]) {
                                    aggregated = true;
                                    if (value < pixelvalue) {
                                        value = pixelvalue;
                                    }

                                    // check clipped threshold
                                    if (pixelvalue > thresholdaggregate[band][1]) {
                                        clipped = true;
                                    }
                                }
                            }
                            if (aggregated) {
                                //String positionstring = i + " " + j;
                                // add pixel to the list of pixels
                                //pixels.put(positionstring, new int[]{i, j, value, clipped ? 1 : 0});
                            	pixels.add(new int[]{i, j, value, clipped ? 1 : 0});
                                localpixels.add(new int[]{i, j});
                            }
                        } else {
                            // point extends to the land mask
                            result = true;
                        }
                    }
                }
            }
        }
        /*
        // go through list
        for(int[] pixel : localpixels)
        if(checkNeighbours(pixels, imagemap, imagedata, thresholdaggregate, pixel, neighboursdistance, tilesize, rastermask))
        result = true;
         */
        return result;
    }

  
    /**
     * 
     */
    public void computeBoatsAttributes(String polarization) {
        Collection<List<int[]>> enumeration = aggregatedBoats.values();
        List <Boat> boatsTemp=new ArrayList<Boat>();
        
        for (List<int[]>agBoat:enumeration) {
            // start with estimating length, size and heading
            double[] boatvalues = Compute.fLenWidHead(agBoat, this.pixsam, this.pixrec);

            // look for maximum brightness point in cluster
            int[][] it = agBoat.toArray(new int[0][]);
        
            // get the first boat in the aggregate
            int[] pixel = null;
            if (it.length>0) {
                pixel = it[0];
                // store in the table the boat values and the estimated size, heading and bearing of the aggregate
                String id=new StringBuilder().append(pixel[0]).append(" ").append(pixel[1]).toString();
                BoatPixel pixelValue = aggregatedPixels.get(id);
                
                //boatvalues[0]=number of pixel boatvalues[1-2]= posx e posy  boatvalues[3-4-5]=length,width,heading
                Boat boatValue=new Boat(pixelValue.id, boatvalues[1], boatvalues[2], boatvalues[0],
                						boatvalues[3], boatvalues[4], boatvalues[5],
                						pixelValue.value,pixelValue.tileAvg,pixelValue.tileStd,
                						pixelValue.threshold,pixelValue.band,polarization);
  
                // look for maximum value in agglomerate
                for (int i=1;i<it.length;i++) {
                    pixel = it[i];
                    String key=new StringBuilder().append(pixel[0]).append(" ").append(pixel[1]).toString();
                    BoatPixel pxBoat = aggregatedPixels.get(key);
                    if (pxBoat.value > boatValue.getMaxValue()) {
                    	boatValue.setMaxValue(pxBoat.value);
                    }
                }
                
                // check if agglomerated boat has a width or a length larger than filterSize
                if (boatValue.getLength() > ConstantVDSAnalysis.filterminSize && boatValue.getWidth() > ConstantVDSAnalysis.filterminSize) { //AG changed || to &&

                    // check if agglomerated boat has a width or a length lower than filterSize
                    if (boatValue.getLength() < ConstantVDSAnalysis.filtermaxSize && boatValue.getWidth() < ConstantVDSAnalysis.filtermaxSize) {
                    	boatsTemp.add(boatValue);
                    }
                }
            }
        }
        //boatsTemp.clear();
        //boatsTemp=sortBoats(boatsTemp);
       // boatArray=boatsTemp.toArray(new Boat[0]);			
    }

   
    
    
   

    public Collection<BoatPixel> getAllDetectedPixelsValues() {
    	return allDetectedPixels.values();
    }
    
    /**
     * 
     * @return
     */
    public List<Geometry> getAllDetectedPixels() {
        List<Geometry> out = new ArrayList<Geometry>();
        GeometryFactory gf = new GeometryFactory();
        
        BoatPixel[] enumeration = allDetectedPixels.values().toArray(new BoatPixel[0]);
        for (BoatPixel pixel:enumeration) {
            out.add(gf.createPoint(new Coordinate(pixel.x, pixel.y)));
        }

        return out;
    }

    public List<Geometry> getThresholdclipPixels() {
        List<Geometry> out = new ArrayList<Geometry>();
        GeometryFactory gf = new GeometryFactory();
        for (BoatConnectedPixelMap boat : listboatneighbours) {
            List<int[]> positions = boat.getThresholdclipPixels();
            for (int[] position : positions) {
                out.add(gf.createPoint(new Coordinate(position[0], position[1])));
            }
        }

        return out;
    }

    public List<Geometry> getThresholdaggregatePixels() {
        List<Geometry> out = new ArrayList<Geometry>();
        GeometryFactory gf = new GeometryFactory();
        for (BoatConnectedPixelMap boat : listboatneighbours) {
            List<int[]> positions = boat.getThresholdaggregatePixels();
            for (int[] position : positions) {
                out.add(gf.createPoint(new Coordinate(position[0], position[1])));
            }
        }

        return out;
    }
    
    
    
    
   
}
