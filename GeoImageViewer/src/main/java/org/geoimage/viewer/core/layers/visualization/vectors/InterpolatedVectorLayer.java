/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.visualization.vectors;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.opengl.OpenGLContext;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.io.AbstractVectorIO;
import org.geoimage.viewer.core.io.GenericCSVIO;
import org.geoimage.viewer.core.io.SimpleShapefile;
import org.geoimage.viewer.core.io.SumoXmlIOOld;
import org.geoimage.viewer.core.layers.GenericLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.visualization.LayerPickedData;
import org.jrc.sumo.core.api.IClickable;
import org.jrc.sumo.core.api.ISave;
import org.jrc.sumo.util.files.FileTypes;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author thoorfr
 */
public class InterpolatedVectorLayer extends GenericLayer implements  ISave, IClickable {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(InterpolatedVectorLayer.class);

    protected boolean active = true;
    protected  GeoImageReader reader;
    protected GeometricLayer glayer;
    protected String type;
    protected float renderWidth = 1;
    protected Color color = new Color(1f, 1f, 1f);
    protected Geometry selectedGeometry;
    private Date date;
    private String idColumn;
    private HashMap<Point, ArrayList<Point>> links = new HashMap<Point, ArrayList<Point>>();
    private String dateColumn;
    private List<Point> interpolated = new ArrayList<Point>();

    public InterpolatedVectorLayer(String layername, GeoImageReader reader, GeometricLayer layer, String idColumn, String dateColumn, Date date) {
    	super(null,layername,null,layer);
        this.date = date;
        this.glayer = layer;
        this.idColumn = idColumn;
        this.dateColumn = dateColumn;
        this.reader=reader;
        createRenderingLayer();
    }


