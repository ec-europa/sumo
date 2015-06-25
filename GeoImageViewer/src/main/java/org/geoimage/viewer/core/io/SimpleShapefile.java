/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.io;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.GeoTransform;
import org.geoimage.def.SarImageReader;
import org.geoimage.exception.GeoTransformException;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.util.PolygonOp;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTS;
import org.geotools.process.vector.ClipProcess;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;

/**
 *
 * @author thoorfr
 */
public class SimpleShapefile extends AbstractVectorIO{
	private static Logger logger= LoggerFactory.getLogger(SimpleShapefile.class);
    public static String CONFIG_URL = "url";
    
    
    private File shpInput=null;
    private GeoTransform transform=null;
    private GeometricLayer layer=null;
    
    
    public SimpleShapefile(File input,GeoTransform transform) { 
    	this.shpInput=input;
    	this.transform=transform;
    }

    @SuppressWarnings("unused")
	private static FileDataStore createDataStore(String filename, SimpleFeatureType ft, String projection) throws Exception {
       	File file = new File(filename);
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(file);
     // Tell the DataStore what type of Coordinate Reference System (CRS) to use
        if (projection != null) {
            ((ShapefileDataStore)dataStore).forceSchemaCRS(CRS.decode(projection));
        }
        
        return dataStore;
    }
	
	
	
