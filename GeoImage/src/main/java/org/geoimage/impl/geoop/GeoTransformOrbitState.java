package org.geoimage.impl.geoop;

import jrc.it.geolocation.geo.S1GeoCodingImpl;

import org.geoimage.def.GeoTransform;
import org.geotools.referencing.GeodeticCalculator;

public class GeoTransformOrbitState implements GeoTransform{
	//private String annotationFile="";
	private S1GeoCodingImpl geocoding=null;
	
	public GeoTransformOrbitState(String annotationFile){
		//this.annotationFile=annotationFile;
		geocoding=new S1GeoCodingImpl(annotationFile);
	}
	
	@Override
	public double[] getPixelFromGeo(double xgeo, double ygeo) {
		double[] coo=geocoding.reverse(xgeo, ygeo);
		return coo;
	}

	@Override
	public double[] getGeoFromPixel(double xpix, double ypix) {
		double[]coo=geocoding.forward(xpix, ypix);
		return coo;
	}

	@Override
	public double[] getPixelSize() {
        double[] pixelsize = {0.0, 0.0};
        // should be in the image reader class
        // get pixel size
        double[] latlonorigin = getGeoFromPixel(0, 0);
        double[] latlon = getGeoFromPixel(100, 0);
        // use the geodetic calculator class to calculate distances in meters
        GeodeticCalculator gc = new GeodeticCalculator();
        gc.setStartingGeographicPoint(latlonorigin[0], latlonorigin[1]);
        gc.setDestinationGeographicPoint(latlon[0], latlon[1]);
        pixelsize[0] = gc.getOrthodromicDistance() / 100;
        latlon = getGeoFromPixel(0, 100);
        gc.setDestinationGeographicPoint(latlon[0], latlon[1]);
        pixelsize[1] = gc.getOrthodromicDistance() / 100;
        
        return pixelsize;
	}

	

}
