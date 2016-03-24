/*
 * 
 */
package jrc.it.geolocation.metadata;

public interface IMetadata {
	public class OrbitStatePosVelox{
		public double px=0;
		public double py=0;
		public double pz=0;
		public double vx=0;
		public double vy=0;
		public double vz=0;
		public double time=0;
		public double timeStampInitSeconds=0;
	};
	
	public class CoordinateConversion{
		public double[] groundToSlantRangeCoefficients=null;
		public double[] slantToGroundRangeCoefficients=null;
		public double groundToSlantRangeOrigin=0;
		public double slantToGroundRangeOrigin=0;
		public long azimuthTime;
		public double groundToSlantRangePolyTimesSeconds;
	};
	
	public abstract void initMetaData();

	public abstract String getType();

	public abstract void setType(String type);

	public boolean isPixelTimeOrderingAscending() ;

	public void setPixelTimeOrderingAscending(boolean pixelTimeOrderingAscending);
	
	public double getNumberOfSamplesPerLine();

	public String getAntennaPointing();
}