package org.geoimage.viewer.core.layers;

import java.awt.Color;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoimage.def.GeoImageReader;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.core.TimeComponent;
import org.geoimage.viewer.core.api.GeometricLayer;
import org.geoimage.viewer.core.api.IVectorLayer;
import org.geoimage.viewer.core.layers.vectors.ComplexEditVDSVectorLayer;
import org.geoimage.viewer.core.layers.vectors.MaskVectorLayer;
import org.geoimage.viewer.core.layers.vectors.SimpleEditVectorLayer;
import org.geoimage.viewer.core.layers.vectors.TimeVectorLayer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class FactoryLayer {
	public final static String TYPE_COMPLEX="complexvds";
	
	//TODO utilizzare queste costanti : in questo momento viene utilizzata solo TYPE_COMPLEX
	public final static String TYPE_SIMPLE="simple";
	public final static String TYPE_TIMESTAMP="timestamp";
	
	
	/**
	 * 
	 * @param type
	 * @param layer
	 * @param parent
	 * @return
	 */
	public static IVectorLayer createVectorLayer(String type, GeometricLayer layer, GeoImageReader reader) {
        String[] schema = layer.getSchema();
        String[] types = layer.getSchemaTypes();
        boolean timestamplayer = false;
        String timecolumnname = "";
        if (type.equals(TYPE_COMPLEX)) {
            Geometry frame = layer.getGeometries().get(0);
            if (!(frame instanceof Point)) {
                layer.remove(frame);
                Vector<Geometry> frames = new Vector<Geometry>();
                frames.add(frame);
                ComplexEditVDSVectorLayer clayer = new ComplexEditVDSVectorLayer(layer.getName(), reader, layer.getGeometryType(), layer);
                clayer.addGeometries("image frame", Color.BLUE, 1, MaskVectorLayer.LINESTRING, frames, false);
                return clayer;
            } else {
                ComplexEditVDSVectorLayer clayer = new ComplexEditVDSVectorLayer(layer.getName(), reader, layer.getGeometryType(), layer);
                return clayer;
            }

        } else {
            for (int i = 0; i < types.length; i++) {
                String t = types[i];
                if (t.equals("Date") || t.equals("Timestamp")) {
                    timestamplayer = true;
                    timecolumnname = schema[i];
                    break;
                }
            }
            if (!timestamplayer) {
                return new SimpleEditVectorLayer(layer.getName(), reader, layer.getGeometryType(), layer);
            } else {
                TimeComponent.setDirty(true);
                return new TimeVectorLayer(layer.getName(), reader, layer.getGeometryType(), layer, timecolumnname);
            }
        }
    }
	
	/**
	 * 
	 * @param name
	 * @param type
	 * @param bufferingDistance
	 * @return
	 */
	 public static IMask createBufferedLayer(String name,String type,double bufferingDistance,GeoImageReader reader,GeometricLayer layer) {
		 MaskVectorLayer mask = null;
        try {
            mask = (new MaskVectorLayer(name, reader,type, layer.clone()));
            mask.buffer(bufferingDistance);
        } catch (Exception ex) {
            Logger.getLogger(MaskVectorLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mask;
    }
	
	

}
