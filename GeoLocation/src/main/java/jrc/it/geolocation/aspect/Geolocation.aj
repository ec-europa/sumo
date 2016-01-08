package jrc.it.geolocation.aspect;

import java.util.List;

public aspect Geolocation {
	private long startTime=0;
	
	
	pointcut callPixFromGeo(): call(double[] pixelFromGeo(double,double));
	pointcut callGeoFromPix(): call(double[] geoFromPixel(double,double));
	pointcut findZero(): call(double[] findZeroDoppler(List<double[]> ,double[] ,double[] ));
	pointcut convol(): call(double[] linearConvolutionMatlabValid(double [],double []));
	
	before(): callPixFromGeo(){
		startTime=System.currentTimeMillis();
	}
	
	after(): callPixFromGeo(){
		long endTime=System.currentTimeMillis();
		long diff=endTime-startTime;
		if(diff>5)
			System.out.println("Long pixel from geo:"+(endTime-startTime));
	}
	
	before(): findZero(){
		startTime=System.currentTimeMillis();
	}
	
	after(): findZero(){
		long endTime=System.currentTimeMillis();
		long diff=endTime-startTime;
		if(diff>=10)
			System.out.println("Long findZeroDoppler:"+(endTime-startTime));
	}
	
	before(): convol(){
		startTime=System.currentTimeMillis();
	}
	
	after(): convol(){
		long endTime=System.currentTimeMillis();
		long diff=endTime-startTime;
		if(diff>=5)
			System.out.println("Convolution:"+(endTime-startTime));
	}
}
