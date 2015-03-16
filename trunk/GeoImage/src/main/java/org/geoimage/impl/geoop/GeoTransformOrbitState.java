package org.geoimage.impl.geoop;

import org.geoimage.def.GeoTransform;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

public class GeoTransformOrbitState implements GeoTransform{

	@Override
	public double[] getPixelFromGeo(double xgeo, double ygeo,
			String inputWktProjection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getPixelFromGeoWithDefaultEps(double xgeo, double ygeo,
			boolean changeProjection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getGeoFromPixel(double xpix, double ypix,
			String outputWktProjection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getGeoFromPixelWithDefaultEps(double xpix, double ypix,
			boolean changeProjection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getPixelFromGeo(double[] src, int srcOffset, double[] dest,
			int destOffset, int numPoints, String inputWktProjection)
			throws NoSuchAuthorityCodeException, FactoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getGeoFromPixel(double[] src, int srcOffset, double[] dest,
			int destOffset, int numPoints, String outputWktProjection)
			throws NoSuchAuthorityCodeException, FactoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTransformTranslation(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int[] getTransformTranslation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getPixelSize() {
		// TODO Auto-generated method stub
		return null;
	}

}
