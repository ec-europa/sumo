package org.geoimage.impl.s1;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoTransformFactory;
import org.geoimage.impl.GDALTIFF;
import org.geoimage.impl.Gcp;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Document;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jrc.it.annotation.reader.jaxb.AdsHeaderType;
import jrc.it.annotation.reader.jaxb.DownlinkInformationType;
import jrc.it.annotation.reader.jaxb.GeolocationGridPointType;
import jrc.it.annotation.reader.jaxb.ImageInformationType;
import jrc.it.annotation.reader.jaxb.OrbitType;
import jrc.it.annotation.reader.jaxb.SwathBoundsType;
import jrc.it.annotation.reader.jaxb.SwathMergeType;
import jrc.it.safe.reader.jaxb.StandAloneProductInformation;
import jrc.it.xml.wrapper.SumoAnnotationReader;
import jrc.it.xml.wrapper.SumoJaxbSafeReader;

/**
 * 
 * 
 * @author 
 */
public abstract class GDALSentinel1 extends SarImageReader {
	protected int[] preloadedInterval = new int[]{0, 0};

    protected AffineTransform matrix;
   
    protected Document doc;
    
    protected double MGtTime = 0;
    protected double MGtauTime = 0;
    
    protected Map<String, GDALTIFF> tiffImages;

    protected List<String> bands = new ArrayList<String>();

	protected double xposition = 0;
    protected double yposition = 0;
    protected double zposition = 0;
    
    protected File mainFolder;
    private String swath=null;
    
	private Logger logger= LoggerFactory.getLogger(GDALSentinel1.class);
	
	private String files[]=new String[1];
    private List<GeolocationGridPointType> points=null;
    private List<String> tiffs=null;
    private List<String> polarizations=null;
    private String safeFilePath=null;
    
    
    @Override
    public abstract int[] readTile(int x, int y, int width, int height,int band);
    @Override
    public abstract void preloadLineTile(int y, int length,int band);
    @Override
	public abstract File getOverviewFile() ;
    
    
    private List<Swath> swaths=null;
    
    public GDALSentinel1(File f,String swath) {
    	super(f);
    	this.swath=swath;
    }

    
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
            gcps = new ArrayList<Gcp>(points.size());
			
