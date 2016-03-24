/*
 * 
 */
package org.geoimage.impl.s1;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;
import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoTransformFactory;
import org.geoimage.impl.Gcp;
import org.geoimage.impl.geoop.GeoTransformOrbitState;
import org.geoimage.impl.imgreader.IReader;
import org.geoimage.impl.imgreader.TIFF;
//import org.geoimage.viewer.core.SumoPlatform;
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
import jrc.it.annotation.reader.jaxb.VelocityType;
import jrc.it.safe.reader.jaxb.StandAloneProductInformation;
import jrc.it.xml.wrapper.SumoAnnotationReader;
import jrc.it.xml.wrapper.SumoJaxbSafeReader;

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

    protected Map<String, IReader> tiffImages;

    //protected List<String> bands = new ArrayList<String>();
    protected List<String> polarizations=null;


	protected double xposition = 0;
    protected double yposition = 0;
    protected double zposition = 0;

    protected File mainFolder;
    protected String swath=null;
    protected int ipfVersion=0;

	private Logger logger= LoggerFactory.getLogger(Sentinel1.class);

	protected String files[]=new String[1];
	protected List<GeolocationGridPointType> points=null;
    protected List<String> tiffs=null;

    protected String safeFilePath=null;
    protected String instumentationMode="";

    @Override
    public abstract int[] readTile(int x, int y, int width, int height,int band);
    @Override
    public abstract void preloadLineTile(int y, int length,int band);
    @Override
	public abstract File getOverviewFile() ;

    protected String geolocationAlgorithm=null;

    protected List<Swath> swaths=null;


    public Sentinel1(String swath,String manifestXMLPath,String geolocationMethod) {
    	super(new File(manifestXMLPath));
    	this.swath=swath;
    	this.geolocationAlgorithm=geolocationMethod;
    }

    public Sentinel1(String swath,File manifestXML,String geolocationMethod) {
    	super(manifestXML);
    	this.swath=swath;
    	this.geolocationAlgorithm=geolocationMethod;
    }


    @Override
    public int getNBand() {
        return polarizations.size();
    }

    public int getIpfVersion() {
		return ipfVersion;
	}
	public void setIpfVersion(int ipfVersion) {
		this.ipfVersion = ipfVersion;
	}


    public String getInstumentationMode() {
		return instumentationMode;
	}
	public void setInstumentationMode(String instumentationMode) {
		this.instumentationMode = instumentationMode;
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

    public String getGeolocationAlgorithm() {
		return geolocationAlgorithm;
	}
	public void setGeolocationAlgorithm(String geolocationAlgorithm) {
		this.geolocationAlgorithm = geolocationAlgorithm;
	}


    @Override
    public boolean initialise() {
        try {
        	SumoJaxbSafeReader safeReader=new SumoJaxbSafeReader(super.manifestFile);

        	files[0]=safeReader.getSafefile().getAbsolutePath();
        	tiffs=safeReader.getTiffsBySwath(this.swath);
        	polarizations=safeReader.getProductInformation().getTransmitterReceiverPolarisation();
        	safeFilePath=safeReader.getSafefile().getAbsolutePath();
        	this.instumentationMode=safeReader.getInstrumentationMode();

        	this.ipfVersion=safeReader.getIPFVersion();

			//set image properties
            tiffImages=getImages();
            String bandName=getBandName(0);

            String nameFirstFile=tiffImages.get(bandName).getImageFile().getName();
            nameFirstFile=nameFirstFile.replace(".tiff", ".xml");
			//load the correct annotation file for the current images (per swath)
			mainFolder=manifestFile.getParentFile();
			String annotationFilePath=new StringBuilder(mainFolder.getAbsolutePath()).append("/annotation/").append(nameFirstFile).toString();

			SumoAnnotationReader annotationReader=new SumoAnnotationReader(annotationFilePath);
			//read the ground control points
        	points= annotationReader.getGridPoints();

        	super.pixelsize[0]=annotationReader.getImageInformation().getRangePixelSpacing().getValue();
        	super.pixelsize[1]=annotationReader.getImageInformation().getAzimuthPixelSpacing().getValue();


            List<SwathMergeType> swathMerges=annotationReader.getSwathMerges();
            List<DownlinkInformationType> downInfos=annotationReader.getDownLinkInformationList();
            swaths=new ArrayList<Swath>();
            for(int i=0;i<downInfos.size();i++){
            	Swath s=new Swath();
            	if(!swathMerges.isEmpty())
            		s.setBounds(swathMerges.get(i).getSwathBoundsList().getSwathBounds());
            	DownlinkInformationType info=downInfos.get(i);
            	s.setAzimuthTime(info.getAzimuthTime().toGregorianCalendar().getTimeInMillis());
            	s.setFirstLineSensingTime(info.getFirstLineSensingTime().toGregorianCalendar().getTimeInMillis());
            	s.setName(info.getSwath().name());
            	s.setLastLineSensingTime(info.getLastLineSensingTime().toGregorianCalendar().getTimeInMillis());
            	s.setPrf(info.getPrf().getValue());
            	swaths.add(s);
            }

        	//read and set the metadata from the manifest and the annotation
			setXMLMetaData(manifestFile,safeReader,annotationReader);

            gcps = getGcps();
            if (gcps == null) {
                dispose();
                return false;
            }


          //TODO: remove or change this control!!!
            if(geolocationAlgorithm.equalsIgnoreCase("ORB")&& this instanceof Sentinel1GRD){
                   geotransform = GeoTransformFactory.createFromOrbitVector(annotationReader);
            }else{
            	String epsg = "EPSG:4326";
            	geotransform = GeoTransformFactory.createFromGcps(gcps, epsg);
            }

            //read the first orbit position from the annotation file
            List<OrbitType> orbitList=annotationReader.getOrbits();
            if(orbitList!=null&&!orbitList.isEmpty()){
            	OrbitType o=orbitList.get(0);
            	xposition=o.getPosition().getX().getValue();
            	yposition=o.getPosition().getY().getValue();
            	zposition=o.getPosition().getZ().getValue();
            }

            //use the middle orbit position to calculate the satellite speed
            VelocityType middlePos=orbitList.get(orbitList.size()/2).getVelocity();
            satelliteSpeed=Math.sqrt((middlePos.getX().getValue()*middlePos.getX().getValue()+
            		middlePos.getY().getValue()*middlePos.getY().getValue()+
            		middlePos.getZ().getValue()*middlePos.getZ().getValue()));


            //set the satellite altitude
            double radialdist = Math.pow(xposition * xposition + yposition * yposition + zposition * zposition, 0.5);
            double[] latlon = geotransform.getGeoFromPixel(0, 0);
            double[] position = new double[3];
            MathTransform convert = CRS.findMathTransform(DefaultGeographicCRS.WGS84, DefaultGeocentricCRS.CARTESIAN);
            convert.transform(latlon, 0, position, 0, 1);

            double earthradial = Math.pow(position[0] * position[0] + position[1] * position[1] + position[2] * position[2], 0.5);
            setSatelliteAltitude(radialdist - earthradial);

            // get incidence angles from gcps
            float firstIncidenceangle = (float) (this.gcps.get(0).getAngle());
            float lastIncidenceAngle = (float) (this.gcps.get(this.gcps.size() - 1).getAngle());
            setIncidenceNear(firstIncidenceangle < lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle);
            setIncidenceFar(firstIncidenceangle > lastIncidenceAngle ? firstIncidenceangle : lastIncidenceAngle);

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
	   Swath s=findSwath(x, y);
	   return s.getPrf();
   }

   public String getSwathName(int x,int y){
	   Swath s=findSwath(x, y);
	   return s.getName();
   }

   /**
    *
    * @param x
    * @param y
    * @return
    */
   private Swath findSwath(int x,int y){
	   if(swaths.size()==1)
		   return swaths.get(0);
	   boolean findPrf=false;
	   Swath s=null;
	   for (int i=0;i<swaths.size()&&!findPrf;i++){
		   s=swaths.get(i);
		   List<SwathBoundsType> bounds=s.getBounds();
		   for(int iBound=0;iBound<bounds.size()&&!findPrf;iBound++){
			   SwathBoundsType bound=bounds.get(iBound);
			   int yMin=bound.getFirstAzimuthLine().getValue().intValue();
			   int yMax=bound.getLastAzimuthLine().getValue().intValue();
			   int xMin=bound.getFirstRangeSample().getValue().intValue();
			   int xMax=bound.getLastRangeSample().getValue().intValue();

			   if((x>=xMin && x<xMax)&&(y>=yMin&&y<yMax)){
				   findPrf=true;
				   break;
				   //prf=s.getPrf();
			   }
		   }
	   }
	   return s;
   }

   /**
    *
    * @return
    */
    private Map<String, IReader> getImages() {
        Map<String, IReader> tiffsMap = new HashMap<String, IReader>();
    	for(String pol:polarizations){
    		for(String tiff:tiffs){
    			if(tiff.toUpperCase().contains(pol.toUpperCase())){
    				tiffsMap.put(pol,new TIFF(new File(tiff),0));
    			}
    		}
    	}
        return tiffsMap;
    }

    @Override
    public int readPixel(int x, int y,int band) {
    	return read(x,y,1,1,band)[0];
    }

    /**
	  *
	  * @param x
	  * @param y
	  * @param width
	  * @param height
	  * @param band
	  * @return
	  */
   public int[] read(int x, int y,int w,int h, int band) {
       Rectangle rect = new Rectangle(x, y, w, h);
       rect = rect.intersection(getImage(band).getBounds());
       int data[]=null;

        TIFF tiff=(TIFF)getImage(band);
        BufferedImage bi=null;
        try {
        	bi=tiff.read(0, rect);
        } catch (Exception ex) {
        	try {
        		//try again
        		Thread.sleep(100);
				bi=tiff.read(0, rect);
			} catch (IOException | InterruptedException e) {
				logger.warn(ex.getMessage());
			}
        }finally{
        }
        if(bi!=null){
			DataBufferUShort raster=(DataBufferUShort)bi.getRaster().getDataBuffer();
			short[] b=raster.getData();
			data=new int[b.length];
	    	for(int i=0;i<b.length;i++)
	    		data[i]=b[i];
        }
       return data;
   }


    @Override
    public String getBandName(int band) {
        return polarizations.get(band);
    }
    public String[] getBands(){
    	return polarizations.toArray(new String[0]);
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
        for(IReader t:tiffImages.values()){
            t.dispose();
        }
        tiffImages=null;
    }


    @Override
    public int[] getAmbiguityCorrection(final int xPos,final int yPos) {
	    orbitInclination = FastMath.toRadians(getSatelliteOrbitInclination());

        double temp, deltaAzimuth, deltaRange;
        int[] output = new int[2];

        try {
        	// already in radian
            double incidenceAngle = getIncidence(xPos);
            double[] lonlat=geotransform.getGeoFromPixel(xPos, yPos);
            double slantRange = getSlanteRange(lonlat[0], lonlat[1]);
          //  double sold=getSlantRange(xPos,incidenceAngle);
            double prf = getPRF(xPos,yPos);

            double sampleDistAzim = getPixelsize()[1];
            double sampleDistRange= getPixelsize()[0];

            temp = (getRadarWaveLenght() * slantRange * prf) /
                    (2 * satelliteSpeed * (1 - FastMath.cos(orbitInclination) / getRevolutionsPerday()));

            //azimuth and delta in number of pixels
            deltaAzimuth = temp / sampleDistAzim;
            deltaRange = (temp * temp) / (2 * slantRange * sampleDistRange * FastMath.sin(incidenceAngle));

            output[0] = (int) FastMath.floor(deltaAzimuth);
            output[1] = (int) FastMath.floor(deltaRange);

        } catch (Exception ex) {
        	logger.error("Problem calculating the Azimuth ambiguity:"+ex.getMessage(),ex);
        }
        return output;
    }

	public double getSlanteRange(double lat, double lon) {
		return ((GeoTransformOrbitState)geotransform).getSlanteRangeDist(lon, lat);
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
            setRevolutionsPerday(14.5833);//repeat cycle of 12 days, cycle length of 175 days ==>175/12=14.5833


    }
    @Override
    public Date getImageDate(){
    	Timestamp t=Timestamp.valueOf(getTimeStampStart());
    	return new Date(t.getTime());
    }


    @Override
    public int getNumberOfBytes() {
        return super.getNumberOfBytes();
    }

    @Override
    public int getType(boolean oneBand) {
        if(oneBand || polarizations.size()<2) return BufferedImage.TYPE_USHORT_GRAY;
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

    @Override
    public double calcSatelliteSpeed(){
    	return satelliteSpeed;
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
		return getImage(0).getxSize();
	}


	@Override
	public int getHeight() {
		return getImage(0).getySize();
	}



	public IReader getImage(int band){
		IReader img=null;
		try{
			img = tiffImages.get(getBandName(band));
		}catch(Exception e){
			logger.error(this.getClass().getName()+":getImage function  "+e.getMessage());
		}
		return img;
	}

	@Override
	public String getSensor() {
		return "S1";
	}
}


