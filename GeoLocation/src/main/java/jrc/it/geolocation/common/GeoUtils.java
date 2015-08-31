package jrc.it.geolocation.common;

import java.util.Scanner;

import org.apache.commons.math3.util.FastMath;

/**
 * 
 * @author Pietro Argentieri
 *
 */
public class GeoUtils {
	// Reference ellipsoid axes
	public final static double semiMajorAxis = 6.378137000000000e+06; 		//KeywVal.semiMajorAxis;
	public final static double semiMinorAxis = 6.356752314245179e+06; 		//KeywVal.semiMinorAxis;
	public final static double semiMajorAxis2=semiMajorAxis*semiMajorAxis;
	public final static double semiMinorAxis2=semiMinorAxis*semiMinorAxis;
	public final static double cc = 299792458; //Speed of light
	public final static double R_HEART=6372.795477598;
	public final static double GRS80_EARTH_MU=3.986005e14;//	Earth gravitational constant from GRS80 model: 3.986005e14 m³/s².
	
	/**
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 */
	public static double[] convertFromGeoToEarthCentred(final double lat, final double lon){
		double pH=GeoUtils.getGeoidH(lon, lat);
		
		double radLat=FastMath.toRadians(lat);
		double radLon=FastMath.toRadians(lon);
		
		// Convert from geographic coordinates to Earth centred Earth fixed coordinates
		double cosLat=FastMath.cos(radLat);
		double sinLat = FastMath.sin(radLat);
		double cosLon =FastMath.cos(radLon);
		double sinLon = FastMath.sin(radLon);
		
		double denomTmp = FastMath.sqrt((semiMajorAxis2) *(cosLat*cosLat) + (semiMinorAxis2)*(sinLat*sinLat));
		
		double pX = (semiMajorAxis2/denomTmp + pH) * cosLat * cosLon;
		double pY = (semiMajorAxis2/denomTmp + pH) * cosLat * sinLon;
		double pZ = (semiMinorAxis2/denomTmp + pH) * sinLat;
		
		double[] pXYZ = {pX ,pY, pZ};
		//double[] pXYZ = {4740162.032532615 , 787287.082659188, 4180253.542739194};
		return pXYZ;
	} 
		
	
	/**
	 * 
	 * @author argenpo
	 *
	 */
	public final static class Geoid{
		private double lat=0;
		private double lon=0;
		private double latRad=0;
		private double lonRad=0;
		private double h=0;
		
		public Geoid(){}
		
		public Geoid(double lat,double lon,double h){
			this.lat=lat;
			this.lon=lon;
			this.latRad=lat*FastMath.PI/180;
			this.lonRad=lon*FastMath.PI/180;
			this.h=h;
		}
		
		/**
		 * 
		 * @return
		 */
		public double getLat() {
			return lat;
		}
		/**
		 * 
		 * @param lat
		 */
		public void setLat(double lat) {
			this.lat = lat;
		}
		/**
		 * 
		 * @return
		 */
		public double getLon() {
			return lon;
		}
		/**
		 * 
		 * @param lon
		 */
		public void setLon(double lon) {
			this.lon = lon;
		}
		/**
		 * 
		 * @return
		 */
		public double getLatRad() {
			return latRad;
		}
		/**
		 * 
		 * @param latRad
		 */
		public void setLatRad(double latRad) {
			this.latRad = latRad;
		}
		/**
		 * 
		 * @return
		 */
		public double getLonRad() {
			return lonRad;
		}
		/**
		 * 
		 * @param lonRad
		 */
		public void setLonRad(double lonRad) {
			this.lonRad = lonRad;
		}
		/**
		 * 
		 * @return
		 */
		public double getH() {
			return h;
		}
		/**
		 * 
		 * @param h
		 */
		public void setH(double h) {
			this.h = h;
		}
	}

	
	private static  Geoid[][] geoidPoints=new Geoid[360][180];

