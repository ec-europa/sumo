package jrc.it.xml.wrapper;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import jrc.it.annotation.reader.jaxb.AdsHeaderType;
import jrc.it.annotation.reader.jaxb.GeolocationGridPointType;
import jrc.it.annotation.reader.jaxb.ImageInformationType;
import jrc.it.annotation.reader.jaxb.L1ProductType;
import jrc.it.annotation.reader.jaxb.L1SwathMergeType;
import jrc.it.annotation.reader.jaxb.OrbitType;
import jrc.it.annotation.reader.jaxb.ProductInformationType;
import jrc.it.annotation.reader.jaxb.SwathMergeListType;
import jrc.it.annotation.reader.jaxb.SwathMergeType;
import jrc.it.safe.reader.xpath.object.wrapper.BurstInformation;

public class SumoAnnotationReader {
	private JAXBContext jaxbContext =null;
	private Unmarshaller unmarshaller =null;
	private L1ProductType unmarshalledObject = null;
	
	public SumoAnnotationReader(String annotationFile) throws JAXBException{
		//create JAXContext instance
	    jaxbContext = JAXBContext.newInstance(jrc.it.annotation.reader.jaxb.ObjectFactory.class);
	    //Unmarshaller
	    unmarshaller = jaxbContext.createUnmarshaller();
		//unmarshal the XML document to get an instance of JAXBElement.
	    JAXBElement<L1ProductType> elemant=(JAXBElement<L1ProductType>) unmarshaller.unmarshal(new File(annotationFile));
	    
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
	
	
	public List<SwathMergeType> swaths(){
		L1SwathMergeType swM=unmarshalledObject.getSwathMerging();
		SwathMergeListType list=swM.getSwathMergeList();
		
		return list.getSwathMerge();
	}
	
	
	
	public static void main(String args[]){
		try {
			SumoAnnotationReader reader=new SumoAnnotationReader("C://tmp//sumo_images//S1_PRF_SWATH_DEVEL//S1A_IW_GRDH_1SDV_20150219T053530_20150219T053555_004688_005CB5_3904.SAFE//annotation//s1a-iw-grd-vv-20150219t053530-20150219t053555-004688-005cb5-001.xml");
			List<SwathMergeType> o=reader.swaths();
			System.out.println(o.toString());
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	
}
