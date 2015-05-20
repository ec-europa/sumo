/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.utils;

import java.util.ArrayList;
import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.def.GeoTransform;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Usefull tools to extract geometry of the image boundaries
 * @author thoorfr
 */
public class GeometryExtractor {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(GeometryExtractor.class);

    public static Geometry getFrame(GeoImageReader gir){
        try {
            GeoTransform gt = gir.getGeoTransform();
            double[] x0;
            double[] x1;
            double[] x2;
            double[] x3;
            x0 = gt.getGeoFromPixel(-50, -50);
            x2 = gt.getGeoFromPixel(50 + gir.getWidth(), 50 + gir.getHeight());
            x3 = gt.getGeoFromPixel(50 + gir.getWidth(), -50);
            x1 = gt.getGeoFromPixel(-50, 50 + gir.getHeight());
            return new WKTReader().read("POLYGON((" + x0[0] + " " + x0[1] + "," + x1[0] + " " + x1[1] + "," + x2[0] + " " + x2[1] + "," + x3[0] + " " + x3[1] + "," + x0[0] + " " + x0[1] + "" + "))");
        } catch (ParseException ex) {
        	logger.error(ex.getMessage(),ex);
        }
        return null;
    }
    
    // return a geometry of grid of Tiles
    public static List<Geometry> getTiles(int width,int height ,int tileSize) {
        
        int horTiles = width / tileSize;
        int verTiles = height / tileSize;
        
        List<Geometry> tiles = new ArrayList<Geometry>(horTiles*verTiles*8);
        
        int[] sizeTile = new int[2];
        // the real size of tiles
        sizeTile[0] = width / horTiles;
        sizeTile[1] = height / verTiles;
        GeometryFactory geomFactory = new GeometryFactory();
        Coordinate[] coo=null;
        for (int j = 0; j < verTiles; j++) {
        	coo=new Coordinate[2];
        	coo[0]=new Coordinate(0, j * sizeTile[1]);
        	coo[1]=new Coordinate((double)width, (double)j * sizeTile[1]);
            tiles.add(geomFactory.createLineString(coo));
        }
        for (int i = 0; i < horTiles; i++) {
        	coo=new Coordinate[2];
        	coo[0]=new Coordinate(i * sizeTile[0], 0);
        	coo[1]=new Coordinate((double)i * sizeTile[0], (double)height);
            tiles.add(geomFactory.createLineString(coo));
        }
        return tiles;

    }
}
