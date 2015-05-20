/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.api;

/**
 *
 * @author thoorfr
 */
public interface ILayerListener {
    public void layerAdded(ILayer l);
    public void layerRemoved(ILayer l);
    public void layerClicked(ILayer l);
}
