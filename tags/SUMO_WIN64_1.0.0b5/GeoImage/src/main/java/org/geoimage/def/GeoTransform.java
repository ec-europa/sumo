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
    /**
     * Computes the subpixel (i.e. precision greater than integer) location of a map coordinates
     * @param xgeo is the longitude
     * @param ygeo is the latitude
     * @param inputWktProjection is the projection system of (xgeo, ygeo) (for instance "EPSG:4326")
     * @return [xpixel, ypixel]
     */
    public double[] getPixelFromGeo(double xgeo, double ygeo, String inputWktProjection);
    /**
     * Computes the map coordinates given the pixel location in the image reference
     * @param xpix the pixel location in x
     * @param ypix the pixel location in y
     * @param outputWktProjection is the projection system of the result (for instance "EPSG:4326")
     * @return [longitude, latitude]
     */
    public double[] getGeoFromPixel(double xpix, double ypix, String outputWktProjection);
    /**
     * Computes the associated list of subpixel (i.e. precision greater than integer)
     * locations of a list of map coordinates
     * @param src is the list in the form of [lon1, lat1, lon2, lat2, ...., lonN, latN]
     * @param srcOffset is the offset of the src list to start the transformation (often 0)
     * @param dest is the list of outputs the size shoud be at least "new double[numPoints - srcOfsset + destOffset]"
     * @param destOffset the offset where the dest should receive the computed points
     * @param numPoints the number of points to be computed (numpoints < src.lenght-srcOffset)
     * @param inputWktProjection the projection of the map coordinates (example: "EPSG:4326")
     * @return the exact list  given in arguments as dest with the computesd points in the form [x1,y1,....,xN, yN]
     */
    public double[] getPixelFromGeo(double[] src, int srcOffset, double[] dest, int destOffset,int numPoints, String inputWktProjection);
    /**
     * Computes the associated list of map coordinates locations from of a list of pixel coordinates
     * @param src is the list in the form of [x1,y1,....,xN, yN]
     * @param srcOffset is the offset of the src list to start the transformation (often 0)
     * @param dest is the list of outputs the size shoud be at least "new double[numPoints - srcOfsset + destOffset]"
     * @param destOffset the offset where the dest should receive the computed points
     * @param numPoints the number of points to be computed (numpoints < src.lenght-srcOffset)
     * @param inputWktProjection the projection of the map coordinates (example: "EPSG:4326")
     * @return the exact list  given in arguments as dest with the computesd points in the form[lon1, lat1, lon2, lat2, ...., lonN, latN]
     */
    public double[] getGeoFromPixel(double[] src, int srcOffset, double[] dest, int destOffset,int numPoints, String outputWktProjection);

    /**
     * set an extra translation to the transformation if this is a corrective
     * factor that is not in the originally defined image geolocation.
     * If not used default is x=0, y=0
     * @param x number of pixels of the translation in x direction
     * @param y number of pixels of the translation in y direction
     */
    public void setTransformTranslation(int x, int y);

    /**
     *
     * @return the pixel translation applied to the geotransform in the form of [delta x,delta y]
     */
    public int[] getTransformTranslation();

    /**
     *
     * @return return the pixelsize in metres in the range and azimuth direction [x,y]
     */
    public double[] getPixelSize();   
}
