/*
 *
 */
package org.geoimage.viewer.core.analysisproc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.geoimage.analysis.AzimuthAmbiguity;
import org.geoimage.analysis.BlackBorderAnalysis;
import org.geoimage.analysis.Boat;
import org.geoimage.analysis.DetectedPixels;
import org.geoimage.analysis.KDistributionEstimation;
import org.geoimage.analysis.S1ArtefactsAmbiguity;
import org.geoimage.analysis.VDSAnalysis;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.impl.s1.Sentinel1;
import org.geoimage.viewer.core.GeometryImage;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditVDSVectorLayer;
import org.geoimage.viewer.util.GeometryExtractor;
import org.jrc.sumo.configuration.PlatformConfiguration;
import org.jrc.sumo.util.Constant;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;


/**
 *
 * @author Pietro Argentieri
 *
 */
public  class AnalysisProcess implements Callable<AnalysisProcess.Results>,VDSAnalysis.ProgressListener {
		private float ENL;
		private VDSAnalysis analysis;

		private int buffer;
		private GeoImageReader gir;
		private boolean stop=false;
		private double neighbouringDistance;
		private int neighbourTilesize;
		private boolean removelandconnectedpixels;
		private boolean display;
		private int numPointLimit=0;
		private boolean displaybandanalysis;
		private String agglomerationMethodology;
		private String bufferedMaskName="";
		private BlackBorderAnalysis blackBorderAnalysis=null;


		public class Results{
			private List<ComplexEditVDSVectorLayer> layerResults;
			private GeoImageReader reader;

			Results(GeoImageReader gir,List<ComplexEditVDSVectorLayer> results){
				layerResults=results;
				reader=gir;
			}

			public List<ComplexEditVDSVectorLayer> getLayerResults() {
				return layerResults;
			}

			public void setLayerResults(List<ComplexEditVDSVectorLayer> layerResults) {
				this.layerResults = layerResults;
			}

			public GeoImageReader getReader() {
				return reader;
			}

			public void setReader(GeoImageReader reader) {
				this.reader = reader;
			}


		}

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
		 * @param gir
		 * @param ENL
		 * @param analysis
		 * @param bufferedMask
		 * @param thresholds
		 * @param buffer
		 * @param blackBorderAnalysis
		 * @param numLimitPoint   num max of point that we can analize (0=all)
		 */
		public AnalysisProcess(GeoImageReader gir,float ENL,VDSAnalysis analysis,
				int buffer,int numLimitPoint,BlackBorderAnalysis blackBorderAnalysis) {
			this.ENL=ENL;
			this.analysis=analysis;

			if(analysis.getCoastMask()!=null)
				bufferedMaskName=analysis.getCoastMask().getMaskName();

			this.buffer=buffer;
			listeners=Collections.synchronizedList(new ArrayList<VDSAnalysisProcessListener>());
			this.gir=gir;//.clone();
			this.numPointLimit=numLimitPoint;
			this.blackBorderAnalysis=blackBorderAnalysis;

			neighbouringDistance=SumoPlatform.getApplication().getConfiguration().getNeighbourDistance(1.0);
            neighbourTilesize=SumoPlatform.getApplication().getConfiguration().getTileSize(200);
            removelandconnectedpixels = PlatformConfiguration.getConfigurationInstance().removeLandConnectedPixel();
            display = SumoPlatform.getApplication().getConfiguration().getDisplayPixel()&&!SumoPlatform.isBatchMode();
            displaybandanalysis= SumoPlatform.getApplication().getConfiguration().getDisplayBandAnalysis();
            agglomerationMethodology = SumoPlatform.getApplication().getConfiguration().getAgglomerationAlg();
		}


