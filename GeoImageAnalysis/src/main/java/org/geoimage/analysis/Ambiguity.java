/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.geoimage.def.SarImageReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	/**
	 * 
	 * 
	 * @param xBoat     	x boat position
	 * @param yBoat			y boat position
	 * @param dAz			Delta Azimuth 	
	 * @param sizePixelX	size of each pixel in meters in range
	 * @param sizePixelY	size of each pixel in meters in Azimuth
	 * @return
	 */
	public static Window createWindowFromAzimuth(int xBoat,int yBoat,int dAz,double sizePixelX,double sizePixelY,boolean up){
		//size in pixel
		int xPixel=new Double(Ambiguity.X_WINDOW_SIZE/sizePixelX+1).intValue();
		int yPixel=new Double(Ambiguity.Y_WINDOW_SIZE/sizePixelY+1).intValue();
		
		//top left corner of the area
		int windowX=xBoat- (xPixel/2);
		int windowY=0;
		if(up)
			windowY=yBoat- (yPixel/2)-dAz;
		else
			windowY=yBoat- (yPixel/2)+dAz;
		
		return new Window(windowX, windowY, xPixel, yPixel);
	}
	
}

/**
 * @author Pietro Argentieri 
 *
 */
public abstract class Ambiguity {

    // Ambiguities arise in multiple distances 
    final int defaultSteps = 2;

    // The echo signal has to be smaller than a percentage of the target
    final double percentageSize = 0.60;//0.60

   
    protected SarImageReader sumoImage = null;
    protected Boat[] boatArray = null;
    
    protected final int AZIMUT_FACTOR=10;
    
    // lits of boats having been filtered out
    List<Boat> ambiguityboatlist = new ArrayList<Boat>();

    //size of the window used to search the ambiguity in meters
    public static final int X_WINDOW_SIZE=250; 
    public static final int Y_WINDOW_SIZE=250; 
    
    private Logger logger= LoggerFactory.getLogger(Ambiguity.class);
    protected int band[]=null;
    
    public Ambiguity(Boat[] boatList, SarImageReader image,int... band) {
    	this.boatArray=boatList;
    	this.sumoImage=image;
    	this.band=band;
    }
    
    public Ambiguity(Boat[]boatList, SarImageReader image, int windowSize, int numSteps,int... band) {
    	this.boatArray=boatList;
    	this.sumoImage=image;
    	this.band=band;
    }
    
    /**
     * 
     * @return the ambiguity boath list
     */
    public List<Boat> getAmbiguityboatlist() {
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
        	Boat boat = ambiguityboatlist.get(i);
            out.add(gf.createPoint(new Coordinate(boat.getPosx(), boat.getPosy())));
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
     * @throws IOException 
     */
    protected int getWindowMaxPixelValue(int x,int y,int sizeX,int sizeY,int band){
    	int highest =0;
    	try{
    		if(y<0)
    			y=0;
    		if(x<0)
    			x=0;
    		if(x>this.sumoImage.getWidth())
    			x=sumoImage.getWidth();
    		if(y>this.sumoImage.getHeight())
    			y=sumoImage.getHeight();
    		
    		//todo: check why the read method doesn't works
	    	int[] data=sumoImage.readTile(x,y,sizeX,sizeY, band);
	    	highest = data[0];
		    for (int index = 1; index < data.length; index ++) {
		        if (data[index] > highest) {
		            highest = data [index];
		        }
		    }
    	}catch(Exception e){
    		logger.warn("Ambiguity - error reading data:"+e.getLocalizedMessage());
    	}    
	    return highest;
    } 
    
    protected boolean isAmbiguity(Boat boat,int xPos, int yPos,int deltaAzimuth,double pxSize ,double pySize) throws IOException{
    	if(band.length==1)
    		return isAmbiguitySingleBand(boat, xPos, yPos, deltaAzimuth, pxSize, pySize);
    	else
    		return isAmbiguityMultipleBand(boat, xPos, yPos, deltaAzimuth, pxSize, pySize);
    }	
    /**
     * 
     * @param boat
     * @param xPos
     * @param yPos
     * @param deltaAzimuth azimuth correction in pixels
     * @param pxSize
     * @param pySize
     * @return
     * @throws IOException
     */
    protected boolean isAmbiguitySingleBand(Boat boat,int xPos, int yPos,int deltaAzimuth,double pxSize ,double pySize) throws IOException{
    	Window winUp=Window.createWindowFromAzimuth(xPos, yPos, deltaAzimuth,pxSize ,pySize,true);
       // logger.info(new StringBuffer().append("\nSearch Window start from: ").append(winUp.x).append(" ").append(winUp.sizeY).append("  D Azimuth:").append(deltaAzimuth).toString());
        int maxVal=getWindowMaxPixelValue(winUp.x,winUp.y,winUp.sizeX,winUp.sizeY,band[0]);
        
        String bb=sumoImage.getBandName(band[0]);
        int boatMaxValue=NumberUtils.max(boat.getStatMap().getMaxValue(bb));
        if(maxVal>(boatMaxValue*AZIMUT_FACTOR)){
        	return true;
        }else{
        	Window winDown=Window.createWindowFromAzimuth(xPos, yPos, deltaAzimuth,pxSize ,pySize,false);
        	maxVal=getWindowMaxPixelValue(winDown.x,winDown.y,winDown.sizeX,winDown.sizeY,band[0]);
        	if(maxVal>(boatMaxValue*AZIMUT_FACTOR)){
            	return true;
        	}	
        }	
        return false;
    }
    /**
     * 
     * @param boat
     * @param xPos
     * @param yPos
     * @param deltaAzimuth azimuth correction in pixels
     * @param pxSize
     * @param pySize
     * @return
     * @throws IOException
     */
    protected boolean isAmbiguityMultipleBand(Boat boat,int xPos, int yPos,int deltaAzimuth,double pxSize ,double pySize) throws IOException{
    	for(int i=0;i<band.length;i++){
	    	Window winUp=Window.createWindowFromAzimuth(xPos, yPos, deltaAzimuth,pxSize ,pySize,true);
	       // logger.info(new StringBuffer().append("\nSearch Window start from: ").append(winUp.x).append(" ").append(winUp.sizeY).append("  D Azimuth:").append(deltaAzimuth).toString());
	        int maxVal=getWindowMaxPixelValue(winUp.x,winUp.y,winUp.sizeX,winUp.sizeY,band[i]);
	        
	        String bb=sumoImage.getBandName(band[i]);
	        int boatMaxValue=NumberUtils.max(boat.getStatMap().getMaxValue(bb));
	        if(maxVal>(boatMaxValue*AZIMUT_FACTOR)){
	        	return true;
	        }else{
	        	Window winDown=Window.createWindowFromAzimuth(xPos, yPos, deltaAzimuth,pxSize ,pySize,false);
	        	maxVal=getWindowMaxPixelValue(winDown.x,winDown.y,winDown.sizeX,winDown.sizeY,band[i]);
	        	if(maxVal>(boatMaxValue*AZIMUT_FACTOR)){
	            	return true;
	        	}	
	        }	
	     
    	}
    	return false;
    }
    
    /**
     * 
     * @param boatList
     * @param image
     */
    public abstract void process(); 
    
    
    
 
    
    

}
