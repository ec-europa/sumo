/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.analysis;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geoimage.def.SarImageReader;
import org.geoimage.utils.IMask;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 *
 * @author thoorfr
 */
public class DetectedPixels {

    private Map<String, DetectedPixel> allDetectedPixels = new HashMap<String, DetectedPixel>();
    private Map<String, DetectedPixel> aggregatedPixels = new HashMap<String, DetectedPixel>();
    private Map<Integer, List<int[]>> aggregatedBoats = new HashMap<Integer, List<int[]>>();
    private List<double[]> boatArray = new ArrayList<double[]>();
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

    
    ArrayList<boatPixels> listboatneighbours = new ArrayList<boatPixels>();

    public DetectedPixels(SarImageReader gir) {
        this.gir = gir;
        // get the image pixel size
        this.pixsam = new Double(gir.getMetadata(SarImageReader.RANGE_SPACING).toString());
        this.pixrec = new Double(gir.getMetadata(SarImageReader.AZIMUTH_SPACING).toString());

        /*double[] pixelsize = gir.getGeoTransform().getPixelSize();
        this.pixsam = pixelsize[0];
        this.pixrec = pixelsize[1];*/

        // calculate search window size using pixel size
        if (SEARCHWINDOW > pixsam) {
            this.searchwindowWidth = (int) (SEARCHWINDOW / pixsam);
        }
        if (SEARCHWINDOW > pixrec) {
            this.searchwindowHeight = (int) (SEARCHWINDOW / pixrec);
        }
        // define the filtering size for boats
       // this.filterminSize = FILTERminSIZE;
       // this.filtermaxSize = FILTERmaxSIZE;
    }

    private class DetectedPixel {

        public int x;
        public int y;
        public int value;
        public double tileAvg;
        public double tileStd;
        public double threshold;
        public int band;
        public int id;

        public DetectedPixel(int x, int y, int value, double tileAvg, double tileStd, double threshold, int band) {
            this.x = x;
            this.y = y;
            this.value = value;
            this.tileAvg = tileAvg;
            this.tileStd = tileStd;
            this.threshold = threshold;
            this.band = band;
        }
    }

    public void add(int x, int y, int value, double tileAvg, double tileStd, double threshold, int band) {
        DetectedPixel boat = new DetectedPixel(x, y, value, tileAvg, tileStd, threshold, band);
        allDetectedPixels.put(x + " " + y, boat);

    }

    // add the detectedpixels
    public void add(DetectedPixels pixels) {
    	Map<String, DetectedPixel> boatpixels = pixels.getDetectedPixels();
    	Collection<DetectedPixel> boats = boatpixels.values();
        for (DetectedPixel boat:boats) {
            allDetectedPixels.put(boat.x + " " + boat.y, boat);
        }
    }

    // marge the detectedpixels
    public void merge(DetectedPixels pixels) {
    	Map<String, DetectedPixel> boatpixels = pixels.getDetectedPixels();
    	Collection<DetectedPixel> boats = boatpixels.values();
    	StringBuilder position = new StringBuilder();
        for (DetectedPixel boat:boats) {
        	position.setLength(0);
            // do not add if position already exists
            position = position.append(boat.x).append(" ").append(boat.y); 
            if (allDetectedPixels.get(position.toString()) == null) {
                allDetectedPixels.put(position.toString(), boat);
            }
        }
    }

    public List<double[]> getBoats() {
        return boatArray;
    }

