/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.geoimage.def.SarImageReader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * This class is used to filter false targets due to azimuthal ambiguities 
 * 
 * @author Juan Ignacio Cicuendez-Perez, adapted by leforth
 *
 */
public class AzimuthAmbiguity {

    final int defaultWindowsSize = 20;

    // Ambiguities arise in multiple distances 
    final int defaultSteps = 2;

    // The echo signal has to be smaller than a percentage of the target
    final double percentageSize = 0.60;//0.60

    // If the peakRCS is about this threshold the point has not ambiguity
    private double rcsThreshold = -25.0;

    // This correction factor in the azimuth is applied when the target producing the ambiguity is large
    private int posCorrection = 30;

    // The minimum size to generate ambiguity
    private int minSize = 5;//10

    // Size of the ambiguity window search
    private int halfWindow = defaultWindowsSize / 2;
    private int numSteps = defaultSteps;
    private SarImageReader sumoImage = null;
    private double[][] boatArray = null;

    // lits of boats having been filtered out
    List<double[]> ambiguityboatlist = new ArrayList<double[]>();

    public List<double[]> getAmbiguityboatlist() {
        return ambiguityboatlist;
    }

    // get a geometry layer from the list of ambiguity boats
    public List<Geometry> getAmbiguityboatgeometry() {
        List<Geometry> out = new ArrayList<Geometry>();
        GeometryFactory gf = new GeometryFactory();
        for (int i = 0; i < ambiguityboatlist.size(); i++) {
            double[] position = ambiguityboatlist.get(i);
            out.add(gf.createPoint(new Coordinate(position[1], position[2])));
        }

        return out;
    }

    public void setRCSThreshold(double rcsThreshold) {

        this.rcsThreshold = rcsThreshold;
    }

    public double getRCSThreshold() {

        return this.rcsThreshold;
    }

    public void setSizeSearchWindow(int windowSize) {

        this.halfWindow = windowSize / 2;

    }

    public int getSizeSearchWindow() {

        return this.halfWindow * 2;

    }

    public void setNumSteps(int num) {

        this.numSteps = num;

    }

    public int getNumSteps() {

        return this.numSteps;

    }

    public AzimuthAmbiguity(double[][] boatList, SarImageReader image, int windowSize, int numSteps, double rcsThreshold) {

        setRCSThreshold(rcsThreshold);
        setSizeSearchWindow(windowSize);
        setNumSteps(numSteps);

        new AzimuthAmbiguity(boatList, image);

    }

    public int[] getSearchWindows(int xPos, int yPos, int[] deltaAzimuth) {

        // The first 4 coordinates are [xmin,xmax,ymin,ymax] of the upper window
        int[] windowCoords = new int[8 * this.numSteps]; // Each step has 8 values -2 windows of 4
        int[] imageSize = new int[]{sumoImage.getWidth(), sumoImage.getHeight()};
        int shiftedPosUp = 0, shiftedPosDown = 0, x_offset = 0, x_shift = 0;

        if (((String) sumoImage.getMetadata(sumoImage.SATELLITE)).equals("RADARSAT")) {
            String myImageType = (String) sumoImage.getMetadata(sumoImage.TYPE);
            if (myImageType.equals("RSAT-1-SAR-SGF") || myImageType.equals("RSAT-1-SAR-SGX")) {
                deltaAzimuth[1] = 20;	// This is really range displacement
            }
        }
        if (sumoImage.getMetadata(sumoImage.ORBIT_DIRECTION).equals("ASCENDING")) {
            x_offset = deltaAzimuth[1];
        } else {
            x_offset = -deltaAzimuth[1];
        }

        // Windows greater than the deltaAzimuth are not permitted
        if (this.halfWindow > deltaAzimuth[1]) {
            this.halfWindow = defaultWindowsSize / 2;
        }

        x_shift = x_offset;

        for (int i = 0; i < this.numSteps; i++) {

            // For the x coordinates
            x_shift = x_shift + 2 * i * x_offset;

            // For the upper window
            if (xPos > halfWindow + x_shift) {
                windowCoords[8 * i] = xPos + x_shift - halfWindow;
            } else {
                windowCoords[8 * i] = 0;
            }

            if (xPos + x_shift + halfWindow < imageSize[0]) {
                windowCoords[1 + 8 * i] = xPos + x_shift + halfWindow;
            } else {
                windowCoords[1 + 8 * i] = imageSize[0] - 1;
            }

            // For the lower window
            if (xPos > halfWindow - x_shift) {
                windowCoords[4 + 8 * i] = xPos - x_shift - halfWindow;
            } else {
                windowCoords[4 + 8 * i] = 0;
            }

            if (xPos - x_shift + halfWindow < imageSize[0]) {
                windowCoords[5 + 8 * i] = xPos - x_shift + halfWindow;
            } else {
                windowCoords[5 + 8 * i] = imageSize[0] - 1;
            }

            shiftedPosUp = yPos - deltaAzimuth[0] * (i + 1);
            shiftedPosDown = yPos + deltaAzimuth[0] * (i + 1);

            // For the y coordinates
            if (shiftedPosUp + halfWindow > 0) {

                windowCoords[3 + 8 * i] = shiftedPosUp + halfWindow;

                if (shiftedPosUp - halfWindow > 0) {
                    windowCoords[2 + 8 * i] = shiftedPosUp - halfWindow;
                } else {
                    windowCoords[2 + 8 * i] = 0;
                }

            } else {

                windowCoords[2 + 8 * i] = 0;
                windowCoords[3 + 8 * i] = 0;
            }

            if (shiftedPosDown - halfWindow < imageSize[1]) {

                windowCoords[6 + 8 * i] = shiftedPosDown - halfWindow;

                if (shiftedPosDown + halfWindow < imageSize[1]) {
                    windowCoords[7 + 8 * i] = shiftedPosDown + halfWindow;
                } else {
                    windowCoords[7 + 8 * i] = imageSize[1] - 1;
                }

            } else {

                windowCoords[6 + 8 * i] = imageSize[1] - 1;
                windowCoords[7 + 8 * i] = imageSize[1] - 1;

            }

        }


        return windowCoords;

    }

