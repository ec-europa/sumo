package jrc.it.geolocation.aspect;

import java.util.List;

public aspect Geolocation {
	private long startTime=0;
	
	
	pointcut callPixFromGeo(): call(double[] pixelFromGeo(double,double));
	pointcut callGeoFromPix(): call(double[] geoFromPixel(double,double));
	pointcut findZero(): call(double[] findZeroDoppler(List<double[]> ,double[] ,double[] ));
	
	before(): callPixFromGeo(){
		startTime=System.currentTimeMillis();
	}
	
	after(): callPixFromGeo(){
		long endTime=System.currentTimeMillis();
		System.out.println("pixel from geo:"+(endTime-startTime));
	}
	
	before(): findZero(){
		startTime=System.currentTimeMillis();
	}
	
	after(): findZero(){
		long endTime=System.currentTimeMillis();
		System.out.println("findZeroDoppler:"+(endTime-startTime));
	}
}
