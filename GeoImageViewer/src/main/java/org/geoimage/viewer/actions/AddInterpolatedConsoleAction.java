/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.geoimage.def.SarImageReader;
import org.geoimage.exception.GeoTransformException;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.io.GenericCSVIO;
import org.geoimage.viewer.core.io.PostgisIO;
import org.geoimage.viewer.core.io.SimpleShapefile;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.InterpolatedVectorLayer;
import org.geoimage.viewer.widget.PostgisSettingsDialog;
import org.geoimage.viewer.widget.dialog.DatabaseDialog;
import org.jrc.sumo.configuration.PlatformConfiguration;
import org.jrc.sumo.core.api.Argument;
import org.jrc.sumo.core.api.iactions.AbstractAction;
import org.jrc.sumo.core.api.layer.ILayer;
import org.slf4j.LoggerFactory;
import org.geoimage.utils.IProgress;


import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author thoorfr
 */
public class AddInterpolatedConsoleAction extends AbstractAction implements IProgress {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(AddInterpolatedConsoleAction.class);

    private JFileChooser fd;
    private static String lastDirectory;
    boolean done = false;
    private String message = "Adding data. Please wait...";

    public AddInterpolatedConsoleAction() {
    	if(lastDirectory==null)
    		lastDirectory = java.util.ResourceBundle.getBundle("GeoImageViewer").getString("image_directory");
        fd = new JFileChooser(lastDirectory);
    }

    public String getName() {
        return "interpolatedvector";
    }

    public String getDescription() {
        return " Add a vector layer, using geotools connection.\n" +
                "Use \"add shp SimpleEditVector [file=/home/data/layer.shp]\" to add the layer.shp file to the image\n" +
                "Use \"add postgis SimpleEditVector [host=myhost.org dbname=database user=user password=pwd table=mytable]\" to add a postgis table to the image\n";
    }

    public boolean execute(final String[] args) {
        if (args.length == 0) {
            return true;
        }
        done = false;
        try {
            if (args[0].equals("shp")) {
                addShapeFile(args);

            } else if (args[0].equals("postgis")) {
                addPostgis(args);

            } else if (args[0].equals("csv")) {
                addSimpleCSV(args);
            } else if (args[0].equals("query")) {
                addQuery(args);
            }
        } catch (Exception e) {
            done = true;
            return false;
        }
        return true;
    }

    private void addPostgis(String[] args) {
        Map<String,Object> config = null;
        String layer = "";
        if (args.length == 4) {
            PostgisSettingsDialog ps = new PostgisSettingsDialog(null, true);
            ps.setVisible(true);
            if (!ps.isOk()) {
                done = true;
            }
            layer = ps.getTable();
            config = ps.getConfig();
        } else {
            ImageLayer l=LayerManager.getIstanceManager().getCurrentImageLayer();
            if(l!=null){
                    try {
                        PostgisIO pio=new PostgisIO(l.getImageReader(),config);
                        pio.setLayerName(layer);
                        GeometricLayer gl = pio.readLayer();
                        addLayerInThread(args[1], args[2], gl, (ImageLayer) l);
                    } catch (Exception ex) {
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

    private void addShapeFile(String[] args) {
        String file = "";
        if (args.length == 4) {
            file = args[3].split("=")[1].replace("%20", " ");
        } else {
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
                    lastDirectory = fd.getSelectedFile().getAbsolutePath();
                    file = fd.getSelectedFile().getCanonicalPath();
                } catch (IOException ex) {
                	logger.error(ex.getMessage(), ex);
                }
            } else {
                return;
            }
        }
        ImageLayer l=LayerManager.getIstanceManager().getCurrentImageLayer();
        if(l!=null){
                try {
                	Polygon imageP=((SarImageReader)l.getImageReader()).getBbox(PlatformConfiguration.getConfigurationInstance().getLandMaskMargin(0));
                    GeometricLayer gl = SimpleShapefile.createIntersectedLayer(new File(file),imageP, ((SarImageReader)l.getImageReader()).getGeoTransform());
                    addLayerInThread(args[1], args[2], gl, (ImageLayer) l);
                } catch (Exception ex) {
                	logger.error(ex.getMessage(), ex);
                }
        }
    }
    
    private void addSimpleCSV(String[] args) {
        if (args.length == 4&&args[1].contains("=")) {
            String file=args[1].split("=")[3];
            ImageLayer l=LayerManager.getIstanceManager().getCurrentImageLayer();
            if(l!=null){
            		GenericCSVIO csvio=new GenericCSVIO(file);//,l.getImageReader().getGeoTransform());
                    GeometricLayer positions = csvio.readLayer();
                    if (positions.getProjection() == null) {
                        addLayerInThread(args[1], args[2], positions, (ImageLayer) l);
                    } else {
                    	try{
                        	positions = GeometricLayer.createImageProjectedLayer(positions, ((ImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
                    			addLayerInThread(args[1], args[2], positions, (ImageLayer) l);
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
                    ImageLayer l=LayerManager.getIstanceManager().getCurrentImageLayer();
                    if(l!=null){
                    		GenericCSVIO csvio=new GenericCSVIO(fd.getSelectedFile());//,l.getImageReader().getGeoTransform());
                    		GeometricLayer positions = csvio.readLayer();
                            if (positions.getProjection() == null) {
                                addLayerInThread(args[1], args[2], positions, (ImageLayer) l);
                            } else {
                                positions = GeometricLayer.createImageProjectedLayer(positions, ((ImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
                                addLayerInThread(args[1], args[2], positions, (ImageLayer) l);
                            }
                    }
                } catch (GeoTransformException ex) {
                	logger.error(ex.getMessage(), ex);
                }
            } 
            return;
        }
    }

    private static ILayer createLayer(String id, final String date, GeometricLayer layer, ImageLayer parent) {
    	Timestamp t=Timestamp.valueOf(((SarImageReader)parent.getImageReader()).getTimeStampStart());
        return new InterpolatedVectorLayer(layer.getName(),parent.getImageReader(), layer, id, date,t);

    }

    public void addLayerInThread(final String id, final String date, final GeometricLayer layer, final ImageLayer il) {
        new Thread(new Runnable() {

            public void run() {
                LayerManager.getIstanceManager().addLayer(createLayer(id, date, layer, il));
                done = true;
            }
        }).start();
    }

    public String getPath() {
        return "Import/Interpolated Vector";
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

    public List<Argument> getArgumentTypes() {
        Argument a1 = new Argument("data type", Argument.STRING, false, "image");
        a1.setPossibleValues(new String[]{"csv", "postgis", "shp", "query"});
        Argument a2 = new Argument("Id Column", Argument.STRING, false, "id");
        Argument a3 = new Argument("Date Column", Argument.STRING, false, "date");
        Argument a4 = new Argument("Interpolation Date", Argument.DATE, false, new Date());
        Vector<Argument> out = new Vector<Argument>();
        out.add(a1);
        out.add(a2);
        out.add(a3);
        out.add(a4);
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
