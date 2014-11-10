/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.def;

import java.util.HashMap;

/**
 * Interface dealing with geographic metadata (ie data to geolocatlise the raster)
 * TODO: clarify relationship and separation of content with SarMetadata
 * @author thoorfr
 */
public interface GeoMetadata {

    public static String TYPE = "type";
    /*
     * Height of the raster data in pixels
     */
    public static String HEIGHT = "height";
    /*
     * Width of the raster data in pixels
     */
    public static String WIDTH = "width";
    /*
     * Timestamp of the beginning of the acquisition
     */
    public static String TIMESTAMP_START = "timestamp start";
    /*
     * Timestamp of the end of the acquisition
     */
    public static String TIMESTAMP_STOP = "timestamp stop";
    /*
     * 1 for 8 bits coded band, 2 for 16 bits coded band and so on...
     */
    public static String NUMBER_BYTES = "number of bytes per band";
    public static String HEADING_ANGLE = "heading angle";
    public static String SENSOR = "sensor";
    public static String LOOK_DIRECTION = "look direction";
    public static String ORBIT_DIRECTION = "orbit direction";
    /*
     * name of the satellite: Envisat, Radarsat, ERS....
     */
    public static String SATELLITE = "satellite";
    public static String PROCESSOR = "processor";
    public static String NUMBER_BANDS = "number of bands";
    public static String INCIDENCE_NEAR = "near incidence angle";
    public static String INCIDENCE_FAR = "far incidence angle";
    public static String SLANT_RANGE_NEAR_EDGE = "slant range near edge";
    public static String AZIMUTH_SPACING = "Azimuth Spacing";
    public static String RANGE_SPACING = "Range Spacing";
    public static String SATELLITE_ALTITUDE = "Satellite Altitude";
    public static String SATELLITE_ORBITINCLINATION = "Satellite Orbit Inclination";
    public static String SIMPLE_TIME_ORDERING = "pixel time ordering";
    
    
    
    
    //Ellipsoid Parameters
    public static String MAJOR_AXIS="semiMajorAxis";
    public static String MINOR_AXIS="semiMinorAxis";
    public static String GEODETIC_TERRA_HEIGHT="geodeticTerrainHeight";

    /*
     * SENTINEL 1
     */
    public static String SWATH = "swath";
    
    
    public static String REVOLUTIONS_PERDAY = "Satellite revolutions per day";
    /*
     * Speed od the satellite during the acquisition
     */
    public static String SATELLITE_SPEED = "Satellite Speed";
    
    
    
    public HashMap<String, Object> getMetadata();
    
    public void setMetadata(String key, Object value);
    
    public Object getMetadata(String key);
    
    //return the rotation of azimuth angle 0-180degree 
    public double getImageAzimuth();
    
    public double getIncidence(int position);

    public double getSlantRange(int position);
    
  //  public void geoCorrect();

}
