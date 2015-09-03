package org.geoimage.impl.tsar;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.math3.util.FastMath;
import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoTransformFactory;
import org.geoimage.impl.Gcp;
import org.geoimage.impl.TIFF;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.LoggerFactory;

import com.sun.media.imageio.plugins.tiff.TIFFImageReadParam;

/**
 * A class that reads Terrasar-X images including process to extract gcps from the
 * time - slant range xml dataset
 * @author thoorfr, gabbaan
 */
public class TerrasarXImage extends SarImageReader {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(TerrasarXImage.class);

    protected String[] files;
    protected int[] preloadedInterval = new int[]{0, 0};
    protected int[] preloadedData;
    protected File tfw;
    protected AffineTransform matrix;
    protected Rectangle bounds;
    protected File productxml;
    protected Document doc;
    protected int MGRows = 0;
    protected int MGCols = 0;
    protected int MGRefRow = 0;
    protected int MGRefCol = 0;
    protected double MGRowSpacing = 0;
    protected double MGColSpacing = 0;
    protected double ImageRowSpacing = 0;
    protected double ImageColSpacing = 0;
    protected double MGtTime = 0;
    protected double MGtauTime = 0;
    protected double rangeTimeStart = 0;
    protected double rangeTimeStop = 0;
    protected double GGtTime = 0;
    protected double GGtauTime = 0;
    protected double xposition = 0;
    protected double yposition = 0;
    protected double zposition = 0;
    protected int[] stripBounds = {0, 0, 0};
    protected Map<String, TIFF> tiffImages;
    protected String overview;
    
    protected Vector<String> bands = new Vector<String>();

    public TerrasarXImage(File f) {
    	super(f);
    }

    @Override
    public int getNBand() {
        return bands.size();
    }
    
    public double[] getPixelsize() {
		return super.pixelsize;
	}
    
