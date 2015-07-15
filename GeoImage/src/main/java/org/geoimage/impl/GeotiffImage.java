package org.geoimage.impl;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.geoimage.def.GeoTransform;
import org.geoimage.def.SarImageReader;
import org.geoimage.exception.GeoTransformException;
import org.geoimage.factory.GeoTransformFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.LoggerFactory;

import com.sun.media.imageio.plugins.tiff.BaselineTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.GeoTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.TIFFDirectory;
import com.sun.media.imageio.plugins.tiff.TIFFField;
import com.sun.media.imageio.plugins.tiff.TIFFImageReadParam;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReader;

public class GeotiffImage extends SarImageReader {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(GeotiffImage.class);

	
	protected int xSize = -1;
	protected int ySize = -1;
	
    private TIFFDirectory td;
    private String[] files;
    private AffineTransform matrix;
    private int[] preloadedInterval = new int[]{0, 0};
    private int[] preloadedData;
    private Rectangle bounds;
    private TIFFImageReader reader;
    private Raster tiffRaster;

	
    /**
     * A class reading the source image as a geotiff. It should be the last attempt
     * in reading the source file since it will use only data contained in the geotiff header.
     * It is completed with possibility to read external dim files or tfw files
     * TODO: create a special class that handle geotiff with DIM files
     */
    public GeotiffImage(File f) {
    	super(f);
    }

    @Override
    public int getNBand() {
        return td.getTIFFField(BaselineTIFFTagSet.TAG_SAMPLES_PER_PIXEL).getAsInt(0);
    }

    @Override
    public List<Gcp> getGcps() throws GeoTransformException {
        if (gcps != null) {
            return gcps;
        }
        if (td.getTIFFField(GeoTIFFTagSet.TAG_MODEL_TIE_POINT) != null && td.getTIFFField(GeoTIFFTagSet.TAG_MODEL_PIXEL_SCALE) != null) {
            gcps = new ArrayList<Gcp>();
            TIFFField mtp = td.getTIFFField(GeoTIFFTagSet.TAG_MODEL_TIE_POINT);
            createGcpsFromTiePoints(mtp.getAsDoubles());
            if (gcps != null) {
                return gcps;
            }
        }
        createGcpsFromCornerPoints();
        return gcps;
    }

    @Override
    public String getAccessRights() {
        return "r";
    }

    @Override
    public String[] getFilesList() {
        return files;
    }

    @Override
    public boolean initialise() {
        try {
           this.displayName=manifestFile.getName();
        	
           
            System.out.println(reader.getNumImages(false));
            xSize = reader.getWidth(0);
            ySize = reader.getHeight(0);
            setMetaHeight(ySize);
            setMetaWidth(xSize);
            String epsg = null;
            if (gcps != null) {
                epsg = "EPSG:4326";
            }
            getTimestamp();
            bounds = new Rectangle(0, 0, xSize, ySize);
            if (manifestFile == null && td.getTIFFField(GeoTIFFTagSet.TAG_MODEL_TRANSFORMATION) != null) {
                double[] m = td.getTIFFField(GeoTIFFTagSet.TAG_MODEL_TRANSFORMATION).getAsDoubles();
                if (m[3] > 180) {
                    m[3] = m[3] - 360;
                }
                matrix = new AffineTransform(m[0], m[4], m[1], m[5], m[3], m[7]);
            } else if (matrix == null && td.getTIFFField(GeoTIFFTagSet.TAG_MODEL_TIE_POINT) != null) {
                double[] m1 = td.getTIFFField(GeoTIFFTagSet.TAG_MODEL_TIE_POINT).getAsDoubles();
                if (m1.length == 6) {
                    double[] m2 = td.getTIFFField(GeoTIFFTagSet.TAG_MODEL_PIXEL_SCALE).getAsDoubles();
                    matrix = new AffineTransform(m2[0], 0, 0, -m2[1], m1[3] - m1[0] * m2[0], m1[4] + m1[1] * m2[1]);
                } else {
                    createGcpsFromTiePoints(m1);
                }
            }
            if (matrix == null && gcps == null) {
                gcps = getGcps();
            }
            if (epsg == null) {
                epsg = "EPSG:";
                if (td.getTIFFField(GeoTIFFTagSet.TAG_GEO_KEY_DIRECTORY) != null) {
                    char[] temp = td.getTIFFField(GeoTIFFTagSet.TAG_GEO_KEY_DIRECTORY).getAsChars();
                    for (int i = 0; i < temp.length; i += 4) {
                        if (temp[i] == 3072) {
                            epsg += (short) temp[i + 3];
                        }
                    }

                    if (epsg.equals("EPSG:")) {
                        for (int i = 0; i < temp.length; i += 4) {
                            if (temp[i] == 2048) {
                                epsg += (short) temp[i + 3];
                            }
                        }
                    }

                    if (epsg.equals("EPSG:")) {
                        epsg = "EPSG:4326";
                    }
                } else {
                    epsg += "4326";
                }
            }
            if (gcps != null) {
                geotransform = GeoTransformFactory.createFromGcps(gcps, epsg);
            } else if (matrix != null) {
                geotransform = GeoTransformFactory.getFromAffineTransform(matrix, epsg);
            } else {
                dispose();
                return false;
            }
        } catch (Exception ex) {
            dispose();
        	logger.error(ex.getMessage(),ex);
            return false;
        }

        if(geotransform==null){
            return false;
        }
        return true;
    }

