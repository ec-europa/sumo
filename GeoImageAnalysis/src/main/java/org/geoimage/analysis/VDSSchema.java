/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.analysis;

/**
 *
 * @author thoorfr
 */
public final class VDSSchema {
    public static final String ID="id";
    public static final String MAXIMUM_VALUE="max_value";
    public static final String TILE_AVERAGE="tile_average";
    public static final String TILE_STANDARD_DEVIATION="tile_std";
    public static final String THRESHOLD="threshold";
    public static final String NUMBER_OF_AGGREGATED_PIXELS="num_pix";
    public static final String RUN_ID="runid";
    public static final String ESTIMATED_LENGTH="length";
    public static final String ESTIMATED_WIDTH="width";
    public static final String ESTIMATED_HEADING="heading";
    public static final String SIGNIFICANCE="significance";
    public static final String DATE="date";
    public static final String VS="vs";

    public static final String[] schema={ID, MAXIMUM_VALUE, TILE_AVERAGE, TILE_STANDARD_DEVIATION, THRESHOLD, NUMBER_OF_AGGREGATED_PIXELS, RUN_ID, ESTIMATED_LENGTH, ESTIMATED_WIDTH, ESTIMATED_HEADING, SIGNIFICANCE, DATE, VS};
    public static final String[] types={"Double", "Double", "Double", "Double", "Double", "Double", "String", "Double", "Double", "Double", "Double","Date", "Double"};
    
    /*static public String[] getSchema(){
        return new String[]{ID, MAXIMUM_VALUE, TILE_AVERAGE, TILE_STANDARD_DEVIATION, THRESHOLD, NUMBER_OF_AGGREGATED_PIXELS, RUN_ID, ESTIMATED_LENGTH, ESTIMATED_WIDTH, ESTIMATED_HEADING, SIGNIFICANCE, DATE, VS};
    }*/

/*    static public String[] getTypes(){
        return new String[]{"Double", "Double", "Double", "Double", "Double", "Double", "String", "Double", "Double", "Double", "Double","Date", "Double"};
    }*/

}
