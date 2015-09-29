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
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;
import org.geoimage.def.GeoTransform;
import org.geoimage.exception.GeoTransformException;
import org.geoimage.viewer.core.layers.AttributesGeometry;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 *
 * @author leforth based on gabban
 *
 *         this class manages the csv files. It uses a connection with the
 *         embedded H2 database for storing csv information, in a temporary
 *         table (TEMPCSV). It takes as input the latitude and longitude columns
 *         of the csv and the date column (optional).
 *
 */
public class GenericCSVIO extends AbstractVectorIO{
	private static org.slf4j.Logger logger = LoggerFactory
			.getLogger(GenericCSVIO.class);
	private File csvFile = null;
	private GeometricLayer glayer=null;
	
	
	

	public GenericCSVIO(File file) {
		csvFile = file;
	}

	public GenericCSVIO(String filePath) {
		csvFile = new File(filePath);
	}
	
	public void read() {
		glayer=readLayer();
	}
	
	/*public GeometricLayer readLayer() {
		GeometricLayer out = null;
		try {
			Connection conn = DriverManager.getConnection("jdbc:h2:~/.sumo/AIS;AUTO_SERVER=TRUE", "sa", "");
			Statement stat = conn.createStatement();
			stat.execute("DROP TABLE TEMPCSV IF EXISTS");
			String sql = null;

			ResultSet rs = Csv.getInstance().read(csvFile.getName(), null, null);
			ResultSetMetaData meta = rs.getMetaData();

			/* check if it is a sumo csv, based on Fix work
			if (meta.getColumnName(1).equals("type=point")) {
				GenericCSVIO csv = new GenericCSVIO(csvFile.getAbsolutePath());
				GeometricLayer gl = csv.read(reader);
				out = GeometricLayer.createImageProjectedLayer(gl,reader.getGeoTransform(), "EPSG:4326");

				return out;
			}*/
/*
			SelectParametersJDialog ff2 = new SelectParametersJDialog(meta);
			ff2.setVisible(true);
			sql = "create table tempcsv as select * from csvread('"	+ csvFile.getName() + "')";
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

			gl.setName(csvFile.getName());
			//addCSVGeom("tempcsv", gl, latName, lonName, dateName,transform, reader.getWidth(),reader.getHeight());
			out = GeometricLayer.createImageProjectedLayer(gl,transform, "EPSG:4326");

			rs.close();
			stat.close();
			conn.close();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		return out;
	}*/
	
	 public GeometricLayer readLayer() {
	        RandomAccessFile fss = null;
	        GeometricLayer out=null;
	        try {
	            fss = new RandomAccessFile(csvFile, "r");
	            String line = null;
	            //first line = info layer
	            final String[] layerinfo = fss.readLine().split(",");
	            //second line = fields name
	            final String[] titles = fss.readLine().split(",");
	            //third line = types
	            String[] types=fss.readLine().split(",");
	            
	            String geomtype = layerinfo[0].split("=")[1];
	            out = new GeometricLayer(geomtype);
	            out.setName(csvFile.getName().substring(0, csvFile.getName().lastIndexOf(".")));
	            
	            if(layerinfo.length==2){
	            	String proj = layerinfo[1].split("=")[1];
		            out.setProjection(proj);
	            }
	           
	            //read attributes 
	            String[] attributes=titles;
	            boolean usegeom=true;
	            if(titles[0].equals("geom")){
	            	//if file contains the geom , atts start from the second colums
                	types=ArrayUtils.subarray(types, 1,types.length);
                	attributes=ArrayUtils.subarray(titles, 1,titles.length);
                }else{
                	usegeom=false;
                }
	            
	            GeometryFactory factory=new GeometryFactory();
	            WKTReader parser=new WKTReader(factory);
	            
	            while ((line = fss.readLine()) != null) {
	                AttributesGeometry atts = new AttributesGeometry(attributes);
	                String[] val = line.split(",");

	                Geometry geom=null;
	                int startCol=1;
	                if(usegeom){
	                	geom = parser.read(val[0]);
	                }else{
	                	//create  geom from lat and lon
	                	double lat=Double.parseDouble(val[0]);
	                	double lon=Double.parseDouble(val[1]);
	                	geom=factory.createPoint(new Coordinate(lon,lat));
	                	startCol=0;
	                }
	                for (int i = startCol; i < val.length; i++) {
	                	
	                	String typ=types[i - startCol];
	                	String att=attributes[i - startCol];
	                	
	                	val[i]=val[i].replace("\"", "");
	                    if (typ.equals("Date")) {
	                        try {
	                        	//val[i]=val[i].trim().replace(" ","T");
	                            Timestamp t=Timestamp.valueOf(val[i]);
	                        	atts.set(att,t);
	                        } catch (Exception e) {
	                            atts.set(att, null);
	                        }
	                    } else  if (typ.equals("Double")) {
	                            try {
	                                atts.set(att, Double.valueOf(val[i].trim()));
	                            } catch (Exception e) {
	                                atts.set(att, 0);
	                            }
                        } else if (typ.equals("Integer")) {
	                                try {
	                                    atts.set(att, Integer.valueOf(val[i].trim()));
	                                } catch (Exception e) {
	                                    atts.set(att, 0);
	                                }
                        } else {
                            atts.set(att, val[i]);
                        }
	                }
	                out.put(geom,atts);
	            }
	        } catch (Exception ex) {
	        	logger.error(ex.getMessage(), ex);
	        } finally {
	            try {
	                fss.close();
	            } catch (IOException ex) {
	            	logger.error(ex.getMessage(), ex);
	            }
	        }
	        return out;
	    }

