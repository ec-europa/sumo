/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.geoimage.def.SarImageReader;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.io.GenericCSVIO;
import org.geoimage.viewer.core.io.GmlIO;
import org.geoimage.viewer.core.io.SimpleShapefile;
import org.geoimage.viewer.core.io.SumoXmlIOOld;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.util.IProgress;
import org.geoimage.viewer.widget.PostgisSettingsDialog;
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
public class AddVectorConsoleAction extends AbstractAction implements IProgress {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(AddVectorConsoleAction.class);
	
    private JFileChooser fd;
    private static String lastDirectory;
    boolean done = false;
    private String message = "Adding data. Please wait...";

    public AddVectorConsoleAction() {
        if(SumoPlatform.getApplication().getConfiguration().getLastVector().equals("")){
        	lastDirectory = java.util.ResourceBundle.getBundle("GeoImageViewer").getString("image_directory");
        }else{
            lastDirectory = new File(SumoPlatform.getApplication().getConfiguration().getLastVector()).getAbsolutePath();
        }
    	fd = new JFileChooser(lastDirectory);

    }

    @Override
    public String getName() {
        return "vector";
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
            if (args[0].equals("shp")) {
                addShapeFile(args);
            } else if (args[0].equals("postgis")) {
                addPostgis(args);
            } else if (args[0].equals("csv")) {
                addGenericCSV(args);
            } else if (args[0].equals("sumo XML")) {
                addSumo(args);
            } else if (args[0].equals("gml")) {
                addGml(args);
            } else if (args[0].equals("query")) {
                addQuery(args);
            }
        } catch (Exception e) {
            errorWindow("Problem with import of vector data\n");
            done = true;
            return false;
        }
        return true;
    }

    private void addGenericCSV(String[] args) {
    	ImageLayer l=LayerManager.getIstanceManager().getCurrentImageLayer();
//AG changed "noncomplexlayer" to "complexvds"
        if (args.length == 2) {
            if(l!=null){
            	GenericCSVIO csv=new GenericCSVIO(fd.getSelectedFile());//,l.getImageReader().getGeoTransform());
                GeometricLayer positions = csv.readLayer();
                if (positions.getProjection() == null) {
                	done=LayerManager.addLayerInThread(FactoryLayer.TYPE_COMPLEX, positions, (ImageLayer) l);
                } else {
                	try{
                    	positions = GeometricLayer.createImageProjectedLayer(positions, 
                    		((ImageLayer) l).getImageReader().getGeoTransform(),
                    		positions.getProjection());
                    		done=LayerManager.addLayerInThread("complexvds", positions, (ImageLayer) l);
                	}catch(Exception e){
                		logger.error(e.getMessage(),e);
                	}		
                }
            }
        } else {
            int returnVal = fd.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    lastDirectory = fd.getSelectedFile().getParent();
                    SumoPlatform.getApplication().getConfiguration().updateConfiguration(Constant.PREF_LASTVECTOR, lastDirectory);
                    GenericCSVIO csv=new GenericCSVIO(fd.getSelectedFile());//,l.getImageReader().getGeoTransform());
                    
                    
                    if(l!=null){
                        GeometricLayer positions = csv.readLayer();
                        if (positions.getProjection() == null) {
                        	done=LayerManager.addLayerInThread("complexvds", positions, (ImageLayer) l);
                        } else {
                            positions = GeometricLayer.createImageProjectedLayer(positions, ((ImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
                            done=LayerManager.addLayerInThread("complexvds", positions, (ImageLayer) l);
                        }
                    }
                } catch (Exception ex) {
                	logger.error(ex.getMessage(), ex);
                }
            }
            return;
        }
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
    private File selectFile(){
    	File file=null;
    	FileFilter f = new FileFilter() {

            public boolean accept(File f) {
                return f.isDirectory() || f.getPath().endsWith("shp") || f.getPath().endsWith("SHP");
            }

            public String getDescription() {
                return "Shapefiles";
            }
        };
        fd.setFileFilter(f);

        int returnVal = fd.showOpenDialog(null);
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
    private void addShapeFile(String[] args) {
        File file = null;
        if (args.length >= 2) {
            file = new File(args[2].split("=")[1].replace("%20", " "));
        } else {
        	file=selectFile();
        }
        
        ImageLayer imgLayer=LayerManager.getIstanceManager().getCurrentImageLayer();
        if(imgLayer!=null){
        	try {
        		Polygon imageP=((SarImageReader)imgLayer.getImageReader()).getBbox(PlatformConfiguration.getConfigurationInstance().getLandMaskMargin(0));
        		long start=System.currentTimeMillis();
                GeometricLayer gl = SimpleShapefile.createIntersectedLayer(file,imageP,((SarImageReader)imgLayer.getImageReader()).getGeoTransform());
                long end=System.currentTimeMillis();
                System.out.println("Shapefile loaded in:"+(end-start));
                // if 5 args, set a specific name
                if (args.length == 3) {
                    if (gl != null) {
                        gl.setName(args[3]);
                    }
                }
                done=LayerManager.addLayerInThread(FactoryLayer.TYPE_NON_COMPLEX, gl, imgLayer);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * 
     * @param args
     */
    private void addSimpleCSV(String[] args) {
    	try {
	        if (args.length == 2) {
	            String file=args[1].split("=")[1];
	            ImageLayer l=LayerManager.getIstanceManager().getCurrentImageLayer();
	            if(l!=null){
	            		GenericCSVIO csv=new GenericCSVIO(file);//,l.getImageReader().getGeoTransform());
	                    GeometricLayer positions = csv.readLayer();
	                    if (positions.getProjection() == null) {
	                    	done=LayerManager.addLayerInThread(FactoryLayer.TYPE_COMPLEX, positions, (ImageLayer) l);
	                    } else {
	                        positions = GeometricLayer.createImageProjectedLayer(positions, l.getImageReader().getGeoTransform(), positions.getProjection());
	                        done=LayerManager.addLayerInThread(FactoryLayer.TYPE_COMPLEX, positions, l);
	                    }
	            }
	        } else {
	            int returnVal = fd.showOpenDialog(null);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	            		ImageLayer l=LayerManager.getIstanceManager().getCurrentImageLayer();
	                    lastDirectory = fd.getSelectedFile().getParent();
	                    GenericCSVIO csv=new GenericCSVIO(fd.getSelectedFile());//,l.getImageReader().getGeoTransform());
	                    
	                    if(l!=null){
	                            GeometricLayer positions = csv.readLayer();
	                            if (positions.getProjection() == null) {
	                            	done=LayerManager.addLayerInThread(FactoryLayer.TYPE_COMPLEX, positions,  l);
	                            } else {
	                                positions = GeometricLayer.createImageProjectedLayer(positions, l.getImageReader().getGeoTransform(), positions.getProjection());
	                                done=LayerManager.addLayerInThread(FactoryLayer.TYPE_COMPLEX, positions,  l);
	                            }
	                    }
	                
	            } else {
	                return;
	            }
	        }
    	} catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }  
    }

    /**
     * 
     * @param args
     */
    private void addSumo(String[] args) {
    	try{
	        if (args.length == 2) {
	        	File f=new File(args[2].split("=")[3]);
	        	ImageLayer l=LayerManager.getIstanceManager().getCurrentImageLayer();
	            if(l!=null){
	            		SumoXmlIOOld old=new SumoXmlIOOld(f);
	            		GeometricLayer positions = old.readLayer();
	                    if (positions.getProjection() == null) {
	                    	done=LayerManager.addLayerInThread(FactoryLayer.TYPE_NON_COMPLEX, positions, l);
	                    } else {
	                        positions = GeometricLayer.createImageProjectedLayer(positions, l.getImageReader().getGeoTransform(), positions.getProjection());
	                        done=LayerManager.addLayerInThread(FactoryLayer.TYPE_NON_COMPLEX, positions, l);
	                    }
	            }
	        } else {
	            int returnVal = fd.showOpenDialog(null);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                try {
	                    lastDirectory = fd.getSelectedFile().getParent();
	                    SumoXmlIOOld old=new SumoXmlIOOld(fd.getSelectedFile());
	                    ImageLayer l=LayerManager.getIstanceManager().getCurrentImageLayer();
	                    if(l!=null){
		                    GeometricLayer positions = old.readLayer();
		                    if (positions.getProjection() == null) {
		                    	done=LayerManager.addLayerInThread(FactoryLayer.TYPE_COMPLEX, positions, (ImageLayer) l);
		                    } else {
		                        positions = GeometricLayer.createImageProjectedLayer(positions, ((ImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
		                        done=LayerManager.addLayerInThread(FactoryLayer.TYPE_COMPLEX, positions, (ImageLayer) l);
		                    }
	                    }    
	                } catch (Exception ex) {
	                	logger.error(ex.getMessage(), ex);
	                    return;
	                }
	            } else {
	                return;
	            }
	        }
    	} catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }   
    }

    /**
     * 
     * @param args
     */
    private void addGml(String[] args) {
    	try{
	        int returnVal = fd.showOpenDialog(null);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            try {
	                lastDirectory = fd.getSelectedFile().getParent();
	                ImageLayer l=LayerManager.getIstanceManager().getCurrentImageLayer();
	                if(l!=null){
	                		GmlIO gmlIO=new GmlIO(fd.getSelectedFile(),(SarImageReader)l.getImageReader());
	                        GeometricLayer positions = gmlIO.readLayer();
	                        if (positions.getProjection() == null) {
	                            done=LayerManager.addLayerInThread(FactoryLayer.TYPE_COMPLEX, positions, (ImageLayer) l);
	                        } else {
	                            positions = GeometricLayer.createImageProjectedLayer(positions, ((ImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
	                            done=LayerManager.addLayerInThread(FactoryLayer.TYPE_COMPLEX, positions, (ImageLayer) l);
	                        }
	                }
	            } catch (Exception ex) {
	            	logger.error(ex.getMessage(), ex);
	                return;
	            }
	        } else {
	            return;
	        }
	    } catch (Exception ex) {
	        logger.error(ex.getMessage(), ex);
	        return;
	    }   
    }

    
   

    @Override
    public String getPath() {
        return "Import/Vector";
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
        Argument a1 = new Argument("data type", Argument.STRING, false, "image");
        a1.setPossibleValues(new String[]{"csv", "shp", "gml", "sumo XML", "postgis", "query"});

        Vector<Argument> out = new Vector<Argument>();
        out.add(a1);
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
