/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.visualization.vectors;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.Vector;

import org.geoimage.opengl.GL2ShapesRender;
import org.geoimage.opengl.OpenGLContext;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.IClickable;
import org.geoimage.viewer.core.api.IEditable;
import org.geoimage.viewer.core.api.IKeyPressed;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.IMouseDrag;
import org.geoimage.viewer.core.api.IMouseMove;
import org.geoimage.viewer.core.layers.AttributesGeometry;
import org.geoimage.viewer.core.layers.GenericLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.visualization.LayerPickedData;
import org.geoimage.viewer.widget.AttributesEditor;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author Pietro Argentieri
 */
public class EditGeometryVectorLayer extends GenericLayer implements IClickable,IMouseMove, IEditable, IMouseDrag, IKeyPressed {

    protected Coordinate editedPoint = null;
    protected boolean edit = false;
    protected boolean changeAttributes = false;
    protected boolean add = false;
    protected boolean delete = false;
    protected boolean move = false;
    protected Vector<Geometry> toBeRemoved = new Vector<Geometry>();
    protected Vector<Geometry> removed = new Vector<Geometry>();
    protected Vector<AttributesGeometry> attrRemoved = new Vector<AttributesGeometry>();
    protected GeometryFactory gf;
    protected boolean undo=false;
    
    
    public EditGeometryVectorLayer(ILayer parent,String layername, String type, GeometricLayer layer) {
        super(parent,layername, type, layer);
        gf = new GeometryFactory();
    }

    @Override
    public void mouseClicked(Point imagePosition, int button, OpenGLContext context) {
        if (!this.edit || button != IClickable.BUTTON1) {
            mouseClicked(imagePosition, context);
            return;
        }
        if (this.move) {
            performMove(imagePosition, context);
        } else if (this.add) {
            performAdd(imagePosition, context);
        } else if (this.delete) {
            performDelete(imagePosition, context);
        } else if (this.changeAttributes) {
            performChangeAttributes(imagePosition, context);
        }
        mouseClicked(imagePosition, context);

    }

    /**
     * 
     * @param imagePosition
     * @param context
     */
    public void mouseClicked(java.awt.Point imagePosition, OpenGLContext context) {
        this.selectedGeometry = null;
        GeometryFactory gf = new GeometryFactory();
        com.vividsolutions.jts.geom.Point p = gf.createPoint(new Coordinate(imagePosition.x, imagePosition.y));
        for (Geometry temp : glayer.getGeometries()) {
            //if (p.equalsExact(temp, 5 * context.getZoom())) {
            if (p.equalsExact(temp, 5 * context.getZoom())) {	
                this.selectedGeometry = temp;
                //System.out.println(""+temp.getCoordinate().x+","+temp.getCoordinate().y);
                LayerPickedData.put(temp, glayer.getAttributes(temp));
            }
        }
    }
    
    /**
     * 
     */
    private void checkRemoved(){
    	if (toBeRemoved.size() > 0) {
            for (Geometry remove : toBeRemoved) {
            	//save in this list for the undo operation
            	removed.add(0,remove);
            	attrRemoved.add(0,glayer.getAttributes(remove));
                glayer.remove(remove);
                if(glayer.getGeometries().size()>0){
                	selectedGeometry = glayer.getGeometries().get(0);
                	//move the visualization on the new selected target
                	Platform.getGeoContext().setX((int) (selectedGeometry.getCoordinate().x / 2));
                    Platform.getGeoContext().setY((int) (selectedGeometry.getCoordinate().y  / 2));
                    Platform.getGeoContext().setDirty(true);
                }	
            }

            toBeRemoved.clear();
        }
        if (undo) {
            if(removed.size()>0){
            	Geometry undo=removed.remove(0);
            	AttributesGeometry add=attrRemoved.remove(0);
            	glayer.put(undo,add);
            }
            undo=false;
        }
    }
    
    @Override
    public void render(OpenGLContext context) {
    	checkRemoved();
        super.render(context);
        
        if (!context.isDirty() || glayer == null || !this.edit) {
            return;
        }

        float zoom = context.getZoom(), width = context.getWidth() * zoom, height = context.getHeight() * zoom;

        if (type.equalsIgnoreCase(GeometricLayer.POLYGON) || type.equalsIgnoreCase(GeometricLayer.LINESTRING)) {
            GL2ShapesRender.renderPolygons(context, width, height,glayer.getGeometries(),width * 2,getColor());
        }

        if (editedPoint != null) {
        	GL2ShapesRender.renderPoint(context,width,height,editedPoint,width * 3,getColor());
        }    
    }

    public void mouseMoved(Point imagePosition, OpenGLContext context) {
        if (this.selectedGeometry == null || this.editedPoint == null) {
            return;
        } else {
            editedPoint.x = imagePosition.x;
            editedPoint.y = imagePosition.y;
        }

    }

    public void setEditable(boolean editable) {
        this.edit = editable;
        if (!this.edit) {
            editedPoint = null;
            updateActions();
        }

    }

