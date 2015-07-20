package org.geoimage.viewer.core.analysisproc;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.geoimage.analysis.AzimuthAmbiguity;
import org.geoimage.analysis.BlackBorderAnalysis;
import org.geoimage.analysis.DetectedPixels;
import org.geoimage.analysis.KDistributionEstimation;
import org.geoimage.analysis.VDSAnalysis;
import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.utils.GeometryExtractor;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.configuration.PlatformConfiguration;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditVDSVectorLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * 
 * @author Pietro Argentieri
 *
 */
public  class AnalysisProcess implements Runnable {
		private float ENL;
		private VDSAnalysis analysis;
		private IMask[] bufferedMask=null;
		private String[] thresholds;
		private int buffer;
		private List<ComplexEditVDSVectorLayer>resultLayers;
		private BlackBorderAnalysis blackBorderAnalysis=null;
		private GeoImageReader gir;
		private boolean stop=false;
		private double neighbouringDistance;
		private int tilesize;
		private boolean removelandconnectedpixels;
		private boolean display;
		
		public boolean isStop() {
			return stop;
		}

		public void setStop(boolean stop) {
			this.stop = stop;
		}

		private List<VDSAnalysisProcessListener> listeners; 
		
		
		/**
		 * 
		 * @return the list of the layers with the target results
		 */
		public List<ComplexEditVDSVectorLayer> getResultLayers() {
			return resultLayers;
		}

		public void setResultLayers(List<ComplexEditVDSVectorLayer> resultLayers) {
			this.resultLayers = resultLayers;
		}

		/**
		 * 
		 * @param gir
		 * @param ENL
		 * @param analysis
		 * @param bufferedMask
		 * @param thresholds
		 * @param buffer
		 * @param blackBorderAnalysis
		 */
		public AnalysisProcess(GeoImageReader gir,float ENL,VDSAnalysis analysis,IMask[] bufferedMask,String[] thresholds,int buffer,BlackBorderAnalysis blackBorderAnalysis) {
			this.ENL=ENL;
			this.analysis=analysis;
			this.bufferedMask=bufferedMask;
			this.blackBorderAnalysis=blackBorderAnalysis;
			this.thresholds=thresholds;
			this.buffer=buffer;
			this.resultLayers=new ArrayList<ComplexEditVDSVectorLayer>();
			listeners=new ArrayList<VDSAnalysisProcessListener>();
			this.gir=gir;//.clone();
			
			neighbouringDistance=Platform.getConfiguration().getNeighbourDistance(1.0);
            tilesize=Platform.getConfiguration().getTileSize(200);
            removelandconnectedpixels = PlatformConfiguration.getConfigurationInstance().removeLandConnectedPixel();
            display = Platform.getConfiguration().getDisplayPixel()&&!Platform.isBatchMode();
		}
   
