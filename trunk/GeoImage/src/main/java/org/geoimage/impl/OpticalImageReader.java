/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.impl;

import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.GeoTransform;
import org.geoimage.def.OpticalMetadata;
import org.geoimage.utils.Constant;
import org.geoimage.utils.IProgress;
import org.geotools.referencing.GeodeticCalculator;

/**
 * An abtract class that provides default implementation of methods to access raster data
 * this should be used to create your own Optical Image reader lie:
 * class MyOpticalImageReader extends OpticalImageReader {
 * ...
 * }
 * @author leforth
 */
public abstract class OpticalImageReader implements GeoImageReader, OpticalMetadata {

    protected static int MAXTILESIZE = 16 * 1024 * 1024;
    protected int xSize = -1;
    protected int ySize = -1;
    protected String name = "";
    protected List<Gcp> gcps;
    private HashMap<String, Object> metadata = new HashMap<String, Object>();
    protected GeoTransform geotransform;
    protected int band = 0;

    public int getBand() {
        return this.band;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return xSize;
    }

    public int getHeight() {
        return ySize;
    }

    public HashMap<String, Object> getMetadata() {
        return (HashMap<String, Object>) metadata.clone();
    }

    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    public List<Gcp> getGcps() {
        return gcps;
    }

    public GeoTransform getGeoTransform() {
        return geotransform;
    }

    public int[] readAndDecimateTile(int x, int y, int width, int height, int outWidth, int outHeight, boolean filter) {
        if (x + width < 0 || y + height < 0 || x > xSize || y > ySize) {
            return new int[outWidth * outHeight];
        }

        if (height < 257) {
            int[] outData = new int[outWidth * outHeight];
            int[] data = readTile(x, y, width, height);
            int decX = Math.round(width / (1f * outWidth));
            int decY = Math.round(height / (1f * outHeight));
            if (data != null) {
                int index = 0;
                for (int j = 0; j < outHeight; j++) {
                    int temp = (int) (j * decY) * width;
                    for (int i = 0; i < outWidth; i++) {
                        if (filter) {
                            for (int h = 0; h < decY; h++) {
                                for (int w = 0; w < decX; w++) {
                                    outData[index] += data[temp + h * width + (int) (i * decX + w)];
                                }
                            }
                            if (decX > 1) {
                                outData[index] /= (int) decX;
                            }
                            if (decY > 1) {
                                outData[index] /= (int) decY;
                            }
                        } else {
                            outData[index] = data[temp + (int) (i * decX)];
                        }
                        index++;
                    }
                }
            }
            return outData;
        } else {
            float incy = height / 256f;
            int[] outData = new int[outWidth * outHeight];
            float decY = height / (1f * outHeight);
            int index = 0;
            for (int i = 0; i < Math.ceil(incy); i++) {
                int tileHeight = (int) Math.min(Constant.TILE_SIZE, height - i * Constant.TILE_SIZE);
                if (tileHeight > decY) {
                    int[] temp = readAndDecimateTile(x, y + i * Constant.TILE_SIZE, width, tileHeight, outWidth, Math.round(tileHeight / decY), filter);
                    if (temp != null) {
                        for (int j = 0; j < temp.length; j++) {
                            if (index < outData.length) {
                                outData[index++] = temp[j];
                            }
                        }
                    } else {
                        index += outWidth * (int) (Constant.TILE_SIZE / decY);
                    }
                    temp = null;
                    //System.gc();
                }
            }
            return outData;
        }
    }

