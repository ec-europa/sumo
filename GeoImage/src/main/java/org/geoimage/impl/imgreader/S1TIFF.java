/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.impl.imgreader;

import java.io.File;

import jrc.it.safe.reader.xpath.object.wrapper.BurstInformation;

/**
 * This is a convenience class that warp a tiff image to easily use in the case
 * of one geotiff per band (like radarsat 2 images)
 * @author thoorfr
 */
public class S1TIFF extends TIFF{
	BurstInformation burstInfo;

   

    /**
     * 
     * @param imageFile
     * @param band form files with multiple band
     */
	public S1TIFF(File imageFile,int band,BurstInformation burstInfo) {
    	super(imageFile,band);
    	this.burstInfo=burstInfo;
    }

	
	
	public void deBurstOperation(){
		//this.image;
	}
	
	
	
	
}
