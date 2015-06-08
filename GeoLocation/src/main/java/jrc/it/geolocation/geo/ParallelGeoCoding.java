package jrc.it.geolocation.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import jrc.it.geolocation.exception.MathException;

import com.vividsolutions.jts.geom.Coordinate;

public class ParallelGeoCoding {
	
	private GeoCoding geoCoding;
	
	
	public ParallelGeoCoding(GeoCoding geoc){
		this.geoCoding=geoc;
	}

	class ParallelReverse implements Callable<double[]>{
		private double lat;
		private double lon;
		
		ParallelReverse(double lat,double lon){
			this.lat=lat;
			this.lon=lon;
		}
		
		@Override
		public double[] call() throws Exception {
			return geoCoding.pixelFromGeo(lat,lon);
		}
	}
	
	class ParallelForward implements Callable<double[]>{
		private double l=0;
		private double p=0;
		
		
		ParallelForward(double l,double p){
			this.l=l;
			this.p=p;
		}
		
		@Override
		public double[] call() throws Exception {
			return geoCoding.geoFromPixel(l, p);
		}
	}
	
	public List<double[]>parallelPixelFromGeo(Coordinate[] coords) throws InterruptedException, ExecutionException {
		ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(4);
		try{
			List<Callable<double[]>> tasks=new ArrayList<Callable<double[]>>();
			for(Coordinate c:coords){
				tasks.add(new ParallelReverse(c.x,c.y));
			}	
			List <Future<double[]>> results = executor.invokeAll(tasks);
			executor.shutdown();
			/*
			List<Future<double[]>> tasks=new ArrayList<Future<double[]>>();
			for(Coordinate c:coords){
				tasks.add(executor.submit(new ParallelReverse(c.x,c.y)));
			}	
			//List <Future<double[]>> results = executor.invokeAll(tasks);
			executor.shutdown();
			*/
        
	        List<double[]> points=new ArrayList<double[]>();
	        for (Future<double[]> result : results) {
	        	List<double[]> l=Arrays.asList(result.get());
	            points.addAll(l);
	        }               
	        return points;
		}catch(Exception e){
			if(!executor.isShutdown())
				executor.shutdown();
			throw e;
		}
       
    }    
	
	public List<double[]>parallelForward(Coordinate[] coords) throws InterruptedException, ExecutionException  {
		ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(3);

		List<Callable<double[]>> tasks=new ArrayList<Callable<double[]>>();
		for(Coordinate c:coords){
			tasks.add(new ParallelForward(c.x,c.y));
		}	
		List <Future<double[]>> results = executor.invokeAll(tasks);
		executor.shutdown();
        
        List<double[]> points=new ArrayList<double[]>();
        for (Future<double[]> result : results) {
        	List<double[]> l=Arrays.asList(result.get());
            points.addAll(l);
        }               
        
        return points;
    }
	
	
	public static void main(String[]args){
		String metaF="H://Radar-Images//S1Med//S1//EW//S1A_EW_GRDH_1SDV_20141020T055155_20141020T055259_002909_0034C1_F8D5.SAFE//annotation//s1a-ew-grd-vv-20141020t055155-20141020t055259-002909-0034c1-001.xml";
		
		GeoCoding gc;
		try {
			gc = new S1GeoCodingImpl(metaF);
			double lat = 41.31735;//43.13935;//42.81202;
			double lon = 2.17263;//3.35876;//10.32972;
			Coordinate c=new Coordinate(lat,lon);
			/*List cc=new ArrayList<>();
			cc.add(c);
			double r[];
			try {
				ParallelGeoCoding p=new ParallelGeoCoding(gc);
				List results=p.parallelReverse(cc);
				System.out.println("-->"+results.get(0));
		    } catch (Exception e) {
				e.printStackTrace();
			}*/
		} catch (MathException e1) {
			e1.printStackTrace();
		}
		
	}
	
	
}
