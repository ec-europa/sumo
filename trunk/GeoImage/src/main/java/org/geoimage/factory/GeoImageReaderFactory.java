package org.geoimage.factory;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import jrc.it.xml.wrapper.SumoJaxbSafeReader;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5Group;

import org.geoimage.def.GeoImageReader;
import org.geoimage.impl.cosmo.CosmoSkymedImage;
import org.geoimage.impl.envi.EnvisatImage_SLC;
import org.geoimage.impl.radarsat.Radarsat1Image;
import org.geoimage.impl.radarsat.Radarsat2Image;
import org.geoimage.impl.radarsat.Radarsat2Image_SLC;
import org.geoimage.impl.s1.Sentinel1;
import org.geoimage.impl.s1.Sentinel1GRD;
import org.geoimage.impl.s1.Sentinel1SLC;
import org.geoimage.impl.tsar.TerrasarXImage;
import org.geoimage.impl.tsar.TerrasarXImage_SLC;
import org.slf4j.LoggerFactory;


public class GeoImageReaderFactory {

    public final static List<String> FORMATS = new Vector<String>();
    private static org.slf4j.Logger logger=LoggerFactory.getLogger(GeoImageReaderFactory.class);
    
    static {
    	try{
    		String[] temp = java.util.ResourceBundle.getBundle("geoimage").getString("formats").split(",");
    		for (String f : temp) {
    			addFormat(f);
    		}
    	}catch(Exception e){
    		logger.error(e.getMessage(), e);
    	}
    }

    /**
     * 
     * @param gir
     * @return
     */
    public static GeoImageReader cloneReader(GeoImageReader gir){
    	//TODO : add "if" for other reader with multiple images (cosmoskymed)
    	GeoImageReader clone=null;
    	if(gir instanceof Sentinel1SLC){
			SumoJaxbSafeReader safeReader = ((Sentinel1SLC) gir).getSafeReader();
			clone=new Sentinel1SLC(safeReader,((Sentinel1SLC) gir).getSwath());
			clone.initialise(safeReader.getSafefile());
    	}else {
    		clone=createReaderForName(gir.getFilesList()[0]).get(0);
    	}
    	return clone;
    }
    
    
    /**
     * try to create the correct reader starting from file name
     * @param file
     * @return
     */
    public static List<GeoImageReader> createReaderForName(String file){
    	List<GeoImageReader> girList=new ArrayList<GeoImageReader>();

        try {
        	File f=new File(file);
	        String parent=f.getParent();
	        //le cosmosky possono avere immagini multiple (mosaic)
	        if(parent.contains("CSKS")||file.contains("CSKS")){
	    		H5File h5file = new H5File(file, H5File.READ);
	    		H5Group group=((H5Group)h5file.get("/"));
	        	List<HObject>hObjs=group.getMemberList();
	        	Iterator<HObject> it=hObjs.iterator();

	        	while(it.hasNext()){
	        		HObject obj=it.next();
	        		CosmoSkymedImage cosmo;
	        		if(obj.getName().equals("MBI"))
	        			cosmo=new CosmoSkymedImage(obj.getName(),null);
	        		else
	        			cosmo=new CosmoSkymedImage(obj.getName()+"/SBI",obj.getName());

	                if (cosmo.initialise(f)) {
	                    logger.info("Successfully reading {0} as {1}...", new Object[]{file,cosmo.getClass()});
	                }
	                if(hObjs.size()>1)
	                	cosmo.setContainsMultipleImage(true);
	                girList.add(cosmo);
	        	}
	        }else if(parent.contains("S1A")||parent.contains("S1B")){//sentinel 1
	        	SumoJaxbSafeReader safeReader=new SumoJaxbSafeReader(f.getAbsolutePath());
	        	boolean multipleImages=false;
	        	
	        	//for multiple images. For SLC product we can have 1 images for each sub-swat and for each polarization
	        	//we create 1 reader for each sub-swath
        		String[] swath=safeReader.getSwaths();
        		if(swath.length>1)
        			multipleImages=true;
        		
        		Sentinel1 sentinel=null;
        		for(String sw:swath){
        			if(parent.contains("SLC_"))
        				sentinel=new Sentinel1SLC(safeReader,sw);
        			else
        				sentinel=new Sentinel1GRD(safeReader,sw);
        			
        			sentinel.setContainsMultipleImage(multipleImages);
        			if (sentinel.initialise(f)) {
	                    logger.info("Successfully reading {0} as {1}...", new Object[]{file,sentinel.getClass()});
	                }else{
	                	logger.warn("Problem reading {0} as {1}...", new Object[]{file,sentinel.getClass()});
	                }
        			girList.add(sentinel);
        		}	
        	}else{
        		GeoImageReader gir=null;
        		if(parent.contains("TDX1_SAR__MGD")){
        			gir= new TerrasarXImage();
        		}else if(parent.contains("TSX1_SAR__MGD")){
	        		gir= new TerrasarXImage();
	        	}else if(parent.contains("TSX1_SAR__SSC")||parent.contains("TDX1_SAR__SSC")){
	        		gir=new TerrasarXImage_SLC();
	        	}else if(parent.contains("RS2")&& parent.contains("SLC")){
	        		gir=new Radarsat2Image_SLC();
	        	}else if(parent.contains("RS2")&& !parent.contains("SLC")){
	        		gir=new Radarsat2Image();
	        	}else if(parent.contains("RS1")){
	        		gir=new Radarsat1Image();
	        	}else if(parent.contains("ASA_")){
	        		gir=new EnvisatImage_SLC();
	        	}else{
	        		return null;
	        	}
	            if (gir.initialise(f)) {
	               logger.info("Successfully reading {0} as {1}...", new Object[]{file, gir.getClass()});
	                girList.add(gir);
	            }else{
	            	girList=null;
	            }
        	}
        } catch (Exception ex) {
            logger.error("Error reading:"+file,  ex);
            girList=null;
        }
        finally{
           return girList;
        }
    }

    /*
     *
     * @param file
     * @param format
     * @return
     *
    public static List<GeoImageReader> create(String file, String format) {
    	List<GeoImageReader> girList=new ArrayList<GeoImageReader>();

        try {
        	File f=new File(file);
    		GeoImageReader gir;
	        Class<?> clazz = Class.forName(format);
	        Object o= clazz.newInstance();
	        gir = (GeoImageReader)o;
            if (gir.initialise(f)) {
                logger.info("Successfully reading {0} as {1}...", new Object[]{file, format});
                girList.add(gir);
            }else{
            	girList=null;
            }
        } catch (Exception ex) {
            logger.error( "Class " + format + " not found.", ex);
            girList=null;
        }
        finally{
           return girList;
        }

    }*/

    private static void addFormat(String f) {
        FORMATS.add(f);
    }
}