    @Override
    /**gcps are computed atrting from the georef.xml file and the mapping_grid.bin file. Thats because the pixel positions are related to time
    / (t,tau) and not to (lon,lat) directly
     */
    public List<Gcp> getGcps() {
        if (gcps != null) {
            return gcps;
        }
        try {
            gcps = new ArrayList<Gcp>();

            //link to the GEOREF.xml file
            File GeoFile = new File((new File((new File(files[0])).getParent())) + "/ANNOTATION/GEOREF.xml");
            SAXBuilder builder = new SAXBuilder();
            Document geodoc = builder.build(GeoFile);
            Element atts = geodoc.getRootElement().getChild("geolocationGrid");
            //number of Geo Grid elements
            Integer GGRows = new Integer(atts.getChild("numberOfGridPoints").getChild("azimuth").getText());
            Integer GGCols = new Integer(atts.getChild("numberOfGridPoints").getChild("range").getText());
            //Geo Grid point spacing
            //    double spacingOfGridPointsAz = Double.parseDouble(atts.getChild("spacingOfGridPoints").getChild("azimuth").getText());
            //    double spacingOfGridPointsRg = Double.parseDouble(atts.getChild("spacingOfGridPoints").getChild("range").getText());
            String GGtTimes = atts.getChild("gridReferenceTime").getChild("tReferenceTimeUTC").getText();
            GGtTimes = GGtTimes.substring(0, GGtTimes.length() - 1);
            GGtTime = Timestamp.valueOf(GGtTimes.replaceAll("T", " ")).getTime();
            GGtauTime = Double.parseDouble(atts.getChild("gridReferenceTime").getChild("tauReferenceTime").getText());

            //read and store the mapping_grid.bin file into an array
            double[][] MappingGrid_t = new double[MGCols][MGRows];
            double[][] MappingGrid_tau = new double[MGCols][MGRows];
            try {
                File MGFile = new File((new File((new File(files[0])).getParent())) + "/AUXRASTER/MAPPING_GRID.bin");
                InputStream is = new FileInputStream(MGFile);
                DataInputStream dis = new DataInputStream(is);
                long length = MGFile.length();
                if (length > Integer.MAX_VALUE) {
                	dis.close();
                    throw new IOException("File is too large");
                } else {
                    byte[] bytes = new byte[(int) length];
                    int offset = 0;
                    int numRead = 0;
                    while (offset < bytes.length
                            && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                        offset += numRead;
                    }
                    if (offset < bytes.length) {
                    	dis.close();
                        throw new IOException("Could not completely read file " + MGFile.getName());
                    }
                    dis.close();
                    is.close();
                    int az_cnt = 0;
                    int rg_cnt = 0;
                    for (int start = 0; start < offset; start = start + 4) {
                        MappingGrid_t[rg_cnt][az_cnt] = arr2float(bytes, start) * 1000 + MGtTime;
                        start = start + 4;
                        MappingGrid_tau[rg_cnt][az_cnt] = arr2float(bytes, start) + MGtauTime;
                        rg_cnt++;
                        if (rg_cnt == MGCols) {
                            az_cnt++;
                            rg_cnt = 0;
                        }
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }

            //read and store the GeoGrid from the GEOREF.xml into an array
            int az_cnt = 0;
            int rg_cnt = 0;
            double GeoGrid_t[][] = new double[GGCols][GGRows];
            double GeoGrid_tau[][] = new double[GGCols][GGRows];
            double GeoGrid_lat[][] = new double[GGCols][GGRows];
            double GeoGrid_lon[][] = new double[GGCols][GGRows];
            double GeoGrid_inc[][] = new double[GGCols][GGRows];

            for (Object o : atts.getChildren("gridPoint")) {
                Element elem = (Element) o;
                GeoGrid_t[rg_cnt][az_cnt] = Double.parseDouble(elem.getChild("t").getText()) * 1000 + GGtTime;
                GeoGrid_tau[rg_cnt][az_cnt] = Double.parseDouble(elem.getChild("tau").getText()) + GGtauTime;
                GeoGrid_lat[rg_cnt][az_cnt] = Double.parseDouble(elem.getChild("lat").getText());
                GeoGrid_lon[rg_cnt][az_cnt] = Double.parseDouble(elem.getChild("lon").getText());
                GeoGrid_inc[rg_cnt][az_cnt] = Double.parseDouble(elem.getChild("inc").getText());
                rg_cnt++;
                if (rg_cnt == GGCols) {
                    az_cnt++;
                    rg_cnt = 0;
                }
            }
            int counter = 0;

            //compute gcps looking for each point in GeoGrid
            for (int b = 0; b < GGRows; b++) {//for each line
                for (int a = 0; a < GGCols; a++) {//for each pixel in the line
                    //search the 4 points which surround the selected point of the GeoGrid
                    double GG_t = GeoGrid_t[a][b];
                    double GG_tau = GeoGrid_tau[a][b];

                    int c = 0;
                    int floor_idxrow = -1;
                    int ceil_idxrow = -1;
                    while (c < MGRows - 1) {
                        //the two different conditions consider the possibility to have both ascending and descending mode
                        if (MappingGrid_t[0][c] <= GG_t && GG_t < MappingGrid_t[0][c + 1]) {
                            floor_idxrow = c;
                            ceil_idxrow = c + 1;
                            break;
                        }
                        if (MappingGrid_t[0][c] >= GG_t && GG_t > MappingGrid_t[0][c + 1]) {
                            floor_idxrow = c + 1;
                            ceil_idxrow = c;
                            break;
                        }
                        c++;
                    }
                    if (GG_t == MappingGrid_t[0][c]) {
                        floor_idxrow = c;
                        ceil_idxrow = c;
                    }

                    int d = 0;
                    int floor_idxcol = -1;
                    int ceil_idxcol = -1;
                    while (d < MGCols - 1) {
                        if (MappingGrid_tau[d][0] <= GG_tau && GG_tau < MappingGrid_tau[d + 1][0]) {
                            floor_idxcol = d;
                            ceil_idxcol = d + 1;
                            break;
                        }
                        if (MappingGrid_tau[d][0] >= GG_tau && GG_tau > MappingGrid_tau[d + 1][0]) {
                            floor_idxcol = d + 1;
                            ceil_idxcol = d;
                            break;
                        }
                        d++;
                    }
                    if (GG_tau == MappingGrid_tau[d][0]) {
                        floor_idxcol = d;
                        ceil_idxcol = d;
                    }

                    //compute line and pixel corresponding to the selected GeoGrid point
                    if (GG_t >= 0 && GG_tau >= 0 && ceil_idxcol != -1 && ceil_idxrow != -1) {

                        int line;
                        if (floor_idxrow == ceil_idxrow) {
                            line = new Double(floor_idxrow * MGRowSpacing / ImageRowSpacing).intValue();
                        } else {
                            double floor_line = (floor_idxrow) * MGRowSpacing / ImageRowSpacing;
                            double ceil_line = (ceil_idxrow) * MGRowSpacing / ImageRowSpacing;
                            if (floor_idxrow > ceil_idxrow) {
                                //linked to ascending/descending mode
                                double temp = floor_line;
                                floor_line = ceil_line;
                                ceil_line = temp;
                            }
                            line = new Double(floor_line + ((ceil_line - floor_line) * (GG_t - MappingGrid_t[0][floor_idxrow]) / (MappingGrid_t[0][ceil_idxrow] - MappingGrid_t[0][floor_idxrow]))).intValue();

                        }

                        int pixel;
                        if (floor_idxcol == ceil_idxcol) {
                            pixel = new Double(floor_idxcol * MGColSpacing / ImageColSpacing).intValue();
                        } else {
                            double floor_pix = (floor_idxcol) * MGColSpacing / ImageColSpacing;
                            double ceil_pix = (ceil_idxcol) * MGColSpacing / ImageColSpacing;
                            if (floor_idxcol > ceil_idxcol) {
                                //linked to ascending/descending mode
                                double temp = floor_pix;
                                floor_pix = ceil_pix;
                                ceil_pix = temp;
                            }
                            pixel = new Double(floor_pix + ((ceil_pix - floor_pix) * (GG_tau - MappingGrid_tau[floor_idxcol][0]) / (MappingGrid_tau[ceil_idxcol][0] - MappingGrid_tau[floor_idxcol][0]))).intValue();
                        }
                        Gcp gcp = new Gcp();

                        counter++;
                        if (pixel >= 0 && pixel < getImage(0).xSize && line >= 0 && line < getImage(0).ySize) {
                            gcp.setXpix(pixel);
                        	gcp.setOriginalXpix(new Double(pixel));
                            gcp.setYpix(new Double(line));
                            gcp.setXgeo(GeoGrid_lon[a][b]);
                            gcp.setYgeo(GeoGrid_lat[a][b]);
                            gcp.setAngle((float) GeoGrid_inc[a][b]);
                            gcps.add(gcp);
                        }
                    }
                }
            }

            return gcps;

        } catch (JDOMException ex) {
        	logger.error(ex.getMessage(),ex);
            return null;
        } catch (IOException ex) {
        	logger.error(ex.getMessage(),ex);
            return null;
        }
    }

    public double getPRF(int x,int y){
        //double prf = 0;
        //check if is the case of TSX ScanSAR
        if (getMode().equals("SC")) {
            int bound1 = getStripBound1();
            int bound2 = getStripBound2();
            int bound3 = getStripBound3();
            //return the different PRF depending by the strip
            if (x >= 0 && x < bound1) {
                return getPRF1();
            }
            if (x < bound2) {
            	return getPRF2();
            }
            if (x < bound3) {
            	return getPRF3();
            }

            return getPRF4();
        }

        //for all the other cases with only one PRF
        return getPRF();


    }
    
    public static float arr2float(byte[] arr, int start) {
        int i = 0;
        int len = 4;
        int cnt = 3;
        byte[] tmp = new byte[len];
        for (i = start; i < (start + len); i++) {
            tmp[cnt] = arr[i];
            cnt--;
        }
        int accum = 0;
        i = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
            accum |= ((long) (tmp[i] & 0xff)) << shiftBy;
            i++;
        }
        return Float.intBitsToFloat(accum);
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
    		 
    		 this.imgName=manifestFile.getParentFile().getName();
    		 
	    	setFile(manifestFile);
	    	parseProductXML(productxml);
	    	tiffImages = getImages();
	    	
	        if(tiffImages==null) return false;
       
            //System.out.println(reader.getNumImages(false));
            TIFF image = tiffImages.values().iterator().next();
            this.displayName= this.imgName;//image.getImageFile().getName();
            
            image.xSize = getWidth();
            image.ySize = getHeight();
            
            bounds = new Rectangle(0, 0, image.xSize, image.ySize);
            gcps = getGcps();
            if (gcps == null) {
                dispose();
                return false;
            }

            //get satellite altitude
            geotransform = GeoTransformFactory.createFromGcps(gcps, "EPSG:4326");
            double radialdist = Math.pow(xposition * xposition + yposition * yposition + zposition * zposition, 0.5);
            MathTransform convert;
            double[] latlon = getGeoTransform().getGeoFromPixel(0, 0);
            double[] position = new double[3];
            convert = CRS.findMathTransform(DefaultGeographicCRS.WGS84, DefaultGeocentricCRS.CARTESIAN);
            convert.transform(latlon, 0, position, 0, 1);
            double earthradial = Math.pow(position[0] * position[0] + position[1] * position[1] + position[2] * position[2], 0.5);
            setSatelliteAltitude(radialdist - earthradial);

            // get incidence angles from gcps
            // !!possible to improve
            float firstIncidenceangle = (float) (this.gcps.get(0).getAngle());
            float lastIncidenceAngle = (float) (this.gcps.get(this.gcps.size() - 1).getAngle());
            setIncidenceNear(firstIncidenceangle < lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle);
            setIncidenceFar(firstIncidenceangle > lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle);


        } catch (TransformException ex) {
        	logger.error(ex.getMessage(),ex);
        } catch (FactoryException ex) {
        	logger.error(ex.getMessage(),ex);
        } catch (Exception ex) {
            dispose();
            logger.error(ex.getMessage(),ex);
            return false;
        }
        return true;
    }
    
    
    /**
     * 
     * @param imageFile
     */
    protected void setFile(File imageFile) {
        files = new String[1];

        try {
            File parent = imageFile;
            if (!parent.isDirectory()) {
                parent = imageFile.getParentFile();
            }
            if(parent.getName().equals("IMAGEDATA")){//change directory if the selected file is the image instead of the xml
                parent = parent.getParentFile();
            }

            int count = 0; //xml counter
            if(!imageFile.getName().endsWith(".xml")){
	            for (File f : parent.listFiles()) {
	                if (f.getName().endsWith(".xml")) {
	                    productxml = f;
	                    //xml path
	                    files[0] = f.getAbsolutePath();
	                    count++;
	                }
	            }
            }else{
            	productxml=imageFile.getAbsoluteFile();
            	files[0] = imageFile.getAbsolutePath();
            }
            if (productxml != null) {
                if (count > 1) {
                    throw new IOException("more than one .xml file found");
                }
            } else {
                throw new IOException("Cannot find product.xml");
            }
            overview=new StringBuilder(parent.getAbsolutePath()).append("\\PREVIEW").append("\\BROWSE.tif").toString();
            
        } catch (IOException ex) {
            dispose();
        }

    }

