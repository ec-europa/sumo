package others.imgproc;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class  GeoToolsVisualization {

   public static void main(String[] args) throws Exception {


       File file = JFileDataStoreChooser.showOpenFile("shp", null);
       if (file == null) {
           return;
       }

       FileDataStore store = FileDataStoreFinder.getDataStore(file);
       SimpleFeatureSource featureSource =store.getFeatureSource(store.getTypeNames()[0]);

       SimpleFeatureType sft = featureSource.getSchema();

       //Create the new type using the former as a template
       SimpleFeatureTypeBuilder stb = new SimpleFeatureTypeBuilder();
       stb.init(sft);
       stb.setName("newFeatureType");

       //Add the new attribute
       stb.add("Cluster", Integer.class);
       SimpleFeatureType newFeatureType = stb.buildFeatureType();


       //Create the collection of new Features
       SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(newFeatureType);
       SimpleFeatureCollection collection = FeatureCollections.newCollection();

       SimpleFeatureIterator it = featureSource.getFeatures().features();
       try {
           while (it.hasNext() && collection.size() <= 10) {
               SimpleFeature sf = it.next();
               sfb.addAll(sf.getAttributes());
               sfb.add(Integer.valueOf(0));
         ///**************************      collection.add(sfb.buildFeature(null));
           }
       } finally {
           it.close();
       }


       File newFile = getNewShapeFile(file);

       ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

       Map<String, Serializable> params = new HashMap<String, Serializable>();
       params.put("url", newFile.toURI().toURL());
       params.put("create spatial index", Boolean.TRUE);

       ShapefileDataStore newDataStore = (ShapefileDataStore)dataStoreFactory.createNewDataStore(params);
       newDataStore.createSchema(newFeatureType);

       // newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);
//Shall I comment this?

       Transaction transaction = new DefaultTransaction("create");

       String typeName = newDataStore.getTypeNames()[0];
       SimpleFeatureSource newFeatureSource =newDataStore.getFeatureSource(typeName);

       if (newFeatureSource instanceof SimpleFeatureStore) {
           SimpleFeatureStore featureStore = (SimpleFeatureStore)newFeatureSource;

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
           System.exit(1);
       }

       //Start the opertaion to display the generated file
       FileDataStore store2 = FileDataStoreFinder.getDataStore(newFile);
       SimpleFeatureSource featureSource2 = store2.getFeatureSource();

       MapContent map = new MapContent();
       map.setTitle("Quickstart");

       Style style = SLD.createSimpleStyle(featureSource2.getSchema());
       Layer layer = new FeatureLayer(featureSource2, style);
       map.addLayer(layer);

       JMapFrame.showMap(map);
   }

   private static File getNewShapeFile(File oldFile) {
       String path = oldFile.getAbsolutePath();
       String newPath = path.substring(0, path.length() - 4) + ".shp";

       JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
       chooser.setDialogTitle("Save shapefile");
       chooser.setSelectedFile(new File(newPath));

       int returnVal = chooser.showSaveDialog(null);

       if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
           // the user canceled the dialog
           System.exit(0);
       }

       File newFile = chooser.getSelectedFile();
       if (newFile.equals(oldFile)) {
           System.out.println("Error: cannot replace " + oldFile);
           System.exit(0);
       }

       return newFile;
   }

}