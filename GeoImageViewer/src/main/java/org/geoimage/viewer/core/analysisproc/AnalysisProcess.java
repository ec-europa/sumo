package org.geoimage.viewer.core.analysisproc;

import static org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditGeometryVectorLayer.ARTEFACTS_AMBIGUITY_TAG;
import static org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditGeometryVectorLayer.AZIMUTH_AMBIGUITY_TAG;
import static org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditGeometryVectorLayer.DETECTED_PIXELS_TAG;
import static org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditGeometryVectorLayer.TRESHOLD_PIXELS_AGG_TAG;
import static org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditGeometryVectorLayer.TRESHOLD_PIXELS_TAG;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.geoimage.analysis.AzimuthAmbiguity;
import org.geoimage.analysis.BlackBorderAnalysis;
import org.geoimage.analysis.DetectedPixels;
import org.geoimage.analysis.KDistributionEstimation;
import org.geoimage.analysis.S1ArtefactsAmbiguity;
import org.geoimage.analysis.VDSAnalysis;
import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.impl.s1.Sentinel1;
import org.geoimage.utils.GeometryExtractor;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.configuration.PlatformConfiguration;
import org.geoimage.viewer.core.layers.AttributesLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditVDSVectorLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;


/**
 * 
 * @author Pietro Argentieri
 *
 */
public  class AnalysisProcess implements Runnable,VDSAnalysis.ProgressListener {
		private float ENL;
		private VDSAnalysis analysis;
		private IMask[] bufferedMask=null;
		private String[] thresholds;
		private int buffer;
		private List<ComplexEditVDSVectorLayer>resultLayers;
		private GeoImageReader gir;
		private boolean stop=false;
		private double neighbouringDistance;
		private int tilesize;
		private boolean removelandconnectedpixels;
		private boolean display;
		private int numPointLimit=0;
		private boolean displaybandanalysis;
		private String agglomerationMethodology;
		
		private static org.slf4j.Logger logger=LoggerFactory.getLogger(AnalysisProcess.class);

		
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
		 * @param numLimitPoint   num max of point that we can analize (0=all) 
		 */
		public AnalysisProcess(GeoImageReader gir,float ENL,VDSAnalysis analysis,IMask[] bufferedMask,String[] thresholds,int buffer,int numLimitPoint) {
			this.ENL=ENL;
			this.analysis=analysis;
			this.bufferedMask=bufferedMask;
			this.thresholds=thresholds;
			this.buffer=buffer;
			this.resultLayers=new ArrayList<ComplexEditVDSVectorLayer>();
			listeners=new ArrayList<VDSAnalysisProcessListener>();
			this.gir=gir;//.clone();
			this.numPointLimit=numLimitPoint;
			
			neighbouringDistance=Platform.getConfiguration().getNeighbourDistance(1.0);
            tilesize=Platform.getConfiguration().getTileSize(200);
            removelandconnectedpixels = PlatformConfiguration.getConfigurationInstance().removeLandConnectedPixel();
            display = Platform.getConfiguration().getDisplayPixel()&&!Platform.isBatchMode();
            displaybandanalysis= Platform.getConfiguration().getDisplayBandAnalysis();
            agglomerationMethodology = Platform.getConfiguration().getAgglomerationAlg();
		}
   
