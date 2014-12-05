/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.GeoTransform;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.GeometricLayer;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.factory.VectorIOFactory;
import org.geoimage.viewer.core.layers.vectors.MaskVectorLayer;
import org.geoimage.viewer.util.PolygonOp;
import org.geoimage.viewer.widget.SelectParametersJDialog;
import org.h2.tools.Csv;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

/**
 *
 * @author leforth based on gabban
 *
 * this class manages the csv files. It uses a connection with the embedded H2 database for storing csv information, in a temporary table (TEMPCSV).
 * It takes as input the latitude and longitude columns of the csv and the date column (optional).
 *
 */
public class GenericCSVIO extends AbstractVectorIO {

    public static String CONFIG_FILE = "file";

    public GeometricLayer read(GeoImageReader reader) {

        try {
            String csvfilename = (String) config.get(CONFIG_FILE);
            GeometricLayer out = null;
            Connection conn = DriverManager.getConnection("jdbc:h2:~/.sumo/AIS;AUTO_SERVER=TRUE", "sa", "");
            Statement stat = conn.createStatement();
            stat.execute("DROP TABLE TEMPCSV IF EXISTS");
            String sql = null;

            ResultSet rs = Csv.getInstance().read(csvfilename, null, null);
            ResultSetMetaData meta = rs.getMetaData();

            //check if it is a sumo csv, based on Fix work
            if (meta.getColumnName(1).equals("type=point")) {
                config = new HashMap();
                config.put(SimpleCSVIO.CONFIG_FILE, csvfilename);
                AbstractVectorIO csv = VectorIOFactory.createVectorIO(VectorIOFactory.CSV, config);
                GeometricLayer gl = csv.read(reader);
                out = GeometricLayer.createImageProjectedLayer(gl,reader.getGeoTransform(), "EPSG:4326");

                return out;
            }

            SelectParametersJDialog ff2 = new SelectParametersJDialog(meta);
            ff2.setVisible(true);
            sql = "create table tempcsv as select * from csvread('" + csvfilename + "')";
            stat.execute(sql);

            String latName = ff2.getLatField();
            String lonName = ff2.getLonField();
            String dateName = "";
            if (ff2.getTimeField() != null) {
                dateName = ff2.getTimeField();
            }


            sql = "select * from tempcsv";
            stat.execute(sql);
            GeometricLayer gl = new GeometricLayer(MaskVectorLayer.POINT);
            gl.setName(new File((String) config.get(CONFIG_FILE)).getName());
            addCSVGeom("tempcsv", gl, latName, lonName, dateName, reader.getGeoTransform(),reader.getWidth(),reader.getHeight());
            out = GeometricLayer.createImageProjectedLayer(gl, reader.getGeoTransform(), "EPSG:4326");

            rs.close();
            stat.close();
            conn.close();


            return out;

        } catch (Exception ex) {
            Logger.getLogger(GenericCSVIO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * 
     */
    //TODO: funziona solo con i points , bisogna correggerlo per salvare correttamente anche i poligoni
    public void save(GeometricLayer glayer, String projection,GeoImageReader gir) {
    	GeoTransform transform=gir.getGeoTransform();
        try {
           /* if (!glayer.getGeometryType().equals(GeometricLayer.POINT)) {
                return;
            }*/
            String file = (String) config.get(CONFIG_FILE);
            new File(file).createNewFile();
            FileWriter fis = new FileWriter(file);
            if (projection == null) {
                fis.write("x,y," + glayer.getSchema(',') + "\n");
            } else {
                fis.write("lat,lon," + glayer.getSchema(',') + "\n");
            }
            String[] schema = glayer.getSchema();
            for (Geometry geom : glayer.getGeometries()) {
                if (projection == null) {
                    Coordinate pos = geom.getCoordinate();
                    fis.write(pos.x + "," + pos.y);
                } else {
                    Coordinate pos = geom.getCoordinate();
                    double[] temp = transform.getGeoFromPixel(pos.x, pos.y, projection);
                    fis.write(temp[1] + "," + temp[0]);

                }
                if (schema.length != 0) {
                    fis.write(",");
                }
                Attributes atts = glayer.getAttributes(geom);
                for (int i = 0; i < schema.length; i++) {

                    String s = "";
                    if(atts.get(schema[i])!= null){
                        s=atts.get(schema[i]).toString();
                    }
                    fis.write( s + (i == schema.length - 1 ? "" : ","));
                }
                fis.write("\n");
            }
            fis.flush();
            fis.close();
        } catch (IOException ex) {
            Logger.getLogger(GenericCSVIO.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    private Geometry parse(String string) {
        if (string.endsWith(";")) {
            string = string.substring(0, string.length() - 1);
        }
        GeometryFactory gf = new GeometryFactory();
        String[] pos = string.split(";");
        if (pos.length == 1) {
            String[] point = pos[0].split(" ");
            return gf.createPoint(new Coordinate(Double.parseDouble(point[0]), Double.parseDouble(point[1])));
        } else {
            Vector<Coordinate> coords = new Vector<Coordinate>();
            for (String point : pos) {
                String[] cc = point.split(" ");
                coords.add(new Coordinate(Double.parseDouble(cc[0]), Double.parseDouble(cc[1])));
            }
            return gf.createPolygon(gf.createLinearRing(coords.toArray(new Coordinate[coords.size()])), null);
        }
    }

    private void addGenericCSV(String[] args) {
        try {

            if (args.length > 0) {
                String csvfilename = args[0];
                Connection conn = DriverManager.getConnection("jdbc:h2:~/.sumo/AIS;AUTO_SERVER=TRUE", "sa", "");
                Statement stat = conn.createStatement();
                stat.execute("DROP TABLE TEMPCSV IF EXISTS");
                String sql = null;

                //"CREATE TABLE CSV (MMSI VARCHAR, IMO VARCHAR, date TIMESTAMP, Lat REAL, Lon REAL, Heading VARCHAR, SoG REAL, Width REAL, Length REAL, Draught REAL, Name VARCHAR, Callsign VARCHAR, Destination VARCHAR, ETA VARCHAR, Status VARCHAR, VeselType VARCHAR, ExtraInfo VARCHAR)";
                //stat.execute(sql);

                ResultSet rs = Csv.getInstance().read(args[0], null, null);
                ResultSetMetaData meta = rs.getMetaData();
                SelectParametersJDialog ff2 = new SelectParametersJDialog(meta);
                ff2.setVisible(true);
                sql = "create table tempcsv as select * from csvread('" + csvfilename + "')";
                stat.execute(sql);

                String latName = ff2.getLatField();
                String lonName = ff2.getLonField();
                String dateName = ff2.getTimeField();

                sql = "select * from tempcsv";
                stat.execute(sql);
                GeometricLayer gl = new GeometricLayer(MaskVectorLayer.POINT);
                //gl.setName(csvfilename.substring(csvfilename.lastIndexOf("/") + 1, csvfilename.length()));
                gl.setName(new File((String) config.get(CONFIG_FILE)).getName());
                IImageLayer l=Platform.getCurrentImageLayer();
                if(l!=null){
                	GeoImageReader reader=l.getImageReader();
                	addCSVGeom("tempcsv", gl, latName, lonName, dateName, reader.getGeoTransform(),reader.getWidth(),reader.getHeight());
                	gl = GeometricLayer.createImageProjectedLayer(gl, ((IImageLayer) l).getImageReader().getGeoTransform(), "EPSG:4326");
                }
                rs.close();
                stat.close();
                conn.close();

            }

        } catch (SQLException ex) {
            Logger.getLogger(GenericCSVIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addCSVGeom(String tableName, GeometricLayer gl, String lat, String lon, String date, GeoTransform gt,int width,int height) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:~/.sumo/AIS;AUTO_SERVER=TRUE", "sa", "");
        Statement stat = conn.createStatement();
        String sql = "select * from " + tableName;
        ResultSet rset = stat.executeQuery(sql);
        ResultSetMetaData rsetM = rset.getMetaData();
        int numcols = rsetM.getColumnCount();
        String[] attributes = new String[numcols];
        String[] types = new String[numcols];
        for (int i = 0; i < numcols; i++) {
            attributes[i] = rsetM.getColumnName(i + 1);
            int type = rsetM.getColumnType(i + 1);
            switch (type) {
                case 12:
                    types[i] = "String";
                    break;
                case 93:
                    types[i] = "Date";
                    break;
                case 7:
                    types[i] = "Double";
                    break;
            }
            if (attributes[i].equals(date)) {
                types[i] = "Date";
            }

        }
        Attributes atts = Attributes.createAttributes(attributes, types);
        GeometryFactory gf = new GeometryFactory();
        Geometry geom = null;

        try {
            double[] x0;
            double[] x1;
            double[] x2;
            double[] x3;
            int margin = Integer.parseInt(java.util.ResourceBundle.getBundle("GeoImageViewer").getString("SimpleShapeFileIO.margin"));
            x0 = gt.getGeoFromPixel(-margin, -margin, "EPSG:4326");
            x2 = gt.getGeoFromPixel(margin + width, margin + height, "EPSG:4326");
            x3 = gt.getGeoFromPixel(margin + width, -margin, "EPSG:4326");
            x1 = gt.getGeoFromPixel(-margin, margin + height, "EPSG:4326");
            
            Polygon imageP=PolygonOp.createPolygon(x0,x1,x2,x3,x0);
            
            int count=0;
            while (rset.next()) {
                if(count==0){
                    count++;
                    for(int i=0;i<numcols;i++){
                        try{
                            Double.parseDouble(rset.getString(i+1));
                            types[i]="Double";
                        }catch(Exception e){

                        }
                    }
                }
                atts = Attributes.createAttributes(attributes, types);
                for (int i = 0; i < numcols; i++) {
                    if (attributes[i].equals(date)) {
                        atts.set(attributes[i], Timestamp.valueOf(rset.getString(i + 1)));
                    } else {
                        if(types[i].equals("Double")){
                            atts.set(attributes[i], new Double(rset.getString(i + 1)));
                        }
                        else{
                            atts.set(attributes[i], rset.getString(i + 1));
                        }
                    }
                }
                geom = gf.createPoint(new Coordinate(Double.parseDouble(rset.getString(lon)), Double.parseDouble(rset.getString(lat))));
                if (imageP.contains(geom)) {
                    gl.put(geom, atts);
                }

            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JOptionPane.showMessageDialog(null, "Problem with date format", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            System.out.println("Problem with date format");
        }
        stat.close();
        conn.close();
    }
}
