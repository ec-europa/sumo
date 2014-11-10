/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.api;

import java.awt.Color;

/**
 * Interface that codes the Vector display behavior
 * @author thoorfr
 */
public interface IVectorLayer {
    /**
     * gets the current displayed Color
     * @return Color
     */
    public Color getColor();
    /**
     * Sets the color to be displayed
     * @param color
     */
    public void setColor(Color color);
    /**
     * Gets the width of the lines of the size of the points
     * @return 
     */
    public float getWidth();
    /**
     * Sets the width of the lines of the size of the points
     * @param width
     */
    public void setWidth(float width);
}
