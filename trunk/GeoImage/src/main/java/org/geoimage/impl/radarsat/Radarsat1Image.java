package org.geoimage.impl.radarsat;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoTransformFactory;
import org.geoimage.impl.Gcp;
import org.geoimage.impl.envi.EnvisatImage;
import org.geoimage.utils.Constant;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.operation.MathTransform;

/**
 * Class that read Radarsat1 Images (CEOS format). Most level1 formats are supported
 * @author thoorfr
 */
public class Radarsat1Image extends SarImageReader {
	protected int xSize = -1;
	protected int ySize = -1;
	
    private RandomAccessFile dat;
    private RandomAccessFile lea;
    private RandomAccessFile tra;
    private final int xOffset = 192;
    private final int offsetImage = 16252;
    private byte[] preloadedData;
    private int[] preloadedInterval = new int[]{0, 0};
    private int numberOfBytes = 2;
    private String directory;
    private boolean logScaling = false;
    private Rectangle bounds;
    /**
     *  all the metadata declaration
     */
    public static String CEOS_WIDTH = "CEOS_WIDTH";
    public static String CEOS_HEIGHT = "CEOS_HEIGHT";
    public static String CEOS_BYTES_PER_PIXEL = "CEOS_BYTES_PER_PIXEL";
    private boolean nearRangeFirst;
    private double[][] srgrCoefTab = {
        {8.4e5, 3.29e-1, 6.04e-7, -2.38e-13, -1.22e-20, 1.98e-26}, // S1
        {9.58e5, 5.87e-1, 4.04e-7, -2.50e-13, 7.07e-20, 3.40e-26}, // S5
        {1.026e6, 6.68e-1, 3.35e-7, -2.20e-13, 8.99e-20, -1.82e-27}, // S6
        {1.06e6, 6.99e-1, 2.96e-7, -1.98e-13, 8.91e-20, -1.36e-26}, // S7
        {9.62e5, 5.92e-1, 4.00e-7, -2.48e-13, 6.967e-20, 4.50e-26}, // F1
        {1.02e6, 6.62e-1, 3.33e-7, -2.18e-13, 8.66e-20, 6.46e-27}, // F3
        {1.05e6, 6.99e-1, 3.15e-7, -2.13e-13, 9.38e-20, -6.146e-27}, // F4
        {1.08e6, 7.10e-1, 2.86e-7, -1.91e-13, 8.78e-20, -1.21e-26}, // F5
        {9.12e5, 5.08e-1, 4.75e-7, -2.68e-13, 3.39e-20, 7.20e-26}, // W2
        {9.134e5, 5.059e-1, 4.78e-7, -2.73e-13, 5.383e-20, 3.44e-26}
    }; // sn
    private double[] srgr_coef;
    // RadarSat-1 file constants
    private int offset_dataset = 720;
    private int offset_processingparam = 40276;
    private int srgrCoefLine;
    private double earthradial;

    public Radarsat1Image() {
    }

    @Override
    public int getNBand() {
        return 1;
    }

    /**
     *  Gets the access rights:<br>&quot;r&quot; = read only<br>&quot;rw&quot; = read/write
     */
    @Override
    public String getAccessRights() {
        return "r";
    }

    @Override
    public String[] getFilesList() {
        return new String[]{displayName, lea == null ? "No LEA" : lea.toString(), tra == null ? "No TRA" : tra.toString()};
    }

    /**
     *  Initialises the image
     *  @return true if correctly initialised, false if not
     */
    @Override
    public boolean initialise(File f) {
        try {
        	super.imgName=f.getParentFile().getName();
        	this.displayName=f.getName();
        	setFile(f);
            if (dat == null) {
                return false;
            }
            String imageType = getStringValue(dat, 48, 14);
            if (!imageType.startsWith("RSAT-1")) {
                return false;
            }
            char chartype = getCharValue(dat, 219);
            setMetadata(NUMBER_BYTES, chartype);
            if (chartype == '8') {
                numberOfBytes = 1;
            }
            if (!openFiles()) {
                return false;
            }
            xSize = getIntegerValue(dat, 251, 5);
            ySize = getIntegerValue(dat, 239, 5);
            setMetadata(WIDTH, xSize);
            setMetadata(HEIGHT, ySize);
            if (!extractGcps()) {
                return false;
            }
            geotransform = GeoTransformFactory.createFromGcps(gcps, "EPSG:4326");
            bounds = new Rectangle(0, 0, xSize, ySize);
            setBand(1);
            return extractMetadata();
        } catch (IOException ex) {
            dispose();
            Logger.getLogger(Radarsat1Image.class.getName()).log(Level.SEVERE, "Can't read properly the file as Radarsat1", ex);
        }
        return false;

    }

