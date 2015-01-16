/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import static org.geoimage.viewer.util.Constant.*;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.math3.util.Precision;
import org.geoimage.analysis.AzimuthAmbiguity;
import org.geoimage.analysis.DetectedPixels;
import org.geoimage.analysis.KDistributionEstimation;
import org.geoimage.analysis.VDSAnalysis;
import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.GeoMetadata;
import org.geoimage.def.SarImageReader;
import org.geoimage.impl.ENL;
import org.geoimage.impl.TiledBufferedImage;
import org.geoimage.utils.IMask;
import org.geoimage.utils.IProgress;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.GeometricLayer;
import org.geoimage.viewer.core.api.IConsoleAction;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.layers.vectors.ComplexEditVDSVectorLayer;
import org.geoimage.viewer.core.layers.vectors.MaskVectorLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 *
 * @author thoorfr
 */
public class VDSAnalysisConsoleAction implements IConsoleAction, IProgress {
	
	/**
	 * 
	 * @author argenpo
	 *
	 */
	public  class AnalysisProcess implements Runnable {
			private float ENL;
			private VDSAnalysis analysis;
			private IMask[] bufferedMask;
			private String[] thresholds;
			private int buffer;
			private List<ComplexEditVDSVectorLayer>resultLayers;
			
			public List<ComplexEditVDSVectorLayer> getResultLayers() {
				return resultLayers;
			}

			public void setResultLayers(List<ComplexEditVDSVectorLayer> resultLayers) {
				this.resultLayers = resultLayers;
			}

			public AnalysisProcess(float ENL,VDSAnalysis analysis,IMask[] bufferedMask,String[] thresholds,int buffer) {
				this.ENL=ENL;
				this.analysis=analysis;
				this.bufferedMask=bufferedMask;
				this.thresholds=thresholds;
				this.buffer=buffer;
				this.resultLayers=new ArrayList<ComplexEditVDSVectorLayer>();
			}
       
