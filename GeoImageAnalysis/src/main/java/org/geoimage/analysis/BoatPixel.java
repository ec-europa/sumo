package org.geoimage.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoatPixel {

        private Map<String, int[]> connectedpixels = new HashMap<String, int[]>();
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

        public BoatPixel(int x, int y, int id, int value, double[][] thresholdvalues) {
            // add initial pixel, clipped value is always set to 1
            connectedpixels.put(new StringBuilder().append(x).append(" ").append(y).toString(), new int[]{x, y, value, 1});
            this.boatposition = new double[]{x, y};
            this.boatmaximumvalue = value;
            this.id = id;
        }

        public void addConnectedPixel(int x, int y, int value, boolean clipped) {
            connectedpixels.put(new StringBuilder().append(x).append(" ").append(y).toString(), new int[]{x, y, value, clipped ? 1 : 0});
        }

        public boolean containsPixel(int x, int y) {
            return connectedpixels.get(new StringBuilder().append(x).append(" ").append(y).toString()) != null;
        }

        public void computeValues(double pixsam,double pixrec) {
            // clip all values below thresholdclip
            List<int[]> clust = new ArrayList<int[]>();
            int[][] pixels = connectedpixels.values().toArray(new int[0][]);
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

        protected double getMaximumValue() {
            return boatmaximumvalue;
        }

        protected void setMeanValue(double meanvalue) {
            this.meanvalue = meanvalue;
        }

        protected double getMeanValue() {
            return meanvalue;
        }

        protected void setStdValue(double stdvalue) {
            this.stdvalue = stdvalue;
        }

        protected double getStdValue() {
            return stdvalue;
        }

        protected void setThresholdValue(double thresholdvalue) {
            this.thresholdvalue = thresholdvalue;
        }

        protected double getThresholdValue() {
            return thresholdvalue;
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