    public int[] readAndDecimateTile(int x, int y, int width, int height, double scalingFactor, boolean filter, IProgress progressbar) {
        System.out.println("readAndDecimateTile(" + x + ", " + y + ", " + width + ", " + height + ")");
        int outWidth = (int) (width * scalingFactor);
        int outHeight = (int) (height * scalingFactor);
        double deltaPixelsX = (double) width / outWidth;
        double deltaPixelsY = (double) height / outHeight;
        double tileHeight = height / (((double) (width * height) / MAXTILESIZE));
        int[] outData = new int[outWidth * outHeight];
        if (height / outHeight > 4) {
            double a = width * 1.0 / outWidth;
            double b = height * 1.0 / outHeight;
            System.out.println(a + "--" + b);
            for (int i = 0; i < outHeight; i++) {
                //System.out.println(i);
                for (int j = 0; j < outWidth; j++) {
                    try {
                        outData[i * outWidth + j] = readTile((int) (x + j * a), (int) (y + i * b), 1, 1)[0];
                    } catch (Exception e) {
                    }
                }
            }
            return outData;
        }
        // load first tile
        int currentY = 0;
        int[] tile = readTile(0, currentY, width, (int) Math.ceil(tileHeight));
        if (progressbar != null) {
            progressbar.setMaximum(outHeight / 100);
        // start going through the image one Tile at a time
        }
        double posY = 0.0;
        for (int j = 0; j < outHeight; j++, posY += deltaPixelsY) {
            // update progress bar
            if (j / 100 - Math.floor(j / 100) == 0) {
                if (progressbar != null) {
                    progressbar.setCurrent(j / 100);
                // check if Tile needs loading
                }
            }
            if (posY > (int) Math.ceil(tileHeight)) {
                tile = readTile(0, currentY + (int) Math.ceil(tileHeight), width, (int) Math.ceil(tileHeight));
                posY -= (int) Math.ceil(tileHeight);
                currentY += (int) Math.ceil(tileHeight);

            }

            double posX = 0.0;
            for (int i = 0; i < outWidth; i++, posX += deltaPixelsX) {
                //System.out.println("i = " + i + ", j = " + j + ", posX = " + posX + ", posY = " + posY);
                outData[i + j * outWidth] = tile[(int) posX * (int) posY];
            }
            //System.gc();
        }

        return outData;
    }

    public double getImageAzimuth() {
        double az = 0;

        //compute the azimuth considering the two left corners of the image
        //azimuth angle in degrees between 0 and +180
        double[] endingPoint = getGeoTransform().getGeoFromPixel(getWidth() / 2, 0, "EPSG:4326");
        double[] startingPoint = getGeoTransform().getGeoFromPixel(getWidth() / 2, getHeight() - 1, "EPSG:4326");
        GeodeticCalculator gc = new GeodeticCalculator();
        gc.setStartingGeographicPoint(startingPoint[0], startingPoint[1]);
        gc.setDestinationGeographicPoint(endingPoint[0], endingPoint[1]);
        az = gc.getAzimuth();
        return az;
    }

    public void dispose() {
    }

    public String getDescription() {
        StringBuilder description = new StringBuilder("Image Acquisition and Generation Parameters:\n")
        .append( "--------------------\n\n")
        .append( "Satellite and Instrument: ")
        .append( "\n" ).append(getMetadata(SATELLITE)).append( "  " ).append(getMetadata(SENSOR))
        .append( "\nHeading Angle: ")
        .append( getMetadata(HEADING_ANGLE))
        .append( "\nOrbit Direction: ")
        .append( getMetadata(ORBIT_DIRECTION))
        .append( "\nImage Dimensions:\n")
        .append( "\tWidth:" ).append(getMetadata(WIDTH))
        .append( "\n\tHeight:" ).append(getMetadata(HEIGHT))
        .append( "\nImage Acquisition Time:\n")
        .append( "\tStart:" ).append(getMetadata(TIMESTAMP_START))
        .append( "\n\tStop:" ).append(getMetadata(TIMESTAMP_STOP))
        .append( "\nImage Pixel Spacing:\n")
        .append( "\tAzimuth:" ).append(getMetadata(AZIMUTH_SPACING))
        .append( "\n\tRange:" ).append( getMetadata(RANGE_SPACING))
        .append( "\nImage Processor and Algorithm: ")
        .append( getMetadata(PROCESSOR))
        .append( "\nSatellite Altitude (m): ")
        .append( getMetadata(SATELLITE_ALTITUDE))
        .append( "\nSatellite Speed (m/s): ")
        .append( getMetadata(SATELLITE_SPEED))
        .append( "\nIncidence Angles (degrees):\n")
        .append( "\tNear: " ).append(getMetadata(INCIDENCE_NEAR))
        .append( "\n\tFar: ").append( getMetadata(INCIDENCE_FAR));

        return description.toString();
    }
/*
    public void geoCorrect() {
        String imagepath = this.getFilesList()[0];
        try {
            FileReader stream = new FileReader(imagepath + "sumoXML.xml");
            char[] filestream = new char[500];
            stream.read(filestream);
            String filestring = new String(filestream);
            // look for the sumo xml header
            if (filestring.contains("<!-- XML document for SUMO purposes -->")) {
                // look for the offset fields
                double longitudeshift = 0.0;
                double latitudeshift = 0.0;
                longitudeshift = Double.parseDouble(filestring.substring(filestring.indexOf("<longitude>") + 11, filestring.indexOf("</longitude>")));
                latitudeshift = Double.parseDouble(filestring.substring(filestring.indexOf("<latitude>") + 10, filestring.indexOf("</latitude>")));
                // translate the image with the file values converted back into pixels
                double[] originlatlon = getGeoTransform().getGeoFromPixelWithDefaultEps(0.0, 0.0,false);
                double[] pixelshift = getGeoTransform().getPixelFromGeoWithDefaultEps(originlatlon[0] + latitudeshift, originlatlon[1] + longitudeshift,false);
                System.out.println(pixelshift[0]);
                getGeoTransform().setTransformTranslation((int) pixelshift[0], (int) pixelshift[1]);
            }
        } catch (Exception ex) {
            Logger.getLogger(OpticalImageReader.class.getName()).log(Level.WARNING, null, ex);
        }
    }*/


