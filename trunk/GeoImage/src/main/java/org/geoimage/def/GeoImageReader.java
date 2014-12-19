package org.geoimage.def;

import java.io.File;
import java.util.List;

import org.geoimage.impl.Gcp;
import org.geoimage.utils.IProgress;

/**
 * Interface to be implemented by all the readers for "geographic" images
 */
public interface GeoImageReader extends GeoMetadata {
    /**
     * version of the reader
     */
    public static final String version = "1.0beta";

    /**
     *
     * @return the current displayed image band
     */
    public int getBand();

    /**
     *
     * @return the image bounding box in lattitude and longitude coordinates
     */
    public List<double[]> getFrameLatLon(int xSize,int ySize);

    /**
     *
     * @return the name of the image
     */
    public String getName();

    /**
     *
     * @return the width of the image raster in pixels
     */
    public int getWidth();

    /**
     *
     * @return the length of the image in pixels
     */
    public int getHeight();

    /**
     *
     * @return the number of bands of the image
     */
    public int getNBand();

    /**
     *
     * @return the format of the image, should be the Class name
     */
    public String getFormat();

    /**
     * @return a String with a human readable description of the image
     */
    public String getDescription();

    /**
     *
     * @return the number of bytes coding one pixel of one band. So far it is supposed
     * to be the same for each band
     */
    public int getNumberOfBytes();

    /**
     * @param oneBand whether it is for 1 band or all together
     * @return One of the BufferedImage.TYPE_....
     * @parameter: oneBand means one single access type (in case of 16 bits per band like Envisat)
     * if false for multi bands access, the user will now that he should downsize each band to 8 bits
     */
    public int getType(boolean oneBand);

    /**
     *  Gets the WKT form of the projection system as defined by OpenGIS
     * @see org.geoimage.impl.AffineGeoTransform
     * @see org.geoimage.impl.GcpsGeoTransform
     * @return an implementation of Geotransform.
     */
    public GeoTransform getGeoTransform();

    /**
     *  @return the tie points (or gcps) of the image.
     */
    public List<Gcp> getGcps();

    /**
     *  Gets the access rights:<br>&quot;r&quot; = read only<br>&quot;rw&quot; = read/write
     * @return
     */
    public String getAccessRights();

    /**
     *
     * @return the Files absolute path that belongs to the image
     */
    public String[] getFilesList();

    /**
     * Initialises the image
     * @return whether the image has been properly initialised, thus readable
     */
    public boolean initialise(File file);


    /**
     * Reads the data in int[]. Access the pixels value (x,y): data[x+y*width].
     * A call to preloadLineTile can be considered to improve memory management.
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public int[] readTile(int x, int y, int width, int height);

    /**
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param outWidth
     * @param outLength
     * @param filter
     * @return
     */
    public int[] readAndDecimateTile(int x, int y, int width, int height, int outWidth, int outLength,int xSize,int ySize, boolean filter);

    /**
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param scalingFactor
     * @param filter
     * @param progressbar
     * @return
     */
    public int[] readAndDecimateTile(int x, int y, int width, int height, double scalingFactor, boolean filter, IProgress progressbar);

    /**
     * Reads single pixel value of the curent band.
     * @param x
     * @param y
     * @return pixel value
     */
    public int read(int x, int y);

    /**
     * Return the name of the band number. For instance in RGBA image this would be:
     * 0 = Red
     * 1 = Green
     * 2 = Blue
     * 3 = Alpha
     * @param band number
     * @return
     */
    public String getBandName(int band);

    /**
     * Sets the band to be read (before the call to Read, ReadTile, etc...)
     * @param band
     */
    public void setBand(int band);

    /**
     * Method to manage the memory for big images. The data within the interval
     * (y,y+height) is preloaded in memory for fast access for readTile.
     * @param y first line of the part of the image to preload.
     * @param height of the part of the image to preload.
     */
    public void preloadLineTile(int y, int height);

    /**
     * clear all the resources opened to read the image
     */
    public void dispose();

    public String getInternalImage();

    
    public GeoImageReader clone();

    public File getOverviewFile();
    
    public boolean supportAzimuthAmbiguity();
}