    public void setFile(File imageFile) {
        displayName = imageFile.getPath();
        try {
            dat = new RandomAccessFile(imageFile, "r");
            directory = imageFile.getParent();
        } catch (Exception ex) {
            dispose();
            Logger.getLogger(Radarsat1Image.class.getName()).log(Level.SEVERE, "Cannot access the file " + imageFile.getName(), ex);
        }
    }

    private boolean extractGcps() {
        int pointer = 0;
        int nbOfGcps = 300;
        int yJump = (ySize / (nbOfGcps / 3 - 1) - 2) * (xSize * numberOfBytes + 192);
        int xrest = xSize * numberOfBytes + 36;
        gcps = new Vector<Gcp>();
        pointer = offsetImage;
        for (int i = 0; i < nbOfGcps / 3; i++) {
            try {
                pointer += 12;
                dat.seek(pointer);
                int temp = dat.readInt();
                Gcp[] lineGcps = new Gcp[3];
                lineGcps[0] = new Gcp();
                lineGcps[0].setXpix(1);
                lineGcps[0].setYpix(temp);

                lineGcps[1] = new Gcp();
                lineGcps[1].setXpix(xSize / 2);
                lineGcps[1].setYpix(temp);

                lineGcps[2] = new Gcp();
                lineGcps[2].setXpix(xSize);
                lineGcps[2].setYpix(temp);
                pointer += 120;
                dat.seek(pointer);
                lineGcps[0].setYgeo(dat.readInt() / 1000000.);
                lineGcps[1].setYgeo(dat.readInt() / 1000000.);
                lineGcps[2].setYgeo(dat.readInt() / 1000000.);

                lineGcps[0].setXgeo(dat.readInt() / 1000000.);
                lineGcps[1].setXgeo(dat.readInt() / 1000000.);
                lineGcps[2].setXgeo(dat.readInt() / 1000000.);


                gcps.add(lineGcps[0]);
                gcps.add(lineGcps[1]);
                gcps.add(lineGcps[2]);

                pointer += 24 + xrest + yJump;
            } catch (IOException ex) {
                Logger.getLogger(Radarsat1Image.class.getName()).log(Level.SEVERE, "Cannot get the gcps", ex);
                return false;
            }
        }
        return true;

    }

