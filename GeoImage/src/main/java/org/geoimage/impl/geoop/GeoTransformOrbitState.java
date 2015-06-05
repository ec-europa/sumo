package org.geoimage.impl.geoop;

import jrc.it.geolocation.exception.GeoLocationException;
import jrc.it.geolocation.exception.MathException;
import jrc.it.geolocation.geo.S1GeoCodingImpl;

import org.geoimage.def.GeoTransform;
import org.geoimage.exception.GeoTransformException;
import org.geotools.referencing.GeodeticCalculator;

public class GeoTransformOrbitState implements GeoTransform{
	//private String annotationFile="";
	private S1GeoCodingImpl geocoding=null;
	private double[] pixelsize = {0.0, 0.0};
	
	public GeoTransformOrbitState(String annotationFile) throws GeoTransformException{
		try{
			//this.annotationFile=annotationFile;
			geocoding=new S1GeoCodingImpl(annotationFile);
			calcPixelSize();
		}catch(MathException e){
			throw new GeoTransformException(e.getMessage());
		}	
	}
	
	@Override
	public double[] getPixelFromGeo(double xgeo, double ygeo)throws GeoTransformException {
		try{
			double[] coo=geocoding.reverse(xgeo, ygeo);
			return coo;
		}catch(GeoLocationException ge){
			throw new GeoTransformException(ge.getMessage());
		}	
	}

	@Override
	public double[] getGeoFromPixel(double xpix, double ypix)throws GeoTransformException {
		try{
			double[]coo=geocoding.forward(xpix, ypix);
			return coo;
		}catch(GeoLocationException ge){
			throw new GeoTransformException(ge.getMessage());
		}
	}
	
	/**
	 * 
	 */
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
	}
	
	@Override
	public double[] getPixelSize() {
        return pixelsize;
	}

	

}
