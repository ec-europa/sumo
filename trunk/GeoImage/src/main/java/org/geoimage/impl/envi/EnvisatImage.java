package org.geoimage.impl.envi;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoTransformFactory;
import org.geoimage.impl.Gcp;
import org.geoimage.utils.Constant;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.operation.MathTransform;

/**
 * Class to read Envisat Image. Most Formats level1 are supported
 * @author thoorfr
 */
public class EnvisatImage extends SarImageReader {
	protected int xSize = -1;
	protected int ySize = -1;
	
    protected int xOffset = 17;
    protected int offsetBand;
    protected File file;
    protected static int MPH = 1247;
    protected int SPH;
    protected RandomAccessFile fss;
    protected byte[] preloadedData;
    protected int[] preloadedInterval = new int[]{0, 0};
    protected Rectangle bounds;
    protected float[] slantRangeTimesTab = new float[11];
    protected float[] incidenceTab = new float[11];
    protected int[] sampNumberTab = new int[11];

    public EnvisatImage() {
    }

    @Override
    public int getWidth() {
        if (this.xSize == -1) {
            this.xSize = Integer.parseInt(((String) (getMetadata("LINE_LENGTH"))).replace("<samples>", "").replace("+", ""));
        }
        return this.xSize;
    }

    @Override
    public int getHeight() {
        if (this.ySize == -1) {
            this.ySize = Integer.parseInt(((String) (getMetadata("MDS1_NUM_DSR"))).replace("<bytes>", "").replace("+", ""));
        }
        return this.ySize;
    }

    @Override
    public int getNBand() {
        if (getMetadata("MDS2_TX_RX_POLAR").equals("")) {
            return 1;
        } else {
            return 2;
        }
    }

    @Override
    public String getAccessRights() {
        return "r";
    }

