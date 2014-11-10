/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.GeometricLayer;
import org.geoimage.viewer.core.api.ISelect;
import org.geoimage.viewer.util.GeometricLayerIO;

/**
 *
 * @author thoorfr
 */
public class SelectableVectorLayer extends SimpleVectorLayer implements ISelect {

    private String whereClause = null;

    public SelectableVectorLayer(String layername, IImageLayer parent, String type, GeometricLayer layer) {
        super(layername, parent, type, layer);
        buildDatabase(glayer);
    }

    @Override
    public void render(GeoContext context) {
        doSelect();
        super.render(context);

    }

    public void select(String whereClause) {
        this.whereClause = whereClause;
    }

    private void doSelect() {
        if (whereClause != null) {
            try {
                Connection conn = DriverManager.getConnection("jdbc:h2:~/.sumo/VectorData;AUTO_SERVER=TRUE", "sa", "");
                Statement stat = conn.createStatement();
                ResultSet rs = stat.executeQuery("SELECT * FROM \"" + glayer.getName() + "\" WHERE " + whereClause);
                WKTReader wkt = new WKTReader();
                String[] schema = glayer.getSchema();
                String[] types = glayer.getSchemaTypes();
                glayer.clear();
                while (rs.next()) {
                    Geometry geom = wkt.read(rs.getString("geom"));
                    Attributes att = Attributes.createAttributes(schema, types);
                    for (String key : schema) {
                        att.set(key, rs.getString(key));
                    }
                    glayer.put(geom, att);
                }
            } catch (Exception ex) {
                Logger.getLogger(SelectableVectorLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
            whereClause = null;
        }
    }

    private void buildDatabase(GeometricLayer glayer) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:h2:~/.sumo/VectorData;AUTO_SERVER=TRUE", "sa", "");
            Statement stat = conn.createStatement();
            stat.execute("DROP TABLE \"" + glayer.getName() + "\" IF EXISTS");
            String sql = null;
            File tempfile = File.createTempFile(glayer.getName(), ".csv");
            GeometricLayerIO.createCSV(glayer, tempfile.getAbsolutePath());
            sql = "create table \"" + glayer.getName() + "\" as select * from csvread('" + tempfile.getAbsolutePath() + "')";
            stat.execute(sql);
            stat.close();
            conn.close();
        } catch (Exception ex) {
            Logger.getLogger(SelectableVectorLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
