/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.api.ilayer;

import java.awt.geom.Area;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Interface that handles the methods to mask part of the raster
 * @author Pietro Argentieri
 */
public interface IMask extends ILayer{
	
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
