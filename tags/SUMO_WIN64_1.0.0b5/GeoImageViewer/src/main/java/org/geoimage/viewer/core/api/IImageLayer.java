/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.api;

import org.geoimage.def.GeoImageReader;

/**
 *
 * @author thoorfr
 */
public interface IImageLayer extends ILayerManager{

    public float getContrast();
    public float getBrightness();
    public void setContrast(float value);
    public void setBrightness(float value);
    public void setMaximumCut(float value);
    public float getMaximumCut();
    public void setBand(int[] values);
    
    /**
     * return the number of bands for the images
     * @return
     */
    public int getNumberOfBands();
    
    /**
     * return the array with the ids of the bands
     * @return
     */
    public int[] getBands();
    public GeoImageReader getImageReader();
    
    //debug
    public void level(int levelIncrease);
    
    
}
