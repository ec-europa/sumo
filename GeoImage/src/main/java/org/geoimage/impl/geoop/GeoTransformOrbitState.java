package org.geoimage.impl.geoop;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.geoimage.def.GeoTransform;
import org.geoimage.exception.GeoTransformException;
import org.geotools.referencing.GeodeticCalculator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import jrc.it.geolocation.exception.GeoLocationException;
import jrc.it.geolocation.exception.MathException;
import jrc.it.geolocation.geo.ParallelGeoCoding;
import jrc.it.geolocation.geo.S1GeoCodingImpl;
import jrc.it.xml.wrapper.SumoAnnotationReader;
/**
 * 
 * @author Pietro Argentieri
 *
 */
public class GeoTransformOrbitState implements GeoTransform{
	//private String annotationFile="";
	private S1GeoCodingImpl geocoding=null;
	private ParallelGeoCoding pGeo=null;
    
		
	
	/**
	 * 
	 * @param annotationFile
	 * @throws GeoTransformException
	 */
	public GeoTransformOrbitState(String annotationFile) throws GeoTransformException{
		try{
			//this.annotationFile=annotationFile;
			geocoding=new S1GeoCodingImpl(annotationFile);
			pGeo=new ParallelGeoCoding(geocoding);
		}catch(Exception e){
			throw new GeoTransformException(e.getMessage());
		}	
	}
	
	/**
	 * 
	 * @param annotationFile
	 * @throws GeoTransformException
	 */
	public GeoTransformOrbitState(SumoAnnotationReader annotationFile) throws GeoTransformException{
		try{
			geocoding=new S1GeoCodingImpl(annotationFile);
			pGeo=new ParallelGeoCoding(geocoding);
		}catch(MathException e){
			throw new GeoTransformException(e.getMessage());
		}	
	}
	
	@Override
	public double[] getPixelFromGeo(double lon, double lat)throws GeoTransformException {
		try{
			double[] coo=geocoding.pixelFromGeo(lon, lat);
			return coo;
		}catch(GeoLocationException ge){
			throw new GeoTransformException(ge.getMessage());
		}	
	}

	@Override
	public double[] getGeoFromPixel(double col, double line)throws GeoTransformException {
		try{
			double[]coo=geocoding.geoFromPixel(line,col);
			return coo;
		}catch(GeoLocationException ge){
			throw new GeoTransformException(ge.getMessage());
		}
	}
	/**
	 * 
	 */
	public List<double[]> getPixelFromGeo(Coordinate[]coords)throws GeoTransformException {
		try{
			List<double[]> coo=pGeo.parallelPixelFromGeo(coords);
			return coo;
		}catch(InterruptedException|ExecutionException ge){
			throw new GeoTransformException(ge.getMessage());
		} 
	}
	/**
	 * 
	 */
	public Geometry transformGeometryPixelFromGeo(Geometry geom)throws GeoTransformException {
		try{
            Coordinate[] coords=geom.getCoordinates();
            List<double[]> coordsConv=pGeo.parallelPixelFromGeo(coords);
            for(int i=0;i<coords.length;i++){
                coords[i].x=coordsConv.get(i)[0];
                coords[i].y=coordsConv.get(i)[1];
            }   
            return geom;
		}catch(InterruptedException|ExecutionException ge){
			throw new GeoTransformException(ge.getMessage());
		} 
	}
	
	@Override
	public Geometry transformGeometryGeoFromPixel(Geometry geom)
			throws GeoTransformException {
		try{
            Coordinate[] coords=geom.getCoordinates();
            List<double[]> coordsConv=pGeo.parallelGeoFromPixel(coords);
            for(int i=0;i<coords.length;i++){
                coords[i].x=coordsConv.get(i)[0];
                coords[i].y=coordsConv.get(i)[1];
            }   
            return geom;
		}catch(InterruptedException|ExecutionException ge){
			throw new GeoTransformException(ge.getMessage());
		} 
	}
	
	public double getSlanteRangeDist(double lon,double lat){
		return geocoding.getSlantRange(lon, lat);
	}
	
	
	/**
	 * 
	 */
	/*
	private void calcPixelSize(){
		try{
	        // should be in the image reader class
	        // get pixel size
	        double[] latlonorigin = getGeoFromPixel(0, 0);
	        double[] latlon = getGeoFromPixel(100, 0);
	
	        GeodeticCalculator geoCalc = new GeodeticCalculator();
	        // use the geodetic calculator class to calculate distances in meters
	        geoCalc.setStartingGeographicPoint(latlonorigin[0], latlonorigin[1]);
	        geoCalc.setDestinationGeographicPoint(latlon[0], latlon[1]);
	        pixelsize[0] = geoCalc.getOrthodromicDistance() / 100;
	        latlon = getGeoFromPixel(0, 100);
	        geoCalc.setDestinationGeographicPoint(latlon[0], latlon[1]);
	        pixelsize[1] = geoCalc.getOrthodromicDistance() / 100;
		}catch(GeoTransformException ge){
			pixelsize = new double[]{0.0, 0.0};
		}    
	}*/
	


}
