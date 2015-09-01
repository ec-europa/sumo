package jrc.it.geolocation.metadata;


import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;

import jrc.it.annotation.reader.jaxb.CoordinateConversionType;
import jrc.it.annotation.reader.jaxb.DownlinkInformationType;
import jrc.it.annotation.reader.jaxb.ImageInformationType;
import jrc.it.annotation.reader.jaxb.OrbitType;
import jrc.it.xml.wrapper.SumoAnnotationReader;


public class S1Metadata extends AbstractMetadata {
	private GregorianCalendar zeroDopplerTimeFirstLineSeconds=null; //S1=productFirstLineUtcTime
	private GregorianCalendar zeroDopplerTimeLastLineSeconds=null;	//S1=productLastLineUtcTime
	private List<OrbitStatePosVelox> orbitStatePosVelox=null;
	private CoordinateConversion[] coordinateConversion=null;
	private double samplingf=0;
	private int nLines=0;
	private double linesPerBurst=0;
	private double azimuthTimeInterval=0;
	private String productType;
	
	public int getNlines() {
		return nLines;
	}

	public void setnLines(int nLines) {
		this.nLines = nLines;
	}

	public GregorianCalendar getZeroDopplerTimeFirstLineSeconds() {
		return zeroDopplerTimeFirstLineSeconds;
	}

	public void setZeroDopplerTimeFirstLineSeconds(
			GregorianCalendar zeroDopplerTimeFirstLineSeconds) {
		this.zeroDopplerTimeFirstLineSeconds = zeroDopplerTimeFirstLineSeconds;
	}

	public GregorianCalendar getZeroDopplerTimeLastLineSeconds() {
		return zeroDopplerTimeLastLineSeconds;
	}
	
	public void setZeroDopplerTimeLastLineSeconds(
			GregorianCalendar zeroDopplerTimeLastLineSeconds) {
		this.zeroDopplerTimeLastLineSeconds = zeroDopplerTimeLastLineSeconds;
	}

	public List<OrbitStatePosVelox> getOrbitStatePosVelox() {
		return orbitStatePosVelox;
	}

	public void setOrbitStatePosVelox(List<OrbitStatePosVelox> orbitStatePosVelox) {
		this.orbitStatePosVelox = orbitStatePosVelox;
	}

	
	public CoordinateConversion[] getCoordinateConversion(){
		return this.coordinateConversion;
	}
	
	
	
	public double getLinesPerBurst() {
		return linesPerBurst;
	}

	public void setLinesPerBurst(double linesPerBurst) {
		this.linesPerBurst = linesPerBurst;
	}

	public int getnLines() {
		return nLines;
	}
	
	

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public S1Metadata() {
	}
	
	/**
	 * PRF mean calculated as Sum(Prf)/LinkArraysize
	 * @return
	 */
	public double getSamplingf() {
		return samplingf;
	}


	public void setSamplingf(double samplingf) {
		this.samplingf = samplingf;
	}
	
	
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	
	
	public double getAzimuthTimeInterval() {
		return azimuthTimeInterval;
	}

	public void setAzimuthTimeInterval(double azimuthTimeInterval) {
		this.azimuthTimeInterval = azimuthTimeInterval;
	}

	
	private long getSecondInDay(GregorianCalendar c){
		long millis=c.getTimeInMillis();
		GregorianCalendar ctemp=(GregorianCalendar) c.clone();
		ctemp.set(GregorianCalendar.HOUR_OF_DAY, 0);
		ctemp.set(GregorianCalendar.MINUTE, 0);
		ctemp.set(GregorianCalendar.SECOND, 0);
		ctemp.set(GregorianCalendar.MILLISECOND, 0);
		long passed = (millis - ctemp.getTimeInMillis())/1000;
		
		return passed;
	}
	
