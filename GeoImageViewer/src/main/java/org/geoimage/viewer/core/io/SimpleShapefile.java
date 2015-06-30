/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.io;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geoimage.def.GeoTransform;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.util.PolygonOp;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.process.vector.ClipProcess;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.PropertyDescriptor;
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

    public static GeometricLayer createIntersectedLayer(File shpInput,Polygon bbox,GeoTransform transform) {
    	GeometricLayer glout=null;
    	DataStore dataStore =null;
        try {
        	Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put("url", shpInput.toURI().toURL());
        	
            //create a DataStore object to connect to the physical source 
            dataStore = DataStoreFinder.getDataStore(params);
            //retrieve a FeatureSource to work with the feature data
            SimpleFeatureSource featureSource = (SimpleFeatureSource) dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
            
            Polygon imageP=bbox;
            	
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
            
           
            glout = GeometricLayer.createImageProjectedLayer(out, transform,null);
            
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
    public void save(File output, String projection,GeoTransform transform) {
    	exportLayer(output,this.layer,projection,transform);
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
        	if(transform!=null)
        		layer = GeometricLayer.createWorldProjectedLayer(layer, transform, projection);
            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
           
            SimpleFeatureSource source=layer.getFeatureSource();
            SimpleFeatureType ft =null;
            if(source!=null){
            	ft = SimpleFeatureTypeBuilder.retype( source.getSchema(), source.getSchema().getCoordinateReferenceSystem() );
            }else{
            	if(layer.getGeometryType().equals(GeometricLayer.POINT))
            		ft=createFeatureType(Point.class);
            	else if(layer.getGeometryType().equals(GeometricLayer.POLYGON))
            		ft=createFeatureType(Polygon.class);
            	else{
            		
            	}
            }	
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
     * Export features to a new shapefile using the map projection in which
     * they are currently displayed
     */
    public static void exportToShapefile(File shpOutput, FeatureCollection<SimpleFeatureType,SimpleFeature> featureCollection,SimpleFeatureType ft) throws Exception {
    	    //create a DataStore object to connect to the physical source 
       	 FileDataStoreFactorySpi factory = new ShapefileDataStoreFactory();
       	 Map map = Collections.singletonMap( "url", shpOutput.toURI().toURL() );
       	 DataStore data = factory.createNewDataStore( map );
       	 data.createSchema( ft );
         SimpleShapefile.exportToShapefile(data, featureCollection, ft);


    }
    /**
     * Export features to a new shapefile using the map projection in which
     * they are currently displayed
     */
     public static void exportToShapefile(DataStore newDataStore, FeatureCollection<SimpleFeatureType,SimpleFeature> featureCollection,SimpleFeatureType ft) throws Exception {
    	

        // carefully open an iterator and writer to process the results
        Transaction transaction = new DefaultTransaction();
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = newDataStore.getFeatureWriter(newDataStore.getTypeNames()[0],  transaction);
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
     
     
     public static void exportGeometriesToShapeFile(List<Geometry> geoms,File fileOutput,String geomType) throws IOException, SchemaException{
    	 //FeatureType ft=createFeatureType(Polygon.class);
    	 FileDataStoreFactorySpi factory = new ShapefileDataStoreFactory();
    	 Map map = Collections.singletonMap( "url", fileOutput.toURI().toURL() );
    	 DataStore data = factory.createNewDataStore( map );
    	 SimpleFeatureType featureType = DataUtilities.createType( "the_geom", "geom:"+geomType+",name:String,age:Integer,description:String" );
    	 data.createSchema( featureType );
    	 
    	 Transaction transaction = new DefaultTransaction();
         FeatureWriter<SimpleFeatureType, SimpleFeature> writer = data.getFeatureWriterAppend(data.getTypeNames()[0], transaction);

         SimpleFeatureBuilder featureBuilder=new SimpleFeatureBuilder(featureType);
         try {
	         int fid=0;
	         for(Geometry g:geoms){
	        	 featureBuilder.add("the_geom");
	        	 featureBuilder.add(g);
	        	 SimpleFeature sf=featureBuilder.buildFeature(""+fid++);
	        	 SimpleFeature sfout=writer.next();
	        	 sfout.setAttributes( sf.getAttributes() );
	             sfout.setDefaultGeometry( g);       
	        	 writer.write();
	    	 }
	         transaction.commit();
	         logger.info("Export to shapefile complete:"+ fileOutput.getAbsolutePath());
         } catch (Exception problem) {
             problem.printStackTrace();
             transaction.rollback();
             logger.error("Export to shapefile failed",problem );
         } finally {
             writer.close();
             transaction.close();
         }
    	 
/*
         FileWriter wr=new FileWriter(fileOutput);
    	 
    	 for(Geometry g:geoms){
    		 WKTWriter writer=new WKTWriter();
    		 writer.write(g, wr);
    	 }
    	 wr.close();*/
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
                  SimpleFeature simplefeature = SimpleFeatureBuilder.build(ft, data, ""+id);
                  collection.add(simplefeature);
              } else if (geom instanceof Polygon) {
                  Object[] data = new Object[glayer.getSchema().length];
                  System.arraycopy(glayer.getAttributes(geom).getValues(), 0, data, 0, data.length );
                  SimpleFeature simplefeature = SimpleFeatureBuilder.build(ft, data, ""+id);
                  collection.add(simplefeature);
                  data = null;
              } else if (geom instanceof LineString) {
                  Object[] data = new Object[glayer.getSchema().length + 1];
                  data[0] = geom;
                  System.arraycopy(glayer.getAttributes(geom).getValues(), 0, data, 1, data.length - 1);
                  SimpleFeature simplefeature = SimpleFeatureBuilder.build(ft, data, ""+id);
                  collection.add(simplefeature);
              }
              id++;
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
    
    
    private static SimpleFeatureType createFeatureType(Class geoClass) {

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

	
    public static void main(String[] args){
    	double cc[][]=new double[][]{{200.0,200.0},{500.0,200.0},{500.0,500.0},{200.0,500.0},{200.0,200.0}};
    	
    	try {
    		Polygon p=PolygonOp.createPolygon(cc);
        	List<Geometry>geoms=new ArrayList<>();
        	geoms.add(p);
			SimpleShapefile.exportGeometriesToShapeFile(geoms,new File("F:\\SumoImgs\\export\\test.shp") , "Polygon");
		} catch (IOException | SchemaException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	
    }
    
  
}