    private boolean extractMetadata() {
        try {
            setMetadata(SATELLITE, "RADARSAT-1");
            setMetadata(SENSOR, "SAR Payload Module");
            initializeModeBeamProduct();
            setMetadata(PRODUCT, getStringValue(dat, 59, 3));
            setMetadata(TYPE, getStringValue(dat, 48, 16));
            setMetadata(PROCESSOR, getStringValue(lea, 1782, 16));
            // convert time position into an offset value
            String time = null;
            String date = null;
            int year = 0;
            int day = 0;
            year = getIntegerValue(lea, 40276 + 149, 4);
            day = getIntegerValue(lea, 40276 + 149 + 5, 3);
            date = getDate(day, year);
            time = getStringValue(lea, 40276 + 149 + 9, 12);
            setMetadata(TIMESTAMP_START, date + " " + time);
            year = getIntegerValue(lea, 40276 + 170, 4);
            day = getIntegerValue(lea, 40276 + 170 + 5, 3);
            date = getDate(day, year);
            time = getStringValue(lea, 40276 + 170 + 9, 12);
            setMetadata(TIMESTAMP_STOP, date + " " + time);
            setMetadata(ENL, String.valueOf(org.geoimage.impl.ENL.getFromGeoImageReader(this)));
            setMetadata(HEADING_ANGLE, String.valueOf(this.getImageAzimuth()));
            setMetadata(POLARISATION, "HH");
            lea.seek(offset_processingparam + 533);
            setMetadata(ORBIT_DIRECTION, new String(lea.readByte() == 'A' ? "ASCENDING" : "DESCENDING"));
            if (((String) getMetadata(ORBIT_DIRECTION)).equalsIgnoreCase("ASCENDING")) {
                nearRangeFirst = true;
            } else {
                nearRangeFirst = false;
            }
            lea.seek(offset_dataset + 1703 - 1);
            byte[] AzimuthString = new byte[16];
            lea.read(AzimuthString, 0, 16);
            setMetadata(AZIMUTH_SPACING, new String(AzimuthString));
            lea.seek(offset_dataset + 1687 - 1);
            byte[] RangeString = new byte[16];
            lea.read(RangeString, 0, 16);
            setMetadata(RANGE_SPACING, new String(RangeString));
            lea.seek(offset_processingparam + 4649 - 1);
            byte[] radialsatString = new byte[16];
            lea.read(radialsatString, 0, 16);
            double radialsat = Double.parseDouble(new String(radialsatString));
            MathTransform convert;
            double[] latlon = getGeoTransform().getGeoFromPixel(0.0, 0.0, "EPSG:4326");
            double[] position = new double[3];
            convert = CRS.findMathTransform(DefaultGeographicCRS.WGS84, DefaultGeocentricCRS.CARTESIAN);
            convert.transform(latlon, 0, position, 0, 1);
            earthradial = Math.pow(position[0] * position[0] + position[1] * position[1] + position[2] * position[2], 0.5);
            setMetadata(SATELLITE_ALTITUDE, String.valueOf(radialsat - earthradial));
            setMetadata(SATELLITE_ORBITINCLINATION, "98.5795");
            lea.seek(offset_processingparam + 4649 - 1);
            byte[] ephorbData = new byte[16];
            lea.read(ephorbData, 0, 16);
            double satellite_speed = Math.pow(3.986005e14 / Double.valueOf(new String(ephorbData)), 0.5);
            setMetadata(SATELLITE_SPEED, String.valueOf(satellite_speed));
            lea.seek(offset_processingparam + 958 + 1);
            byte[] PRFString = new byte[16];
            lea.read(PRFString, 0, 16);
            setMetadata(PRF, new String(PRFString));
            lea.seek(offset_dataset + 501 + 1);
            byte[] radarWavelength = new byte[16];
            lea.read(radarWavelength, 0, 16);
            setMetadata(RADAR_WAVELENGTH, new String(radarWavelength));
            setMetadata(REVOLUTIONS_PERDAY, String.valueOf(14.29988851));
            // for the constant calibration, with the radarsat we take the
            // middle value of the look up table (line Constant.TILE_SIZE)
            lea.seek(65922 + 88 + 16 * Constant.TILE_SIZE);
            byte[] KString = new byte[16];
            lea.read(KString, 0, 16);
            setMetadata(K, new String(KString));

            // get the SRGR parameters values
            int pointer;
            srgr_coef = new double[6];
            for (int i = 0; i <= 5; i++) {
                pointer = offset_processingparam + 4908 - 1 + i * 16;
                srgr_coef[i] = getExpValue(lea, pointer, 16);
            }
            /*
            if (temp[0].equals("dat")) {
            int offset = Integer.parseInt(temp[1]);
            int length = Integer.parseInt(temp[2]);
            if (temp[4].equals("ascii")) {
            try {
            String value = getStringValue(dat, offset, length);
            setMetadata(key, value);
            } catch (IOException ex) {
            Logger.getLogger(Radarsat1Image.class.getName()).log(Level.SEVERE, "can't extract metadata for key " + key, ex);
            }
            }
            } else if (temp[0].equals("lea")) {
            int offset = Integer.parseInt(temp[1]);
            int length = Integer.parseInt(temp[2]);
            if (temp[4].equals("ascii")) {
            try {
            String value = getStringValue(lea, offset, length);
            setMetadata(key, value);
            } catch (IOException ex) {
            Logger.getLogger(Radarsat1Image.class.getName()).log(Level.SEVERE, "can't extract metadata for key " + key, ex);
            }
            }
             */

            if ((srgr_coef[0] < 500000) || (srgr_coef[5] == -1)) {
                System.out.println("srgr coefficients from the header are not good, default coefficients are applied...");
                String imgBeam = (String) getMetadata(BEAM);
                String imgMode = (String) getMetadata(MODE);
                if (imgBeam.equalsIgnoreCase("S1")) {
                    srgrCoefLine = 0;
                }
                if (imgBeam.equalsIgnoreCase("S5")) {
                    srgrCoefLine = 1;
                }
                if (imgBeam.equalsIgnoreCase("S6")) {
                    srgrCoefLine = 2;
                }
                if (imgBeam.equalsIgnoreCase("S7")) {
                    srgrCoefLine = 3;
                }
                if (imgBeam.equalsIgnoreCase("F1")) {
                    srgrCoefLine = 4;
                }
                if (imgBeam.equalsIgnoreCase("F3")) {
                    srgrCoefLine = 5;
                }
                if (imgBeam.equalsIgnoreCase("F4")) {
                    srgrCoefLine = 6;
                }
                if (imgBeam.equalsIgnoreCase("F5")) {
                    srgrCoefLine = 7;
                }
                if (imgBeam.equalsIgnoreCase("W2")) {
                    srgrCoefLine = 8;
                }
                if (imgMode.equalsIgnoreCase("SN")) {
                    srgrCoefLine = 9;
                }
                for (int i = 0; i <= 5; i++) {
                    srgr_coef[i] = srgrCoefTab[srgrCoefLine][i];
                }
            }

            // get incidence angles from gcps and convert them into radians
            double firstIncidenceangle = getIncidence(0);
            double lastIncidenceAngle = getIncidence(getWidth());
            setMetadata(INCIDENCE_NEAR, String.valueOf(firstIncidenceangle < lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle));
            setMetadata(INCIDENCE_FAR, String.valueOf(firstIncidenceangle > lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle));

        } catch (Exception ex) {
            Logger.getLogger(Radarsat1Image.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    private String getStringValue(RandomAccessFile file, int pointer, int length) throws java.io.IOException {
        file.seek(pointer);
        byte[] bs = new byte[length];
        file.readFully(bs);
        return new String(bs);
    }

    private int getIntegerValue(RandomAccessFile file, int pointer, int nbDigits) throws java.io.IOException {
        return new Integer(getStringValue(file, pointer, nbDigits).trim());
    }

    @Override
    public int read(int x, int y) {
        int result = 0;
        long temp = 0;
        byte[] pixelByte = new byte[2];
        if (x >= 0 & y >= 0 & x < xSize & y < ySize) {
            if (numberOfBytes == 2) {
                try {
                    temp = (y * (xOffset + xSize * 2) + xOffset + x * 2);
                    dat.seek(temp + offsetImage);
                    dat.read(pixelByte, 0, 2);
                    int interm1 = pixelByte[0];
                    int interm2 = pixelByte[1];
                    result = ((interm1 & 0xff) << 8 | interm2 & 0xff);
                } catch (IOException e) {
                    Logger.getLogger(EnvisatImage.class.getName()).log(Level.SEVERE, "cannot read pixel (" + x + "," + y + ")", e);
                }
            } else {
                try {
                    temp = (y * (xOffset + xSize) + xOffset + x);
                    dat.seek(temp + offsetImage);
                    dat.read(pixelByte, 0, 1);
                    result = pixelByte[0];
                } catch (IOException e) {
                    Logger.getLogger(EnvisatImage.class.getName()).log(Level.SEVERE, "cannot read pixel (" + x + "," + y + ")", e);
                }
            }
        }

        return result;
    }

    @Override
    public String getBandName(int band) {
        return "H/H";
    }

    @Override
    public void setBand(int band) {
        //do nothing since there is one single band
    }

    @Override
    public void preloadLineTile(int y, int length) {
        preloadedInterval = new int[]{y, y + length};
        if (numberOfBytes == 2) {
            int tileOffset = offsetImage + (y * (xSize * 2 + xOffset));
            preloadedData = new byte[(xSize * 2 + xOffset) * length];
            try {
                dat.seek(tileOffset);
                dat.read(preloadedData);
            } catch (IOException e) {
                Logger.getLogger(Radarsat1Image.class.getName()).log(Level.SEVERE, "cannot preload the line tile", e);
            }
        } else {
            int tileOffset = offsetImage + (y * (xSize + xOffset));
            preloadedData = new byte[(xSize + xOffset) * length];
            try {
                dat.seek(tileOffset);
                dat.read(preloadedData);
            } catch (IOException e) {
                Logger.getLogger(Radarsat1Image.class.getName()).log(Level.SEVERE, "cannot preload the line tile", e);
            }
        }

    }

    @Override
    public int[] readTile(int x, int y, int width, int height) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect = rect.intersection(bounds);
        int[] tile = new int[height * width];
        if (rect.isEmpty()) {
            return tile;
        }
        if (rect.y != preloadedInterval[0] || rect.y + rect.height != preloadedInterval[1]) {
            preloadLineTile(rect.y, rect.height);
        }
        if (numberOfBytes == 2) {
            int yOffset = xOffset + 2 * xSize;
            int xinit = rect.x - x;
            int yinit = rect.y - y;
            for (int i = 0; i < rect.height; i++) {
                for (int j = 0; j < rect.width; j++) {
                    int temp = i * yOffset + 2 * j + 2 * rect.x + xOffset;
                    tile[(i + yinit) * width + j + xinit] = ((preloadedData[temp] & 0xff) << 8) | (preloadedData[temp + 1] & 0xff);
                }
            }
            return tile;
        } else if (numberOfBytes == 1) {
            int yOffset = xOffset + xSize;
            int xinit = rect.x - x;
            int yinit = rect.y - y;
            for (int i = 0; i < rect.height; i++) {
                System.arraycopy(preloadedData, i * yOffset + rect.x, tile, (i + yinit) * width + xinit, rect.width);
            }
            return tile;
        }

        return null;
    }
    /*
    public IntTile readTile(int x, int y, int width, int height) {
    if (y + height > ySize) {
    y = ySize - height;
    }
    if (y < 0) {
    y = 0;
    }
    if (x + width > xSize) {
    x = xSize - width;
    }
    if (x < 0) {
    x = 0;
    }
    if (y < preloadedInterval[0] || y + height > preloadedInterval[1]) {
    preloadLineTile(y, height);
    }
    if (numberOfBytes == 2) {
    IntTile tile = new IntTile(x, y, width, height);
    int[] data = new int[height * width];
    int index = 0;
    int yOffset = xOffset + 2 * xSize;
    for (int i = 0; i < height; i++) {
    for (int j = 0; j < width; j++) {
    data[index++] = ((preloadedData[i * yOffset + 2 * j + 2 * x + xOffset] & 0xff) << 8) | (preloadedData[i * yOffset + 2 * j + xOffset + 2 * x + 1] & 0xff);
    }
    }
    tile.setData(data);
    return tile;
    } else if (numberOfBytes == 1) {
    IntTile tile = new IntTile(x, y, width, height);
    int[] data = new int[height * width];
    int index = 0;
    int yOffset = xOffset + xSize;
    for (int i = 0; i < height; i++) {
    for (int j = 0; j < width; j++) {
    data[index++] = preloadedData[i * yOffset + j + x + xOffset];
    }
    }
    tile.setData(data);
    return tile;
    }
    return null;
    }
     */

    @Override
    public int[] readAndDecimateTile(int x, int y, int width, int height, int outWidth, int outHeight,int xSize, int ySize, boolean filter) {
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
                    int[] temp = readAndDecimateTile(x, y + i * Constant.TILE_SIZE, width, tileHeight, outWidth, Math.round(tileHeight / decY),xSize,ySize, filter);
                    if (temp != null) {
                        for (int j = 0; j < temp.length; j++) {
                            if (index < outData.length) {
                                outData[index++] = temp[j];
                            }
                        }
                    } else {
                        index += outWidth * (int) (Constant.TILE_SIZE / decY);
                    }
                }
            }
            return outData;
        }
    }

    private String getDate(int value, int year) {
        String date;
        int february = 28, i = -1;
        if (year % 4 == 0) {
            february = 29;
        }
        int[] daysMonth = {31, february, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        while (value > 0) {
            i++;
            value -= daysMonth[i];
        }
        String day = "";
        String month = "";
        if (i + 1 < 10) {
            month = "0" + String.valueOf(i + 1);
        } else {
            month = String.valueOf(i + 1);
        }
        if (value + daysMonth[i] < 10) {
            day = "0" + String.valueOf(value + daysMonth[i]);
        } else {
            day = String.valueOf(value + daysMonth[i]);
        }
        date = String.valueOf(year) + "-" + month + "-" + day;
        return date;
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            if (dat != null) {
                dat.close();
            }
            if (lea != null) {
                lea.close();
            }
            if (tra != null) {
                tra.close();
            }
            lea = null;
            tra = null;
            dat = null;
        } catch (IOException ex) {
            Logger.getLogger(Radarsat1Image.class.getName()).log(Level.SEVERE, "Can't close al the files", ex);
        }
    }

    private boolean openFiles() {
        String[] allFiles = null;
        File check = null;
        lea = null;
        float fileLength;
        int i = 0;

        File dir = new File(directory);
        allFiles = dir.list();

        while (i < allFiles.length) {
            // This section searches info files on the list: lea or trailer
            check = new File(directory + File.separator + allFiles[i]);
            fileLength = check.length();
            if (fileLength == 92618 || fileLength == 9680) { //leader file
                try {
                    lea = new RandomAccessFile(check.getAbsolutePath(), "r");
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Radarsat1Image.class.getName()).log(Level.SEVERE, "Cannot open lea file " + check.getName(), ex);
                }
            }
            if (fileLength == 720 || fileLength == 83658) { //tra file
                try {
                    tra = new RandomAccessFile(check.getAbsolutePath(), "r");
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Radarsat1Image.class.getName()).log(Level.SEVERE, "Cannot open tra file " + check.getName(), ex);
                }
            }
            if (allFiles[i].equalsIgnoreCase("flaglogscaling")) {
                logScaling = true;
            }
            i++;
        }
        return lea != null;
    }

    @Override
    public int getNumberOfBytes() {
        return numberOfBytes;
    }

    @Override
    public int getType(boolean oneBand) {
        //only one band
        if (numberOfBytes == 1) {
            return BufferedImage.TYPE_BYTE_GRAY;
        } else {
            return BufferedImage.TYPE_USHORT_GRAY;
        }
    }

    @Override
    public String getFormat() {
        return getClass().getCanonicalName();
    }

    @Override
    public double getSlantRange(int pos_range) {
        double slant_range = 0;
        double sampleDistRange = getGeoTransform().getPixelSize()[1];
        int j = 0;

        if (nearRangeFirst) {
            j = pos_range;
        } else {
            j = getWidth() - pos_range;
        }
        slant_range = srgr_coef[0] + j * sampleDistRange * srgr_coef[1] + Math.pow(j * sampleDistRange, 2) * srgr_coef[2] + Math.pow(j * sampleDistRange, 3) * srgr_coef[3] + Math.pow(j * sampleDistRange, 4) * srgr_coef[4] + Math.pow(j * sampleDistRange, 5) * srgr_coef[5];

        return slant_range;
    }

    @Override
    public double getIncidence(int pos_range) {
        double incidence = 0;
        double denominator, numerator, tan;
        double slant_range;
        double r, h;

        h = Double.parseDouble((String) getMetadata(SATELLITE_ALTITUDE));

        // Calculate the slant range for the incidence angle
        slant_range = this.getSlantRange(pos_range);

        // Calculate the incidence angle
        numerator = Math.pow(h, 2) - Math.pow(slant_range, 2) + 2 * earthradial * h;
        denominator = 2 * slant_range * earthradial;
        incidence = (Math.acos(numerator / denominator));

        // Calculate the beam elevation angle
        double elevationAngle = Math.asin(Math.sin(incidence * Math.PI / 180) * (earthradial / (earthradial + h)));

        return incidence;
    }

    /**
     * @param file
     *            :The file in which it reads
     * @param pointer
     *            :Location in the file
     * @param nbBytes :
     *            number of bytes used for the value
     * @return
     * @throws IOException
     * @throws IOException
     */
    public double getExpValue(RandomAccessFile file, int pointer, int nbBytes) throws IOException {
        String convert = "";
        double data = 0;
        int i;

        for (i = 0; i < nbBytes; i++) {
            convert += getCharValue(file, pointer++);        // System.out.println("getExpValue : "+convert);
        }
        convert = convert.trim();
        if (convert.equalsIgnoreCase("")) {
            System.out.println("getExpValue, nothing at this place : " + pointer);
            data = -1;
        } else {
            data = Double.valueOf(convert);
        }
        return data;
    }

    /**
     * Fetches a field to discriminate between Narrow mode,Standard mode
     */
    public void initializeModeBeamProduct() {
        int Pos_Rsat = 59;
        String imgMode = "";
        String imgBeam = "";
        try {
            String imgProduct = getStringValue(dat, Pos_Rsat, 3);
            if (imgProduct.equalsIgnoreCase("scn")) {
                imgMode = "SN";
                // there are 2 beams in SNA combination and 3 in SNB
                if (getIntegerValue(lea, offset_processingparam + 930, 1) == 2) {
                    imgBeam = "SNA";
                } else {
                    imgBeam = "SNB";
                }
            } else {
                imgBeam = getStringValue(lea, offset_processingparam + 931, 2);
                imgMode = "" + imgBeam.charAt(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        setMetadata(BEAM, imgBeam);
        setMetadata(MODE, imgMode);

    }

    public String getInternalImage() {
  		return null;
  	}

	@Override
	public int getWidth() {
		return xSize;
	}

	@Override
	public int getHeight() {
		return ySize;
	}

	@Override
	public File getOverviewFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean supportAzimuthAmbiguity() {
		return true;
	}
	@Override
	public String getImgName() {
		return imgName;
	}
}