	/**
	 * this part of code load the file that contains the geoid height 
	 */
	static{
		Scanner inputStream=null;
		try{
			//ArrayList<GeoUtils.Geoid> geoids=new ArrayList<GeoUtils.Geoid>();
			Geoid[] points=new Geoid[180];
			inputStream = new Scanner(GeoUtils.class.getClassLoader().getResourceAsStream("egm96.csv"));
			inputStream.next();
			int col=0;
			int row=0;
			while(inputStream.hasNext()){
                //read single line, put in string
                String data = inputStream.next();
                String vals[]=data.split(";");
                double lon=Double.parseDouble(vals[0]);
                double lat=Double.parseDouble(vals[1]);
                double h=Double.parseDouble(vals[2]);
                GeoUtils.Geoid g=new GeoUtils.Geoid(lat,lon,h);
                points[col]=g;
                col++;
                if(col==180){
                	geoidPoints[row]=points;//geoids.toArray(new Geoid[0]);
                	points=new Geoid[180];
                	col=0;
                	row++;
                }
            }
			inputStream.close();
		}catch(Exception e){
			e.printStackTrace();
			if(inputStream!=null)
				inputStream.close();
		}
	}
	/**
	 * 
	 * @param lon
	 * @param lat
	 * @return the geoid Height for a point (lon,lat)
	 */
	public static double  getGeoidH(double lon,double lat){
	    double h=0;
	    double minDist =1000;
		//first value
		for(int i=0;i<geoidPoints.length&&minDist>50;i++){
			Geoid g=geoidPoints[i][0];
			minDist = distance(lon,lat,g.lon,g.lat);
		    h=g.h;
			for(int j=1;j<geoidPoints[i].length;j++){
				//i=i+(new Double(minDist).intValue()/115);
				g=geoidPoints[i][j];
				//geoidPoints
			    double dist =distance(lon,lat,g.lon,g.lat);
			    if(minDist >dist){ 
			    	minDist=dist;
			    	h=g.h;
			    }
			}   
		}
		//System.out.println("-->H:"+h);
		return h;
		
	}
	
	
	/**
	 * search a point with distance<50 from the (lon,lat) parameter and return the geoid height  
	 * 
	 * @param lon
	 * @param lat
	 * @return the geoid Height for a point (lon,lat)
	 */
	public static double  getGeoidHX(final double lon,final double lat){
		double h=0;
		
		double lonRad=lon*FastMath.PI/180;
		double latRad=lat*FastMath.PI/180;
		
		boolean finded=false;
		
		for(int row=0;row<geoidPoints.length&&!finded;row++){
			int middle=geoidPoints[0].length/2;
			Geoid geoRow[]=geoidPoints[row];
			//first value
			Geoid g=geoRow[middle];
			double middleDist = distance(lonRad,latRad,g.lonRad,g.latRad);
		    if(middleDist<50){
		    	h= g.h;
		    }else{
		    	int left=0;
				int right=geoidPoints[0].length;
				
				for(;right-left>1;){
					int middleLeft=(left+middle)/2;
					int middleRight=middle+(right-middle)/2;
					
					double middleDistL = distance(lonRad,latRad,geoRow[middleLeft].lonRad,geoRow[middleLeft].latRad);
					double middleDistR = distance(lonRad,latRad,geoRow[middleRight].lonRad,geoRow[middleRight].latRad);
					if(middleDistL<middleDistR){
						right=middle;
						middle=left+(right-left)/2;
						middleDist=middleDistL;
					}else{
						left=middle;
						middle=left+(right-left)/2;
						middleDist=middleDistR;
					}
					if(middleDist<50){
				    	h=geoRow[middle].h;
				    	finded=true;
				    	break;
				    }
					
					
				}
		    }
		}    
	    //System.out.println("H:"+h);
		return h;
		
	}
	
	/**
	 * 
	 * @param lon1
	 * @param lat1
	 * @param lon2
	 * @param lat2 
	 * @return calculate the distance between 2 points
	 */
	public static double distance(double lon1, double lat1, double lon2, double lat2){
		lon1=(lon1*FastMath.PI)/180;
		lat1=(lat1*FastMath.PI)/180;
		
		lon2=(lon2*FastMath.PI)/180;
		lat2=(lat2*FastMath.PI)/180;
	    
		return distanceRad(lon1,lat1,lon2,lat2);
		
	}
	
