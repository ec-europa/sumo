/*
 * 
 */
package org.geoimage.def;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

public interface IGcpsGeoTransform extends GeoTransform{

	
    public double[] getGeoFromPixel(double xpix, double ypix, String outputEpsProjection);
    public double[] getPixelFromGeo(double xgeo, double ygeo, String outputEpsProjection);
    public double[] getPixelFromGeo(double[] src, int srcOffset, double[] dest, int destOffset,int numPoints, String inputEpsProjection)throws NoSuchAuthorityCodeException, FactoryException ;
    public double[] getGeoFromPixel(double[] src, int srcOffset, double[] dest, int destOffset,int numPoints, String outputEpsProjection)throws NoSuchAuthorityCodeException, FactoryException;

	
}
