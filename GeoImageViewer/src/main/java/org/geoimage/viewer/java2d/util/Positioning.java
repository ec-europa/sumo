/*
 * 
 */
package org.geoimage.viewer.java2d.util;

import java.awt.Point;

import org.geoimage.def.GeoTransform;
import org.geoimage.exception.GeoTransformException;
import org.geotools.referencing.GeodeticCalculator;

public class Positioning {

		public static GeodeticCalculator computeDistance(GeoTransform gt,Point initPosition,Point endPosition,Point imagePosition) throws GeoTransformException {
	        double[] init = gt.getGeoFromPixel(initPosition.x, initPosition.y);
	        double[] end = null;
	        GeodeticCalculator gc = new GeodeticCalculator();
	        //Point2D initPoint = new Point2D.Double(init[1], init[0]);
	        gc.setStartingGeographicPoint(init[0], init[1]);
	        //Point2D endPoint = null;
	        if (endPosition != null) {
	            end = gt.getGeoFromPixel(endPosition.x, endPosition.y);
	            //endPoint = new Point2D.Double(end[1], end[0]);
	            gc.setDestinationGeographicPoint(end[0], end[1]);
	        } else {
	            end = gt.getGeoFromPixel(imagePosition.x, imagePosition.y);
	            //endPoint = new Point2D.Double(end[1], end[0]);
	            gc.setDestinationGeographicPoint(end[0], end[1]);
	        }
	        return gc;
	    }
}
