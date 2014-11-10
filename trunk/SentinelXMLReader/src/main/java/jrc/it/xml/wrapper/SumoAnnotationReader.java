package jrc.it.xml.wrapper;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import jrc.it.annotation.reader.jaxb.AdsHeader;
import jrc.it.annotation.reader.jaxb.GeolocationGridPoint;
import jrc.it.annotation.reader.jaxb.ImageInformation;
import jrc.it.annotation.reader.jaxb.Orbit;
import jrc.it.annotation.reader.jaxb.Product;
import jrc.it.annotation.reader.jaxb.ProductInformation;
import jrc.it.safe.reader.xpath.object.wrapper.BurstInformation;

public class SumoAnnotationReader {
	private JAXBContext jaxbContext =null;
	private Unmarshaller unmarshaller =null;
	private Product unmarshalledObject = null;
	
	public SumoAnnotationReader(String annotationFile) throws JAXBException{
		//create JAXContext instance
	    jaxbContext = JAXBContext.newInstance(jrc.it.annotation.reader.jaxb.ObjectFactory.class);
	    //Unmarshaller
	    unmarshaller = jaxbContext.createUnmarshaller();
		//unmarshal the XML document to get an instance of JAXBElement.
	    unmarshalledObject = (Product)unmarshaller.unmarshal(new File(annotationFile));

	}
	
	/**
	 * 
	 * @return
	 */
	public List<GeolocationGridPoint> getGridPoints() {
	    List<GeolocationGridPoint> points=unmarshalledObject.getGeolocationGrid().getGeolocationGridPointList().getGeolocationGridPoint();
		return points;
	} 
	
	/**
	 * 
	 * @return
	 */
	public List<Orbit> getOrbits() {
	    List<Orbit> orbits=unmarshalledObject.getGeneralAnnotation().getOrbitList().getOrbit();
		return orbits;
	}

	
	/**
	 * 
	 * @return return the jaxb object that contains image informations
	 */
	public ImageInformation getImageInformation(){
		return unmarshalledObject.getImageAnnotation().getImageInformation();
	}
	
	
	/**
	 *  
	 * @return return header annotation info from xml
	 */
	public AdsHeader getHeader(){
		AdsHeader header=unmarshalledObject.getAdsHeader();
		return header;
	}
	
	public ProductInformation getProductInformation(){
		return unmarshalledObject.getGeneralAnnotation().getProductInformation();
	}
	/**
	 * Return burst informations
	 * @return
	 */
	public BurstInformation getBurstInformation(){
		return new BurstInformation(unmarshalledObject.getSwathTiming());
	}
	
	
	
}
