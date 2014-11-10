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
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 *  Class that provides the GeoTransform based on a set of Ground Control Points.
 *  A polynomial transformation of degree 2 is computed that fits the best those points
 * @author thoorfr
 */
public class GcpsGeoTransform implements GeoTransform {

    private List<Gcp> gcps;
    private CoordinateReferenceSystem sourceCRS;
    private WarpTransform2D pix2geo;
    private WarpTransform2D geo2pix;
  //  private int m_translationX;
  //  private int m_translationY;
    
    /**
     * Create your GeoTransform provided the initial list of gcps
     * @param gcps is the list of gcps of the image
     * @param epsgGeoProj is the String that codes the projection of your mapped gcps
     *  for instance "EPSG:4326". The list of possible values are the one from Geotools library
     */
    public GcpsGeoTransform(List<Gcp> gcps, String epsgGeoProj) {
        this.gcps = gcps;
        try {
            sourceCRS = CRS.decode(epsgGeoProj);
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
        geo2pix = new WarpTransform2D(dst, src, i < 6 ? 1 : 2);
        
    //    m_translationX = 0;
    //    m_translationY = 0;
    }

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
        out[0] = out[0];// + m_translationX;
        out[1] = out[1];// + m_translationY;
        return out;
    }

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
                    Logger.getLogger(GcpsGeoTransform.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        geo2pix.transform(dest, destOffset, dest, destOffset, numPoints);
        for (int i = 0; i < numPoints * 2;) {
            dest[0] = dest[0];// + m_translationX;
            dest[1] = dest[1];// + m_translationY;
        }

        return dest;
    }

    public double[] getGeoFromPixel(double[] src, int srcOffset, double[] dest, int destOffset, int numPoints, String outputEpsgProjection) {
        double[] srctranslated = new double[src.length];
        for (int i = 0; i < numPoints * 2;) {
            srctranslated[0] = src[0];// + m_translationX;
            srctranslated[1] = src[1];// + m_translationY;
        }
        if (dest == null) {
            dest = new double[src.length];
        }
        pix2geo.transform(src, srcOffset, dest, destOffset, numPoints);
        if (outputEpsgProjection != null) {
            for (int i = 0; i < numPoints * 2;) {
                try {
                    double[] temp = new double[3];
                    CoordinateReferenceSystem crs = CRS.decode(outputEpsgProjection);
                    MathTransform math = CRS.findMathTransform(sourceCRS, crs);
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
        // keep new tranform values
        //m_translationX += x;
        //m_translationY += y;
    }

    public int[] getTransformTranslation() {
        return new int[]{};//m_translationX, m_translationY};
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
}