	/**
	 * 
	 */
	public void initMetaData(String annotationFilePath){
		try {
			super.type="S1";
			super.antennaPointing="Right";
			super.setPixelTimeOrderingAscending(true);//IS K in the matlab code!!
			
			SumoAnnotationReader annotationReader=new SumoAnnotationReader(annotationFilePath);
			ImageInformationType imgInformation =annotationReader.getImageInformation();
			XMLGregorianCalendar  firstLineUtc=imgInformation.getProductFirstLineUtcTime();
			XMLGregorianCalendar  lastLineUtc=imgInformation.getProductLastLineUtcTime();
			
			this.productType=annotationReader.getHeader().getProductType().name();
			super.samplePixelSpacing=annotationReader.getImageInformation().getRangePixelSpacing().getValue();
			super.mode=annotationReader.getHeader().getMode().name();
			linesPerBurst=annotationReader.getBurstInformation().getLinePerBust();
			azimuthTimeInterval= annotationReader.getImageInformation().getAzimuthTimeInterval().getValue();
			
			zeroDopplerTimeFirstLineSeconds=firstLineUtc.toGregorianCalendar();
			zeroDopplerTimeLastLineSeconds=lastLineUtc.toGregorianCalendar();
			
			nLines=annotationReader.getImageInformation().getNumberOfLines().getValue().intValue();
			
			List<OrbitType>  orbits=annotationReader.getOrbits();
			
			orbitStatePosVelox=new ArrayList<S1Metadata.OrbitStatePosVelox>();
			
			for(OrbitType ot:orbits){
				OrbitStatePosVelox pv=new OrbitStatePosVelox();
				pv.px=ot.getPosition().getX().getValue();
				pv.py=ot.getPosition().getY().getValue();
				pv.pz=ot.getPosition().getZ().getValue();
				
				pv.vx=ot.getVelocity().getX().getValue();
				pv.vy=ot.getVelocity().getY().getValue();
				pv.vz=ot.getVelocity().getZ().getValue();
				
				GregorianCalendar gc=ot.getTime().toGregorianCalendar();
				pv.time=gc.getTimeInMillis()/1000.0;
				pv.timeStampInitSeconds=getSecondInDay(gc);//minutes*60+gc.get(GregorianCalendar.SECOND)+(gc.get(GregorianCalendar.MILLISECOND)/1000.0);
				orbitStatePosVelox.add(pv);
			}
			
			List<CoordinateConversionType> cConversion=annotationReader.getCoordinateConversionData();
			coordinateConversion=new S1Metadata.CoordinateConversion[cConversion.size()];
			int i=0; 
			for(CoordinateConversionType ccType:cConversion){
				CoordinateConversion cc=new CoordinateConversion();
				String[] vals=ccType.getGrsrCoefficients().getValue().split(" ");
				cc.groundToSlantRangeCoefficients=convertFromStringArray(vals);
				cc.groundToSlantRangeOrigin=ccType.getGr0().getValue();
				
				vals=ccType.getSrgrCoefficients().getValue().split(" ");
				cc.slantToGroundRangeCoefficients=convertFromStringArray(vals);
				cc.groundToSlantRangeOrigin=ccType.getSr0().getValue();
				coordinateConversion[i]=cc;
				XMLGregorianCalendar xmlGc=ccType.getAzimuthTime();
				cc.azimuthTime=xmlGc.toGregorianCalendar().getTimeInMillis();
				cc.groundToSlantRangePolyTimesSeconds=getSecondInDay(xmlGc.toGregorianCalendar());//minutes*60+xmlGc.getSecond()+(xmlGc.getMillisecond()/1000.0);
				i++;
			}
			//PRF and PRF mean
			//List<DownlinkInformationType> links=annotationReader.getDownLinkInformationList();
			/*for(DownlinkInformationType info:links){
				samplingf+=info.getPrf().getValue();
			}
			samplingf=samplingf/links.size();*/
			samplingf=7000/annotationReader.getImageInformation().getAzimuthPixelSpacing().getValue();
			numberOfSamplesPerLine=annotationReader.getImageInformation().getNumberOfSamples().getValue().doubleValue();
		} catch (JAXBException e) {
			e.printStackTrace();
		}	
	}



	private double[] convertFromStringArray(String[] array){
		double[] vals = new double[array.length];
		for (int i = 0; i < vals.length; i++) {
		    vals[i] = Double.parseDouble(array[i]);
		}
		return vals;
	}

	@Override
	public double getNumberOfSamplesPerLine() {
		return super.numberOfSamplesPerLine;
	}

	@Override
	public String getAntennaPointing() {
		return super.antennaPointing;
	}


	
}