		public BlackBorderAnalysis runBBAnalysis(){
			//run the blackborder analysis for the s1 images
             if(blackBorderAnalysis!=null){
            	 notifyBBAnalysis("Start BB Analysis");
            	 if(!analysis.isAnalyseSingleTile()){
            		 blackBorderAnalysis.analyse(SumoPlatform.getApplication().getConfiguration().getNumTileBBAnalysis(),BlackBorderAnalysis.ANALYSE_ALL);
            	 }else{
            		 int[] sideToAnalyze=new int[]{-1 ,-1 ,-1 ,-1};
            		 if(analysis.getxTileToAnalyze()<SumoPlatform.getApplication().getConfiguration().getNumTileBBAnalysis())
            			 sideToAnalyze[0]=BlackBorderAnalysis.ANALYSE_LEFT;
            		 if(analysis.getxTileToAnalyze()>(gir.getWidth()/Constant.TILESIZE -SumoPlatform.getApplication().getConfiguration().getNumTileBBAnalysis()-1))
            			 sideToAnalyze[1]=BlackBorderAnalysis.ANALYSE_RIGHT;
            		 if(analysis.getyTileToAnalyze()<SumoPlatform.getApplication().getConfiguration().getNumTileBBAnalysis())
            			 sideToAnalyze[2]=BlackBorderAnalysis.ANALYSE_TOP;
            		 if(analysis.getyTileToAnalyze()>(gir.getHeight()/Constant.TILESIZE -SumoPlatform.getApplication().getConfiguration().getNumTileBBAnalysis()-1))
            			 sideToAnalyze[3]=BlackBorderAnalysis.ANALYSE_BOTTOM;

            		 blackBorderAnalysis.analyse(SumoPlatform.getApplication().getConfiguration().getNumTileBBAnalysis(),sideToAnalyze);
            	 }
             }
             //end blackborder analysis
             return blackBorderAnalysis;
		}

