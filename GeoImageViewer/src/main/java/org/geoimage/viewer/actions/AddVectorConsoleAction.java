/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.io.GenericCSVIO;
import org.geoimage.viewer.core.io.GmlIO;
import org.geoimage.viewer.core.io.SimpleShapefile;
import org.geoimage.viewer.core.io.SumoXmlIOOld;
import org.geoimage.viewer.core.layers.GenericLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.geoimage.viewer.util.IProgress;
import org.geoimage.viewer.util.files.ArchiveUtil;
import org.geoimage.viewer.util.files.NoaaClient;
import org.geoimage.viewer.widget.PostgisSettingsDialog;
import org.geoimage.viewer.widget.dialog.ActionDialog.Argument;
import org.geoimage.viewer.widget.dialog.DatabaseDialog;
import org.jrc.sumo.configuration.PlatformConfiguration;
import org.jrc.sumo.util.Constant;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author thoorfr+ga
 * this class is called whenever you want to open one of the supported vector formats (shp, csv, xml, gml, query). It is opened as a new layer, linked to the active image.
 */
public class AddVectorConsoleAction extends SumoAbstractAction implements IProgress {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(AddVectorConsoleAction.class);

    private JFileChooser fd;
    private static String lastDirectory;
    boolean done = false;
    private String message = "Adding data. Please wait...";

    public AddVectorConsoleAction() {
    	super("vector","Import/Vector");
        if(SumoPlatform.getApplication().getConfiguration().getLastVector().equals("")){
        	lastDirectory = java.util.ResourceBundle.getBundle("GeoImageViewer").getString("image_directory");
        }else{
            lastDirectory = new File(SumoPlatform.getApplication().getConfiguration().getLastVector()).getAbsolutePath();
        }
    	fd = new JFileChooser(lastDirectory);

    }

    @Override
    public String getDescription() {
        return " Add a vector layer, using geotools connection.\n" +
                "Use \"add shp SimpleEditVector [file=/home/data/layer.shp]\" to add the layer.shp file to the image\n" +
                "Use \"add postgis SimpleEditVector [host=myhost.org dbname=database user=user password=pwd table=mytable]\" to add a postgis table to the image\n";
    }

    @Override
    public boolean execute(final String[] args) {
        if (args.length < 1) {
            errorWindow("Wrong arguments for add vector action\n" + getDescription());
            done = true;
            return true;
        }
        done = false;
        try {
        	GenericLayer lay=null;
        	File shp=null;
            if (args[0].equals("shp")) {
                int t=MaskVectorLayer.COASTLINE_MASK;
                if(args[1].equalsIgnoreCase("ice")){
                	try{
	                	t=MaskVectorLayer.ICE_MASK;
	                	if(args[1].equals("remote")){
	                		GeoImageReader reader=SumoPlatform.getApplication().getCurrentImageReader();
	                		String cachePath=SumoPlatform.getApplication().getCachePath();
	                		File cache=new File(cachePath+File.separator+System.currentTimeMillis());
	                		if(!cache.exists())
	                			cache.mkdirs();
	                		File f=NoaaClient.download(reader.getImageDate(),cache.getAbsolutePath());

	                		if(f.getName().endsWith("zip")||f.getName().endsWith("gz")){
	                			ArchiveUtil.unZip(f.getAbsolutePath());
	                			File[] shpfiles=f.getParentFile().listFiles((java.io.FileFilter) pathname -> FilenameUtils.getExtension(pathname.getName()).equalsIgnoreCase("shp"));
	                			shp=shpfiles[0];
	                		}
	                	}else{
	                		shp=selectFile(new String[]{"shp","SHP"});
	                	}
                	}catch(Exception e){
                		shp=null;
                	}
                }else{
             		shp=selectFile(new String[]{"shp","SHP"});
            	}
                if(shp!=null){
                	GeometricLayer gl=loadShapeFile(shp,"");
                	lay=FactoryLayer.createMaskLayer(gl,t);
                }

            } else if (args[0].equals("postgis")) {
                addPostgis(args);

            } else if (args[0].equals("csv")) {
            	GeometricLayer positions=loadGenericCSV(args);
                lay=FactoryLayer.createComplexLayer(positions);
                done=LayerManager.addLayerInThread(lay);

            } else if (args[0].equals("sumo XML")) {
            /*    int t=MaskVectorLayer.COASTLINE_MASK;
                if(args[1].equalsIgnoreCase("ice"))
                	t=MaskVectorLayer.ICE_MASK;

            	GeometricLayer positions=loadSumoXML(args);
        		lay=FactoryLayer.createMaskLayer(positions,t);*/

            } else if (args[0].equals("gml")) {
            	GeometricLayer positions=loadGml(args);
                GenericLayer gl=FactoryLayer.createComplexLayer(positions);
                done=LayerManager.addLayerInThread(gl);

            } else if (args[0].equals("query")) {
                addQuery(args);
            }
            if(lay!=null)
            	done=LayerManager.addLayerInThread(lay);
        } catch (Exception e) {
            errorWindow("Problem with import of vector data\n");
            done = true;
            return false;
        }
        return true;
    }

