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
        private int[] corners;
        
        

		private double stdvalue = 0.0;
        
        //HH,HV,VH,VV 
        private double[] thresholdvalue = {0,0,0,0};
        private double[] meanvalue = {0,0,0,0};
        private int[] maxValues = {0,0,0,0};
        private double[] significance = {0,0,0,0};
        private double[] average = {0,0,0,0};
        
        private double[] stDev = {0,0,0,0};

        public BoatConnectedPixelMap(int cornerx,int cornery,int x, int y, int id, int value) {
            // add initial pixel, clipped value is always set to 1
            connectedpixels.put(new StringBuilder().append(x).append(" ").append(y).toString(), new int[]{x, y, value, 1});
            this.boatposition = new double[]{x, y};
            this.corners=new int[]{cornerx,cornery};
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
        
        public int[] getCorners() {
			return corners;
		}

		public void setCorners(int[] corners) {
			this.corners = corners;
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

        protected int[] getMaximumValues() {
            return maxValues;
        }
        
        protected void setMaxValues(int[] maxValues) {
            this.maxValues=maxValues;
        }
        protected void putMaxValue(int band,int maxValue) {
            this.maxValues[band]=maxValue;
        }
        
        
        
        protected void setStdValues(double[] stDev) {
            this.stDev=stDev;
        }
        protected void putStDevValue(int band,double stDev) {
            this.stDev[band]=stDev;
        }
        protected double[] getStDevValues() {
            return stDev;
        }
        
        protected void setAvgValues(double[] avgVals) {
            this.average=avgVals;
        }
        protected void putAvgValue(int band,double avgVals) {
            this.average[band]=avgVals;
        }
        protected double[] getAvgValues() {
            return average;
        }
        
        
        protected void setSignificanceValues(double[] sign) {
            this.significance=sign;
        }
        protected void putSignificanceValue(int band,double sign) {
            this.significance[band]=sign;
        }
        protected double[] getSignificanceValues() {
            return significance;
        }
        
        protected void setMeanValue(double[] meanvalue) {
            this.meanvalue = meanvalue;
        }
        protected double getMeanValue(int band) {
            return meanvalue[band];
        }
        protected double[] getMeanValues() {
            return meanvalue;
        }
        
        protected void putMeanValue(int band,double meanvalue) {
            this.meanvalue[band]=meanvalue;
        }
        
        protected double getMeanValueBand(int band) {
            return this.meanvalue[band];
        }
        
        protected void setStdValue(double stdvalue) {
            this.stdvalue = stdvalue;
        }

        protected double getStdValue() {
            return stdvalue;
        }

        protected void setThresholdValue(double[] thresholdvalue) {
            this.thresholdvalue = thresholdvalue;
        }

        protected double[] getThresholdValue() {
            return thresholdvalue;
        }
        protected void putThresholdValue(int band,double thresholdvalue) {
            this.thresholdvalue[band]=thresholdvalue;
        }

        protected double getThresholdValueBand(int band) {
            return thresholdvalue[band];
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

        protected void setTouchesLandMask(boolean touchlandmask) {
            this.touchlandmask = touchlandmask;
        }

        protected boolean touchesLand() {
            return touchlandmask;
        }
    }
