package org.geoimage.viewer.core.layers;

import java.awt.Color;
import java.io.File;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.SarImageReader;
import org.geoimage.viewer.common.OptionMenu;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.Attributes;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.ISave;
import org.geoimage.viewer.core.api.IThreshable;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.io.GenericCSVIO;
import org.geoimage.viewer.core.io.SimpleShapefile;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GenericLayer implements ILayer, ISave, IThreshable{
	protected String name = "";
	protected boolean active = true;
	protected boolean isRadio = false;
	protected GeometricLayer glayer;
	protected Color color = new Color(1f, 1f, 1f);
	protected ILayer parent=null;
	protected boolean threshable = false;
	protected float renderWidth = 1;
	protected String type;
	protected Geometry selectedGeometry;
	public static enum symbol {point, circle, square, triangle, cross};
	protected symbol displaysymbol = symbol.point;
	protected double currentThresh = 0;
	protected double minThresh = 0;
	protected double maxThresh = 0;
	
	/**
	 * 
	 * @param parent
	 * @param layername
	 * @param type
	 * @param layer
	 */
	public GenericLayer(ILayer parent,String layername,String type, GeometricLayer layer) {
    	this.parent=parent;
        this.name = layername;
        this.displaysymbol = symbol.point;
        this.type = type;
    }
	
	@Override
	public void render(GeoContext context){
		  if (!context.isDirty()||Platform.isBatchMode()) {
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
	        	List<Geometry> geomList=glayer.getGeometries();

	        	if (!threshable) {
	                if (getType().equalsIgnoreCase(GeometricLayer.POINT)) {
	                    switch (this.displaysymbol) {
	                        case point: {
	                            gl.glPointSize(this.renderWidth);
	                            gl.glBegin(GL.GL_POINTS);
	                            for (Geometry temp : geomList) {
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
	                        	gl.glBegin(GL.GL_POINTS);
	                        	gl.glLineWidth(this.renderWidth);
	                        	for (int ii=0;ii<geomList.size();ii++) {
	                        	   Geometry temp =geomList.get(ii);
		                           Coordinate point = temp.getCoordinate();
		                           double dx=(point.x - x) / width;
		                           double dy=1 - (point.y - y) / height;
	                        	   for (int i=0; i < 360; i++){
	                        		   //double angle = 2 * Math.PI * i / 360;
	                        		   double xx = dx+Math.sin(i)*0.005;
	                        		   double yy = dy+Math.cos(i)*0.005;
	                        		   
	                        		   gl.glVertex2d(xx,yy);
	                        		   //System.out.println(""+xx+"--"+yy);
	                        	   }
		                        } 
	                    	    gl.glEnd();
	                            gl.glFlush();
	                        }
	                        break;
	                        case square: {
	                        	//usato anche per disegnare i contorni delle detection
	                            for (Geometry temp : geomList) {
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
	                            for (Geometry temp : geomList) {
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
	                            for (Geometry temp : geomList) {
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
	                } else if (getType().equalsIgnoreCase(GeometricLayer.POLYGON)) {
	                    for (Geometry tmp : geomList) {
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
	                } else if (getType().equalsIgnoreCase(GeometricLayer.LINESTRING)) {
	                    for (Geometry temp : geomList) {
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
	                } else if (getType().equalsIgnoreCase(GeometricLayer.MIXED)) {
	                    for (Geometry temp : geomList) {
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
	                if (getType().equalsIgnoreCase(GeometricLayer.POINT)) {
	                    switch (this.displaysymbol) {
	                        case point: {
	                            gl.glPointSize(this.renderWidth);
	                            gl.glBegin(GL.GL_POINTS);
	                            for (Geometry temp : geomList) {
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
	                        	gl.glBegin(GL.GL_POINTS);
	                        	gl.glLineWidth(this.renderWidth);
	                        	for (int ii=0;ii<geomList.size();ii++) {
	                        	   Geometry temp =geomList.get(ii);
		                           Coordinate point = temp.getCoordinate();
		                           double dx=(point.x - x) / width;
		                           double dy=1 - (point.y - y) / height;
	                        	   for (int i=0; i < 360; i++){
	                        		   //double angle = 2 * Math.PI * i / 360;
	                        		   double xx = dx+Math.sin(i)*0.005;
	                        		   double yy = dy+Math.cos(i)*0.005;
	                        		   
	                        		   gl.glVertex2d(xx,yy);
	                        		   //System.out.println(""+xx+"--"+yy);
	                        	   }
		                        } 
	                    	    gl.glEnd();
	                            gl.glFlush();
	                        }
	                        break;
	                        case square: {
	                            for (Geometry temp : geomList) {
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
	                            for (Geometry temp : geomList) {
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
	                            for (Geometry temp : geomList) {
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
	                } else if (getType().equalsIgnoreCase(GeometricLayer.POLYGON)) {
	                    for (Geometry temp : geomList) {
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
	                } else if (getType().equalsIgnoreCase(GeometricLayer.LINESTRING)) {
	                    for (Geometry temp : geomList) {
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
	                } else if (getType().equalsIgnoreCase(GeometricLayer.MIXED)) {
	                    for (Geometry temp : geomList) {
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

	

	public boolean isActive() {
        return active;
    }

	public void setActive(boolean active) {
        this.active=active;
    }
	
	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
	
	
    public boolean isRadio() {
        return isRadio;
    }

    public void setIsRadio(boolean radio) {
        isRadio = radio;
    }
    
    public void init(ILayer parent) {
		this.parent = parent;
	}

	
	public ILayer getParent() {
		return parent;
	}

	public void setParent(ILayer parent) {
		this.parent = parent;
	}
	
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public Geometry getSelectedGeometry() {
		return selectedGeometry;
	}

	public void setSelectedGeometry(Geometry selectedGeometry) {
		this.selectedGeometry = selectedGeometry;
	}
	
    public void setThresh(double thresh) {
        currentThresh = thresh;
    }
    public double getThresh() {
        return currentThresh;
    }

    
    public GeometricLayer getGeometriclayer() {
        return glayer;
    }

    public void setGeometriclayer(GeometricLayer glayer) {
        this.glayer = glayer;
    }
    
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void dispose() {
	}

	public void save(String file, int formattype, String projection) {
    	SarImageReader reader=(SarImageReader) Platform.getCurrentImageReader();
        if (formattype==ISave.OPT_EXPORT_CSV) {
            if (!file.endsWith(".csv")) {
                file = file.concat(".csv");
            }
            GenericCSVIO.export(new File(file),FactoryLayer.createThresholdedLayer(glayer,currentThresh,threshable), projection,reader.getGeoTransform(),false);
        } else if (formattype==ISave.OPT_EXPORT_SHP) {
            SimpleShapefile.exportLayer(new File(file),glayer,projection,reader.getGeoTransform());
        }

    }


	@Override
	 public OptionMenu[] getFileFormatTypes() {
    	OptionMenu[] opts=new OptionMenu[2];
    	opts[0]=new OptionMenu(ISave.OPT_EXPORT_CSV,ISave.STR_EXPORT_CSV); 
    	opts[1]=new OptionMenu(ISave.OPT_EXPORT_SHP,ISave.STR_EXPORT_SHP);
        return opts; 
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
    
    public symbol getDisplaysymbol() {
        return displaysymbol;
    }

    public void setDisplaysymbol(symbol displaysymbol) {
        this.displaysymbol = displaysymbol;
    }
    public double getMaximumThresh() {
        return maxThresh;
    }

    public double getMinimumThresh() {
        return minThresh;
    }

    public boolean isThreshable() {
        return threshable;
    }

}