    public void render(OpenGLContext context) {
        if (!context.isDirty()) {
            return;
        }
        int x = context.getX(), y = context.getY();
        float zoom = context.getZoom(), width = context.getWidth() * zoom, height = context.getHeight() * zoom;
        GL2 gl = context.getGL().getGL2();
        float[] c = color.getColorComponents(null);
        gl.glColor3f(c[0], c[1], c[2]);

        if (glayer != null) {
            gl.glPointSize(this.renderWidth);
            gl.glBegin(GL.GL_POINTS);
            for (Point temp : interpolated) {
                Coordinate point = temp.getCoordinate();
                gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);

            }
            gl.glEnd();
            gl.glFlush();
            if (selectedGeometry != null) {
                gl.glPointSize(this.renderWidth * 3);
                gl.glBegin(GL.GL_POINTS);
                for (Point temp : links.get(selectedGeometry)) {
                    Coordinate point = temp.getCoordinate();
                    gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);

                }
                gl.glEnd();
                gl.glFlush();
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRadio() {
        return false;
    }

    public String getDescription() {
        return getName();
    }

    public void dispose() {
        glayer = null;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float getWidth() {
        return this.renderWidth;
    }

    public void setWidth(float width) {
        this.renderWidth = width;
    }

    public void save(String file, int formattype, String projection) {
    	switch (formattype){
    	
	    	case AbstractVectorIO.SIMPLE_SHAPEFILE:{
	            try {
	                SimpleShapefile.exportLayer(new File(file),glayer,projection,((SarImageReader)reader).getGeoTransform());
	            } catch (Exception ex) {
	            	logger.error(ex.getMessage(),ex);
	            }
	            break;
	        }
	    	case AbstractVectorIO.SUMO_OLD:{
	            try {
	                SumoXmlIOOld.export(new File(file), glayer, projection, (SarImageReader)reader);
	            } catch (Exception ex) {
	            	logger.error(ex.getMessage(),ex);
	            }
	            break;
	        }
	    	case AbstractVectorIO.CSV:{
	            try {
	                GenericCSVIO.export(new File(file),glayer, projection,reader.getGeoTransform(),false);
	            } catch (Exception ex) {
	            	logger.error(ex.getMessage(),ex);
	            }
	            break;
	        }
    	}
    }

    public FileTypes[] getFileFormatTypes() {
    	FileTypes[] opts=new FileTypes[3];
    	opts[0]=new FileTypes(ISave.OPT_EXPORT_CSV,ISave.STR_EXPORT_CSV); 
    	opts[1]=new FileTypes(ISave.OPT_EXPORT_SHP,ISave.STR_EXPORT_SHP);
    	opts[2]=new FileTypes(ISave.OPT_EXPORT_XML_SUMO_OLD,ISave.STR_EXPORT_XML_SUMO_OLD);
        return opts; 
        
    }

    public boolean intersects(int x, int y, int width, int height) {
        return false;
    }

    public void mouseClicked(java.awt.Point imagePosition, int button) {
    	OpenGLContext context=SumoPlatform.getApplication().getGeoContext();
        this.selectedGeometry = null;
        GeometryFactory gf = new GeometryFactory();
        Point p = gf.createPoint(new Coordinate(imagePosition.x, imagePosition.y));
        for (Point temp : interpolated) {
            if (temp.isWithinDistance(p, 5 * context.getZoom())) {
                this.selectedGeometry = temp;
                LayerPickedData.put(temp, glayer.getAttributes(temp));
            }
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GeometricLayer getGeometriclayer() {
        return glayer;
    }

    public void setGeometriclayer(GeometricLayer glayer) {
        this.glayer = glayer;
    }

    public boolean contains(int x, int y) {
        return false;
    }

    public BufferedImage rasterize(BufferedImage image, int offsetX, int offsetY, double scalingFactor) {
        Rectangle rect = image.getRaster().getBounds();
        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coords = new Coordinate[]{
            new Coordinate((int) (((double) rect.getMinX() / scalingFactor) + offsetX), (int) (((double) rect.getMinY() / scalingFactor) + offsetY)),
            new Coordinate((int) (((double) rect.getMaxX() / scalingFactor) + offsetX), (int) (((double) rect.getMinY() / scalingFactor) + offsetY)),
            new Coordinate((int) (((double) rect.getMaxX() / scalingFactor) + offsetX), (int) (((double) rect.getMaxY() / scalingFactor) + offsetY)),
            new Coordinate((int) (((double) rect.getMinX() / scalingFactor) + offsetX), (int) (((double) rect.getMaxY() / scalingFactor) + offsetY)),
            new Coordinate((int) (((double) rect.getMinX() / scalingFactor) + offsetX), (int) (((double) rect.getMinY() / scalingFactor) + offsetY)),
        };
        Polygon geom = gf.createPolygon(gf.createLinearRing(coords), null);
        Graphics g2d = image.getGraphics();
        g2d.setColor(Color.WHITE);
        for (Geometry p : glayer.getGeometries()) {
            if (p.intersects(geom)) {
                int[] xPoints = new int[p.getNumPoints()];
                int[] yPoints = new int[p.getNumPoints()];
                int i = 0;
                for (Coordinate c : p.getCoordinates()) {
                    xPoints[i] = (int) ((c.x - offsetX) * scalingFactor);
                    yPoints[i++] = (int) ((c.y - offsetY) * scalingFactor);
                }
                g2d.fillPolygon(xPoints, yPoints, p.getNumPoints());
            }
        }
        g2d.dispose();
        return image;
    }

    public Area getShape() {
        Area maskArea = new Area();
        Rectangle rect = new Rectangle(0, 0, reader.getWidth(), reader.getHeight());
        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coords = new Coordinate[]{
            new Coordinate((int) rect.getMinX(), (int) rect.getMinY()),
            new Coordinate((int) rect.getMaxX(), (int) rect.getMinY()),
            new Coordinate((int) rect.getMaxX(), (int) rect.getMaxY()),
            new Coordinate((int) rect.getMinX(), (int) rect.getMaxY()),
            new Coordinate((int) rect.getMinX(), (int) rect.getMinY()),
        };
        Polygon geom = gf.createPolygon(gf.createLinearRing(coords), null);
        for (Geometry p : glayer.getGeometries()) {
            if (p.intersects(geom)) {
                int[] xPoints = new int[p.getNumPoints()];
                int[] yPoints = new int[p.getNumPoints()];
                int i = 0;
                for (Coordinate c : p.getCoordinates()) {
                    xPoints[i] = (int) (c.x);
                    yPoints[i++] = (int) (c.y);
                }
                maskArea.add(new Area(new java.awt.Polygon(xPoints, yPoints, p.getNumPoints())));
            }
        }
        return maskArea;
    }

    private void createRenderingLayer() {
        HashMap<Object, ArrayList<Point>> associations = new HashMap<Object, ArrayList<Point>>();
        GeometryFactory gf = new GeometryFactory();
        for (Geometry geom : glayer.getGeometries()) {
            Object id = glayer.getAttributes(geom).get(this.idColumn);
            ArrayList<Point> list = associations.get(id);
            if (list == null) {
                list = new ArrayList<Point>();
                associations.put(id, list);
            }
            list.add((Point) geom);
        }

        for (Object id : associations.keySet()) {
            ArrayList<Point> list = associations.get(id);
            if (list == null) {
                continue;
            }
            if (list.size() == 1) {
                links.put((Point) list.get(0), list);
                interpolated.add((Point) list.get(0));
            } else {
                Point before = null;
                Date beforeDate = null;
                Point after = null;
                Date afterDate = null;
                for (Point geom : list) {
                    Date temp = (Date) glayer.getAttributes(geom).get(this.dateColumn);
                    if (date.after(temp)) {
                        if (before == null) {
                            before = geom;
                            beforeDate = temp;
                        } else if (temp.after(beforeDate)) {
                            before = geom;
                            beforeDate = temp;
                        }
                    } else if (date.before(temp)) {
                        if (after == null) {
                            after = geom;
                            afterDate = temp;
                        } else if (temp.before(afterDate)) {
                            after = geom;
                            afterDate = temp;
                        }
                    }
                }
                if (before == null) {
                    links.put(after, list);
                    interpolated.add(after);
                } else if (after == null) {
                    links.put(before, list);
                    interpolated.add(before);
                } else {
                    long b = beforeDate.getTime();
                    long a = afterDate.getTime();
                    long m = date.getTime();
                    double ratio = (a - m) / (1.0 * a - b);
                    Point p = gf.createPoint(new Coordinate(after.getX() - ratio * (after.getX() - before.getX()), after.getY() - ratio * (after.getY() - before.getY())));
                    links.put(p, list);
                    interpolated.add(p);
                }
            }
        }
    }


}
