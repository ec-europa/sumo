/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

import org.geoimage.viewer.core.api.iactions.IAction;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.widget.dialog.ActionDialog.Argument;

/**
 *
 * @author thoorfr
 */
public class BrightnessConsoleAction extends SumoAbstractAction implements IAction	 {

	public BrightnessConsoleAction(){
		super("","");
	}

    public String getName() {
        return "brightness";
    }

    public String getDescription() {
        return "change the offset factor of the active ImageLayer.\n" +
                "Use \"brightness 2.5\" to set the offset to 2.5\n" +
                "Use \"brightness -100\" to set the offset to -100\n" +
                "Use \"brightness -0.2 +\" to substract 0.2 to the current scale factor";
    }

    public boolean execute() {
    	Object fl=paramsAction.values().iterator().next();
        if (paramsAction.size() == 1) {
            for (ILayer l : LayerManager.getIstanceManager().getLayers().keySet()) {
                if (l instanceof ImageLayer & l.isActive()) {
                    ((ImageLayer) l).setBrightness(Float.parseFloat((String)fl));
                }
            }
        } else if (paramsAction.size() == 2) {
            float br = Float.parseFloat((String)fl);
            for (ILayer l : LayerManager.getIstanceManager().getLayers().keySet()) {
                if (l instanceof ImageLayer & l.isActive()) {
                    ((ImageLayer) l).setBrightness(((ImageLayer) l).getBrightness() + br);
                }
            }

        }

        return true;
    }

    public String getPath() {
        return "Tools/Brightness";
    }

    public List<Argument> getArgumentTypes() {
        Argument a1=new Argument("offset", Argument.FLOAT, true, 0,"offset");
        Vector<Argument> out=new Vector<Argument>();
        out.add(a1);
        return out;
    }

	@Override
	public void actionPerformed(ActionEvent e) {
	}

	@Override
	public boolean isIndeterminate() {
		return true;
	}

	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	public int getMaximum() {
		return 0;
	}

	@Override
	public int getCurrent() {
		return 0;
	}


	@Override
	public void setCurrent(int i) {

	}

	@Override
	public void setMaximum(int size) {

	}

	@Override
	public void setIndeterminate(boolean value) {

	}

	@Override
	public void setDone(boolean value) {
	}
}
