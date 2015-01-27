/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers;

import java.awt.Frame;
import java.awt.Point;

import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.IClickable;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.widget.ThumbnailsDialog;

/**
 *
 * @author thoorfr
 */
public class ThumbnailsSmallLayer implements ILayer, IClickable {

    private ThumbnailsLayer parent;
    private boolean active = true;
    private Point imagePosition;
    private ThumbnailsDialog pd;

    public ThumbnailsSmallLayer(ThumbnailsLayer layer) {
        this.parent = layer;
        this.pd = new ThumbnailsDialog(Frame.getFrames()[0], false, layer);
        this.pd.setVisible(true);
    }

    public String getName() {
        return "Thumbnails Viewer";
    }

    public void setName(String name) {

    }

    public void render(GeoContext context) {
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.pd.setVisible(active);
    }

    public boolean isRadio() {
        return false;
    }

    public ILayer getParent() {
        return this.parent;
    }

    public String getDescription() {
        return "Gives the positon of the mouse in the image";
    }

    public void dispose() {
        pd.setVisible(false);
        pd.dispose();
    }

    public void mouseClicked(Point imagePosition, int button, GeoContext context) {
        this.imagePosition = imagePosition;
        if (active) {
            pd.setBufferedImage(parent.get(imagePosition));
        }
    }
}
