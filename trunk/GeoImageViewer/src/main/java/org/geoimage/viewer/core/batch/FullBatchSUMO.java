/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.batch;
import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoimage.analysis.DetectedPixels;
import org.geoimage.analysis.KDistributionEstimation;
import org.geoimage.analysis.VDSAnalysis;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.impl.ENL;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.actions.VDSAnalysisConsoleAction;
import org.geoimage.viewer.core.api.GeometricLayer;
import org.geoimage.viewer.core.factory.VectorIOFactory;
import org.geoimage.viewer.core.io.AbstractVectorIO;
import org.geoimage.viewer.core.io.KmlIO;
import org.geoimage.viewer.core.io.PostgisIO;
import org.geoimage.viewer.core.io.SumoXmlIOOld;
import org.geoimage.viewer.core.layers.thumbnails.ThumbnailsManager;
//import org.geotools.data.postgis.PostgisDataStoreFactory;

/**
 *
 * @author posadmo
 */
public class FullBatchSUMO {

    //threshold used for ...
    protected double thresh = 1.5;
    private float thresHH = 1.5f;
    private float thresHV = 1.2f;
    private float thresVH = 1.5f;
    private float thresVV = 1.5f;
    // buffer used for ....
    protected double buffer = 30;
    protected String output = "";
    protected List<File> selectedFiles = new ArrayList<File>();
    protected boolean createThumbs = false;
    protected boolean ddbbStore = false;
    protected String[] ddbbCon = {"139.191.8.113","5432","public","template_postgis","postgres","admin"};

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
           /**@todo
            * arguments
            * -d directory where the images are (list is recursive)
            * -f formats to analysed (several at a time allowed: n1, tif, h5)
            * -o output dir to store files if no ddbb storage
            * -t threshold for the calculation
            * -tb thumbnails storage (only available if file storage)
            * -p -> database storage
            *       -h hostname
            *       -pt port
            *       -sc schema
            *       -D (ddbb name);
            *       -U user ;
            *       -pw password
            *
            */
        FullBatchSUMO pr = new FullBatchSUMO();
        try {
            List<String> arguments = Arrays.asList(args);
            
            //se gli argomenti non contengono o contengono entrambi 
            if (!arguments.contains("-d")  ^!arguments.contains("-f") )
            {
                System.out.println("ERROR: no files to analysed if path / format / output dir are not specified");
                System.exit(0);
            }
            else if (!arguments.contains("-p") || !arguments.contains("-o")){

                System.out.println("ERROR: no files to storage mode specified");
                System.exit(0);
            }

            File path = new File(arguments.get(arguments.indexOf("-d")+1));
            String filter = arguments.get(arguments.indexOf("-f")+1);
            // Define a filter for files with the specified format
            FileFilter ff = new SimpleFileDealer(filter);
            //Filter files according to extension
            //pr.selectFileList(path,ff);
            pr.listRecursively(path, 0, ff);
            // Setting threshold if defined
            if (arguments.contains("-t")) {
                pr.thresh = Float.parseFloat(arguments.get(arguments.indexOf("-t")+1));
            }
            if (arguments.contains("-o")) {
                // Setting output dir
                pr.output = arguments.get(arguments.indexOf("-o")+1);
                // Generate thumbnails boolean
                if (arguments.contains("-tb")) {
                pr.createThumbs = Boolean.getBoolean(arguments.get(arguments.indexOf("-t")+1)) ;
                }
            }

            //Store into ddbb flag
             if (arguments.contains("-p")) {
                 pr.ddbbStore = Boolean.parseBoolean("true");
                if (arguments.contains("-h")) {
                    pr.ddbbCon[0]= arguments.get(arguments.indexOf("-h")+1);
                }
                if (arguments.contains("-pt")) {
                    pr.ddbbCon[1]= arguments.get(arguments.indexOf("-pt")+1);
                }if (arguments.contains("-sc")) {
                    pr.ddbbCon[2]= arguments.get(arguments.indexOf("-sc")+1);
                }if (arguments.contains("-D")) {
                    pr.ddbbCon[3]= arguments.get(arguments.indexOf("-D")+1);
                }if (arguments.contains("-U")) {
                    pr.ddbbCon[4]= arguments.get(arguments.indexOf("-U")+1);
                }if (arguments.contains("-pw")) {
                    pr.ddbbCon[5]= arguments.get(arguments.indexOf("-pw")+1);
                }

             }
            pr.executeAnalysis();
        } catch (Exception e) {
            e.printStackTrace();//printUse();
            Logger.getLogger(FullBatchSUMO.class.getName()).log(Level.SEVERE, null, e);
        }
    }


    /**
	 * listRecursively, to retrieve the list of image files to be analysed under a certain directory
	 * @param fdir
	 * @param depth
	 * @param ff
	 */
 public void listRecursively(File fdir, int depth, FileFilter ff) {
        int      MAX_DEPTH  = 20;  // Max 20 levels (directory nesting)

        if (fdir.isDirectory() && depth < MAX_DEPTH) {
            for (File f : fdir.listFiles(ff)) {
                // Go over each file/subdirectory.
                listRecursively(f, depth+1,ff);
                if (f.isFile())
                {
                    //System.out.println( f.getAbsolutePath());// Print name.}
                    Logger.getLogger(FullBatchSUMO.class.getName()).log(Level.INFO, null, f.getAbsolutePath());
                    selectedFiles.add(f);
                }

            }

        }
    }