		/**
		 *  Exec the analysis process
		 */
		public void run() {
			notifyStartProcessListener();
			SarImageReader reader=((SarImageReader)gir);
			
			//run the black border analysis 
             if(blackBorderAnalysis!=null){
            	 blackBorderAnalysis.analyse(5);
             }	 
             // list of bands
             int numberofbands = gir.getNBand();
             int[] bands = new int[numberofbands];

             
             // create K distribution
             final KDistributionEstimation kdist = new KDistributionEstimation(ENL);
             DetectedPixels mergePixels = new DetectedPixels(reader);
             DetectedPixels banddetectedpixels[]=new DetectedPixels[numberofbands];
             
             
             AzimuthAmbiguity[] azimuthAmbiguity =new AzimuthAmbiguity[numberofbands];
             List<Geometry>allAzimuthAmbiguity=new ArrayList<>();
             
             boolean displaybandanalysis = Platform.getConfiguration().getDisplayBandAnalysis();
             String agglomerationMethodology = Platform.getConfiguration().getAgglomerationAlg();
             
             //landmask name
             String bufferedMaskName="";
             if(bufferedMask!=null && bufferedMask.length>0){
            	 bufferedMaskName=bufferedMask[0].getName(); 
             }
             
             String timeStampStart=reader.getTimeStampStart();
             double azimuth=reader.getAzimuthSpacing();
             
             //int processors = Runtime.getRuntime().availableProcessors();
             //ExecutorService executor = Executors.newFixedThreadPool(processors);
             
             try{
	            // final List<Future<DetectedPixels>> tasks=new ArrayList<Future<DetectedPixels>>();
	            
	             /*   parallel test version
	             // compute detections for each band separately
	             for (int band = 0; band < numberofbands&&!stop; band++) {
	            	 notifyAnalysisBand( new StringBuilder().append("VDS: analyzing band ").append(gir.getBandName(band)).toString());
	            	 final int b=band;
	            	 
	            	 Callable<DetectedPixels> anal=new Callable<DetectedPixels>(){
	            		    @Override
	            			public DetectedPixels call() throws Exception {
	                         	return analysis.run(kdist,blackBorderAnalysis,b);
	            			}
	            	 };
	            	 tasks.add(executor.submit(anal));
	            	
	             }    
	             executor.shutdown();
	             
	             for (int idx=0;idx<tasks.size();idx++) {
	            	Future<DetectedPixels> result =tasks.get(idx);
	            	banddetectedpixels[idx]=result.get();
	            	if (mergePixels == null) {
	            		mergePixels=banddetectedpixels[idx];
	            	}else{
	            		mergePixels.merge(banddetectedpixels[idx]);	
	            	}
	
	 	        }   */
	             
	             for (int band = 0; band < numberofbands&&!stop; band++) {
	            	 notifyAnalysisBand( new StringBuilder().append("VDS: analyzing band ").append(gir.getBandName(band)).toString());
	            	
	            	 analysis.run(kdist,blackBorderAnalysis,band);

	            	 banddetectedpixels[band]=analysis.getPixels();

	            	 if (mergePixels == null) {
	            		mergePixels=banddetectedpixels[band];
	            	}else{
	            		mergePixels.merge(banddetectedpixels[band]);	
	            	}
	            	 
	            	 
	            	 bands[band] = band;
	            	 String trheshString=thresholds[getPolIdx(gir.getBandName(band))];
	                 
	                 if (numberofbands < 1 || displaybandanalysis) {
	                     notifyAgglomerating( new StringBuilder().append("VDS: agglomerating detections for band ").append(gir.getBandName(band)).toString());
	
	                     if (agglomerationMethodology.startsWith("d")) {
	                         // method distance used
	                         banddetectedpixels[band].agglomerate();
	                         banddetectedpixels[band].computeBoatsAttributes();
	                     } else {
	                         // method neighbours used
	                         if(stop)
	                        	 break;
	                         banddetectedpixels[band].agglomerateNeighbours(neighbouringDistance, tilesize,removelandconnectedpixels, 
	                         		 new int[]{band},(bufferedMask != null) && (bufferedMask.length != 0) ? bufferedMask[0] : null, kdist);
	                        
	                     }
	                     
	                     if (!gir.supportAzimuthAmbiguity()) {
	                         System.out.println("\nSatellite sensor not supported for Azimuth Ambiguity detection");
	                     }else{
	                    	 notifyCalcAzimuth("VDS: looking for azimuth ambiguities...");
	                         azimuthAmbiguity[band] = new AzimuthAmbiguity(banddetectedpixels[band].getBoats(), (SarImageReader) gir,band);
	                     }
                         
	                     
	                     String layerName=new StringBuilder("VDS analysis ").append(gir.getBandName(band)).append(" ").append(trheshString).toString();
	                     
	                     ComplexEditVDSVectorLayer vdsanalysis = new ComplexEditVDSVectorLayer(Platform.getCurrentImageLayer(),layerName,
	                    		 "point", createGeometricLayer(timeStampStart,azimuth, banddetectedpixels[band]),
	                    		 thresholds,ENL,buffer,bufferedMaskName,""+band);
	                     
	                     if (!agglomerationMethodology.startsWith("d")) {
	                         vdsanalysis.addGeometries("thresholdaggregatepixels", new Color(0x0000FF), 1, GeometricLayer.POINT, banddetectedpixels[band].getThresholdaggregatePixels(), display);
	                         vdsanalysis.addGeometries("thresholdclippixels", new Color(0x00FFFF), 1, GeometricLayer.POINT, banddetectedpixels[band].getThresholdclipPixels(), display);
	                     }
	                     vdsanalysis.addGeometries("detectedpixels", new Color(0x00FF00), 1, GeometricLayer.POINT, banddetectedpixels[band].getAllDetectedPixels(), display);
	                     
	                     List<Geometry> az=azimuthAmbiguity[band].getAmbiguityboatgeometry();
                         allAzimuthAmbiguity.addAll(az);
	                     vdsanalysis.addGeometries("azimuthambiguities", new Color(0xFFD000), 5, GeometricLayer.POINT, az, display);

                         
                         if ((bufferedMask != null) && (bufferedMask.length > 0)) {
	                        vdsanalysis.addGeometries("bufferedmask", new Color(0x0000FF), 1, GeometricLayer.POLYGON, bufferedMask[0].getGeometries(), display);
	                     }
	                     //leave display params forced to false
	                     vdsanalysis.addGeometries("tiles", new Color(0xFF00FF), 1, GeometricLayer.LINESTRING, GeometryExtractor.getTiles(gir.getWidth(),gir.getHeight(),analysis.getTileSize()), false);
	                     
	
	                     //if(!Platform.isBatchMode())
	                    	 //Platform.getLayerManager().addLayer(vdsanalysis);
	                     notifyLayerReady(vdsanalysis); 
	                     resultLayers.add(vdsanalysis);
	                 }
	             }
	             if(stop){
	            	 stop();
	            	 return;
	             }	 
	             // display merged results if there is more than one band
	             if (bands.length > 1) {
	                 notifyAgglomerating("VDS: agglomerating detections...");
	                 
	                 if (agglomerationMethodology.startsWith("d")) {
	                     // method distance used
	                     mergePixels.agglomerate();
	                     mergePixels.computeBoatsAttributes();
	                 } else {
	                     // method neighbours used
	                     mergePixels.agglomerateNeighbours(neighbouringDistance, tilesize, removelandconnectedpixels, bands, (bufferedMask != null) && (bufferedMask.length != 0) ? bufferedMask[0] : null, kdist);
	                 }
	
	                 if(stop){
	                     stop();
	                	 return;
	                 }
	                 
	                 //TODO: per ora Merged viene utilizzato per indicare che e' il layer del merge e non delle bande ma VA CAMBIATO!!!
	                 ComplexEditVDSVectorLayer vdsanalysisLayer = new ComplexEditVDSVectorLayer(Platform.getCurrentImageLayer(),"VDS analysis all bands merged", 
	                		 																	"point", createGeometricLayer(timeStampStart,azimuth, mergePixels),
	                		 																	thresholds,ENL,buffer,bufferedMaskName,"Merged");
	                 boolean display = Platform.getConfiguration().getDisplayPixel();
	                 if (!agglomerationMethodology.startsWith("d")) {
	                     vdsanalysisLayer.addGeometries("thresholdaggregatepixels", new Color(0x0000FF), 1, GeometricLayer.POINT, mergePixels.getThresholdaggregatePixels(), display);
	                     vdsanalysisLayer.addGeometries("thresholdclippixels", new Color(0x00FFFF), 1, GeometricLayer.POINT, mergePixels.getThresholdclipPixels(), display);
	                 }
	                 vdsanalysisLayer.addGeometries("detectedpixels", new Color(0x00FF00), 1, GeometricLayer.POINT, mergePixels.getAllDetectedPixels(), display);
	                 
	                 if ((bufferedMask != null) && (bufferedMask.length > 0)) {
	                     vdsanalysisLayer.addGeometries("bufferedmask", new Color(0x0000FF), 1, GeometricLayer.POLYGON, bufferedMask[0].getGeometries(), display);
	                 }
	                 vdsanalysisLayer.addGeometries("tiles", new Color(0xFF00FF), 1, GeometricLayer.LINESTRING,GeometryExtractor.getTiles(gir.getWidth(),gir.getHeight(),analysis.getTileSize()), false);
	
	                 
	                 vdsanalysisLayer.addGeometries("azimuthambiguities", new Color(0xFFD000), 5, GeometricLayer.POINT,allAzimuthAmbiguity , display);
	                 //if(!Platform.isBatchMode())
	                 //	 Platform.getLayerManager().addLayer(vdsanalysisLayer);
	                 notifyLayerReady(vdsanalysisLayer);
	                 resultLayers.add(vdsanalysisLayer);
	             }
	             stop();
             }catch(Exception ee){
             //catch(ExecutionException|InterruptedException ee){
            	 ee.printStackTrace();
             }    
         }
		
