package org.geoimage.analysis;

public class Boat{
	public final static int AMBIGUITY_TYPE_AZIMUTH=1;
	public final static int AMBIGUITY_TYPE_ARTEFACTS=2;
	
	protected double posx;
	protected double posy;
	private double size;
	private double length;
	private double width;
	private double heading;
	private int[] maxValue;
	private double[] tileAvg={0.0,0.0,0.0,0.0};
	private double[] tileStd={0.0,0.0,0.0,0.0};
	private double[] threshold={0.0,0.0,0.0,0.0};
	private double[] significance={0.0,0.0,0.0,0.0};
	private double[] meanValue={0.0,0.0,0.0,0.0};


	private double band;
	private double id;
	private boolean isAmbiguity=false;
	private int ambiguityType=0;
	

	public Boat(double id,double x,double y,double size,double length,double width,double heading){
		this.id=id;
		this.posx=x;
		this.posy=y;
		this.size=size;
		this.length=length;
		this.width=width;
		this.heading=heading;
	}
	
	public Boat(double id,double x,double y,double size,double length,double width,double heading,
			int value[],double tileAvg,double tileStd,double threshold, int band){
		this.id=id;
		this.posx=x;
		this.posy=y;
		this.size=size;
		this.length=length;
		this.width=width;
		this.heading=heading;
		this.maxValue = value;
        this.tileAvg[band] = tileAvg;
        this.tileStd[band]=tileStd;
        this.tileStd[band]=threshold;
        this.band = band;
	}
	

	public double getId() {
		return id;
	}



	public void setId(double id) {
		this.id = id;
	}



	public int[] getMaxValue() {
		return maxValue;
	}



	public void setMaxValue(int[] value) {
		this.maxValue = value;
	}



	public double[] getTileAvg() {
		return tileAvg;
	}



	public void setTileAvg(double tileAvg[]) {
		this.tileAvg = tileAvg;
	}



	public double[] getTileStd() {
		return tileStd;
	}



	public void setTileStd(double[] tileStd) {
		this.tileStd = tileStd;
	}



	public double[] getThreshold() {
		return threshold;
	}



	public void setThreshold(double[] threshold) {
		this.threshold = threshold;
	}

	public double[] getSignificance() {
		return significance;
	}

	public void setSignificance(double[] significance) {
		this.significance = significance;
	}

	public double getBand() {
		return band;
	}



	public void setBand(double band) {
		this.band = band;
	}

	public int getAmbiguityType() {
		return ambiguityType;
	}

	public void setAmbiguityType(int ambiguityType) {
		this.ambiguityType = ambiguityType;
	}

	public double getPosx() {
		return posx;
	}

	public void setPosx(double posx) {
		this.posx = posx;
	}

	public double getPosy() {
		return posy;
	}

	public void setPosy(double posy) {
		this.posy = posy;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading;
	}
	
	public boolean isAmbiguity() {
		return isAmbiguity;
	}



	public void setAmbiguity(boolean isAmbiguity) {
		this.isAmbiguity = isAmbiguity;
	}


	public double[] getMeanValue() {
		return meanValue;
	}

	public void setMeanValue(double[] meanValue) {
		this.meanValue = meanValue;
	}

}
