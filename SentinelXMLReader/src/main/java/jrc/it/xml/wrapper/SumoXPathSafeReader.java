package jrc.it.xml.wrapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import jrc.it.safe.reader.xpath.object.wrapper.AcquisitionPeriod;
import jrc.it.safe.reader.xpath.object.wrapper.OrbitInformation;
import jrc.it.safe.reader.xpath.object.wrapper.ProductInformation;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.xml.sax.SAXException;

/*class CustomValidationEventHandler implements ValidationEventHandler {

	public boolean handleEvent(ValidationEvent evt) {
		System.out.println("Event Info: " + evt);
		if (evt.getMessage().contains("unexpected element"))
			return true;
		return false;
	}

}*/

public class SumoXPathSafeReader implements ISumoSafeReader {
	

	private File safefile;
	private Document safe;
	// use the default implementation
	XPathFactory xFactory = null;

	private ProductInformation productInformation = null;
	private OrbitInformation orbitWrapper = null;
	private AcquisitionPeriod acquPeriod=null;
	private File[] measurements=null;

	
	public SumoXPathSafeReader(File safePath) throws JDOMException, IOException,
		JAXBException {
		this.safefile=safePath;
		init();
	}

	public SumoXPathSafeReader(String safePath) throws JDOMException, IOException,
			JAXBException {
		this.safefile=new File(safePath);
		init();
	}
	
	private void init() throws JDOMException, IOException{
		SAXBuilder builder = new SAXBuilder();
		safe = builder.build(safefile);
		xFactory = XPathFactory.instance();
	}
	

	public void readXML() throws JAXBException, SAXException{
		readAcquisitionPeriod();
		readGeneralProductInformation();
		readMesasurementFilesFromSafe();
		readorbitInformation();
	}
	
	/*
	 * Return the mesaurements
	 * 
	 * @return
	 *
	public File[] getMesasurementFiles() {
		File safefile = new File(safePath);
		String measurementPath = safefile.getParent() + "//measurement";

		File[] measurementFiles = new File(measurementPath).listFiles();

		return measurementFiles;
	}*/

	public void readMesasurementFilesFromSafe() {
		XPathExpression<Element> expr = xFactory
				.compile(
						"/xfdu:XFDU/dataObjectSection/dataObject[@repID='s1Level1ProductSchema']/byteStream/fileLocation",
						Filters.element(), null, xfdu);

		List<Element> values = expr.evaluate(safe);
		this.measurements=new File[values.size()];
		
		String measurementPath = safefile.getParent() + "/measurement";

		for (int i = 0; i < values.size(); i++) {
			Element e = values.get(i);
			String href = e.getAttributeValue("href");
			if (href.startsWith("./"))
				href = href.substring(2);
			measurements[i] = new File(measurementPath + "/" + href);
			System.out.println(measurements[i]);
		}

	}

	public void readGeneralProductInformation() throws JAXBException,
			SAXException {
		String xPathGenProdInfo = "/xfdu:XFDU/metadataSection/metadataObject/metadataWrap/xmlData/*[name()='generalProductInformation']";
		String xPathStandAloneInfo = "/xfdu:XFDU/metadataSection/metadataObject/metadataWrap/xmlData/s1sarl1:standAloneProductInformation";

		XPathExpression<Element> expr = xFactory.compile(xPathGenProdInfo,
				Filters.element(), null, xfdu);
		List<Element> value = expr.evaluate(safe);
		if (value == null || value.isEmpty()) {
			expr = xFactory.compile(xPathStandAloneInfo, Filters.element(),
					null, s1sarl1, xfdu);
			value = expr.evaluate(safe);
		}

		List<Element> informationsNode = value.get(0).getChildren();
		productInformation = new ProductInformation();

		for (Element e : informationsNode) {
			String name = e.getName();
			String val = e.getValue();
			productInformation.putValueInfo(name, val);
		}

	}

	public void readAcquisitionPeriod() throws JAXBException, SAXException {
		acquPeriod = new AcquisitionPeriod();

		String xPathStartTime = "/xfdu:XFDU/metadataSection/metadataObject/metadataWrap/xmlData//*[name()='safe:startTime']";
		String xPathStopTime = "/xfdu:XFDU/metadataSection/metadataObject/metadataWrap/xmlData//*[name()='safe:stopTime']";

		XPathExpression<Element> expr = xFactory.compile(xPathStartTime,
				Filters.element(), null, xfdu, safeNs);
		Element e = expr.evaluateFirst(safe);

		if (e != null)
			acquPeriod.setStartTime(e.getValue());

		expr = xFactory.compile(xPathStopTime, Filters.element(), null, xfdu,
				safeNs);
		e = expr.evaluateFirst(safe);
		if (e != null)
			acquPeriod.setStopTime(e.getValue());
	}

	public void readorbitInformation() throws JAXBException, SAXException {
		String xPathOrbit = "/xfdu:XFDU/metadataSection/metadataObject/metadataWrap/xmlData/safe:orbitReference";
		XPathExpression<Element> expr = xFactory.compile(xPathOrbit,
				Filters.element(), null, xfdu, safeNs);
		Element orbit = expr.evaluateFirst(safe);
		orbitWrapper = new OrbitInformation();

		/*
		 * //search element in innerd document using xpath Document dd=new
		 * Document(); orbit.detach(); dd.setRootElement(orbit); String
		 * xPathOrbitNumeberStart
		 * ="/safe:orbitReference/safe:orbitNumber[@type='start']"; expr =
		 * xFactory
		 * .compile(xPathOrbitNumeberStart,Filters.element(),null,safeNs);
		 * Element orbitNumber=expr.evaluateFirst(dd); List<Element>
		 * orbitInfos=orbitNumber.getChildren();
		 */
		// read inner element without xpath
		List<Element> oNumbers = orbit.getChildren("orbitNumber",safeNs);
		if (oNumbers != null)
			for (Element e : oNumbers) {
				String type = e.getAttributeValue("type");
				if (type.equals(OrbitInformation.ORBIT_TYPE_START))
					orbitWrapper.setOrbitNumberStart(e.getValue());
				else if (type.equals(OrbitInformation.ORBIT_TYPE_STOP))
					orbitWrapper.setOrbitNumberStop(e.getValue());
			}
		List<Element> relativeONumbers = orbit
				.getChildren("relativeOrbitNumber",safeNs);
		if (relativeONumbers != null)
			for (Element e : relativeONumbers) {
				String type = e.getAttributeValue("type");
				if (type.equals(OrbitInformation.ORBIT_TYPE_START))
					orbitWrapper.setOrbitNumberStart(e.getValue());
				else if (type.equals(OrbitInformation.ORBIT_TYPE_STOP))
					orbitWrapper.setOrbitNumberStop(e.getValue());
			}

		Element cycle = orbit.getChild("cycleNumber",safeNs);
		if (cycle != null)
			orbitWrapper.setCycleNumber(cycle.getValue());
		Element phase = orbit.getChild("phaseIdentifier",safeNs);
		if (phase != null)
			orbitWrapper.setPhaseIdentifier(phase.getValue());

	}

	public ProductInformation getProductInformation() {
		return productInformation;
	}

	public void setProductInformation(ProductInformation productInformation) {
		this.productInformation = productInformation;
	}

	public AcquisitionPeriod getAcquPeriod() {
		return acquPeriod;
	}

	public void setAcquPeriod(AcquisitionPeriod acquPeriod) {
		this.acquPeriod = acquPeriod;
	}
	
	public File[] getMeasurements() {
		return measurements;
	}

	public void setMeasurements(File[] measurements) {
		this.measurements = measurements;
	}
}