    public double getPRF(int x,int y){
        return getPRF();


    }
  
	@Override
	public int[] read(int x, int y, int w, int h, int band) {
		 return readTile(x, y, w, h, band) ;
	}
    
    @Override
    public int[] readTile(int x, int y, int width, int height,int band) {
        return readTile(x, y, width, height, new int[width * height],band);
    }

    public int[] readTile(int x, int y, int width, int height, int[] tile,int band) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect = rect.intersection(bounds);
        if (rect.isEmpty()) {
            return tile;
        }
        if (rect.y != preloadedInterval[0] | rect.y + rect.height != preloadedInterval[1]) {
            preloadLineTile(rect.y, rect.height, band);
        }
        int[] data = new int[height * width];
        int yOffset = xSize;
        int xinit = rect.x - x;
        int yinit = rect.y - y;
        for (int i = 0; i < rect.height; i++) {
            for (int j = 0; j < rect.width; j++) {
                int temp = i * yOffset + j + rect.x;
                data[(i + yinit) * width + j + xinit] = preloadedData[temp];
            }
        }
        return data;
    }

    @Override
    public int readPixel(int x, int y,int band) {
        TIFFImageReadParam t = new TIFFImageReadParam();
        t.setSourceRegion(new Rectangle(x, y, 1, 1));
        int[] pix = new int[1];
        tiffRaster.getPixel(x, y, pix);
        return pix[0];
    }

    @Override
    public String getBandName(int band) {
        return "" + band;
    }
