package org.geoimage.analysis;

public class AnalysisUtil {

	
	
	/**
     * @param thresholdParams threshold values setted as parameters 
     * @param threshTile[4] contains the threshold values for a tile divided into 4 areas 
     * 							kdist.estimate(rastermask,data);
     * 							double[] thresh = kdist.getDetectThresh() //this are valid input params
     * @return thresholds for the tile
     */
    public static double[] calcThreshWindowVals(double thresholdParams,double[] detectedTreshTile){
    	double threshWindowsVals[]=new double[4];
    	threshWindowsVals[0]=(thresholdParams * (detectedTreshTile[5] - 1.0) + 1.0) * detectedTreshTile[1] / detectedTreshTile[5];
    	threshWindowsVals[1]=(thresholdParams * (detectedTreshTile[5] - 1.0) + 1.0) * detectedTreshTile[2] / detectedTreshTile[5];
    	threshWindowsVals[2]=(thresholdParams * (detectedTreshTile[5] - 1.0) + 1.0) * detectedTreshTile[3] / detectedTreshTile[5];
    	threshWindowsVals[3]=(thresholdParams * (detectedTreshTile[5] - 1.0) + 1.0) * detectedTreshTile[4] / detectedTreshTile[5];
    	
    	return threshWindowsVals;
    }
    
    /**
     *  calculate new statistics using tile centered around pixel
     * @param cornerx
     * @param cornery
     * @param width
     * @param height
     * @param bands
     * @param data
     * @param kdist
     * @return
     */
    public static double[][] calculateImagemapStatistics(int cornerx, int cornery, int width,int height, int[] bands,int data[][], KDistributionEstimation kdist) {
        int numberofbands = bands.length;
        double[][] imagestat = new double[numberofbands][5];
        for (int i = 0; i < numberofbands; i++) {
            int band = bands[i];
            kdist.setImageData(cornerx,cornery, width, height,0,0,band);
            kdist.estimate(null,data[i]);
            double[] thresh = kdist.getDetectThresh();
            imagestat[i][0] = thresh[0];
            imagestat[i][1] = thresh[1] / thresh[5];
            imagestat[i][2] = thresh[2] / thresh[5];
            imagestat[i][3] = thresh[3] / thresh[5];
            imagestat[i][4] = thresh[4] / thresh[5];
        }

        return imagestat;
    }
}
