/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.def;

/**
 * This a container defining for all kind of metadata names sensible for Sar sensors
 * @author thoorfr
 */
public interface SarMetadata {

    public static String MODE = "mode";
    public static String BEAM = "beam";
    public static String PRODUCT = "product";
    public static String ENL = "enl";
    public static String POLARISATION = "polarisation";
    public static String RADAR_WAVELENGTH = "Radar Instrument Wavelength";
    public static String PRF = "SAR Instrument PRF";
    public static String PRF1 = "PRF1"; //SAR Instrument PRF1 used in TSX SC";
    public static String PRF2 = "PRF2"; //"SAR Instrument PRF2 used in TSX SC";
    public static String PRF3 = "PRF3"; //"SAR Instrument PRF3 used in TSX SC";
    public static String PRF4 = "PRF4"; //"SAR Instrument PRF4 used in TSX SC";
    public static String STRIPBOUND1 = "STRIPBOUND1"; //"TSX SC bound between strips 1-2";
    public static String STRIPBOUND2 = "STRIPBOUND2"; //"TSX SC bound between strips 2-3";
    public static String STRIPBOUND3 = "STRIPBOUND3"; //"TSX SC bound between strips 3-4";
    public static String K = "K value for Beta Nought calculation";


    public double getBetaNought(int x, double DN);

    public double getBetaNoughtDb(int x, double DN);

    public double getSigmaNoughtDb(int[] pixel, double value, double incidence_angle);

    public double getSigmaNoughtDb(double betaNoughtDb, double incidence_angle);
}
