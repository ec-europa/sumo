package others;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.geotools.data.DataUtilities;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.styling.StyleFactory;
import org.geotools.swing.JMapFrame;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class WorldScreen {
    static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
    static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);
    static CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
    static final int MAX_NAME_LENGTH = 20;
    static MapContent map;
    static JMapFrame mapFrame;
    
public static void main(String[] args) throws Exception {
    final SimpleFeatureType TYPE = DataUtilities.createType("Location", "location:Point,name:String"); // see createFeatureType();
    DefaultFeatureCollection collection = new DefaultFeatureCollection(); 
    
    //DATA
    double lon[] = {123.31, 12, 20.222};
    double lat[] = {48.4, 52, 30.333};
    String name[] = { "Point1", "Point2", "Point3" };
     //Define Collection
     GeometryFactory factory = JTSFactoryFinder.getGeometryFactory(null);    
    
     for(int i=0;i<=2;i++){            
            Point point = factory.createPoint( new Coordinate(lon[i],lat[i]));
            SimpleFeature feature = SimpleFeatureBuilder.build( TYPE, new Object[]{point, name[i]}, null );
            collection.add( feature );
     }
     
     //Define FeatureSource
    // FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = DataUtilities.source(collection);        

     map = new DefaultMapContext();
     map.setTitle("The Point");
     
     Layer layer=new FeatureLayer(collection,null);
     map.addLayer(layer);     
     
     
     mapFrame = new JMapFrame(map);
     mapFrame.setSize(600, 600);
     //Iterate Collection
     FeatureIterator<SimpleFeature> iterator = collection.features();
     try {
         while (iterator.hasNext()) {
             SimpleFeature feature = iterator.next();

             Geometry geometry = (Geometry) feature.getDefaultGeometry();
             
             Coordinate[] coords = geometry.getCoordinates();
             for( int i = 0; i < coords.length; i++ ) {
                 System.out.println("================================================");
                 System.out.println("Actual\t x: "+coords[i].x + " \ty: " + coords[i].y);
                //Get Pixel Value of Coordinates
                 Point2D pixelValue=getWorldtoScreen(coords[i].x, coords[i].y);
                 System.out.println("Pixel\t x: "+pixelValue.getX()+" \tPixel y: \t"+pixelValue.getY());
                 //Get Coordinates from Pixel
                 Point2D coordValue=getScreentoWorld(pixelValue.getX(), pixelValue.getY());
                 System.out.println("Coord\t x: "+coordValue.getX()+" \tCoord y: \t"+coordValue.getY());
             }
         }
     }
     finally {
         if( iterator != null ){
             // YOU MUST CLOSE THE ITERATOR!
             iterator.close();
         }
     }
     mapFrame.enableStatusBar(true);
     mapFrame.setVisible(true);
}

public static Point2D getWorldtoScreen(double x, double y){
    
    Rectangle imageBounds=null;
    ReferencedEnvelope mapBounds=null;
    try{
//        mapBounds=map.getLayerBounds();
        imageBounds = mapFrame.getBounds();
        int width = (int)imageBounds.getWidth();
        int height = (int)imageBounds.getHeight();
    }catch(Exception e){
        
    } 
    
    
    AffineTransform world2screen =
    RendererUtilities.worldToScreenTransform(mapBounds, imageBounds);
    Point2D pointScreenAbsolute = new Point2D.Double(x, y);
    Point2D pointScreen = world2screen.transform(pointScreenAbsolute, null);
    return pointScreen;
}


public static Point2D getScreentoWorld(double x, double y) throws Exception {
    Rectangle imageBounds=null;
    ReferencedEnvelope mapBounds=null;
    try{
//        mapBounds=map.getLayerBounds();
        imageBounds = mapFrame.getBounds();
        int width = (int)imageBounds.getWidth();
        int height = (int)imageBounds.getHeight();
    }catch(Exception e){
        
    } 
    
    AffineTransform world2screen =
    RendererUtilities.worldToScreenTransform(mapBounds, imageBounds);
    
    AffineTransform screen2world = world2screen.createInverse();
    Point2D pointScreenAbsolute = new Point2D.Double(x, y);
    Point2D pointScreen = screen2world.transform(pointScreenAbsolute, null); 
    return pointScreen;
} 
}