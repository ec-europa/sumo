/*
 * 
 */
package org.geoimage.impl.s1;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geoimage.factory.GeoTransformFactory;
import org.geoimage.impl.imgreader.GeoToolsGDALReader;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jrc.it.annotation.reader.jaxb.AdsHeaderType;
import jrc.it.annotation.reader.jaxb.DownlinkInformationType;
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
public class GDALSentinel1 extends Sentinel1 {
    protected Map<String, GeoToolsGDALReader> tiffImages;

	private Logger logger= LoggerFactory.getLogger(GDALSentinel1.class);
    protected int[] preloadedData;
    
    
    
    
    @Override
    public int[] readTile(int x, int y, int width, int height,int band) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect = rect.intersection(getImage(band).getBounds());
        int[] tile = new int[height * width];
        if (rect.isEmpty()) {
            return tile;
        }

        if (rect.y != preloadedInterval[0] || rect.y + rect.height != preloadedInterval[1]||preloadedData.length<(rect.width*rect.height-1)) {
            preloadLineTile(rect.y, rect.height,band);
        }else{
        	//logger.debug("using preloaded data");
        }

        int yOffset = getImage(band).getxSize();
        int xinit = rect.x - x;
        int yinit = rect.y - y;
        for (int i = 0; i < rect.height; i++) {
            for (int j = 0; j < rect.width; j++) {
                int temp = i * yOffset + j + rect.x;
                try{
                	tile[(i + yinit) * width + j + xinit] = preloadedData[temp];
                }catch(ArrayIndexOutOfBoundsException e ){
                	logger.warn("readTile function:"+e.getMessage());
                }	
            }
            }
        return tile;
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
   public int readTileXY(int x, int y, int band) {
       int val=0;

       	if (y < 0||y>this.getHeight()||x<0||x>this.getWidth()) {
	            val= 0;
       	}else{
	 	        GeoToolsGDALReader tiff=(GeoToolsGDALReader)getImage(band);
	 	        try {
	 	        	int[] bi=null;
	        		bi=tiff.readPixValues(x, y, 1, 1);
	 	        	val=bi[0];
	 	        } catch (Exception ex) {
	 	            logger.error(ex.getMessage(),ex);
	 	        }
       	}  
       
       return val;
   }
   


	
	public  void preloadLineTile(int y, int length,int band) {
       if (y < 0) {
           return;
       }
       preloadedInterval = new int[]{y, y + length};
       Rectangle rect = new Rectangle(0, y, getImage(band).getxSize(), length);
       
       GeoToolsGDALReader tiff=(GeoToolsGDALReader)getImage(band);
       rect=tiff.getBounds().intersection(rect);
       
       try {
       	int[] bi=null;
       	try{
       		bi = tiff.readPixValues(0, y, rect.width, rect.height);
       	}catch(Exception e){
       		logger.warn("Problem reading image POS x:"+0+ "  y: "+y +"   try to read again");
       		try {
   			    Thread.sleep(100);                 
   			} catch(InterruptedException exx) {
   			    Thread.currentThread().interrupt();
   			}
       		bi=tiff.readPixValues(0, y, rect.width, rect.height);
       	}	
       	preloadedData=bi;
       } catch (Exception ex) {
           logger.error(ex.getMessage(),ex);
       }finally{
       	//tiff.reader.addIIOReadProgressListener(this);
       	//readComplete=false;
       	
       }
   }
	
	@Override
	public File getOverviewFile() {
		return null;
	}
		
    public GDALSentinel1(String swath,File manifest,String geolocationMethod) {
    	super(swath,manifest,geolocationMethod);
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
        	instumentationMode=safeReader.getInstrumentationMode();
        	
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
            if(geolocationAlgorithm.equalsIgnoreCase("ORB")){
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

      GeoToolsGDALReader tiff=(GeoToolsGDALReader)getImage(band);
       try {
   		int[] b=tiff.readPixValues(x, y, w,h);
   		data=new int[b.length];
       	for(int i=0;i<b.length;i++)
       		data[i]=b[i];
   		
       } catch (Exception ex) {
           logger.warn(ex.getMessage());
       }finally{
       }
      
      return data;
  }
    
   /**
    * 
    * @return
    */
    private Map<String, GeoToolsGDALReader> getImages() {
        Map<String, GeoToolsGDALReader> tiffsMap = new HashMap<String, GeoToolsGDALReader>();
    	for(String pol:polarizations){
    		for(String tiff:tiffs){
    			if(tiff.toUpperCase().contains(pol.toUpperCase())){
    				tiffsMap.put(pol,new GeoToolsGDALReader(new File(tiff),1));
    			}
    		}
    	}
        return tiffsMap;
    }

   

    
    @Override
    public long readPixel(int x, int y,int band) {
    	GeoToolsGDALReader tiff=null;
        try {
        	String b=getBandName(band);
        	tiff=tiffImages.get(b);
            return tiff.readPixValues(x, y,1,1)[0];
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }finally{
        }
       
        return -1;
    }

    

   
    @Override
    public void dispose() {
        super.dispose();
        if(tiffImages==null) return;
        for(GeoToolsGDALReader t:tiffImages.values()){
          //  t.dispose();
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

        	this.imgName=manifestFile.getParentFile().getName();
        	
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

    public double[] getPixelsize() {
		return super.pixelsize;
	}
	

	public GeoToolsGDALReader getImage(int band){
		GeoToolsGDALReader img=null;
		try{
			img = tiffImages.get(getBandName(band));
		}catch(Exception e){ 
			logger.error(this.getClass().getName()+":getImage function  "+e.getMessage());
		}
		return img;
	}

	@Override
    public String getDisplayName(int band) {
    	try{
        	return tiffImages.get(getBandName(band)).getImageFile().getName();
    	}catch(Exception e){
    		return "S1"+System.currentTimeMillis();
    	}		
    }

}


