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
	private List<CoordinateConversion> coordinateConversion=null;
	private double samplingf=0;
	private int nLines=0;
	private double linesPerBurst=0;
	private double azimuthTimeInterval=0;
	
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

	
	public List<CoordinateConversion> getCoordinateConversion(){
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

	/**
	 * 
	 */
	public void initMetaData(String annotationFilePath){
		try {
			super.type="S1";
			super.antennaPointing="Right";
			
			
			SumoAnnotationReader annotationReader=new SumoAnnotationReader(annotationFilePath);
			ImageInformationType imgInformation =annotationReader.getImageInformation();
			XMLGregorianCalendar  firstLineUtc=imgInformation.getProductFirstLineUtcTime();
			XMLGregorianCalendar  lastLineUtc=imgInformation.getProductLastLineUtcTime();

			if(annotationReader.getProductInformation().getPass().equalsIgnoreCase("ascending")){
				super.setPixelTimeOrderingAscending(true);
			}else{
				super.setPixelTimeOrderingAscending(false);
			}
				
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
				
				pv.timeStampInitSeconds=gc.get(GregorianCalendar.MINUTE)*60+gc.get(GregorianCalendar.SECOND)+(gc.get(GregorianCalendar.MILLISECOND)/1000.0);
				orbitStatePosVelox.add(pv);
			}
			
			List<CoordinateConversionType> cConversion=annotationReader.getCoordinateConversionData();
			coordinateConversion=new ArrayList<S1Metadata.CoordinateConversion>();
			for(CoordinateConversionType ccType:cConversion){
				CoordinateConversion cc=new CoordinateConversion();
				String[] vals=ccType.getGrsrCoefficients().getValue().split(" ");
				cc.groundToSlantRangeCoefficients=convertFromStringArray(vals);
				cc.groundToSlantRangeOrigin=ccType.getGr0().getValue();
				
				vals=ccType.getSrgrCoefficients().getValue().split(" ");
				cc.slantToGroundRangeCoefficients=convertFromStringArray(vals);
				cc.groundToSlantRangeOrigin=ccType.getSr0().getValue();
				coordinateConversion.add(cc);
				XMLGregorianCalendar xmlGc=ccType.getAzimuthTime();
				cc.azimuthTime=xmlGc.toGregorianCalendar().getTimeInMillis();
				cc.groundToSlantRangePolyTimesSeconds=xmlGc.getMinute()*60+xmlGc.getSecond()+(xmlGc.getMillisecond()/1000.0);
			}
			
			//PRF and PRF mean
			List<DownlinkInformationType> links=annotationReader.getDownLinkInformationList();
			for(DownlinkInformationType info:links){
				samplingf+=info.getPrf().getValue();
			}
			samplingf=samplingf/links.size();
			
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
