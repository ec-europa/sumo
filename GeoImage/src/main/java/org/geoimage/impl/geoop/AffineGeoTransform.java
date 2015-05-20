/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.impl.geoop;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import org.geoimage.def.GeoTransform;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thoorfr
 */
public class AffineGeoTransform implements GeoTransform {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(AffineGeoTransform.class);

	
	
    CoordinateReferenceSystem sourceCRS;
    AffineTransform pix2geo;
    AffineTransform geo2pix;
    private int m_translationX;
    private int m_translationY;

    public AffineGeoTransform(AffineTransform atpix2geo, String epsgGeoProj) {
        pix2geo = atpix2geo;
        m_translationX = 0;
        m_translationY = 0;
        try {
            geo2pix = pix2geo.createInverse();
            sourceCRS = CRS.decode(epsgGeoProj);
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }
    
    /**
     * 
     */
    public double[] getPixelFromGeo(double xgeo, double ygeo, String inputEpsgProjection) {
        double[] out = new double[]{xgeo, ygeo};
        if (inputEpsgProjection != null) {
            try {
                double[] temp = new double[]{xgeo, ygeo, 0};
                CoordinateReferenceSystem crs = CRS.decode(inputEpsgProjection);
                MathTransform math = CRS.findMathTransform(crs, sourceCRS);
                math.transform(temp, 0, temp, 0, 1);
                out[0] = temp[0];
                out[1] = temp[1];
            } catch (Exception ex) {
            	logger.error(ex.getMessage(),ex);
            }
        }
        geo2pix.transform(out, 0, out, 0, 1);
        return out;
    }

    public double[] getGeoFromPixel(double xpix, double ypix, String outputEpsgProjection) {
        double[] out = new double[2];
        pix2geo.transform(new double[]{xpix, ypix}, 0, out, 0, 1);
        if (outputEpsgProjection != null) {
            try {
                double[] temp = new double[3];
                CoordinateReferenceSystem crs = CRS.decode(outputEpsgProjection);
                MathTransform math = CRS.findMathTransform(sourceCRS, crs);
                math.transform(new double[]{out[0], out[1], 0}, 0, temp, 0, 1);
                out[0] = temp[0];
                out[1] = temp[1];
            } catch (Exception ex) {
            	logger.error(ex.getMessage(),ex);
            }
        }
        return out;
    }

    public double[] getPixelFromGeo(double[] src, int srcOffset, double[] dest, int destOffset, int numPoints, String inputEpsgProjection) {
        if (dest == null) {
            dest = new double[src.length];
        }
        if (inputEpsgProjection != null) {
            for (int i = 0; i < numPoints * 2;) {
                try {
                    double[] temp = new double[]{src[srcOffset+i], src[srcOffset+i+1], 0};
                    CoordinateReferenceSystem crs = CRS.decode(inputEpsgProjection);
                    MathTransform math = CRS.findMathTransform(crs, sourceCRS);
                    math.transform(temp, 0, temp, 0, 1);
                    dest[destOffset+i++] = temp[0];
                    dest[destOffset+i++] = temp[1];
                } catch (Exception ex) {
                	logger.error(ex.getMessage(),ex);
                }
            }
        }
        geo2pix.transform(dest, destOffset, dest, destOffset, numPoints);
        return dest;
    }

    public double[] getGeoFromPixel(double[] src, int srcOffset, double[] out, int destOffset, int numPoints, String outputEpsgProjection) {
        if (out == null) {
            out = new double[src.length];
        }
        pix2geo.transform(src, srcOffset, out, destOffset, numPoints);
        if (outputEpsgProjection != null) {
            for (int i = 0; i < numPoints * 2;) {
                try {
                    double[] temp = new double[3];
                    CoordinateReferenceSystem crs = CRS.decode(outputEpsgProjection);
                    MathTransform math = CRS.findMathTransform(sourceCRS, crs);
                    math.transform(new double[]{out[srcOffset+i], out[srcOffset+i+1], 0}, 0, temp, 0, 1);
                    out[destOffset+i++] = temp[0];
                    out[destOffset+i++] = temp[1];
                } catch (Exception ex) {
                	logger.error(ex.getMessage(),ex);
                }
            }
        }
        return out;
    }

    public void setTransformTranslation(int x, int y) {
        try {
            // go back to initial values
            pix2geo.translate(-m_translationX, -m_translationY);
            // apply new translation to transform
            pix2geo.translate(-x, -y);
            // update reverse transform
            geo2pix = pix2geo.createInverse();
            // keep new tranform values
            m_translationX = -x;
            m_translationY = -y;
        } catch (NoninvertibleTransformException ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }

    public int[] getTransformTranslation() {
        return new int[]{m_translationX, m_translationY};
    }

    // get pixel size, returns the pixel size in both directions
    public double[] getPixelSize()
    {
        double[] pixelsize = {0.0, 0.0};
        // should be in the image reader class
        // get pixel size
        double[] latlonorigin = getGeoFromPixel(0, 0, "EPSG:4326");
        double[] latlon = getGeoFromPixel(100, 0, "EPSG:4326");
        // use the geodetic calculator class to calculate distances in meters
        GeodeticCalculator gc = new GeodeticCalculator();
        gc.setStartingGeographicPoint(latlonorigin[0], latlonorigin[1]);
        gc.setDestinationGeographicPoint(latlon[0], latlon[1]);
        pixelsize[0] = gc.getOrthodromicDistance() / 100;
        latlon = getGeoFromPixel(0, 100, "EPSG:4326");
        gc.setDestinationGeographicPoint(latlon[0], latlon[1]);
        pixelsize[1] = gc.getOrthodromicDistance() / 100;
        
        return pixelsize;
    }


	@Override
	public double[] getPixelFromGeo(double xgeo, double ygeo) {
		return getPixelFromGeo(xgeo, ygeo, "EPSG:4326");
	}

	@Override
	public double[] getGeoFromPixel(double xpix, double ypix) {
		return getGeoFromPixel(xpix, ypix, "EPSG:4326");
	}
}