    private Map<String, DetectedPixel> getDetectedPixels() {
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
                    DetectedPixel boat = allDetectedPixels.get(i + " " + j);
                    if (boat != null && !(i == xx && j == yy)) {
                        if (aggregatedPixels.get(i + " " + j) == null) {
                            boat.id=idd;
                            List<int[]> agBoat = aggregatedBoats.get(idd);
                            if (agBoat == null) {
                                agBoat = new ArrayList<int[]>();
                            }
                            agBoat.add(new int[]{i, j});
                            aggregatedBoats.put(idd, agBoat);
                            aggregatedPixels.put(i + " " + j, boat);
                            list.add(new int[]{i, j, idd});
                        }
                    }
                }
            }
        }
    }

    private class boatPixels {

        private Map<String, int[]> connectedpixels = new HashMap<String, int[]>();
        private double[][] thresholdvalues;
        private double boatnumberofpixels = 0.0;
        private double[] boatposition;
        private double boatwidth = 0.0;
        private double boatlength = 0.0;
        private double boatheading = 0.0;
        private int id = 0;
        private double boatmaximumvalue = 0.0;
        private boolean touchlandmask = false;
        private double thresholdvalue = 0.0;
        private double meanvalue = 0.0;
        private double stdvalue = 0.0;

        public boatPixels(int x, int y, int id, int value, double[][] thresholdvalues) {
            // add initial pixel, clipped value is always set to 1
            connectedpixels.put(x + " " + y, new int[]{x, y, value, 1});
            this.thresholdvalues = thresholdvalues;
            this.boatposition = new double[]{x, y};
            this.boatmaximumvalue = value;
            this.id = id;
        }

        public void addConnectedPixel(int x, int y, int value, boolean clipped) {
            connectedpixels.put(x + " " + y, new int[]{x, y, value, clipped ? 1 : 0});
        }

        public boolean containsPixel(int x, int y) {
            return connectedpixels.get(x + " " + y) != null;
        }

        public void computeValues() {
            // clip all values below thresholdclip
            List<int[]> clust = new ArrayList<int[]>();
            Collection<int[]> pixels = connectedpixels.values();
            for (int[] pixel: pixels) {
                if (pixel[3] == 1) {
                    clust.add(pixel);
                }
                // look for maximum value in pixels
                if (pixel[2] > boatmaximumvalue) {
                    boatmaximumvalue = pixel[2];
                }
            }
            // calculate length and width for cluster
            double[] result = LenWidHedd(clust, new double[]{pixsam, pixrec});
            boatnumberofpixels = result[0];
            boatposition[0] = result[1];
            boatposition[1] = result[2];
            boatlength = result[3];
            boatwidth = result[4];
            boatheading = result[5];
        }

        public int getId() {
            return id;
        }

        public double getBoatheading() {
            return boatheading;
        }

        public double getBoatlength() {
            return boatlength;
        }

        public double getBoatnumberofpixels() {
            return boatnumberofpixels;
        }

        public double[] getBoatposition() {
            return boatposition;
        }

        public double getBoatwidth() {
            return boatwidth;
        }

        private double getMaximumValue() {
            return boatmaximumvalue;
        }

        private void setMeanValue(double meanvalue) {
            this.meanvalue = meanvalue;
        }

        private double getMeanValue() {
            return meanvalue;
        }

        private void setStdValue(double stdvalue) {
            this.stdvalue = stdvalue;
        }

        private double getStdValue() {
            return stdvalue;
        }

        private void setThresholdValue(double thresholdvalue) {
            this.thresholdvalue = thresholdvalue;
        }

        private double getThresholdValue() {
            return thresholdvalue;
        }

        private List<int[]> getThresholdclipPixels() {
            // clip all values below thresholdclip
            List<int[]> clust = new ArrayList<int[]>();
            Collection<int[]> pixels = connectedpixels.values();
            for (int[] pixel:pixels) {
                if (pixel[3] == 1) {
                	clust.add(pixel);
                }
            }

            return clust;
        }

        private List<int[]> getThresholdaggregatePixels() {
            // clip all values below thresholdclip
            List<int[]> clust = new ArrayList<int[]>();
            Collection<int[]> pixels = connectedpixels.values();
            for (int[] pixel:pixels) {
                clust.add(pixel);
            }

            return clust;
        }

        private void setLandMask(boolean touchlandmask) {
            this.touchlandmask = touchlandmask;
        }

        private boolean touchesLand() {
            return touchlandmask;
        }
    }

    // aggregate using the neighbours within tilesize
    private void aggregate(int neighboursdistance, int tilesize, boolean removelandconnectedpixels, int[] bands, IMask mask, KDistributionEstimation kdist) {
        int id = 0;
        // scan through list of detected pixels
        Collection<DetectedPixel>pixels=allDetectedPixels.values();
        for (DetectedPixel p: pixels) {
            int xx = p.x;
            int yy = p.y;
            // check pixels is not aggregated
            boolean checked = false;
            for (boatPixels boatpixel : listboatneighbours) {
                if (boatpixel.containsPixel(xx, yy)) {
                    checked = true;
                    break;
                }
            }

            if (checked) {
                continue;
            }

            //System.out.println("New Pixel " + xx + " " + yy);
            // get image data in tile
            int cornerx = Math.min(Math.max(0, xx - tilesize / 2), gir.getWidth() - tilesize);
            int cornery = Math.min(Math.max(0, yy - tilesize / 2), gir.getHeight() - tilesize);
            // check if there is land in tile
            Raster rastermask = null;
            if (mask != null) {
                // check if land in tile
                if (mask.intersects(cornerx, cornery, tilesize, tilesize)) {
                    // create raster mask
                    rastermask = (mask.rasterize(new Rectangle(cornerx, cornery, tilesize, tilesize), -cornerx, -cornery, 1.0)).getData();
                }
            }
            // boat relative coordinates
            int boatx = xx - cornerx;
            int boaty = yy - cornery;
            int numberbands = bands.length;
            // calculate thresholds
            double[][] statistics = calculateImagemapStatistics(new Rectangle(cornerx, cornery, tilesize, tilesize), bands, rastermask, kdist);
            double[][] thresholdvalues = new double[numberbands][2];
            int[][] data = new int[numberbands][];
            int maxvalue = 0;
            boolean pixelabove = false;
            for (int bandcounter = 0; bandcounter < numberbands; bandcounter++) {
                int band = bands[bandcounter];
                gir.setBand(band);
                data[bandcounter] = gir.readTile(cornerx, cornery, tilesize, tilesize);
                // average the tile mean values
                double mean = (statistics[bandcounter][1] + statistics[bandcounter][2] + statistics[bandcounter][3] + statistics[bandcounter][4]) / 4;
                //System.out.println("mean = " + mean);
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
                // add pixel to the list
                boatPixels boatpixel = new boatPixels(xx, yy, id++, data[0][boatx + boaty * tilesize], thresholdvalues);
                boatpixel.setMeanValue((statistics[0][1] + statistics[0][2] + statistics[0][3] + statistics[0][4]) / 4);
                boatpixel.setStdValue(statistics[0][0]);
                boatpixel.setThresholdValue(p.threshold);
                listboatneighbours.add(boatpixel);
                // start list of aggregated pixels
                Map<String, int[]> boataggregatedpixels = new HashMap<String, int[]>();
                int[] imagemap = new int[tilesize * tilesize];
                for (int i = 0; i < tilesize * tilesize; i++) {
                    imagemap[i] = 0;
                }
                boolean result = checkNeighbours(boataggregatedpixels, imagemap, data, thresholdvalues, new int[]{boatx, boaty}, neighboursdistance, tilesize, rastermask);
                // set flag for touching land
                boatpixel.setLandMask(result);
                // shift pixels by cornerx and cornery and store in boat list
                Collection<int[]> aggregatedpixels = boataggregatedpixels.values();
                for (int[] pixel : aggregatedpixels) {
                    boatpixel.addConnectedPixel(pixel[0] + cornerx, pixel[1] + cornery, pixel[2], pixel[3] == 1 ? true : false);
                }
            } else {
            }
        }

        // if remove connected to land pixels flag
        if (removelandconnectedpixels) {
            // create copy of list of boats
            List<boatPixels> listofboats = (List<boatPixels>) listboatneighbours.clone();
            listboatneighbours.clear();
            // remove all boats connecting to land
            for (boatPixels boat : listofboats) {
                if (!boat.touchesLand()) {
                    listboatneighbours.add(boat);
                }
            }
        }

        // generate statistics and values for boats
        computeBoatsAttributesAndStatistics(listboatneighbours);
    }

    // calculate new statistics using tile centered around pixel
    public double[][] calculateImagemapStatistics(Rectangle imagerectangle, int[] bands, Raster mask, KDistributionEstimation kdist) {
        int numberofbands = bands.length;
        double[][] imagestat = new double[numberofbands][5];
        for (int i = 0; i < numberofbands; i++) {
            int band = bands[i];
            gir.setBand(band);
            kdist.setImageData(gir, imagerectangle.x, imagerectangle.y, 1, 1, imagerectangle.width, imagerectangle.height);
            kdist.estimate(null);
            double[][][] thresh = kdist.getDetectThresh();
            imagestat[i][0] = thresh[0][0][0];
            imagestat[i][1] = thresh[0][0][1] / thresh[0][0][5];
            imagestat[i][2] = thresh[0][0][2] / thresh[0][0][5];
            imagestat[i][3] = thresh[0][0][3] / thresh[0][0][5];
            imagestat[i][4] = thresh[0][0][4] / thresh[0][0][5];
        }

        return imagestat;
    }

    public boolean checkNeighbours(Map<String, int[]> pixels, int[] imagemap, int[][] imagedata, double[][] thresholdaggregate, int[] position, int neighboursdistance, int tilesize, Raster rastermask) {
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
                                String positionstring = i + " " + j;
                                // add pixel to the list of pixels
                                pixels.put(positionstring, new int[]{i, j, value, clipped ? 1 : 0});
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
        
    	Collection<DetectedPixel> boats = allDetectedPixels.values();
        int id = -1;
        for (DetectedPixel boat:boats) {
            List<int[]> agBoat = new ArrayList<int[]>();
            int x = boat.x;
            int y = boat.y;
            String position = x + " " + y;
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

    public void agglomerateNeighbours(double distance, int tilesize, boolean removelandconnectedpixels, int[] bands, IMask mask, KDistributionEstimation kdist) {
        aggregate((int) distance, tilesize, removelandconnectedpixels, bands, mask, kdist);
    }

    public void computeBoatsAttributes() {
        Collection<List<int[]>> enumeration = aggregatedBoats.values();
        for (List<int[]>agBoat:enumeration) {
            // start with estimating length, size and heading
            double[] boatvalues = fLenWidHead(agBoat, this.pixsam, this.pixrec);

            // look for maximum brightness point in cluster
            Iterator<int[]> it = agBoat.iterator();
            // get teh first boat in teh aggregate
            int[] pixel = null;
            if (it.hasNext()) {
                pixel = it.next();
                // store in the table the boat values and the estimated size, heading and bearing of the aggregate
                DetectedPixel pixelValue = aggregatedPixels.get(pixel[0] + " " + pixel[1]);
                double[] boatValue = new double[11];
                boatValue[0] = pixelValue.id;
                boatValue[3] = pixelValue.value;
                boatValue[4] = pixelValue.tileAvg;
                boatValue[5] = pixelValue.tileStd;
                boatValue[6] = pixelValue.threshold;
                // set the number of pixels
                boatValue[7] = boatvalues[0];
                // set the position
                boatValue[1] = boatvalues[1];
                boatValue[2] = boatvalues[2];
                // set the estimated length, width and heading
                boatValue[8] = boatvalues[3];
                boatValue[9] = boatvalues[4];
                boatValue[10] = boatvalues[5];

                // look for maximum value in agglomerate
                while (it.hasNext()) {
                    pixel = it.next();
                    DetectedPixel boat = aggregatedPixels.get(pixel[0] + " " + pixel[1]);
                    if (boat.value > boatValue[3]) {
                        boatValue[3] = boat.value;
                    }
                }
                // check if agglomerated boat has a width or a length larger than filterSize
                if (boatValue[8] > this.filterminSize && boatValue[9] > this.filterminSize) { //AG changed || to &&

                    // check if agglomerated boat has a width or a length lower than filterSize
                    if (boatValue[8] < this.filtermaxSize && boatValue[9] < this.filtermaxSize) {
                        boatArray.add(boatValue);
                    }
                }
            }
        }

        // create new array
        Map<Integer, List<double[]>> hashtableBoat = new HashMap<Integer, List<double[]>>();
        // list for keys
        List<Integer> keys = new ArrayList<Integer>();
        // sort the list by position
        for (double[] boat : boatArray) {
            // if entry does not exist in the hashtable create it
            if (hashtableBoat.get(new Integer((int) (boat[2] / 512))) == null) {
                hashtableBoat.put(new Integer((int) (boat[2] / 512)), new ArrayList<double[]>());
                keys.add((int) (boat[2] / 512));
            }
            // sort boat positions by stripes of 512
            hashtableBoat.get(new Integer((int) (boat[2] / 512))).add(boat);
        }

        // clear the boatArray
        boatArray.clear();

        // for each stripe sort the boats by columns within each stripe
        Collections.sort(keys);
        for (Integer key : keys) {
            // get the boats in the stripe
            List<double[]> boatsinStripe = hashtableBoat.get(key);
            if (boatsinStripe != null) {
                for (int i = 1; i < boatsinStripe.size(); i++) {
                    double positionX = boatsinStripe.get(i)[1];
                    for (int j = 0; j < i; j++) {
                        if (positionX < boatsinStripe.get(j)[1]) {
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
            for (double[] boat : boatsinStripe) {
                boatArray.add(boat);
            }
        }

    }

    //AG inserted a test to filter boats by length
    private void computeBoatsAttributesAndStatistics(List<boatPixels> list) {

        boatArray.clear();
        // compute attributes and statistics values on boats
        for (boatPixels boat : list) {
            boat.computeValues();
            if(boat.getBoatlength()>this.filterminSize && boat.getBoatlength()<this.filtermaxSize){
            double[] boatvalues = new double[]{boat.getId(), boat.getBoatposition()[0], boat.getBoatposition()[1], boat.getMaximumValue(), boat.getMeanValue(), boat.getStdValue(), boat.getThresholdValue(), boat.getBoatnumberofpixels(), boat.getBoatlength(), boat.getBoatwidth(), boat.getBoatheading()};
            boatArray.add(boatvalues);
            }
        }

        // sort the boats array by positions to facilitate navigation
        // create new array
        Hashtable<Integer, List<double[]>> hashtableBoat = new Hashtable<Integer, List<double[]>>();
        // list for keys
        List<Integer> keys = new ArrayList<Integer>();
        // sort the list by position
        for (double[] boat : boatArray) {
            // if entry does not exist in the hashtable create it
            if (hashtableBoat.get(new Integer((int) (boat[2] / 512))) == null) {
                hashtableBoat.put(new Integer((int) (boat[2] / 512)), new ArrayList<double[]>());
                keys.add((int) (boat[2] / 512));
            }
            // sort boat positions by stripes of 512
            hashtableBoat.get(new Integer((int) (boat[2] / 512))).add(boat);
        }

        // clear the boatArray
        boatArray.clear();

        // for each stripe sort the boats by columns within each stripe
        Collections.sort(keys);
        for (Integer key : keys) {
            // get the boats in the stripe
            List<double[]> boatsinStripe = hashtableBoat.get(key);
            if (boatsinStripe != null) {
                for (int i = 1; i < boatsinStripe.size(); i++) {
                    double positionX = boatsinStripe.get(i)[1];
                    for (int j = 0; j < i; j++) {
                        if (positionX < boatsinStripe.get(j)[1]) {
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
            for (double[] boat : boatsinStripe) {
                // update the ID
                boat[0] = id++;
                boatArray.add(boat);
            }
        }

    }

    //
    // Compute Length (m), Width (m) and Heading (deg) of target cluster
    //   Clust     Cluster obtained e.g. by ClustGrow, i.e. Clust( 1, :) are
    //             sample locations of cluster and Clust( 2, :) record locations
    //   PixszSam  Sample pixel size in m
    //   PixszRec  Record pixel size in m
    //   Tlen      Length of cluster in m
    //   Twid      Width of custer in m
    //   Thed      Heading (orientation) of cluster long axis
    // Heading is positive (anti-clockwise) wrt sample axis, with the record axis
    // at +90 deg wrt sample axis, 180 deg ambigue, between [-90..+90].
    // Calculated by least-squares fit of a line through the pixels in the cluster.
    // (Each pixel is treated as (x,y) pair, assuming errors in both x and y.)
    // Based on CFAR08.m
    //
    // See also LenWidHed.m (script)
    // (c) H. Greidanus 2004
    public double[] fLenWidHead(List<int[]> Clust, double PixszSam, double PixszRec) {
        double Tlen = 0.0;
        double Twid = 0.0;
        double Thed = 0.0;
        double aa = 0.0;
        // Make sums of pixel coordinates
        int icpt = Clust.size();
        double tt[] = matlabSumRow(Clust, 1);
        double Sjs = tt[0];
        double Sjr = tt[1];
        tt = matlabSumRow(Clust, 2);
        double Sjs2 = tt[0];
        double Sjr2 = tt[1];
        double Sjsjr = matlabSumCross(Clust);

        // Quadratic sums and cross-sum in units of meters
        double s2x = (Sjs2 - Sjs * Sjs / icpt) * PixszSam * PixszSam;
        double s2y = (Sjr2 - Sjr * Sjr / icpt) * PixszRec * PixszRec;
        double r2 = (Sjsjr - Sjs * Sjr / icpt) * PixszSam * PixszRec;

        if (r2 != 0) {
            // Elongated shape
            double QQ = (s2y - s2x) / (2 * r2);
            aa = QQ + Math.sqrt(QQ * QQ + 1);
            double a2 = QQ - Math.sqrt(QQ * QQ + 1);
            double dd = (aa * (aa * s2x - 2 * r2) + s2y) / (aa * aa + 1);
            double d2 = (a2 * (a2 * s2x - 2 * r2) + s2y) / (a2 * a2 + 1);
            if (d2 < dd) {
                aa = a2;
            }
            a2 = -1 / aa;
            // The next is to find min and max of d1 and d2 (in units of m)
            double d1mn = aa * Clust.get(0)[0] * PixszSam - Clust.get(0)[1] * PixszRec;
            double d1mx = d1mn;
            double d2mn = a2 * Clust.get(0)[0] * PixszSam - Clust.get(0)[1] * PixszRec;
            double d2mx = d2mn;
            for (int icp = 2; icp < icpt; icp++) {
                dd = aa * Clust.get(icp)[0] * PixszSam - Clust.get(icp)[1] * PixszRec;
                if (dd < d1mn) {
                    d1mn = dd;
                } else {
                    if (dd > d1mx) {
                        d1mx = dd;
                    }
                }
                dd = a2 * Clust.get(icp)[0] * PixszSam - Clust.get(icp)[1] * PixszRec;
                if (dd < d2mn) {
                    d2mn = dd;
                } else {
                    if (dd > d2mx) {
                        d2mx = dd;
                    }
                }
            }

            Twid = (d1mx - d1mn) / Math.sqrt(aa * aa + 1);
            Tlen = (d2mx - d2mn) / Math.sqrt(a2 * a2 + 1);
        } else {
            // Round shape or line on sample axis or on record axis
            double[] srmn = matlabMinRow(Clust);
            double[] srmx = matlabMaxRow(Clust);
            Twid = (srmx[1] - srmn[1]) * PixszRec;
            Tlen = (srmx[0] - srmn[0]) * PixszSam;
            if (s2x == s2y) {
                // Round shape
                aa = 0.0;
                Twid = (Twid + Tlen) / 2;
                Tlen = Twid;
            } else {
                aa = 0;
                // Line on sample axis
                if (s2x < s2y) {
                    // No, line on record axis
                    aa = Double.POSITIVE_INFINITY;
                    double a2 = Tlen;
                    Tlen = Twid;
                    Twid = a2;
                }
            }
        }

        // Deconvolve
        Tlen = Math.max(Tlen - Math.sqrt(PixszSam * PixszRec), 0);
        Twid = Math.max(Twid - Math.sqrt(PixszSam * PixszRec), 0);
        // Size calculated is difference between the extreme pixel centers,
        // therefore 0 if 1 pixel wide or long; give minimum size (not perfect)
        Tlen = Math.sqrt(Tlen * Tlen + PixszSam * PixszRec);
        Twid = Math.sqrt(Twid * Twid + PixszSam * PixszRec);

        // Heading in degrees [-90..90]
        Thed = 180.0 / Math.PI * Math.atan(aa);

        // calculate centre using min and max
        double[] centre = new double[2];
        centre[0] = matlabMinRow(Clust)[0] + (matlabMaxRow(Clust)[0] - matlabMinRow(Clust)[0]) / 2;
        centre[1] = matlabMinRow(Clust)[1] + (matlabMaxRow(Clust)[1] - matlabMinRow(Clust)[1]) / 2;

        return new double[]{icpt, centre[0], centre[1], Tlen, Twid, Thed};

    }

    // LenWidHedd Length, width, heading and centre of pixel cluster
    // 
    //    Clust  Cluster obtained e.g. by ClustGrow, i.e. Clust( 1, :) are
    //           sample locations of cluster and Clust( 2, :) record locations
    //    Pixsz  [PixszSam PixszRec] Sample, record pixel size in m
    //    Len    Length of cluster in m
    //    Wid    Width of custer in m
    //    Hed    Heading (orientation) of cluster long axis in rad
    //           heading in coordinate frame in m (different from heading in
    //           coordinate frame in pixels in case Pixsz(1)~=Pixsz(2))
    //           In case of round cluster, NaN
    //    Cen    Centre of clustre [sam; rec] in pixels (real)
    //  Heading is positive (anti-clockwise) wrt sample (x-)axis, with the record (y-)
    //  axis at +90 deg wrt sample axis, 180 deg ambiguous, between [-pi/2..+pi/2].
    //  Calculated by least-squares fit of a line through the pixels in the cluster
    //  as plotted in a frame in meters using Pixsz.
    //  (Each pixel is treated as (x,y) pair, assuming errors in both x and y.)
    //  Based on CFAR08.m
    //  Improved version of LenWidHedm; uses cluster size based on average pixel
    //  location instead of extreme pixel location, and better treatment for non-
    //  square pixels and for nearly-round clusters.
    //  Recursively discards outlying pixels from cluster.
    // 
    //  See also LenWidHedBox, LenWidHedm, fLenWidHead, LenWidHead.m
    //  (c) H. Greidanus 2008

    //  Subroutines: none
    public double[] LenWidHedd(List<int[]> Clust, double[] Pixsz) {
        double Len = 0.0;
        double Wid = 0.0;
        double Hed = Double.NaN;
        double[] Cen = {Double.NaN, Double.NaN};
        double ap = 0.0;

        //  Parameter
        double fRecl = 0.6; //  # Pixels limit for outlier removal in length
        double fRecw = 0.4; //  # Pixels limit for outlier removal in width
        //  (the higher the less outliers removed)

        double Nptc = (double) Clust.size(); //  # Pixels in cluster

        if (Nptc == 0) {
            //  Empty cluster
            return new double[]{Len, Wid, Hed, Cen[0], Cen[1]};
        }

        //  (Note, Clust is [x; y] for each pixel in the cluster)
        //  Sums, quadratic sums and cross-sum in units of pixels
        double[] Sj = matlabSumRow(Clust, 1); //  Sigma( x), Sigma( y)
        double[] Sj2 = matlabSumRow(Clust, 2); //  Sigma( x2), Sigma( y2)
        double Sjx = matlabSumCross(Clust); //  Sigma( xy)

        Cen[0] = Sj[0] / Nptc; //  [centre-x; centre-y]
        Cen[1] = Sj[1] / Nptc; //  [centre-x; centre-y]

        //System.out.println("\nCen: " + Cen[0] + " " + Cen[1] + " Nptc: " + Nptc + " Sj2: " + Sj2[0] + " " + Sj2[1] + " Sjx: " + Sjx);

        // create new cluster
        List<double[]> Clust0 = new ArrayList<double[]>();
        for (int[] element : Clust) {
            Clust0.add(new double[]{(double) element[0] - Cen[0], (double) element[1] - Cen[1]}); //  Cluster pixels wrt centre
        }
        //  Stdev and correlation in units of meters
        double s2x = (Sj2[0] - Sj[0] * Sj[0] / Nptc) * Pixsz[0] * Pixsz[0]; //  Sigma( x2)- Sigma( x)^2/ N
        double s2y = (Sj2[1] - Sj[1] * Sj[1] / Nptc) * Pixsz[1] * Pixsz[1]; //  Sigma( y2)- Sigma( y)^2/ N
        double r2 = (Sjx - Sj[0] * Sj[1] / Nptc) * Pixsz[0] * Pixsz[1]; //  Sigma( xy)- Sigma( x).Sigma( y)/ N
        //System.out.println("Cen: " + Cen[0] + " " + Cen[1] + " r2 " + r2 + " s2x " + s2x + " s2y " + s2y);

        if ((Math.abs(r2) / (Pixsz[0] * Pixsz[1])) > 1 / 40.0) // (if r2~= 0)
        {
            //  Elongated shape (as seen in meters frame)
            double QQ = (s2y - s2x) / (2.0 * r2);
            ap = QQ + Math.sqrt(QQ * QQ + 1.0); //  RC of best fitting line (in meters coord frame)
            //  RC of line perpendicular to best fitting line:
            double a2 = QQ - Math.sqrt(QQ * QQ + 1.0);
            //  (However, ap and a2 may be interchanged, not known here)
            double w1 = ap * ap + 1.0;
            double w2 = a2 * a2 + 1.0;
            //  Distances of all cluster pixels to best fitting line (in meters)
            //  times sqrt( w1):
            double[] d1 = matlabSumProdRegmat2D(new double[]{ap * Pixsz[0], -1.0 * Pixsz[1]}, Clust0);
            //  Average (absolute) distance (m) of cluster pixels to line (times 2):
            double ma1 = 2.0 * matlabMeanAbs1D(d1) / Math.sqrt(w1);
            //  One pixel distance along direction ap is dl1 meters:
            double dl1 = Math.sqrt((Pixsz[0] * Pixsz[0] + ap * ap * Pixsz[1] * Pixsz[1]) / w1);
            // Wid= max( ma1+ realsqrt( 0.5* dl1* dl1+ ma1* ma1)- dl1, dl1); //  Width (meters)
            // Wid= max( ma1+ realsqrt( 0.5* dl1* dl1+ ma1* ma1), dl1); //  Width (meters)
            //  (The above formula approximates the relation between "ma1" and "Wid"
            //  for a rectangular noise-free cluster, width an accuracy of +/- 0.5
            //  pixel and a minimum value of 1 pixel for an unresolved target.)
            Wid = Math.max(2.0 * ma1, dl1); //  Width (meters)
            //  Same for perpendicular line:
            double[] d2 = matlabSumProdRegmat2D(new double[]{a2 * Pixsz[0], -1.0 * Pixsz[1]}, Clust0);
            double ma2 = 2 * matlabMeanAbs1D(d2) / Math.sqrt(w2);
            double dl2 = Math.sqrt((Pixsz[0] * Pixsz[0] + a2 * a2 * Pixsz[1] * Pixsz[1]) / w2);
            // Len= max( ma2+ realsqrt( 0.5* dl2* dl2+ ma2* ma2)- dl2, dl2); //  Length (meters)
            // Len= max( ma2+ realsqrt( 0.5* dl2* dl2+ ma2* ma2), dl2); //  Length (meters)
            Len = Math.max(2.0 * ma2, dl2); //  Length (meters)
            //  At this point, it is still not know which of Len and Wid is actually
            //  the longer or shorter dimension
            //System.out.println(" Len: " + Len + " Wid: " + Wid + " ma1 " + ma1 + " dl1 " + dl1 + " ma2 " + ma2 + " dl2 " + dl2 + " ap " + ap + " a2 " + a2);

            //  Which points are outliers (further than fRec pixels out of box):
            List<int[]> clust1 = new ArrayList<int[]>();
            // find( abs( d1)/ realsqrt( w1)- Wid/ 2> fRecw* dl1 | abs( d2)/ realsqrt( w2)- Len/ 2> fRecl* dl2);
            for (int i = 0; i < d1.length; i++) {
                if ((Math.abs(d1[i]) / Math.sqrt(w1) - Wid / 2.0 > fRecw * dl1) || (Math.abs(d2[i]) / Math.sqrt(w2) - Len / 2.0 > fRecl * dl2)) {
                    //  Remove outliers and call recursively
                } else {
                    clust1.add(Clust.get(i));
                }
            }

            //System.out.println("Clust size : " + Clust.size() + " clust1 size: " + clust1.size());
            if (clust1.size() < Clust.size()) {
                //System.out.println(clust1.size());
                double[] result = LenWidHedd(clust1, Pixsz);
                return result;
            }

            //  Change length and width if needed to get length as the longest (in m):
            if (Len < Wid) {
                double tmp = Len;
                Len = Wid;
                Wid = tmp;
                ap = a2;
            }
        } else {
            //  Round shape or line on sample axis or on record axis
            //System.out.println("Round shape or line on sample axis or on record axis Cen: " + Cen[0] + " " + Cen[1] + " Len: " + Len + " Wid: " + Wid);

            //  Average (absolute) distance of cluster pixels [to x-axis; to y-axis]
            //  (times 2) (in units of pixels):
            double[] ma = matlabMeanAbs2D(Clust0);
            ma[0] = ma[0] * 2.0;
            ma[1] = ma[1] * 2.0;
            //  Cluster size along [x-axis; y-axis] (meters):
            // Siz= max( ma+ realsqrt( 0.5+ ma.* ma)- 1, 1).* Pixsz;
            double[] Siz = new double[]{Math.max(ma[0] + Math.sqrt(0.5 + ma[0] * ma[0]), 1) * Pixsz[0], Math.max(ma[1] + Math.sqrt(0.5 + ma[1] * ma[1]), 1) * Pixsz[1]};
            if (Math.abs(Math.sqrt(s2x / Nptc) - Math.sqrt(s2y / Nptc)) < (Math.max(Pixsz[0], Pixsz[1]) / 40.0)) //  (if s2x== s2y)
            {
                //  Round shape
                Len = (Siz[0] + Siz[1]) / 2.0;
                Wid = Len;
                ap = 0.0;
            } else {
                if (s2x > s2y) {
                    //  Line on sample axis
                    Len = Siz[0];
                    Wid = Siz[1];
                    ap = 0.0;
                } else {
                    //  Line on record axis
                    Len = Siz[1];
                    Wid = Siz[0];
                    ap = Double.POSITIVE_INFINITY;
                }
            }
        }

        Hed = Math.atan(ap); //  Heading in rad [-pi/2..pi/2]

        return new double[]{Clust.size(), Cen[0], Cen[1], Len, Wid, Math.toDegrees(Hed)};
    }

    // return the smallest x and the smallest y in the Vector
    private double[] matlabMinRow(List<int[]> table) {
        double[] result = {0.0, 0.0};
        if (table.size() > 0) {
            result[0] = table.get(0)[0];
            result[1] = table.get(0)[1];
            for (int i = 1; i < table.size(); i++) {
                if (result[0] > (double) table.get(i)[0]) {
                    result[0] = (double) table.get(i)[0];
                }
                if (result[1] > (double) table.get(i)[1]) {
                    result[1] = (double) table.get(i)[1];
                }
            }
        }
        return result;
    }

    // return the largest x and the largest y in the Vector
    private double[] matlabMaxRow(List<int[]> table) {
        double[] result = {0.0, 0.0};
        if (table.size() > 0) {
            result[0] = table.get(0)[0];
            result[1] = table.get(0)[1];
            for (int i = 1; i < table.size(); i++) {
                if (result[0] < (double) table.get(i)[0]) {
                    result[0] = (double) table.get(i)[0];
                }
                if (result[1] < (double) table.get(i)[1]) {
                    result[1] = (double) table.get(i)[1];
                }
            }
        }
        return result;
    }

    private double[] matlabSumRow(List<int[]> table, int power) {
        double[] result = {0.0, 0.0};
        for (int i = 0; i < table.size(); i++) {
            result[0] = result[0] + Math.pow((double) table.get(i)[0], power);
            result[1] = result[1] + Math.pow((double) table.get(i)[1], power);
        }
        return result;
    }

    private double matlabSumCross(List<int[]> table) {
        double result = 0.0;
        for (int i = 0; i < table.size(); i++) {
            result = result + (double) table.get(i)[0] * (double) table.get(i)[1];
        }
        return result;
    }

    private double[] matlabSumProdRegmat2D(double regmat[], List<double[]> table) {
        double[] result = new double[table.size()];
        for (int i = 0; i < table.size(); i++) {
            result[i] = regmat[0] * (double) table.get(i)[0] + regmat[1] * (double) table.get(i)[1];
        }
        return result;
    }

    private double[] matlabMeanAbs2D(List<double[]> table) {
        double[] result = new double[]{0.0, 0.0};
        for (int i = 0; i < table.size(); i++) {
            result[0] = result[0] + (double) Math.abs(table.get(i)[0]);
            result[1] = result[1] + (double) Math.abs(table.get(i)[1]);
        }
        // calculate mean of the values
        result[0] = result[0] / table.size();
        result[1] = result[1] / table.size();

        return result;
    }

    private double matlabMeanAbs1D(double[] table) {
        double result = 0.0;
        for (int i = 0; i < table.length; i++) {
            result += (double) Math.abs(table[i]);
        }
        // calculate mean of the values
        result = result / table.length;

        return result;
    }

    public double[] getValues() {
        double[] values = new double[2 * boatArray.size()];
        int i = 0;
        for (double[] val : boatArray) {
            values[i++] = val[1];
            values[i++] = val[2];
        }
        return values;
    }

    public List<Geometry> getAllDetectedPixels() {
        List<Geometry> out = new ArrayList<Geometry>();
        GeometryFactory gf = new GeometryFactory();
        
        Collection<DetectedPixel> enumeration = allDetectedPixels.values();
        for (DetectedPixel pixel:enumeration) {
            out.add(gf.createPoint(new Coordinate(pixel.x, pixel.y)));
        }

        return out;
    }

    public List<Geometry> getThresholdclipPixels() {
        List<Geometry> out = new ArrayList<Geometry>();
        GeometryFactory gf = new GeometryFactory();
        for (boatPixels boat : listboatneighbours) {
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
        for (boatPixels boat : listboatneighbours) {
            List<int[]> positions = boat.getThresholdaggregatePixels();
            for (int[] position : positions) {
                out.add(gf.createPoint(new Coordinate(position[0], position[1])));
            }
        }

        return out;
    }
};
