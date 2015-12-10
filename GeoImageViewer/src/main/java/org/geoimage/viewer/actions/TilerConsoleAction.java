/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.util.ImageTiler;
import org.jrc.sumo.core.api.Argument;
import org.jrc.sumo.core.api.iactions.AbstractAction;
import org.jrc.sumo.core.api.layer.ILayer;

/**
 *
 * @author thoorfr
 */
public class TilerConsoleAction extends AbstractAction {

    public String getName() {
        return "tiler";
    }

    public String getDescription() {
        return " Build the tiles overview of the current image.\n" +
                "Use \"tiler\" ";
    }

    public boolean execute(String[] args) {
        for (final ILayer l : LayerManager.getIstanceManager().getLayers().keySet()) {
            if (l instanceof ImageLayer & l.isActive()) {
                new Thread(new Runnable() {
                    public void run() {
                        GeoImageReader gir =GeoImageReaderFactory.createReaderForName(((ImageLayer)l).getImageReader().getFilesList()[0],null).get(0);
                        ImageTiler it = new ImageTiler(gir);
                        it.generateTiles(((ImageLayer)l).getActiveBand());
                        gir.dispose();
                    }
                }).start();
                return true;
            }
        }
        return true;
    }

    public String getPath() {
        return "Tools/Image/Create Tiles";
    }
    public List<Argument> getArgumentTypes() {
        return null;
    }
}
