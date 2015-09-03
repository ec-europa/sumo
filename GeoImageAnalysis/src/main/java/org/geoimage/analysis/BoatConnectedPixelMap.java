package org.geoimage.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 
 * @author argenpo
 *this class manage the connection between the Boat and the pixels
 *
 */
public class BoatConnectedPixelMap {

        private Map<String, int[]> connectedpixels = new HashMap<String, int[]>();
        private double boatnumberofpixels = 0.0;
        private double[] boatposition;
        private double boatwidth = 0.0;
        private double boatlength = 0.0;
        private double boatheading = 0.0;
        private int id = 0;
        private int maxValue = 0;
        private boolean touchlandmask = false;
        
        
        private double stdvalue = 0.0;
        
        
        private List<Double> thresholdvalue = new ArrayList<>();
        private List<Double> meanvalue = new ArrayList<>();
        private int[] maxValues = null;

        public BoatConnectedPixelMap(int x, int y, int id, int value) {
            // add initial pixel, clipped value is always set to 1
            connectedpixels.put(new StringBuilder().append(x).append(" ").append(y).toString(), new int[]{x, y, value, 1});
            this.boatposition = new double[]{x, y};
            this.maxValue = value;
            this.id = id;
        }

        public void addConnectedPixel(int x, int y, int value, boolean clipped) {
            connectedpixels.put(new StringBuilder().append(x).append(" ").append(y).toString(), new int[]{x, y, value, clipped ? 1 : 0});
        }

        public boolean containsPixel(int x, int y) {
            return connectedpixels.get(new StringBuilder().append(x).append(" ").append(y).toString()) != null;
        }

        /**
         * 
         * @param pixsam
         * @param pixrec
         */
        public void computeValues(double pixsam,double pixrec) {
            // clip all values below thresholdclip
            List<int[]> clust = new ArrayList<int[]>();
            int[][] pixels = connectedpixels.values().toArray(new int[0][]);
            for (int[] pixel: pixels) {
                if (pixel[3] == 1) {
                    clust.add(pixel);
                }
                // look for maximum value in pixels
                if (pixel[2] > maxValue) {
                    maxValue = pixel[2];
                }
            }
            // calculate length and width for cluster
            double[] result = Compute.lenWidHedd(clust, new double[]{pixsam, pixrec});
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

        protected int[] getMaximumValueS() {
            return maxValues;
        }
        
        protected void setMaxValues(int band,int[] maxValues) {
            this.maxValues=maxValues;
        }
        
        protected void setMeanValue(List<Double> meanvalue) {
            this.meanvalue = meanvalue;
        }
        protected List<Double> getMeanValue(int band) {
            return meanvalue;
        }
        
        protected double getMeanValueBand(int band) {
            return this.meanvalue.get(band);
        }
        
        protected void setStdValue(double stdvalue) {
            this.stdvalue = stdvalue;
        }

        protected double getStdValue() {
            return stdvalue;
        }

        protected void setThresholdValue(List<Double> thresholdvalue) {
            this.thresholdvalue = thresholdvalue;
        }

        protected List<Double> getThresholdValue() {
            return thresholdvalue;
        }
        protected void putThresholdValue(int band,double thresholdvalue) {
            this.thresholdvalue.add(band,thresholdvalue);
        }

        protected double getThresholdValueBand(int band) {
            return thresholdvalue.get(band);
        }


        protected List<int[]> getThresholdclipPixels() {
            // clip all values below thresholdclip
            List<int[]> clust = new ArrayList<int[]>();
            int[][] pixels = connectedpixels.values().toArray(new int[0][]);
            for (int[] pixel:pixels) {
                if (pixel[3] == 1) {
                	clust.add(pixel);
                }
            }

            return clust;
        }

        protected List<int[]> getThresholdaggregatePixels() {
            // clip all values below thresholdclip
            List<int[]> clust = new ArrayList<int[]>();
            int[][] pixels = connectedpixels.values().toArray(new int[0][]);
            for (int[] pixel:pixels) {
                clust.add(pixel);
            }

            return clust;
        }

        protected void setLandMask(boolean touchlandmask) {
            this.touchlandmask = touchlandmask;
        }

        protected boolean touchesLand() {
            return touchlandmask;
        }
    }