			public void run() {

                 running = false;

                 // create K distribution
                 KDistributionEstimation kdist = new KDistributionEstimation(ENL);

                 DetectedPixels pixels = new DetectedPixels((SarImageReader) gir);
                 // list of bands
                 int numberofbands = gir.getNBand();
                 int[] bands = new int[numberofbands];
                 
                 message=new StringBuilder();
                 // compute detections for each band separately
                 for (int band = 0; band < numberofbands; band++) {
                     gir.setBand(band);
                     bands[band] = band;
                     
                     message.setLength(0);
                     message =  message.append("VDS: analysing image...");
                     if (numberofbands > 1) {
                         message = message.append(" for band ").append(gir.getBandName(band));
                     }
                     setCurrent(2);
                     analysis.run(kdist);
                     DetectedPixels banddetectedpixels = analysis.getPixels();
                     
                     if (pixels == null) {
                         done = true;
                         return;
                     }else{
                     	pixels.merge(banddetectedpixels);
                     }	
                    
                     boolean displaybandanalysis = Platform.getPreferences().readRow(PREF_DISPLAY_BANDS).equalsIgnoreCase("true");
                     if (numberofbands < 1 || displaybandanalysis) {
                    	 message.setLength(0);
                         message =  message.append("VDS: agglomerating detections for band ").append(gir.getBandName(band)).append("...");
                         
                         setCurrent(3);

                         String agglomerationMethodology = (Platform.getPreferences()).readRow(PREF_AGGLOMERATION_METHODOLOGY);
                         if (agglomerationMethodology.startsWith("d")) {
                             // method distance used
                             banddetectedpixels.agglomerate();
                             banddetectedpixels.computeBoatsAttributes();
                         } else {
                             // method neighbours used
                             double neighbouringDistance;
                             try {
                                 neighbouringDistance = Double.parseDouble((Platform.getPreferences()).readRow(PREF_NEIGHBOUR_DISTANCE));
                             } catch (NumberFormatException e) {
                                 neighbouringDistance = 1.0;
                             }
                             int tilesize;
                             try {
                                 tilesize = Integer.parseInt((Platform.getPreferences()).readRow(PREF_NEIGHBOUR_TILESIZE));
                             } catch (NumberFormatException e) {
                                 tilesize = 200;
                             }
                             boolean removelandconnectedpixels = (Platform.getPreferences().readRow(PREF_REMOVE_LANDCONNECTEDPIXELS)).equalsIgnoreCase("true");
                             banddetectedpixels.agglomerateNeighbours(neighbouringDistance, tilesize, 
                            		 removelandconnectedpixels, 
                            		 new int[]{band}, 
                            		 (bufferedMask != null) && (bufferedMask.length != 0) ? bufferedMask[0] : null, kdist);
                         }
                         message.setLength(0);
                         // look for Azimuth ambiguities in the pixels
                         message =message.append("VDS: looking for azimuth ambiguities...");
                         
                         setCurrent(4);
                         
                         AzimuthAmbiguity azimuthAmbiguity = new AzimuthAmbiguity(banddetectedpixels.getBoats(), (SarImageReader) gir);

                         String layerName=new StringBuilder("VDS analysis ").append(gir.getBandName(band)).append(" ").append(thresholds[band]).toString();
                         
                         ComplexEditVDSVectorLayer vdsanalysis = new ComplexEditVDSVectorLayer(layerName, 
                        		 				(SarImageReader) gir, "point", 
                        		 				createGeometricLayer(gir, banddetectedpixels),
                        		 				thresholds,ENL,buffer,bufferedMask[0].getName());
                     
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
                         vdsanalysis.addGeometries("tiles", new Color(0xFF00FF), 1, MaskVectorLayer.LINESTRING, analysis.getTiles(), false);
                         // set the color and symbol values for the VDS layer
                         try {
                             String widthstring = "";
                             if (band == 0) {
                                 widthstring = Platform.getPreferences().readRow(PREF_TARGETS_SIZE_BAND_0);
                             }
                             if (band == 1) {
                                 widthstring = Platform.getPreferences().readRow(PREF_TARGETS_SIZE_BAND_1);
                             }
                             if (band == 2) {
                                 widthstring = Platform.getPreferences().readRow(PREF_TARGETS_SIZE_BAND_2);
                             }
                             if (band == 3) {
                                 widthstring = Platform.getPreferences().readRow(PREF_TARGETS_SIZE_BAND_3);
                             }
                             int displaywidth = Integer.parseInt(widthstring);
                             vdsanalysis.setWidth(displaywidth);
                         } catch (NumberFormatException e) {
                             vdsanalysis.setWidth(1);
                         }
                         try {
                             String colorString = "";
                             if (band == 0) {
                                 colorString = Platform.getPreferences().readRow(PREF_TARGETS_COLOR_BAND_0);
                             }
                             if (band == 1) {
                                 colorString = Platform.getPreferences().readRow(PREF_TARGETS_COLOR_BAND_1);
                             }
                             if (band == 2) {
                                 colorString = Platform.getPreferences().readRow(PREF_TARGETS_COLOR_BAND_2);
                             }
                             if (band == 3) {
                                 colorString = Platform.getPreferences().readRow(PREF_TARGETS_COLOR_BAND_3);
                             }
                             Color colordisplay = new Color(Integer.decode(colorString));
                             vdsanalysis.setColor(colordisplay);
                         } catch (NumberFormatException e) {
                             vdsanalysis.setColor(new Color(0x0000FF));
                         }
                         try {
                             String symbolString = "";
                             if (band == 0) {
                                 symbolString = Platform.getPreferences().readRow(PREF_TARGETS_SYMBOL_BAND_0);
                             }
                             if (band == 1) {
                                 symbolString = Platform.getPreferences().readRow(PREF_TARGETS_SYMBOL_BAND_1);
                             }
                             if (band == 2) {
                                 symbolString = Platform.getPreferences().readRow(PREF_TARGETS_SYMBOL_BAND_2);
                             }
                             if (band == 3) {
                                 symbolString = Platform.getPreferences().readRow(PREF_TARGETS_SYMBOL_BAND_3);
                             }
                             vdsanalysis.setDisplaysymbol(MaskVectorLayer.symbol.valueOf(symbolString));
                         } catch (EnumConstantNotPresentException e) {
                             vdsanalysis.setDisplaysymbol(MaskVectorLayer.symbol.square);
                         }
                         if(il!=null)
                        	 il.addLayer(vdsanalysis);
                         resultLayers.add(vdsanalysis);
                     }
                 }

                 // display merged results if there is more than one band
                 if (bands.length > 1) {
                     message =  new StringBuilder("VDS: agglomerating detections...");
                     setCurrent(3);

                     String agglomerationMethodology = (Platform.getPreferences()).readRow(PREF_AGGLOMERATION_METHODOLOGY);
                     if (agglomerationMethodology.startsWith("d")) {
                         // method distance used
                         pixels.agglomerate();
                         pixels.computeBoatsAttributes();
                     } else {
                         // method neighbours used
                         double neighbouringDistance;
                         try {
                             neighbouringDistance = Double.parseDouble((Platform.getPreferences()).readRow(PREF_NEIGHBOUR_DISTANCE));
                         } catch (NumberFormatException e) {
                             neighbouringDistance = 1.0;
                         }
                         int tilesize;
                         try {
                             tilesize = Integer.parseInt((Platform.getPreferences()).readRow(PREF_NEIGHBOUR_TILESIZE));
                         } catch (NumberFormatException e) {
                             tilesize = 200;
                         }
                         boolean removelandconnectedpixels = (Platform.getPreferences().readRow(PREF_REMOVE_LANDCONNECTEDPIXELS)).equalsIgnoreCase("true");
                         pixels.agglomerateNeighbours(neighbouringDistance, tilesize, removelandconnectedpixels, bands, (bufferedMask != null) && (bufferedMask.length != 0) ? bufferedMask[0] : null, kdist);
                     }

                     // look for Azimuth ambiguities in the pixels
                     AzimuthAmbiguity azimuthAmbiguity = new AzimuthAmbiguity(pixels.getBoats(), (SarImageReader)gir);// GeoImageReaderFactory.createReaderForName(gir.getFilesList()[0]).get(0));

                     ComplexEditVDSVectorLayer vdsanalysisLayer = new ComplexEditVDSVectorLayer("VDS analysis all bands merged", 
                    		 																	gir, "point", createGeometricLayer(gir, pixels),
                    		 																	thresholds,ENL,buffer,bufferedMask[0].getName());
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
                     vdsanalysisLayer.addGeometries("tiles", new Color(0xFF00FF), 1, MaskVectorLayer.LINESTRING, analysis.getTiles(), false);
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
                     if(il!=null)
                    	 il.addLayer(vdsanalysisLayer);
                     resultLayers.add(vdsanalysisLayer);
                 }
                 done = true;
             }
         }
	
	
	
	

