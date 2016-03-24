/*
 * 
 */
package org.geoimage.impl.cosmo;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.geoimage.factory.GeoTransformFactory;
import org.geoimage.impl.Gcp;
import org.slf4j.LoggerFactory;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;

public class CosmoSkymedStreetMap extends AbstractCosmoSkymedImage {
	 private static org.slf4j.Logger logger=LoggerFactory.getLogger(CosmoSkymedStreetMap.class);
	
	
	public CosmoSkymedStreetMap(File file,String pathImg,String group){
		super(file,pathImg,group);
	}
	
	
	
}
