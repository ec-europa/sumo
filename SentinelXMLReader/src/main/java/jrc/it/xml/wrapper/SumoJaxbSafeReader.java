package jrc.it.xml.wrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

import org.jdom2.JDOMException;
import org.jdom2.xpath.XPathFactory;
import org.slf4j.LoggerFactory;

import jrc.it.safe.reader.jaxb.AcquisitionPeriod;
import jrc.it.safe.reader.jaxb.FrameSet;
import jrc.it.safe.reader.jaxb.InstrumentMode;
import jrc.it.safe.reader.jaxb.OrbitReference;
import jrc.it.safe.reader.jaxb.Platform;
import jrc.it.safe.reader.jaxb.StandAloneProductInformation;
import jrc.it.safe.reader.jaxb.XFDU;
import jrc.it.safe.reader.jaxb.XFDU.DataObjectSection.DataObject;
import jrc.it.safe.reader.jaxb.XFDU.MetadataSection.MetadataObject;


@XmlRootElement(name="xfdu:XFDU")
public class SumoJaxbSafeReader  implements ISumoSafeReader {
    private static org.slf4j.Logger logger=LoggerFactory.getLogger(SumoJaxbSafeReader.class);

	private File safefile;
	
	private JAXBContext jaxbContext =null;
	XPathFactory xFactory = null;
	
	private StandAloneProductInformation productInformation;
	private AcquisitionPeriod acquisitionPeriod;
	private FrameSet frameSet;
	private OrbitReference orbitReference;
	private Platform platform;
	
	private String[] swaths=null;
	private String[] hrefsTiff=null;
	
	

	private HashMap <String,List<String>> swathMeasurementsMap=null;
	
	
	public SumoJaxbSafeReader(final File safePath) throws JDOMException, IOException,JAXBException {
		this.safefile=safePath;
		init();
	}

	public SumoJaxbSafeReader(final String safePath) throws JDOMException, IOException,JAXBException{
		this.safefile=new File(safePath);
		init();
	}
	
	private void init() throws JAXBException {
		
		//create JAXContext instance
	    jaxbContext = (org.eclipse.persistence.jaxb.JAXBContext) JAXBContext.newInstance(jrc.it.safe.reader.jaxb.ObjectFactory.class);
	    javax.xml.bind.Unmarshaller unmarshaller=jaxbContext.createUnmarshaller();
	
	    StreamSource streamSource= new StreamSource(safefile.getAbsolutePath());
	    XFDU xfduDoc = unmarshaller.unmarshal(streamSource,XFDU.class).getValue();
	    
	    List<MetadataObject> metadataObject=xfduDoc.getMetadataSection().getMetadataObject();
	    for(MetadataObject meta:metadataObject){
	    	if(meta.getID().equalsIgnoreCase("generalProductInformation")){
	    		productInformation=meta.getMetadataWrap().getXmlData().getStandAloneProductInformation();
	    	}else if(meta.getID().equalsIgnoreCase("acquisitionPeriod")){
	    		acquisitionPeriod=meta.getMetadataWrap().getXmlData().getAcquisitionPeriod();
	    	}else if(meta.getID().equalsIgnoreCase("measurementFrameSet")){
	    		frameSet=meta.getMetadataWrap().getXmlData().getFrameSet();
	    	}else if(meta.getID().equalsIgnoreCase("measurementOrbitReference")){
	    		orbitReference=meta.getMetadataWrap().getXmlData().getOrbitReference();
	    	}else if(meta.getID().equalsIgnoreCase("platform")){
	    		platform=meta.getMetadataWrap().getXmlData().getPlatform();
	    	}
	    }
	    
	    String measurementsFolder=safefile.getParent() ;
	    
    
	    //read swath list
	    InstrumentMode instrMode=platform.getInstrument().getExtension().getInstrumentMode();
	    this.swaths=instrMode.getSwath().toArray(new String[0]);
	    ArrayList<String> hrefsTiffList=new ArrayList<String>(); 	    
	    
	    //put measurements (tiff files) in a map for polarization and swath
	    this.swathMeasurementsMap=new HashMap<String, List<String>>();
	    List<DataObject> dataObject=xfduDoc.getDataObjectSection().getDataObject();
	    for(DataObject data:dataObject){
	    	//	repID='s1Level1MeasurementSchema'
	    	if(data.getRepID().equalsIgnoreCase("s1Level1MeasurementSchema")){
	    		String tiff=measurementsFolder+"/"+data.getByteStream().getFileLocation().getHref().substring(2);
	    		hrefsTiffList.add(tiff);
	    		
	    		for(String sw:swaths){
	    			if(tiff.toLowerCase().contains(sw.toLowerCase())){
	    				List<String>measurements=swathMeasurementsMap.get(sw);
	    				if(measurements==null)
	    					measurements=new ArrayList<String>();
	    				measurements.add(tiff);
	    				swathMeasurementsMap.put(sw,measurements);
	    			}	
	    		}
	    		
	    	}
	    }
	    this.hrefsTiff=hrefsTiffList.toArray(new String[0]);
	}
	
		
	public OrbitReference getOrbitReference() {
		return orbitReference;
	}

	public void setOrbitReference(OrbitReference orbitReference) {
		this.orbitReference = orbitReference;
	}

	public StandAloneProductInformation getProductInformation() {
		return productInformation;
	}

	public void setProductInformation(
			StandAloneProductInformation productInformation) {
		this.productInformation = productInformation;
	}

	public AcquisitionPeriod getAcquisitionPeriod() {
		return acquisitionPeriod;
	}

	public void setAcquisitionPeriod(AcquisitionPeriod acquisitionPeriod) {
		this.acquisitionPeriod = acquisitionPeriod;
	}

	public FrameSet getFrameSet() {
		return frameSet;
	}

	public void setFrameSet(FrameSet frameSet) {
		this.frameSet = frameSet;
	}

	public HashMap<String, List<String>> getSwathMeasurementsMap() {
		return swathMeasurementsMap;
	}

	public void setSwathMeasurementsMap(
			HashMap<String, List<String>> swathMeasurementsMap) {
		this.swathMeasurementsMap = swathMeasurementsMap;
	}


	public String[] getSwaths() {
		return swaths;
	}

	public void setSwaths(String[] swaths) {
		this.swaths = swaths;
	}
	public String[] getHrefsTiff() {
		return hrefsTiff;
	}

	public void setHrefsTiff(String[] hrefsTiff) {
		this.hrefsTiff = hrefsTiff;
	}

	public List<String>getTiffsBySwath(String swath){
		return this.swathMeasurementsMap.get(swath);	
	}
	/**
	 * return ascending or descending
	 * @return
	 */
	public String getOrbitDirection(){
		return orbitReference.getExtension().getOrbitProperties().getPass();
	}
	public File getSafefile() {
		return safefile;
	}

	public void setSafefile(File safefile) {
		this.safefile = safefile;
	}

	
}
