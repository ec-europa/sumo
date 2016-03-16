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
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.io.GenericCSVIO;
import org.geoimage.viewer.core.io.PostgisIO;
import org.geoimage.viewer.core.io.SimpleShapefile;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.SumoActionEvent;
import org.geoimage.viewer.core.layers.SumoActionListener;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.InterpolatedVectorLayer;
import org.geoimage.viewer.widget.PostgisSettingsDialog;
import org.geoimage.viewer.widget.dialog.ActionDialog.Argument;
import org.geoimage.viewer.widget.dialog.DatabaseDialog;
import org.jrc.sumo.configuration.PlatformConfiguration;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author thoorfr
 */
public class AddInterpolatedConsoleAction extends SumoAbstractAction {
	private  org.slf4j.Logger logger=LoggerFactory.getLogger(AddInterpolatedConsoleAction.class);

    private JFileChooser fd;
    private static String lastDirectory;

    public AddInterpolatedConsoleAction() {
    	super("interpolatedvector","Import/Interpolated Vector");
    	if(lastDirectory==null)
    		lastDirectory = java.util.ResourceBundle.getBundle("GeoImageViewer").getString("image_directory");
        fd = new JFileChooser(lastDirectory);
    }


    public String getDescription() {
        return " Add a vector layer, using geotools connection.\n" +
                "Use \"add shp SimpleEditVector [file=/home/data/layer.shp]\" to add the layer.shp file to the image\n" +
                "Use \"add postgis SimpleEditVector [host=myhost.org dbname=database user=user password=pwd table=mytable]\" to add a postgis table to the image\n";
    }

    public boolean execute() {
        if (paramsAction.size() == 0) {
            return true;
        }
        done = false;
        try {
        	String message = "Adding Image. Please wait...";
        	super.notifyEvent(new SumoActionEvent(SumoActionEvent.STARTACTION, message, -1));


        	String type=paramsAction.get("data type");
            if (type.equals("shp")) {
                addShapeFile();
            } else if (type.equals("postgis")) {
               // addPostgis();
            } else if (type.equals("csv")) {
                addSimpleCSV();
            } else if (type.equals("query")) {
               // addQuery();
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
                        LayerManager.addLayerInThread(createLayer(args[1], args[2], gl, (ImageLayer) l));

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

    private void addShapeFile() {
        String file = "";
        if (paramsAction.size() == 4) {
            file = getParamValue("Date Column").split("=")[1].replace("%20", " ");
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
                    LayerManager.addLayerInThread(createLayer(getParamValue("data type"),
                    		getParamValue("Id Column"), gl, (ImageLayer) l));

                } catch (Exception ex) {
                	logger.error(ex.getMessage(), ex);
                }
        }
    }

    private void addSimpleCSV() {
        if (paramsAction.size() == 4&&paramsAction.get("Id Column").contains("=")) {
            String file=paramsAction.get("Id Column").split("=")[3];
            ImageLayer l=LayerManager.getIstanceManager().getCurrentImageLayer();
            if(l!=null){
            		GenericCSVIO csvio=new GenericCSVIO(file);//,l.getImageReader().getGeoTransform());
                    GeometricLayer positions = csvio.readLayer();
                    if (positions.getProjection() == null) {
                        LayerManager.addLayerInThread(createLayer(paramsAction.get("Id Column"),
                        		paramsAction.get("Date Column"), positions, (ImageLayer) l));
                    } else {
                    	try{
                        	positions = GeometricLayer.createImageProjectedLayer(positions, ((ImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
                    			LayerManager.addLayerInThread(createLayer(paramsAction.get("Id Column"),
                                		paramsAction.get("Date Column"), positions, (ImageLayer) l));
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
                            	LayerManager.addLayerInThread(createLayer(paramsAction.get("Id Column"),
                                		paramsAction.get("Date Column"), positions, (ImageLayer) l));
                            } else {
                                positions = GeometricLayer.createImageProjectedLayer(positions, ((ImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
                                LayerManager.addLayerInThread(createLayer(paramsAction.get("Id Column"),
                                		paramsAction.get("Date Column"), positions, (ImageLayer) l));
                            }
                    }
                } catch (Exception ex) {
                	logger.error(ex.getMessage(), ex);
                }
            }
            return;
        }
    }

    private static ILayer createLayer(String id, final String date, GeometricLayer layer, ImageLayer parent) {
    	String start=((SarImageReader)parent.getImageReader()).getTimeStampStart();
    	start=start.replace("Z","");
    	Timestamp t=Timestamp.valueOf(start);
        return new InterpolatedVectorLayer(layer.getName(),parent.getImageReader(), layer, id, date,t);

    }




    public List<Argument> getArgumentTypes() {
        Argument a1 = new Argument("data type", Argument.STRING, false, "image","data type");
        a1.setPossibleValues(new String[]{"csv", "postgis", "shp", "query"});
        Argument a2 = new Argument("Id Column", Argument.STRING, false, "id","Id Column");
        Argument a3 = new Argument("Date Column", Argument.STRING, false, "date","Date Column");
        Argument a4 = new Argument("Interpolation Date", Argument.DATE, false, new Date(),"Interpolation Date");
        Vector<Argument> out = new Vector<Argument>();
        out.add(a1);
        out.add(a2);
        out.add(a3);
        out.add(a4);
        return out;
    }



}
