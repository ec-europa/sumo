package org.geoimage.viewer.core.layers;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.SarImageReader;
import org.geoimage.opengl.GL2ShapesRender;
import org.geoimage.opengl.OpenGLContext;
import org.geoimage.viewer.common.OptionMenu;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.Attributes;
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
        this.glayer=layer;
    }
	

    @Override
	public void render(OpenGLContext context){
		  if (!context.isDirty()||Platform.isBatchMode()) {
	            return;
	        }
	        int x = context.getX();
	        int y = context.getY();
	        float zoom = context.getZoom();
	        float width = context.getWidth() * zoom;
	        float height = context.getHeight() * zoom;
	        
	        if (glayer != null) {
	        	List<Geometry> geomList=glayer.getGeometries();

	        	if (!threshable) {
	                if (getType().equalsIgnoreCase(GeometricLayer.POINT)) {
	                    switch (this.displaysymbol) {
	                        case point: {
	                        	GL2ShapesRender.renderPolygons(context, width, height, geomList, this.renderWidth, color);
	                            if (selectedGeometry != null) {
	                            	GL2ShapesRender.renderPoint(context, width, height, selectedGeometry.getCoordinate(), this.renderWidth, color);
	                            }
	                        }
	                        break;
	                        case circle: {
	                        	GL2ShapesRender.renderCircle(context, width, height, geomList, this.renderWidth, color);
	                        }
	                        break;
	                        case square: {
	                        	//usato anche per disegnare i contorni delle detection
	                        	GL2ShapesRender.renderSquare(context,width,height,geomList,selectedGeometry,renderWidth,color);
	                        }
	                        break;
	                        case cross: {
	                        	GL2ShapesRender.renderCross(context,width,height,geomList,selectedGeometry,renderWidth,color);
	                        }
	                        break;
	                        case triangle: {
	                        	GL2ShapesRender.renderTriangle(context,width,height,geomList,selectedGeometry,renderWidth,color);
	                        }
	                        break;
	                        default: {
	                        }
	                    }
	                } else if (getType().equalsIgnoreCase(GeometricLayer.POLYGON)) {
	                    for (Geometry tmp : geomList) {
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
		                        	GL2ShapesRender.drawPoly(context,line.getCoordinates(),width,height,x,y,rWidth,color);
		                        	//draw holes
		                        	for(int i=0;i<interior;i++){
		                        		LineString line2=polygon.getInteriorRingN(i);
		                        		GL2ShapesRender.drawPoly(context,line2.getCoordinates(),width,height,x,y,rWidth,color);
		                        	}
		                       }else{
		                    	   GL2ShapesRender.drawPoly(context,polygon.getCoordinates(),width,height,x,y,rWidth,color);
		                       }
	                    	}else if(tmp instanceof MultiPolygon){
	                    		MultiPolygon mpolygon=(MultiPolygon)tmp;
	                            if (mpolygon.getCoordinates().length < 1) {
	                                continue;
	                            }
	                            float rWidth=mpolygon == selectedGeometry ? this.renderWidth * 2 : this.renderWidth;
	                            GL2ShapesRender.drawPoly(context,mpolygon.getCoordinates(),width,height,x,y,rWidth,color);

	                    	}
	                    }
	                } else if (getType().equalsIgnoreCase(GeometricLayer.LINESTRING)) {
	                    for (Geometry temp : geomList) {
	                        if (temp.getCoordinates().length < 1) {
	                            continue;
	                        }
                            float size=(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
                            GL2ShapesRender.renderPolygon(context, width, height, temp.getCoordinates(), size, color);
	                    }
	                } else if (getType().equalsIgnoreCase(GeometricLayer.MIXED)) {
	                    for (Geometry temp : geomList) {
	                        if (temp.getCoordinates().length < 1) {
	                            continue;
	                        }
	                        if (temp instanceof LineString||temp instanceof Polygon) {
	                            float size=(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
	                            GL2ShapesRender.renderPolygon(context, width, height, temp.getCoordinates(), size, color);
	                        } else if (temp instanceof Point) {
	                            float size=(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
	                            Coordinate point = temp.getCoordinate();
	                            GL2ShapesRender.renderPoint(context, width, height, point, size, color);
	                        }else if (temp instanceof MultiPoint) {
	                            float size=(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
	                            GL2ShapesRender.renderPolygon(context, width, height, temp.getCoordinates(), size, color);
	                        }
	                    }
	                }
	            } else {
	                if (getType().equalsIgnoreCase(GeometricLayer.POINT)) {
	                	List<Geometry> toVisualize=new ArrayList<>();
                    	for (Geometry temp : geomList) {
                            if (((Double) glayer.getAttributes(temp).get(VDSSchema.SIGNIFICANCE)) > currentThresh) {
                            	toVisualize.add(temp);
                            }
                        }
	                    switch (this.displaysymbol) {
	                        case point: {
	                        	GL2ShapesRender.renderPolygons(context, width, height, toVisualize, this.renderWidth, color);
	                        	if (selectedGeometry != null) {
	                            	GL2ShapesRender.renderPoint(context, width, height, selectedGeometry.getCoordinate(), this.renderWidth*2, color);
	                            }
	                        }
	                        break;
	                        case circle: {
	                        	GL2ShapesRender.renderCircle(context, width, height, toVisualize, this.renderWidth, color);
	                        }
	                        break;
	                        case square: {
	                        	GL2ShapesRender.renderSquare(context,width,height,toVisualize,selectedGeometry,renderWidth,color);
	                        }
	                        break;
	                        case cross: {
	                        	GL2ShapesRender.renderCross(context,width,height,toVisualize,selectedGeometry,renderWidth,color);
	                        }
	                        break;
	                        case triangle: {
	                        	GL2ShapesRender.renderTriangle(context,width,height,toVisualize,selectedGeometry,renderWidth,color);
	                        }
	                        break;
	                        default: {
	                        }
	                    }
	                } else if (getType().equalsIgnoreCase(GeometricLayer.POLYGON)||getType().equalsIgnoreCase(GeometricLayer.LINESTRING)) {
	                    for (Geometry temp : geomList) {
	                        if (((Double) glayer.getAttributes(temp).get(VDSSchema.SIGNIFICANCE)) > currentThresh) {
	                            if (temp.getCoordinates().length < 1) {
	                                continue;
	                            }
	                            float size=(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
	                            GL2ShapesRender.renderPolygon(context, width, height, temp.getCoordinates(), size, color);
	                        }
	                    }
	                } else if (getType().equalsIgnoreCase(GeometricLayer.MIXED)) {
	                    for (Geometry temp : geomList) {
	                        if (((Double) glayer.getAttributes(temp).get(VDSSchema.SIGNIFICANCE)) > currentThresh) {
	                            if (temp.getCoordinates().length < 1) {
	                                continue;
	                            }
	                            if (temp instanceof Polygon||temp instanceof LineString) {
	                                float size=(temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth);
	                                GL2ShapesRender.renderPolygon(context, width, height, temp.getCoordinates(), size, color);
	                            } else if (temp instanceof Point) {
	                                float size=temp == selectedGeometry ? this.renderWidth * 2 : this.renderWidth;
	                                Coordinate point = temp.getCoordinate();
	                                GL2ShapesRender.renderPoint(context, width, height, point, size, color);
	                            }
	                        }
	                    }
	                }
	            }
	        }
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
		return name;
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