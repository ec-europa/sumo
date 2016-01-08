/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.def;

import java.util.List;

import org.geoimage.exception.GeoTransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;


/**
 * Interface that manages the transformation of pixel from/to map coordinates
 * @author thoorfr
 */
public interface GeoTransform {
    public double[] getPixelFromGeo(double xgeo, double ygeo)throws GeoTransformException;
    public double[] getGeoFromPixel(double xpix, double ypix)throws GeoTransformException;
    
    public List<double[]> getPixelFromGeo(Coordinate[] coords)throws GeoTransformException;
    public Geometry transformGeometryPixelFromGeo(Geometry geo)throws GeoTransformException;
    public Geometry transformGeometryGeoFromPixel(Geometry geo)throws GeoTransformException;
    
}
