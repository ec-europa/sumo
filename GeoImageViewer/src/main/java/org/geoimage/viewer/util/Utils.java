/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geoimage.def.SarImageReader;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author thoorfr
 */
public class Utils {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(Utils.class);
	
    public static String toString(Map map) {
        String out = "";
        Set keys = map.keySet();
        for (Object o : keys) {
            out += o.toString() + "=";
            Object val = map.get(o);
            if (val == null) {
                out += "null\n";
            } else if (val instanceof String[]) {
                out += "[";
                for (String a : ((String[]) val)) {
                    out += a + ";";
                }
                out += "]\n";
            } else if (val instanceof int[]) {
                out += "[";
                for (int a : ((int[]) val)) {
                    out += a + ";";
                }
                out += "]\n";
            } else if (val instanceof float[]) {
                out += "[";
                for (float a : ((float[]) val)) {
                    out += a + ";";
                }
                out += "]\n";
            } else if (val instanceof boolean[]) {
                out += "[";
                for (boolean a : ((boolean[]) val)) {
                    out += a + ";";
                }
                out += "]\n";
            } else if (val instanceof double[]) {
                out += "[";
                for (double a : ((double[]) val)) {
                    out += a + ";";
                }
                out += "]\n";
            } else if (val instanceof long[]) {
                out += "[";
                if (((long[]) val).length > 500) {
                    out += ((long[]) val).length + " elements";
                } else {
                    for (int i = 0; i < ((long[]) val).length; i++) {
                        out += ((long[]) val)[i] + ";";
                    }
                }
                out += "]\n";
            } else {
                out += val.toString() + "\n";
            }
        }
        return out;
    }
    
    
    
    
    public static void writeGeometriesInTmpFile(String tmpPathFile,List<Geometry>geometries){
    	try{
	    	String aa=geometries.toString();
	        File fo=new File(tmpPathFile);
	        if(!fo.exists())
	        	fo.createNewFile();
	        FileOutputStream ff=new FileOutputStream(fo);
	        ff.write(aa.getBytes());
	        ff.flush();
	        ff.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}   
    }
    
    
    
    public static SimpleFeatureType createFeatureType(Class geoClass) {

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Location");
        builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference system

        // add attributes in order
        builder.add(geoClass.getSimpleName(), geoClass);
        builder.length(15).add("Name", String.class); // <- 15 chars width for name field
        builder.add("Number", Integer.class);

        // build the type
        final SimpleFeatureType ft = builder.buildFeatureType();

        return ft;
    }
    
    public static List<SimpleFeature> createfeatures(SimpleFeatureType type,List<Geometry>geometries) {
        List<SimpleFeature> features = new ArrayList<SimpleFeature>();        

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);

        for(Geometry g : geometries){
                featureBuilder.add(g);
                SimpleFeature feature = featureBuilder.buildFeature(null);
                features.add(feature);
        }
        return features;
    }   
    
    /**
     * 
     * @param reader
     * @param thresholdArrayValues
     * @return
     */
    public static String[] getStringThresholdsArray(SarImageReader reader,float[] thresholdArrayValues){
    	int numberOfBands=reader.getNBand();
	    final String[] thresholds = new String[numberOfBands];
	    //management of the strings added at the end of the layer name in order to remember the used threshold
	    for (int bb = 0; bb < numberOfBands; bb++) {
	        if (reader.getBandName(bb).equals("HH") || reader.getBandName(bb).equals("H/H")) {
	            thresholds[bb] = "" + thresholdArrayValues[0];
	        } else if (reader.getBandName(bb).equals("HV") || reader.getBandName(bb).equals("H/V")) {
	            thresholds[bb] = "" + thresholdArrayValues[1];
	        } else if (reader.getBandName(bb).equals("VH") || reader.getBandName(bb).equals("V/H")) {
	            thresholds[bb] = "" + thresholdArrayValues[2];
	        } else if (reader.getBandName(bb).equals("VV") || reader.getBandName(bb).equals("V/V")) {
	            thresholds[bb] = "" + thresholdArrayValues[3];
	        }
	    }
	    return thresholds;
    }    
    
}
