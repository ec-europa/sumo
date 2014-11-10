/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.batchmode;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoimage.analysis.DetectedPixels;
import org.geoimage.utils.IMask;
import org.geoimage.analysis.VDSAnalysis;
import org.geoimage.def.GeoImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.impl.SarImageReader;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.GeometricLayer;
import org.geoimage.viewer.core.io.SimpleCSVIO;
import org.geoimage.viewer.core.io.SumoXmlIOOld;
import org.geoimage.viewer.core.io.AbstractVectorIO;
import org.geoimage.viewer.core.io.factory.VectorIOFactory;
import org.geoimage.viewer.core.layers.SimpleVectorLayer;

/**
 *
 * @author thoorfr
 */
public class BatchVDSAnalysis {

    private static String file = "E:/Satellite Images/AIS4FISH/TheNetherlands/Rsat-1/Standard/2005-06-10-std5-desc/20050610055417.DAT";
    private static String outfile = "test.csv";
    private static float threshold = 1.5f;
    private static String format = "csv";
    private static float enl = 5.0f;
    private static List<IMask> masks = new Vector<IMask>();
    private static boolean m_geolocationCorrection;

    public static void main(String[] args) throws Exception {
        parse(args);
        GeoImageReader gir = GeoImageReaderFactory.createReaderForName(file).get(0);
        if(!(gir instanceof SarImageReader)){
            return;
        }
        // create new buffered mask with bufferingDistance using the mask in parameters
        final ArrayList<IMask> bufferedMask = new ArrayList<IMask>();
        for(IMask maskList : masks)
            bufferedMask.add(maskList.createBufferedMask(15.0));
        if (m_geolocationCorrection) {
            //ShiftVector shiftVector;
            //GeolocationCorrectionAnalysis geolocationCorrection = new GeolocationCorrectionAnalysis(gir, glayer, null, args[2], scalingFactor, shiftMask);
            //geolocationCorrection.start();
        }
        VDSAnalysis analysis = new VDSAnalysis((SarImageReader) gir,masks, enl, threshold, threshold, threshold, threshold, null);
        analysis.run(null);
        DetectedPixels pixels = analysis.getPixels();
        if (pixels == null) {
            return;
        }
        pixels.agglomerate();
        pixels.computeBoatsAttributes();
        GeometricLayer gl = createGeometricLayer(pixels);
        if (format.equals("xml")) {
            Map config = new HashMap();
            config.put(SumoXmlIOOld.CONFIG_FILE, file);
            AbstractVectorIO sio = VectorIOFactory.createVectorIO(VectorIOFactory.SUMO_OLD, config, gir);
            sio.save(gl, "");
        } else if (format.equals("csv")) {
            Map config = new HashMap();
            config.put(SimpleCSVIO.CONFIG_FILE, outfile);
            AbstractVectorIO csvio = VectorIOFactory.createVectorIO(VectorIOFactory.CSV, config, gir);
            csvio.save(gl, "EPSG:4326");
        } else if (format.equals("shp")) {
            try {
                Map config = new HashMap();
                config.put(SimpleCSVIO.CONFIG_FILE, outfile);
                AbstractVectorIO shpio = VectorIOFactory.createVectorIO(VectorIOFactory.SIMPLE_SHAPEFILE, config, gir);
                shpio.save(gl, "EPSG:4326");
            } catch (Exception ex) {
                Logger.getLogger(BatchVDSAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        gir.dispose();
    }

    private static GeometricLayer createGeometricLayer(DetectedPixels pixels) {
        GeometricLayer out = new GeometricLayer("point");
        GeometryFactory gf = new GeometryFactory();
        for (double[] boat : pixels.getBoats()) {
            String[] schema = new String[]{"id", "maximum value", "tile average", "tile standard deviation", "threshold", "num pixels", "runid"};
            String[] types = new String[]{"Double", "Double", "Double", "Double", "Double", "Double", "String"};
            Attributes atts = Attributes.createAttributes(schema, types);
            long runid = System.currentTimeMillis();
            atts.set("id", boat[0]);
            atts.set("maximum value", boat[3]);
            atts.set("tile average", boat[4]);
            atts.set("tile standard deviation", boat[5]);
            atts.set("threshold", boat[6]);
            atts.set("runid", runid + "");
            atts.set("num pixels", boat[7]);
            out.put(gf.createPoint(new Coordinate(boat[1], boat[2])), atts);
            out.setName("BatchVDSAnalysis");
        }
        return out;
    }

    private static void parse(String[] args) {
        try {
            int i = 0;
            while (i < args.length) {
                if (args[i].equals("-file")) {
                    file = args[i + 1];
                    i += 2;
                } else if (args[i].equals("-out")) {
                    outfile = args[i + 1].split("#")[1];
                    format = args[i + 1].split("#")[0];
                    i += 2;
                } else if (args[i].equals("-t")) {
                    threshold = Float.parseFloat(args[i + 1]);
                    i += 2;
                } else if (args[i].equals("-enl")) {
                    enl = Float.parseFloat(args[i + 1]);
                    i += 2;
                } else if (args[i].equals("-mask")) {
                    String maskformat = args[i + 1].split("#")[1];
                    String maskfile = args[i + 1].split("#")[0];
                    if (maskformat.equals("csv")) {
                        Map config = new HashMap();
                        config.put(SumoXmlIOOld.CONFIG_FILE, maskfile);
                        AbstractVectorIO sio = VectorIOFactory.createVectorIO(VectorIOFactory.SUMO_OLD, config, null);
                        GeometricLayer gl = sio.read();
                        masks.add(new SimpleVectorLayer(maskfile, null, gl.getGeometryType(), gl));
                    } else if (maskformat.equals("shp")) {
                        if (file == null) {
                            System.out.println("Warning: mask " + maskfile + " could not be used. please put -mask arguments after -file arguments.");
                            i += 2;
                            continue;
                        }
                        URL url = new File(maskfile).toURI().toURL();
                        GeoImageReader gir = GeoImageReaderFactory.createReaderForName(file).get(0);
                        Map config = new HashMap();
                        config.put("url", url);
                        AbstractVectorIO shpio = VectorIOFactory.createVectorIO(VectorIOFactory.SIMPLE_SHAPEFILE, config, gir);
                        GeometricLayer gl = shpio.read();
                        gir.dispose();
                        masks.add(new SimpleVectorLayer(maskfile, null, gl.getGeometryType(), gl));
                    }
                    i += 2;
                } else {
                    System.out.println("WARNING: argument " + args[i] + " being skipped please check your command line.");
                    i++;
                }
            }
        } catch (Exception e) {
            printUse();
        }
    }

    private static void printUse() {
        System.exit(0);
    }
}