	/**
     * 
     */
	public void save(File output,String projection,GeoTransform transform) {
		export(output,glayer,projection,transform,false);
	}
	
	/**
	 * 
	 * @param output
	 * @param glayer
	 * @param projection
	 * @param transform
	 * @param append
	 */
	public static void export(File output,GeometricLayer glayer,String projection,GeoTransform transform,boolean append) {
		FileWriter fis=null;
		try {
			fis = new FileWriter(output);
			fis.write("type=" + glayer.getGeometryType());
            if (projection != null) {
                fis.write(",projection=" + projection + "\n");
            }
			if (projection == null) {
				fis.write("geom,x,y," + glayer.getSchema(',') + "\n");
			} else {
				fis.write("geom,lat,lon," + glayer.getSchema(',') + "\n");
			}
			
			//TODO: text the export and import csv after the schema modification
			fis.write(glayer.getGeometryType()+ '\n');//+",Double,Double,"+glayer.getSchemaTypes(',') + '\n');
			String[] schema = glayer.getSchema();
			for (Geometry geom : glayer.getGeometries()) {
				Geometry geomGeo=transform.transformGeometryGeoFromPixel(geom);
				fis.write("\""+geomGeo.toText()+"\""+",");
				Coordinate[] pos = geom.getCoordinates();
				
				StringBuffer cc=new StringBuffer("\"");
				
				for (int i = 0; i < pos.length; i++) {
					if (projection == null) {
						cc.append(pos[i].x + "," + pos[i].y);
					} else {
						double[] temp;
						try {
							temp = transform.getGeoFromPixel(pos[i].x, pos[i].y);
							cc.append(temp[1] + "," + temp[0]);
						} catch (GeoTransformException e) {
							logger.warn("Can't write coordinates:"+ e.getMessage());
						}
					}
					if(i!=pos.length-1)
						cc.append(" ");
				}
				if (schema.length != 0) {
					cc.append(",");
				}
				cc.append("\"");
				fis.write(cc.toString());
				
				AttributesGeometry atts = glayer.getAttributes(geom);
				for (int ii = 0; ii < schema.length; ii++) {
					String s = "";
					if (atts.get(schema[ii]) != null) {
						s = "\""+atts.get(schema[ii]).toString()+"\"";
					}
					fis.write(s + (ii == schema.length - 1 ? "" : ","));
				}

				fis.write('\n');
			}
			
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (GeoTransformException e1) {
			e1.printStackTrace();
		}finally{
			try {
				fis.flush();
				fis.close();
			} catch (IOException e) {
			}
		}

	}
/*
	public void save(GeometricLayer glayer, String projection) {
        try {
            String file = ((String) config.get(CONFIG_FILE)).replace('\\','/');
            new File(file).createNewFile();
            FileWriter fis = new FileWriter(file);
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
                        double[] temp = gir.getGeoTransform().getGeoFromPixel(pos.x, pos.y, projection);
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
        } catch (IOException ex) {
            Logger.getLogger(SimpleCSVIO.class.getName()).log(Level.SEVERE, null, ex);
        }


    }*/
	
	
	
	public static void createSimpleCSV(GeometricLayer glayer, String file,boolean append) throws IOException {
		FileWriter fw = new FileWriter(file,append);
		try{
	        if(!append)
	        	fw.append("geom," + glayer.getSchema(',') + "\n");
	        WKTWriter wkt = new WKTWriter();
	        for (Geometry geom : glayer.getGeometries()) {
	            fw.append("\"" + wkt.writeFormatted(geom) + "\",");
	            AttributesGeometry att = glayer.getAttributes(geom);
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
		}finally{    
			fw.flush();
			fw.close();
		}	
    }
	
	/**
	 * 
	 * @param output
	 * @param geoms
	 * @param transform
	 * @param imageId
	 * @param append
	 * @throws IOException 
	 */
	public static void geomCsv(File output,List<Geometry> geoms,GeoTransform transform,String imageId,boolean append) throws IOException {
		FileWriter fis=null;
		try {
			fis = new FileWriter(output,append);
			if(!append)
				fis.append("imageId,geom\n");

			for (Geometry geom : geoms) {
				if(transform!=null){
					try {
						geom = transform.transformGeometryGeoFromPixel(geom);
					} catch (GeoTransformException e) {
						logger.error("Geometry not saved in csv",e);
					}
				}
				fis.append(imageId+",\""+geom.toText()+"\"");
				fis.append("\n");
			}
			
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}finally{
			fis.flush();
			fis.close();
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
			return gf.createPoint(new Coordinate(Double.parseDouble(point[0]),
					Double.parseDouble(point[1])));
		} else {
			Vector<Coordinate> coords = new Vector<Coordinate>();
			for (String point : pos) {
				String[] cc = point.split(" ");
				coords.add(new Coordinate(Double.parseDouble(cc[0]), Double
						.parseDouble(cc[1])));
			}
			return gf.createPolygon(gf.createLinearRing(coords
					.toArray(new Coordinate[coords.size()])), null);
		}
	}


	
	
}