	/**
	 * 
	 * @param lon1 in radians
	 * @param lat1 in radians
	 * @param lon2 in radians
	 * @param lat2 in radians
	 * @return calculate the distance between 2 points
	 */
	public static double distanceRad(double lonRad1, double latRad1, double lonRad2, double latRad2){
		
		
		double dlon = FastMath.abs(lonRad1 - lonRad2);
		double p=FastMath.acos(FastMath.sin(latRad2)*FastMath.sin(latRad1)+FastMath.cos(latRad2) * FastMath.cos(latRad1) * FastMath.cos(dlon));
		double d = R_HEART * p ;
		
		return d;
	}
	
	
	/**
	 * 
	 * @param latitude
	 * @param ellipseMin 
	 * @param ellipseMaj
	 * @return calculate the heart radius for a latitude
	 */
	public static double earthRadiusFromLatitude(double latitude, double ellipseMin,double ellipseMaj ){
		double tan=FastMath.tan(latitude*FastMath.PI/180);
		double ellipseRapp=(ellipseMin/ellipseMaj)*(ellipseMin/ellipseMaj);
		double d=tan*tan;//FastMath.pow(FastMath.tan(latitude*FastMath.PI/180),2);
		double reEarth=ellipseMin*FastMath.sqrt((1+d)/((ellipseRapp*ellipseRapp)+d));
		
		return reEarth;
		
	}
	
	
	/**
	 *			 ( Parameters have the names used in radarsat2 metadata )
	 *
	 * @param complex
	 * @param slantRangenearEdge 
	 * @param samplePixelSpacing
	 * @param ngrd
	 * @param nsam
	 * @return
	 */
	public static double gcpSlantRangeAndIncidenceAngleForComplex(double slantRangeNearEdge,double sizeXPixel,double samplePixelSpacing,double xPix,double hsat,double earthRad,String timeOrdering){
		double slantAndIA=0;
		
		//calculate the slant range for the gpc
		double slantRangeFarGpc=slantRangeNearEdge+sizeXPixel*samplePixelSpacing;

		double slntRangePixel=0;
		if(timeOrdering.equalsIgnoreCase("Increasing")){		
			//calculate the gnd range for the gpc
			slntRangePixel=slantRangeFarGpc+xPix*samplePixelSpacing;
		}else{
			slntRangePixel=slantRangeFarGpc-xPix*samplePixelSpacing;
		}
		
		//incidence angle
		slantAndIA=FastMath.acos(hsat*(1+hsat/(2*earthRad))/slntRangePixel-slntRangePixel/(2*earthRad));
		
		return slantAndIA;
	}
	
	/**
	 *			 ( Parameters have the names used in radarsat2 metadata )
	 *
	 * @param complex
	 * @param gndRange 
	 * @param samplePixelSpacing
	 * @param ngrd
	 * @param nsam
	 * @return
	 */
	public static double gcpIncidenceAngleForGRD(double slntRangeInNearRange,double sizeXPixel,double samplePixelSpacing,double xPix,double hsat,double earthRad,String timeOrdering){
		double srAndIA=0;
		//convert slant range into grnd range 
		double gndRangeAtNearRange=earthRad*FastMath.acos(1-(slntRangeInNearRange*slntRangeInNearRange-hsat*hsat)/(2*earthRad*(earthRad+hsat))  );
		double gndRangePixel=0;
		
		double gndRangeAtFarRange=gndRangeAtNearRange+sizeXPixel*samplePixelSpacing;
		
		if(timeOrdering.equalsIgnoreCase("Increasing")){		
			//calculate the gnd range for the gpc
			gndRangePixel=gndRangeAtNearRange+xPix*samplePixelSpacing;
		}else{
			gndRangePixel=gndRangeAtFarRange-xPix*samplePixelSpacing;
		}
		//slant range
		double finalSlantRange=FastMath.sqrt(hsat*hsat + (2*earthRad*(earthRad+hsat))*(1-FastMath.cos(gndRangePixel/earthRad)));

		//incidence angle
		srAndIA=FastMath.acos((hsat*(1+hsat/(2*earthRad))/finalSlantRange) - (finalSlantRange/(2*earthRad)));			
		
		//double conv=srAndIA*180/Math.PI;
		//System.out.println(conv);
		return srAndIA;
		
	}
	
