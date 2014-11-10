/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.api;

import org.geoimage.viewer.core.*;


/**
 *
 * @author thoorfr
 */
public interface ILayer {
    public String getName();
    public void setName(String name);
    public void render(GeoContext context);
    public boolean isActive();
    public void setActive(boolean active);
    public boolean isRadio();
    public ILayerManager getParent();
    public String getDescription();
    public void dispose();
}
