package org.geoimage.viewer.core.factory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geoimage.analysis.VDSSchema;
import org.geoimage.def.GeoImageReader;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.TimeComponent;
import org.geoimage.viewer.core.api.ilayer.IMask;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.GenericLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditVDSVectorLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.TimeVectorLayer;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class FactoryLayer {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(FactoryLayer.class);

	/*
	 * public final static String TYPE_COMPLEX="complexvds"; public final static
	 * String TYPE_NON_COMPLEX="noncomplexlayer"; //TODO utilizzare queste
	 * costanti : in questo momento viene utilizzata solo TYPE_COMPLEX public
	 * final static String TYPE_SIMPLE="simple"; public final static String
	 * TYPE_TIMESTAMP="timestamp"; public final static String TYPE_DATE="Date";
	 */

	/**
	 *
	 * @param type
	 * @param layer
	 * @param parent
	 * @return //TODO: implement another way to understand the type of the layer
	 *         public static GenericLayer createGenericLayerOLD(String type,
	 *         GeometricLayer layer, GeoImageReader reader,String landMask) {
	 *
	 *         boolean timestamplayer = false;
	 *
	 *         if (type.equals(TYPE_COMPLEX)) { ComplexEditVDSVectorLayer
	 *         clayer=null; Geometry frame = layer.getGeometries().get(0); if
	 *         (!(frame instanceof Point)) { layer.remove(frame); List
	 *         <Geometry> frames = new ArrayList<Geometry>(); frames.add(frame);
	 *
	 *         clayer = new
	 *         ComplexEditVDSVectorLayer(LayerManager.getIstanceManager().
	 *         getCurrentImageLayer(), layer.getName(), layer.getGeometryType(),
	 *         layer,landMask);
	 *
	 *         clayer.addGeometries("image frame", Color.BLUE, 1,
	 *         GeometricLayer.LINESTRING, frames, false);
	 *
	 *         } else { clayer = new
	 *         ComplexEditVDSVectorLayer(LayerManager.getIstanceManager().
	 *         getCurrentImageLayer(), layer.getName(), layer.getGeometryType(),
	 *         layer,landMask);
	 *
	 *         } return clayer;
	 *
	 *         } else { String timecolumnname = ""; // TODO: check if this part
	 *         of the code is necessary /* for (int i = 0; i < types.length;
	 *         i++) { String t = types[i]; // TODO: check if this type of layer
	 *         is used!!! if (t.equals(TYPE_DATE) || t.equals(TYPE_TIMESTAMP)) {
	 *         timestamplayer = true; timecolumnname = schema[i]; break; } }* if
	 *         (!timestamplayer) { //TODO: implement another way to understand
	 *         the type of the layer return new
	 *         MaskVectorLayer(LayerManager.getIstanceManager().
	 *         getCurrentImageLayer(), layer.getName(),
	 *         layer.getGeometryType(),MaskVectorLayer.COASTLINE_MASK,layer); }
	 *         else { TimeComponent.setDirty(true); return new
	 *         TimeVectorLayer(LayerManager.getIstanceManager().
	 *         getCurrentImageLayer(),layer.getName(), layer.getGeometryType(),
	 *         layer, timecolumnname); } } }
	 */

	/**
	 *
	 * @param type
	 * @param layer
	 * @param parent
	 * @return
	 */
	public static GenericLayer createComplexLayer(GeometricLayer layer) {

		ComplexEditVDSVectorLayer clayer = null;
		Geometry frame = layer.getGeometries().get(0);
		if (!(frame instanceof Point)) {
			layer.remove(frame);
			List<Geometry> frames = new ArrayList<Geometry>();
			frames.add(frame);

			clayer = new ComplexEditVDSVectorLayer(LayerManager.getIstanceManager().getCurrentImageLayer(),
					layer.getName(), layer.getGeometryType(), layer);

			clayer.addGeometries("image frame", Color.BLUE, 1, GeometricLayer.LINESTRING, frames, false);

		} else {
			clayer = new ComplexEditVDSVectorLayer(LayerManager.getIstanceManager().getCurrentImageLayer(),
					layer.getName(), layer.getGeometryType(), layer);

		}
		return clayer;
	}

	/**
	 *
	 * @param type
	 * @param layer
	 * @param parent
	 * @return
	 */
	public static GenericLayer createMaskLayer(GeometricLayer layer,int maskType) {

		boolean timestamplayer = false;
		String timecolumnname = "";
		// TODO: check if this part of the code is necessary
		/*
		 * for (int i = 0; i < types.length; i++) { String t = types[i]; //
		 * TODO: check if this type of layer is used!!! if (t.equals(TYPE_DATE)
		 * || t.equals(TYPE_TIMESTAMP)) { timestamplayer = true; timecolumnname
		 * = schema[i]; break; } }
		 */
		if (!timestamplayer) {
			// TODO: implement another way to understand the type of the layer
			return new MaskVectorLayer(LayerManager.getIstanceManager().getCurrentImageLayer(), layer.getName(),
					layer.getGeometryType(),maskType , layer);
		} else {
			TimeComponent.setDirty(true);
			return new TimeVectorLayer(LayerManager.getIstanceManager().getCurrentImageLayer(), layer.getName(),
					layer.getGeometryType(), layer, timecolumnname);
		}
	}

	/**
	 *
	 * @param name
	 * @param geomType
	 * @param maskType
	 *            : Type of mask: Coastline, Ice or Windfarm. See
	 *            MaskVectorLayer constant
	 * @param bufferingDistance
	 * @return
	 */
	public static IMask createMaskLayer(String name, String geomType, double bufferingDistance,	GeometricLayer layer, int maskType) {
		MaskVectorLayer mask = null;
		try {
			mask = (new MaskVectorLayer(LayerManager.getIstanceManager().getCurrentImageLayer(), name, geomType,maskType, layer.clone()));
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
	public static GeometricLayer createThresholdedLayer(GeometricLayer layer, double currentThresh,
			boolean threshable) {
		GeometricLayer out = layer.clone();
		if (threshable) {
			List<Geometry> remove = new ArrayList<Geometry>();
			for (Geometry geom : Collections.unmodifiableList(out.getGeometries())) {
				if (new Double("" + out.getAttributes(geom).get(VDSSchema.SIGNIFICANCE)) < currentThresh) {
					remove.add(geom);
				}
			}
			for (Geometry geom : remove) {
				out.remove(geom);
			}
		}
		return out;

	}

}
