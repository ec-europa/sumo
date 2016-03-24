/*
 * 
 */
package org.geoimage.analysis;

import java.util.ArrayList;
import java.util.Collection;
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

        private Map<String,ConPixel> connectedpixels = new HashMap<String, ConPixel>();

		private double boatnumberofpixels = 0.0;
        private double[] boatposition;
        private double boatwidth = 0.0;
        private double boatlength = 0.0;
        private double boatheading = 0.0;
        private int id = 0;
        private int maxValue = 0;
		private boolean touchlandmask = false;
        private int[] corners;

        public class ConPixel {

            public int x;
            public int y;
            public int value;
            public boolean clippedValue=true;

            public ConPixel(){}
            public ConPixel(int x, int y, int value, boolean clipped) {
                this.x = x;
                this.y = y;
                this.value = value;
                this.clippedValue=clipped;
            }
        }

	//	private double stdvalue = 0.0;

		private BoatStatisticMapPolarization statMap;

		public BoatConnectedPixelMap(int cornerx,int cornery,int x, int y, int id, int value) {
            // add initial pixel, clipped value is always set to 1
            connectedpixels.put(new StringBuilder().append(x).append(" ").append(y).toString(), new ConPixel(x, y, value, true));
            this.boatposition = new double[]{x, y};
            this.corners=new int[]{cornerx,cornery};
            this.maxValue = value;
            this.id = id;
            statMap=new BoatStatisticMapPolarization();
        }

        public void addConnectedPixel(int x, int y, int value, boolean clipped) {
            connectedpixels.put(new StringBuilder().append(x).append(" ").append(y).toString(), new ConPixel(x, y, value, clipped));
        }

        public boolean containsPixel(int x, int y) {
            return connectedpixels.get(new StringBuilder().append(x).append(" ").append(y).toString()) != null;
        }

        public Collection<ConPixel> getConnectedpixels() {
			return connectedpixels.values();
		}


        /**
         *
         * @param pixsam
         * @param pixrec
         */
        public void computeValues(double pixsam,double pixrec) {
            // clip all values below thresholdclip
            List<int[]> clust = new ArrayList<int[]>();
            Collection<ConPixel> pixels = connectedpixels.values();
            for (ConPixel pixel: pixels) {
                if (pixel.clippedValue) {
                    clust.add(new int[]{pixel.x,pixel.y,pixel.value,pixel.clippedValue?1:0});
                }
                // look for maximum value in pixels
                if (pixel.value > maxValue) {
                    maxValue = pixel.value;
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
        public int getMaxValue() {
			return maxValue;
		}

		public void setMaxValue(int maxValue) {
			this.maxValue = maxValue;
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




        protected List<int[]> getThresholdclipPixels() {
            // clip all values below thresholdclip
            List<int[]> clust = new ArrayList<int[]>();
            //int[][] pixels = connectedpixels.values().toArray(new int[0][]);
            ConPixel[] pixels = connectedpixels.values().toArray(new ConPixel[0]);
            for (ConPixel pixel:pixels) {
                if (pixel.clippedValue) {
                	clust.add(new int[]{pixel.x,pixel.y,pixel.value,pixel.clippedValue?1:0});
                }
            }

            return clust;
        }

        protected List<int[]> getThresholdaggregatePixels() {
            // clip all values below thresholdclip
            List<int[]> clust = new ArrayList<int[]>();
            ConPixel[] pixels = connectedpixels.values().toArray(new ConPixel[0]);
            for (ConPixel pixel:pixels) {
                clust.add(new int[]{pixel.x,pixel.y,pixel.value,pixel.clippedValue?1:0});
            }

            return clust;
        }

        protected void setTouchesLandMask(boolean touchlandmask) {
            this.touchlandmask = touchlandmask;
        }

        protected boolean touchesLand() {
            return touchlandmask;
        }

        public BoatStatisticMapPolarization getStatMap() {
			return statMap;
		}

		public void setStatMap(BoatStatisticMapPolarization statMap) {
			this.statMap = statMap;
		}
    }