    private StringBuilder message = new StringBuilder("starting VDS Analysis...");
    private int current = 0;
    private int maximum = 3;
    private boolean done = false;
    private boolean indeterminate;
    @SuppressWarnings("unused")
	private boolean running = false;
    private GeoImageReader gir = null;
    private IImageLayer il = null;
    private List<IMask> mask = null;
   
    public VDSAnalysisConsoleAction() {
        
    }

    public String getName() {
        return "vds";
    }

    public String getDescription() {
        return "Compute a VDS (Vessel Detection System) analysis.\n"
                + "Use \"vds k-dist 1.5 GSHHS\" to run a analysis with k-distribuion clutter model with a threshold of 1.5 using the land mask \"GSHHS...\"";
    }

    /**
     * run the analysis called from ActionDialog
     */
    public boolean execute(String[] args) {
        // initialise the buffering distance value
        int bufferingDistance = Double.valueOf((Platform.getPreferences()).readRow(PREF_BUFFERING_DISTANCE)).intValue();

        if (args.length < 2) {
            return true;
        } else {

            if (args[0].equals("k-dist")) {

                done = false;
                message = new StringBuilder("VDS: initialising parameters...");
                setCurrent(1);

                
                IImageLayer cl=Platform.getCurrentImageLayer();
                GeoImageReader reader = ((IImageLayer) cl).getImageReader();
                if (reader instanceof SarImageReader || reader instanceof TiledBufferedImage) {
                    gir = reader;
                    il = (IImageLayer) cl;
                }
                if (gir == null) {
                    done = true;
                    return false;
                }

                //this part mange the different thresholds for different bands
                //in particular is also looking for which band is available and leave the threshold to 0 for the not available bands
                float thrHH = 0;
                float thrHV = 0;
                float thrVH = 0;
                float thrVV = 0;
                
                int numberofbands = gir.getNBand();
                for (int bb = 0; bb < numberofbands; bb++) {
                    if (gir.getBandName(bb).equals("HH") || gir.getBandName(bb).equals("H/H")) {
                        thrHH = Float.parseFloat(args[bb + 1]);
                    } else if (gir.getBandName(bb).equals("HV") || gir.getBandName(bb).equals("H/V")) {
                        thrHV = Float.parseFloat(args[bb + 1]);
                    } else if (gir.getBandName(bb).equals("VH") || gir.getBandName(bb).equals("V/H")) {
                        thrVH = Float.parseFloat(args[bb + 1]);
                    } else if (gir.getBandName(bb).equals("VV") || gir.getBandName(bb).equals("V/V")) {
                        thrVV = Float.parseFloat(args[bb + 1]);
                    }
                }
                final float thresholdHH = thrHH;
                final float thresholdHV = thrHV;
                final float thresholdVH = thrVH;
                final float thresholdVV = thrVV;

                //read the land mask
                mask = new ArrayList<IMask>();
                //if (args.length > 5) {
                for (ILayer l : il.getLayers()) {
                    if (l instanceof IMask & l.getName().startsWith(args[numberofbands + 1])) {
                        mask.add((IMask) l);
                    }
                }

                //read the buffer distance
                bufferingDistance = Integer.parseInt(args[numberofbands + 2]);
                final float ENL = Float.parseFloat(args[numberofbands + 3]);

                // create new buffered mask with bufferingDistance using the mask in parameters
                final IMask[] bufferedMask = new IMask[mask.size()];
                
                for (int i=0;i<mask.size();i++) {
                	IMask maskList = mask.get(i);
               		bufferedMask[i]=FactoryLayer.createMaskLayer(maskList.getName(), maskList.getType(), bufferingDistance,reader, ((MaskVectorLayer)maskList).getGeometriclayer());
                }
                
                final VDSAnalysis analysis = new VDSAnalysis((SarImageReader) gir, bufferedMask, ENL, thresholdHH, thresholdHV, thresholdVH, thresholdVV, this);
                
                final String[] thresholds = new String[numberofbands];
                //management of the strings added at the end of the layer name in order to remember the used threshold
                for (int bb = 0; bb < numberofbands; bb++) {
                    if (gir.getBandName(bb).equals("HH") || gir.getBandName(bb).equals("H/H")) {
                        thresholds[bb] = "" + thresholdHH;
                    } else if (gir.getBandName(bb).equals("HV") || gir.getBandName(bb).equals("H/V")) {
                        thresholds[bb] = "" + thresholdHV;
                    } else if (gir.getBandName(bb).equals("VH") || gir.getBandName(bb).equals("V/H")) {
                        thresholds[bb] = "" + thresholdVH;
                    } else if (gir.getBandName(bb).equals("VV") || gir.getBandName(bb).equals("V/V")) {
                        thresholds[bb] = "" + thresholdVV;
                    }
                }
                
                Thread t=new Thread(new AnalysisProcess(ENL, analysis, bufferedMask, thresholds,bufferingDistance));
                t.start();
            }

            return true;
        }
    }
    
    
    /**
     * 
     * @param ENL
     * @param analysis
     * @param bufferedMask
     * @param thresholds
     * @return
     */
    public List<ComplexEditVDSVectorLayer> runBatchAnalysis(GeoImageReader reader,float ENL, VDSAnalysis analysis,IMask[] bufferedMask, String[] thresholds,int buffer){
    	this.gir=reader;
    	AnalysisProcess ap=new AnalysisProcess(ENL,analysis, bufferedMask, thresholds, buffer);
        ap.run();
        return ap.resultLayers;
    }
    
    
    
