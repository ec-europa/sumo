/*
 * 
 */
package jrc.it.geolocation.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.vividsolutions.jts.geom.Coordinate;

import jrc.it.geolocation.metadata.impl.S1Metadata;


public class ParallelGeoCoding {

	private GeoCoding geoCoding;

	public ParallelGeoCoding(GeoCoding geoc) {
		this.geoCoding = geoc;
	}

	class ParallelForward implements Callable<double[]> {
		private double l = 0;
		private double p = 0;

		/**
		 * 
		 * @param l l=line=y
		 * @param p p=pixel=x
		 */
		ParallelForward(double l, double p) {
			this.l = l;
			this.p = p;
		}

		@Override
		public double[] call() throws Exception {
			return geoCoding.geoFromPixel(l, p);
		}
	}

	class ParallelReverse implements Callable<double[]> {
		private double lat;
		private double lon;

		ParallelReverse(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}

		@Override
		public double[] call() throws Exception {
			return geoCoding.pixelFromGeo(lat, lon);
		}
	}

	/**
	 * 
	 * @param coords
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public List<double[]> parallelPixelFromGeo(final Coordinate[] coords)
			throws InterruptedException, ExecutionException {
		int processors = Runtime.getRuntime().availableProcessors();
		ThreadPoolExecutor executor = new ThreadPoolExecutor(2, processors, 2, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());//(ThreadPoolExecutor)Executors.newFixedThreadPool(processors);
		try {
			final List<Future<double[]>> tasks = new ArrayList<Future<double[]>>();
			for (int i = 0; i < coords.length; i++) {
				tasks.add(executor.submit(new ParallelReverse(coords[i].x, coords[i].y)));
			}
			executor.shutdown();

			final List<double[]> points = new ArrayList<double[]>();
			for (Future<double[]> result : tasks) {
				List<double[]> l = Arrays.asList(result.get());
				points.addAll(l);
			}

			return points;
		} catch (Exception e) {
			if (!executor.isShutdown())
				executor.shutdown();
			throw e;
		}
	}

	public List<double[]> parallelGeoFromPixel(final Coordinate[] coords)
			throws InterruptedException, ExecutionException {
		int processors = Runtime.getRuntime().availableProcessors();
		ThreadPoolExecutor executor = new ThreadPoolExecutor(2, processors, 5000, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());//(ThreadPoolExecutor)Executors.newFixedThreadPool(processors);

		List<Callable<double[]>> tasks = new ArrayList<Callable<double[]>>();
		for (final Coordinate c : coords) {
			tasks.add(new ParallelForward(c.y,c.x));
		}
		List<Future<double[]>> results = executor.invokeAll(tasks);
		executor.shutdown();

		List<double[]> points = new ArrayList<double[]>();
		for (Future<double[]> result : results) {
			List<double[]> l = Arrays.asList(result.get());
			points.addAll(l);
		}

		return points;
	}

	/*            Test con fork join
	class ParallelReverse extends RecursiveTask<double[]>{
		private double lat;
		private double lon;
		
		ParallelReverse(double lat,double lon){
			this.lat=lat;
			this.lon=lon;
		}
		
		@Override
		public double[] compute() {
			double[] result=null;
			try {
				result=geoCoding.pixelFromGeo(lat,lon);
			} catch (GeoLocationException e) {
				e.printStackTrace();
			}
			return result;
		}
	}
	
	public List<double[]>parallelPixelFromGeo(Coordinate[] coords) throws InterruptedException, ExecutionException {
		final ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		try{
	
			//List<ForkJoinTask<double[]>> tasks=new ArrayList<ForkJoinTask<double[]>>();
			List<RecursiveTask<double[]>> forks = new LinkedList<>();
			for(Coordinate c:coords){
				ParallelReverse t=new ParallelReverse(c.x,c.y);
				forks.add(t);
				//t.fork();
				forkJoinPool.execute(t);
			}	
	
			forkJoinPool.shutdown();
			
			List<double[]> points=new ArrayList<double[]>();
	        for (Future<double[]> result : forks) {
	        	List<double[]> l=Arrays.asList(result.get());
	            points.addAll(l);
	        }   
	    
	        return points;
		}catch(Exception e){
			if(!forkJoinPool.isShutdown())
				forkJoinPool.shutdown();
			throw e;
		}
	   
	}   */

	public static void main(String[] args) {
		String metaF = "H://Radar-Images//S1Med//S1//EW//S1A_EW_GRDH_1SDV_20141020T055155_20141020T055259_002909_0034C1_F8D5.SAFE//annotation//s1a-ew-grd-vv-20141020t055155-20141020t055259-002909-0034c1-001.xml";

		GeoCoding gc;
		try {
			S1Metadata meta =new S1Metadata(metaF);
			meta.initMetaData();
			gc = new S1GeoCodingImpl(meta);
			double lat = 41.31735;//43.13935;//42.81202;
			double lon = 2.17263;//3.35876;//10.32972;
			Coordinate c = new Coordinate(lat, lon);
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
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

}
