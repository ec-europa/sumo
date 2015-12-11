/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.thumbnails;

import java.awt.Frame;
import java.awt.Point;

import org.geoimage.opengl.OpenGLContext;
import org.geoimage.viewer.core.layers.GenericLayer;
import org.geoimage.viewer.widget.dialog.ThumbnailsDialog;
import org.jrc.sumo.core.api.IClickable;
import org.jrc.sumo.core.api.layer.ILayer;

/**
 *
 * @author thoorfr
 */
public class ThumbnailsSmallLayer extends GenericLayer implements  IClickable {

    private boolean active = true;
    private Point imagePosition;
    

	private ThumbnailsDialog pd;

    public ThumbnailsSmallLayer(ThumbnailsLayer layer) {
    	super(layer,"",null,null);
        this.pd = new ThumbnailsDialog(Frame.getFrames()[0], false, layer);
        this.pd.setVisible(true);
        super.init((ILayer)layer);
    }

    public String getName() {
        return "Thumbnails Viewer";
    }

    public void setName(String name) {

    }

    public void render(OpenGLContext context) {
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

    public Point getImagePosition() {
		return imagePosition;
	}

	public void setImagePosition(Point imagePosition) {
		this.imagePosition = imagePosition;
	}

    public String getDescription() {
        return "Gives the positon of the mouse in the image";
    }

    public void dispose() {
        pd.setVisible(false);
        pd.dispose();
    }

    public void mouseClicked(Point imagePosition, int button,Object geoContext) {
        this.imagePosition = imagePosition;
        if (active) {
            pd.setBufferedImage(((ThumbnailsLayer)super.getParent()).get(imagePosition));
        }
    }
}
