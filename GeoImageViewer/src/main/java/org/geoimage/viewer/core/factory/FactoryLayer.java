package org.geoimage.viewer.core.factory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.GeoImageReader;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.TimeComponent;
import org.geoimage.viewer.core.api.IVectorLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.vectors.ComplexEditVDSVectorLayer;
import org.geoimage.viewer.core.layers.vectors.MaskVectorLayer;
import org.geoimage.viewer.core.layers.vectors.SimpleEditVectorLayer;
import org.geoimage.viewer.core.layers.vectors.TimeVectorLayer;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class FactoryLayer {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(FactoryLayer.class);

	
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
	public static IVectorLayer createVectorLayer(String type, GeometricLayer layer, GeoImageReader reader,String landMask) {
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
                ComplexEditVDSVectorLayer clayer = new ComplexEditVDSVectorLayer(Platform.getCurrentImageLayer(),layer.getName(),  layer.getGeometryType(), layer,landMask);
                clayer.addGeometries("image frame", Color.BLUE, 1, MaskVectorLayer.LINESTRING, frames, false);
                return clayer;
            } else {
                ComplexEditVDSVectorLayer clayer = new ComplexEditVDSVectorLayer(Platform.getCurrentImageLayer(),layer.getName(), layer.getGeometryType(), layer,landMask);
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
                return new SimpleEditVectorLayer(Platform.getCurrentImageLayer(),layer.getName(), layer.getGeometryType(), layer);
            } else {
                TimeComponent.setDirty(true);
                return new TimeVectorLayer(Platform.getCurrentImageLayer(),layer.getName(), layer.getGeometryType(), layer, timecolumnname);
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
	 public static IMask createMaskLayer(String name,String type,double bufferingDistance,GeoImageReader reader,GeometricLayer layer) {
		 MaskVectorLayer mask = null;
        try {
            mask = (new MaskVectorLayer(Platform.getCurrentImageLayer(),name, type, layer.clone()));
           	mask.buffer(bufferingDistance);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return mask;
    }
	/**
	 * 
	 * @param layer
	 * @return
	 */
	  public static  GeometricLayer createThresholdedLayer(GeometricLayer layer,double currentThresh,boolean threshable) {
	        GeometricLayer out = layer.clone();
	        if (!threshable) {
	            return out;
	        }
	        List<Geometry> remove = new ArrayList<Geometry>();
	        for (Geometry geom : Collections.unmodifiableList(out.getGeometries())) {
	            if (new Double("" + out.getAttributes(geom).get(VDSSchema.SIGNIFICANCE)) < currentThresh) {
	                remove.add(geom);
	            }
	        }
	        for (Geometry geom : remove) {
	            out.remove(geom);
	        }
	        return out;

	    }

}