		/**
		 *  Exec the analysis process
		 */
		public void run() {
			notifyStartProcessListener();
			SarImageReader reader=((SarImageReader)gir);
			
			//run the blackborder analysis for the s1 images
			BlackBorderAnalysis blackBorderAnalysis=null;
            if(gir instanceof Sentinel1){
	                MaskVectorLayer mv=null;
	           	 	if(bufferedMask!=null&&bufferedMask.length>0)
	           	 		mv=(MaskVectorLayer)bufferedMask[0];
	           	 	if(mv!=null)
	           	 		blackBorderAnalysis= new BlackBorderAnalysis(gir,mv.getGeometries());
	           	 	else 
	           		    blackBorderAnalysis= new BlackBorderAnalysis(gir,null);
             } 	
             if(blackBorderAnalysis!=null){
            	 blackBorderAnalysis.analyse(5);
             }
             //end blackborder analysis
             
             // list of bands
             int numberofbands = gir.getNBand();
             int[] bands = new int[numberofbands];

             
             // create K distribution
             final KDistributionEstimation kdist = new KDistributionEstimation(ENL,blackBorderAnalysis);
             
             
             DetectedPixels mergePixels = new DetectedPixels(reader);
             DetectedPixels banddetectedpixels[]=new DetectedPixels[numberofbands];
             
             
             AzimuthAmbiguity[] azimuthAmbiguity =new AzimuthAmbiguity[numberofbands];
             S1ArtefactsAmbiguity[] arAmbiguity =new S1ArtefactsAmbiguity[numberofbands];
             
             List<Geometry>allAmbiguities=new ArrayList<>();
             
             //landmask name
             String bufferedMaskName="";
             if(bufferedMask!=null && bufferedMask.length>0){
            	 bufferedMaskName=bufferedMask[0].getName(); 
             }
             
             String timeStampStart=reader.getTimeStampStart();
             double azimuth=reader.getAzimuthSpacing();
             
             try{
	             for (int band = 0; band < numberofbands&&!stop; band++) {
	            	 notifyAnalysisBand( new StringBuilder().append("VDS: analyzing band ").append(gir.getBandName(band)).toString());
	            	 
	            	 int vTiles=analysis.getVerTiles();
	            	 notifyVDSAnalysis("Performing VDS Analysis",vTiles);
	            	 analysis.addProgressListener(this);

	            	 //identify probably target
	            	 analysis.run(kdist,blackBorderAnalysis,band);
	            	 banddetectedpixels[band]=analysis.getPixels();
	            	 
	            	 if(numPointLimit!=0&&banddetectedpixels[band].getAllDetectedPixels().size()>numPointLimit){
	            		 logger.warn("Too much points. Stop Image analysis!!!");
	            		 return;
	            	 }
	            	 
	            	 if (mergePixels == null) {
	            		mergePixels=banddetectedpixels[band];
	            	}else{
	            		mergePixels.merge(banddetectedpixels[band]);	
	            	}
	            	 
	            	 bands[band] = band;
	            	 String trheshString=thresholds[getPolIdx(gir.getBandName(band))];
	                 
	                 if (numberofbands < 1 || displaybandanalysis) {
	                     notifyAgglomerating( new StringBuilder().append("VDS: agglomerating detections for band ").append(gir.getBandName(band)).toString());

		            	 //merge pixel to build the "boats"
	                     if (agglomerationMethodology.startsWith("d")) {
	                         // method distance used
	                         banddetectedpixels[band].agglomerate();
	                         banddetectedpixels[band].computeBoatsAttributes();
	                     } else {
	                         // method neighbours used
	                         if(stop)
	                        	 break;
	                         
	                         analysis.agglomerateNeighbours(banddetectedpixels[band],neighbouringDistance, tilesize,removelandconnectedpixels, 
	                         		 (bufferedMask != null) && (bufferedMask.length != 0) ? bufferedMask[0] : null, kdist,band);
	                     }
	                     
	                     String layerName=new StringBuilder("VDS analysis ").append(gir.getBandName(band)).append(" ").append(trheshString).toString();
	                     
	                     ComplexEditVDSVectorLayer vdsanalysisLayer = new ComplexEditVDSVectorLayer(Platform.getCurrentImageLayer(),layerName,
	                    		 "point", createGeometricLayer(timeStampStart,azimuth, banddetectedpixels[band]),
	                    		 thresholds,ENL,buffer,bufferedMaskName,""+band);

	                     vdsanalysisLayer.addGeometries(DETECTED_PIXELS_TAG, new Color(0x00FF00), 1, GeometricLayer.POINT, banddetectedpixels[band].getAllDetectedPixels(), display);
	                     
	                     if (!agglomerationMethodology.startsWith("d")) {
	                         vdsanalysisLayer.addGeometries(TRESHOLD_PIXELS_AGG_TAG, new Color(0x0000FF), 1, GeometricLayer.POINT, banddetectedpixels[band].getThresholdaggregatePixels(), display);
	                         vdsanalysisLayer.addGeometries(TRESHOLD_PIXELS_TAG, new Color(0x00FFFF), 1, GeometricLayer.POINT, banddetectedpixels[band].getThresholdclipPixels(), display);
	                     }
	                     
	                     //Azimuth Ambiguities
	                     notifyCalcAzimuth("VDS: looking for azimuth ambiguities...");
	                     azimuthAmbiguity[band] = new AzimuthAmbiguity(banddetectedpixels[band].getBoats(), (SarImageReader) gir,band);
	                     List<Geometry> az=azimuthAmbiguity[band].getAmbiguityboatgeometry();
                         allAmbiguities.addAll(az);
	                     vdsanalysisLayer.addGeometries(AZIMUTH_AMBIGUITY_TAG,Color.RED,5, GeometricLayer.POINT, az, display);

	                     //Azimuth Ambiguities ONLY FOR S1
	                     if(gir instanceof Sentinel1){
	                    	 if(((Sentinel1)gir).getInstumentationMode().equalsIgnoreCase("EW")||((Sentinel1)gir).getInstumentationMode().equalsIgnoreCase("IW")){
			                     notifyCalcAzimuth("VDS: looking for artefacts ambiguities...");
			                     arAmbiguity[band]  = new S1ArtefactsAmbiguity(banddetectedpixels[band].getBoats(), (SarImageReader) gir,band);	
			                     List<Geometry> artefactsA=arAmbiguity[band].getAmbiguityboatgeometry();
			                     allAmbiguities.addAll(artefactsA);
			                     vdsanalysisLayer.addGeometries(ARTEFACTS_AMBIGUITY_TAG,Color.CYAN,5, GeometricLayer.POINT, artefactsA, display);
	                    	 }    
	                     }    
                         
                         if ((bufferedMask != null) && (bufferedMask.length > 0)) {
	                        vdsanalysisLayer.addGeometries("bufferedmask", Color.BLUE, 1, GeometricLayer.POLYGON, bufferedMask[0].getGeometries(), true);
	                     }
	                     //leave display params forced to false
	                     vdsanalysisLayer.addGeometries("tiles", new Color(0xFF00FF), 1, GeometricLayer.LINESTRING, GeometryExtractor.getTiles(gir.getWidth(),gir.getHeight(),analysis.getTileSize()), false);
	                     
	
	                     //if(!Platform.isBatchMode())
	                    	 //Platform.getLayerManager().addLayer(vdsanalysis);
	                     notifyLayerReady(vdsanalysisLayer); 
	                     resultLayers.add(vdsanalysisLayer);
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
	                	 analysis.agglomerateNeighbours(mergePixels,neighbouringDistance, tilesize, removelandconnectedpixels, (bufferedMask != null) && (bufferedMask.length != 0) ? bufferedMask[0] : null, kdist,bands);
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
	                     vdsanalysisLayer.addGeometries(TRESHOLD_PIXELS_AGG_TAG, new Color(0x0000FF), 1, GeometricLayer.POINT, mergePixels.getThresholdaggregatePixels(), display);
	                     vdsanalysisLayer.addGeometries(TRESHOLD_PIXELS_TAG, new Color(0x00FFFF), 1, GeometricLayer.POINT, mergePixels.getThresholdclipPixels(), display);
	                 }
	                 vdsanalysisLayer.addGeometries(DETECTED_PIXELS_TAG, new Color(0x00FF00), 1, GeometricLayer.POINT, mergePixels.getAllDetectedPixels(), display);
	                 
	                 if ((bufferedMask != null) && (bufferedMask.length > 0)) {
	                     vdsanalysisLayer.addGeometries("bufferedmask", new Color(0x0000FF), 1, GeometricLayer.POLYGON, bufferedMask[0].getGeometries(), true);
	                 }
	                 vdsanalysisLayer.addGeometries("tiles", new Color(0xFF00FF), 1, GeometricLayer.LINESTRING,GeometryExtractor.getTiles(gir.getWidth(),gir.getHeight(),analysis.getTileSize()), false);
	                 
	                 vdsanalysisLayer.addGeometries(ComplexEditVDSVectorLayer.AMBIGUITY_TAG, Color.RED, 5, GeometricLayer.POINT,allAmbiguities , display);
	                 
	                 //if(!Platform.isBatchMode())
	                 //	 Platform.getLayerManager().addLayer(vdsanalysisLayer);
	                 notifyLayerReady(vdsanalysisLayer);
	                 resultLayers.add(vdsanalysisLayer);
	             }
	             stop();
             }catch(Exception ee){
            	 ee.printStackTrace();
             }    
         }
		
