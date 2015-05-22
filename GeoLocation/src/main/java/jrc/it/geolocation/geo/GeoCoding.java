package jrc.it.geolocation.geo;

public interface GeoCoding {

	/**
	 * 
	 * @param lat
	 * @param lon
	 */
	public abstract double[] reverse(double lat, double lon);
	public abstract double[] forward(double l, double p);

}