/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.GeoTransform;
import org.geoimage.impl.GcpsGeoTransform;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.GeometricLayer;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
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
import com.vividsolutions.jts.io.WKTReader;

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
        /*FileDataStoreFactorySpi factory = new ShapefileDataStoreFactory();

        File file = new File(filename);

        // Create a Map object used by our DataStore Factory
        Map<String, Serializable> map = Collections.singletonMap("url", (Serializable) (file.toURI().toURL()));

        DataStore myData = factory.createNewDataStore(map);

        // Create the Shapefile (empty at this point)
        System.out.println(ft.getGeometryDescriptor());
        myData.createSchema(ft);

        // Tell the DataStore what type of Coordinate Reference System (CRS) to use
        if (projection != null) {
            ((ShapefileDataStore)myData).forceSchemaCRS(CRS.decode(projection));
        }*/
    	
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
    public static FeatureCollection<SimpleFeatureType,SimpleFeature>  createFeatures(SimpleFeatureType ft, GeometricLayer glayer, String projection, GeoTransform gt) throws Exception {
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
                 Object[] data = new Object[glayer.getSchema().length + 1];
                 data[0] = geom;
                 System.arraycopy(glayer.getAttributes(geom).getValues(), 0, data, 1, data.length - 1);
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
            StringBuilder sch = new StringBuilder();
            String[] schema = glayer.getSchema();
            String[] types = glayer.getSchemaTypes();
            for (int i = 0; i < schema.length; i++) {
                sch.append(",").append(schema[i]).append(":").append(types[i]);
            }
            
            String geomType = "MultiPolygon";
            if (glayer.getGeometries().get(0) instanceof Point) {
                geomType = "Point";
            }
            
            String typeSpec=new StringBuilder("geom:").append(geomType).append(":srid=").append(glayer.getProjection().replace("EPSG:", ""))
            		.append(",").append(sch.substring(1)).append(",").toString();
            
            SimpleFeatureType featureType = DataUtilities.createType(name, typeSpec);

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
            GcpsGeoTransform gt =(GcpsGeoTransform) gir.getGeoTransform();
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

            x0 = gt.getGeoFromPixelWithDefaultEps(-margin, -margin,false);

            x01 = gt.getGeoFromPixelWithDefaultEps(-margin, gir.getHeight()/3,false);
            x02 = gt.getGeoFromPixelWithDefaultEps(-margin, gir.getHeight()/2,false);
            x03 = gt.getGeoFromPixelWithDefaultEps(-margin, gir.getHeight()*2/3,false);

            x1 = gt.getGeoFromPixelWithDefaultEps(-margin, margin + gir.getHeight(),false);

            x12 = gt.getGeoFromPixelWithDefaultEps(margin + gir.getWidth()/2, margin +gir.getHeight(),false);

            x2 = gt.getGeoFromPixelWithDefaultEps(margin + gir.getWidth(), margin + gir.getHeight(),false);

            x21 = gt.getGeoFromPixelWithDefaultEps(margin + gir.getWidth(), gir.getHeight()*2/3,false);
            x22 = gt.getGeoFromPixelWithDefaultEps(margin + gir.getWidth(), gir.getHeight()/2,false);
            x23 = gt.getGeoFromPixelWithDefaultEps(margin + gir.getWidth(), gir.getHeight()/3,false);

            x3 = gt.getGeoFromPixelWithDefaultEps(margin + gir.getWidth(), -margin,false);

            x31 = gt.getGeoFromPixelWithDefaultEps(margin+gir.getWidth()/2, -margin,false);

            double minx = Math.min(x0[0], Math.min(x01[0], Math.min(x02[0], Math.min(x03[0], Math.min(x1[0], Math.min(x12[0], Math.min(x2[0], Math.min(x21[0], Math.min(x22[0], Math.min(x23[0], Math.min(x3[0], x31[0])))))))))));
            double maxx = Math.max(x0[0], Math.max(x01[0], Math.max(x02[0], Math.max(x03[0], Math.max(x1[0], Math.max(x12[0], Math.max(x2[0], Math.max(x21[0], Math.max(x22[0], Math.max(x23[0], Math.max(x3[0], x31[0])))))))))));
            double miny = Math.min(x0[1], Math.min(x01[1], Math.min(x02[1], Math.min(x03[1], Math.min(x1[1], Math.min(x12[1], Math.min(x2[1], Math.min(x21[1], Math.min(x22[1], Math.min(x23[1], Math.min(x3[1], x31[1])))))))))));
            double maxy = Math.max(x0[1], Math.max(x01[1], Math.max(x02[1], Math.max(x03[1], Math.max(x1[1], Math.max(x12[1], Math.max(x2[1], Math.max(x21[1], Math.max(x22[1], Math.max(x23[1], Math.max(x3[1], x31[1])))))))))));

            String f=new StringBuilder("BBOX(").append(geomName).append(",").append(minx).append(",").append(miny).append(",").append(maxx).append(",").append(maxy+")").toString();
            
            Filter filter=CQL.toFilter(f);
            System.out.println(filter);

            Polygon imageP = (Polygon) new WKTReader().read("POLYGON((" +
                    x0[0] + " " + x0[1] + "," +
                    x01[0] + " " + x01[1] + "," +
                    x02[0] + " " + x02[1] + "," +
                    x03[0] + " " + x03[1] + "," +
                    x1[0] + " " + x1[1] + "," +
                    x12[0] + " " + x12[1] + "," +
                    x2[0] + " " + x2[1] + "," +
                    x21[0] + " " + x21[1] + "," +
                    x22[0] + " " + x22[1] + "," +
                    x23[0] + " " + x23[1] + "," +
                    x3[0] + " " + x3[1] + "," +
                    x31[0] + " " + x31[1] + "," +
                    x0[0] + " " + x0[1] + "" +
                    "))");
            System.out.println(imageP);
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
	 * @param imageP
	 * @param geoName
	 * @param dataStore
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
	                        Geometry p2 = ((Geometry) f.getDefaultGeometryProperty().getValue()).intersection(imageP);
	                        //Geometry p2 = (Geometry) f.getDefaultGeometryProperty().getValue();
	                        for (int i = 0; i < p2.getNumGeometries(); i++) {
	                            if (!p2.getGeometryN(i).isEmpty()) {
	                                out.put(p2.getGeometryN(i), at);
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

    @Override
    public void save(GeometricLayer layer, String projection,GeoImageReader reader) {
    	GeoTransform transform=reader.getGeoTransform();
        try {
            layer = GeometricLayer.createWorldProjectedLayer(layer, transform, projection);
            String filename = ((URL) config.get(CONFIG_URL)).getPath();
            System.out.println(filename);
            //new File(filename).createNewFile();
            layername = filename.substring(filename.lastIndexOf(File.separator) + 1, filename.lastIndexOf("."));
            System.out.println(layername);
            SimpleFeatureType ft = createFeatureType(layername, layer);
            //build the type
            FileDataStore fileDataStore = createDataStore(filename, ft, projection);
            FeatureCollection<SimpleFeatureType,SimpleFeature>  features = createFeatures(ft, layer, projection, transform);
            
            writeToShapefile(fileDataStore, features);
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
            out[i++] = at.getType().getBinding().getSimpleName();
        }
        return out;
    }
    
    /*
    public GeometricLayer readTestWithAffine() {
        try {
            GeometricLayer out = null;
            int margin = Integer.parseInt(java.util.ResourceBundle.getBundle("GeoImageViewer").getString("SimpleShapeFileIO.margin"));
            //margin=0;
            //create a DataStore object to connect to the physical source 
            DataStore dataStore = DataStoreFinder.getDataStore(config);
            //retrieve a FeatureSource to work with the feature data
            SimpleFeatureStore featureSource = (SimpleFeatureStore) dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
            String geomName = featureSource.getSchema().getGeometryDescriptor().getLocalName();
            
            FeatureSource<SimpleFeatureType, SimpleFeature> ss = DataUtilities.source(featureSource.getFeatures());        

            
            MapContent  map = new MapContent();
            map.setTitle("The Point");
            Layer layer=new FeatureLayer(ss.getFeatures(),null);
            map.addLayer(layer);     
            ReferencedEnvelope mapBounds=map.getMaxBounds();
            Rectangle imageBounds=new Rectangle(new Dimension(gir.getWidth(),gir.getHeight()));

            AffineTransform gt =RendererUtilities.worldToScreenTransform(mapBounds, imageBounds);
            
            Point2D x0=new Point2D.Double();
            Point2D x1=new Point2D.Double();
            Point2D x2=new Point2D.Double();
            Point2D x3=new Point2D.Double();
            Point2D x01=new Point2D.Double(); //image center coords           
            Point2D x02=new Point2D.Double(); //image center coords
            Point2D x03=new Point2D.Double(); //image center coords
            Point2D x12=new Point2D.Double(); //image center coords
            Point2D x21=new Point2D.Double(); //image center coords
            Point2D x22=new Point2D.Double(); //image center coords
            Point2D x23=new Point2D.Double(); //image center coords
            Point2D x31=new Point2D.Double(); //image center coords

            gt.transform(new Point2D.Double(-margin, -margin), x0);
            gt.transform(new Point2D.Double(-margin, gir.getHeight()/3), x01);
            gt.transform(new Point2D.Double(-margin, gir.getHeight()/2), x02);
            gt.transform(new Point2D.Double(-margin, gir.getHeight()*2/3), x03);
            gt.transform(new Point2D.Double(-margin, margin + gir.getHeight()), x1);
            gt.transform(new Point2D.Double(margin + gir.getWidth()/2, margin +gir.getHeight()), x12);
            gt.transform(new Point2D.Double(margin + gir.getWidth(), margin + gir.getHeight()), x2);
            gt.transform(new Point2D.Double(margin + gir.getWidth(), gir.getHeight()*2/3), x21);
            gt.transform(new Point2D.Double(margin + gir.getWidth(), gir.getHeight()/2), x22);
            gt.transform(new Point2D.Double(margin + gir.getWidth(), gir.getHeight()/3), x23);
            gt.transform(new Point2D.Double(margin + gir.getWidth(), -margin), x3);
            gt.transform(new Point2D.Double(margin + gir.getWidth()/2, -margin), x31);


            double minx = Math.min(x0.getX(), Math.min(x01.getX(), Math.min(x02.getX(), Math.min(x03.getX(), Math.min(x1.getX(), Math.min(x12.getX(), Math.min(x2.getX(), Math.min(x21.getX(), Math.min(x22.getX(), Math.min(x23.getX(), Math.min(x3.getX(), x31.getX())))))))))));
            double maxx = Math.max(x0.getX(), Math.max(x01.getX(), Math.max(x02.getX(), Math.max(x03.getX(), Math.max(x1.getX(), Math.max(x12.getX(), Math.max(x2.getX(), Math.max(x21.getX(), Math.max(x22.getX(), Math.max(x23.getX(), Math.max(x3.getX(), x31.getX())))))))))));
            double miny = Math.min(x0.getY(), Math.min(x01.getY(), Math.min(x02.getY(), Math.min(x03.getY(), Math.min(x1.getY(), Math.min(x12.getY(), Math.min(x2.getY(), Math.min(x21.getY(), Math.min(x22.getY(), Math.min(x23.getY(), Math.min(x3.getY(), x31.getY())))))))))));
            double maxy = Math.max(x0.getY(), Math.max(x01.getY(), Math.max(x02.getY(), Math.max(x03.getY(), Math.max(x1.getY(), Math.max(x12.getY(), Math.max(x2.getY(), Math.max(x21.getY(), Math.max(x22.getY(), Math.max(x23.getY(), Math.max(x3.getY(), x31.getY())))))))))));

            String f=new StringBuilder("BBOX(").append(geomName).append(",").append(minx).append(",").append(miny).append(",").append(maxx).append(",").append(maxy+")").toString();
            
            Filter filter=CQL.toFilter(f);
            System.out.println(filter);

            Polygon imageP = (Polygon) new WKTReader().read("POLYGON((" +
                    x0.getX() + " " + x0.getY() + "," +
                    x01.getX() + " " + x01.getY() + "," +
                    x02.getX() + " " + x02.getY() + "," +
                    x03.getX() + " " + x03.getY() + "," +
                    x1.getX() + " " + x1.getY() + "," +
                    x12.getX() + " " + x12.getY() + "," +
                    x2.getX() + " " + x2.getY() + "," +
                    x21.getX() + " " + x21.getY() + "," +
                    x22.getX() + " " + x22.getY() + "," +
                    x23.getX() + " " + x23.getY() + "," +
                    x3.getX() + " " + x3.getY() + "," +
                    x31.getX() + " " + x31.getY() + "," +
                    x0.getX() + " " + x0.getY() + "" +
                    "))");
            System.out.println(imageP);
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
            GeometricLayer glout = GeometricLayer.createImageProjectedLayer(out, gt);
            return glout;
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
        return null;

    }*/
}
