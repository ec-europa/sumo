package org.geoimage.utils;

import java.util.ArrayList;
import java.util.Scanner;

import org.geoimage.impl.Gcp;

public class GeoUtils {
	public static class Geoid{
		private double lat;
		private double lon;
		private double h;
		
		public Geoid(){}
		
		public Geoid(double lat,double lon,double h){
			this.lat=lat;
			this.lon=lon;
			this.h=h;
		}
		
		public double getLat() {
			return lat;
		}
		public void setLat(double lat) {
			this.lat = lat;
		}
		public double getLon() {
			return lon;
		}
		public void setLon(double lon) {
			this.lon = lon;
		}
		public double getH() {
			return h;
		}
		public void setH(double h) {
			this.h = h;
		}
	}

	
	private static  Geoid[] geoidPoints=null;
	
	static{
		Scanner inputStream=null;
		try{
			ArrayList<GeoUtils.Geoid> geoids=new ArrayList<GeoUtils.Geoid>();
			inputStream = new Scanner(GeoUtils.class.getClassLoader().getResourceAsStream("egm96.csv"));
			inputStream.next();
			while(inputStream.hasNext()){
                //read single line, put in string
                String data = inputStream.next();
                String vals[]=data.split(";");
                double lon=Double.parseDouble(vals[0]);
                double lat=Double.parseDouble(vals[1]);
                double h=Double.parseDouble(vals[2]);
                GeoUtils.Geoid g=new GeoUtils.Geoid(lat,lon,h);
                geoids.add(g);
            }
			inputStream.close();
			
			geoidPoints=geoids.toArray(new Geoid[0]);
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
	 * @return
	 */
	public static double  getGeoidH(double lon,double lat){
		int i=0;

		//first value
		Geoid g=geoidPoints[i];
		double minDist = distance(lon,lat,g.lon,g.lat);
	    
	    double h=g.h;
		i++;
		for(;i<geoidPoints.length&&minDist>110;i++){
			if(minDist>5000)
				i=i+25;
			g=geoidPoints[i];
			//geoidPoints
		    double dist =distance(lon,lat,g.lon,g.lat);
		    if(minDist >dist){ 
		    	minDist=dist;
		    	h=g.h;
		    }	
		    
		}
		return h;
		
	}	
	
	/**
	 * 
	 * @param lon1
	 * @param lat1
	 * @param lon2
	 * @param lat2
	 * @return
	 */
	public static double distance(double lon1, double lat1, double lon2, double lat2){
		double R=6372.795477598;
		
		lon1=(lon1*Math.PI)/180;
		lon2=(lon2*Math.PI)/180;
		lat1=(lat1*Math.PI)/180;
		lat2=(lat2*Math.PI)/180;
		
	    
		double dlon = Math.abs(lon1 - lon2);
		double p=Math.acos(Math.sin(lat2)*Math.sin(lat1)+Math.cos(lat2) * Math.cos(lat1) * Math.cos(dlon));
		double d = R * p ;
		
		return d;
	}
	
	
	/**
	 * 
	 * @param latitude
	 * @param ellipseMin 
	 * @param ellipseMaj
	 * @return
	 */
	public static double earthRadiusFromLatitude(double latitude, double ellipseMin,double ellipseMaj ){
		double d=Math.pow(Math.tan(latitude*Math.PI/180),2);
		double reEarth=ellipseMin*Math.sqrt((1+d)/(Math.pow((ellipseMin/ellipseMaj),2)+d));
		
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
	public static double gcpSlantRangeAndIncidenceAngleForComplex(double slantRangeNearEdge,double sizeXPixel,double samplePixelSpacing,Gcp gpc,double hsat,double earthRad,String timeOrdering){
		double slantAndIA=0;
		
		//calculate the slant range for the gpc
		double slantRangeFarGpc=slantRangeNearEdge+sizeXPixel*samplePixelSpacing;

		double slntRangePixel=0;
		if(timeOrdering.equalsIgnoreCase("Increasing")){		
			//calculate the gnd range for the gpc
			slntRangePixel=slantRangeFarGpc+gpc.getXpix()*samplePixelSpacing;
		}else{
			slntRangePixel=slantRangeFarGpc-gpc.getXpix()*samplePixelSpacing;
		}
		
		//incidence angle
		slantAndIA=Math.acos(hsat*(1+hsat/(2*earthRad))/slntRangePixel-slntRangePixel/(2*earthRad));
		
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
	public static double gcpIncidenceAngleForGRD(double slntRangeInNearRange,double sizeXPixel,double samplePixelSpacing,Gcp gpc,double hsat,double earthRad,String timeOrdering){
		double srAndIA=0;
		

		//convert slant range into grnd range 
		double gndRangeAtNearRange=earthRad*Math.acos(1-(slntRangeInNearRange*slntRangeInNearRange-hsat*hsat)/(2*earthRad*(earthRad+hsat))  );
		double gndRangePixel=0;
		
		double gndRangeAtFarRange=gndRangeAtNearRange+sizeXPixel*samplePixelSpacing;
		
		if(timeOrdering.equalsIgnoreCase("Increasing")){		
			//calculate the gnd range for the gpc
			gndRangePixel=gndRangeAtNearRange+gpc.getXpix()*samplePixelSpacing;
		}else{
			gndRangePixel=gndRangeAtFarRange-gpc.getXpix()*samplePixelSpacing;
		}
		//slant range
		double finalSlantRange=Math.sqrt(hsat*hsat + (2*earthRad*(earthRad+hsat))*(1-Math.cos(gndRangePixel/earthRad)));

		//incidence angle
		srAndIA=Math.acos((hsat*(1+hsat/(2*earthRad))/finalSlantRange) - (finalSlantRange/(2*earthRad)));			
		
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
		
		double dx=Math.cos(incidenceAngle)*(dHeight-geoidHeight);
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
		
		double dx=(height-geoidHeight)/Math.tan(incidenceAngle);
		return dx;
	}

	
	public static double addMetersToLongitude(double longitude,double latitude, double dx,double rHeart){
		double k=0.000009;
		double l=dx*k;
		//longitude=	longitude + (dx / rHeart) * (180 / Math.PI) / Math.cos(latitude * Math.PI/180);
		
		return longitude+l; 
	}
	
	
	public static void main(String[] args){
		GeoUtils.getGeoidH(6.027,43.155);
		//GeoUtils.distance(6.027,43.155,-8.625,-27.5);
		
		
		/*<referenceEllipsoidParameters>
		<ellipsoidName>WGS84</ellipsoidName>
		<semiMajorAxis units="m">6.378137000000000e+06</semiMajorAxis>
		<semiMinorAxis units="m">6.356752314245179e+06</semiMinorAxis>
		<datumShiftParameters units="m">0.000000000000000e+00 0.000000000000000e+00 0.000000000000000e+00</datumShiftParameters>
		<geodeticTerrainHeight units="m">2.457402038574219e+01</geodeticTerrainHeight>
	</referenceEllipsoidParameters>*/
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
		
		Gcp gcp=new Gcp();
		gcp.setXgeo(3.452459651443478);
		gcp.setYgeo(7.917209269527878);
		gcp.setZgeo(2.457402038574219e+01);
		gcp.setXpix(3.60272095e+03);//pixel
		gcp.setYpix(2.86479004e+04);//line
		
		//<sampledPixelSpacing units="m">1.18326979e+01</sampledPixelSpacing>
		double sampledPixelSpacing=1.18326979e+01;
		
		/*<incidenceAngleNearRange units="deg">3.07271442e+01</incidenceAngleNearRange>
			<incidenceAngleFarRange units="deg">3.95531425e+01</incidenceAngleFarRange>
			<slantRangeNearEdge units="m">9.066664660259531e+05</slantRangeNearEdge>
			<satelliteHeight units="m">7.943865625000000e+05</satelliteHeight>*/
		
		double slantRangeNearEdge=9.066664660259531e+05;
		double satelliteHeight=7.943865625000000e+05;
		
		//double ia=GeoUtils.gcpSlantRangeAndIncidenceAngleForComplex(slantRangeNearEdge,sampledPixelSpacing,gcp,satelliteHeight,eartR);
		System.out.println(eartR);
		//System.out.println(ia);
		
		//<geodeticTerrainHeight units="m">2.457402038574219e+01</geodeticTerrainHeight>
	//	double x=gcpComplexGeoCorrectionMeters(ia,2.457402038574219e+01,gcp);
		
	//	System.out.println(gcp.getXgeo());
	//	System.out.println(x);
	}
	
}