		private void stop(){
           notifyEndProcessListener();
           removeAllProcessListener();
		}
		
		/**
		 * 
		 * @param timeStampStart
		 * @param azimuth
		 * @param pixels
		 * @return
		 */
		public GeometricLayer createGeometricLayer(String timeStampStart,double azimuth, DetectedPixels pixels) {
	        GeometricLayer out = new GeometricLayer("point");
	        out.setName("VDS Analysis");
	        GeometryFactory gf = new GeometryFactory();
	        long runid = System.currentTimeMillis();
	        int count=0;
	        for (DetectedPixels.Boat boat : pixels.getBoats()) {
	            AttributesLayer atts = AttributesLayer.createAttributes(VDSSchema.schema, VDSSchema.types);
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
	            //compute the direction of the vessel considering the azimuth of the image result is between 0 and 180 degree
	            double degree = boat.getHeading() + 90 + azimuth;
	            if (degree > 180) {
	                degree = degree - 180;
	            }
	         
	            atts.set(VDSSchema.ESTIMATED_HEADING, degree);
	            out.put(gf.createPoint(new Coordinate(boat.getPosx(), boat.getPosy())), atts);
	        }
	        return out;
	    }
		
		/**
		 * 
		 * @param polarization
		 * @return
		 */
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
		 
		/**
		 *  
		 * @param listener
		 */
		public void addProcessListener(VDSAnalysisProcessListener listener){
			this.listeners.add(listener);
		} 
		/**
		 * 
		 * @param listener
		 */
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
		
		public void notifyVDSAnalysis(String message,int maxSteps){
			for(VDSAnalysisProcessListener listener:listeners){
				listener.performVDSAnalysis(message,maxSteps);
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

		@Override
		public void startRowProcesseing(int step) {
			for(VDSAnalysisProcessListener listener:listeners){
				listener.nextVDSAnalysisStep(step);
			}
		}

		@Override
		public void endRowProcesseing(int row) {
		}
		
     }

