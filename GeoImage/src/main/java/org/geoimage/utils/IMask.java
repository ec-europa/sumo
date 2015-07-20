/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.utils;

import com.vividsolutions.jts.geom.Geometry;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.List;

import org.geoimage.viewer.core.api.ILayer;

/**
 * Interface that handles the methods to mask part of the raster
 * @author thoorfr
 */
public interface IMask extends ILayer{

    public boolean intersects(int x, int y, int width, int height);
    public boolean contains(int x, int y);
    public boolean includes(int x, int y, int width, int height);
    public BufferedImage rasterize(Rectangle rect, int offsetX, int offsetY, double scalingFactor);
    public BufferedImage rasterize(int x,int y,int w,int h,  int offsetX, int offsetY, double scalingFactor) ;
    public Area getShape(int width, int height);
    public void buffer(double bufferingDistance);
    public List<Geometry> getGeometries();
    

}
