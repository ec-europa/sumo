/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jrc.sumo.core.api;

/**
 *
 * @author gabbaan
 */
public interface IThreshable {
    
    public double getMaximumThresh();

    public double getMinimumThresh();

    public void setThresh(double thresh);

    public double getThresh();

    public boolean isThreshable();

    public int[] getHistogram(int numClasses);

}
