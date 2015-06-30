/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.vectors;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.SarImageReader;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.common.OptionMenu;
import org.geoimage.viewer.core.PickedData;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.IClickable;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.ISave;
import org.geoimage.viewer.core.api.IThreshable;
import org.geoimage.viewer.core.api.IVectorLayer;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.io.GenericCSVIO;
import org.geoimage.viewer.core.io.SimpleShapefile;
import org.geoimage.viewer.core.layers.AbstractLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.util.PolygonOp;
import org.geotools.feature.SchemaException;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

/**
 *
 * @author thoorfr
 */
public class MaskVectorLayer extends AbstractLayer implements IVectorLayer, ISave, IMask, IClickable, IThreshable {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(MaskVectorLayer.class);

    public final static String POINT = GeometricLayer.POINT;
    public final static String POLYGON = GeometricLayer.POLYGON;
    public final static String LINESTRING = GeometricLayer.LINESTRING;
    public final static String MIXED = GeometricLayer.MIXED;
    protected boolean active = true;
    protected GeometricLayer glayer;
 //   protected Geometry total=null;
   	
	protected String type;
    protected String name;
    protected float renderWidth = 1;
    protected Color color = new Color(1f, 1f, 1f);
    protected Geometry selectedGeometry;
    private symbol displaysymbol = symbol.point;
    protected boolean threshable = false;
    private double minThresh = 0;
    private double maxThresh = 0;
    protected double currentThresh = 0;
    
    
    
    public MaskVectorLayer(ILayer parent,String layername,String type, GeometricLayer layer) {
    	super.parent=parent;
        this.name = layername;
        this.type = type;
        if (layer == null) {
            return;
        }
        this.glayer = layer;
        String test = glayer.getSchema('/');
        if (test.contains(VDSSchema.SIGNIFICANCE)) {
            calculateMaxMinTresh();
            threshable = true;
        }
    }
    


    public double getMaximumThresh() {
        return maxThresh;
    }

    public double getMinimumThresh() {
        return minThresh;
    }

    public void setThresh(double thresh) {
        currentThresh = thresh;
    }

    public boolean isThreshable() {
        return threshable;
    }

    private void calculateMaxMinTresh() {
        minThresh = Double.MAX_VALUE;
        maxThresh = Double.MIN_VALUE;
        for (Attributes att : glayer.getAttributes()) {
            double temp = new Double("" + att.get(VDSSchema.SIGNIFICANCE));
            if (temp < minThresh) {
                minThresh = temp;
            }
            if (temp > maxThresh) {
                maxThresh = temp;
            }
        }
        currentThresh = minThresh - 0.01;
    }

    public int[] getHistogram(int numClasses) {
        if (threshable) {
            int[] out = new int[numClasses];
            for (Attributes att : glayer.getAttributes()) {
                double temp = new Double("" + att.get(VDSSchema.SIGNIFICANCE));
                int classe = (int) ((numClasses - 1) * (temp - minThresh) / (maxThresh - minThresh));
                out[classe]++;
            }
            return out;
        }
        return null;
    }

    public double getThresh() {
        return currentThresh;
    }

  

    public static enum symbol {point, circle, square, triangle, cross};

 

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 
     * @param gl
     * @param cs
     * @param width
     * @param height
     * @param x
     * @param y
     */
    protected void drawPoly(GL2 gl,Coordinate[] cs,float width,float height,int x,int y,float rwidth){
    	gl.glLineWidth(rwidth);
        gl.glBegin(GL.GL_LINE_STRIP);
        for (int p = 0; p < cs.length; p++) {
        	double vx=(cs[p].x - x) / width;
        	double vy=1 - (cs[p].y - y) / height;
            gl.glVertex2d(vx,vy);
        }
       
        //close polygon
        Coordinate point = cs[0];
        gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
        gl.glEnd();
        gl.glFlush();
    }

