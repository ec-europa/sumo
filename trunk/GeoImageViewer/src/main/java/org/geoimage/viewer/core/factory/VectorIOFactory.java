package org.geoimage.viewer.core.factory;

import java.util.Map;

import org.geoimage.viewer.core.io.AbstractVectorIO;
import org.geoimage.viewer.core.io.GenericCSVIO;
import org.geoimage.viewer.core.io.GeoRSSIO;
import org.geoimage.viewer.core.io.GmlIO;
import org.geoimage.viewer.core.io.KmlIO;
import org.geoimage.viewer.core.io.PostgisIO;
import org.geoimage.viewer.core.io.SimpleShapefileIO;
import org.geoimage.viewer.core.io.SumoXMLWriter;
import org.geoimage.viewer.core.io.SumoXmlIOOld;

public class VectorIOFactory {
	public static final int SIMPLE_SHAPEFILE = 0;//"SimpleShapeFile";
    public static final int GML = 1;//"GML";
    public static final int POSTGIS = 2;//"Postgis";
    public static final int SUMO_OLD = 3;//"Sumo";
    public static final int CSV = 4;//"Csv";
    public static final int GENERIC_CSV = 5;//"Generic Csv";
    public static final int KML = 6;//"Kml";
    public static final int GEORSS = 7;//"GeoRss";
    
    public static final int SUMO = 8;//"New Sumo XML";
	
	
	/**
     * 
     * @param type
     * @param config
     * @param gir
     * @return
     */
    public static AbstractVectorIO createVectorIO(int type, Map<String, Object> config) {
    	AbstractVectorIO vio=null;
    	switch(type){
	    	case SIMPLE_SHAPEFILE: {
	            vio = new SimpleShapefileIO();
	            break;
	        } 
	    	case POSTGIS: {
	            vio = new PostgisIO();
	            break;
	    	} 
	    	case GML: {
	            vio = new GmlIO();
	            break;
	        }
	    	case SUMO: {
	            vio = new SumoXMLWriter();
	            break;
	        }
	    	case SUMO_OLD: {
	            vio = new SumoXmlIOOld();
	            break;
	        }
	    	case CSV: {
	            vio = new GenericCSVIO();
	            break;
	        } 
	    	case GENERIC_CSV :{
	            vio = new GenericCSVIO();
	            break;
	        } 
	    	case KML:{
	            vio = new KmlIO();
	            break;
	        }
	    	case GEORSS: {
	            vio = new GeoRSSIO();
	            break;
	        }
    	}
        vio.setConfig(config);
        return vio;
    }
}
