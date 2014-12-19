package org.geoimage.impl.s1;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jrc.it.annotation.reader.jaxb.AdsHeader;
import jrc.it.annotation.reader.jaxb.GeolocationGridPoint;
import jrc.it.annotation.reader.jaxb.ImageInformation;
import jrc.it.annotation.reader.jaxb.Orbit;
import jrc.it.safe.reader.jaxb.StandAloneProductInformation;
import jrc.it.xml.wrapper.SumoAnnotationReader;
import jrc.it.xml.wrapper.SumoJaxbSafeReader;

import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoTransformFactory;
import org.geoimage.impl.Gcp;
import org.geoimage.impl.TIFF;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Document;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.imageio.plugins.tiff.TIFFImageReadParam;

/**
 * 
 * 
 * @author 
 */
public abstract class Sentinel1 extends SarImageReader {
	protected int[] preloadedInterval = new int[]{0, 0};

    protected AffineTransform matrix;
   
    protected Document doc;
    
    protected double MGtTime = 0;
    protected double MGtauTime = 0;
    
    protected Map<String, TIFF> tiffImages;

    protected List<String> bands = new ArrayList<String>();
    private SumoJaxbSafeReader safeReader=null;
    private SumoAnnotationReader annotationReader=null;

	protected double xposition = 0;
    protected double yposition = 0;
    protected double zposition = 0;
    
    protected File mainFolder;
    private String swath=null;
    
	private Logger logger= LoggerFactory.getLogger(Sentinel1.class);
    
    
    
    public Sentinel1(SumoJaxbSafeReader safeReader,String swath) {
    	this.safeReader=safeReader; 
    	this.swath=swath;
    }

    @Override
    public abstract int[] readTile(int x, int y, int width, int height);
    @Override
    public abstract void preloadLineTile(int y, int length);
    @Override
	public abstract File getOverviewFile() ;
    public abstract TIFF getActiveImage();
    
    
    @Override
    public int getNBand() {
        return bands.size();
    }

    /**
     * read ground control points from xml annotation file. 
     *   
     * There is one annotation file for each tiff image 
     * The annotation files are files in annotation folder with name "tiff image name".xml
     * 
     * @return list of ground control points
     */
    @Override
    public List<Gcp> getGcps() {
        if (gcps == null||gcps.size()==0) {
	        	
        	//read the ground control points
        	List<GeolocationGridPoint> points= annotationReader.getGridPoints();
            gcps = new ArrayList<Gcp>(points.size());
			
            for(int index=0;index<points.size()-1;index++){
            	GeolocationGridPoint point=points.get(index);
            	Gcp gcp=new Gcp();
            	try{
            		gcp.setAngle(Float.parseFloat((point.getIncidenceAngle().getContent())));
            	}catch(Exception e){
            		logger.info("Incident Angle not valid for grid point n:"+index);
            	}
            	gcp.setXgeo(point.getLongitude());
            	gcp.setYgeo(point.getLatitude());
            	gcp.setYpix(point.getLine().doubleValue());
            	gcp.setXpix(point.getPixel().doubleValue());
            	gcp.setOriginalXpix(point.getPixel().doubleValue());
            	gcp.setZgeo(point.getHeight());
            	gcps.add(gcp);
            }
        }    
        return gcps;
    }

    

    @Override
    public String getAccessRights() {
        return "r";
    }

    @Override
    public String[] getFilesList() {
    	String files[]=new String[1];
    	files[0]=this.safeReader.getSafefile().getAbsolutePath();
        return files;
    }

