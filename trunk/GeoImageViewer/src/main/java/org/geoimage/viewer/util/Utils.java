/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author thoorfr
 */
public class Utils {

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
    
    public static void createShapeFileFromPolygons(String file,List<Geometry>geometries){
    	
    	try{
	        File newFile = new File(file);
	
	        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
	
	        Map<String, Serializable> params = new HashMap<String, Serializable>();
	        params.put("url", newFile.toURI().toURL());
	        params.put("create spatial index", Boolean.TRUE);
	
	        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
	        SimpleFeatureType type=createFeatureType();
	        
	        newDataStore.createSchema(type);
	
	        /*
	         * Write the features to the shapefile
	         */
	        Transaction transaction = new DefaultTransaction("create");
	
	        String typeName = newDataStore.getTypeNames()[0];
	        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
	
	        if (featureSource instanceof SimpleFeatureStore) {
	            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
	
	            List<SimpleFeature> features=createfeatures(type,geometries);
	            
	            /*
	             * SimpleFeatureStore has a method to add features from a
	             * SimpleFeatureCollection object, so we use the ListFeatureCollection
	             * class to wrap our list of features.
	             */
	            SimpleFeatureCollection collection = new ListFeatureCollection(type, features);
	            featureStore.setTransaction(transaction);
	            try {
	                featureStore.addFeatures(collection);
	                transaction.commit();
	
	            } catch (Exception problem) {
	                problem.printStackTrace();
	                transaction.rollback();
	
	            } finally {
	                transaction.close();
	            }
	        } else {
	            System.out.println(typeName + " does not support read/write access");
	        }
    	}catch(Exception e){
    		e.printStackTrace();
    	}   
    }
    
    private static SimpleFeatureType createFeatureType() {

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Location");
        builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference system

        // add attributes in order
        builder.add("Polygon", Polygon.class);
        builder.length(15).add("Name", String.class); // <- 15 chars width for name field
        builder.add("Number", Integer.class);

        // build the type
        final SimpleFeatureType LOCATION = builder.buildFeatureType();

        return LOCATION;
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
    
    
}