    public double getIncidence(int position) {
        double incidenceangle = 0.0;
        // estimation of incidence angle based on near and range distance values
        double nearincidence = Math.toRadians(Double.parseDouble((String) getMetadata(INCIDENCE_NEAR)));
        double sataltitude = Double.parseDouble((String) getMetadata(SATELLITE_ALTITUDE));
        double distancerange = sataltitude * Math.tan(nearincidence) + position * getGeoTransform().getPixelSize()[1];
        incidenceangle = Math.atan(distancerange / sataltitude);
        return incidenceangle;
    }

    private double getSatelliteSpeed() {
        // calculate satellite speed
/*
        double seconds = ((double)(getTimestamp(GeoImageReaderBasic.TIMESTAMP_STOP).getTime() - getTimestamp(GeoImageReaderBasic.TIMESTAMP_START).getTime())) / 1000;
        // calculate satellite speed in azimuth pixels / seconds
        double azimuthpixelspeed = ((double)getHeight() * (6400000 + sataltitude) / 6400000) / seconds;
        return azimuthpixelspeed * getGeoTransform().getPixelSize()[0];
         */
        double satellite_speed = 0.0;

        // check if satellite speed has been calculated
        if (getMetadata(SATELLITE_SPEED) != null) {
            satellite_speed = Double.valueOf((String) getMetadata(SATELLITE_SPEED));
        } else {
            // Ephemeris --> R + H
            //Approaching the orbit as circular V=SQRT(GM/(R+H))
            double sataltitude = Double.parseDouble((String) getMetadata(SATELLITE_ALTITUDE));
            satellite_speed = Math.pow(3.986005e14 / (6371000 + sataltitude), 0.5);
            setMetadata(SATELLITE_SPEED, String.valueOf(satellite_speed));
        }

        return satellite_speed;
    }

    public double getSlantRange(int position) {
        double slantrange = 0.0;
        double incidenceangle = getIncidence(position);
        double sataltitude = Double.parseDouble((String) getMetadata(SATELLITE_ALTITUDE));
        // calculate slant range
        if (Math.cos(incidenceangle) != 0.0) {
            slantrange = sataltitude / Math.cos(incidenceangle);
        }
        return slantrange;
    }

    /**
     * @param file
     *            The file in which it reads
     * @param pointer
     *            Location in the file
     * @param nbBytes
     * @return
     * @throws IOException
     */
    protected float getFloatValue(RandomAccessFile file, int pointer, int nbBytes) throws IOException {
        String convert = "";
        Float temp = null;
        float data = 0;
        int i;
        for (i = 0; i < nbBytes; i++) {
            convert += getCharValue(file, pointer++);
        }
        System.out.println("getFloatValue: " + convert);
        convert = convert.trim();
        if (convert.equalsIgnoreCase("")) {
            System.out.println("getFloatValue : nothing at this place");
            data = -1;
        } else {
            temp = new Float(convert);
            data = temp.floatValue();
        }
        return data;
    }

    protected char getCharValue(RandomAccessFile file, int pointer) throws IOException {
        file.seek(pointer);
        return (char) file.readByte();
    }

    public List<double[]> getFrameLatLon() {
        if (geotransform != null) {
            ArrayList<double[]> latlonframe = new ArrayList<double[]>();

            // use the four image corners to define the image frame
            latlonframe.add(geotransform.getGeoFromPixel(0, 0, "EPSG:4326"));
            latlonframe.add(geotransform.getGeoFromPixel(xSize, 0, "EPSG:4326"));
            latlonframe.add(geotransform.getGeoFromPixel(xSize, ySize, "EPSG:4326"));
            latlonframe.add(geotransform.getGeoFromPixel(0, ySize, "EPSG:4326"));

            return latlonframe;
        }

        return null;
    }


    public GeoImageReader clone(){
        return null;
    }
}
