package safe.reader;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import safe.annotation.GeolocationGridPointList;
import safe.annotation.Product;

public class SumoAnnotationReader {
	private JAXBContext jaxbContext =null;
	private Unmarshaller unmarshaller =null;
	private Product unmarshalledObject = null;
	
	public SumoAnnotationReader(String annotationFile) throws JAXBException{
		//create JAXContext instance
	    jaxbContext = JAXBContext.newInstance(safe.annotation.ObjectFactory.class);
	    //Unmarshaller
	    unmarshaller = jaxbContext.createUnmarshaller();
		//unmarshal the XML document to get an instance of JAXBElement.
	    unmarshalledObject = (Product)unmarshaller.unmarshal(new File(annotationFile));

	}
	
	/**
	 * 
	 * @return
	 * @throws JAXBException
	 */
	public GeolocationGridPointList getGridPoints() throws JAXBException{
	    GeolocationGridPointList points=unmarshalledObject.getGeolocationGrid().getGeolocationGridPointList();
		return points;
	} 
	
	
}
