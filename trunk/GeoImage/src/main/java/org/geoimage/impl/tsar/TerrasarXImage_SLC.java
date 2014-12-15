package org.geoimage.impl.tsar;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoimage.factory.GeoTransformFactory;
import org.geoimage.impl.Gcp;
import org.geoimage.impl.TIFF;
import org.geoimage.impl.envi.EnvisatImage;
import org.geoimage.utils.ByteUtils;
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

/**
 * A class that reads Terrasar-X SLC images
 * @author gabbaan
 */
public class TerrasarXImage_SLC extends TerrasarXImage {
    private int xOffset = 8; //each row has 8 bytes of offset at the beginning
    //private int offsetBand; //to consider images with more than one band, NOT YET IMPLEMENTED!!!
    private byte[] preloadedDataSLC;
   // protected Map<String, File> images;
    RandomAccessFile fss;

    public TerrasarXImage_SLC() {

    }

    @Override
    public boolean initialise(File file) {
    	try {
    		this.name=file.getName();
    		setFile(file);
        	
        	parseProductXML(productxml);
        	
        	tiffImages = getImages();
        	image = tiffImages.values().iterator().next();
        	image.xSize = new Integer((String)getMetadata(WIDTH));
            image.ySize = new Integer((String)getMetadata(HEIGHT));
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
            double[] latlon = getGeoTransform().getGeoFromPixel(0, 0, "EPSG:4326");
            double[] position = new double[3];
            convert = CRS.findMathTransform(DefaultGeographicCRS.WGS84, DefaultGeocentricCRS.CARTESIAN);
            convert.transform(latlon, 0, position, 0, 1);
            double earthradial = Math.pow(position[0] * position[0] + position[1] * position[1] + position[2] * position[2], 0.5);
            setMetadata(SATELLITE_ALTITUDE, String.valueOf(radialdist - earthradial));

            // get incidence angles from gcps
            // !!possible to improve
            float firstIncidenceangle = (float) (this.gcps.get(0).getAngle());
            float lastIncidenceAngle = (float) (this.gcps.get(this.gcps.size() - 1).getAngle());
            setMetadata(INCIDENCE_NEAR, String.valueOf(firstIncidenceangle < lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle));
            setMetadata(INCIDENCE_FAR, String.valueOf(firstIncidenceangle > lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle));

        } catch (TransformException ex) {
            Logger.getLogger(TerrasarXImage_SLC.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FactoryException ex) {
            Logger.getLogger(TerrasarXImage_SLC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

   
    /**
     * 
     * @return
     */
    private Map<String, TIFF> getImages() {
        @SuppressWarnings("unchecked")
		List<Object> elements = doc.getRootElement().getChild("productComponents").getChildren("imageData");
        Map<String, TIFF> tiffs = new HashMap<String, TIFF>();
        bands=new Vector<String>();
        for (Object o : elements) {
            if (o instanceof Element) {
                StringBuilder f = new StringBuilder(productxml.getParent())
                		.append("\\")
                		.append( ((Element) o).getChild("file").getChild("location").getChild("path").getText())
                		.append("\\").append(((Element) o).getChild("file").getChild("location").getChild("filename").getText());
                String polarisation = ((Element) o).getChild("polLayer").getValue();
                tiffs.put(polarisation,new TIFF(new File(f.toString()),0));
                bands.add(polarisation);
            }
        }
        return tiffs;
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

        try{
	        int yOffset = xOffset + 4 * image.xSize;
	        int xinit = rect.x - x;
	        int yinit = rect.y - y;
	        for (int i = 0; i < rect.height; i++) {
	            for (int j = 0; j < rect.width; j++) {
	                int temp = i * yOffset + j * 4 + 4 * rect.x + xOffset;
	                //long real = (preloadedDataSLC[temp + 0] << 8) | (preloadedDataSLC[temp + 1] & 0xff);
	                //long img = ((preloadedDataSLC[temp + 2]) << 8) | (preloadedDataSLC[temp + 3] & 0xff);
	                byte[] bufferReal={preloadedDataSLC[temp], preloadedDataSLC[temp+1]};

	                //true = use Big Endian
	                long real = ByteUtils.byteArrayToShort(bufferReal,true);
	                byte[] bufferImg={preloadedDataSLC[temp+2], preloadedDataSLC[temp+3]};
	                long img = ByteUtils.byteArrayToShort(bufferImg,true);

	                tile[(i + yinit) * width + j + xinit] = (int) Math.sqrt(real * real + img * img);

	            }
	        }
        }catch(Exception e){
            Logger.getLogger(TerrasarXImage_SLC.class.getName()).log(Level.SEVERE, "cannot read pixel (" + x + "," + y + ")", e);
        }
        return tile;
    }


    @Override
    //used by the position dialog to link pixel(x,y) to pixel value
    public int read(int x, int y) {
        int result = 0;
        long temp = 0;
        byte[] pixelByte = new byte[4];
        if (x >= 0 & y >= 0 & x < image.xSize & y < image.ySize) {
            try {
                temp = ((y+4) * (xOffset + image.xSize * 4) + xOffset + x * 4);
                fss.seek(temp);
                fss.read(pixelByte, 0, 4);
                byte interm0 = pixelByte[0];
                byte interm1 = pixelByte[1];
                byte interm2 = pixelByte[2];
                byte interm3 = pixelByte[3];
                long real = ((interm0) << 8) | (interm1 & 0xFF);
                long img = ((interm2) << 8) | (interm3 & 0xFF);
                result = (int) Math.sqrt(real * real + img * img);
            } catch (Exception e) {
                Logger.getLogger(TerrasarXImage_SLC.class.getName()).log(Level.SEVERE, "cannot read pixel (" + x + "," + y + ")", e);
            }
        }
        return result;
    }

    @Override
    public void preloadLineTile(int y, int length) {
        if (y < 0) {
            return;
        }
        //if(preloadedDataSLC==null){
	        preloadedInterval = new int[]{y, y + length};
	        //y+4 skips the first 4 lines of offset
	        int tileOffset = (y+4) * (image.xSize * 4 + xOffset);
	        preloadedDataSLC = new byte[(image.xSize * 4 + xOffset) * length];
	        //preloadedDataSLC = new byte[getWidth()*getHeight()];
	        try {
	            File fimg=tiffImages.get("HH").getImageFile();
	        	fss = new RandomAccessFile(fimg.getAbsolutePath(), "r");
	        	fss.seek(tileOffset);
	            fss.read(preloadedDataSLC);
	            fss.close();
	        } catch (IOException e) {
	            Logger.getLogger(EnvisatImage.class.getName()).log(Level.SEVERE, "cannot preload the line tile", e);
	        }
    }

    //set all the metadata available in the xml file
    public void parseProductXML(File productxml) throws TransformException {
        try {
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build(productxml);
            Element atts = doc.getRootElement().getChild("productInfo");
            setMetadata(SATELLITE, new String("TerraSAR-X"));
            setMetadata(SENSOR, atts.getChild("acquisitionInfo").getChild("sensor").getText());
            setMetadata(POLARISATION, atts.getChild("acquisitionInfo").getChild("polarisationList").getChild("polLayer").getText());
            setMetadata(LOOK_DIRECTION, atts.getChild("acquisitionInfo").getChild("lookDirection").getText());
            setMetadata(MODE, atts.getChild("acquisitionInfo").getChild("imagingMode").getText());
            bands.add((String) getMetadata(POLARISATION));

            setMetadata(PRODUCT, atts.getChild("productVariantInfo").getChild("productType").getText());
            setMetadata(ORBIT_DIRECTION, atts.getChild("missionInfo").getChild("orbitDirection").getText());
            String h=atts.getChild("imageDataInfo").getChild("imageRaster").getChild("numberOfRows").getText();
            setMetadata(HEIGHT, h);
            String w=atts.getChild("imageDataInfo").getChild("imageRaster").getChild("numberOfColumns").getText();
            setMetadata(WIDTH,w );

            setMetadata(NUMBER_BYTES, new Integer(atts.getChild("imageDataInfo").getChild("imageDataDepth").getText()) / 8);
            setMetadata(ENL, atts.getChild("imageDataInfo").getChild("imageRaster").getChild("azimuthLooks").getText());
            
            setMetadata(HEADING_ANGLE, atts.getChild("sceneInfo").getChild("headingAngle").getText());
            rangeTimeStart = Double.valueOf(atts.getChild("sceneInfo").getChild("rangeTime").getChild("firstPixel").getText());
            rangeTimeStop = Double.valueOf(atts.getChild("sceneInfo").getChild("rangeTime").getChild("lastPixel").getText());
            String time = atts.getChild("sceneInfo").getChild("start").getChild("timeUTC").getText();
            //time = time.substring(0, time.lastIndexOf("."));
            setMetadata(TIMESTAMP_START,time.replaceAll("T", " "));
            time = atts.getChild("sceneInfo").getChild("stop").getChild("timeUTC").getText();
            //time = time.substring(0, time.lastIndexOf("."));
            setMetadata(TIMESTAMP_STOP,time.replaceAll("T", " "));

            atts = doc.getRootElement().getChild("productSpecific");
            setMetadata(RANGE_SPACING, atts.getChild("complexImageInfo").getChild("projectedSpacingRange").getChild("slantRange").getText());
            setMetadata(AZIMUTH_SPACING, atts.getChild("complexImageInfo").getChild("projectedSpacingAzimuth").getText());


            // calculate satellite speed using state vectors
            atts = doc.getRootElement().getChild("platform").getChild("orbit").getChild("stateVec");
            double xvelocity = Double.valueOf(atts.getChildText("velX"));
            double yvelocity = Double.valueOf(atts.getChildText("velY"));
            double zvelocity = Double.valueOf(atts.getChildText("velZ"));
            double satellite_speed = Math.sqrt(xvelocity * xvelocity + yvelocity * yvelocity + zvelocity * zvelocity);
            setMetadata(SATELLITE_SPEED, String.valueOf(satellite_speed));
            xposition = Double.valueOf(atts.getChildText("posX"));
            yposition = Double.valueOf(atts.getChildText("posY"));
            zposition = Double.valueOf(atts.getChildText("posZ"));

            float radarFrequency = new Float(doc.getRootElement().getChild("instrument").getChild("radarParameters").getChild("centerFrequency").getText());
            setMetadata(RADAR_WAVELENGTH, String.valueOf(299792457.9 / radarFrequency));

            setMetadata(SATELLITE_ORBITINCLINATION, "97.44");
            setMetadata(REVOLUTIONS_PERDAY, String.valueOf(11));


            //metadata used for ScanSAR mode during the Azimuth ambiguity computation
            if (getMetadata(MODE).equals("SC")) {
                //extraction of the 4 PRF codes
                int prf_count = 1;
                for (Object o : doc.getRootElement().getChild("instrument").getChildren("settings")) {
                    Element elem = (Element) o;
                    setMetadata("PRF" + prf_count, elem.getChild("settingRecord").getChild("PRF").getText());
                    prf_count++;
                }

                setMetadata(PRF, ""); //to recognise the TSX SC in the azimuth computation

                //the SC mode presents 4 strips which overlap, the idea is to consider one strip till the middle of the overlap area
                int b = 1;
                for (Object o : doc.getRootElement().getChild("processing").getChildren("processingParameter")) {
                    if (b == 4) {
                        continue;
                    }
                    Element elem = (Element) o;
                    double start_range_time = new Double(elem.getChild("scanSARBeamOverlap").getChild("rangeTimeStart").getText());
                    double stop_range_time = new Double(elem.getChild("scanSARBeamOverlap").getChild("rangeTimeStop").getText());
                    double aver_range_time = start_range_time + (stop_range_time - start_range_time) / 2;

                    int stripBound = new Double(((aver_range_time - rangeTimeStart) * image.xSize) / (rangeTimeStop - rangeTimeStart)).intValue();
                    setMetadata("STRIPBOUND" + b++, new Integer(stripBound).toString());
                }
            }else{
                setMetadata("PRF", doc.getRootElement().getChild("instrument").getChild("settings").getChild("settingRecord").getChild("PRF").getText());
            }

            setMetadata(K, doc.getRootElement().getChild("calibration").getChild("calibrationConstant").getChild("calFactor").getText());

        } catch (JDOMException ex) {
            Logger.getLogger(TerrasarXImage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TerrasarXImage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void setBand(int band) {
        this.band = band;
    }
    @Override
    //in the SLC mode, the georef.xml file contains directly the link between pixel position (x,y) and (lon, lat)
    public List<Gcp> getGcps() {
        try {
            if (gcps != null) {
                return gcps;
            }
            gcps = new ArrayList<Gcp>();
            //link to the GEOREF.xml file
            File GeoFile = new File((new File((new File(files[0])).getParent())) + "/ANNOTATION/GEOREF.xml");
            SAXBuilder builder = new SAXBuilder();
            Document geodoc = builder.build(GeoFile);
            Element atts = geodoc.getRootElement().getChild("geolocationGrid");
            //number of Geo Grid elements
            Integer GGRows = new Integer(atts.getChild("numberOfGridPoints").getChild("azimuth").getText());
            Integer GGCols = new Integer(atts.getChild("numberOfGridPoints").getChild("range").getText());

            for (Object o : atts.getChildren("gridPoint")) {
                Gcp gcp = new Gcp();
                Element elem = (Element) o;
                gcp.setXpix(Double.parseDouble(elem.getChild("col").getText()));
                gcp.setOriginalXpix(Double.parseDouble(elem.getChild("col").getText()));
                gcp.setYpix(Double.parseDouble(elem.getChild("row").getText()));
                gcp.setXgeo(Double.parseDouble(elem.getChild("lon").getText()));
                gcp.setYgeo(Double.parseDouble(elem.getChild("lat").getText()));
                gcp.setAngle(Float.parseFloat(elem.getChild("inc").getText()));
                gcps.add(gcp);
            }
            return gcps;
        } catch (JDOMException ex) {
            Logger.getLogger(TerrasarXImage_SLC.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(TerrasarXImage_SLC.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}