    public static GeometricLayer createGeometricLayer(GeoImageReader gir, DetectedPixels pixels) {
        GeometricLayer out = new GeometricLayer("point");
        out.setName("VDS Analysis");
        GeometryFactory gf = new GeometryFactory();
        long runid = System.currentTimeMillis();
        int count=0;
        for (double[] boat : pixels.getBoats()) {

            //String[] schema = VDSSchema.getSchema();
            //String[] types = VDSSchema.getTypes();

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
            String t=(String)gir.getMetadata(GeoMetadata.TIMESTAMP_START);
            t=t.replace("Z", "");
            atts.set(VDSSchema.DATE, Timestamp.valueOf(t));
            atts.set(VDSSchema.VS, 0);
            //compute the direction of the vessel considering the azimuth of the image
            //result is between 0 and 180 degree
            double azimuth = gir.getImageAzimuth();
            double degree = boat[10] + 90 + azimuth;
            if (degree > 180) {
                degree = degree - 180;
            }
         
            atts.set(VDSSchema.ESTIMATED_HEADING, degree);
            out.put(gf.createPoint(new Coordinate(boat[1], boat[2])), atts);
        }
        return out;
    }
    /**
 *
 * @param gir
 * @param pixels
 * @param runid
 * @return
 */
    public static GeometricLayer createGeometricLayer(GeoImageReader gir, DetectedPixels pixels, long runid) {
        GeometricLayer out = new GeometricLayer("point");
        out.setName("VDS Analysis");
        GeometryFactory gf = new GeometryFactory();
        int count=0;
        for (double[] boat : pixels.getBoats()) {

           // String[] schema = VDSSchema.getSchema();
           // String[] types = VDSSchema.getTypes();

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
            atts.set(VDSSchema.SIGNIFICANCE, (boat[3]-boat[4])/(boat[4]*boat[5]));
            try{
                atts.set(VDSSchema.DATE,Timestamp.valueOf(""+gir.getMetadata(GeoMetadata.TIMESTAMP_START)));
            }catch (java.lang.IllegalArgumentException iae){
               //stores directly timestamp
               atts.set(VDSSchema.DATE,gir.getMetadata(GeoMetadata.TIMESTAMP_START));
            }

            atts.set(VDSSchema.SIGNIFICANCE, (boat[3] - boat[4]) / (boat[4] * boat[5]));
            atts.set(VDSSchema.DATE, Timestamp.valueOf("" + gir.getMetadata(GeoMetadata.TIMESTAMP_START)));
            atts.set(VDSSchema.VS, 0);
            //compute the direction of the vessel considering the azimuth of the image
            //result is between 0 and 180 degree
            double azimuth = gir.getImageAzimuth();
            double degree = boat[10] + 90 + azimuth;
            if (degree > 180) {
                degree = degree - 180;
            }
        
            atts.set(VDSSchema.ESTIMATED_HEADING, degree);
            out.put(gf.createPoint(new Coordinate(boat[1], boat[2])), atts);
        }

        return out;
    }
/**
 *
 * @return
 */
    public String getPath() {
        return "Analysis/VDS";
    }