	/**
	 * 
	 * @param incidenceAngle
	 * @param dheight geoid height-geodeticTerrainHeight
	 * @param gcp
	 * @return	horizontal shift
	 */
	public static double gcpComplexGeoCorrectionMeters(double incidenceAngle,double dHeight,double geoidHeight){
		double dx=FastMath.cos(incidenceAngle)*(dHeight-geoidHeight);
		return dx;
	}
	
	
	/**
	 * 
	 * @param incidenceAngle
	 * @param dheight geoid height-geodeticTerrainHeight
	 * @param gcp
	 * @return	horizontal shift
	 */
	public static double gcpGrdGeoCorrectionMeters(double incidenceAngle,double height,double geoidHeight){
		
		double dx=(height-geoidHeight)/FastMath.tan(incidenceAngle);
		return dx;
	}

	/**
	 * 
	 * @param longitude
	 * @param latitude
	 * @param dx
	 * @param rHeart
	 * @return add meters to a longitude and return the new longitude
	 */
	public static double addMetersToLongitude(double longitude,double latitude, double dx,double rHeart){
		double k=0.000009;
		double l=dx*k;
		//longitude=	longitude + (dx / rHeart) * (180 / Math.PI) / Math.cos(latitude * Math.PI/180);
		
		return longitude+l; 
	}
	
	
	public static void main(String[] args){
		System.out.println("H:"+GeoUtils.getGeoidH(2.17235,41.31749));
		//GeoUtils.distance(6.027,43.155,-8.625,-27.5);
		
	}	
		/*<referenceEllipsoidParameters>
		<ellipsoidName>WGS84</ellipsoidName>
		<semiMajorAxis units="m">6.378137000000000e+06</semiMajorAxis>
		<semiMinorAxis units="m">6.356752314245179e+06</semiMinorAxis>
		<datumShiftParameters units="m">0.000000000000000e+00 0.000000000000000e+00 0.000000000000000e+00</datumShiftParameters>
		<geodeticTerrainHeight units="m">2.457402038574219e+01</geodeticTerrainHeight>
	</referenceEllipsoidParameters>*/
	/*
		double eartR=GeoUtils.earthRadiusFromLatitude(3.452459651443478, 6.378137000000000e+06, 6.356752314245179e+06);
		
		/*<imageTiePoint>
					<imageCoordinate>
						<line>2.86479004e+04</line>
						<pixel>3.60272095e+03</pixel>
					</imageCoordinate>
					<geodeticCoordinate>
						<latitude units="deg">3.452459651443478e+00</latitude>
						<longitude units="deg">7.917209269527878e+00</longitude>
						<height units="m">2.457402038574219e+01</height>
					</geodeticCoordinate>
				</imageTiePoint>*/
/*		
		Gcp gcp=new Gcp();
		gcp.setXgeo(3.452459651443478);
		gcp.setYgeo(7.917209269527878);
		gcp.setZgeo(2.457402038574219e+01);
		gcp.setXpix(3.60272095e+03);//pixel
		gcp.setYpix(2.86479004e+04);//line
		
		//<sampledPixelSpacing units="m">1.18326979e+01</sampledPixelSpacing>
		double sampledPixelSpacing=1.18326979e+01;
		
		
		double slantRangeNearEdge=9.066664660259531e+05;
		double satelliteHeight=7.943865625000000e+05;
		
		//double ia=GeoUtils.gcpSlantRangeAndIncidenceAngleForComplex(slantRangeNearEdge,sampledPixelSpacing,gcp,satelliteHeight,eartR);
		System.out.println(eartR);
		//System.out.println(ia);
		
		//<geodeticTerrainHeight units="m">2.457402038574219e+01</geodeticTerrainHeight>
	//	double x=gcpComplexGeoCorrectionMeters(ia,2.457402038574219e+01,gcp);
		
	//	System.out.println(gcp.getXgeo());
	//	System.out.println(x);
	}*/
	
}
