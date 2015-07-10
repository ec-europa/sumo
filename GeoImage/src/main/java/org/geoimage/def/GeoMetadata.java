/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.def;

import org.geoimage.exception.GeoTransformException;


/**
 * Interface dealing with geographic metadata (ie data to geolocatlise the raster)
 * TODO: clarify relationship and separation of content with SarMetadata
 * @author thoorfr
 */
public interface GeoMetadata {

    public final static String TYPE = "type";
    /*
     * Height of the raster data in pixels
     */
    public final static String HEIGHT = "height";
    /*
     * Width of the raster data in pixels
     */
    public final  static String WIDTH = "width";
    
    /*
     * Timestamp of the beginning of the acquisition
     */
    public final static String TIMESTAMP_START = "timestamp start";
    /*
     * Timestamp of the end of the acquisition
     */
    public final static String TIMESTAMP_STOP = "timestamp stop";
    /*
     * 1 for 8 bits coded band, 2 for 16 bits coded band and so on...
     */
    public final static String NUMBER_BYTES = "number of bytes per band";
    public final static String HEADING_ANGLE = "heading angle";
    public final static String SENSOR = "sensor";
    public final static String LOOK_DIRECTION = "look direction";
    public final static String ORBIT_DIRECTION = "orbit direction";
    /*
     * name of the satellite: Envisat, Radarsat, ERS....
     */
    public final static String SATELLITE = "satellite";
    public final static String PROCESSOR = "processor";
    public final static String NUMBER_BANDS = "number of bands";
    public final static String INCIDENCE_NEAR = "near incidence angle";
    public final static String INCIDENCE_FAR = "far incidence angle";
    public final static String SLANT_RANGE_NEAR_EDGE = "slant range near edge";
    public final static String AZIMUTH_SPACING = "Azimuth Spacing";
    public final static String RANGE_SPACING = "Range Spacing";
    public final static String SATELLITE_ALTITUDE = "Satellite Altitude";
    public final static String SATELLITE_ORBITINCLINATION = "Satellite Orbit Inclination";
    public final static String SIMPLE_TIME_ORDERING = "pixel time ordering";
    
    
    
    
    //Ellipsoid Parameters
    public final static String MAJOR_AXIS="semiMajorAxis";
    public final static String MINOR_AXIS="semiMinorAxis";
    public final static String GEODETIC_TERRA_HEIGHT="geodeticTerrainHeight";

    /*
     * SENTINEL 1
     */
    public final static String SWATH = "swath";
    
    
    public final static String REVOLUTIONS_PERDAY = "Satellite revolutions per day";
    /*
     * Speed od the satellite during the acquisition
     */
    public final static String SATELLITE_SPEED = "Satellite Speed";
    
    
    
    
    
    
    
    //return the rotation of azimuth angle 0-180degree 
    public double getImageAzimuth() throws GeoTransformException;
    
    public double getIncidence(int position);

    public double getSlantRange(int position,double incidenceAngle);
    
  //  public void geoCorrect();

}
