/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.api.ilayer;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.List;


import com.vividsolutions.jts.geom.Geometry;

/**
 * Interface that handles the methods to mask part of the raster
 * @author Pietro Argentieri
 */
public interface IMask extends ILayer{
	/**
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
   // public boolean intersects(int x, int y, int width, int height);
    /**
     * 
     * @param x
     * @param y
     * @return
     */
  //  public boolean contains(int x, int y);
    /**
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
  //  public boolean includes(int x, int y, int width, int height);
    /**
     * rasterize the mask clipped with the Rectangle scaled back to full size with an offset onto a BufferedImage
     * 
     * @param rect
     * @param offsetX
     * @param offsetY
     * @param scalingFactor
     * @return
     */
   // public BufferedImage rasterize(Rectangle rect, int offsetX, int offsetY, double scalingFactor);
    /**
     * rasterize the mask clipped with the Rectangle scaled back to full size with an offset onto a BufferedImage
     * 
     * @param x
     * @param y
     * @param w
     * @param h
     * @param offsetX
     * @param offsetY
     * @param scalingFactor
     * @return
     */
    //public BufferedImage rasterize(int x,int y,int w,int h,  int offsetX, int offsetY, double scalingFactor) ;
    /**
     * 
     * @param width
     * @param height
     * @return
     */
    public Area getShape(int width, int height);
    /**
     * 
     * @param bufferingDistance
     */
    public void buffer(double bufferingDistance);
    /**
     * 
     * @return
     */
    public List<Geometry> getGeometries();
    

}