            for(int index=0;index<points.size()-1;index++){
            	GeolocationGridPointType point=points.get(index);
            	Gcp gcp=new Gcp();
            	try{
            		gcp.setAngle(new Float(point.getIncidenceAngle().getValue()));
            	}catch(Exception e){
            		logger.info("Incident Angle not valid for grid point n:"+index);
            	}
            	gcp.setXgeo(point.getLongitude().getValue());
            	gcp.setYgeo(point.getLatitude().getValue());
            	gcp.setYpix(point.getLine().getValue().intValue());
            	gcp.setXpix(point.getPixel().getValue().intValue());
            	gcp.setOriginalXpix(point.getPixel().getValue().doubleValue());
            	gcp.setZgeo(point.getHeight().getValue());
            	gcps.add(gcp);
            }
        }    
        return gcps;
    }

    @Override
    public String[] getFilesList() {
        return files;
    }

    
    @Override
    public boolean initialise() {
        try {
        	SumoJaxbSafeReader safeReader=new SumoJaxbSafeReader(super.manifestFile);

        	files[0]=safeReader.getSafefile().getAbsolutePath();
        	tiffs=safeReader.getTiffsBySwath(this.swath);
        	polarizations=safeReader.getProductInformation().getTransmitterReceiverPolarisation();
        	safeFilePath=safeReader.getSafefile().getAbsolutePath();
        	
        	
			//set image properties
            tiffImages=getImages();
            String bandName=getBandName(0);
            
			//activeImage = tiffImages.values().iterator().next();

            String nameFirstFile=tiffImages.get(bandName).getImageFile().getName();//new File(safeReader.getHrefsTiff()[0]).getName();
            nameFirstFile=nameFirstFile.replace(".tiff", ".xml");
			//load the correct annotation file for the current images (per swath) 
			mainFolder=manifestFile.getParentFile();
			String annotationFilePath=new StringBuilder(mainFolder.getAbsolutePath()).append("/annotation/").append(nameFirstFile).toString();
			
			SumoAnnotationReader annotationReader=new SumoAnnotationReader(annotationFilePath);
			//read the ground control points
        	points= annotationReader.getGridPoints();
			

            List<SwathMergeType> swathMerges=annotationReader.getSwathMerges();
            List<DownlinkInformationType> downInfos=annotationReader.getDownLinkInformationList();
            swaths=new ArrayList<Swath>();
            for(int i=0;i<swathMerges.size();i++){
            	Swath s=new Swath();
            	s.setBounds(swathMerges.get(i).getSwathBoundsList().getSwathBounds());
            	DownlinkInformationType info=downInfos.get(i);
            	s.setAzimuthTime(info.getAzimuthTime().toGregorianCalendar().getTimeInMillis());
            	s.setFirstLineSensingTime(info.getFirstLineSensingTime().toGregorianCalendar().getTimeInMillis());
            	s.setName(info.getSwath().name());
            	s.setLastLineSensingTime(info.getLastLineSensingTime().toGregorianCalendar().getTimeInMillis());
            	s.setPrf(info.getPrf().getValue());
            }
            
        	//read and set the metadata from the manifest and the annotation
			setXMLMetaData(manifestFile,safeReader,annotationReader);
            
            gcps = getGcps();
            if (gcps == null) {
                dispose();
                return false;
            }
            String epsg = "EPSG:4326";
            //geotransform = GeoTransformFactory.createFromGcps(gcps, epsg);
            geotransform = GeoTransformFactory.createFromOrbitVector(annotationFilePath);
            
            
            //read the first orbit position from the annotation file
            List<OrbitType> orbitList=annotationReader.getOrbits();
            if(orbitList!=null&&!orbitList.isEmpty()){
            	OrbitType o=orbitList.get(0);
            	xposition=o.getPosition().getX().getValue();
            	yposition=o.getPosition().getY().getValue();
            	zposition=o.getPosition().getZ().getValue();
            }
            
            //set the satellite altitude
            double radialdist = Math.pow(xposition * xposition + yposition * yposition + zposition * zposition, 0.5);
            double[] latlon = getGeoTransform().getGeoFromPixel(0, 0);
            double[] position = new double[3];
            MathTransform convert = CRS.findMathTransform(DefaultGeographicCRS.WGS84, DefaultGeocentricCRS.CARTESIAN);
            convert.transform(latlon, 0, position, 0, 1);
            double earthradial = Math.pow(position[0] * position[0] + position[1] * position[1] + position[2] * position[2], 0.5);
            setSatelliteAltitude(radialdist - earthradial);

            // get incidence angles from gcps
            float firstIncidenceangle = (float) (this.gcps.get(0).getAngle());
            float lastIncidenceAngle = (float) (this.gcps.get(this.gcps.size() - 1).getAngle());
            setIncidenceNear(firstIncidenceangle < lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle);
            setIncidenceNear(firstIncidenceangle > lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle);


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
     */
   @Override
   public double getPRF(int x,int y){
	   boolean findPrf=false;
	   double prf=0;
	   for (int i=0;i<swaths.size()&&!findPrf;i++){
		   Swath s=swaths.get(i);
		   List<SwathBoundsType> bounds=s.getBounds();
		   for(int iBound=0;iBound<bounds.size()&&!findPrf;iBound++){
			   SwathBoundsType bound=bounds.get(iBound);
			   int xMin=bound.getFirstAzimuthLine().getValue().intValue();
			   int xMax=bound.getLastAzimuthLine().getValue().intValue();
			   int yMin=bound.getFirstRangeSample().getValue().intValue();
			   int yMax=bound.getLastRangeSample().getValue().intValue();
			   
			   if((x>=xMin && x<xMax)&&(y>=yMin&&y<yMax)){
				   findPrf=true;
				   prf=s.getPrf();
			   }
		   }
	   }
	   return prf;
   } 
    
    
    
    
   /**
    * 
    * @return
    */
    private Map<String, GDALTIFF> getImages() {
        Map<String, GDALTIFF> tiffsMap = new HashMap<String, GDALTIFF>();
    	for(String pol:polarizations){
    		for(String tiff:tiffs){
    			if(tiff.toUpperCase().contains(pol.toUpperCase())){
    				tiffsMap.put(pol,new GDALTIFF(new File(tiff),0));
    			}
    		}
    	}
    	bands=polarizations;
        return tiffsMap;
    }

   

    
    @Override
    public int readPixel(int x, int y,int band) {
        GDALTIFF tiff=null;
        try {
        	String b=getBandName(band);
        	tiff=tiffImages.get(b);
            return tiff.readShortValues(x, y,1,1)[0];
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }finally{
        }
       
        return -1;
    }

    @Override
    public String getBandName(int band) {
        return bands.get(band);
    }

    /**
     * set the current band and the associate image 
     *
    @Override
    public void setBand(int band) {
        this.band = band;
        if(tiffImages==null)
        	tiffImages=getImages();
    }*/

   
    @Override
    public void dispose() {
        super.dispose();
        if(tiffImages==null) return;
        for(GDALTIFF t:tiffImages.values()){
            t.dispose();
        }
        tiffImages=null;
    }

    /**
     * 
     * @param productxml
     * @param safeReader
     * @param annotationReader
     * @throws TransformException
     */
    private void setXMLMetaData(File productxml,SumoJaxbSafeReader safeReader,SumoAnnotationReader annotationReader) throws TransformException {

        	setSatellite(new String("Sentinel-1"));
        	setSwath(this.swath);
        	
        	//polarizations string
        	StandAloneProductInformation prodInfo=safeReader.getProductInformation();
        	List<String> pols=prodInfo.getTransmitterReceiverPolarisation();
        	String strPol="";
            for (String p:pols) {
            	strPol=strPol.concat(p).concat(" ");
            }
            setPolarization(strPol);
            setSensor("S1");
            
            //annotation header informations
            AdsHeaderType header=annotationReader.getHeader();
            setProduct(header.getProductType().value());
            
            setSatelliteOrbitInclination(98.18);
            setOrbitDirection(safeReader.getOrbitDirection());

            ImageInformationType imageInformaiton=annotationReader.getImageInformation();
            setRangeSpacing(imageInformaiton.getRangePixelSpacing().getValue());
            setAzimuthSpacing(imageInformaiton.getAzimuthPixelSpacing().getValue());
            setMetaHeight(imageInformaiton.getNumberOfLines().getValue().intValue());
            setMetaWidth(imageInformaiton.getNumberOfSamples().getValue().intValue());
            //setSatelliteSpeed(prodInfo.getProduct);
			float enl=org.geoimage.impl.ENL.getFromGeoImageReader(this);
            setENL(String.valueOf(enl));

            String start=header.getStartTime().toString().replace('T', ' ');	
            String stop=header.getStopTime().toString().replace('T', ' ');
            setTimeStampStart(start);//Timestamp.valueOf(start));
            setTimeStampStop(stop);//Timestamp.valueOf(stop));
            
            String bytesStr=annotationReader.getImageInformation().getOutputPixels();
            int bytes=Integer.parseInt(bytesStr.substring(0,3).trim())/8;
            setNumberOfBytes(bytes);

            double radarFrequency = annotationReader.getProductInformation().getRadarFrequency().getValue();
            setRadarWaveLenght(299792457.9 / radarFrequency);
            setRevolutionsPerday(14.3);

            
    }

    @Override
    public int getNumberOfBytes() {
        return super.getNumberOfBytes();
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
    public String getDisplayName(int band) {
    	try{
        	return tiffImages.get(getBandName(band)).getImageFile().getName();
    	}catch(Exception e){
    		return "S1"+System.currentTimeMillis();
    	}		
    }
    
    @Override
    public String getImgName() {
    	try{
    		String name=tiffImages.get(getBandName(0)).getImageFile().getParentFile().getParentFile().getName();
    		return name;
    	}catch(Exception e ){			
    		return tiffImages.get(getBandName(0)).getImageFile().getName();
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
		return safeFilePath;
	}
	

    @Override
	public int getWidth() {
		return getImage(0).xSize;
	}


	@Override
	public int getHeight() {
		return getImage(0).ySize;
	}
	
	

	public GDALTIFF getImage(int band){
		GDALTIFF img=null;
		try{
			img = tiffImages.get(getBandName(band));
		}catch(Exception e){ 
			logger.error(this.getClass().getName()+":getImage function  "+e.getMessage());
		}
		return img;
	}

	

}


