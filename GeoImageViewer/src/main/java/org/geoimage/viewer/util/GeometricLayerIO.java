/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;
import java.io.FileWriter;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.GeometricLayer;

/**
 *
 * @author thoorfr
 */
public class GeometricLayerIO {

    public static void createCSV(GeometricLayer glayer, String file) throws Exception {
        FileWriter fw = new FileWriter(file);
        fw.append("geom," + glayer.getSchema(',') + "\n");
        WKTWriter wkt = new WKTWriter();
        for (Geometry geom : glayer.getGeometries()) {
            fw.append("\"" + wkt.writeFormatted(geom) + "\",");
            Attributes att = glayer.getAttributes(geom);
            if (att == null || att.getSchema().length==0) {
                fw.append("\n");
                continue;
            }
            for (int i = 0; i < att.getSchema().length; i++) {
                String key = att.getSchema()[i];
                fw.append(att.get(key) + "");
                if (i < att.getSchema().length - 1) {
                    fw.append(",");
                } else {
                    fw.append("\n");
                }
            }
        }
        fw.flush();
        fw.close();
    }
}