    /**
     * 
     * @return
     */
    private Map<String, TIFF> getImages() {
        @SuppressWarnings("unchecked")
		List<Element> elements = doc.getRootElement().getChild("productComponents").getChildren("imageData");
        Map<String, TIFF> tiffs = new HashMap<String, TIFF>();
        bands=new Vector<String>();
        for (Object o : elements) {
            if (o instanceof Element) {
                File f = new File(productxml.getParent()+"\\"+((Element) o).getChild("file").getChild("location").getChild("path").getText()+"\\"+((Element) o).getChild("file").getChild("location").getChild("filename").getText());
                String polarisation = ((Element) o).getChild("polLayer").getValue();
                tiffs.put(polarisation, new TIFF(f,0));
                bands.add(polarisation);
            }
        }
        return tiffs;
    }
    
    @Override
	public int[] read(int x, int y, int w, int h, int band) throws IOException {
		Rectangle rect = new Rectangle(x, y, w, h);
        rect = rect.intersection(bounds);
        int[] data= new int[h*w];
        if (rect.isEmpty()) {
            return data;
        }

        TIFFImageReadParam tirp=new TIFFImageReadParam();
        tirp.setSourceRegion(rect);
        TIFF tiff=getImage(band);
        int[] rawData = tiff.getReader().read(0, tirp).getRaster().getSamples(x, y,w,h, 0, (int[]) null);
        
        int yOffset = getImage(band).xSize;
        int xinit = rect.x - x;
        int yinit = rect.y - y;
        for (int i = 0; i < rect.height; i++) {
            for (int j = 0; j < rect.width; j++) {
                int temp = i * yOffset + j + rect.x;
                data[(i + yinit) * w + j + xinit] = rawData[temp];
            }
        }
        return data;
	}

