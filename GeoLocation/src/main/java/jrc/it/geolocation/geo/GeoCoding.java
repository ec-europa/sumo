package jrc.it.geolocation.geo;

import jrc.it.geolocation.exception.GeoLocationException;

public interface GeoCoding {

	/**
	 *  in Matlab is reverse
	 * @param lat
	 * @param lon
	 */
	public abstract double[] pixelFromGeo(double lat, double lon)throws GeoLocationException;
	/**
	 * in Matlab is forward
	 * @param l
	 * @param p
	 * @return
	 * @throws GeoLocationException
	 */
	public abstract double[] geoFromPixel(double l, double p)throws GeoLocationException;

}