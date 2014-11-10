/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.impl;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoimage.def.GeoTransform;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.operation.transform.WarpTransform2D;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;

/**
 *  Class that provides the GeoTransform based on a set of Ground Control Points.
 *  A polynomial transformation of degree 2 is computed that fits the best those points
 * @author thoorfr
 */
public class GcpsGeoTransform implements GeoTransform {

  //  private List<Gcp> gcps;
    private CoordinateReferenceSystem sourceCRS;
    private WarpTransform2D pix2geo;
    private WarpTransform2D geo2pix;
    private String defaultEpsgProjection;
    private MathTransform defaultMath;
    private CoordinateReferenceSystem defaultCrs;
    
    /**
     * Create your GeoTransform provided the initial list of gcps
     * @param gcps is the list of gcps of the image
     * @param epsgGeoProj is the String that codes the projection of your mapped gcps
     *  for instance "EPSG:4326". The list of possible values are the one from Geotools library
     */
    public GcpsGeoTransform(List<Gcp> gcps, String epsgGeoProj) {
    	this.defaultEpsgProjection=epsgGeoProj;
        try {
            sourceCRS = CRS.decode(epsgGeoProj);
            defaultCrs = CRS.decode(defaultEpsgProjection);
            defaultMath = CRS.findMathTransform(defaultCrs, sourceCRS);
        } catch (Exception ex) {
            Logger.getLogger(GcpsGeoTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
        Point2D[] src = new Point2D[gcps.size()];
        Point2D[] dst = new Point2D[gcps.size()];
        int i = 0;
        for (Gcp gcp : gcps) {
            src[i] = new Point2D.Double(gcp.getXpix(), gcp.getYpix());
            dst[i] = new Point2D.Double(gcp.getXgeo(), gcp.getYgeo());
            i++;            
        }
        if(gcps.size()<6){
            Point2D[] srctemp=src;
            Point2D[] dsttemp=dst;
            src=new Point2D.Double[src.length*2-1];
            dst=new Point2D.Double[dst.length*2-1];
            for(int j=0;j<srctemp.length;j++){
                src[j]=srctemp[j];
                dst[j]=dsttemp[j];
            }
            for(int j=0;j<srctemp.length-1;j++){
                src[srctemp.length+j]=new Point2D.Double((srctemp[j+1].getX()+srctemp[j].getX())/2,(srctemp[j+1].getY()+srctemp[j].getY())/2);
                dst[dsttemp.length+j]=new Point2D.Double((dsttemp[j+1].getX()+dsttemp[j].getX())/2,(dsttemp[j+1].getY()+dsttemp[j].getY())/2);
                
            }
        }
        //TODO try to change the polynomial dedree
        pix2geo = new WarpTransform2D(src, dst, i < 6 ? 1 : 2);
        try {
			geo2pix = (WarpTransform2D) pix2geo.inverse();
		} catch (NoninvertibleTransformException e) {
			geo2pix=new WarpTransform2D(dst, src, i < 6 ? 1 : 2);
		}
    }
    
    public double[] getPixelFromGeoWithDefaultEps(double xgeo, double ygeo,boolean changeProjection) {
        double[] out = new double[]{xgeo, ygeo};
        if(changeProjection){
		    try {
		        double[] temp = new double[]{xgeo, ygeo, 0};
		        defaultMath.transform(temp, 0, temp, 0, 1);
		        out[0] = temp[0];
		        out[1] = temp[1];
		    } catch (Exception ex) {
		        Logger.getLogger(GcpsGeoTransform.class.getName()).log(Level.SEVERE, null, ex);
		    }
        }    
        geo2pix.transform(out, 0, out, 0, 1);
        out[0] = out[0];
        out[1] = out[1];
        return out;
    }

    public double[] getGeoFromPixelWithDefaultEps(double xpix, double ypix,boolean changeProjection) {
        double[] out = new double[2];
		pix2geo.transform(new double[]{xpix , ypix }, 0, out, 0, 1);
		if(changeProjection){
	        try {
	            double[] temp = new double[3];
	            defaultMath.transform(new double[]{out[0], out[1], 0}, 0, temp, 0, 1);
	            out[0] = temp[0];
	            out[1] = temp[1];
	        } catch (Exception ex) {
	            Logger.getLogger(GcpsGeoTransform.class.getName()).log(Level.SEVERE, null, ex);
	        }
		}    
        return out;
    }

    /**
     * @param inputEpsgProjection if null use the original projection
     */
    @Override
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
                Logger.getLogger(GcpsGeoTransform.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        geo2pix.transform(out, 0, out, 0, 1);
        out[0] = out[0];
        out[1] = out[1];
        return out;
    }

    /**
     * @param outputEpsgProjection if null use the original projection
     */
    public double[] getGeoFromPixel(double xpix, double ypix, String outputEpsgProjection) {
        double[] out = new double[2];
		pix2geo.transform(new double[]{xpix , ypix }, 0, out, 0, 1);
        //pix2geo.transform(new double[]{xpix + m_translationX, ypix + m_translationY}, 0, out, 0, 1);
        if (outputEpsgProjection != null) {
            try {
                double[] temp = new double[3];
                CoordinateReferenceSystem crs = CRS.decode(outputEpsgProjection);
                MathTransform math = CRS.findMathTransform(sourceCRS, crs);
                math.transform(new double[]{out[0], out[1], 0}, 0, temp, 0, 1);
                out[0] = temp[0];
                out[1] = temp[1];
            } catch (Exception ex) {
                Logger.getLogger(GcpsGeoTransform.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return out;
    }

    public double[] getPixelFromGeo(double[] src, int srcOffset, double[] dest, int destOffset, int numPoints, String inputEpsgProjection) throws NoSuchAuthorityCodeException, FactoryException {
        if (dest == null) {
            dest = new double[src.length];
        }
        if (inputEpsgProjection != null) {
        	CoordinateReferenceSystem crs = CRS.decode(inputEpsgProjection);
            MathTransform math = CRS.findMathTransform(crs, sourceCRS);
            for (int i = 0; i < numPoints * 2;) {
                try {
                    double[] temp = new double[]{src[srcOffset+i], src[srcOffset+i+1], 0};
                    
                    math.transform(temp, 0, temp, 0, 1);
                    dest[destOffset+i++] = temp[0];
                    dest[destOffset+i++] = temp[1];
                } catch (Exception ex) {
                    Logger.getLogger(GcpsGeoTransform.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        geo2pix.transform(dest, destOffset, dest, destOffset, numPoints);
        for (int i = 0; i < numPoints * 2;) {
            dest[0] = dest[0];
            dest[1] = dest[1];
        }

        return dest;
    }

    public double[] getGeoFromPixel(double[] src, int srcOffset, double[] dest, int destOffset, int numPoints, String outputEpsgProjection) throws NoSuchAuthorityCodeException, FactoryException {
        double[] srctranslated = new double[src.length];
        for (int i = 0; i < numPoints * 2;) {
            srctranslated[0] = src[0];
            srctranslated[1] = src[1];
        }
        if (dest == null) {
            dest = new double[src.length];
        }
        pix2geo.transform(src, srcOffset, dest, destOffset, numPoints);
        if (outputEpsgProjection != null) {
        	CoordinateReferenceSystem crs = CRS.decode(outputEpsgProjection);
            MathTransform math = CRS.findMathTransform(sourceCRS, crs);
            for (int i = 0; i < numPoints * 2;) {
                try {
                    double[] temp = new double[3];
                    math.transform(new double[]{dest[srcOffset + i], dest[srcOffset + i + 1], 0}, 0, temp, 0, 1);
                    dest[destOffset + i++] = temp[0];
                    dest[destOffset + i++] = temp[1];
                } catch (Exception ex) {
                    Logger.getLogger(AffineGeoTransform.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return dest;
    }

    public void setTransformTranslation(int x, int y) {
    }

    public int[] getTransformTranslation() {
        return new int[]{};
    }

    // get pixel size, returns the pixel size in both directions
    public double[] getPixelSize()
    {
        double[] pixelsize = {0.0, 0.0};
        // should be in the image reader class
        // get pixel size
        double[] latlonorigin = getGeoFromPixelWithDefaultEps(0, 0,false);
        double[] latlon = getGeoFromPixelWithDefaultEps(100, 0,false);
        // use the geodetic calculator class to calculate distances in meters
        GeodeticCalculator gc = new GeodeticCalculator();
        gc.setStartingGeographicPoint(latlonorigin[0], latlonorigin[1]);
        gc.setDestinationGeographicPoint(latlon[0], latlon[1]);
        pixelsize[0] = gc.getOrthodromicDistance() / 100;
        latlon = getGeoFromPixelWithDefaultEps(0, 100,false);
        gc.setDestinationGeographicPoint(latlon[0], latlon[1]);
        pixelsize[1] = gc.getOrthodromicDistance() / 100;
        
        return pixelsize;
    }
}