    @Override
    public int[] readTile(int x, int y, int width, int height,int band) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect = rect.intersection(bounds);
        int[] tile = new int[height * width];
        if (rect.isEmpty()) {
            return tile;
        }

        if (rect.y != preloadedInterval[0] || rect.y + rect.height != preloadedInterval[1]) {
            preloadLineTile(rect.y, rect.height,band);
        }

        int yOffset =  getImage(band).xSize;
        int xinit = rect.x - x;
        int yinit = rect.y - y;
        for (int i = 0; i < rect.height; i++) {
            for (int j = 0; j < rect.width; j++) {
                int temp = i * yOffset + j + rect.x;
               tile[(i + yinit) * width + j + xinit] = preloadedData[temp];
            }
        }
        return tile;
    }

    @Override
    public int readPixel(int x, int y,int band) {
        TIFFImageReadParam t = new TIFFImageReadParam();
        t.setSourceRegion(new Rectangle(x, y, 1, 1));
        TIFF tiff=getImage(band);
        try {
            return  tiff.getReader().read(0, t).getRGB(x, y);
        } catch (IOException ex) {
        	logger.error(ex.getMessage(),ex);
        }
        return -1;
    }

    @Override
    public String getBandName(int band) {
        return bands.get(band);
    }
    /**
     * 
     * @return
     */
    public String[] getBands(){
    	return bands.toArray(new String[0]);
    }
    
  /*  @Override
    public void setBand(int band) {
        this.band = band;
        this.image=tiffImages.get(bands.get(band));
    }*/

    @Override
    public void preloadLineTile(int y, int length,int band) {
        if (y < 0) {
            return;
        }
        preloadedInterval = new int[]{y, y + length};
        Rectangle rect = new Rectangle(0, y,  getImage(band).xSize, length);
        TIFFImageReadParam tirp = new TIFFImageReadParam();
        tirp.setSourceRegion(rect);
        TIFF tiff=getImage(band);
        try {
            preloadedData =  tiff.getReader().read(0, tirp).getRaster().getSamples(0, 0,  getImage(band).xSize, length, 0, (int[]) null);
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if(tiffImages==null) return;
        for(TIFF t:tiffImages.values()){
            t.dispose();
        }
        tiffImages=null;
    }

    public void parseProductXML(File productxml) throws TransformException {
        try {
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build(productxml);
            Element atts = doc.getRootElement().getChild("productInfo");
            setSatellite("TerraSAR-X");
            setSensor("TX");//atts.getChild("acquisitionInfo").getChild("sensor").getText());
            String pols = "";
            for (Object o : atts.getChild("acquisitionInfo").getChild("polarisationList").getChildren("polLayer")) {
                Element elem = (Element) o;
                pols = pols + elem.getText()+" ";
                //bands.add(elem.getText());
            }
            pols.substring(0, pols.length()-1);
            setPolarization(pols);

            setLookDirection(atts.getChild("acquisitionInfo").getChild("lookDirection").getText());
            setMode(atts.getChild("acquisitionInfo").getChild("imagingMode").getText());


            setProduct(atts.getChild("productVariantInfo").getChild("productType").getText());
            setOrbitDirection(atts.getChild("missionInfo").getChild("orbitDirection").getText());
            setMetaHeight(Integer.parseInt(atts.getChild("imageDataInfo").getChild("imageRaster").getChild("numberOfRows").getText()));
            String w=atts.getChild("imageDataInfo").getChild("imageRaster").getChild("numberOfColumns").getText();
            setMetaWidth(Integer.parseInt(w) );
            
            setRangeSpacing(Float.parseFloat(atts.getChild("imageDataInfo").getChild("imageRaster").getChild("rowSpacing").getText()));
            setAzimuthSpacing(Float.parseFloat(atts.getChild("imageDataInfo").getChild("imageRaster").getChild("columnSpacing").getText()));
            
            pixelsize[0]=getRangeSpacing();
            pixelsize[1]=getAzimuthSpacing();
            
            setNumberOfBytes(new Integer(atts.getChild("imageDataInfo").getChild("imageDataDepth").getText()) / 8);
            setENL(atts.getChild("imageDataInfo").getChild("imageRaster").getChild("azimuthLooks").getText());
          
            setHeadingAngle(Double.parseDouble(atts.getChild("sceneInfo").getChild("headingAngle").getText()));
            rangeTimeStart = Double.valueOf(atts.getChild("sceneInfo").getChild("rangeTime").getChild("firstPixel").getText());
            rangeTimeStop = Double.valueOf(atts.getChild("sceneInfo").getChild("rangeTime").getChild("lastPixel").getText());
            String time = atts.getChild("sceneInfo").getChild("start").getChild("timeUTC").getText();
            //time = time.substring(0, time.lastIndexOf("."));
            setTimeStampStart(time.replaceAll("T", " "));
            time = atts.getChild("sceneInfo").getChild("stop").getChild("timeUTC").getText();
            //time = time.substring(0, time.lastIndexOf("."));
            setTimeStampStop(time.replaceAll("T", " "));
            // calculate satellite speed using state vectors
            atts = doc.getRootElement().getChild("platform").getChild("orbit").getChild("stateVec");
            double xvelocity = Double.valueOf(atts.getChildText("velX"));
            double yvelocity = Double.valueOf(atts.getChildText("velY"));
            double zvelocity = Double.valueOf(atts.getChildText("velZ"));
            double satellite_speed = Math.sqrt(xvelocity * xvelocity + yvelocity * yvelocity + zvelocity * zvelocity);
            setSatelliteSpeed(satellite_speed);
            xposition = Double.valueOf(atts.getChildText("posX"));
            yposition = Double.valueOf(atts.getChildText("posY"));
            zposition = Double.valueOf(atts.getChildText("posZ"));

            float radarFrequency = new Float(doc.getRootElement().getChild("instrument").getChild("radarParameters").getChild("centerFrequency").getText());
            setRadarWaveLenght(299792457.9 / radarFrequency);

            setSatelliteOrbitInclination(97.44);
            setRevolutionsPerday(11);


            Double[] prf=null;
            //metadata used for ScanSAR mode during the Azimuth ambiguity computation
            if (getMode().equals("SC")) {
            	List<Element> sets=doc.getRootElement().getChild("instrument").getChildren("settings");
            	prf=new Double[sets.size()];
                //extraction of the 4 PRF codes
                int prf_count = 0;
                for (Object o : sets) {
                    Element elem = (Element) o;
                    //setMetadata("PRF" + prf_count, elem.getChild("settingRecord").getChild("PRF").getText());
                    prf[prf_count]=Double.parseDouble(elem.getChild("settingRecord").getChild("PRF").getText());
                    prf_count++;
                }
                setPRF1(prf[0]);
                setPRF2(prf[1]);
                setPRF3(prf[2]);
                
                setPRF(null); //to recognise the TSX SC in the azimuth computation

                //the SC mode presents 4 strips which overlap, the idea is to consider one strip till the middle of the overlap area
                int b = 0;
                int strip[]=new int[3];
                for (Object o : doc.getRootElement().getChild("processing").getChildren("processingParameter")) {
                    if (b == 3) {
                        continue;
                    }
                    Element elem = (Element) o;
                    double start_range_time = new Double(elem.getChild("scanSARBeamOverlap").getChild("rangeTimeStart").getText());
                    double stop_range_time = new Double(elem.getChild("scanSARBeamOverlap").getChild("rangeTimeStop").getText());
                    double aver_range_time = start_range_time + (stop_range_time - start_range_time) / 2;
                    int width=Integer.parseInt(w);
                    int stripBound = new Double(((aver_range_time - rangeTimeStart) * width) / (rangeTimeStop - rangeTimeStart)).intValue();
                    //setMetadata("STRIPBOUND" + b++, new Integer(stripBound).toString());
                    
                    strip[0]=new Integer(stripBound);
                    b++;
                }
                setStripBound1(strip[0]);
                setStripBound2(strip[1]);
                setStripBound3(strip[2]);
            }
            
            String val=doc.getRootElement().getChild("calibration").getChild("calibrationConstant").getChild("calFactor").getText();
            setK(Double.parseDouble(val));
            
            //row and cols of the mapping_grid table used for geolocation
            MGRows = new Integer(doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("imageRaster").getChild("numberOfRows").getText());
            MGCols = new Integer(doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("imageRaster").getChild("numberOfColumns").getText());
            MGRefRow = new Integer(doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("gridReferenceTime").getChild("refRow").getText());
            MGRefCol = new Integer(doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("gridReferenceTime").getChild("refCol").getText());
            MGRowSpacing = Double.valueOf(doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("imageRaster").getChild("rowSpacing").getText());
            MGColSpacing = Double.valueOf(doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("imageRaster").getChild("columnSpacing").getText());
            String MGtTimes = doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("gridReferenceTime").getChild("tReferenceTimeUTC").getText();
            MGtTimes = MGtTimes.substring(0, MGtTimes.length() - 1);
            MGtTime = Timestamp.valueOf(MGtTimes.replaceAll("T", " ")).getTime();
            MGtauTime = Double.valueOf(doc.getRootElement().getChild("productSpecific").getChild("projectedImageInfo").getChild("mappingGridInfo").getChild("gridReferenceTime").getChild("tauReferenceTime").getText());
            ImageRowSpacing = Double.valueOf(doc.getRootElement().getChild("productInfo").getChild("imageDataInfo").getChild("imageRaster").getChild("rowSpacing").getText());
            ImageColSpacing = Double.valueOf(doc.getRootElement().getChild("productInfo").getChild("imageDataInfo").getChild("imageRaster").getChild("columnSpacing").getText());


        } catch (JDOMException ex) {
        	logger.error(ex.getMessage(),ex);
        } catch (IOException ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }
    
    @Override
    public int getNumberOfBytes() {
        return super.getNumberOfBytes();
    }

    @Override
    public int getType(boolean oneBand) {
        int nBand = getNBand();
        /*if (oneBand | nBand == 1) {
            return BufferedImage.TYPE_USHORT_GRAY;
        } else if (nBand == 2) {
            return BufferedImage.TYPE_BYTE_GRAY;
        } else if (nBand == 3) {
            return BufferedImage.TYPE_INT_RGB;
        } else if (nBand == 4) {
            return BufferedImage.TYPE_INT_ARGB;
        }

        return BufferedImage.TYPE_USHORT_GRAY;*/
        if(oneBand || bands.size()<2) return BufferedImage.TYPE_USHORT_GRAY;
        else return BufferedImage.TYPE_INT_RGB;
    }

    @Override
    public String getFormat() {
        return getClass().getCanonicalName();
    }

    @Override
    public String getDisplayName(int band) {
        return displayName;
    }

    public String getInternalImage() {
  		return null;
  	}

	@Override
	public int getWidth() {
		return  getImage(0).xSize;
	}

	@Override
	public int getHeight() {
		return getImage(0).ySize;
	}

	@Override
	public File getOverviewFile() {
		return new File(this.overview);
	}

	

	@Override
	public String getImgName() {
		return imgName;
	}
	
	public TIFF getImage(int band){
		return tiffImages.get(getBandName(band));
	}

	

}