     public void render(GeoContext context) {
        if (!context.isDirty()) {
            return;
        }
        int x = context.getX();
        int y = context.getY();
        float zoom = context.getZoom();
        float width = context.getWidth() * zoom;
        float height = context.getHeight() * zoom;
        
        GL2 gl = context.getGL().getGL2();
        float[] c = color.getColorComponents(null);
        gl.glColor3f(c[0], c[1], c[2]);

        if (glayer != null) {
            if (!threshable) {
                if (getType().equalsIgnoreCase(POINT)) {
                    switch (this.displaysymbol) {
                        case point: {
                            gl.glPointSize(this.renderWidth);
                            gl.glBegin(GL.GL_POINTS);
                            for (Geometry temp : glayer.getGeometries()) {
                                Coordinate point = temp.getCoordinate();
                                gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                            }
                            gl.glEnd();
                            gl.glFlush();
                            if (selectedGeometry != null) {
                                gl.glPointSize(this.renderWidth * 2);
                                gl.glBegin(GL.GL_POINTS);
                                Coordinate point = selectedGeometry.getCoordinate();
                                gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                                gl.glEnd();
                                gl.glFlush();
                            }
                        }
                        break;
                        case circle: {
                        }
                        break;
                        case square: {
                        	//usato anche per disegnare i contorni delle detection
                            for (Geometry temp : glayer.getGeometries()) {
                                gl.glLineWidth(temp == selectedGeometry ? this.renderWidth * 3 : this.renderWidth);
                                Coordinate point = new Coordinate(temp.getCoordinate());
                                point.x = (point.x - x) / width;
                                point.y = 1 - (point.y - y) / height;
                                double rectwidth = 0.01;
                                gl.glBegin(GL.GL_LINE_STRIP);
                                gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
                                gl.glVertex2d(point.x - rectwidth, point.y + rectwidth);
                                gl.glVertex2d(point.x + rectwidth, point.y + rectwidth);
                                gl.glVertex2d(point.x + rectwidth, point.y - rectwidth);
                                gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
                                gl.glEnd();
                                gl.glFlush();
                            }
                        }
                        break;
                        case cross: {
                            for (Geometry temp : glayer.getGeometries()) {
                                gl.glLineWidth(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
                                Coordinate point = new Coordinate(temp.getCoordinate());
                                point.x = (point.x - x) / width;
                                point.y = 1 - (point.y - y) / height;
                                double rectwidth = 0.01;
                                gl.glBegin(GL.GL_LINE_STRIP);
                                gl.glVertex2d(point.x - rectwidth, point.y);
                                gl.glVertex2d(point.x + rectwidth, point.y);
                                gl.glEnd();
                                gl.glBegin(GL.GL_LINE_STRIP);
                                gl.glVertex2d(point.x, point.y - rectwidth);
                                gl.glVertex2d(point.x, point.y + rectwidth);
                                gl.glEnd();
                                gl.glFlush();
                            }
                        }
                        break;
                        case triangle: {
                            for (Geometry temp : glayer.getGeometries()) {
                                gl.glLineWidth(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
                                Coordinate point = new Coordinate(temp.getCoordinate());
                                point.x = (point.x - x) / width;
                                point.y = 1 - (point.y - y) / height;
                                double rectwidth = 0.01;
                                gl.glBegin(GL.GL_LINE_STRIP);
                                gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
                                gl.glVertex2d(point.x, point.y + rectwidth);
                                gl.glVertex2d(point.x + rectwidth, point.y - rectwidth);
                                gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
                                gl.glEnd();
                                gl.glFlush();
                            }

                        }
                        break;
                        default: {
                        }
                    }
                } else if (getType().equalsIgnoreCase(POLYGON)) {
                    for (Geometry tmp : glayer.getGeometries()) {
                    /*	Geometry gg;
						try {
							gg = Platform.getCurrentImageReader().getGeoTransform().transformGeometryGeoFromPixel(tmp);
							System.out.println(gg.toText());
						} catch (GeoTransformException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
                    	
                    	if(tmp instanceof Polygon){
	                    	Polygon polygon=(Polygon)tmp;
	                        if (polygon.getCoordinates().length < 1) {
	                            continue;
	                        }
	                        float rWidth=polygon == selectedGeometry ? this.renderWidth * 2 : this.renderWidth;
	                        
	                        
	                        int interior=polygon.getNumInteriorRing();
	
	                        if(interior>0){
	                        	//draw external polygon
	                        	LineString line=polygon.getExteriorRing();
	                        	drawPoly(gl,line.getCoordinates(),width,height,x,y,rWidth);
	                        	//draw holes
	                        	for(int i=0;i<interior;i++){
	                        		LineString line2=polygon.getInteriorRingN(i);
	                        		drawPoly(gl,line2.getCoordinates(),width,height,x,y,rWidth);
	                        	}
	                       }else{
	                        	drawPoly(gl,polygon.getCoordinates(),width,height,x,y,rWidth);
	                       }
                    	}else if(tmp instanceof MultiPolygon){
                    		MultiPolygon mpolygon=(MultiPolygon)tmp;
                            if (mpolygon.getCoordinates().length < 1) {
                                continue;
                            }
                            float rWidth=mpolygon == selectedGeometry ? this.renderWidth * 2 : this.renderWidth;
                           	drawPoly(gl,mpolygon.getCoordinates(),width,height,x,y,rWidth);

                    	}
                    }
                } else if (getType().equalsIgnoreCase(LINESTRING)) {
                    for (Geometry temp : glayer.getGeometries()) {
                        if (temp.getCoordinates().length < 1) {
                            continue;
                        }
                        
                        gl.glLineWidth(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
                        gl.glBegin(GL.GL_LINE_STRIP);
                        Coordinate[] cs = temp.getCoordinates();
                        for (int p = 0; p < cs.length; p++) {
                            gl.glVertex2d((cs[p].x - x) / width, 1 - (cs[p].y - y) / height);
                        }
                        gl.glEnd();
                        gl.glFlush();
                    }
                } else if (getType().equalsIgnoreCase(MIXED)) {
                    for (Geometry temp : glayer.getGeometries()) {
                        if (temp.getCoordinates().length < 1) {
                            continue;
                        }
                        if (temp instanceof LineString) {
                            gl.glLineWidth(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
                            gl.glBegin(GL.GL_LINE_STRIP);
                            for (Coordinate point : temp.getCoordinates()) {
                                gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                            }
                            gl.glEnd();
                            gl.glFlush();
                        } else if (temp instanceof Polygon) {
                            gl.glLineWidth(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
                            gl.glBegin(GL.GL_LINE_STRIP);
                            for (Coordinate point : temp.getCoordinates()) {
                                gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                            }
                            gl.glEnd();
                            gl.glFlush();
                        } else if (temp instanceof Point) {
                            gl.glPointSize(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
                            gl.glBegin(GL.GL_POINTS);
                            Coordinate point = temp.getCoordinate();
                            gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                            gl.glEnd();
                            gl.glFlush();
                        }else if (temp instanceof MultiPoint) {
                            gl.glPointSize(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
                            gl.glBegin(GL.GL_LINE_STRIP);
                            Coordinate[] points=temp.getCoordinates();
                            for (int i=0;i<points.length-1;i++) {
                                gl.glVertex2d((points[i].x - x) / width, 1 - (points[i].y - y) / height);
                            }
                            gl.glEnd();
                            gl.glFlush();
                        }
                    }
                }
            } else {
                if (getType().equalsIgnoreCase(POINT)) {
                    switch (this.displaysymbol) {
                        case point: {
                            gl.glPointSize(this.renderWidth);
                            gl.glBegin(GL.GL_POINTS);
                            for (Geometry temp : glayer.getGeometries()) {
                                if (((Double) glayer.getAttributes(temp).get(VDSSchema.SIGNIFICANCE)) > currentThresh) {
                                    Coordinate point = temp.getCoordinate();
                                    gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                                }
                            }
                            gl.glEnd();
                            gl.glFlush();
                            if (selectedGeometry != null) {
                                gl.glPointSize(this.renderWidth * 2);
                                gl.glBegin(GL.GL_POINTS);
                                Coordinate point = selectedGeometry.getCoordinate();
                                gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                                gl.glEnd();
                                gl.glFlush();
                            }
                        }
                        break;
                        case circle: {
                        }
                        break;
                        case square: {
                            for (Geometry temp : glayer.getGeometries()) {
                                if (((Double) glayer.getAttributes(temp).get(VDSSchema.SIGNIFICANCE)) > currentThresh) {
                                    gl.glLineWidth(temp == selectedGeometry ? this.renderWidth * 3 : this.renderWidth);
                                    Coordinate point = new Coordinate(temp.getCoordinate());
                                    point.x = (point.x - x) / width;
                                    point.y = 1 - (point.y - y) / height;
                                    double rectwidth = 0.01;
                                    
                                    gl.glBegin(GL.GL_LINE_STRIP);
                                    gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
                                    gl.glVertex2d(point.x - rectwidth, point.y + rectwidth);
                                    gl.glVertex2d(point.x + rectwidth, point.y + rectwidth);
                                    gl.glVertex2d(point.x + rectwidth, point.y - rectwidth);
                                    gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
                                    gl.glEnd();
                                    gl.glFlush();
                                }
                            }
                        }
                        break;
                        case cross: {
                            for (Geometry temp : glayer.getGeometries()) {
                                if (((Double) glayer.getAttributes(temp).get(VDSSchema.SIGNIFICANCE)) > currentThresh) {
                                    gl.glLineWidth(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
                                    
                                    Coordinate point = new Coordinate(temp.getCoordinate());
                                    point.x = (point.x - x) / width;
                                    point.y = 1 - (point.y - y) / height;
                                    double rectwidth = 0.01;
                                    gl.glBegin(GL.GL_LINE_STRIP);
                                    gl.glVertex2d(point.x - rectwidth, point.y);
                                    gl.glVertex2d(point.x + rectwidth, point.y);
                                    gl.glEnd();
                                    gl.glBegin(GL.GL_LINE_STRIP);
                                    gl.glVertex2d(point.x, point.y - rectwidth);
                                    gl.glVertex2d(point.x, point.y + rectwidth);
                                    gl.glEnd();
                                    gl.glFlush();
                                }
                            }
                        }
                        break;
                        case triangle: {
                            for (Geometry temp : glayer.getGeometries()) {
                                if (((Double) glayer.getAttributes(temp).get(VDSSchema.SIGNIFICANCE)) > currentThresh) {
                                    gl.glLineWidth(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
                                    Coordinate point = new Coordinate(temp.getCoordinate());
                                    point.x = (point.x - x) / width;
                                    point.y = 1 - (point.y - y) / height;
                                    double rectwidth = 0.01;
                                    gl.glBegin(GL.GL_LINE_STRIP);
                                    gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
                                    gl.glVertex2d(point.x, point.y + rectwidth);
                                    gl.glVertex2d(point.x + rectwidth, point.y - rectwidth);
                                    gl.glVertex2d(point.x - rectwidth, point.y - rectwidth);
                                    gl.glEnd();
                                    gl.glFlush();
                                }
                            }

                        }
                        break;
                        default: {
                        }
                    }
                } else if (getType().equalsIgnoreCase(POLYGON)) {
                    for (Geometry temp : glayer.getGeometries()) {
                        if (((Double) glayer.getAttributes(temp).get(VDSSchema.SIGNIFICANCE)) > currentThresh) {
                            if (temp.getCoordinates().length < 1) {
                                continue;
                            }
                            
                            gl.glLineWidth(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
                            gl.glBegin(GL.GL_LINE_STRIP);
                            for (Coordinate point : temp.getCoordinates()) {
                                gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                            }
                            Coordinate point = temp.getCoordinates()[0];
                            gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                            gl.glEnd();
                            gl.glFlush();
                        }
                    }
                } else if (getType().equalsIgnoreCase(LINESTRING)) {
                    for (Geometry temp : glayer.getGeometries()) {
                        if (((Double) glayer.getAttributes(temp).get(VDSSchema.SIGNIFICANCE)) > currentThresh) {
                            if (temp.getCoordinates().length < 1) {
                                continue;
                            }
                            gl.glLineWidth(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
                            gl.glBegin(GL.GL_LINE_STRIP);
                            for (Coordinate point : temp.getCoordinates()) {
                                gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                            }
                            gl.glEnd();
                            gl.glFlush();
                        }
                    }
                } else if (getType().equalsIgnoreCase(MIXED)) {
                    for (Geometry temp : glayer.getGeometries()) {
                        if (((Double) glayer.getAttributes(temp).get(VDSSchema.SIGNIFICANCE)) > currentThresh) {
                            if (temp.getCoordinates().length < 1) {
                                continue;
                            }
                            if (temp instanceof LineString) {
                                gl.glLineWidth(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
                                gl.glBegin(GL.GL_LINE_STRIP);
                                for (Coordinate point : temp.getCoordinates()) {
                                    gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                                }
                                gl.glEnd();
                                gl.glFlush();
                            } else if (temp instanceof Polygon) {
                                gl.glLineWidth(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
                                gl.glBegin(GL.GL_LINE_STRIP);
                                for (Coordinate point : temp.getCoordinates()) {
                                    gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                                }
                                gl.glEnd();
                                gl.glFlush();
                            } else if (temp instanceof Point) {
                                gl.glPointSize(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
                                gl.glBegin(GL.GL_POINTS);
                                Coordinate point = temp.getCoordinate();
                                gl.glVertex2d((point.x - x) / width, 1 - (point.y - y) / height);
                                gl.glEnd();
                                gl.glFlush();
                            }
                        }
                    }
                }
            }
        }

    }

    public Geometry getSelectedGeometry() {
		return selectedGeometry;
	}

	public void setSelectedGeometry(Geometry selectedGeometry) {
		this.selectedGeometry = selectedGeometry;
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
    	if(glayer!=null){
    		glayer.clear();
    		glayer = null;
    	}	
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
    	SarImageReader reader=(SarImageReader) Platform.getCurrentImageReader();
        if (formattype==ISave.OPT_EXPORT_CSV) {
            if (!file.endsWith(".csv")) {
                file = file.concat(".csv");
            }
            GenericCSVIO.export(new File(file),FactoryLayer.createThresholdedLayer(glayer,currentThresh,threshable), projection,reader.getGeoTransform());
        } else if (formattype==ISave.OPT_EXPORT_SHP) {
            SimpleShapefile.exportLayer(new File(file),glayer,projection,reader.getGeoTransform());
        }

    }

    public OptionMenu[] getFileFormatTypes() {
    	OptionMenu[] opts=new OptionMenu[2];
    	opts[0]=new OptionMenu(ISave.OPT_EXPORT_CSV,ISave.STR_EXPORT_CSV); 
    	opts[1]=new OptionMenu(ISave.OPT_EXPORT_SHP,ISave.STR_EXPORT_SHP);
        return opts; 
    }

    public boolean intersects(int x, int y, int width, int height) {
        try {
            if (getType().equals("point")) {
                return false;
            }
            double[][]c={{x,y},{(x + width),y},{(x + width),(y + height)},{x, (y + height)},{x, y}};
            
            Geometry geom =(Geometry)(PolygonOp.createPolygon(c));
          //for test only
           /*  try {
					SimpleShapefile.exportGeometriesToShapeFile(glayer.getGeometries(),new File("F:\\SumoImgs\\export\\aaa2.shp") ,"Polygon");
				} catch (IOException | SchemaException e) {
					e.printStackTrace();
				}*/
            /*if(x>18400&&x<19000&&y>18700&&y<18900){
            	try {
            		List<Geometry>lg=new ArrayList<>();
            		lg.add(geom);
					SimpleShapefile.exportGeometriesToShapeFile(lg,new File("F:\\SumoImgs\\export\\gg"+x+"-"+y+".shp") ,"Polygon");
				} catch (IOException | SchemaException e) {
					e.printStackTrace();
				}
            }*/
            	if(glayer!=null){
            	//if(total==null){
        			GeometryFactory builder = new GeometryFactory();
	                Point p1 = builder.createPoint(new Coordinate(c[0][0],c[0][1]));
	                Point p2 = builder.createPoint(new Coordinate(c[1][0],c[1][1]));
	                Point p3 = builder.createPoint(new Coordinate(c[2][0],c[2][1]));
	                Point p4 = builder.createPoint(new Coordinate(c[3][0],c[3][1]));

		            for (Geometry pp : glayer.getGeometries()) {
		            	if(pp.isValid()){
			            	 if(pp.contains(p1)&&pp.contains(p2)&&pp.contains(p3)&&pp.contains(p4)){
			            		return true;
			            	 }
		            	}else{
		            		 Coordinate[] cs=pp.getCoordinates();
		            		 List<Coordinate>lcs=new ArrayList<Coordinate>();
		            		 lcs.addAll(Arrays.asList(cs));
		            		 lcs.add(cs[0]);
		            		 
		            		 Polygon e=builder.createPolygon(lcs.toArray(new Coordinate[0]));
			            	 if(e.contains(p1)&&e.contains(p2)&&e.contains(p3)&&e.contains(p4)){
			            		return true;
			            	 }

		            	}
		            }
            	}
            /*
		            	Geometry p=(Geometry) pp.clone();
		            	if(p instanceof MultiPolygon){
		            		MultiPolygon mp=(MultiPolygon)p;
		            		for(int i=0;i<mp.getNumGeometries();i++){
			             		Geometry g=mp.getGeometryN(i).buffer(0);
			             		if(!g.isValid()){
			            			 Geometry gg=g.convexHull();
			            			 if (gg.intersects(geom)||geom.intersects(gg)) 
			 		        			return true;
			            		}
		            		}	
		            	}else{
		            		if(!p.isValid()){
		            			 Geometry g=p.convexHull();
		            			 if (g.intersects(geom)||geom.intersects(g)) 
			 		        			return true;
		            		}else{
		            			if (p.intersects(geom)||geom.intersects(p)) 
		 		        			return true;
		            		}
				            /*if(total==null)
	            				total=p;
	            			else
	            				total=total.union(p);
	            			*/
		           // 	} 
		            	  
		          //  }
		          /*  try {
						SimpleShapefile.exportGeometriesToShapeFile(glayer.getGeometries(),new File("F:\\SumoImgs\\export\\totalXXX.shp") ,"Polygon");
	            		List<Geometry>lg=new ArrayList<>();
	            		lg.add(total);
						SimpleShapefile.exportGeometriesToShapeFile(lg,new File("F:\\SumoImgs\\export\\totalYYY.shp") ,"Polygon");
					} catch (IOException | SchemaException e) {
						e.printStackTrace();
					}*/
            
          //  if(total.intersects(geom))
          //  	return true;
        } catch (ParseException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return false;
    }
	/**
	 * 
	 */
    public void mouseClicked(java.awt.Point imagePosition, int button, GeoContext context) {
        this.selectedGeometry = null;
        GeometryFactory gf = new GeometryFactory();
        Point p = gf.createPoint(new Coordinate(imagePosition.x, imagePosition.y));
        for (Geometry temp : glayer.getGeometries()) {
            //if (temp.isWithinDistance(p, 5 * context.getZoom())) {
            if (p.equalsExact(temp, 5 * context.getZoom())) {
                this.selectedGeometry = temp;
                PickedData.put(temp, glayer.getAttributes(temp));
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
    
    /**
     * check if the layer contains the geometry
     * @param g
     * @return
     */
    public boolean contains(Geometry g) {
        for (Geometry p : glayer.getGeometries()) {
            if (p.contains(g)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean contains(int x, int y) {
        if (getType().equals(POINT)) {
            return false;
        }
        GeometryFactory gf = new GeometryFactory();
        Point geom = gf.createPoint(new Coordinate(x, y));
        for (Geometry p : glayer.getGeometries()) {
            if (p.contains(geom)) {
                return true;
            }
        }
        return false;
    }

    public boolean includes(int x, int y, int width, int height) {
        try {
            if (getType().equals("point")) {
                return false;
            }
            WKTReader wkt = new WKTReader();
            StringBuilder polyStr=new StringBuilder("POLYGON((" )
            						.append(x).append(" ")
            						.append(y).append(",")
            						.append((x + width)).append(" ")
            						.append(y).append(",")
            						.append((x + width)).append(" ")
            						.append((y + height)).append(",")
            						.append(x).append(" ")
            						.append((y + height)).append(",")
            						.append(x).append(" ")
            						.append(y).append("))");
            
            Geometry geom = wkt.read(polyStr.toString());
            for (Geometry p : glayer.getGeometries()) {
                if (geom.within(p)) {
                    return true;
                }
            }
            return false;
        } catch (ParseException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return false;
    }

    // rasterize the mask clipped with the Rectangle scaled back to full size with an offset onto a BufferedImage
    public BufferedImage rasterize(Rectangle rect, int offsetX, int offsetY, double scalingFactor) {
        // create the buffered image of the size of the Rectangle
        BufferedImage image = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_BYTE_BINARY);
        GeometryFactory gf = new GeometryFactory();
        // define the clipping region in full scale
        Coordinate[] coords = new Coordinate[]{
            new Coordinate((int) (((double) rect.getMinX() / scalingFactor)), (int) (((double) rect.getMinY() / scalingFactor))),
            new Coordinate((int) (((double) rect.getMaxX() / scalingFactor)), (int) (((double) rect.getMinY() / scalingFactor))),
            new Coordinate((int) (((double) rect.getMaxX() / scalingFactor)), (int) (((double) rect.getMaxY() / scalingFactor))),
            new Coordinate((int) (((double) rect.getMinX() / scalingFactor)), (int) (((double) rect.getMaxY() / scalingFactor))),
            new Coordinate((int) (((double) rect.getMinX() / scalingFactor)), (int) (((double) rect.getMinY() / scalingFactor))),};
        Polygon geom = gf.createPolygon(gf.createLinearRing(coords), null);
        Graphics g2d = image.getGraphics();
        g2d.setColor(Color.WHITE);
        for (Geometry p : glayer.getGeometries()) {
            if (p.intersects(geom)) {
                int[] xPoints = new int[p.getNumPoints()];
                int[] yPoints = new int[p.getNumPoints()];
                int i = 0;
                for (Coordinate c : p.getCoordinates()) {
                    xPoints[i] = (int) ((c.x + offsetX) * scalingFactor);
                    yPoints[i++] = (int) ((c.y + offsetY) * scalingFactor);
                }
                g2d.fillPolygon(xPoints, yPoints, p.getNumPoints());
            }
        }
        g2d.dispose();
        return image;
    }

    public Area getShape(int width, int height) {
        Area maskArea = new Area();
        
        Rectangle rect = new Rectangle(0, 0, width,height);//reader.getWidth(), reader.getHeight());

        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coords = new Coordinate[]{
            new Coordinate((int) rect.getMinX(), (int) rect.getMinY()),
            new Coordinate((int) rect.getMaxX(), (int) rect.getMinY()),
            new Coordinate((int) rect.getMaxX(), (int) rect.getMaxY()),
            new Coordinate((int) rect.getMinX(), (int) rect.getMaxY()),
            new Coordinate((int) rect.getMinX(), (int) rect.getMinY()),};
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

    public symbol getDisplaysymbol() {
        return displaysymbol;
    }

    public void setDisplaysymbol(symbol displaysymbol) {
        this.displaysymbol = displaysymbol;
    }

    
    /**
     * create the new buffered layer
     */
    public void buffer(double bufferingDistance) {
        Geometry[] bufferedGeom=glayer.getGeometries().toArray(new Geometry[0]);
        
        for (int i=0;i<bufferedGeom.length;i++) {
        	//applico il buffer alla geometria
            if(bufferingDistance>0)
            	bufferedGeom[i] = EnhancedPrecisionOp.buffer(bufferedGeom[i], bufferingDistance);
        	bufferedGeom[i] = PolygonOp.removeInteriorRing(bufferedGeom[i]);
        }
        // then merge them
        List<Geometry> newgeoms = new ArrayList<Geometry>();
        List<Geometry> remove = new ArrayList<Geometry>();
        
       
        //ciclo sulle nuove geometrie
        for (Geometry g : bufferedGeom) {
            boolean isnew = true;
            remove.clear();
            for (Geometry newg : newgeoms) {
                if (newg.contains(g)) { //se newg contiene g -> g deve essere rimossa
                    isnew = false;
                    break;
                } else if (g.contains(newg)) { //se g contiene newg -> newg deve essere rimossa
                    remove.add(newg);
                }
            }
            if (isnew) {
                newgeoms.add(g);
            }
            newgeoms.removeAll(remove);
        }
        glayer.clear();

        // assign new value
        for (Geometry geom :newgeoms) {
            glayer.put(geom);
        }
        
    }
    
  
    
    public List<Geometry> getGeometries() {
        return glayer.getGeometries();
    }


}