	@Override
	public void read() {
		this.layer=createLayer(this.shpInput,transform);
	}
		
	
	public static GeometricLayer createLayer(File shpInput,GeoTransform transform) {
    	GeometricLayer glout=null;
        try {
        	Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put("url", shpInput.toURI().toURL());
        	
            //create a DataStore object to connect to the physical source 
            DataStore dataStore = DataStoreFinder.getDataStore(params);
            //retrieve a FeatureSource to work with the feature data
            SimpleFeatureSource featureSource = (SimpleFeatureSource) dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
            
    
            SimpleFeatureCollection fc=featureSource.getFeatures();

            if (fc.isEmpty()) {
                return null;
            }
            String[] schema = createSchema(fc.getSchema().getDescriptors());
            String[] types = createTypes(fc.getSchema().getDescriptors());

            String geoName = fc.getSchema().getGeometryDescriptor().getType().getName().toString();
            
            GeometricLayer out=GeometricLayer.createLayerFromFeatures(geoName, dataStore, fc, schema, types);
            dataStore.dispose();
           
            glout = GeometricLayer.createImageProjectedLayer(out, transform,null);
            
            
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
        return glout;

    }

    public static GeometricLayer createIntersectedLayer(File shpInput,SarImageReader gir) {
    	GeometricLayer glout=null;
    	DataStore dataStore =null;
        try {
        	Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put("url", shpInput.toURI().toURL());
        	
            //create a DataStore object to connect to the physical source 
            dataStore = DataStoreFinder.getDataStore(params);
            //retrieve a FeatureSource to work with the feature data
            SimpleFeatureSource featureSource = (SimpleFeatureSource) dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
            
            Polygon imageP=gir.getBbox();
            
            ClipProcess clip=new ClipProcess();
            SimpleFeatureCollection fc=clip.execute(featureSource.getFeatures(), imageP,true);

           /* CoordinateReferenceSystem worldCRS = DefaultGeographicCRS.WGS84;
            ReferencedEnvelope env = new ReferencedEnvelope(imageP.getEnvelopeInternal(),worldCRS);
            RectangularClipProcess clip=new RectangularClipProcess();
    		SimpleFeatureCollection fc=clip.execute(featureSource.getFeatures(), env, true);
    		*/
            if (fc.isEmpty()) {
                return null;
            }
            String[] schema = createSchema(fc.getSchema().getDescriptors());
            String[] types = createTypes(fc.getSchema().getDescriptors());

            String geoName = fc.getSchema().getGeometryDescriptor().getType().getName().toString();
            
            GeometricLayer out=GeometricLayer.createFromSimpleGeometry(imageP, geoName, dataStore, fc, schema, types);
            
           
            glout = GeometricLayer.createImageProjectedLayer(out, gir.getGeoTransform(),null);
            
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }finally{
        	dataStore.dispose();
        }
        return glout;

    }
	
    /**
     * 
     * @param output
     * @param layer
     * @param projection
     * @param reader
     */
    public static void exportLayer(File output,GeometricLayer layer, String projection,GeoTransform transform) {
        try {
            layer = GeometricLayer.createWorldProjectedLayer(layer, transform, projection);
            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
           
            SimpleFeatureType ft = SimpleFeatureTypeBuilder.retype( layer.getFeatureSource().getSchema(), layer.getFeatureSource().getSchema().getCoordinateReferenceSystem() );

            Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put("url", output.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
            ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            newDataStore.createSchema(ft);
            
            FeatureCollection<SimpleFeatureType,SimpleFeature>  features = createFeatures(ft, layer, projection);
            
            exportToShapefile(newDataStore,features,ft);
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }
    
    /**
     * 
     * @param output
     * @param layer
     * @param projection
     * @param reader
     */
    public void save(File output, String projection,GeoTransform transform) {
    	exportLayer(output,this.layer,projection,transform);
    }
    
    /**
     * Export features to a new shapefile using the map projection in which
     * they are currently displayed
     */
     public static void save(FeatureCollection<?, ?> featureCollection,SimpleFeatureSource source,File fileOut) throws Exception {
        SimpleFeatureType schema = source.getSchema();


        // set up the math transform used to process the data
        CoordinateReferenceSystem dataCRS = schema.getCoordinateReferenceSystem();
        CoordinateReferenceSystem worldCRS = schema.getCoordinateReferenceSystem();
        boolean lenient = true; // allow for some error due to different datums
        MathTransform transform = CRS.findMathTransform( dataCRS, worldCRS, lenient );
        

        // And create a new Shapefile with a slight modified schema
        DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
        Map<String, Serializable> create = new HashMap<String, Serializable>();
        create.put("url", fileOut.toURI().toURL());
        create.put("create spatial index", Boolean.FALSE);        

        DataStore newDataStore = factory.createNewDataStore(create);
        SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype( schema, worldCRS );
        newDataStore.createSchema( featureType );
        

        // carefully open an iterator and writer to process the results
        Transaction transaction = new DefaultTransaction("Reproject");
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = newDataStore.getFeatureWriterAppend( newDataStore.getTypeNames()[0], transaction);
        FeatureIterator<SimpleFeature> iterator = (FeatureIterator<SimpleFeature>) featureCollection.features();                
        try {
            while( iterator.hasNext() ){
                // copy the contents of each feature and transform the geometry
                SimpleFeature feature = iterator.next();
                SimpleFeature copy = writer.next();
                copy.setAttributes( feature.getAttributes() );

                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                Geometry geometry2 = JTS.transform(geometry, transform);
                
                copy.setDefaultGeometry( geometry2 );                
                writer.write();
            }
            transaction.commit();
            JOptionPane.showMessageDialog(null, "Export to shapefile complete" );
        } catch (Exception problem) {
            problem.printStackTrace();
            transaction.rollback();
            JOptionPane.showMessageDialog(null, "Export to shapefile failed" );
        } finally {
            writer.close();
            iterator.close();
            transaction.close();
        }
    }
		 
	
	        	
	        	
    

    /**
     * Export features to a new shapefile using the map projection in which
     * they are currently displayed
     */
     public static void exportToShapefile(DataStore newDataStore, FeatureCollection<SimpleFeatureType,SimpleFeature> featureCollection,SimpleFeatureType ft) throws Exception {
    	

        // carefully open an iterator and writer to process the results
        Transaction transaction = new DefaultTransaction();
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = newDataStore.getFeatureWriterAppend(newDataStore.getTypeNames()[0], transaction);
        FeatureIterator<SimpleFeature> iterator = featureCollection.features();                
        try {
            while( iterator.hasNext() ){
                SimpleFeature feature = iterator.next();
                SimpleFeature copy = writer.next();
                copy.setAttributes( feature.getAttributes() );
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                copy.setDefaultGeometry( geometry.buffer(0));                
                writer.write();
            }
            transaction.commit();
            logger.info("Export to shapefile complete" );
        } catch (Exception problem) {
            problem.printStackTrace();
            transaction.rollback();
            logger.error("Export to shapefile failed",problem );
        } finally {
            writer.close();
            iterator.close();
            transaction.close();
        }
    }
     
     /**
      * 
      * @param ft
      * @param glayer
      * @param projection
      * @param gt
      * @return
      * @throws Exception
      */
     public static FeatureCollection<SimpleFeatureType,SimpleFeature>  createFeatures(SimpleFeatureType ft, GeometricLayer glayer, String projection) throws Exception {
     	 DefaultFeatureCollection collection = new DefaultFeatureCollection();        
          int id=0;
          for (Geometry geom : glayer.getGeometries()) {
              if (geom instanceof Point) {
                  Object[] data = new Object[ft.getDescriptors().size()];
                  System.arraycopy(glayer.getAttributes(geom).getValues(), 0, data, 1, data.length-1);
                  data[0] = geom;
                  SimpleFeature simplefeature = SimpleFeatureBuilder.build(ft, data, ""+id++);
                  collection.add(simplefeature);
              } else if (geom instanceof Polygon) {
                  Object[] data = new Object[glayer.getSchema().length];
                  System.arraycopy(glayer.getAttributes(geom).getValues(), 0, data, 0, data.length );
                  SimpleFeature simplefeature = SimpleFeatureBuilder.build(ft, data, ""+id++);
                  collection.add(simplefeature);
                  data = null;
              } else if (geom instanceof LineString) {
                  Object[] data = new Object[glayer.getSchema().length + 1];
                  data[0] = geom;
                  System.arraycopy(glayer.getAttributes(geom).getValues(), 0, data, 1, data.length - 1);
                  SimpleFeature simplefeature = SimpleFeatureBuilder.build(ft, data, ""+id++);
                  collection.add(simplefeature);
              }
          }
         return collection;
     }

    private static String[] createSchema(Collection<PropertyDescriptor> attributeTypes) {
        String[] out = new String[attributeTypes.size()];
        int i = 0;
        for (PropertyDescriptor at : attributeTypes) {
            out[i++] = at.getName().toString();
        }
        return out;
    }

    private static String[] createTypes(Collection<PropertyDescriptor> attributeTypes) {
        String[] out = new String[attributeTypes.size()];
        int i = 0;
        for (PropertyDescriptor at : attributeTypes) {
            out[i++] = at.getType().getBinding().getName();
        }
        return out;
    }

	

    /* private static void writeToShapefile(DataStore data, FeatureCollection<SimpleFeatureType,SimpleFeature> collection) {
	SimpleFeatureStore store = null;
	
    DefaultTransaction transaction = new DefaultTransaction();
    try {
        String featureName = data.getTypeNames()[0];
        // Tell it the name of the shapefile it should look for in our DataStore
        store = (SimpleFeatureStore) (data.getFeatureSource(featureName));
        // Then set the transaction for that FeatureStore
        store.setTransaction(transaction);

    } catch (IOException e) {
        e.printStackTrace();
    }

    try {
        store.addFeatures(collection);
        transaction.commit();
        transaction.close();
    } catch (Exception ex) {
        try {
            transaction.rollback();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}*/
}
