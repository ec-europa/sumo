/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.geoimage.utils.IProgress;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.TimeComponent;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.GeometricLayer;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.IVectorLayer;
import org.geoimage.viewer.core.io.GenericCSVIO;
import org.geoimage.viewer.core.io.SumoXmlIOOld;
import org.geoimage.viewer.core.io.AbstractVectorIO;
import org.geoimage.viewer.core.io.factory.VectorIOFactory;
import org.geoimage.viewer.core.layers.ComplexEditVDSVectorLayer;
import org.geoimage.viewer.core.layers.SimpleEditVectorLayer;
import org.geoimage.viewer.core.layers.SimpleVectorLayer;
import org.geoimage.viewer.core.layers.TimeVectorLayer;
import org.geoimage.viewer.widget.DatabaseDialog;
import org.geoimage.viewer.widget.PostgisSettingsDialog;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 *
 * @author thoorfr+ga
 * this class is called whenever you want to open one of the supported vector formats (shp, csv, xml, gml, query). It is opened as a new layer, linked to the active image.
 */
public class AddVectorConsoleAction extends ConsoleAction implements IProgress {

    private JFileChooser fd;
    private static String lastDirectory;
    boolean done = false;
    private String message = "Adding data. Please wait...";

    public AddVectorConsoleAction() {
    	if(lastDirectory==null)
    		lastDirectory = java.util.ResourceBundle.getBundle("GeoImageViewer").getString("image_directory");
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
        //System.out.println(args[0]);

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
//AG changed "noncomplexlayer" to "complexvds"
        if (args.length == 2) {
            Map config = new HashMap();
            config.put(GenericCSVIO.CONFIG_FILE, args[1].split("=")[1]);
            for (ILayer l : Platform.getLayerManager().getLayers()) {
                if (l instanceof IImageLayer && l.isActive()) {
                    AbstractVectorIO csv = VectorIOFactory.createVectorIO(VectorIOFactory.CSV, config, ((IImageLayer) l).getImageReader());
                    GeometricLayer positions = csv.read();
                    if (positions.getProjection() == null) {
                        addLayerInThread("complexvds", positions, (IImageLayer) l);
                    } else {
                        positions = GeometricLayer.createImageProjectedLayer(positions, ((IImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
                        addLayerInThread("complexvds", positions, (IImageLayer) l);
                    }
                }
            }
        } else {
            int returnVal = fd.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    lastDirectory = fd.getSelectedFile().getParent();
                    Map config = new HashMap();
                    config.put(GenericCSVIO.CONFIG_FILE, fd.getSelectedFile().getCanonicalPath());
                    for (ILayer l : Platform.getLayerManager().getLayers()) {
                        if (l instanceof IImageLayer && l.isActive()) {
                            AbstractVectorIO csv = VectorIOFactory.createVectorIO(VectorIOFactory.GENERIC_CSV, config, ((IImageLayer) l).getImageReader());
                            GeometricLayer positions = csv.read();
                            if (positions.getProjection() == null) {
                                addLayerInThread("complexvds", positions, (IImageLayer) l);
                            } else {
                                positions = GeometricLayer.createImageProjectedLayer(positions, ((IImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
                                addLayerInThread("complexvds", positions, (IImageLayer) l);
                            }
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(AddInterpolatedConsoleAction.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
            } else {
                return;
            }
        }
    }
/*
    private void addPostgis(String[] args) {
        Map config = null;
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
            config = new HashMap();
            config.put(PostgisDataStoreFactory.DBTYPE.key, "postgis");
            config.put(PostgisDataStoreFactory.SCHEMA.key, "public");
            config.put(PostgisDataStoreFactory.PORT.key, "5432");
            for (int i = 1; i < args.length; i++) {
                if (args[i].startsWith("host=")) {
                    config.put(PostgisDataStoreFactory.HOST.key, args[i].replace("host=", ""));
                } else if (args[i].startsWith("dbname=")) {
                    config.put(PostgisDataStoreFactory.DATABASE.key, args[i].replace("dbname=", ""));
                } else if (args[i].startsWith("user=")) {
                    config.put(PostgisDataStoreFactory.USER.key, args[i].replace("user=", ""));
                } else if (args[i].startsWith("password=")) {
                    config.put(PostgisDataStoreFactory.PASSWD.key, args[i].replace("password=", ""));
                } else if (args[i].startsWith("table=")) {
                    layer = args[i].replace("table=", "");
                }
            }
        }
        for (ILayer l : Platform.getLayerManager().getLayers()) {
            if (l instanceof IImageLayer & l.isActive()) {
                try {
                    DatabaseDialog dialog = new DatabaseDialog(null, false);
                    Connection conn = DriverManager.getConnection("jdbc:postgresql://" + config.get(PostgisDataStoreFactory.HOST.key).toString() + ":" + config.get(PostgisDataStoreFactory.PORT.key) + "/" + config.get(PostgisDataStoreFactory.DATABASE.key).toString(), config.get(PostgisDataStoreFactory.USER.key).toString(), config.get(PostgisDataStoreFactory.PASSWD.key).toString());
                    dialog.setConnection(conn);
                    dialog.setImageLayer((IImageLayer) l, args[1]);
                    dialog.setVisible(true);
                    done = true;
                    return;
                } catch (Exception ex) {
                    done = true;
                    Logger.getLogger(AddInterpolatedConsoleAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }*/
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
        for (ILayer l : Platform.getLayerManager().getLayers()) {
            if (l instanceof IImageLayer && l.isActive()) {
                try {
                    DatabaseDialog dialog = new DatabaseDialog(null, false);
                    Connection conn = DriverManager.getConnection("jdbc:postgresql://" + config.get("host").toString()
                            + ":" + config.get("port") + "/" + config.get("dbname"), config.get("user"), config.get("password"));
                    dialog.setConnection(conn);
                    dialog.setImageLayer((IImageLayer) l, args[1]);
                    dialog.setVisible(true);
                    done = true;
                    return;
                } catch (Exception ex) {
                    done = true;
                    Logger.getLogger(AddInterpolatedConsoleAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void addQuery(String[] args) {
        try {
            for (ILayer l : Platform.getLayerManager().getLayers()) {
                if (l instanceof IImageLayer && l.isActive()) {
                    DatabaseDialog dialog = new DatabaseDialog(null, true);
                    Connection conn = DriverManager.getConnection("jdbc:h2:~/.sumo/VectorData;AUTO_SERVER=TRUE", "sa", "");
                    dialog.setConnection(conn);
                    dialog.setImageLayer((IImageLayer) l, args[1]);
                    dialog.setVisible(true);
                    done = true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(AddInterpolatedConsoleAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addShapeFile(String[] args) {
        Map<String,Object> config = new HashMap<String,Object>();
        String file = "";
        if (args.length >= 2) {
            file = args[2].split("=")[1].replace("%20", " ");
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
                    Logger.getLogger(AddInterpolatedConsoleAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                return;
            }
        }
        try {
            config.put("url", new File(file).toURI().toURL());
        } catch (Exception e) {
            return;
        }
        for (ILayer l : Platform.getLayerManager().getLayers()) {
            if (l instanceof IImageLayer && l.isActive()) {
                try {
                    AbstractVectorIO shpio = VectorIOFactory.createVectorIO(VectorIOFactory.SIMPLE_SHAPEFILE, config, ((IImageLayer) l).getImageReader());
                    GeometricLayer gl = shpio.read();
                    // if 5 args, set a specific name
                    if (args.length == 3) {
                        if (gl != null) {
                            gl.setName(args[3]);
                        }
                    }
                    addLayerInThread("noncomplexlayer", gl, (IImageLayer) l);
                } catch (Exception ex) {
                    Logger.getLogger(AddInterpolatedConsoleAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void addSimpleCSV(String[] args) {
        if (args.length == 2) {
            Map config = new HashMap();
            config.put(GenericCSVIO.CONFIG_FILE, args[1].split("=")[1]);
            for (ILayer l : Platform.getLayerManager().getLayers()) {
                if (l instanceof IImageLayer & l.isActive()) {
                    AbstractVectorIO csv = VectorIOFactory.createVectorIO(VectorIOFactory.CSV, config, ((IImageLayer) l).getImageReader());
                    GeometricLayer positions = csv.read();
                    if (positions.getProjection() == null) {
                        addLayerInThread("complexvds", positions, (IImageLayer) l);
                    } else {
                        positions = GeometricLayer.createImageProjectedLayer(positions, ((IImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
                        addLayerInThread("complexvds", positions, (IImageLayer) l);
                    }
                }
            }
        } else {
            int returnVal = fd.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    lastDirectory = fd.getSelectedFile().getParent();
                    Map config = new HashMap();
                    config.put(GenericCSVIO.CONFIG_FILE, fd.getSelectedFile().getCanonicalPath());
                    for (ILayer l : Platform.getLayerManager().getLayers()) {
                        if (l instanceof IImageLayer & l.isActive()) {
                            AbstractVectorIO csv = VectorIOFactory.createVectorIO(VectorIOFactory.CSV, config, ((IImageLayer) l).getImageReader());
                            GeometricLayer positions = csv.read();
                            if (positions.getProjection() == null) {
                                addLayerInThread("complexvds", positions, (IImageLayer) l);
                            } else {
                                positions = GeometricLayer.createImageProjectedLayer(positions, ((IImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
                                addLayerInThread("complexvds", positions, (IImageLayer) l);
                            }
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(AddInterpolatedConsoleAction.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
            } else {
                return;
            }
        }
    }

    private void addSumo(String[] args) {
        if (args.length == 2) {
            Map config = new HashMap();
            config.put(SumoXmlIOOld.CONFIG_FILE, args[2].split("=")[3]);
            for (ILayer l : Platform.getLayerManager().getLayers()) {
                if (l instanceof IImageLayer & l.isActive()) {
                    AbstractVectorIO csv = VectorIOFactory.createVectorIO(VectorIOFactory.SUMO_OLD, config, ((IImageLayer) l).getImageReader());
                    GeometricLayer positions = csv.read();
                    if (positions.getProjection() == null) {
                        addLayerInThread("noncomplexlayer", positions, (IImageLayer) l);
                    } else {
                        positions = GeometricLayer.createImageProjectedLayer(positions, ((IImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
                        addLayerInThread("noncomplexlayer", positions, (IImageLayer) l);
                    }
                }
            }
        } else {
            int returnVal = fd.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    lastDirectory = fd.getSelectedFile().getParent();
                    Map config = new HashMap();
                    config.put(SumoXmlIOOld.CONFIG_FILE, fd.getSelectedFile().getCanonicalPath());
                    for (ILayer l : Platform.getLayerManager().getLayers()) {
                        if (l instanceof IImageLayer & l.isActive()) {
                            AbstractVectorIO csv = VectorIOFactory.createVectorIO(VectorIOFactory.SUMO_OLD, config, ((IImageLayer) l).getImageReader());
                            GeometricLayer positions = csv.read();
                            if (positions.getProjection() == null) {
                                addLayerInThread("complexvds", positions, (IImageLayer) l);
                            } else {
                                positions = GeometricLayer.createImageProjectedLayer(positions, ((IImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
                                addLayerInThread("complexvds", positions, (IImageLayer) l);
                            }
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(AddInterpolatedConsoleAction.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
            } else {
                return;
            }
        }
    }

    private void addGml(String[] args) {

        int returnVal = fd.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                lastDirectory = fd.getSelectedFile().getParent();
                Map config = new HashMap();
                config.put(SumoXmlIOOld.CONFIG_FILE, fd.getSelectedFile().getCanonicalPath());
                for (ILayer l : Platform.getLayerManager().getLayers()) {
                    if (l instanceof IImageLayer && l.isActive()) {
                        AbstractVectorIO csv = VectorIOFactory.createVectorIO(VectorIOFactory.GML, config, ((IImageLayer) l).getImageReader());
                        GeometricLayer positions = csv.read();
                        if (positions.getProjection() == null) {
                            addLayerInThread("complexvds", positions, (IImageLayer) l);
                        } else {
                            positions = GeometricLayer.createImageProjectedLayer(positions, ((IImageLayer) l).getImageReader().getGeoTransform(), positions.getProjection());
                            addLayerInThread("complexvds", positions, (IImageLayer) l);
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(AddInterpolatedConsoleAction.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        } else {
            return;
        }

    }

    public static IVectorLayer createLayer(String type, GeometricLayer layer, IImageLayer parent) {
        String[] schema = layer.getSchema();
        String[] types = layer.getSchemaTypes();
        boolean timestamplayer = false;
        String timecolumnname = "";
        if (type.equals("complexvds")) {
            Geometry frame = layer.getGeometries().get(0);
            if (!(frame instanceof Point)) {
                layer.remove(frame);
                Vector<Geometry> frames = new Vector<Geometry>();
                frames.add(frame);
                ComplexEditVDSVectorLayer clayer = new ComplexEditVDSVectorLayer(layer.getName(), parent, layer.getGeometryType(), layer);
                clayer.addGeometries("image frame", Color.BLUE, 1, SimpleVectorLayer.LINESTRING, frames, false);
                return clayer;
            } else {
                ComplexEditVDSVectorLayer clayer = new ComplexEditVDSVectorLayer(layer.getName(), parent, layer.getGeometryType(), layer);
                return clayer;
            }

        } else {
            for (int i = 0; i < types.length; i++) {
                String t = types[i];
                if (t.equals("Date") || t.equals("Timestamp")) {
                    timestamplayer = true;
                    timecolumnname = schema[i];
                    break;
                }
            }
            if (!timestamplayer) {
                return new SimpleEditVectorLayer(layer.getName(), parent, layer.getGeometryType(), layer);
            } else {
                TimeComponent.setDirty(true);
                return new TimeVectorLayer(layer.getName(), parent, layer.getGeometryType(), layer, timecolumnname);
            }
        }
    }

    public void addLayerInThread(final String type, final GeometricLayer layer, final IImageLayer il) {
        if (layer != null) {
            new Thread(new Runnable() {

                public void run() {
                    IVectorLayer ivl = createLayer(type, layer, il);
                    ivl.setColor(Color.GREEN);
                    ivl.setWidth(5);
                    il.addLayer((ILayer) ivl);
                    done = true;
                }
            }).start();
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JOptionPane.showMessageDialog(null, "Empty layer, not added to layers", "Warning", JOptionPane.ERROR_MESSAGE);
                }
            });
            done = true;
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
