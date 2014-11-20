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

/**
 *
 * @author thoorfr
 */
public class BrightnessConsoleAction implements IConsoleAction {

    public String getName() {
        return "brightness";
    }

    public String getDescription() {
        return "change the offset factor of the active ImageLayer.\n" +
                "Use \"brightness 2.5\" to set the offset to 2.5\n" +
                "Use \"brightness -100\" to set the offset to -100\n" +
                "Use \"brightness -0.2 +\" to substract 0.2 to the current scale factor";
    }

    public boolean execute(String[] args) {
        if (args.length == 1) {
            for (ILayer l : Platform.getLayerManager().getLayers()) {
                if (l instanceof IImageLayer & l.isActive()) {
                    ((IImageLayer) l).setBrightness(Float.parseFloat(args[0]));
                }
            }
        } else if (args.length == 2) {
            float br = Float.parseFloat(args[0]);
            for (ILayer l : Platform.getLayerManager().getLayers()) {
                if (l instanceof IImageLayer & l.isActive()) {
                    ((IImageLayer) l).setBrightness(((IImageLayer) l).getBrightness() + br);
                }
            }

        }

        return true;
    }
    
    public String getPath() {
        return "Tools/Brightness";
    }
    
    public List<Argument> getArgumentTypes() {
        Argument a1=new Argument("offset", Argument.FLOAT, true, 0);
        Vector<Argument> out=new Vector<Argument>();
        out.add(a1);
        return out;
    }
}
