/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.IConsoleAction;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.util.ImageTiler;

/**
 *
 * @author thoorfr
 */
public class TilerConsoleAction implements IConsoleAction {

    public String getName() {
        return "tiler";
    }

    public String getDescription() {
        return " Build the tiles overview of the current image.\n" +
                "Use \"tiler\" ";
    }

    public boolean execute(String[] args) {
        for (final ILayer l : Platform.getLayerManager().getLayers().keySet()) {
            if (l instanceof IImageLayer & l.isActive()) {
                new Thread(new Runnable() {
                    public void run() {
                        GeoImageReader gir =GeoImageReaderFactory.createReaderForName(((IImageLayer)l).getImageReader().getFilesList()[0]).get(0);
                        ImageTiler it = new ImageTiler(gir);
                        it.generateTiles(((IImageLayer)l).getBand());
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
