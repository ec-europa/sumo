/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.wwj;

import gov.nasa.worldwind.Locatable;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.Renderable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.impl.SarImageReader;

/**
 *
 * @author thoorfr
 */
public class WWGeoImage implements Renderable, Locatable{
    private GlobeAnnotation annotation;
    private Polyline bounds;
    private boolean annotationVisible=false;
    private Color color;
    private Object owner;


    public static WWGeoImage create(GeoImageReader gir){
        List<double[]> imageframe = gir.getFrameLatLon(((SarImageReader)gir).getWidth(),((SarImageReader)gir).getHeight());
        if (imageframe != null) {
            List<LatLon> ll = new ArrayList<LatLon>();
            for (double[] c : imageframe) {
                ll.add(new LatLon(Angle.fromDegreesLatitude(c[1]), Angle.fromDegreesLongitude(c[0])));
            }
            WWGeoImage gi = new WWGeoImage(new Polyline(ll, 1000), new GlobeAnnotation(gir.getFilesList()[0], new Position(new LatLon(Angle.fromDegreesLatitude(imageframe.get(0)[1]), Angle.fromDegreesLongitude(imageframe.get(0)[0])), 2000)),  Color.CYAN, Color.CYAN);
            //gi.setAnnotationVisible(true);
            return gi;
        }
        return null;
    }

    public WWGeoImage(Polyline bounds, GlobeAnnotation annotation, Color color, Color highlightedcolor) {
        this.bounds=bounds;
        this.bounds.setClosed(true);
        bounds.setHighlightColor(highlightedcolor);
        this.annotation=annotation;
        this.annotation.setDelegateOwner(this);
        this.color = color;
    }

    public boolean getAnnotationVisible() {
        return annotationVisible;
    }
    
    public void setAnnotationVisible(boolean value){
        this.annotationVisible=value;
    }
    
    public void render(DrawContext dc) {
        if(owner==null){
            bounds.setHighlighted(true);
            if(annotationVisible) annotation.render(dc);
        }
        else{
            bounds.setHighlighted(false);
            //bounds.setColor(color);
        }
        bounds.render(dc);
    }
    
    

    public Position getPosition() {
        return annotation.getReferencePosition();
    }

    public GlobeAnnotation getAnnotation() {
        return annotation;
    }

    public void setDelegateOwner(Object owner) {
        this.owner=owner;
    }

    public Object getDelegateOwner(){
        return this.owner;
    }

}
