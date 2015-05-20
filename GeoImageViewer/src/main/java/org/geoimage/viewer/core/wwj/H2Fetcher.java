/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.wwj;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Polyline;
import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.geoimage.viewer.core.Catalogue;
import org.geoimage.viewer.core.ImagePlanning;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author thoorfr
 */
public class H2Fetcher {

    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("GeoImageViewerPU");

    public H2Fetcher() {
    }

    public static TimeRenderableLayer getGeometries() throws Exception {
        TimeRenderableLayer out = new TimeRenderableLayer();
        EntityManager em = emf.createEntityManager();
        Query q = em.createNamedQuery("Catalogue.findAll");
        List<Catalogue> images = q.getResultList();

        WKTReader wktr = new WKTReader();
        Geometry imageGeom = null;
        for (Catalogue image : images) {
            try {
                imageGeom = wktr.read(image.getGeom());
                List<LatLon> ll = new ArrayList<LatLon>();
                for (Coordinate c : imageGeom.getCoordinates()) {
                    ll.add(new LatLon(Angle.fromDegreesLatitude(c.y), Angle.fromDegreesLongitude(c.x)));
                }
                Coordinate c = imageGeom.getCentroid().getCoordinate();
                WWGeoImage gi = new WWGeoImage(new Polyline(ll, 1000), new GlobeAnnotation(image.getImagename(), new Position(new LatLon(Angle.fromDegreesLatitude(c.y), Angle.fromDegreesLongitude(c.x)), 2000)), Color.WHITE, Color.GREEN);
                out.addRenderable(gi, image.getDateCreation());
            } finally {
                continue;
            }
        }
        return out;
    }

    public static List<ImagePlanning> getImagePlanning(String dbname) throws Exception {
        // create list of image planning
        List<ImagePlanning> listImageplanning = new ArrayList<ImagePlanning>();
        // connect to database
        Connection conn = DriverManager.getConnection("jdbc:h2:~/.sumo/" + dbname + ";AUTO_SERVER=TRUE", "sa", "");
        Statement stat = conn.createStatement();
        // get image planning
        String sql = "SELECT * FROM IMAGEPLAN";
        ResultSet res = stat.executeQuery(sql);
        WKTReader wktr = new WKTReader();
        Geometry imageGeom = null;
        while (!res.isClosed() && res.next()) {
            ImagePlanning imagePlanning = new ImagePlanning();
            imagePlanning.setName(res.getString("IMAGE"));
            imagePlanning.setAcquisitionTime(Timestamp.valueOf(res.getString("STARTDATE")));
            imagePlanning.setAcquisitionStopTime(Timestamp.valueOf(res.getString("ENDDATE")));
            imagePlanning.setRemoteLocation(new URL(res.getString("URL")));
            imagePlanning.setAction(res.getString("ACTION"));
            try {
                imageGeom = wktr.read(res.getString("AREA"));
            } catch (Exception e) {
                imageGeom = null;
            }
            imagePlanning.setArea(imageGeom);
            // check the maximum time for acquisition
            if (imagePlanning.getAcquisitionStopTime().getTime() - System.currentTimeMillis() < 0) {
                try {
                    // remove the row from the database
                    stat.execute("DELETE FROM IMAGEPLAN WHERE IMAGE = '" + res.getString("IMAGE") + "' AND STARTDATE = '" + res.getString("STARTDATE") + "'");
                } catch (Exception e) {
                    System.out.println("Could not delete entry " + res.getString("IMAGE"));
                }
            } else {
                listImageplanning.add(imagePlanning);
            }
        }
        stat.close();
        conn.close();

        return listImageplanning;
    }

    public static void addImagePlanning(String dbname, File planningFile) throws Exception {
        // create xml reader
        SAXBuilder builder = new SAXBuilder();
        Document doc;
        doc = builder.build(planningFile);
        Element atts = doc.getRootElement();
        // check format first
        if (atts.getName().equalsIgnoreCase("imageplanning")) {
            // connect to database
            Connection conn = DriverManager.getConnection("jdbc:h2:~/.sumo/" + dbname + ";AUTO_SERVER=TRUE", "sa", "");
            Statement stat = conn.createStatement();
            String sql = "create table if not exists IMAGEPLAN (IMAGE VARCHAR(255), URL VARCHAR(1024), STARTDATE VARCHAR(255), ENDDATE VARCHAR(255), AREA VARCHAR(1024), ACTION VARCHAR(2048))";
            stat.execute(sql);
            // clear table before filling it in
            stat.execute("DELETE FROM IMAGEPLAN");
            // scan through images
            for (Object o : atts.getChildren("Image")) {
                Element element = (Element) o;
                // create sql statement
                sql = "INSERT INTO IMAGEPLAN VALUES('" + element.getChildText("name") + "', '" + element.getChildText("url") + "', '" + element.getChildText("startDate") + "', '" + element.getChildText("endDate") + "', '" + element.getChildText("area") + "', '" + element.getChildText("action") + "');";
                stat.execute(sql);
            }
            stat.close();
            conn.close();
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JOptionPane.showMessageDialog(null, "Format not supported", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

    }
}