/**
 *
 * @param path
 * @param filter
 */
 public void executeAnalysis()
     {
         int i = 0;
         for(Iterator<File> it = selectedFiles.iterator(); it.hasNext();)
         {
            runAnalysis(it.next(),i);
            i++;
         }
      }



 private void runAnalysis(File f,int idx) {
     /*if (new File(output + "/" + f.getParentFile().getName()).exists()) {

         return;
     }*/
     GeoImageReader gir = GeoImageReaderFactory.createReaderForName(f.getAbsolutePath()).get(0);
     if (!(gir instanceof SarImageReader)) {
         return;
     }
     URL url;
     //System.out.println(url);
     Map config = new HashMap();
     url = this.getClass().getResource("/org/geoimage/viewer/core/resources/shapefile/Global GSHHS Land Mask.shp");
     config.put("url", url);
     AbstractVectorIO shpio = VectorIOFactory.createVectorIO(VectorIOFactory.SIMPLE_SHAPEFILE, config );
     GeometricLayer mask = shpio.read(gir);

     IMask[] masks = new IMask[1];
     if (mask != null) {
//         masks[0]=new MaskVectorLayer("mask", null, mask.getGeometryType(), mask).createBufferedMask(buffer);
     }
     for (int i = 0; i < gir.getNBand(); i++) {
         gir.setBand(i);
         VDSAnalysis vds = new VDSAnalysis((SarImageReader) gir, masks, ENL.getFromGeoImageReader((SarImageReader) gir), thresHH,thresHV,thresVH,thresVV, true);
         vds.run(new KDistributionEstimation(ENL.getFromGeoImageReader((SarImageReader)gir)));
         DetectedPixels pixels = vds.getPixels();
         pixels.agglomerate();
         pixels.computeBoatsAttributes();
         //create geomatric layer containing the analysis
         long runid = System.currentTimeMillis();
         GeometricLayer gl = VDSAnalysisConsoleAction.createGeometricLayer(gir, pixels,runid);

         if (!this.ddbbStore)// filebased storage
         {
             gl.setName("VDS_" + gir.getBandName(i).replace("/", ""));
             //export to files
              new File(output + "/" + f.getParentFile().getName()).mkdirs();
             //config.put(KmlIO.CONFIG_FILE, output + "/" + f.getParentFile().getName() + "/" + gl.getName() + ".sumo.kmz");
             config.put(KmlIO.CONFIG_FILE, output + "/" + f.getParentFile().getName() + "/" + idx  + gl.getName() + ".sumo.kmz");
             AbstractVectorIO kmzio = VectorIOFactory.createVectorIO(VectorIOFactory.KML, config );
             kmzio.save(gl, "EPSG:4326",gir);
             if (createThumbs && gl.getGeometries().size() < 200) {
                 ThumbnailsManager tm = new ThumbnailsManager(output + "/" + f.getParentFile().getName() + "/band" + i + "/");
                 tm.createThumbnailsDir(gl, "id", gir, null);
             } else {
                 Map config2 = new HashMap();
                 //config2.put(SumoXmlIO.CONFIG_FILE, output + "/" + f.getName() + "/" + gl.getName() + ".sumo.xml");
                 config2.put(SumoXmlIOOld.CONFIG_FILE, output + "/" + f.getParentFile().getName() + "/"+ idx  + gl.getName() + ".sumo.xml");
                 AbstractVectorIO csv = VectorIOFactory.createVectorIO(VectorIOFactory.SUMO_OLD, config2 );
                 csv.save(gl, "EPSG:4326",gir);
             }
         }
         else{// DDBB based storage

             // connection parameters setting
             Map cfg = new HashMap();
             cfg.put("dbtype", "postgis");
             cfg.put("host", ddbbCon[0]);
             cfg.put("port", Integer.parseInt(ddbbCon[1]) );
             cfg.put("schema", ddbbCon[2]);
             cfg.put("dbname", ddbbCon[3]);
             cfg.put("username", ddbbCon[4]);
             cfg.put("password", ddbbCon[5]);
             // Setting to store analysed image's properties

             String[] imageCis = { "imagery",f.getParentFile().getName(), f.getName(), gir.getFormat(), gir.getBandName(i),String.valueOf(runid)};

              // PostGIS vector layer
             PostgisIO pgio = (PostgisIO)VectorIOFactory.createVectorIO(VectorIOFactory.POSTGIS, cfg );
             // setup the layername to be updated VESSELS
             pgio.setLayerName("vessels");
             // store all VDS onto DDBB
             pgio.saveAll(gl, "EPSG:4326", imageCis,gir);

         }
     }
     gir.dispose();

 }


}
/* private void runAnalysis_originale(File f,int idx) {
            /*if (new File(output + "/" + f.getParentFile().getName()).exists()) {

                return;
            }*//*
            GeoImageReader gir = GeoImageReaderFactory.create(f.getAbsolutePath());
            if (!(gir instanceof SarImageReader)) {
                return;
            }
            URL url;
            //System.out.println(url);
            Map config = new HashMap();
            url = this.getClass().getResource("/org/geoimage/viewer/core/resources/shapefile/Global GSHHS Land Mask.shp");
            config.put("url", url);
            VectorIO shpio = VectorIO.createVectorIO(VectorIO.SIMPLE_SHAPEFILE, config, gir);
            GeometricLayer mask = shpio.read();

            List<IMask> masks = new Vector<IMask>();
            if (mask != null) {
                masks.add(new SimpleVectorLayer("mask", null, mask.getGeometryType(), mask).createBufferedMask(buffer));
            }
            for (int i = 0; i < gir.getNBand(); i++) {
                gir.setBand(i);
                VDSAnalysis vds = new VDSAnalysis((SarImageReader) gir, masks, ENL.getFromGeoImageReader((SarImageReader) gir), thresHH,thresHV,thresVH,thresVV, true);
                vds.run(new KDistributionEstimation(ENL.getFromGeoImageReader((SarImageReader)gir)));
                DetectedPixels pixels = vds.getPixels();
                pixels.agglomerate();
                pixels.computeBoatsAttributes();
                //create geomatric layer containing the analysis
                long runid = System.currentTimeMillis();
                GeometricLayer gl = VDSAnalysisConsoleAction.createGeometricLayer(gir, pixels,runid);

                if (!this.ddbbStore)// filebased storage
                {
                    gl.setName("VDS_" + gir.getBandName(i).replace("/", ""));
                    //export to files
                     new File(output + "/" + f.getParentFile().getName()).mkdirs();
                    //config.put(KmlIO.CONFIG_FILE, output + "/" + f.getParentFile().getName() + "/" + gl.getName() + ".sumo.kmz");
                    config.put(KmlIO.CONFIG_FILE, output + "/" + f.getParentFile().getName() + "/" + idx  + gl.getName() + ".sumo.kmz");
                    VectorIO kmzio = VectorIO.createVectorIO(VectorIO.KML, config, gir);
                    kmzio.save(gl, "EPSG:4326");
                    if (createThumbs && gl.getGeometries().size() < 200) {
                        ThumbnailsManager tm = new ThumbnailsManager(output + "/" + f.getParentFile().getName() + "/band" + i + "/");
                        tm.createThumbnailsDir(gl, "id", gir, null);
                    } else {
                        Map config2 = new HashMap();
                        //config2.put(SumoXmlIO.CONFIG_FILE, output + "/" + f.getName() + "/" + gl.getName() + ".sumo.xml");
                        config2.put(SumoXmlIO.CONFIG_FILE, output + "/" + f.getParentFile().getName() + "/"+ idx  + gl.getName() + ".sumo.xml");
                        VectorIO csv = VectorIO.createVectorIO(VectorIO.SUMO, config2, gir);
                        csv.save(gl, "EPSG:4326");
                    }
                }
                else{// DDBB based storage

                    // connection parameters setting
                    Map cfg = new HashMap();
                    cfg.put(PostgisDataStoreFactory.DBTYPE.key, "postgis");
                    cfg.put(PostgisDataStoreFactory.HOST.key, ddbbCon[0]);
                    cfg.put(PostgisDataStoreFactory.PORT.key, Integer.parseInt(ddbbCon[1]) );
                    cfg.put(PostgisDataStoreFactory.SCHEMA.key, ddbbCon[2]);
                    cfg.put(PostgisDataStoreFactory.DATABASE.key, ddbbCon[3]);
                    cfg.put(PostgisDataStoreFactory.USER.key, ddbbCon[4]);
                    cfg.put(PostgisDataStoreFactory.PASSWD.key, ddbbCon[5]);
                    // Setting to store analysed image's properties

                    String[] imageCis = { "imagery",f.getParentFile().getName(), f.getName(), gir.getFormat(), gir.getBandName(i),String.valueOf(runid)};

                     // PostGIS vector layer
                    PostgisIO pgio = (PostgisIO)VectorIO.createVectorIO(VectorIO.POSTGIS, cfg, gir);
                    // setup the layername to be updated VESSELS
                    pgio.setLayerName("vessels");
                    // store all VDS onto DDBB
                    pgio.saveAll(gl, "EPSG:4326", imageCis);

                }
            }
            gir.dispose();

        }


   }*/
/**
 * FileListFilter to select the target files to be analysed
 */
class SimpleFileDealer implements FileFilter {

  String[] extensions;
  String description;
  /**
   * Constructor, more than one extension can be added ex. -> 'n1','txt','nc'
   * @param ext
   */
  public SimpleFileDealer(String ext) {
    this (new String[] {ext}, null);
  }
/**
   * Constructor, more than one extension can be added ex. -> 'n1','txt','nc'
   * also containing
   * @param ext
   */
  public SimpleFileDealer(String[] exts, String descr) {
    // Clone and lowercase the extensions
    extensions = new String[exts.length];
    for (int i = exts.length - 1; i >= 0; i--) {
      extensions[i] = exts[i].toLowerCase();
    }
    // Make sure we have a valid (if simplistic) description
    description = (descr == null ? exts[0] + " files" : descr);
  }

  public boolean accept(File f) {
    // We always allow directories, regardless of their extension
    if (f.isDirectory()) { return true; }

    // Ok, it??�s a regular file, so check the extension
    String name = f.getName().toLowerCase();
    for (int i = extensions.length - 1; i >= 0; i--) {
      if (name.endsWith(extensions[i])) {
        return true;
      }
    }
    return false;
  }

  public String getDescription() { return description; }

}

