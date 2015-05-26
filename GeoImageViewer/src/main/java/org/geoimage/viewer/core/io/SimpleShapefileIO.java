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
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.GeometricLayer;
import org.geoimage.viewer.util.PolygonOp;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 *
 * @author thoorfr
 */
public class SimpleShapefileIO extends AbstractVectorIO {
	private static Logger logger= LoggerFactory.getLogger(SimpleShapefileIO.class);
    public static String CONFIG_URL = "url";

    public SimpleShapefileIO() {
        super();
    }

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

    public GeometricLayer read(GeoImageReader gir) {
        try {
            GeometricLayer out = null;
            int margin = Integer.parseInt(java.util.ResourceBundle.getBundle("GeoImageViewer").getString("SimpleShapeFileIO.margin"));
            //margin=0;
            //create a DataStore object to connect to the physical source 
            DataStore dataStore = DataStoreFinder.getDataStore(config);
            //retrieve a FeatureSource to work with the feature data
            SimpleFeatureSource featureSource = (SimpleFeatureSource) dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
            String geomName = featureSource.getSchema().getGeometryDescriptor().getLocalName();
            //GcpsGeoTransform gt =(GcpsGeoTransform) gir.getGeoTransform();
            GeoTransform gt = gir.getGeoTransform();
            //FilterFactory ff = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());
            double[] x0;
            double[] x1;
            double[] x2;
            double[] x3;
            double[] x01; //image center coords
            double[] x02; //image center coords
            double[] x03; //image center coords
            double[] x12; //image center coords
            double[] x21; //image center coords
            double[] x22; //image center coords
            double[] x23; //image center coords
            double[] x31; //image center coords

            x0 = gt.getGeoFromPixel(-margin, -margin);
            x01 = gt.getGeoFromPixel(-margin, gir.getHeight()/3);
            x02 = gt.getGeoFromPixel(-margin, gir.getHeight()/2);
            x03 = gt.getGeoFromPixel(-margin, gir.getHeight()*2/3);

            x1 = gt.getGeoFromPixel(-margin, margin + gir.getHeight());
            x12 = gt.getGeoFromPixel(margin + gir.getWidth()/2, margin +gir.getHeight());
           
            x2 = gt.getGeoFromPixel(margin + gir.getWidth(), margin + gir.getHeight());
            x21 = gt.getGeoFromPixel(margin + gir.getWidth(), gir.getHeight()*2/3);
            x22 = gt.getGeoFromPixel(margin + gir.getWidth(), gir.getHeight()/2);
            x23 = gt.getGeoFromPixel(margin + gir.getWidth(), gir.getHeight()/3);
            
            x3 = gt.getGeoFromPixel(margin + gir.getWidth(), -margin);
            x31 = gt.getGeoFromPixel(margin+gir.getWidth()/2, -margin);

            double minx = Math.min(x0[0], Math.min(x01[0], Math.min(x02[0], Math.min(x03[0], Math.min(x1[0], Math.min(x12[0], Math.min(x2[0], Math.min(x21[0], Math.min(x22[0], Math.min(x23[0], Math.min(x3[0], x31[0])))))))))));
            double maxx = Math.max(x0[0], Math.max(x01[0], Math.max(x02[0], Math.max(x03[0], Math.max(x1[0], Math.max(x12[0], Math.max(x2[0], Math.max(x21[0], Math.max(x22[0], Math.max(x23[0], Math.max(x3[0], x31[0])))))))))));
            double miny = Math.min(x0[1], Math.min(x01[1], Math.min(x02[1], Math.min(x03[1], Math.min(x1[1], Math.min(x12[1], Math.min(x2[1], Math.min(x21[1], Math.min(x22[1], Math.min(x23[1], Math.min(x3[1], x31[1])))))))))));
            double maxy = Math.max(x0[1], Math.max(x01[1], Math.max(x02[1], Math.max(x03[1], Math.max(x1[1], Math.max(x12[1], Math.max(x2[1], Math.max(x21[1], Math.max(x22[1], Math.max(x23[1], Math.max(x3[1], x31[1])))))))))));

            logger.debug("minx:"+minx+"  maxx:"+maxx+"   miny:"+miny+   "maxy:"+ maxy);
            
            String f=new StringBuilder("BBOX(").append(geomName).append(",").append(minx).append(",").append(miny).append(",").append(maxx).append(",").append(maxy+")").toString();
            
            Filter filter=CQL.toFilter(f);
            System.out.println(filter);

            //poligono con punti di riferimento dell'immagine
            Polygon imageP=PolygonOp.createPolygon(x0,x01,x02,x03,x1,x12,x2,x21,x22,x23,x3,x31,x0);
            logger.debug("Polygon imageP isvalid:"+imageP.isValid());
            //filtro prendendo solo le 'features' nell'area di interesse
            FeatureCollection<?, ?> fc=featureSource.getFeatures(filter);
            if (fc.isEmpty()) {
                return null;
            }
            String[] schema = createSchema(fc.getSchema().getDescriptors());
            String[] types = createTypes(fc.getSchema().getDescriptors());

            String geoName = fc.getSchema().getGeometryDescriptor().getType().getName().toString();
            out=createFromSimpleGeometry(imageP, geoName, dataStore, fc, schema, types);
            dataStore.dispose();
            fc=null;
            System.gc();
            GeometricLayer glout = GeometricLayer.createImageProjectedLayer(out, gt,null);
            return glout;
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
        return null;

    }
	/**
	 *  
	 * 
	 * 
	 * @param imageP poligono creato con i punti di riferimento dell'immagine
	 * @param geoName
	 * @param dataStore  shape file
	 * @param fc
	 * @param schema
	 * @param types
	 * @return Polygons (geometry) that are the intersection between the shape file and the sar image
	 * @throws IOException
	 */
    private GeometricLayer createFromSimpleGeometry(Polygon imageP, String geoName, DataStore dataStore, FeatureCollection fc, String[] schema, String[] types) throws IOException{
        GeometricLayer out=null;
        if (geoName.contains("Polygon") || geoName.contains("Line")) {
                out = new GeometricLayer(GeometricLayer.POLYGON);
                out.setName(dataStore.getTypeNames()[0]);
                FeatureIterator<?> fi = fc.features();
                try{
	                while (fi.hasNext()) {
	                    Feature f = fi.next();
	                    try {
	                        Attributes at = Attributes.createAttributes(schema, types);
	                        for (int i = 0; i < f.getProperties().size(); i++) {
	                            at.set(schema[i], f.getProperty(schema[i]).getValue());
	                        }
	                        Geometry g=(Geometry) f.getDefaultGeometryProperty().getValue();
	                        g=TopologyPreservingSimplifier.simplify(g,0.01);

	                        //buffer(0) is used to avoid intersection errors 
	                        Geometry p2 = EnhancedPrecisionOp.intersection(g.buffer(0),imageP);
	                        if(!p2.isEmpty()){
		                        p2=TopologyPreservingSimplifier.simplify(p2,0.01);
		                    	for (int i = 0; i < p2.getNumGeometries(); i++) {
		                            if (!p2.getGeometryN(i).isEmpty()) {
		                                out.put(p2.getGeometryN(i), at);
		                            }
		                        }
	                        }	
	                            
	                    } catch (Exception ex) {
	                    	logger.error(ex.getMessage(),ex);
	                    }
	                }
                }finally{
                	fi.close();
                }   
                //out.put(imageP, Attributes.createAttributes(schema, types));
            } else if (geoName.contains("Point")) {
                out = new GeometricLayer(GeometricLayer.POINT);
                FeatureIterator<?> fi = fc.features();
                try{
	                out.setName(dataStore.getTypeNames()[0]);
	                while (fi.hasNext()) {
	                    Feature f = fi.next();
	                    Attributes at = Attributes.createAttributes(schema, types);
	                    for (int i = 0; i < f.getProperties().size(); i++) {
	                        at.set(schema[i],f.getProperty(schema[i]).getValue());
	                    }
	                    Geometry p2 = ((Geometry) (f.getDefaultGeometryProperty().getValue())).intersection(imageP);
	                    if (!p2.isEmpty()) {
	                        out.put(p2, at);
	                    }
	
	                }
	            }finally{
	            	fi.close();
	            }  
            }
        return out;
    }
/*
    @Override
    public void save(GeometricLayer layer, String projection,SarImageReader reader) {
    	GeoTransform transform=reader.getGeoTransform();
        try {
            layer = GeometricLayer.createWorldProjectedLayer(layer, transform, projection);
            String filename = ((URL) config.get(CONFIG_URL)).getPath();
            layername = filename.substring(filename.lastIndexOf(File.separator) + 1, filename.lastIndexOf("."));
            SimpleFeatureType ft = createFeatureType(layername, layer);
            FileDataStore fileDataStore = createDataStore(filename, ft, projection);
            FeatureCollection<SimpleFeatureType,SimpleFeature>  features = createFeatures(ft, layer, projection);
            
            writeToShapefile(fileDataStore, features);
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }*/
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
            
            writeToShapefile(newDataStore, features);
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
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
