/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.util.List;
import java.util.Vector;

import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.iactions.AbstractConsoleAction;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.image.ImageLayer;

/**
 *
 * @author thoorfr
 * this class manages the switch between different bands if the loaded image is multiband. Inserting �bs� in the console the visible band change each time.
 *
 */
public class BandSwitcherConsoleAction extends AbstractConsoleAction {

    public String getName() {
        return "bs";
    }

    public String getDescription() {
        return "";
    }

    public boolean execute(String[] args) {
        for (ILayer l : LayerManager.getIstanceManager().getLayers().keySet()) {
            if (l instanceof ImageLayer & l.isActive()) {
            	ImageLayer imL=((ImageLayer) l);
            	
            	//int[] bands=((IImageLayer) l).getBands()[0]+1);
            	int bb=imL.getActiveBand()+1;
            	if(bb==(imL.getNumberOfBands())){
            		bb=0;
            	}
            	imL.setActiveBand(bb);
            	
           		((ImageLayer)imL).setName(imL.getImageReader());
                SumoPlatform.setInfo(imL.getImageReader().getBandName(((ImageLayer) l).getActiveBand()), 2000);
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


	@Override
	public String getCommand() {
		return "bs";
	}
}