		private void stop(){
           notifyEndProcessListener();
           removeAllProcessListener();
		}
		
		
		public GeometricLayer createGeometricLayer(String timeStampStart,double azimuth, DetectedPixels pixels) {
	        GeometricLayer out = new GeometricLayer("point");
	        out.setName("VDS Analysis");
	        GeometryFactory gf = new GeometryFactory();
	        long runid = System.currentTimeMillis();
	        int count=0;
	        for (DetectedPixels.Boat boat : pixels.getBoats()) {
	            Attributes atts = Attributes.createAttributes(VDSSchema.schema, VDSSchema.types);
	            atts.set(VDSSchema.ID, count++);
	            atts.set(VDSSchema.MAXIMUM_VALUE, boat.getValue());
	            atts.set(VDSSchema.TILE_AVERAGE, boat.getTileAvg());
	            atts.set(VDSSchema.TILE_STANDARD_DEVIATION, boat.getTileStd());
	            atts.set(VDSSchema.THRESHOLD, boat.getThreshold());
	            atts.set(VDSSchema.RUN_ID, runid + "");
	            atts.set(VDSSchema.NUMBER_OF_AGGREGATED_PIXELS, boat.getSize());
	            atts.set(VDSSchema.ESTIMATED_LENGTH, boat.getLength());
	            atts.set(VDSSchema.ESTIMATED_WIDTH, boat.getWidth());
	            atts.set(VDSSchema.SIGNIFICANCE, (boat.getLength() - boat.getWidth()) / (boat.getWidth() * boat.getHeading()));
	            timeStampStart=timeStampStart.replace("Z", "");
	            atts.set(VDSSchema.DATE, Timestamp.valueOf(timeStampStart));
	            atts.set(VDSSchema.VS, 0);
	            //compute the direction of the vessel considering the azimuth of the image
	            //result is between 0 and 180 degree
	            double degree = boat.getHeading() + 90 + azimuth;
	            if (degree > 180) {
	                degree = degree - 180;
	            }
	         
	            atts.set(VDSSchema.ESTIMATED_HEADING, degree);
	            out.put(gf.createPoint(new Coordinate(boat.getPosx(), boat.getPosy())), atts);
	        }
	        return out;
	    }
		
