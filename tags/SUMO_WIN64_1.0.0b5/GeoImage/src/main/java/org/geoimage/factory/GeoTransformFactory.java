/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.factory;

import java.awt.geom.AffineTransform;
import java.util.List;

import org.geoimage.def.GeoTransform;
import org.geoimage.impl.AffineGeoTransform;
import org.geoimage.impl.Gcp;
import org.geoimage.impl.GcpsGeoTransform;

/**
 * Simple class that contains factory methods that return the appropriate
 * implementation of the GeoTransform interface
 * @author thoorfr
 */
public class GeoTransformFactory {

    public static GeoTransform getFromAffineTransform(AffineTransform atpix2geo, String wktGeoProj) {
        return new AffineGeoTransform(atpix2geo, wktGeoProj);
    }
    
    public static GeoTransform createFromGcps(List<Gcp> gcps, String wktGeoProj) {
        return new GcpsGeoTransform(gcps, wktGeoProj);
    }
}


