package jrc.it.geolocation.geo;

import jrc.it.geolocation.exception.GeoLocationException;

public interface GeoCoding {

	/**
	 * 
	 * @param lat
	 * @param lon
	 */
	public abstract double[] reverse(double lat, double lon)throws GeoLocationException;
	public abstract double[] forward(double l, double p)throws GeoLocationException;

}