    @Override
    public String[] getFilesList() {
        String out = "";
        try {
            out = file.getCanonicalPath();
        } catch (IOException ex) {
            Logger.getLogger(EnvisatImage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new String[]{out};
    }

    @Override
    public boolean initialise(File file) {
    	this.name=file.getName();
    	this.file=file;
        fss = null;
        try {
            if (file == null) {
                return false;
            }
            fss = new RandomAccessFile(file, "r");
            byte[] magicb = new byte[8];
            fss.read(magicb);
            String magic = new String(magicb);
            if (!magic.equals("PRODUCT=")) {
                return false;
            }

            SPH = 0;
            String prefix = "";
            String temp;
            while (fss.getFilePointer() < MPH + SPH) {
                temp = fss.readLine();
                if (temp.contains("=")) {
                    String[] spltemp = temp.split("=");

                    if (spltemp[0].equals("SPH_SIZE")) {
                        String bob = spltemp[1];
                        bob = bob.replace("+", "");
                        SPH = Integer.parseInt(bob.replace("<bytes>", ""));
                    }
                    if (spltemp[0].equals("DS_NAME")) {
                        prefix = spltemp[1].replace("\"", "").trim() + "_";
                    }
                    spltemp[1] = spltemp[1].replace("\"", "").replace("\n", "").trim();

                    setMetadata(prefix + spltemp[0], spltemp[1]);
                    //System.out.println(prefix + spltemp[0] + "=" + spltemp[1]);

                }
            }
            name = getFilesList()[0]; //String) getMetadata("PRODUCT");
            extractGcps(fss);
            getWidth();
            setMetadata(WIDTH, xSize);
            getHeight();
            setMetadata(HEIGHT, ySize);
            getTimestamp();
            bounds = new Rectangle(0, 0, xSize, ySize);
            geotransform = GeoTransformFactory.createFromGcps(gcps, "EPSG:4326");
            setMetadata(SATELLITE, "ENVISAT");
            setMetadata(SENSOR, "ASAR");
            setMetadata(BEAM, getMetadata("SWATH"));
            setMetadata(TYPE, "ASAR");
            setMetadata(ENL, String.valueOf(org.geoimage.impl.ENL.getFromGeoImageReader(this)));
            setMetadata(HEADING_ANGLE, String.valueOf(this.getImageAzimuth()));
            // get incidence angles from gcps and convert them into radians
            float firstIncidenceangle = (float) (this.gcps.get(0).getAngle());
            float lastIncidenceAngle = (float) (this.gcps.get(this.gcps.size() - 1).getAngle());
            setMetadata(LOOK_DIRECTION, firstIncidenceangle < lastIncidenceAngle ? "RIGHT" : "LEFT");
            setMetadata(INCIDENCE_NEAR, String.valueOf(firstIncidenceangle < lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle));
            setMetadata(INCIDENCE_FAR, String.valueOf(firstIncidenceangle > lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle));
            setMetadata(POLARISATION, getMetadata("MDS1_TX_RX_POLAR") + ", " + getMetadata("MDS2_TX_RX_POLAR"));
            setMetadata(ORBIT_DIRECTION, getMetadata("PASS"));
            setMetadata(PROCESSOR, getMetadata("SOFTWARE_VER") + " with " + getMetadata("ALGORITHM"));
            //remove the <m> otherwise crashed in detected pixels
            setMetadata(AZIMUTH_SPACING, getMetadata("AZIMUTH_SPACING").toString().replace("<m>", ""));
            setMetadata(RANGE_SPACING, getMetadata("RANGE_SPACING").toString().replace("<m>", ""));
            setMetadata(MODE, getMetadata("SPH_DESCRIPTOR"));
            if(getMetadata("SPH_DESCRIPTOR").equals("Image Mode Medium Res Image")){
                //if IMM then set the ENL to 18
                setMetadata(ENL, String.valueOf(18));
            }
            double xposition = Double.parseDouble(((String) getMetadata("X_POSITION")).replaceAll("<m>", ""));
            double yposition = Double.parseDouble(((String) getMetadata("Y_POSITION")).replaceAll("<m>", ""));
            double zposition = Double.parseDouble(((String) getMetadata("Z_POSITION")).replaceAll("<m>", ""));
            double radialdist = Math.pow(xposition * xposition + yposition * yposition + zposition * zposition, 0.5);
            MathTransform convert;
            double[] latlon = getGeoTransform().getGeoFromPixel(0.0, 0.0, "EPSG:4326");
            double[] position = new double[3];
            convert = CRS.findMathTransform(DefaultGeographicCRS.WGS84, DefaultGeocentricCRS.CARTESIAN);
            convert.transform(latlon, 0, position, 0, 1);
            double earthradial = Math.pow(position[0] * position[0] + position[1] * position[1] + position[2] * position[2], 0.5);
            setMetadata(SATELLITE_ALTITUDE, String.valueOf(radialdist - earthradial));
            double xvelocity = Double.parseDouble(((String) getMetadata("X_VELOCITY")).replaceAll("<m/s>", ""));
            double yvelocity = Double.parseDouble(((String) getMetadata("Y_VELOCITY")).replaceAll("<m/s>", ""));
            double zvelocity = Double.parseDouble(((String) getMetadata("Z_VELOCITY")).replaceAll("<m/s>", ""));
            double velocity = Math.pow(xvelocity * xvelocity + yvelocity * yvelocity + zvelocity * zvelocity, 0.5);
            setMetadata(SATELLITE_SPEED, String.valueOf(velocity));
            setMetadata(SATELLITE_ORBITINCLINATION, "98.5485");
            int offset_processing = Integer.parseInt(((String) getMetadata("MAIN PROCESSING PARAMS ADS_DS_OFFSET")).replace("<bytes>", "").replace("+", ""));
            fss.seek(offset_processing + 703);
            setMetadata(PRF, String.valueOf(fss.readFloat()));
            fss.seek(offset_processing + 987);
            float radarFrequency = fss.readFloat();
            setMetadata(RADAR_WAVELENGTH, String.valueOf(299792457.9 / radarFrequency));
            setMetadata(REVOLUTIONS_PERDAY, String.valueOf(14.32247085));
            fss.seek(offset_processing + 1381);
            setMetadata(K, String.valueOf(fss.readFloat()));

        } catch (Exception ex) {
            Logger.getLogger(EnvisatImage.class.getName()).log(Level.SEVERE, null, ex);
            dispose();
            return false;
        } finally {
        }

        return true;
    }



    // </editor-fold>
    public int getMPH() {
        return MPH;
    }

    // </editor-fold>
    public int getSPH() {
        return SPH;
    }

    // </editor-fold>
    protected void extractGcps(RandomAccessFile fss) {
        int num_dsr = Integer.parseInt(((String) (getMetadata("GEOLOCATION GRID ADS_NUM_DSR"))).replace("+", ""));
        try {
            int geolocationOffset = Integer.parseInt(((String) (getMetadata("GEOLOCATION GRID ADS_DS_OFFSET"))).replace("+", "").replace("<bytes>", ""));
            Gcp[] gcps2 = new Gcp[11 * (num_dsr + 1)];
            for (int i = 0; i < gcps2.length; i++) {
                gcps2[i] = new Gcp();
            }
            fss.seek(geolocationOffset);
            int pointer = geolocationOffset;

            /* read the number of lines (num_lines) in each slice, to add at each num_dsr cicle
             * For a stripline product, which may consist of multiple slices in a single MDS,
             * line_num is reset to 1 at the beginning of each slice, use num_lines instead
             */
            int pointer2 = pointer + 17;
            fss.seek(pointer2);
            int num_lines = fss.readInt();
            pointer -= 521;

            for (int i = 0; i < num_dsr; i++) {
                pointer = pointer + 521;
                //jump to the block of X values
                pointer2 = pointer + 25;
                fss.seek(pointer2);

                //for each tie (num_dsr) there are 11 gcps
                for (int j = 0; j < 11; j++) {
                    gcps2[i * 11 + j].setXpix(fss.readInt());
                    gcps2[i * 11 + j].setOriginalXpix(new Double(fss.readInt()));
                    gcps2[i * 11 + j].setYpix(num_lines * i + 1);
                }

                //jump to the block of Angles
                pointer2 += 88;
                fss.seek(pointer2);
                for (int j = 0; j < 11; j++) {
                    gcps2[i * 11 + j].setAngle(fss.readFloat());
                }
                for (int j = 0; j < 11; j++) {
                    gcps2[i * 11 + j].setYgeo(fss.readInt() / 1000000.);
                }
                for (int j = 0; j < 11; j++) {
                    gcps2[i * 11 + j].setXgeo(fss.readInt() / 1000000.);
                }
            }
            // AG read the last line tie points
            //skip the utc line
            pointer2 += 166;
            fss.seek(pointer2);
            for (int j = 0; j < 11; j++) {
                gcps2[num_dsr * 11 + j].setXpix(fss.readInt());
                gcps2[num_dsr * 11 + j].setYpix(num_lines * num_dsr - 1);
            }
            //jump to the block of Angles
            pointer2 += 88;
            fss.seek(pointer2);
            for (int j = 0; j < 11; j++) {
                gcps2[num_dsr * 11 + j].setAngle(fss.readFloat());
            }
            for (int j = 0; j < 11; j++) {
                gcps2[num_dsr * 11 + j].setYgeo(fss.readInt() / 1000000.);
            }
            for (int j = 0; j < 11; j++) {
                gcps2[num_dsr * 11 + j].setXgeo(fss.readInt() / 1000000.);
            }


            gcps = new java.util.Vector<Gcp>();
            for (int i = 0; i < gcps2.length; i++) {
                gcps.add(gcps2[i]);
            }
            setBand(0);

            // take the sample number, slant range time and incidence angle for
            // the middle azimuth line : the 5th
            for (int i = 0; i <= 10; i++) {
                pointer = geolocationOffset + 521 * 5 + 25 + i * 4;
                fss.seek(pointer);
                sampNumberTab[i] = fss.readInt();
                fss.seek(pointer + 44);
                slantRangeTimesTab[i] = fss.readFloat();
                fss.seek(pointer + 88);
                incidenceTab[i] = fss.readFloat();
            }

        } catch (IOException ex) {
            Logger.getLogger(EnvisatImage.class.getName()).log(Level.SEVERE, "cannot read gcps", ex);
        }

    }

    // </editor-fold>
    @Override
    public int read(int x, int y) {
        int result = 0;
        long temp = 0;
        byte[] pixelByte = new byte[2];
        // System.out.println(this.imageType);
        if (x >= 0 & y >= 0 & x < xSize & y < ySize) {
            try {
                temp = (y * (xOffset + xSize * 2) + xOffset + x * 2);
                fss.seek(temp + offsetBand);
                fss.read(pixelByte, 0, 2);
                int interm1 = pixelByte[0];
                int interm2 = pixelByte[1];
                result = ((interm1) << 8 | interm2 & 0xff);
            } catch (IOException e) {
                Logger.getLogger(EnvisatImage.class.getName()).log(Level.SEVERE, "cannot read pixel (" + x + "," + y + ")", e);
            }

        }

        return result;
    }

    @Override
    public void preloadLineTile(int y, int length) {
        if (y < 0) {
            return;
        }
        preloadedInterval = new int[]{y, y + length};
        int tileOffset = offsetBand + (y * (xSize * 2 + xOffset));
        preloadedData = new byte[(xSize * 2 + xOffset) * length];
        try {
            fss.seek(tileOffset);
            fss.read(preloadedData);
        } catch (IOException e) {
            Logger.getLogger(EnvisatImage.class.getName()).log(Level.SEVERE, "cannot preload the line tile", e);
        }
    }

    @Override
    public String getBandName(int band) {
        if (band++ < getNBand()) {
            return (String) getMetadata("MDS" + band + "_TX_RX_POLAR");
        } else {
            return "";
        }
    }

    @Override
    public void setBand(int band) {
        if (!(band < getNBand())) {
            return;
        }
        offsetBand = Integer.parseInt(((String) (getMetadata("MDS1_DS_OFFSET"))).replace("+", "").replace("<bytes>", ""));
        if (band == 1) {
            offsetBand = Integer.parseInt(((String) (getMetadata("MDS2_DS_OFFSET"))).replace("+", "").replace("<bytes>", ""));
        }
        preloadedInterval = new int[]{0, 0};
        this.band = band;
    }

    public int getXOffset() {
        return xOffset;
    }

    @Override
    public int[] readTile(int x, int y, int width, int height) {
        return readTile(x, y, width, height, new int[width * height]);
    }

    public int[] readTile(int x, int y, int width, int height, int[] tile) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect = rect.intersection(bounds);
        if (rect.isEmpty()) {
            return tile;
        }
        if (rect.y != preloadedInterval[0] || rect.y + rect.height != preloadedInterval[1]) {
            preloadLineTile(rect.y, rect.height);
        }
        int yOffset = xOffset + 2 * xSize;
        int xinit = rect.x - x;
        int yinit = rect.y - y;
        for (int i = 0; i < rect.height; i++) {
            for (int j = 0; j < rect.width; j++) {
                int temp = i * yOffset + 2 * j + 2 * rect.x + xOffset;
                tile[(i + yinit) * width + j + xinit] = ((preloadedData[temp + 0]) << 8) | (preloadedData[temp + 1] & 0xff);
            }
        }
        return tile;
    }

    @Override
    public int[] readAndDecimateTile(int x, int y, int width, int height, int outWidth, int outHeight,int xSize,int ySize, boolean filter) {
        if (height < 257) {
            //System.out.printf("readAndDecimateTile(%d, %d, %d, %d, %d, %d)", x, y, width, height, outWidth, outHeight);
            int[] outData = new int[outWidth * outHeight];
            int[] data = readTile(x, y, width, height);
            int decX = Math.round(width / (1f * outWidth));
            int decY = Math.round(height / (1f * outHeight));
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
            return outData;
        } else {
            float incy = height / 256f;
            int[] outData = new int[outWidth * outHeight];
            float decY = height / (1f * outHeight);
            int index = 0;
            for (int i = 0; i < Math.ceil(incy); i++) {
                int tileHeight = (int) Math.min(Constant.TILE_SIZE, height - i * Constant.TILE_SIZE);
                if (tileHeight > decY) {
                    int[] temp = readAndDecimateTile(x, y + i * Constant.TILE_SIZE, width, tileHeight, outWidth, Math.round(tileHeight / decY), xSize, ySize,  filter);
                    int min = Math.min(temp.length, outData.length - index);
                    for (int j = 0; j < min; j++) {
                        outData[index++] = temp[j];
                    }
                }
            }
            return outData;
        }
    }

    protected void getTimestamp() {
        String time = (String) getMetadata("SENSING_START");
        String[] date = time.split(" ")[0].split("-");
        String month = "";
        if (date[1].equals("JAN")) {
            month = "01";
        } else if (date[1].equals("FEB")) {
            month = "02";
        } else if (date[1].equals("MAR")) {
            month = "03";
        } else if (date[1].equals("APR")) {
            month = "04";
        } else if (date[1].equals("MAY")) {
            month = "05";
        } else if (date[1].equals("JUN")) {
            month = "06";
        } else if (date[1].equals("JUL")) {
            month = "07";
        } else if (date[1].equals("AUG")) {
            month = "08";
        } else if (date[1].equals("SEP")) {
            month = "09";
        } else if (date[1].equals("OCT")) {
            month = "10";
        } else if (date[1].equals("NOV")) {
            month = "11";
        } else if (date[1].equals("DEC")) {
            month = "12";
        }
        String outdate = date[2] + "-" + month + "-" + date[0];
        setMetadata(TIMESTAMP_START, Timestamp.valueOf(outdate + " " + time.split(" ")[1]));
        time = (String) getMetadata("SENSING_STOP");
        date = time.split(" ")[0].split("-");
        month = "";
        if (date[1].equals("JAN")) {
            month = "01";
        } else if (date[1].equals("FEB")) {
            month = "02";
        } else if (date[1].equals("MAR")) {
            month = "03";
        } else if (date[1].equals("APR")) {
            month = "04";
        } else if (date[1].equals("MAY")) {
            month = "05";
        } else if (date[1].equals("JUN")) {
            month = "06";
        } else if (date[1].equals("JUL")) {
            month = "07";
        } else if (date[1].equals("AUG")) {
            month = "08";
        } else if (date[1].equals("SEP")) {
            month = "09";
        } else if (date[1].equals("OCT")) {
            month = "10";
        } else if (date[1].equals("NOV")) {
            month = "11";
        } else if (date[1].equals("DEC")) {
            month = "12";
        }
        outdate = date[2] + "-" + month + "-" + date[0];
        setMetadata(TIMESTAMP_STOP, Timestamp.valueOf(outdate + " " + time.split(" ")[1]));
    }


    @Override
    public void dispose() {
        super.dispose();
        try {
            if (fss != null) {
                fss.close();
            }
            fss = null;
        } catch (IOException ex) {
            Logger.getLogger(EnvisatImage.class.getName()).log(Level.SEVERE, "Can't close the file", ex);
        }
    }

    @Override
    public double getSlantRange(int position) {
        float slant_range_time;
        float lightSpeed = (float) 0.299792458; // * 10^-9
        double slant_range = 0;
        int j = 0;

        if (((String) getMetadata(LOOK_DIRECTION)).equalsIgnoreCase("RIGHT")) {
            j = position;
        } else {
            j = getWidth() - position;
        }

        slant_range_time = extrapol(slantRangeTimesTab, j);
        slant_range = (slant_range_time * lightSpeed) / 2;
        return slant_range;
    }

    /**
     * Make the interpolation between the value of the tab to find the right
     * incidence angle
     *
     * @param tab
     *            contain the values of the incidence angle for the middle
     *            azimuth
     * @param pos
     *            is the position in the range direction
     * @return the value of the incidence
     */
    private float extrapol(float[] tab, int pos) {
        int i;
        int sizeSample, numSampleL, numSampleU, pos_sample;
        float extrapol;

        i = 0;
        if (pos == 0) {
            extrapol = tab[0];
        } else {
            if (pos == getWidth()) {
                extrapol = tab[10];
            } else {
                while (pos >= sampNumberTab[i]) {
                    i++;
                }
                if (pos == sampNumberTab[i]) {
                    extrapol = tab[i];
                } else {
                    numSampleU = i;
                    numSampleL = numSampleU - 1;
                    pos_sample = pos - sampNumberTab[numSampleL];
                    sizeSample = sampNumberTab[numSampleU] - sampNumberTab[numSampleL];
                    extrapol = (tab[numSampleL] * (sizeSample - pos_sample) + tab[numSampleU] * pos_sample) / sizeSample;
                }
            }
        }
        return extrapol;
    }

    @Override
    public int getNumberOfBytes() {
        return 2;
    }

    @Override
    public int getType(boolean oneBand) {
        return BufferedImage.TYPE_USHORT_GRAY;
    }

    @Override
    public String getFormat() {

        return getClass().getCanonicalName();
    }

    public String getInternalImage() {
		return null;
	}

	@Override
	public File getOverviewFile() {
		return null;
	}
}
