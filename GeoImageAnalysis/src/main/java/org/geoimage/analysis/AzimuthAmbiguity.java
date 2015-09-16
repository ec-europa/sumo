/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.analysis;

import org.geoimage.def.SarImageReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is used to filter false targets due to azimuthal ambiguities 
 * 
 * @author Pietro Argentieri 
 *
 */
public class AzimuthAmbiguity extends Ambiguity{
    private Logger logger= LoggerFactory.getLogger(AzimuthAmbiguity.class);

    
    public AzimuthAmbiguity(Boat[] boatList, SarImageReader image,int... band) {
    	super(boatList,image,band);
    }
    
    public AzimuthAmbiguity(Boat[]boatList, SarImageReader image, int windowSize, int numSteps, int... band) {
    	super(boatList,image,windowSize,numSteps,band);
    }
    
  
    
    @Override
    public void process() {
        int[] deltaAzimuth;

        try{
	        // scan through the list of boats
	        for (int i = 0; i < boatArray.length; i++) {
	            Boat myBoat = boatArray[i];
	            
	            int xPos = (int) myBoat.getPosx();
	            int yPos = (int) myBoat.getPosy();
	            
	            //is already in pixel
	            deltaAzimuth =sumoImage.getAmbiguityCorrection(xPos,yPos);
	            
	            double[] pixSize=sumoImage.getPixelsize();
	            
	            //first check to dAzimuth
	            if(isAmbiguity(myBoat,xPos, yPos, deltaAzimuth[0],pixSize[0] ,pixSize[1])){
	            	ambiguityboatlist.add(myBoat);
	            	myBoat.setAmbiguity(true);
	            	myBoat.setAmbiguityType(Boat.AMBIGUITY_TYPE_AZIMUTH);
	            //second check to dAzimuth*2	
	            }else if(isAmbiguity(myBoat,xPos, yPos, (deltaAzimuth[0]*2),pixSize[0] ,pixSize[1])){
	            	ambiguityboatlist.add(myBoat);
	            	myBoat.setAmbiguity(true);
	            	myBoat.setAmbiguityType(Boat.AMBIGUITY_TYPE_AZIMUTH);
	            }else if(isAmbiguity(myBoat,xPos, yPos, (deltaAzimuth[0]*3),pixSize[0] ,pixSize[1])){
	            	ambiguityboatlist.add(myBoat);
	            	myBoat.setAmbiguity(true);
	            	myBoat.setAmbiguityType(Boat.AMBIGUITY_TYPE_AZIMUTH);
	            }
	        }
        }catch(Exception e){
        	logger.error("Error processing Azimuth Ambiguity",e);
        }
    }
    
    

}
