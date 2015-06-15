package jrc.it.geolocation.aspect;

import java.util.List;

import jrc.it.geolocation.exception.MathException;
import jrc.it.geolocation.metadata.S1Metadata;

public aspect HermiteAspect {
	private long startTime=0;
	
	pointcut inter() : execution (* interpolation(double[], 
			List<S1Metadata.OrbitStatePosVelox>,Double[],int,int,
			double,List<double[]>,List<double[]>,List<Double>)throws MathException);

	
	
	pointcut invert(): call (* invertMatrix(double[][]));
	
	
	
	
	before(): inter(){
		startTime=System.currentTimeMillis();
	}
	after(): inter(){
		long endTime=System.currentTimeMillis();
		System.out.println("Hermite Interpolation:"+(endTime-startTime));
	}
	
	
	before(): invert(){
		startTime=System.currentTimeMillis();
	}
	after(): invert(){
		long endTime=System.currentTimeMillis();
		System.out.println("Invert Matrix:"+(endTime-startTime));
	}
}
