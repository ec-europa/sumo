/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.io;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.GeoTransform;
import org.geoimage.def.SarImageReader;
import org.geoimage.exception.GeoTransformException;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.util.PolygonOp;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;

/**
 *
 * @author thoorfr
 */
public class SimpleShapefileIO extends AbstractVectorIO {
	private static Logger logger= LoggerFactory.getLogger(SimpleShapefileIO.class);
    public static String CONFIG_URL = "url";
    private int margin=100;
    
    public SimpleShapefileIO() {
        super();
        margin = Integer.parseInt(java.util.ResourceBundle.getBundle("GeoImageViewer").getString("SimpleShapeFileIO.margin"));
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
    	 DefaultFeatureCollection collection = new DefaultFeatureCollection();        //GeometryFactory gf = new GeometryFactory();
         int id=0;
         for (Geometry geom : glayer.getGeometries()) {
             if (geom instanceof Point) {
                 Object[] data = new Object[ft.getDescriptors().size()];
                 System.arraycopy(glayer.getAttributes(geom).getValues(), 0, data, 1, data.length-1);
                 data[0] = geom;
                 SimpleFeature simplefeature = SimpleFeatureBuilder.build(ft, data, ""+id++);
                 collection.add(simplefeature);
             } else if (geom instanceof Polygon) {
                 //Object[] data = new Object[glayer.getSchema().length + 1];
                 //data[0] = geom;
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

   
    /**
     * 
     * @param name
     * @param glayer
     * @return
     */
    public static SimpleFeatureType createFeatureType(String name, GeometricLayer glayer) {
        try {

            // Tell this shapefile what type of data it will store
            // Shapefile handle only : Point, MultiPoint, MultiLineString, MultiPolygon
            String sch = "";
            String[] schema = glayer.getSchema();
            String[] types = glayer.getSchemaTypes();
            for (int i = 0; i < schema.length; i++) {
                sch += "," + schema[i] + ":" + types[i];
            }
            sch = sch.substring(1);
            String geomType = "MultiPolygon";
            if (glayer.getGeometries().get(0) instanceof Point) {
                geomType = "Point";
            }
            // "geom:" + geomType + ":srid=" +
            SimpleFeatureType featureType = DataUtilities.createType(geomType, sch);

            //to create other fields you can use a string like :
            // "geom:MultiLineString,FieldName:java.lang.Integer"
            // field name can not be over 10 characters
            // use a ',' between each field
            // field types can be : java.lang.Integer, java.lang.Long, 
            // java.lang.Double, java.lang.String or java.util.Date

            return featureType;
        } catch (SchemaException ex) {
        	logger.error(ex.getMessage(),ex);
        }
        return null;
    }
    
	
	private Polygon buildPolygon(GeoImageReader gir) throws ParseException, GeoTransformException, CQLException{
		   double h=gir.getHeight();
		   double w=gir.getWidth();
		
	       GeoTransform gt = gir.getGeoTransform();
           
           double[] x0 = gt.getGeoFromPixel(-margin, -margin);
           double[] x01 = gt.getGeoFromPixel(-margin, h/3); //image center coords
           double[] x02 = gt.getGeoFromPixel(-margin, h/2); //image center coords
           double[] x03 = gt.getGeoFromPixel(-margin, h*2/3); //image center coords
           double[] x1 = gt.getGeoFromPixel(-margin, margin + h);
           double[] x12 = gt.getGeoFromPixel(margin + w/2, margin +h); //image center coords
           double[] x2 = gt.getGeoFromPixel(margin + w, margin + h);
           double[] x21 = gt.getGeoFromPixel(margin + w, h*2/3); //image center coords
           double[] x22 = gt.getGeoFromPixel(margin + w, h/2); //image center coords
           double[] x23 = gt.getGeoFromPixel(margin + w, h/3); //image center coords
           double[] x3 = gt.getGeoFromPixel(margin + w, -margin);
           double[] x31 = gt.getGeoFromPixel(margin+w/2, -margin); //image center coords

           //poligono con punti di riferimento dell'immagine
           Polygon imageP=PolygonOp.createPolygon(x0,x01,x02,x03,x1,x12,x2,x21,x22,x23,x3,x31,x0);
           
           logger.debug("Polygon imageP isvalid:"+imageP.isValid());
           
           return imageP;
	}
	

    public GeometricLayer read(GeoImageReader gir) {
    	GeometricLayer glout=null;
        try {
        	
            //create a DataStore object to connect to the physical source 
            DataStore dataStore = DataStoreFinder.getDataStore(config);
            //retrieve a FeatureSource to work with the feature data
            SimpleFeatureSource featureSource = (SimpleFeatureSource) dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
            String geomName = featureSource.getSchema().getGeometryDescriptor().getLocalName();
            
            Polygon imageP=buildPolygon(gir);
            
            Envelope e=imageP.getBoundary().getEnvelopeInternal();
            String f=new StringBuilder("BBOX(").append(geomName).append(",")
            		.append(e.getMinX()).append(",")
            		.append(e.getMinY()).append(",")
            		.append(e.getMaxX()).append(",")
            		.append(e.getMaxY())
            		.append(")").toString();
            
            //String f2=new StringBuilder("CROSSES(").append(geomName).append(",").append(imageP.toText()).append(")").toString();
            
            Filter filter=CQL.toFilter(f);
            
            //filtro prendendo solo le 'features' nell'area di interesse
            FeatureCollection<?, ?> fc=featureSource.getFeatures(filter);
            if (fc.isEmpty()) {
                return null;
            }
            String[] schema = createSchema(fc.getSchema().getDescriptors());
            String[] types = createTypes(fc.getSchema().getDescriptors());

            String geoName = fc.getSchema().getGeometryDescriptor().getType().getName().toString();
            GeometricLayer out=GeometricLayer.createFromSimpleGeometry(imageP, geoName, dataStore, fc, schema, types);
            dataStore.dispose();
           
            
            fc=null;
            
            glout = GeometricLayer.createImageProjectedLayer(out, gir.getGeoTransform(),null);
            
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
        return glout;

    }
	
    
    
    @Override
    public void save(GeometricLayer layer, String projection,SarImageReader reader) {
    	GeoTransform transform=reader.getGeoTransform();
        try {
            layer = GeometricLayer.createWorldProjectedLayer(layer, transform, projection);
            String filename = ((URL) config.get(CONFIG_URL)).getPath();
            layername = filename.substring(filename.lastIndexOf(File.separator) + 1, filename.lastIndexOf("."));
            
            File newFile = new File(filename);
            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
            Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put("url", newFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);

            SimpleFeatureType ft = createFeatureType(layername, layer);
            ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            newDataStore.createSchema(ft);
            
            FeatureCollection<SimpleFeatureType,SimpleFeature>  features = createFeatures(ft, layer, projection);
            
            //writeToShapefile(newDataStore, features);
            exportToShapefile(ft, features, newFile.getName(), newFile.getParentFile());
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }
    
    private static void writeToShapefile(DataStore data, FeatureCollection<SimpleFeatureType,SimpleFeature> collection) {
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
    }

 // exportToShapefile start
    public DataStore exportToShapefile(SimpleFeatureType ft,FeatureCollection<SimpleFeatureType,SimpleFeature> collection, String typeName, File directory)
            throws IOException {
        // existing feature source from MemoryDataStore
        //SimpleFeatureSource featureSource = memory.getFeatureSource(typeName);
        //SimpleFeatureType ft = featureSource.getSchema();
        
        String fileName = typeName;//ft.getTypeName();
        File file = new File(directory, fileName);
        
        Map<String, java.io.Serializable> creationParams = new HashMap<String, java.io.Serializable>();
        creationParams.put("url", DataUtilities.fileToURL(file));
        
        FileDataStoreFactorySpi factory = FileDataStoreFinder.getDataStoreFactory("shp");
        DataStore dataStore = factory.createNewDataStore(creationParams);
        
        dataStore.createSchema(ft);
        
        // The following workaround to write out the prj is no longer needed
        // ((ShapefileDataStore)dataStore).forceSchemaCRS(ft.getCoordinateReferenceSystem());
        
        SimpleFeatureStore featureStore = (SimpleFeatureStore) dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
        
        Transaction t = new DefaultTransaction();
        try {
            //SimpleFeatureCollection collection = featureSource.getFeatures(); // grab all features
            featureStore.addFeatures(collection);
            t.commit(); // write it out
        } catch (IOException eek) {
            eek.printStackTrace();
            try {
                t.rollback();
            } catch (IOException doubleEeek) {
                // rollback failed?
            	doubleEeek.printStackTrace();
            }
        } finally {
            t.close();
        }
        return dataStore;
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

   
}
