/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.def;


/**
 * Interface that manages the transformation of pixel from/to map coordinates
 * @author thoorfr
 */
public interface GeoTransform {
    public double[] getPixelFromGeo(double xgeo, double ygeo);
    public double[] getGeoFromPixel(double xpix, double ypix);
    public double[] getPixelSize();   
}
