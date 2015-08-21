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
    private SarImageReader gir = null;
    
    ArrayList<BoatPixel> listboatneighbours = new ArrayList<BoatPixel>();

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
    
    
    public class Boat{
    	public final static int AMBIGUITY_TYPE_AZIMUTH=1;
    	public final static int AMBIGUITY_TYPE_ARTEFACTS=2;
    	
    	private double posx;
    	private double posy;
    	private double size;
    	private double length;
    	private double width;
    	private double heading;
    	private double value;
    	private double tileAvg;
    	private List<Double> tileStd;
    	private List<Double> threshold;
    	private double band;
    	private double id;
    	private boolean isAmbiguity=false;
    	private int ambiguityType=0;
    	

		public Boat(double id,double x,double y,double size,double length,double width,double heading,
				double value,double tileAvg,List<Double> tileStd,List<Double> threshold, double band){
    		this.id=id;
			this.posx=x;
    		this.posy=y;
    		this.size=size;
    		this.length=length;
    		this.width=width;
    		this.heading=heading;
    		this.value = value;
            this.tileAvg = tileAvg;
            this.tileStd = tileStd;
            this.threshold = threshold;
            this.band = band;
    	}
		
		public Boat(double id,double x,double y,double size,double length,double width,double heading,
				double value,double tileAvg,double tileStd,double threshold, int band){
    		this.id=id;
			this.posx=x;
    		this.posy=y;
    		this.size=size;
    		this.length=length;
    		this.width=width;
    		this.heading=heading;
    		this.value = value;
            this.tileAvg = tileAvg;
            this.tileStd = new ArrayList<Double>();
            this.tileStd.add(band,tileStd);
            this.threshold = new ArrayList<Double>();
            this.tileStd.add(band,threshold);
            this.band = band;
    	}
		

		public double getId() {
			return id;
		}



		public void setId(double id) {
			this.id = id;
		}



		public double getValue() {
			return value;
		}



		public void setValue(double value) {
			this.value = value;
		}



		public double getTileAvg() {
			return tileAvg;
		}



		public void setTileAvg(double tileAvg) {
			this.tileAvg = tileAvg;
		}



		public List<Double> getTileStd() {
			return tileStd;
		}



		public void setTileStd(List<Double> tileStd) {
			this.tileStd = tileStd;
		}



		public List<Double> getThreshold() {
			return threshold;
		}



		public void setThreshold(List<Double> threshold) {
			this.threshold = threshold;
		}



		public double getBand() {
			return band;
		}



		public void setBand(double band) {
			this.band = band;
		}

		public int getAmbiguityType() {
			return ambiguityType;
		}

		public void setAmbiguityType(int ambiguityType) {
			this.ambiguityType = ambiguityType;
		}

		public double getPosx() {
			return posx;
		}

		public void setPosx(double posx) {
			this.posx = posx;
		}

		public double getPosy() {
			return posy;
		}

		public void setPosy(double posy) {
			this.posy = posy;
		}

		public double getSize() {
			return size;
		}

		public void setSize(double size) {
			this.size = size;
		}

		public double getLength() {
			return length;
		}

		public void setLength(double length) {
			this.length = length;
		}

		public double getWidth() {
			return width;
		}

		public void setWidth(double width) {
			this.width = width;
		}

		public double getHeading() {
			return heading;
		}

		public void setHeading(double heading) {
			this.heading = heading;
		}
		
		public boolean isAmbiguity() {
			return isAmbiguity;
		}



		public void setAmbiguity(boolean isAmbiguity) {
			this.isAmbiguity = isAmbiguity;
		}


    }
    

    public DetectedPixels(SarImageReader gir) {
        this.gir = gir;
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
        Pixel boat = new Pixel(x, y, value, tileAvg, tileStd, threshold, band);
        StringBuilder point=new StringBuilder("").append(x).append(" ").append(y);
        allDetectedPixels.put(point.toString(), boat);

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

    public DetectedPixels.Boat[] getBoats() {
        return boatArray;
    }

    private Map<String, Pixel> getDetectedPixels() {
        return allDetectedPixels;
    }

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
                    Pixel boat = allDetectedPixels.get(key);
                    if (boat != null && !(i == xx && j == yy)) {
                        if (aggregatedPixels.get(key) == null) {
                            boat.id=idd;
                            List<int[]> agBoat = aggregatedBoats.get(idd);
                            if (agBoat == null) {
                                agBoat = new ArrayList<int[]>();
                            }
                            agBoat.add(new int[]{i, j});
                            aggregatedBoats.put(idd, agBoat);
                            aggregatedPixels.put(key, boat);
                            list.add(new int[]{i, j, idd});
                        }
                    }
                }
            }
        }
    }

    

    /**
     *  aggregate using the neighbours within tilesize
     * @param neighboursdistance
     * @param tilesize
     * @param removelandconnectedpixels
     * @param bands
     * @param mask
     * @param kdist
     * @throws IOException
     *
    private void aggregate(int neighboursdistance, int tilesize, boolean removelandconnectedpixels, int[] bands, IMask mask, KDistributionEstimation kdist)throws IOException {
        int id = 0;
        // scan through list of detected pixels
        Pixel pixels[]=allDetectedPixels.values().toArray(new Pixel[0]);
        int count=0;
        
        //loop on all detected pixel
        for (Pixel detectedPix: pixels) {
        	count++;
        	
            int xx = detectedPix.x;
            int yy = detectedPix.y;
            
            if((count % 100)==0)
            	logger.info(new StringBuilder().append("Aggregating pixel Num:").append(count).append("  x:").append(xx).append("   y:").append(yy).toString() );
            
            // check pixels is not aggregated
            boolean checked = false;
            for (BoatPixel boatpixel : listboatneighbours) {
                if (boatpixel.containsPixel(xx, yy)) {
                    checked = true;
                    break;
                }
            }

            if (checked) {
                continue;
            }

            // get image data in tile
            int cornerx = Math.min(Math.max(0, xx - tilesize / 2), gir.getWidth() - tilesize);
            int cornery = Math.min(Math.max(0, yy - tilesize / 2), gir.getHeight() - tilesize);
            
            // boat relative coordinates
            int boatx = xx - cornerx;
            int boaty = yy - cornery;
            int numberbands = bands.length;

            //read the area for the bands
            int[][] data = new int[numberbands][];
            try{
	            for (int bandcounter = 0; bandcounter < numberbands; bandcounter++) {
	            	data[bandcounter] = gir.read(cornerx, cornery, tilesize, tilesize,bands[bandcounter]);
	            }	
            }catch(IOException e){
        		logger.error(e.getMessage());
        		throw e;
        	}
            
            // calculate thresholds
            double[][] statistics = AnalysisUtil.calculateImagemapStatistics(cornerx, cornery, tilesize, tilesize, bands, data, kdist);
            
            double[][] thresholdvalues = new double[numberbands][2];
            int maxvalue = 0;
            boolean pixelabove = false;
            
            for (int bandcounter = 0; bandcounter < numberbands; bandcounter++) {
                // average the tile mean values
                double mean = (statistics[bandcounter][1] + statistics[bandcounter][2] + statistics[bandcounter][3] + statistics[bandcounter][4]) / 4;
                

                //TODO CHEK if is true....this is the thresholds for the agglomeration. Change in the output with the trheshold calculated by the anlysis
                // aggregate value is mean + 3 * std
                thresholdvalues[bandcounter][0] = mean + 3 * mean * statistics[bandcounter][0];
                // clip value is mean + 5 * std
                thresholdvalues[bandcounter][1] = mean + 5 * mean * statistics[bandcounter][0];
                // check the pixel is still above the new threshold
                int value = data[bandcounter][boatx + boaty * tilesize];
                if (value > thresholdvalues[bandcounter][1]) {
                    pixelabove = true;
                }
                // find maximum value amongst bands
                if (value > maxvalue) {
                    maxvalue = data[bandcounter][boatx + boaty * tilesize];
                }
            }
            
            // add pixel only if above new threshold
            if (pixelabove) {
            	// check if there is land in tile
                Raster rastermask = null;
                if (mask != null) {
                    // check if land in tile
                    if (mask.intersects(cornerx, cornery, tilesize, tilesize)) {
                        // create raster mask
                        rastermask = (mask.rasterize(cornerx, cornery, tilesize, tilesize, -cornerx, -cornery, 1.0)).getData();
                    }
                }
            	
                
                // add pixel to the list
                BoatPixel boatpixel = new BoatPixel(xx, yy, id++, data[0][boatx + boaty * tilesize]);
                for(int i=0;i<numberbands;i++){
                	Raster rastermask = (mask[0].rasterize(xLeftTile, yTopTile, sizeX+dx, sizeY+dy, -xLeftTile, -yTopTile, 1.0)).getData();
                	kdist.setImageData(sizeX, sizeY, sizeTileX, sizeTileY, row, col, band);
                	kdist.estimate(rastermask, data);
	            	//TODO  for each band,calculate here the "trheshold tile " to put in the xml for the new tile 
                	double threshWindowsVals[]=calcThreshWindowVals(thresholdAnalysisParams, thresh);
                	boatpixel.putMeanValue(i,(statistics[i][1] + statistics[i][2] + statistics[i][3] + statistics[i][4]) / 4);
                	boatpixel.putThresholdValue(i,detectedPix.threshold);
                }	
                listboatneighbours.add(boatpixel);
                
                // start list of aggregated pixels
                List<int[]> boataggregatedpixels = new ArrayList<int[]>();
                int[] imagemap = new int[tilesize * tilesize];
                for (int i = 0; i < tilesize * tilesize; i++) {
                    imagemap[i] = 0;
                }
                
                boolean result = checkNeighbours(boataggregatedpixels, imagemap, data, thresholdvalues, new int[]{boatx, boaty}, neighboursdistance, tilesize, rastermask);
                
                // set flag for touching land
                boatpixel.setLandMask(result);
                
                // shift pixels by cornerx and cornery and store in boat list
                //Collection<int[]> aggregatedpixels = boataggregatedpixels.values();
                for (int[] pixel : boataggregatedpixels) {
                    boatpixel.addConnectedPixel(pixel[0] + cornerx, pixel[1] + cornery, pixel[2], pixel[3] == 1 ? true : false);
                }
            } else {
            }
            
        }

        // if remove connected to land pixels flag
        if (removelandconnectedpixels) {
            // remove all boats connecting to land
        	List<BoatPixel> toRemove=new ArrayList<BoatPixel>();
            for (int i=0;i<listboatneighbours.size();i++) {
            	BoatPixel boat = listboatneighbours.get(i);
                if (boat.touchesLand()) {
                    toRemove.add(boat);
                }
            }
            listboatneighbours.removeAll(toRemove);
        }
        // generate statistics and values for boats
        computeBoatsAttributesAndStatistics(listboatneighbours);
        
    }*/


    

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
                            //if(rastermask != null) System.out.println(i + " " + j + "mask value is" + rastermask.getSample(i, j, 0));
                            //else System.out.println(i + " " + j);
                            // check if pixel is above threshold in one of the bands
                            boolean aggregated = false;
                            boolean clipped = false;
                            int value = 0;
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
                Boat boatValue=new Boat(pixelValue.id, boatvalues[1], boatvalues[2], boatvalues[0], boatvalues[3], boatvalues[4], boatvalues[5],
                		pixelValue.value,pixelValue.tileAvg,pixelValue.tileStd,pixelValue.threshold,pixelValue.band);
  
                // look for maximum value in agglomerate
                for (int i=1;i<it.length;i++) {
                    pixel = it[i];
                    String key=new StringBuilder().append(pixel[0]).append(" ").append(pixel[1]).toString();
                    Pixel boat = aggregatedPixels.get(key);
                    if (boat.value > boatValue.getValue()) {
                    	boatValue.setValue(boat.value);
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

        // create new array
        Map<Integer, List<Boat>> hashtableBoat = new HashMap<Integer, List<Boat>>();
        // list for keys
        List<Integer> keys = new ArrayList<Integer>();
        // sort the list by position
        for (Boat boat : boatsTemp) {
            // if entry does not exist in the hashtable create it
            if (hashtableBoat.get(new Integer((int) (boat.posy / 512))) == null) {
                hashtableBoat.put(new Integer((int) (boat.posy / 512)), new ArrayList<Boat>());
                keys.add((int) (boat.posy / 512));
            }
            // sort boat positions by stripes of 512
            hashtableBoat.get(new Integer((int) (boat.posy / 512))).add(boat);
        }

        // clear the boatArray
        boatsTemp.clear();

        // for each stripe sort the boats by columns within each stripe
        Collections.sort(keys);
        for (Integer key : keys) {
            // get the boats in the stripe
            List<Boat> boatsinStripe = hashtableBoat.get(key);
            if (boatsinStripe != null) {
                for (int i = 1; i < boatsinStripe.size(); i++) {
                    double positionX = boatsinStripe.get(i).posx;
                    for (int j = 0; j < i; j++) {
                        if (positionX < boatsinStripe.get(j).posx) {
                            // insert element at the right position
                            boatsinStripe.add(j, boatsinStripe.get(i));
                            // remove element from its previous position increased by one position
                            boatsinStripe.remove(i + 1);
                            break;
                        }
                    }
                }
            }
            // add the sorted boats to the table
            for (Boat boat : boatsinStripe) {
            	boatsTemp.add(boat);
            }
        }
        boatArray=boatsTemp.toArray(new Boat[0]);			//new double[boatsTemp.size()][];
    }

    /**
     * AG inserted a test to filter boats by length
     * @param list
     */
    protected void computeBoatsAttributesAndStatistics(List<BoatPixel> list) {
    	 List <Boat> boatsTemp=new ArrayList<Boat>();
    	 
        // compute attributes and statistics values on boats
        for (BoatPixel boat : list) {
            boat.computeValues(pixsam,pixrec);
            if(boat.getBoatlength()>this.filterminSize && boat.getBoatlength()<this.filtermaxSize){
            	
            	//TODO: Adattare il codice a gestire tutte le bande 
            	//TODO: calcolare i significance qui!!
            	Boat b=new Boat(boat.getId(),(int)boat.getBoatposition()[0], (int)boat.getBoatposition()[1],(int)boat.getBoatnumberofpixels(),
            			(int)boat.getBoatlength(),(int)boat.getBoatwidth(),(int)boat.getBoatheading(),boat.getMaximumValue(),boat.getMeanValueBand(0),
            			boat.getStdValue(),boat.getThresholdValueBand(0),0);
            	
            	boatsTemp.add(b);
            }
        }

        // sort the boats array by positions to facilitate navigation
        HashMap<Integer, List<Boat>> hashtableBoat = new HashMap<Integer, List<Boat>>();
        // list for keys
        List<Integer> keys = new ArrayList<Integer>();
        // sort the list by position
        for (Boat boat : boatsTemp) {
            // if entry does not exist in the hashtable create it
            if (hashtableBoat.get(new Integer((int) (boat.getPosy() / 512))) == null) {
                hashtableBoat.put(new Integer((int) (boat.getPosy() / 512)), new ArrayList<Boat>());
                keys.add((int) (boat.getPosy() / 512));
            }
            // sort boat positions by stripes of 512
            hashtableBoat.get(new Integer((int) (boat.getPosy() / 512))).add(boat);
        }

        // clear the boatArray
        boatsTemp.clear();

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
                boatsTemp.add(boat);
            }
        }
        boatArray=boatsTemp.toArray(new Boat[0]);
    }

    /**
     * 
     * @return
     */
    public double[] getValues() {
        double[] values = new double[2 * boatArray.length];
        int i = 0;
        for (Boat boat : boatArray) {
            values[i++] = boat.getPosx();//val[1];
            values[i++] = boat.getPosy();//val[2];
        }
        return values;
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
        for (BoatPixel boat : listboatneighbours) {
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
        for (BoatPixel boat : listboatneighbours) {
            List<int[]> positions = boat.getThresholdaggregatePixels();
            for (int[] position : positions) {
                out.add(gf.createPoint(new Coordinate(position[0], position[1])));
            }
        }

        return out;
    }
};
