package jrc.it.xml.wrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.LoggerFactory;

import jrc.it.annotation.reader.jaxb.AdsHeaderType;
import jrc.it.annotation.reader.jaxb.CoordinateConversionType;
import jrc.it.annotation.reader.jaxb.DownlinkInformationListType;
import jrc.it.annotation.reader.jaxb.DownlinkInformationType;
import jrc.it.annotation.reader.jaxb.GeolocationGridPointType;
import jrc.it.annotation.reader.jaxb.ImageInformationType;
import jrc.it.annotation.reader.jaxb.L1CoordinateConversionType;
import jrc.it.annotation.reader.jaxb.L1ProductType;
import jrc.it.annotation.reader.jaxb.L1SwathMergeType;
import jrc.it.annotation.reader.jaxb.OrbitType;
import jrc.it.annotation.reader.jaxb.ProductInformationType;
import jrc.it.annotation.reader.jaxb.ReplicaInformationListType;
import jrc.it.annotation.reader.jaxb.ReplicaInformationType;
import jrc.it.annotation.reader.jaxb.SwathMergeListType;
import jrc.it.annotation.reader.jaxb.SwathMergeType;
import jrc.it.safe.reader.xpath.object.wrapper.BurstInformation;


public class SumoAnnotationReader {
	private JAXBContext jaxbContext =null;
	private Unmarshaller unmarshaller =null;
	private L1ProductType unmarshalledObject = null;
	private File annotation=null;

	private static org.slf4j.Logger logger=LoggerFactory.getLogger(SumoAnnotationReader.class);

	
	public SumoAnnotationReader(String annotationFile) throws JAXBException{
		//create JAXContext instance
	    jaxbContext = JAXBContext.newInstance(jrc.it.annotation.reader.jaxb.ObjectFactory.class);
	    //Unmarshaller
	    unmarshaller = jaxbContext.createUnmarshaller();
	    
	    annotation=new File(annotationFile);
		//unmarshal the XML document to get an instance of JAXBElement.
	    JAXBElement<L1ProductType> elemant=(JAXBElement<L1ProductType>) unmarshaller.unmarshal(annotation);
	    
	    unmarshalledObject = elemant.getValue();
	    
	}
	
	/**
	 * 
	 * @return
	 */
	public List<GeolocationGridPointType> getGridPoints() {
	    List<GeolocationGridPointType> points=unmarshalledObject.getGeolocationGrid().getGeolocationGridPointList().getGeolocationGridPoint();
		return points;
	} 
	
	/**
	 * 
	 * @return
	 */
	public List<OrbitType> getOrbits() {
	    List<OrbitType> orbits=unmarshalledObject.getGeneralAnnotation().getOrbitList().getOrbit();
		return orbits;
	}

	
	/**
	 * 
	 * @return return the jaxb object that contains image informations
	 */
	public ImageInformationType getImageInformation(){
		return unmarshalledObject.getImageAnnotation().getImageInformation();
	}
	
	
	/**
	 *  
	 * @return return header annotation info from xml
	 */
	public AdsHeaderType getHeader(){
		AdsHeaderType header=unmarshalledObject.getAdsHeader();
		return header;
	}
	
	public ProductInformationType getProductInformation(){
		return unmarshalledObject.getGeneralAnnotation().getProductInformation();
	}
	/**
	 * Return burst informations
	 * @return
	 */
	public BurstInformation getBurstInformation(){
		return new BurstInformation(unmarshalledObject.getSwathTiming());
	}
	
	/**
	 * 
	 * @return
	 */
	public List<SwathMergeType> getSwathMerges(){
		L1SwathMergeType swM=unmarshalledObject.getSwathMerging();
		SwathMergeListType list=swM.getSwathMergeList();
		
		return list.getSwathMerge();
	}
	
	/**
	 * 
	 * @return
	 */
	public List<DownlinkInformationType> getDownLinkInformationList(){
		DownlinkInformationListType downList=unmarshalledObject.getGeneralAnnotation().getDownlinkInformationList();
		List<DownlinkInformationType> infos= downList.getDownlinkInformation();
		return infos;
	}
	
	public List<ReplicaInformationType> getReplicaInformationList(){
		ReplicaInformationListType replicaList=unmarshalledObject.getGeneralAnnotation().getReplicaInformationList();
		List<ReplicaInformationType> replicas= replicaList.getReplicaInformation();
		return replicas;
	}
	
	
	/**
	 * read the coordinateConversion information
	 * @return
	 */
	public List<CoordinateConversionType> getCoordinateConversionData(){
		List<CoordinateConversionType> ccList=new ArrayList<CoordinateConversionType>();
		try{
			L1CoordinateConversionType l1ccList=unmarshalledObject.getCoordinateConversion();
			ccList=l1ccList.getCoordinateConversionList().getCoordinateConversion();
		}catch(Exception e){
			logger.info("CoordinateConversion Array is empty");
		}	
		return ccList;
	}
	
	
	
	
	public static void main(String args[]){
		try {
			SumoAnnotationReader reader=new SumoAnnotationReader("C://tmp//sumo_images//S1_PRF_SWATH_DEVEL//S1A_IW_GRDH_1SDV_20150219T053530_20150219T053555_004688_005CB5_3904.SAFE//annotation//s1a-iw-grd-vv-20150219t053530-20150219t053555-004688-005cb5-001.xml");
			List<OrbitType>orbits=reader.getOrbits();
			
			
			//List<SwathMergeType> o=reader.getSwathMerges();
			System.out.println(orbits.toString());
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	
}
