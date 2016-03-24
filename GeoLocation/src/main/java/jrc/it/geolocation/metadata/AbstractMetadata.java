/*
 * 
 */
package jrc.it.geolocation.metadata;

import java.util.List;

import jrc.it.geolocation.metadata.IMetadata.CoordinateConversion;
import jrc.it.geolocation.metadata.IMetadata.OrbitStatePosVelox;

public abstract class AbstractMetadata implements IMetadata{
	//for S1 always Increasing
	protected boolean pixelTimeOrderingAscending;
	protected String type="";
	
	//for S1 is rangePixelSpacing
    protected float samplePixelSpacing;
    protected float azimuthPixelSpacing;
    

	//for S1 always 0
    protected double groundRangeOrigin=0; 
    protected double numberOfSamplesPerLine=0;
    protected String antennaPointing;
    protected String mode="";
    
    protected int nLines=0;
    
    protected List<OrbitStatePosVelox> orbitStatePosVelox=null;
    protected CoordinateConversion[] coordinateConversion=null;
	protected String productType;
	
	
	
	
	public abstract String getAntennaPointing();

	public abstract void initMetaData();
	
	public abstract double getNumberOfSamplesPerLine();
	
	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type=type;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setAntennaPointing(String antennaPointing) {
		this.antennaPointing = antennaPointing;
	}

	
	public boolean isPixelTimeOrderingAscending() {
		return pixelTimeOrderingAscending;
	}

	public void setPixelTimeOrderingAscending(boolean pixelTimeOrderingAscending) {
		this.pixelTimeOrderingAscending = pixelTimeOrderingAscending;
	}

	public double getSamplePixelSpacing() {
		return samplePixelSpacing;
	}

	public void setSamplePixelSpacing(float samplePixelSpacing) {
		this.samplePixelSpacing = samplePixelSpacing;
	}
	
	public double getGroundRangeOrigin() {
		return groundRangeOrigin;
	}


	public void setGroundRangeOrigin(double groundRangeOrigin) {
		this.groundRangeOrigin = groundRangeOrigin;
	}

	public void setNumberOfSamplesPerLine(double numberOfSamplesPerLine) {
		this.numberOfSamplesPerLine = numberOfSamplesPerLine;
	}

	public float getAzimuthPixelSpacing() {
		return azimuthPixelSpacing;
	}

	public void setAzimuthPixelSpacing(float azimuthPixelSpacing) {
		this.azimuthPixelSpacing = azimuthPixelSpacing;
	}
	
	public int getNlines() {
		return nLines;
	}

	public void setnLines(int nLines) {
		this.nLines = nLines;
	}
	
	public CoordinateConversion[] getCoordinateConversion(){
		return this.coordinateConversion;
	}
	
	public List<OrbitStatePosVelox> getOrbitStatePosVelox() {
		return orbitStatePosVelox;
	}

	public void setOrbitStatePosVelox(List<OrbitStatePosVelox> orbitStatePosVelox) {
		this.orbitStatePosVelox = orbitStatePosVelox;
	}
}
