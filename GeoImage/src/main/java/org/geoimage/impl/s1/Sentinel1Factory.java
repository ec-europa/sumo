/*
 * 
 */
package org.geoimage.impl.s1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gdal.gdal.gdal;
import org.geoimage.def.GeoImageReader;
import org.geoimage.viewer.core.SumoPlatform;
import org.jdom2.JDOMException;
import org.jrc.sumo.configuration.PlatformConfiguration;

import it.geosolutions.imageio.gdalframework.GDALUtilities;
import jrc.it.xml.wrapper.SumoJaxbSafeReader;


public class Sentinel1Factory {
    private static Logger logger=LogManager.getLogger(Sentinel1Factory.class);



	public static List<GeoImageReader> instanceS1Reader(File f,String geoAlgorithm) throws JDOMException, IOException, JAXBException{
		List<GeoImageReader> girList=new ArrayList<GeoImageReader>();
		final SumoJaxbSafeReader safeReader=new SumoJaxbSafeReader(f.getAbsolutePath());
    	boolean multipleImages=false;

    	//for multiple images. For SLC product we can have 1 images for each sub-swat and for each polarization
    	//we create 1 reader for each sub-swath
		String[] swath=safeReader.getSwaths();
		if(swath.length>1)
			multipleImages=true;

		Sentinel1 sentinel=null;
		String parent=f.getParent();
		for(String sw:swath){
			if(parent.contains("SLC_")){
				sentinel=new Sentinel1SLC(sw,f,geoAlgorithm);
			}else{
			   try{
				   if(PlatformConfiguration.getConfigurationInstance().getUseGdalForS1(false)&&GDALUtilities.isGDALAvailable()){
					   gdal.AllRegister();
					   sentinel=new GDALSentinel1(sw,f,geoAlgorithm);
				   }else{
					   sentinel=new Sentinel1GRD(sw,f,geoAlgorithm);
				   }
			   }catch(Exception e){
				    sentinel=new Sentinel1GRD(sw,f,geoAlgorithm);
			   }
			}
			sentinel.setContainsMultipleImage(multipleImages);
			if (sentinel.initialise()) {
                logger.info("Successfully reading {0} as {1}...", new Object[]{f.getName(),sentinel.getClass()});
            }else{
            	logger.warn("Problem reading {0} as {1}...", new Object[]{f.getName(),sentinel.getClass()});
            }
			girList.add(sentinel);
		}


		return girList;
	}
}