		/**
		 *  Exec the analysis process
		 */
		public 	Results call() {
			List<ComplexEditVDSVectorLayer>resultLayers=new ArrayList<>();

			notifyStartProcessListener();
			SarImageReader reader=((SarImageReader)gir);

			String[] thresholdsString=StringUtils.join(analysis.getThresholdsParams(),",").split(",");

			BlackBorderAnalysis blackBorderAnalysis=runBBAnalysis();

             analysis.setBlackBorderAnalysis(blackBorderAnalysis);

             // list of bands
             int numberofbands = gir.getNBand();
             int[] bands = new int[numberofbands];

             // create K distribution
             int noiseFloor=SumoPlatform.getApplication().getConfiguration().getNoiseFloor(0);
             int thresPixelMin=SumoPlatform.getApplication().getConfiguration().getThreshMinPixelValue(500);
             final KDistributionEstimation kdist = new KDistributionEstimation(ENL,noiseFloor,thresPixelMin);

             DetectedPixels mergePixels = new DetectedPixels(reader.getRangeSpacing(),reader.getAzimuthSpacing());
             DetectedPixels banddetectedpixels[]=new DetectedPixels[numberofbands];

             String timeStampStart=reader.getTimeStampStart();
             double azimuth=reader.getAzimuthSpacing();

             int xtileSize = analysis.getRealSizeX();
             int ytileSize = analysis.getRealSizeY();
             
             try{
	             for (int band = 0; band < numberofbands&&!stop; band++) {
	            	 notifyAnalysisBand( new StringBuilder().append("VDS: analyzing band ").append(gir.getBandName(band)).toString());

	            	 int vTiles=analysis.getVerTiles();
	            	 notifyVDSAnalysis("Performing VDS Analysis",vTiles);
	            	 analysis.addProgressListener(this);

	            	 //identify probably target
	            	 banddetectedpixels[band]=analysis.analyse(kdist,band);
	            	 //banddetectedpixels[band]=analysis.getPixels();

	            	 if(numPointLimit!=0&&banddetectedpixels[band].getAllDetectedPixels().size()>numPointLimit){
	            		 logger.warn("Too much points. Stop Image analysis!!!");
	            		 return null;
	            	 }

	            	 if (mergePixels == null) {
	            		mergePixels=banddetectedpixels[band];
	            	}else{
	            		mergePixels.merge(banddetectedpixels[band]);
	            	}

	            	 bands[band] = band;
	            	 String polarization=reader.getBands()[band];

	                 if (numberofbands < 1 || displaybandanalysis) {
	                     notifyAgglomerating( new StringBuilder().append("VDS: agglomerating detections for band ").append(polarization).toString());

	                     Boat[] boats=null;
		            	 //merge pixel to build the "boats"
	                     if (agglomerationMethodology.startsWith("d")) {
	                         // method distance used
	                         banddetectedpixels[band].agglomerate();
	                         banddetectedpixels[band].computeBoatsAttributes(polarization);
	                     } else {
	                         // method neighbours used
	                         if(stop)
	                        	 break;

	                         boats=analysis.agglomerateNeighbours(banddetectedpixels[band],neighbouringDistance, neighbourTilesize,removelandconnectedpixels,
	                         		 (analysis.getCoastMask() != null) ? analysis.getCoastMask() : null, kdist,polarization,band);
	                     }

	                     String layerName=new StringBuilder("VDS analysis ").append(polarization).append(" ").append(analysis.getThresholdParam(polarization)).toString();

	                     ComplexEditVDSVectorLayer vdsanalysisLayer = new ComplexEditVDSVectorLayer(LayerManager.getIstanceManager().getCurrentImageLayer(),layerName,
	                    		 "point", new GeometryImage("VDS Analysis","point",timeStampStart,azimuth, boats),
	                    		 thresholdsString,ENL,buffer,bufferedMaskName,""+band);

	                     vdsanalysisLayer.addDetectedPixels(banddetectedpixels[band].getAllDetectedPixels(), display);

	                     if (!agglomerationMethodology.startsWith("d")) {
	                         vdsanalysisLayer.addThreshAggPixels(banddetectedpixels[band].getThresholdaggregatePixels(), display);
	                         vdsanalysisLayer.addThresholdPixels(banddetectedpixels[band].getThresholdclipPixels(), display);
	                     }

	                     //Azimuth Ambiguities
	                     notifyCalcAzimuth("VDS: looking for azimuth ambiguities...");
	                     AzimuthAmbiguity azimuthAmbiguity = new AzimuthAmbiguity(boats, (SarImageReader) gir,band);
	                     azimuthAmbiguity.process();
	                     List<Geometry> az=azimuthAmbiguity.getAmbiguityboatgeometry();
	                     vdsanalysisLayer.addAzimuthAmbiguities(az, display);

	                     //Azimuth Ambiguities ONLY FOR S1
	                     if(gir instanceof Sentinel1){
	                    	 if(((Sentinel1)gir).getInstumentationMode().equalsIgnoreCase("EW")||((Sentinel1)gir).getInstumentationMode().equalsIgnoreCase("IW")){
			                     notifyCalcAzimuth("VDS: looking for artefacts ambiguities...");
			                     S1ArtefactsAmbiguity arAmbiguity  = new S1ArtefactsAmbiguity(boats, (SarImageReader) gir,band);
			                     arAmbiguity.process();
			                     List<Geometry> artefactsA=arAmbiguity.getAmbiguityboatgeometry();
			                     vdsanalysisLayer.addArtefactsAmbiguities(artefactsA, display);
	                    	 }
	                     }

                         if ((analysis.getCoastMask() != null) ) {
	                        vdsanalysisLayer.addGeometries("bufferedmask", Color.BLUE, 1, GeometryImage.POLYGON,
	                        		analysis.getCoastMask().getMaskGeometries(), true);
	                     }
	                     //leave display params forced to false
	                     vdsanalysisLayer.addGeometries("tiles", new Color(0xFF00FF), 1, 
	                    		 GeometryImage.LINESTRING,
	                    		 GeometryExtractor.getTiles(gir.getWidth(),gir.getHeight(),xtileSize,ytileSize), 
	                    		 false);

	                     notifyLayerReady(vdsanalysisLayer);
	                     resultLayers.add(vdsanalysisLayer);
	                 }
	             }
	             if(stop){
	            	 return null;
	             }

	             // create the merged Layers if we have more than one band
	             if (bands.length > 1) {
	                 notifyAgglomerating("VDS: agglomerating detections...");
	                 Boat[] boats=null;
	                 if (agglomerationMethodology.startsWith("d")) {
	                     // method distance used
	                     mergePixels.agglomerate();
	                     mergePixels.computeBoatsAttributes("merge");
	                 } else {
	                     // method neighbours used
	                	 boats=analysis.agglomerateNeighbours(mergePixels,neighbouringDistance, neighbourTilesize, removelandconnectedpixels, (analysis.getCoastMask() != null)  ? analysis.getCoastMask() : null, kdist,"merge",bands);
	                 }
	                 if(stop){
	                	 return null;
	                 }
	                 //TODO: per ora Merged viene utilizzato per indicare che e' il layer del merge e non delle bande ma VA CAMBIATO!!!
	                 ComplexEditVDSVectorLayer vdsanalysisLayer = new ComplexEditVDSVectorLayer(LayerManager.getIstanceManager().getCurrentImageLayer(),"VDS analysis all bands merged",
	                		 																	"point", new GeometryImage("VDS Analysis","point",timeStampStart,azimuth, boats),
	                		 																	thresholdsString,ENL,buffer,bufferedMaskName,"Merged");
	                 boolean display = SumoPlatform.getApplication().getConfiguration().getDisplayPixel();
	                 if (!agglomerationMethodology.startsWith("d")) {
	                     vdsanalysisLayer.addThreshAggPixels(mergePixels.getThresholdaggregatePixels(), display);
	                     vdsanalysisLayer.addThresholdPixels(mergePixels.getThresholdclipPixels(), display);
	                 }
	                 vdsanalysisLayer.addDetectedPixels(mergePixels.getAllDetectedPixels(), display);

	                 if ((analysis.getCoastMask() != null)) {
	                     vdsanalysisLayer.addGeometries("bufferedmask", new Color(0x0000FF), 1, GeometryImage.POLYGON, analysis.getCoastMask().getMaskGeometries(), true);
	                 }
	                 vdsanalysisLayer.addGeometries("tiles", new Color(0xFF00FF), 1, 
	                		 GeometryImage.LINESTRING,
	                		 GeometryExtractor.getTiles(gir.getWidth(),gir.getHeight(),xtileSize,ytileSize), 
	                		 false);


	               //Azimuth Ambiguities
                     notifyCalcAzimuth("VDS: looking for azimuth ambiguities...");
                     AzimuthAmbiguity azimuthAmbiguity = new AzimuthAmbiguity(boats, (SarImageReader) gir,bands);
                     azimuthAmbiguity.process();
                     List<Geometry> az=azimuthAmbiguity.getAmbiguityboatgeometry();
                     vdsanalysisLayer.addAzimuthAmbiguities(az, display);

                     //Azimuth Ambiguities ONLY FOR S1
                     if(gir instanceof Sentinel1){
                    	 if(((Sentinel1)gir).getInstumentationMode().equalsIgnoreCase("EW")||((Sentinel1)gir).getInstumentationMode().equalsIgnoreCase("IW")){
		                     notifyCalcAzimuth("VDS: looking for artefacts ambiguities...");
		                     S1ArtefactsAmbiguity arAmbiguity  = new S1ArtefactsAmbiguity(boats, (SarImageReader) gir,bands);
		                     arAmbiguity.process();
		                     List<Geometry> artefactsA=arAmbiguity.getAmbiguityboatgeometry();
		                     vdsanalysisLayer.addArtefactsAmbiguities(artefactsA, display);
                    	 }
                     }
	                 notifyLayerReady(vdsanalysisLayer);
	                 resultLayers.add(vdsanalysisLayer);
	             }
	             notifyEndProcessListener();
             }catch(Exception ee){
            	 ee.printStackTrace();
             }finally{
            	 if(this.listeners!=null)
            		 removeAllProcessListener();
             }
             return new Results(reader, resultLayers);
         }


		/**
		 *
		 * @param listener
		 */
		public synchronized void addProcessListener(VDSAnalysisProcessListener listener){
			this.listeners.add(listener);
		}
		/**
		 *
		 * @param listener
		 */
		public synchronized void removeProcessListener(VDSAnalysisProcessListener listener){
			this.listeners.remove(listener);
		}
		public synchronized void removeAllProcessListener(){
			this.listeners.clear();
		}
		public void notifyEndProcessListener(){
			for(VDSAnalysisProcessListener listener:listeners){
				listener.endAnalysis(gir.getImgName());
			}
		}
		public void notifyStartProcessListener(){
			for(VDSAnalysisProcessListener listener:listeners){
				listener.startAnalysis(gir.getImgName());
			}
		}
		public void notifyBBAnalysis(String msg){
			for(VDSAnalysisProcessListener listener:listeners){
				listener.startBlackBorederAnalysis(msg);
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
				listener.nextVDSAnalysisStep("Tile:",step,analysis.getVerTiles());
			}
		}
		@Override
		public void endRowProcesseing(int row) {
		}


		public void dispose(){
	       // removeAllProcessListener();
			analysis=null;
	        blackBorderAnalysis=null;
		}
     }

