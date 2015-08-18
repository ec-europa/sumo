/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.layers.visualization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.geoimage.viewer.core.layers.AttributesLayer;
import org.geoimage.viewer.core.wwj.VectorObject;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A class that deals with the Vector objects that have been clicked on.
 * @author thoorfr
 */
public class LayerPickedData {
    
    static private Map<Geometry,AttributesLayer> data=new HashMap<Geometry, AttributesLayer>();
    private static VectorObject topOject;
    private static List<ChangeListener> listeners=new Vector<ChangeListener>();
    
    public static void put(Geometry geom, AttributesLayer att){
        if(data.isEmpty()){
            LayerPickedData.topOject=new VectorObject(geom, att);
        }
        data.put(geom, att);
        fire();
    }
    
    public static void clear(){
        data.clear();
        fire();
    }
    
    public static VectorObject getTopObject(){
        return topOject;
    }
    
    public static List<VectorObject> getListOfOjects(){
        Vector<VectorObject> out=new Vector<VectorObject>();
        for(Geometry geom: data.keySet()){
            out.add(new VectorObject(geom, data.get(geom)));
        }
        return out;
    }
    
    public static void addListener(ChangeListener l){
        listeners.add(l);
    }
    
    public static void removeListener(ChangeListener l){
        listeners.remove(l);
    }
    
    public static void clearListeners(){
        listeners.clear();
    }
    
    private static void fire(){
        for(ChangeListener l:listeners){
            l.stateChanged(new ChangeEvent(data));
        }
    }

}