    public List<Argument> getArgumentTypes() {
        Argument a1 = new Argument("algorithm", Argument.STRING, false, "k-dist");
        a1.setPossibleValues(new Object[]{"k-dist"});
        Argument a2 = new Argument("thresholdHH", Argument.FLOAT, false, 1.5);
        Argument a21 = new Argument("thresholdHV", Argument.FLOAT, false, 1.2);
        Argument a22 = new Argument("thresholdVH", Argument.FLOAT, false, 1.5);
        Argument a23 = new Argument("thresholdVV", Argument.FLOAT, false, 1.5);

        Argument a3 = new Argument("mask", Argument.STRING, true, "no mask choosen");
        ArrayList<String> vectors = new ArrayList<String>();
        IImageLayer il=Platform.getCurrentImageLayer();
        /*
        for (ILayer l : Platform.getLayerManager().getLayers()) {
            if (l.isActive() && l instanceof IImageLayer) {
                il = (IImageLayer) l;
                break;
            }
        }*/

        if (il != null) {
            for (ILayer l : il.getLayers()) {
                if (l instanceof MaskVectorLayer && !((MaskVectorLayer) l).getType().equals(MaskVectorLayer.POINT)) {
                    vectors.add(l.getName());
                }
            }
        }
        a3.setPossibleValues(vectors.toArray());
        Vector<Argument> out = new Vector<Argument>();

        Argument a4 = new Argument("Buffer (pixels)", Argument.FLOAT, false, (Platform.getPreferences()).readRow(PREF_BUFFERING_DISTANCE));

        //management of the different threshold in the VDS parameters panel
        out.add(a1);
        int numberofbands = il.getImageReader().getNBand();
        for (int bb = 0; bb < numberofbands; bb++) {
            if (il.getImageReader().getBandName(bb).equals("HH") || il.getImageReader().getBandName(bb).equals("H/H")) {
                out.add(a2);
            } else if (il.getImageReader().getBandName(bb).equals("HV") || il.getImageReader().getBandName(bb).equals("H/V")) {
                out.add(a21);
            } else if (il.getImageReader().getBandName(bb).equals("VH") || il.getImageReader().getBandName(bb).equals("V/H")) {
                out.add(a22);
            } else if (il.getImageReader().getBandName(bb).equals("VV") || il.getImageReader().getBandName(bb).equals("V/V")) {
                out.add(a23);
            }
        }

        out.add(a3);
        out.add(a4);
        if (il.getImageReader() instanceof SarImageReader) {
            Argument aEnl = new Argument("ENL", Argument.FLOAT, false, ENL.getFromGeoImageReader((SarImageReader) il.getImageReader()));
            out.add(aEnl);
        }

        return out;
    }

    public boolean isIndeterminate() {
        return this.indeterminate;
    }

    public boolean isDone() {
        return this.done;
    }

    public int getMaximum() {
        return this.maximum;
    }

    public int getCurrent() {
        return this.current;
    }

    public String getMessage() {
        return this.message.toString();
    }

    public void setCurrent(int i) {
        current = i;
    }

    public void setMaximum(int size) {
        maximum = size;
    }

    public void setMessage(String string) {
        message =  new StringBuilder(string);
    }

    public void setIndeterminate(boolean value) {
        indeterminate = value;
    }

    public void setDone(boolean value) {
        done = value;
    }
}
