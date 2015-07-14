/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.FastMath;
import org.geoimage.def.SarImageReader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;


class Window{
	int x=0;
	int y=0;
	int sizeX=0;
	int sizeY=0;
	
	public Window(int x,int y,int sizeX,int sizeY){
		this.x=x;
		this.y=y;
		this.sizeX=sizeX;
		this.sizeY=sizeY;
	}
	
	public static Window createWindowFromAzimuth(int xBoat,int yBoat,int dAz,int sizeX,int sizeY,int pixelSize){
		int windowX=xBoat- (AzimuthAmbiguity.X_WINDOW_SIZE/2);
		int windowY=yBoat- (AzimuthAmbiguity.Y_WINDOW_SIZE/2)+dAz;
		
		return new Window(windowX, windowY, sizeX, sizeY);
	}
	
}

/**
 * This class is used to filter false targets due to azimuthal ambiguities 
 * 
 * @author Pietro Argentieri ( Juan Ignacio Cicuendez-Perez, adapted by leforth)
 *
 */
public class AzimuthAmbiguity {

    // Ambiguities arise in multiple distances 
    final int defaultSteps = 2;

    // The echo signal has to be smaller than a percentage of the target
    final double percentageSize = 0.60;//0.60

    // If the peakRCS is about this threshold the point has not ambiguity
    private double rcsThreshold = -25.0;

    
    private SarImageReader sumoImage = null;
    private double[][] boatArray = null;

    // lits of boats having been filtered out
    List<double[]> ambiguityboatlist = new ArrayList<double[]>();

    //size of the window used to search the ambiguity in meters
    public static final int X_WINDOW_SIZE=250; 
    public static final int Y_WINDOW_SIZE=250; 
    

    
    public AzimuthAmbiguity(double[][] boatList, SarImageReader image) {
    	init();
    }
    
    public AzimuthAmbiguity(double[][] boatList, SarImageReader image, int windowSize, int numSteps, double rcsThreshold) {

        setRCSThreshold(rcsThreshold);
        setSizeSearchWindow(windowSize);
        setNumSteps(numSteps);

        init();

    }
    
    /**
     * 
     * @return the ambiguity boath list
     */
    public List<double[]> getAmbiguityboatlist() {
        return ambiguityboatlist;
    }

    /**
     * get a List of geometry: each geometry is an ambiguity boat
     * 
     * @return List of the geometry for the ambiguity boats
     */
    public List<Geometry> getAmbiguityboatgeometry() {
        List<Geometry> out = new ArrayList<Geometry>();
        GeometryFactory gf = new GeometryFactory();
        
        //loop on ambigutiy boat
        for (int i = 0; i < ambiguityboatlist.size(); i++) {
            double[] position = ambiguityboatlist.get(i);
            out.add(gf.createPoint(new Coordinate(position[1], position[2])));
        }

        return out;
    }

    /**
     * find max pixel Value
     * 
     * @param x
     * @param y
     * @param sizeX
     * @param sizeY
     * @return
     */
    private int getWindowMaxPixelValue(int x,int y,int sizeX,int sizeY){
    	int[] data=sumoImage.readTile(x,y,sizeX,sizeY, 0);
    	int highest = data[0];
	    for (int index = 1; index < data.length; index ++) {
	        if (data[index] > highest) {
	            highest = data [index];
	        }
	    }
	    return highest;
    } 
    
    
    /**
     * 
     * @param boatList
     * @param image
     */
    private void init() {
        int[] windowCoords, deltaAzimuth;


        // scan through the list of boats
        for (int i = 0; i < boatArray.length; i++) {
            double[] myBoat = boatArray[i];
            int xPos = (int) myBoat[1];
            int yPos = (int) myBoat[2];
            deltaAzimuth =sumoImage.getAmbiguityCorrection(xPos,yPos);

            Window winUp=Window.createWindowFromAzimuth(xPos, yPos, deltaAzimuth, sizeX, sizeY);
            
            System.out.println(new StringBuffer().append("\nSearch Window: ").append(xPos).append(" ").append(yPos).append(" ").append(deltaAzimuth[0]).toString());

            
            
            for (int j = 0; j < boatArray.length; j++) {
                if (j == i) {
                    continue;
                }
                double[] testBoat = boatArray[j];

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

}
