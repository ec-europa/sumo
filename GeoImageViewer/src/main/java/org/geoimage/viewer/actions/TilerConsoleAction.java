/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import org.geoimage.def.GeoImageReader;
import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.util.ImageTiler;

/**
 *
 * @author thoorfr
 */
public class TilerConsoleAction extends SumoAbstractAction {


	public TilerConsoleAction(){
    	super("tiler","Tools/Image/Create Tiles");
    }


    public String getDescription() {
        return " Build the tiles overview of the current image.\n" +
                "Use \"tiler\" ";
    }

    public boolean execute() {
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




}
