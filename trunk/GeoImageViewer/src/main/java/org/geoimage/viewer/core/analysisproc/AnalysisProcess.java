package org.geoimage.viewer.core.analysisproc;

import static org.geoimage.viewer.util.Constant.PREF_AGGLOMERATION_METHODOLOGY;
import static org.geoimage.viewer.util.Constant.PREF_DISPLAY_BANDS;
import static org.geoimage.viewer.util.Constant.PREF_DISPLAY_PIXELS;
import static org.geoimage.viewer.util.Constant.PREF_REMOVE_LANDCONNECTEDPIXELS;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_COLOR_BAND_0;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_COLOR_BAND_1;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_COLOR_BAND_2;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_COLOR_BAND_3;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_COLOR_BAND_MERGED;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SIZE_BAND_0;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SIZE_BAND_1;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SIZE_BAND_2;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SIZE_BAND_3;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SIZE_BAND_MERGED;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SYMBOL_BAND_0;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SYMBOL_BAND_1;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SYMBOL_BAND_2;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SYMBOL_BAND_3;
import static org.geoimage.viewer.util.Constant.PREF_TARGETS_SYMBOL_BAND_MERGED;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
import org.geoimage.viewer.core.api.GeometricLayer;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.layers.vectors.ComplexEditVDSVectorLayer;
import org.geoimage.viewer.core.layers.vectors.MaskVectorLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * 
 * @author argenpo
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
		
		public boolean isStop() {
			return stop;
		}

		public void setStop(boolean stop) {
			this.stop = stop;
		}

		private List<VDSAnalysisProcessListener> listeners; 
		
		
		public List<ComplexEditVDSVectorLayer> getResultLayers() {
			return resultLayers;
		}

		public void setResultLayers(List<ComplexEditVDSVectorLayer> resultLayers) {
			this.resultLayers = resultLayers;
		}

		public AnalysisProcess(GeoImageReader gir,float ENL,VDSAnalysis analysis,IMask[] bufferedMask,String[] thresholds,int buffer,BlackBorderAnalysis blackBorderAnalysis) {
			this.ENL=ENL;
			this.analysis=analysis;
			this.bufferedMask=bufferedMask;
			this.blackBorderAnalysis=blackBorderAnalysis;
			this.thresholds=thresholds;
			this.buffer=buffer;
			this.resultLayers=new ArrayList<ComplexEditVDSVectorLayer>();
			listeners=new ArrayList<VDSAnalysisProcessListener>();
			this.gir=gir.clone();
		}
   
		
		public void run() {
			notifyStartProcessListener();
			
			//run the black border analysis 
             if(blackBorderAnalysis!=null){
            	 blackBorderAnalysis.analyse(5);
             }	 
             
             // create K distribution
             KDistributionEstimation kdist = new KDistributionEstimation(ENL);
             DetectedPixels pixels = new DetectedPixels((SarImageReader) gir);
             
             // list of bands
             int numberofbands = gir.getNBand();
             int[] bands = new int[numberofbands];
             
             AzimuthAmbiguity azimuthAmbiguity =null;
             
             // compute detections for each band separately
             for (int band = 0; band < numberofbands&&!stop; band++) {
            	 
            	 String trheshString=thresholds[getPolIdx(gir.getBandName(band))];
                 bands[band] = band;
                 
                 String timeStampStart=((SarImageReader)gir).getTimeStampStart();
                 double azimuth=((SarImageReader)gir).getAzimuthSpacing();
                 
                 notifyAnalysisBand( "VDS: analyzing band "+gir.getBandName(band));
                 
                 analysis.run(kdist,blackBorderAnalysis,band);
                 DetectedPixels banddetectedpixels = analysis.getPixels();
                 
                 if (pixels == null) {
                     return;
                 }else{
                 	pixels.merge(banddetectedpixels);
                 }	
                
                 
                 boolean displaybandanalysis = Platform.getPreferences().readRow(PREF_DISPLAY_BANDS).equalsIgnoreCase("true");
                 if (numberofbands < 1 || displaybandanalysis) {
                     
                     notifyAgglomerating( "VDS: agglomerating detections for band "+gir.getBandName(band));

                     
                     String agglomerationMethodology = (Platform.getPreferences()).readRow(PREF_AGGLOMERATION_METHODOLOGY);
                     if (agglomerationMethodology.startsWith("d")) {
                         // method distance used
                         banddetectedpixels.agglomerate();
                         banddetectedpixels.computeBoatsAttributes();
                     } else {
                         // method neighbours used
                         double neighbouringDistance=Platform.getPreferences().getNeighbourDistance(1.0);
                         int tilesize=Platform.getPreferences().getTileSize(200);
                         boolean removelandconnectedpixels = (Platform.getPreferences().readRow(PREF_REMOVE_LANDCONNECTEDPIXELS)).equalsIgnoreCase("true");
                         
                         if(stop)
                        	 break;
                         banddetectedpixels.agglomerateNeighbours(neighbouringDistance, tilesize, 
                        		 removelandconnectedpixels, 
                        		 new int[]{band}, 
                        		 (bufferedMask != null) && (bufferedMask.length != 0) ? bufferedMask[0] : null, kdist);
                        
                     }
                     
                     if (!gir.supportAzimuthAmbiguity()) {
                         System.out.println("\nSatellite sensor not supported for Azimuth Ambiguity detection");
                     }else{
                    	 notifyCalcAzimuth("VDS: looking for azimuth ambiguities...");
                         azimuthAmbiguity = new AzimuthAmbiguity(banddetectedpixels.getBoats(), (SarImageReader) gir);
                     }    
                     
                     String layerName=new StringBuilder("VDS analysis ").append(gir.getBandName(band)).append(" ").append(trheshString).toString();
                     
                     
                     String name="";
                     if(bufferedMask!=null && bufferedMask.length>0){
                    	name=bufferedMask[0].getName(); 
                     }
                     
                     
                     ComplexEditVDSVectorLayer vdsanalysis = new ComplexEditVDSVectorLayer(Platform.getCurrentImageLayer(),layerName, 
                    		 				"point", createGeometricLayer(timeStampStart,azimuth, banddetectedpixels),
                    		 				thresholds,ENL,buffer,name);
                 
                     boolean display = Platform.getPreferences().readRow(PREF_DISPLAY_PIXELS).equalsIgnoreCase("true");
                     if (!agglomerationMethodology.startsWith("d")) {
                         vdsanalysis.addGeometries("thresholdaggregatepixels", new Color(0x0000FF), 1, MaskVectorLayer.POINT, banddetectedpixels.getThresholdaggregatePixels(), display);
                         vdsanalysis.addGeometries("thresholdclippixels", new Color(0x00FFFF), 1, MaskVectorLayer.POINT, banddetectedpixels.getThresholdclipPixels(), display);
                     }
                     vdsanalysis.addGeometries("detectedpixels", new Color(0x00FF00), 1, MaskVectorLayer.POINT, banddetectedpixels.getAllDetectedPixels(), display);
                     vdsanalysis.addGeometries("azimuthambiguities", new Color(0xFFD000), 5, MaskVectorLayer.POINT, azimuthAmbiguity.getAmbiguityboatgeometry(), display);
                     if ((bufferedMask != null) && (bufferedMask.length > 0)) {
                         vdsanalysis.addGeometries("bufferedmask", new Color(0x0000FF), 1, MaskVectorLayer.POLYGON, bufferedMask[0].getGeometries(), display);
                     }
                     vdsanalysis.addGeometries("tiles", new Color(0xFF00FF), 1, MaskVectorLayer.LINESTRING, GeometryExtractor.getTiles(gir.getWidth(),gir.getHeight(),analysis.getTileSize()), false);
                     // set the color and symbol values for the VDS layer
                    
                	 String colorString = "";
                     String widthstring = "";
                     String symbolString = "";
                     if (band == 0) {
                         widthstring = Platform.getPreferences().readRow(PREF_TARGETS_SIZE_BAND_0);
                         colorString = Platform.getPreferences().readRow(PREF_TARGETS_COLOR_BAND_0);
                         symbolString = Platform.getPreferences().readRow(PREF_TARGETS_SYMBOL_BAND_0);
                     }
                     if (band == 1) {
                         widthstring = Platform.getPreferences().readRow(PREF_TARGETS_SIZE_BAND_1);
                         colorString = Platform.getPreferences().readRow(PREF_TARGETS_COLOR_BAND_1);
                         symbolString = Platform.getPreferences().readRow(PREF_TARGETS_SYMBOL_BAND_1);
                     }
                     if (band == 2) {
                         widthstring = Platform.getPreferences().readRow(PREF_TARGETS_SIZE_BAND_2);
                         colorString = Platform.getPreferences().readRow(PREF_TARGETS_COLOR_BAND_2);
                         symbolString = Platform.getPreferences().readRow(PREF_TARGETS_SYMBOL_BAND_2);
                     }
                     if (band == 3) {
                         widthstring = Platform.getPreferences().readRow(PREF_TARGETS_SIZE_BAND_3);
                         colorString = Platform.getPreferences().readRow(PREF_TARGETS_COLOR_BAND_3);
                         symbolString = Platform.getPreferences().readRow(PREF_TARGETS_SYMBOL_BAND_3);
                     }
                     
                     try {    
                         int displaywidth = Integer.parseInt(widthstring);
                         vdsanalysis.setWidth(displaywidth);
                     } catch (NumberFormatException e) {
                         vdsanalysis.setWidth(1);
                     }
                     try {
                         Color colordisplay = new Color(Integer.decode(colorString));
                         vdsanalysis.setColor(colordisplay);
                     } catch (NumberFormatException e) {
                         vdsanalysis.setColor(new Color(0x0000FF));
                     }
                     try {
                         vdsanalysis.setDisplaysymbol(MaskVectorLayer.symbol.valueOf(symbolString));
                     } catch (EnumConstantNotPresentException e) {
                         vdsanalysis.setDisplaysymbol(MaskVectorLayer.symbol.square);
                     }
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
                 
                 
                 String agglomerationMethodology = (Platform.getPreferences()).readRow(PREF_AGGLOMERATION_METHODOLOGY);
                 if (agglomerationMethodology.startsWith("d")) {
                     // method distance used
                     pixels.agglomerate();
                     pixels.computeBoatsAttributes();
                 } else {
                     // method neighbours used
                     double neighbouringDistance=Platform.getPreferences().getNeighbourDistance(1.0);
                     int tilesize=Platform.getPreferences().getTileSize(200);

                     boolean removelandconnectedpixels = (Platform.getPreferences().readRow(PREF_REMOVE_LANDCONNECTEDPIXELS)).equalsIgnoreCase("true");
                     pixels.agglomerateNeighbours(neighbouringDistance, tilesize, removelandconnectedpixels, bands, (bufferedMask != null) && (bufferedMask.length != 0) ? bufferedMask[0] : null, kdist);
                 }

                 if (!gir.supportAzimuthAmbiguity()) {
                     System.out.println("\nSatellite sensor not supported for Azimuth Ambiguity detection");
                 }else{
                	 notifyCalcAzimuth("VDS: looking for azimuth ambiguities...");
                	 azimuthAmbiguity = new AzimuthAmbiguity(pixels.getBoats(), (SarImageReader)gir);// GeoImageReaderFactory.createReaderForName(gir.getFilesList()[0]).get(0));
                 }
                 
                 if(stop){
                     stop();
                	 return;
                 }
                 String name="";
                 if ((bufferedMask != null) && (bufferedMask.length > 0)) {
                	 name=bufferedMask[0].getName();
                 }
                 String t=((SarImageReader)gir).getTimeStampStart();
                 double azimuth=((SarImageReader)gir).getAzimuthSpacing();
                 
                 ComplexEditVDSVectorLayer vdsanalysisLayer = new ComplexEditVDSVectorLayer(Platform.getCurrentImageLayer(),"VDS analysis all bands merged", 
                		 																	"point", createGeometricLayer(t,azimuth, pixels),
                		 																	thresholds,ENL,buffer,name);
                 boolean display = Platform.getPreferences().readRow(PREF_DISPLAY_PIXELS).equalsIgnoreCase("true");
                 if (!agglomerationMethodology.startsWith("d")) {
                     vdsanalysisLayer.addGeometries("thresholdaggregatepixels", new Color(0x0000FF), 1, MaskVectorLayer.POINT, pixels.getThresholdaggregatePixels(), display);
                     vdsanalysisLayer.addGeometries("thresholdclippixels", new Color(0x00FFFF), 1, MaskVectorLayer.POINT, pixels.getThresholdclipPixels(), display);
                 }
                 vdsanalysisLayer.addGeometries("detectedpixels", new Color(0x00FF00), 1, MaskVectorLayer.POINT, pixels.getAllDetectedPixels(), display);
                 vdsanalysisLayer.addGeometries("azimuthambiguities", new Color(0xFFD000), 5, MaskVectorLayer.POINT, azimuthAmbiguity.getAmbiguityboatgeometry(), display);
                 if ((bufferedMask != null) && (bufferedMask.length > 0)) {
                     vdsanalysisLayer.addGeometries("bufferedmask", new Color(0x0000FF), 1, MaskVectorLayer.POLYGON, bufferedMask[0].getGeometries(), display);
                 }
                 vdsanalysisLayer.addGeometries("tiles", new Color(0xFF00FF), 1, MaskVectorLayer.LINESTRING,GeometryExtractor.getTiles(gir.getWidth(),gir.getHeight(),analysis.getTileSize()), false);
                 // set the color and symbol values for the VDS layer
                 try {
                     String widthstring = Platform.getPreferences().readRow(PREF_TARGETS_SIZE_BAND_MERGED);
                     int displaywidth = Integer.parseInt(widthstring);
                     vdsanalysisLayer.setWidth(displaywidth);
                 } catch (NumberFormatException e) {
                     vdsanalysisLayer.setWidth(1);
                 }
                 try {
                     String colorString = Platform.getPreferences().readRow(PREF_TARGETS_COLOR_BAND_MERGED);
                     Color colordisplay = new Color(Integer.decode(colorString));
                     vdsanalysisLayer.setColor(colordisplay);
                 } catch (NumberFormatException e) {
                     vdsanalysisLayer.setColor(new Color(0xFFAA00));
                 }
                 try {
                     String symbolString = Platform.getPreferences().readRow(PREF_TARGETS_SYMBOL_BAND_MERGED);
                     vdsanalysisLayer.setDisplaysymbol(MaskVectorLayer.symbol.valueOf(symbolString));
                 } catch (EnumConstantNotPresentException e) {
                     vdsanalysisLayer.setDisplaysymbol(MaskVectorLayer.symbol.square);
                 }

                 //if(!Platform.isBatchMode())
                 //	 Platform.getLayerManager().addLayer(vdsanalysisLayer);
                 notifyLayerReady(vdsanalysisLayer);
                 resultLayers.add(vdsanalysisLayer);
             }
             stop();
         }
		
		private void stop(){
           notifyEndProcessListener();
           removeAllProcessListener();
		}
		
		
		public static GeometricLayer createGeometricLayer(String timeStampStart,double azimuth, DetectedPixels pixels) {
	        GeometricLayer out = new GeometricLayer("point");
	        out.setName("VDS Analysis");
	        GeometryFactory gf = new GeometryFactory();
	        long runid = System.currentTimeMillis();
	        int count=0;
	        for (double[] boat : pixels.getBoats()) {
	            Attributes atts = Attributes.createAttributes(VDSSchema.schema, VDSSchema.types);
	            atts.set(VDSSchema.ID, count++);
	            atts.set(VDSSchema.MAXIMUM_VALUE, boat[3]);
	            atts.set(VDSSchema.TILE_AVERAGE, boat[4]);
	            atts.set(VDSSchema.TILE_STANDARD_DEVIATION, boat[5]);
	            atts.set(VDSSchema.THRESHOLD, boat[6]);
	            atts.set(VDSSchema.RUN_ID, runid + "");
	            atts.set(VDSSchema.NUMBER_OF_AGGREGATED_PIXELS, boat[7]);
	            atts.set(VDSSchema.ESTIMATED_LENGTH, boat[8]);
	            atts.set(VDSSchema.ESTIMATED_WIDTH, boat[9]);
	            atts.set(VDSSchema.SIGNIFICANCE, (boat[3] - boat[4]) / (boat[4] * boat[5]));
	            timeStampStart=timeStampStart.replace("Z", "");
	            atts.set(VDSSchema.DATE, Timestamp.valueOf(timeStampStart));
	            atts.set(VDSSchema.VS, 0);
	            //compute the direction of the vessel considering the azimuth of the image
	            //result is between 0 and 180 degree
	            double degree = boat[10] + 90 + azimuth;
	            if (degree > 180) {
	                degree = degree - 180;
	            }
	         
	            atts.set(VDSSchema.ESTIMATED_HEADING, degree);
	            out.put(gf.createPoint(new Coordinate(boat[1], boat[2])), atts);
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

