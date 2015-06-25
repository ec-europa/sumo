/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.util.Vector;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.GeoTransform;
import org.geoimage.def.SarImageReader;
import org.geoimage.exception.GeoTransformException;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 *
 * @author thoorfr
 */
public class SimpleCSVIO extends AbstractVectorIO {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(SimpleCSVIO.class);
	private GeometricLayer glayer=null;
	
	private File file=null;
	
	public SimpleCSVIO(File input){
		this.file=input;
	}
    
	public void read() {
		glayer=createLayer();
	}
	
    public GeometricLayer createLayer() {
        RandomAccessFile fss = null;
        try {
            fss = new RandomAccessFile(file, "r");
            String line = null;
            String[] temp = fss.readLine().split(",");
            String type = temp[0].split("=")[1];
            GeometricLayer out = new GeometricLayer(type);
            out.setName(file.getName().substring(0, file.getPath().lastIndexOf(".")));
            if (temp.length == 2) {
                out.setProjection(temp[1].split("=")[1]);
            }
            line = fss.readLine();
            String[] attributes = null;
            if (line.equals("")) {
                attributes = new String[]{};
            } else {
                attributes = line.split(",");
            }
            line = fss.readLine();
            String[] types = null;
            if (line.equals("")) {
                types = new String[]{};
            } else {
                types = line.split(",");
            }
            while ((line = fss.readLine()) != null) {
                Attributes atts = Attributes.createAttributes(attributes, types);
                String[] val = line.split(",");
                if (val.length != attributes.length + 1) {
                    continue;
                }
                Geometry geom = parse(val[0]);
                for (int i = 1; i < val.length; i++) {
                    if (types[i - 1].equals("Date")) {
                        try {
                            atts.set(attributes[i - 1], Timestamp.valueOf(val[i]));
                        } catch (Exception e) {
                            atts.set(attributes[i - 1], null);
                        }
                    } else {
                        if (types[i - 1].equals("Double")) {
                            try {
                                atts.set(attributes[i - 1], Double.valueOf(val[i]));
                            } catch (Exception e) {
                                atts.set(attributes[i - 1], Double.NaN);
                            }
                        } else {
                            if (types[i - 1].equals("Integer")) {
                                try {
                                    atts.set(attributes[i - 1], Integer.valueOf(val[i]));
                                } catch (Exception e) {
                                    atts.set(attributes[i - 1], 0);
                                }
                            } else {
                                atts.set(attributes[i - 1], val[i]);
                            }

                        }
                    }
                }
                out.put(geom, atts);
            }
            return out;
        } catch (Exception ex) {
        	logger.error(ex.getMessage(), ex);
        } finally {
            try {
                fss.close();
            } catch (IOException ex) {
            	logger.error(ex.getMessage(), ex);
            }
        }
        return null;
    }

    
    
    @Override
	public void save(File output, String projection, GeoTransform transform) {
    	export(output,glayer,projection,transform); 
	}
    
    public static void export(File output, GeometricLayer glayer, String projection,GeoTransform transform) {
        try {
            FileWriter fis = new FileWriter(output);
            fis.write("type=" + glayer.getGeometryType());
            if (projection != null) {
                fis.write(",projection=" + projection + "\n");
            }
            fis.write(glayer.getSchema(',') + "\n");
            fis.write(glayer.getSchemaTypes(',') + "\n");
            String[] schema = glayer.getSchema();
            for (Geometry geom : glayer.getGeometries()) {
                if (projection == null) {
                    for (Coordinate pos : geom.getCoordinates()) {
                        fis.write(pos.x + " " + pos.y + ";");
                    }
                } else {
                    for (Coordinate pos : geom.getCoordinates()) {
                        double[] temp = transform.getGeoFromPixel(pos.x, pos.y);
                        fis.write(temp[0] + " " + temp[1] + ";");
                    }
                }
                if (schema.length != 0) {
                    fis.write(",");
                }
                Attributes atts = glayer.getAttributes(geom);
                for (int i = 0; i < schema.length; i++) {
                    Object o = atts.get(schema[i]);
                    if (o == null) {
                        fis.write(" " + (i == schema.length - 1 ? "" : ","));
                    } else {
                        fis.write(o.toString() + (i == schema.length - 1 ? "" : ","));
                    }
                }
                fis.write("\n");
            }
            fis.flush();
            fis.close();
        } catch (IOException |GeoTransformException ex) {
        	logger.error(ex.getMessage(), ex);
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

	


}
