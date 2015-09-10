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
	private int maxValue;
	private double band;
	private double id;
	private boolean isAmbiguity=false;
	private int ambiguityType=0;
	
	private BoatStatisticMapPolarization statMap;

	public BoatStatisticMapPolarization getStatMap() {
		return statMap;
	}

	public void setStatMap(BoatStatisticMapPolarization statMap) {
		this.statMap = statMap;
	}

	public Boat(double id,double x,double y,double size,double length,double width,double heading){
		this.id=id;
		this.posx=x;
		this.posy=y;
		this.size=size;
		this.length=length;
		this.width=width;
		this.heading=heading;
		
		statMap=new BoatStatisticMapPolarization();
	}
	
	public Boat(double id,double x,double y,double size,double length,double width,double heading,
			int value,double tileAvg,double tileStd,double threshold, int band,String pol){
		
		this.id=id;
		this.posx=x;
		this.posy=y;
		this.size=size;
		this.length=length;
		this.width=width;
		this.heading=heading;
		
		statMap=new BoatStatisticMapPolarization();
		statMap.setMaxValue(value,pol);
		statMap.setTileAvg(tileAvg,pol);
		statMap.setTileStd(tileStd,pol);
		statMap.setTreshold(threshold,pol);
        this.band = band;
	}
	
	

	public double getId() {
		return id;
	}



	public void setId(double id) {
		this.id = id;
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
	
	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}
	
	public boolean isAmbiguity() {
		return isAmbiguity;
	}

	public void setAmbiguity(boolean isAmbiguity) {
		this.isAmbiguity = isAmbiguity;
	}

	public int[] getAllMaxValue(){
		return this.statMap.getAllMaxValue();
	}
	public double[] getAllTileAvg(){
		return this.statMap.getAllTileAvg();
	}
	public double[] getAllTrhesh(){
		return this.statMap.getAllTrhesh();
	}
	public double[] getAllTileStd(){
		return this.statMap.getAllTileStd();
	}
	public double[] getAllSignificance(){
		return this.statMap.getAllSignificance();
	}
	
	/*public void setTileAvg(String pol,double value){
		this.statMap.setTileAvg(value, pol);
	}
	public void setTresh(String pol,double value){
		this.statMap.setTreshold(value, pol);
	}
	public void setTileStd(String pol,double value){
		this.statMap.setTileStd(value, pol);
	}
	public void setSignificance(String pol,double value){
		this.statMap.setSignificance(value, pol);
	}
	public void setMaxVal(String pol,int value){
		this.statMap.setMaxValue(value, pol);
	}
	*/
	
	
	
	
}