    public boolean isEditable() {
        return this.edit;
    }

    public void setAddAction(boolean add) {
        if (add) {
            updateActions();
        }

        this.add = add;
    }

    public void setDeleteAction(boolean delete) {
        if (delete) {
            updateActions();
        }

        this.delete = delete;

    }

    public boolean isAddAction() {
        return this.add;
    }

    public boolean isDeleteAction() {
        return this.delete;
    }

    public void setMoveAction(boolean move) {
        if (move) {
            updateActions();
        }

        this.move = move;
    }

    public boolean isMoveAction() {
        return this.move;
    }

    private double computeDistance(Coordinate point1, Coordinate point2, Coordinate point3, Coordinate point4) {
        return point1.distance(point2)
                + point2.distance(point3)
                + point3.distance(point4);
    }

    protected void performAdd(Point imagePosition, OpenGLContext context) {
        if (type.equals(GeometricLayer.POINT)) {
            selectedGeometry = gf.createPoint(new Coordinate(imagePosition.x, imagePosition.y));
            glayer.put(selectedGeometry);
        } else if (type.equals(GeometricLayer.POLYGON) || type.equals(GeometricLayer.LINESTRING)) {
            if (selectedGeometry == null && (glayer.getGeometries().size() != 0)) {
                mouseClicked(imagePosition, BUTTON1, context);
                return;
            }
            if (glayer.getGeometries().size() == 0 | selectedGeometry == null) {
                try {
                    double step = 5 * context.getZoom();
                    Coordinate initend = new Coordinate(imagePosition.x - step, imagePosition.y - step);
                    if (type.equals(GeometricLayer.POLYGON)) {
                        selectedGeometry = gf.createPolygon(gf.createLinearRing(new Coordinate[]{initend, new Coordinate(imagePosition.x - step, imagePosition.y + step), new Coordinate(imagePosition.x + step, imagePosition.y + step), initend}), null);
                    } else {
                        selectedGeometry = gf.createLineString(new Coordinate[]{initend, new Coordinate(imagePosition.x - step, imagePosition.y + step)});
                    }
                    glayer.put(selectedGeometry);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            Coordinate closestPoint = null;
            Geometry currGeom = selectedGeometry;
            if (currGeom == null) {
                return;
            }
            int index = -1;
            double dist = Double.MAX_VALUE;
            if (type.equals(GeometricLayer.POLYGON)) {
                Coordinate[] polygon = ((Polygon) currGeom).getExteriorRing().getCoordinates();
                for (int i = 0; i < polygon.length - 1; i++) {
                    Coordinate point = polygon[i];
                    if ((imagePosition.x - point.x) * (imagePosition.x - point.x) + (imagePosition.y - point.y) * (imagePosition.y - point.y) < dist) {
                        closestPoint = point;
                        index = i;
                        dist = (imagePosition.x - point.x) * (imagePosition.x - point.x) + (imagePosition.y - point.y) * (imagePosition.y - point.y);
                    }

                }
                editedPoint = new Coordinate(imagePosition.x, imagePosition.y);
                int indexAfter = index + 1;
                if (indexAfter == polygon.length) {
                    indexAfter = 0;
                }
                int indexBefore = index - 1;
                if (indexBefore < 0) {
                    indexBefore = polygon.length - 1;
                }
                Coordinate after = polygon[indexAfter];
                Coordinate before = polygon[indexBefore];
                double distance1 = computeDistance(before, editedPoint, closestPoint, after);
                double distance2 = computeDistance(before, closestPoint, editedPoint, after);
                if (distance1 < distance2) {
                    Coordinate[] newC = new Coordinate[polygon.length + 1];
                    for (int i = 0; i < index; i++) {
                        newC[i] = polygon[i];
                    }
                    newC[index] = editedPoint;
                    for (int i = index + 1; i < newC.length; i++) {
                        newC[i] = polygon[i - 1];
                    }
                    //closing the ring
                    newC[newC.length - 1] = newC[0];
                    Geometry newGeom = gf.createPolygon(gf.createLinearRing(newC), null);
                    glayer.replace(currGeom, newGeom);
                    selectedGeometry = newGeom;
                } else {
                    Coordinate[] newC = new Coordinate[polygon.length + 1];
                    for (int i = 0; i < index; i++) {
                        newC[i] = polygon[i];
                    }
                    newC[index] = editedPoint;
                    for (int i = index + 1; i < newC.length; i++) {
                        newC[i] = polygon[i - 1];
                    }
                    //closing the ring
                    newC[newC.length - 1] = newC[0];
                    Geometry newGeom = gf.createPolygon(gf.createLinearRing(newC), null);
                    glayer.replace(currGeom, newGeom);
                    selectedGeometry = newGeom;
                }
            }

            if (type.equals(GeometricLayer.POINT)) {
                glayer.put(gf.createPoint(editedPoint));
                editedPoint = null;
                return;
            }


            editedPoint = null;
        }
    }

    protected void performDelete(Point imagePosition, OpenGLContext context) {
        if (selectedGeometry == null) {
            //super.mouseClicked(imagePosition, BUTTON1, context);
            return;
        }
        if (this.editedPoint == null) {
            com.vividsolutions.jts.geom.Point p = gf.createPoint(new Coordinate(imagePosition.x, imagePosition.y));
            if (type.equals(GeometricLayer.POINT)) {
                if (p.isWithinDistance(selectedGeometry, 5 * context.getZoom())) {
                    toBeRemoved.add(selectedGeometry);
                }
                selectedGeometry = null;
            } else if (type.equals(GeometricLayer.POLYGON)) {
                Coordinate[] polygon = ((Polygon) selectedGeometry).getExteriorRing().getCoordinates();
                for (Coordinate point : polygon) {
                    if (p.isWithinDistance(gf.createPoint(point), 5 * context.getZoom())) {
                        if (polygon.length == 4) {
                            toBeRemoved.add(selectedGeometry);
                        } else {
                            Coordinate[] newC = new Coordinate[polygon.length - 1];
                            int jump = 0;
                            for (int i = 0; i < polygon.length; i++) {
                                if (polygon[i] == point) {
                                    jump = 1;
                                } else {
                                    newC[i - jump] = polygon[i];
                                }
                            }
                            //closing the ring
                            newC[newC.length - 1] = newC[0];
                            Geometry newGeom = gf.createPolygon(gf.createLinearRing(newC), null);
                            glayer.replace(selectedGeometry, newGeom);
                            selectedGeometry = newGeom;
                        }
                        break;
                    }
                }
            }
        }
    }

    protected void performMove(Point imagePosition, OpenGLContext context) {
        if (selectedGeometry == null) {
            //super.mouseClicked(imagePosition, IClickable.BUTTON1, context);

            if (this.editedPoint == null && selectedGeometry != null) {
                if (type.equals(GeometricLayer.POINT)) {
                    this.editedPoint = selectedGeometry.getCoordinate();
                } else if (type.equals(GeometricLayer.POLYGON)) {
                    LineString ls = ((Polygon) selectedGeometry).getExteriorRing();
                    for (int i = 0; i < ls.getNumPoints(); i++) {
                        Coordinate point = ls.getCoordinateN(i);
                        if (Math.abs(imagePosition.x - point.x) < 5 * context.getZoom() && Math.abs(imagePosition.y - point.y) < 5 * context.getZoom()) {
                            this.editedPoint = point;
                            break;
                        }
                    }
                }
                if (this.editedPoint == null) {
                    selectedGeometry.geometryChanged();
                    selectedGeometry = null;
                }
            } else {
                if (selectedGeometry != null) {
                    selectedGeometry.geometryChanged();
                }
                this.editedPoint = null;
            }
        } else {
            selectedGeometry.geometryChanged();
            editedPoint = null;
            selectedGeometry = null;
        }
    }

    private void updateActions() {
        this.selectedGeometry = null;
        this.move = false;
        this.add = false;
        this.delete = false;
        this.changeAttributes = false;
        this.editedPoint = null;
    }

    public void mouseDragged(final Point initPosition, final Point imagePosition, int button, OpenGLContext context) {
        if (selectedGeometry == null | !this.edit | this.move | this.add | this.delete) {
            return;
        }
        if (!selectedGeometry.contains(gf.createPoint(new Coordinate(initPosition.x, initPosition.y)))) {
            selectedGeometry = null;
            return;
        }
        if (type.equals(GeometricLayer.POLYGON) || type.equals(GeometricLayer.LINESTRING)) {
            selectedGeometry.apply(new CoordinateSequenceFilter() {

                public void filter(CoordinateSequence seq, int i) {
                    if (i == seq.size() - 1) {
                        if (seq.getCoordinate(i) == seq.getCoordinate(0)) {
                            return;
                        }
                    }
                    seq.getCoordinate(i).x += imagePosition.x - initPosition.x;
                    seq.getCoordinate(i).y += imagePosition.y - initPosition.y;
                }

                public boolean isDone() {
                    return false;
                }

                public boolean isGeometryChanged() {
                    return true;
                }
            });
        }

    }

    public void keyPressed(KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            if (selectedGeometry != null) {
                toBeRemoved.add(selectedGeometry);
                selectedGeometry = null;
            }
        }else if (evt.isControlDown()&&evt.getKeyCode()==90){ //90=Z  
            undo=true;
        }
    }

    public void setChangeAttributesAction(boolean change) {
        this.changeAttributes = change;
    }

    public boolean isChangeAttributesAction() {
        return changeAttributes;
    }

    private void performChangeAttributes(Point imagePosition, OpenGLContext context) {
        if (selectedGeometry == null) {
            //mouseClicked(imagePosition, BUTTON1, context);
        }
        if (selectedGeometry != null) {
            AttributesEditor ae = new AttributesEditor(new java.awt.Frame(), true);
            ae.setAttributes(this.glayer.getAttributes(selectedGeometry));
            ae.setVisible(true);
        }

    }
}