/*
    @Override
    public void setBand(int band) {
        this.band = band;
        preloadedInterval = new int[]{0, 0};
    }*/

    @Override
    public void preloadLineTile(int y, int length,int band) {
        if (y < 0) {
            return;
        }
        preloadedInterval = new int[]{y, y + length};
        Rectangle rect = new Rectangle(0, y, xSize, length);
        TIFFImageReadParam tirp = new TIFFImageReadParam();
        tirp.setSourceRegion(rect);
        try {
            preloadedData = reader.read(0, tirp).getRaster().getSamples(0, 0, xSize, length, band, (int[]) null);
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }

    private void createGcpsFromTiePoints(double[] m1) {
        if (m1.length % 6 != 0) {
            return;
        }
        gcps = new Vector<Gcp>();
        for (int i = 0; i < m1.length; i++) {
            Gcp gcp = new Gcp();
            gcp.setXpix(m1[i++]);
            gcp.setOriginalXpix(m1[i++]);
            gcp.setYpix(m1[i++]);
            i++;
            gcp.setXgeo(m1[i++]);
            gcp.setYgeo(m1[i++]);
            gcps.add(gcp);
        }
    }

    private void getTimestamp() {
        try {
            String datetime = td.getTIFFField(BaselineTIFFTagSet.TAG_DATE_TIME).getAsString(0);
            datetime = datetime.replaceFirst(":", "-").replaceFirst(":", "-");
            setTimeStampStop(Timestamp.valueOf(datetime).toString());
            setTimeStampStart(Timestamp.valueOf(datetime).toString());
        } catch (Exception e) {
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            reader.dispose();
            reader = null;
            manifestFile = null;
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }
   

    private void parsePTS(File pts) {
        try {
            RandomAccessFile temp = new RandomAccessFile(pts, "r");
            String line = null;
            this.gcps = new ArrayList<Gcp>();
            while ((line = temp.readLine()) != null) {
                String[] nums = line.split(" ");
                if (nums.length == 4) {
                    Gcp gcp = new Gcp();
                    gcp.setXgeo(Double.parseDouble(nums[0]));
                    gcp.setYgeo(Double.parseDouble(nums[1]));
                    gcp.setXpix(Double.parseDouble(nums[2]));
                    gcp.setYpix(Double.parseDouble(nums[3]));
                    gcps.add(gcp);
                }
            }
            temp.close();
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }

    private void parseTFW() {
        try {
            RandomAccessFile temp = new RandomAccessFile(manifestFile, "r");
            double m00 = Double.parseDouble(temp.readLine());
            double m10 = Double.parseDouble(temp.readLine());
            double m01 = Double.parseDouble(temp.readLine());
            double m11 = Double.parseDouble(temp.readLine());
            double m02 = Double.parseDouble(temp.readLine());
            double m12 = Double.parseDouble(temp.readLine());
            matrix = new AffineTransform(m00, m10, m01, m11, m02, m12);
            temp.close();
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }

    }

    private void parseDIM() {
        try {
            this.gcps = new ArrayList<Gcp>();

            SAXBuilder builder = new SAXBuilder();
            Document doc;
            doc = builder.build(manifestFile);

            Element atts = doc.getRootElement().getChild("Dataset_Frame");
            for (Object o : atts.getChildren("Vertex")) {
                Gcp gcp = new Gcp();
                Element elem = (Element) o;
                gcp.setXpix(Double.parseDouble(elem.getChild("FRAME_COL").getText()));
                gcp.setYpix(Double.parseDouble(elem.getChild("FRAME_ROW").getText()));
                gcp.setXgeo(Double.parseDouble(elem.getChild("FRAME_LON").getText()));
                gcp.setYgeo(Double.parseDouble(elem.getChild("FRAME_LAT").getText()));
                gcp.setZgeo(Double.parseDouble("0.0"));
                gcps.add(gcp);
            }

            // calculate the matrix from the gcps
            //WarpTransform2D

            atts = atts.getChild("Scene_Center");
            Gcp gcp = new Gcp();
            gcp.setXpix(Double.parseDouble(atts.getChild("FRAME_COL").getText()));
            gcp.setYpix(Double.parseDouble(atts.getChild("FRAME_ROW").getText()));
            gcp.setXgeo(Double.parseDouble(atts.getChild("FRAME_LON").getText()));
            gcp.setYgeo(Double.parseDouble(atts.getChild("FRAME_LAT").getText()));
            gcp.setZgeo(Double.parseDouble("0.0"));
            gcps.add(gcp);

        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }

    }

    @Override
    public int getNumberOfBytes() {
        return 2;
    }

    @Override
    public int getType(boolean oneBand) {
        int nBand = getNBand();
        if (oneBand | nBand == 1) {
            //if (td.getTIFFField(BaselineTIFFTagSet.TAG_STRIP_BYTE_COUNTS).getAsInt(0) == 1) {
            //    return BufferedImage.TYPE_BYTE_GRAY;
            //} else if (td.getTIFFField(BaselineTIFFTagSet.TAG_STRIP_BYTE_COUNTS).getAsInt(0) == 2) {
            return BufferedImage.TYPE_USHORT_GRAY;
            //}
        } else if (nBand == 2) {
            return BufferedImage.TYPE_BYTE_GRAY;
        } else if (nBand == 3) {
            return BufferedImage.TYPE_INT_RGB;
        } else if (nBand == 4) {
            return BufferedImage.TYPE_INT_RGB;
        }
        return BufferedImage.TYPE_BYTE_GRAY;
    }

    @Override
    public GeoTransform getGeoTransform() {
        return geotransform;
    }

    public void setProjectionReferenceSystem(String wktPRS) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getFormat() {
        return getClass().getCanonicalName();
    }

    private void createGcpsFromCornerPoints() throws GeoTransformException {
        double[] x0;
        double[] x1;
        double[] x2;
        double[] x3;
        x0 = geotransform.getGeoFromPixel(0, 0);
        x2 = geotransform.getGeoFromPixel(getWidth(), getHeight());
        x3 = geotransform.getGeoFromPixel(getWidth(), 0);
        x1 = geotransform.getGeoFromPixel(0, getHeight());
        gcps = new Vector<Gcp>();
        gcps.add(new Gcp(0, 0, x0[0], x0[1]));
        gcps.add(new Gcp(0, getHeight(), x1[0], x1[1]));
        gcps.add(new Gcp(getWidth(), getHeight(), x2[0], x2[1]));
        gcps.add(new Gcp(getWidth(), 0, x3[0], x3[1]));
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
		return null;
	}
	@Override
	public boolean supportAzimuthAmbiguity() {
		return false;
	}

	@Override
	public String getImgName() {
		return displayName;
	}

	@Override
	public String getDisplayName(int band) {
		return displayName;
	}



}

