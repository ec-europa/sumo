/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.api;

/**
 *
 * @author thoorfr
 */
public class VDSFields {
    
    public static String ID="id";
    public static String MAXIMUM_VALUE="maximum value";
    public static String TILE_AVERAGE="tile average";
    public static String TILE_STD="tile standard deviation";
    public static String THRESHOLD="threshold";
    public static String NUMBER_OF_PIXELS="num pixels";
    public static String RUNID="runid";
    public static String LENGTH="Estimated Length";
    public static String WIDTH="Estimated Width";
    public static String HEADING="Estimated Heading";
    public static String SIZE_CLASSIFICATION="Size Classification";

    
    public static String[] getSchema(){
        return new String[]{ID, MAXIMUM_VALUE, TILE_AVERAGE, TILE_STD, THRESHOLD, NUMBER_OF_PIXELS, RUNID, LENGTH, WIDTH, HEADING, SIZE_CLASSIFICATION};
    }
    
    public static String[] getTypes(){
         return new String[]{"Double", "Double", "Double", "Double", "Double", "Double", "String", "Double", "Double", "Double", "String"};
    }
}
