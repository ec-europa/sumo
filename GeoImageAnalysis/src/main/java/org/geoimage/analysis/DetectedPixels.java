/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.analysis;

import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geoimage.def.SarImageReader;
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

    private Map<String, Pixel> allDetectedPixels = new HashMap<String, Pixel>();
    private Map<String, Pixel> aggregatedPixels = new HashMap<String, Pixel>();
    private Map<Integer, List<int[]>> aggregatedBoats = new HashMap<Integer, List<int[]>>();
    private Boat[] boatArray = null;
    // size of the search window in meters
    private final int SEARCHWINDOW = 50;
    // filter size of boats in meters
    private final double FILTERminSIZE = 3;
    private final double FILTERmaxSIZE = 1000;
    private final double filterminSize = FILTERminSIZE;
    private final double filtermaxSize = FILTERmaxSIZE;
    private double pixsam;//range
    private double pixrec;//azimuth
    private int searchwindowWidth = 1;
    private int searchwindowHeight = 1;
    
    ArrayList<BoatConnectedPixelMap> listboatneighbours = new ArrayList<BoatConnectedPixelMap>();

    private Logger logger= LoggerFactory.getLogger(DetectedPixels.class);

    
    public class Pixel {

        public int x;
        public int y;
        public int value;
        public double tileAvg;
        public double tileStd;
        public double threshold;
        public int band;
        public int id;

        public Pixel(int x, int y, int value, double tileAvg, double tileStd, double threshold, int band) {
            this.x = x;
            this.y = y;
            this.value = value;
            this.tileAvg = tileAvg;
            this.tileStd = tileStd;
            this.threshold = threshold;
            this.band = band;
        }
    }
    
    
   
    /**
     * 
     * @param gir
     */
    public DetectedPixels(SarImageReader gir) {
        // get the image pixel size
        this.pixsam = gir.getRangeSpacing();
        this.pixrec = gir.getAzimuthSpacing();

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
        Pixel boatPixel = new Pixel(x, y, value, tileAvg, tileStd, threshold, band);
        StringBuilder point=new StringBuilder("").append(x).append(" ").append(y);
        allDetectedPixels.put(point.toString(), boatPixel);

    }

    /**
     *  add the detectedpixels
     * @param pixels
     */
    public void addAll(DetectedPixels pixels) {
    	Map<String, Pixel> BoatPixel = pixels.getDetectedPixels();
    	Collection<Pixel> boats = BoatPixel.values();
    	StringBuilder point=new StringBuilder();
        for (Pixel boat:boats) {
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
    	Map<String, Pixel> BoatPixel = pixels.getDetectedPixels();
    	Collection<Pixel> boats = BoatPixel.values();
    	StringBuilder position = new StringBuilder();
        for (Pixel boat:boats) {
        	position.setLength(0);
            // do not add if position already exists
            position = position.append(boat.x).append(" ").append(boat.y); 
            //if (allDetectedPixels.get(position.toString()) == null) {
            	//if the value is already in the map it will be replaced 
                allDetectedPixels.put(position.toString(), boat);
            //}
        }
    }

    public Boat[] getBoats() {
        return boatArray;
    }

    private Map<String, Pixel> getDetectedPixels() {
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
                    Pixel pixel = allDetectedPixels.get(key);
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
    public boolean checkNeighbours(List<int[]> pixels, int[] imagemap, int[][] imagedata, double[][] thresholdaggregate, int[] position, int neighboursdistance, int tilesize, Raster rastermask) {
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

    public void agglomerate() {
    	Pixel[] boats = allDetectedPixels.values().toArray(new Pixel[0]);
        int id = -1;
        for (Pixel boat:boats) {
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
     */
    public void computeBoatsAttributes() {
        Collection<List<int[]>> enumeration = aggregatedBoats.values();
        List <Boat> boatsTemp=new ArrayList<Boat>();
        
        for (List<int[]>agBoat:enumeration) {
            // start with estimating length, size and heading
            double[] boatvalues = Compute.fLenWidHead(agBoat, this.pixsam, this.pixrec);

            // look for maximum brightness point in cluster
            int[][] it = agBoat.toArray(new int[0][]);
        
            // get teh first boat in teh aggregate
            int[] pixel = null;
            if (it.length>0) {
                pixel = it[0];
                // store in the table the boat values and the estimated size, heading and bearing of the aggregate
                String id=new StringBuilder().append(pixel[0]).append(" ").append(pixel[1]).toString();
                Pixel pixelValue = aggregatedPixels.get(id);
                
                //boatvalues[0]=number of pixel boatvalues[1-2]= posx e posy  boatvalues[3-4-5]=length,width,heading
                Boat boatValue=new Boat(pixelValue.id, boatvalues[1], boatvalues[2], boatvalues[0],
                						boatvalues[3], boatvalues[4], boatvalues[5],
                						new int[]{pixelValue.value},pixelValue.tileAvg,pixelValue.tileStd,
                						pixelValue.threshold,pixelValue.band);
  
                // look for maximum value in agglomerate
                for (int i=1;i<it.length;i++) {
                    pixel = it[i];
                    String key=new StringBuilder().append(pixel[0]).append(" ").append(pixel[1]).toString();
                    Pixel pxBoat = aggregatedPixels.get(key);
                    if (pxBoat.value > boatValue.getMaxValue()[0]) {
                    	boatValue.setMaxValue(new int[]{pxBoat.value});
                    }
                }
                
                // check if agglomerated boat has a width or a length larger than filterSize
                if (boatValue.getLength() > this.filterminSize && boatValue.getWidth() > this.filterminSize) { //AG changed || to &&

                    // check if agglomerated boat has a width or a length lower than filterSize
                    if (boatValue.getLength() < this.filtermaxSize && boatValue.getWidth() < this.filtermaxSize) {
                    	boatsTemp.add(boatValue);
                    }
                }
            }
        }
        boatsTemp.clear();
        boatsTemp=sortBoats(boatsTemp);
        boatArray=boatsTemp.toArray(new Boat[0]);			
    }

   
    
    
    /**
     * AG inserted a test to filter boats by length
     * @param list
     */
    protected void computeBoatsAttributesAndStatistics(List<BoatConnectedPixelMap> list) {
    	 List <Boat> boatsTemp=new ArrayList<Boat>();
    	 
        // compute attributes and statistics values on boats
        for (BoatConnectedPixelMap boatPxMap : list) {
            boatPxMap.computeValues(pixsam,pixrec);
            if(boatPxMap.getBoatlength()>this.filterminSize && boatPxMap.getBoatlength()<this.filtermaxSize){
            	
            	//TODO: Adattare il codice a gestire tutte le bande 
            	//TODO: calcolare i significance qui!!
            	
            	Boat b=new Boat(boatPxMap.getId()						//id
            			,(int)boatPxMap.getBoatposition()[0]			//x
            			, (int)boatPxMap.getBoatposition()[1]			//y
            			,(int)boatPxMap.getBoatnumberofpixels()			//size
            			,(int)boatPxMap.getBoatlength()					//length
            			,(int)boatPxMap.getBoatwidth()					//width
            			,(int)boatPxMap.getBoatheading());				//heading

    			//,boat.getMaximumValue(),boat.getMeanValueBand(0),
    			//boat.getStdValue(),boat.getThresholdValueBand(0),0);

            	
    			List<Double> thresholdsTile=boatPxMap.getThresholdValue();
    			double max=boatPxMap.getMaximumValue();
    			for(Double t:thresholdsTile){
    				
    			}
            	
            	
            	boatsTemp.add(b);
            }
        }
        //boatsTemp.clear();
        boatsTemp=sortBoats(boatsTemp);
        boatArray=boatsTemp.toArray(new Boat[0]);
    }

    public Collection<Pixel> getAllDetectedPixelsValues() {
    	return allDetectedPixels.values();
    }
    
    /**
     * 
     * @return
     */
    public List<Geometry> getAllDetectedPixels() {
        List<Geometry> out = new ArrayList<Geometry>();
        GeometryFactory gf = new GeometryFactory();
        
        Pixel[] enumeration = allDetectedPixels.values().toArray(new Pixel[0]);
        for (Pixel pixel:enumeration) {
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
    
    /**
     * 
     * @param boats
     * @return
     */
    private List<Boat> sortBoats(List <Boat> boats){
    	ArrayList<Boat> sorted=new ArrayList<>();
    	// sort the boats array by positions to facilitate navigation
        HashMap<Integer, List<Boat>> hashtableBoat = new HashMap<Integer, List<Boat>>();
        // list for keys
        List<Integer> keys = new ArrayList<Integer>();

        // sort the list by position
        for (Boat boat : boats) {
        	Integer posyKey=new Integer((int) (boat.getPosy() / 512));

        	// if entry does not exist in the hashtable create it
            if (hashtableBoat.get(posyKey) == null) {
                hashtableBoat.put(posyKey, new ArrayList<Boat>());
                keys.add((int) posyKey);
            }
            // sort boat positions by stripes of 512
            hashtableBoat.get(posyKey).add(boat);
        }

        // for each stripe sort the boats by columns within each stripe
        Collections.sort(keys);
        for (Integer key : keys) {
            // get the boats in the stripe
            List<Boat> boatsinStripe = hashtableBoat.get(key);
            if (boatsinStripe != null) {
                for (int i = 1; i < boatsinStripe.size(); i++) {
                    double positionX = boatsinStripe.get(i).getPosx();
                    for (int j = 0; j < i; j++) {
                        if (positionX < boatsinStripe.get(j).getPosy()) {
                            // insert element at the right position
                            boatsinStripe.add(j, boatsinStripe.get(i));
                            // remove element from its previous position increased by one position
                            boatsinStripe.remove(i + 1);
                            break;
                        }
                    }
                }
            }

            int id = 0;
            
            // add the sorted boats to the table
            for (Boat boat : boatsinStripe) {
                // update the ID
                boat.setId(id++);
                sorted.add(boat);
            }
        }
        return sorted;
    }
    
}