		 protected int getPolIdx(String polarization){
	    	 int pol=0;
	    	 if (polarization.equals("HH") || polarization.equals("H/H")) {
	            pol=0;
	         } else if (polarization.equals("HV") || polarization.equals("H/V")) {
	        	pol=1;
	         } else if (polarization.equals("VH") || polarization.equals("V/H")) {
	        	pol=2;
	         } else if (polarization.equals("VV") || polarization.equals("V/V")) {
	        	pol=3;
	         }
	    	 return pol;
	    }
		 
		 
		public void addProcessListener(VDSAnalysisProcessListener listener){
			this.listeners.add(listener);
		} 
		public void removeProcessListener(VDSAnalysisProcessListener listener){
			this.listeners.remove(listener);
		}
		
		public void removeAllProcessListener(){
			this.listeners.clear();
		}
		
		public void notifyEndProcessListener(){
			for(VDSAnalysisProcessListener listener:listeners){
				listener.endAnalysis();
			}
		} 
		 
		public void notifyStartProcessListener(){
			for(VDSAnalysisProcessListener listener:listeners){
				listener.startAnalysis();
			}
		} 
		
		public void notifyAgglomerating(String message){
			for(VDSAnalysisProcessListener listener:listeners){
				listener.agglomerating(message);
			}
		}
		
		public void notifyLayerReady(ILayer layer){
			for(VDSAnalysisProcessListener listener:listeners){
				listener.layerReady(layer);
			}
		}
		
		public void notifyAnalysisBand(String message){
			for(VDSAnalysisProcessListener listener:listeners){
				listener.startAnalysisBand(message);
			}
		}
		
		public void notifyCalcAzimuth(String message){
			for(VDSAnalysisProcessListener listener:listeners){
				listener.calcAzimuthAmbiguity(message);
			}
		}
		
     }