    /**
     *
     * @param args
     */
    private GeometricLayer loadGenericCSV(String[] args) {
    	ImageLayer l=LayerManager.getIstanceManager().getCurrentImageLayer();
    	GeometricLayer positions=null;
    	if(l!=null){
    		try {
		    	GenericCSVIO csv=null;

	            File f=selectFile(new String[]{"csv","CSV"});
	            csv=new GenericCSVIO(f);

	            if(csv!=null){
	                positions = csv.readLayer();
	                if (positions.getProjection() != null) {
	                    positions = GeometricLayer.createImageProjectedLayer(positions, ((ImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
	                }
	            }
	        } catch (Exception ex) {
	        	logger.error(ex.getMessage(), ex);
	        }
    	}
    	return positions;
    }

    private void addPostgis(String[] args) {
        Map <String,String> config = null;
        String layer = "";
        if (args.length == 1) {
            PostgisSettingsDialog ps = new PostgisSettingsDialog(null, true);
            ps.setVisible(true);
            if (!ps.isOk()) {
                done = true;
            }
            layer = ps.getTable();
            config = ps.getConfig();

        } else if (args.length == 6) {
            config = new HashMap<String,String>();
            config.put("dbtype", "postgis");
            config.put("schema", "public");
            config.put("port", "5432");
            for (int i = 1; i < args.length; i++) {
                if (args[i].startsWith("host=")) {
                    config.put("host", args[i].replace("host=", ""));
                } else if (args[i].startsWith("dbname=")) {
                    config.put("dbname", args[i].replace("dbname=", ""));
                } else if (args[i].startsWith("user=")) {
                    config.put("user", args[i].replace("user=", ""));
                } else if (args[i].startsWith("password=")) {
                    config.put("password", args[i].replace("password=", ""));
                } else if (args[i].startsWith("table=")) {
                    layer = args[i].replace("table=", "");
                }
            }
        }
        for (ILayer l : SumoPlatform.getApplication().getLayerManager().getLayers().keySet()) {
            if (l instanceof ImageLayer && l.isActive()) {
                try {
                    DatabaseDialog dialog = new DatabaseDialog(null, false);
                    Connection conn = DriverManager.getConnection("jdbc:postgresql://" + config.get("host").toString()
                            + ":" + config.get("port") + "/" + config.get("dbname"), config.get("user"), config.get("password"));
                    dialog.setConnection(conn);
                    dialog.setImageLayer((ImageLayer) l, args[1]);
                    dialog.setVisible(true);
                    done = true;
                    return;
                } catch (Exception ex) {
                    done = true;
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
    }

    private void addQuery(String[] args) {
        try {
            for (ILayer l : SumoPlatform.getApplication().getLayerManager().getLayers().keySet()) {
                if (l instanceof ImageLayer && l.isActive()) {
                    DatabaseDialog dialog = new DatabaseDialog(null, true);
                    Connection conn = DriverManager.getConnection("jdbc:h2:~/.sumo/VectorData;AUTO_SERVER=TRUE", "sa", "");
                    dialog.setConnection(conn);
                    dialog.setImageLayer((ImageLayer) l, args[1]);
                    dialog.setVisible(true);
                    done = true;
                }
            }
        } catch (SQLException ex) {
        	logger.error(ex.getMessage(), ex);
        }
    }

    /**
     *
     * @return
     */
    private File selectFile(String[] extsFilter){
    	File file=null;
    	FileFilter f =null;
    	if(extsFilter!=null & extsFilter.length>0){
	    	f = new FileNameExtensionFilter("Vector files",extsFilter);
	        fd.setFileFilter(f);
    	}


        int returnVal = fd.showOpenDialog(null);
        if(f!=null)
        	fd.removeChoosableFileFilter(f);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                lastDirectory = fd.getSelectedFile().getParent();
                SumoPlatform.getApplication().getConfiguration().updateConfiguration(Constant.PREF_LASTVECTOR, lastDirectory);
                file = fd.getSelectedFile();
            } catch (Exception ex) {
            	logger.error(ex.getMessage(), ex);
            }
        }
        return file;
    }



    /**
     *
     * @param args
     */
    private GeometricLayer loadShapeFile(File file,String name){//String[] args) {
        GeometricLayer gl=null;

        ImageLayer imgLayer=LayerManager.getIstanceManager().getCurrentImageLayer();
        if(imgLayer!=null){
        	try {
        		Polygon imageP=((SarImageReader)imgLayer.getImageReader()).getBbox(PlatformConfiguration.getConfigurationInstance().getLandMaskMargin(0));
        		long start=System.currentTimeMillis();
                 gl = SimpleShapefile.createIntersectedLayer(file,imageP,((SarImageReader)imgLayer.getImageReader()).getGeoTransform());
                long end=System.currentTimeMillis();
                System.out.println("Shapefile loaded in:"+(end-start));
                // if 5 args, set a specific name
                if (name!=null&&!name.equals("")) {
                    if (gl != null) {
                        gl.setName(name);
                    }
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        return gl;
    }

    /**
     *
     * @param args
     */
    private void addSimpleCSV(String[] args) {
    	try {
    		GeometricLayer positions=null;
    		GenericCSVIO csv=null;
	        if (args.length == 2) {
	            String file=args[1].split("=")[1];

           		csv=new GenericCSVIO(file);
	        } else {
	            int returnVal = fd.showOpenDialog(null);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                    lastDirectory = fd.getSelectedFile().getParent();
	                    csv=new GenericCSVIO(fd.getSelectedFile());//,l.getImageReader().getGeoTransform());
	            }
	        }
            if(csv!=null){
                positions = csv.readLayer();
                if (positions.getProjection() != null) {
                	ImageLayer l=LayerManager.getIstanceManager().getCurrentImageLayer();
                	positions = GeometricLayer.createImageProjectedLayer(positions, l.getImageReader().getGeoTransform(), positions.getProjection());

                }
            }
	        GenericLayer gl=FactoryLayer.createComplexLayer(positions);
            done=LayerManager.addLayerInThread(gl);
    	} catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }
    }

    /**
     *
     * @param args
     */
    private GeometricLayer loadSumoXML(String[] args) {
    	GeometricLayer positions =null;
    	File sumoXml=null;
    	try{
	        if (args.length == 2) {
	        	sumoXml=new File(args[2].split("=")[3]);
	        } else {
	            int returnVal = fd.showOpenDialog(null);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                    lastDirectory = fd.getSelectedFile().getParent();
	                    sumoXml=fd.getSelectedFile();
	            }
	        }
            ImageLayer l=LayerManager.getIstanceManager().getCurrentImageLayer();
	        SumoXmlIOOld old=new SumoXmlIOOld(sumoXml);
            positions = old.readLayer();
            if (positions.getProjection() != null) {
            	positions = GeometricLayer.createImageProjectedLayer(positions, ((ImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
            }

    	} catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    	return positions;
    }

    /**
     *
     * @param args
     */
    private GeometricLayer loadGml(String[] args) {
    	GeometricLayer positions =null;
    	try{
	        int returnVal = fd.showOpenDialog(null);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	                lastDirectory = fd.getSelectedFile().getParent();
	                ImageLayer l=LayerManager.getIstanceManager().getCurrentImageLayer();

            		GmlIO gmlIO=new GmlIO(fd.getSelectedFile(),(SarImageReader)l.getImageReader());
                    positions = gmlIO.readLayer();
                    if (positions.getProjection() != null) {
                    	positions = GeometricLayer.createImageProjectedLayer(positions, ((ImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
                    }
	        }
	    } catch (Exception ex) {
	        logger.error(ex.getMessage(), ex);
	        return null;
	    }
    	return positions;
    }

    public boolean isIndeterminate() {
        return true;
    }

    public boolean isDone() {
        return done;
    }

    public int getMaximum() {
        return 1;
    }

    public int getCurrent() {
        return 1;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public List<Argument> getArgumentTypes() {
    	List<Argument> out = new ArrayList<Argument>();
        Argument a1 = new Argument("data type", Argument.STRING, false, "image");
        a1.setPossibleValues(new String[]{"csv", "shp", "gml", "sumo XML", "postgis", "query"});

        Argument a2 = new Argument("type", Argument.STRING, false, "coastline");
        a2.setPossibleValues(new String[]{"coastline", "ice","other"});

        Argument a3= new Argument("Load from Noaa",Argument.BOOLEAN,true,false);
        a3.setCondition(a2,"ice");

        out.add(a1);
        out.add(a2);
        out.add(a3);

        return out;
    }

    public void setCurrent(int i) {
    }

    public void setMaximum(int size) {
    }

    public void setMessage(String string) {
    }

    public void setIndeterminate(boolean value) {
    }

    public void setDone(boolean value) {
        done = value;
    }
}