    public boolean checkEcho(double[] targetBoat, double[] testBoat, int[] windowCoords) {

        int echo = 4;
        int xTestBoat = (int) (testBoat[1]);
        int yTestBoat = (int) (testBoat[2]);
        int yWindowCorrection = 0;

        //AG removed because I don't have k value on RSAT2...
        //double targetPeakVal = sumoImage.getSigmaNoughtDb(new int[]{(int) targetBoat[1], (int) targetBoat[2]}, targetBoat[3], sumoImage.getIncidence((int) targetBoat[1]));
        //double testPeakVal = sumoImage.getSigmaNoughtDb(new int[]{xTestBoat, yTestBoat}, testBoat[3], sumoImage.getIncidence((int) xTestBoat));

        double targetPeakVal =targetBoat[3];
        double testPeakVal =testBoat[3];

        /*
        if(targetPeakVal == -1){

        targetPeakVal = Math.log(Math.pow(targetBoat.classification.maxValue, 2)/Math.sin(targetBoat.statistics2.incidence * Math.PI / 180));
        testPeakVal = Math.log(Math.pow(testBoat.classification.maxValue, 2)/Math.sin(testBoat.statistics2.incidence * Math.PI / 180));

        }
         */

        int targetNumPixels = (int) targetBoat[7];
        int testNumPixels = (int) testBoat[7];

        //AG check if the echo has a pixel max value bigger than the target/10
        if (testBoat[3]>targetBoat[3]/2) return false;

        // If the vessel is very large and saturated its centre might be badly positioned, we add
        // extra length to the search window
        if (targetNumPixels > 100) {
            yWindowCorrection = posCorrection;
        }

        for (int k = 0; k < this.numSteps; k++) {

            // The size of a detected echo can not have a greater number of pixels than the target
            if (testNumPixels > (Math.floor(Math.pow(percentageSize, k + 1) * targetNumPixels))) {
                continue;
            }

            // We have if the test boat fall within the ambiguity azimuth windows

            // The echo coordinates should be in the x-range
            if ((xTestBoat > windowCoords[0 + 8 * k]) && (xTestBoat < windowCoords[1 + 8 * k])) {

                // Checking in the upper window

                if ((yTestBoat > windowCoords[2 + 8 * k] - yWindowCorrection) && (yTestBoat < windowCoords[3 + 8 * k] + yWindowCorrection)) {

                    if (testPeakVal < targetPeakVal) {

                        ambiguityboatlist.add(testBoat);
                        return true;

                    }
                }
            }

            // Checking in the lower window

            if ((xTestBoat > windowCoords[4 + 8 * k]) && (xTestBoat < windowCoords[5 + 8 * k])) {


                if ((yTestBoat > windowCoords[6 + 8 * k] - yWindowCorrection) && (yTestBoat < windowCoords[7 + 8 * k] + yWindowCorrection)) {

                    if (testPeakVal < targetPeakVal) {

                        ambiguityboatlist.add(testBoat);
                        return true;

                    }
                }
            }

        }
        return false;

    }

    public AzimuthAmbiguity(double[][] boatList, SarImageReader image) {

        int echo = 4;
        int[] windowCoords, deltaAzimuth;

        this.boatArray = boatList;

        if ((boatArray == null) || (boatArray.length < 1)) {
            return;
        }

        this.sumoImage = image;

        // Get image type
        if (!image.supportAzimuthAmbiguity()) {
            System.out.println("\nSatellite sensor not supported for Azimuth Ambiguity detection");
            return;
        }

        //AG commented
        // check a few values before starting
        /*if (Double.parseDouble((String) sumoImage.getMetadata(sumoImage.K)) == 0.0) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JOptionPane.showMessageDialog(null, "K constant not available, please check your SAR processor", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            return;
        }*/

        // scan through the list of boats
        for (int i = 0; i < boatArray.length; i++) {

            double[] myBoat = boatArray[i];
            int xPos = (int) myBoat[1];
            int yPos = (int) myBoat[2];
            int id = (int) myBoat[0];

            deltaAzimuth = image.getAmbiguityCorrection(xPos);
            windowCoords = this.getSearchWindows(xPos, yPos, deltaAzimuth);
            System.out.println("\nSearch Window: " + xPos + " " + yPos + " " + deltaAzimuth[0]);

            // Only vessels with a minimum number of pixel can generate an ambiguity
            if (myBoat[7] < minSize) {
                continue;
            }

            //AG commented
            /*double peakRCS = image.getSigmaNoughtDb(new int[]{xPos, yPos}, myBoat[3], image.getIncidence(xPos));
            // Only strong RCS yield azimuth ambiguity
            if (peakRCS < rcsThreshold) {
                continue;
            }*/

            for (int j = 0; j < boatArray.length; j++) {

                if (j == i) {
                    continue;
                }
                double[] testBoat = boatArray[j];

                int test_id = (int) testBoat[0];

                // If the test vessel has already been classified as echo then continue
                if (ambiguityboatlist.contains(testBoat)) {
                    continue;
                }

                //Replacing the boat if it is an echo
                if (checkEcho(myBoat, testBoat, windowCoords)) {
                    // The echo is linked to the original target
                    ambiguityboatlist.add(testBoat);
                }
            }

        }

    }

}
