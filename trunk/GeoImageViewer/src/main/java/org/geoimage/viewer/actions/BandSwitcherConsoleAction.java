/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.util.List;
import java.util.Vector;

import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.IConsoleAction;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.layers.FastImageLayer;

/**
 *
 * @author thoorfr
 * this class manages the switch between different bands if the loaded image is multiband. Inserting �bs� in the console the visible band change each time.
 *
 */
public class BandSwitcherConsoleAction implements IConsoleAction {

    public String getName() {
        return "bs";
    }

    public String getDescription() {
        return "";
    }

    public boolean execute(String[] args) {
        for (ILayer l : Platform.getLayerManager().getLayers()) {
            if (l instanceof IImageLayer & l.isActive()) {
            	IImageLayer imL=((IImageLayer) l);
            	
            	//int[] bands=((IImageLayer) l).getBands()[0]+1);
            	int bb=(imL.getBand()+1) % ((IImageLayer) l).getNumberOfBands();
            	imL.setBand(bb);
           		((FastImageLayer)imL).setName(imL.getImageReader());
                Platform.setInfo(imL.getImageReader().getBandName(((IImageLayer) l).getBand()), 2000);
            }
        }
        return true;
    }

    public String getPath() {
        return "Tools/Band Switch";
    }

    public List<Argument> getArgumentTypes() {
        Argument a1 = new Argument("offset", Argument.FLOAT, true, 0);
        Vector<Argument> out = new Vector<Argument>();
        out.add(a1);
        return out;
    }
}