    @Override
    public boolean initialise(File manifestXML) {
        try {
        	if(safeReader==null)
        		safeReader=new SumoJaxbSafeReader(manifestXML);
        	
			//set image properties
            tiffImages=getImages();
            String bandName=getBandName(0);
            
			//activeImage = tiffImages.values().iterator().next();

            String nameFirstFile=tiffImages.get(bandName).getImageFile().getName();//new File(safeReader.getHrefsTiff()[0]).getName();
            nameFirstFile=nameFirstFile.replace(".tiff", ".xml");
			//load the correct annotation file for the current images (per swath) 
			mainFolder=manifestXML.getParentFile();
			String annotationFilePath=new StringBuilder(mainFolder.getAbsolutePath()).append("/annotation/").append(nameFirstFile).toString();
			annotationReader=new SumoAnnotationReader(annotationFilePath);
		
			//read and set the metadata from the manifest and the annotation
			setXMLMetaData(manifestXML);
            
            gcps = getGcps();
            if (gcps == null) {
                dispose();
                return false;
            }
            String epsg = "EPSG:4326";
            geotransform = GeoTransformFactory.createFromGcps(gcps, epsg);
            
            //read the first orbit position from the annotation file
            List<Orbit> orbitList=annotationReader.getOrbits();
            if(orbitList!=null&&!orbitList.isEmpty()){
            	Orbit o=orbitList.get(0);
            	xposition=o.getPosition().getX();
            	yposition=o.getPosition().getY();
            	zposition=o.getPosition().getZ();
            }
            
            //set the satellite altitude
            double radialdist = Math.pow(xposition * xposition + yposition * yposition + zposition * zposition, 0.5);
            double[] latlon = getGeoTransform().getGeoFromPixel(0, 0, epsg);
            double[] position = new double[3];
            MathTransform convert = CRS.findMathTransform(DefaultGeographicCRS.WGS84, DefaultGeocentricCRS.CARTESIAN);
            convert.transform(latlon, 0, position, 0, 1);
            double earthradial = Math.pow(position[0] * position[0] + position[1] * position[1] + position[2] * position[2], 0.5);
            setMetadata(SATELLITE_ALTITUDE, String.valueOf(radialdist - earthradial));

            // get incidence angles from gcps
            float firstIncidenceangle = (float) (this.gcps.get(0).getAngle());
            float lastIncidenceAngle = (float) (this.gcps.get(this.gcps.size() - 1).getAngle());
            setMetadata(INCIDENCE_NEAR, String.valueOf(firstIncidenceangle < lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle));
            setMetadata(INCIDENCE_FAR, String.valueOf(firstIncidenceangle > lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle));


            return true;
        } catch (TransformException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (FactoryException ex) {
        	logger.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            dispose();
            logger.error(ex.getMessage(), ex);
        } catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
        
        return false;
    }

   /**
    * 
    * @return
    */
    private Map<String, TIFF> getImages() {
    	List<String> tiffs=safeReader.getTiffsBySwath(this.swath);
    	
        Map<String, TIFF> tiffsMap = new HashMap<String, TIFF>();

    	List<String> polarizations=safeReader.getProductInformation().getTransmitterReceiverPolarisation();
    	for(String pol:polarizations){
    		for(String tiff:tiffs){
    			if(tiff.toUpperCase().contains(pol.toUpperCase())){
    				tiffsMap.put(pol,new TIFF(new File(tiff),0));
    			}
    		}
    	}
    	bands=polarizations;
        return tiffsMap;
    }

   

    
    @Override
    public int read(int x, int y) {
        TIFFImageReadParam t = new TIFFImageReadParam();
        t.setSourceRegion(new Rectangle(x, y, 1, 1));
        try {
        	String b=getBandName(band);
            return tiffImages.get(b).reader.read(0, t).getRGB(x, y);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return -1;
    }

    @Override
    public String getBandName(int band) {
        return bands.get(band);
    }

    /**
     * set the current band and the associate image 
     */
    @Override
    public void setBand(int band) {
        this.band = band;
        if(tiffImages==null)
        	tiffImages=getImages();
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

    public void setXMLMetaData(File productxml) throws TransformException {

        	setMetadata(SATELLITE, new String("Sentinel-1"));
        	setMetadata(SWATH,this.swath);
        	
        	//polarizations string
        	StandAloneProductInformation prodInfo=safeReader.getProductInformation();
        	List<String> pols=prodInfo.getTransmitterReceiverPolarisation();
        	String strPol="";
            for (String p:pols) {
            	strPol=strPol.concat(p).concat(" ");
            }
            setMetadata(POLARISATION, strPol);
            setMetadata(SENSOR, "Sentinel-1");
            
            //annotation header informations
            AdsHeader header=annotationReader.getHeader();
            setMetadata(PRODUCT, header.getProductType());
            
            setMetadata(ORBIT_DIRECTION, safeReader.getOrbitDirection());

            ImageInformation imageInformaiton=annotationReader.getImageInformation();
            setMetadata(RANGE_SPACING,imageInformaiton.getRangePixelSpacing());
            setMetadata(AZIMUTH_SPACING,imageInformaiton.getAzimuthPixelSpacing());
            setMetadata(HEIGHT,imageInformaiton.getNumberOfLines());
            setMetadata(WIDTH, imageInformaiton.getNumberOfSamples());
			float enl=org.geoimage.impl.ENL.getFromGeoImageReader(this);
            setMetadata(ENL,enl );

            String start=header.getStartTime().replace('T', ' ');	
            String stop=header.getStopTime().replace('T', ' ');
            setMetadata(TIMESTAMP_START,start);//Timestamp.valueOf(start));
            setMetadata(TIMESTAMP_STOP,stop);//Timestamp.valueOf(stop));
            
            String bytesStr=annotationReader.getImageInformation().getOutputPixels();
            int bytes=Integer.parseInt(bytesStr.substring(0,3).trim())/8;
            setMetadata(NUMBER_BYTES,bytes);

            Double radarFrequency = new Double(annotationReader.getProductInformation().getRadarFrequency());
            setMetadata(RADAR_WAVELENGTH, String.valueOf(299792457.9 / radarFrequency));
            

            
    }

    @Override
    public int getNumberOfBytes() {
        return (Integer) getMetadata(NUMBER_BYTES);
    }

    @Override
    public int getType(boolean oneBand) {
        if(oneBand || bands.size()<2) return BufferedImage.TYPE_USHORT_GRAY;
        else return BufferedImage.TYPE_INT_RGB;
    }

    @Override
    public String getFormat() {
        return getClass().getCanonicalName();
    }

    @Override
    public String getName() {
    	try{
        	return tiffImages.get(getBandName(band)).getImageFile().getName();
    	}catch(Exception e){
    		return "S1"+System.currentTimeMillis();
    	}		
    }

    public String getInternalImage() {
  		return null;
  	}
    
    public String getSwath() {
		return swath;
	}


	public void setSwath(String swath) {
		this.swath = swath;
	}
	
	
	public String getSafeFilePath(){
		return safeReader.getSafefile().getAbsolutePath();
	}
	
	public SumoJaxbSafeReader getSafeReader() {
		return safeReader;
	}

	public void setSafeReader(SumoJaxbSafeReader safeReader) {
		this.safeReader = safeReader;
	}
	
	public SumoAnnotationReader getAnnotationReader() {
		return annotationReader;
	}

	public void setAnnotationReader(SumoAnnotationReader annotationReader) {
		this.annotationReader = annotationReader;
	}

	@Override
	public boolean supportAzimuthAmbiguity() {
		return true;
	}
}


