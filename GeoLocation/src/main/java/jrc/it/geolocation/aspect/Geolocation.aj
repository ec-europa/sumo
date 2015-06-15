package jrc.it.geolocation.aspect;

public aspect Geolocation {
	pointcut callPixFromGeo(): call(double[] pixelFromGeo(double,double));
	pointcut callGeoFromPix(): call(double[] geoFromPixel(double,double));
	pointcut findZero(): call(double[] findZeroDoppler(double[][] ,double[] ,double[] ));
	
	before(): callPixFromGeo(){
		
	}
	after(): callPixFromGeo(){
		
	}
}
