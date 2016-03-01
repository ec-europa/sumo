/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.util.List;
import java.util.Vector;

import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.widget.dialog.ActionDialog.Argument;

/**
 *
 * @author thoorfr+AG
 *
 * this class manages the contrast of the image. It adds a control in the menu where it is possible to manually insert the desired contrast. Otherwise, the contrast could be managed by the console. The command �c +� followed by a number will increase the actual contrast of the inserted number. The opposite is for �c �� number. The command �c sl� opens a slider for changing the contrast.
 *
 */
public class ContrastConsoleAction extends AbstractConsoleAction {

    public ContrastConsoleAction() {
		super("c");
	}
    public String getCommand() {
        return "c";
    }

    public String getDescription() {
        return "change the scale factor of the active ImageLayer.\n" +
                "Use \"contrast 2.5\" to set the scale factor to 2.5\n" +
                "Use \"contrast +0.1\" to add 0.1 to the current scale factor\n" +
                "Use \"contrast -0.2\" to substract 0.2 to the current scale factor";
    }

    public boolean execute() {
        if (paramsAction.size() != 1) {
            return false;
        }
        String val=paramsAction.get("value");
        //manage a slider for changing the contrast
      /*    for (ILayer l : LayerManager.getIstanceManager().getLayers().keySet()) {
            if (l instanceof ImageLayer && l.isActive()) {
                Slider slider = new Slider(true, Slider.CONTRAST);
                slider.setSize(200, 40);
                slider.setExpandable(false);
                slider.setShrinkable(false);
                slider.addListener(Slider.createContrastListener((ImageLayer) l));
                {
                    FormData fd = new FormData();
                    fd.right = new FormAttachment(100, -50);
                    fd.top = new FormAttachment(100, -300);
                    slider.setLayoutData(fd);
                }
                slider.setValue(((ImageLayer) l).getContrast());
                SumoPlatform.getApplication().getGeoContext().getFenguiDisplay().addWidget(slider);
                slider.hook(SumoPlatform.getApplication().getGeoContext().getFenguiDisplay());

                SumoPlatform.getApplication().getGeoContext().getFenguiDisplay().updateMinSize();
                SumoPlatform.getApplication().getGeoContext().getFenguiDisplay().layout();
                return true;
            }
        }*/
        // c +(number) to increase the actual contrast
        if (val.startsWith("+")) {
            float contrast = Float.parseFloat(val.substring(1));
            for (ILayer l : LayerManager.getIstanceManager().getLayers().keySet()) {
                if (l instanceof ImageLayer & l.isActive()) {
                    ((ImageLayer) l).setContrast(((ImageLayer) l).getContrast() + contrast);
                }
            }
        // c -(number) to decrease the actual contrast
        } else if (val.startsWith("-")) {
            float contrast = Float.parseFloat(val.substring(1));
            for (ILayer l : LayerManager.getIstanceManager().getLayers().keySet()) {
                if (l instanceof ImageLayer & l.isActive()) {
                    ((ImageLayer) l).setContrast(((ImageLayer) l).getContrast() - contrast);
                }
            }
        } else {
            float contrast = Float.parseFloat(val);
            for (ILayer l :LayerManager.getIstanceManager().getLayers().keySet()) {
                if (l instanceof ImageLayer & l.isActive()) {
                    ((ImageLayer) l).setContrast(contrast);
                }
            }
        }

        return true;
    }
    
    public String getPath() {
        return "Tools/Contrast";
    }
    
    public List<Argument> getArgumentTypes() {
        Argument a1=new Argument("value", Argument.FLOAT, true, 1,"value");
        Vector<Argument> out=new Vector<Argument>();
        out.add(a1);
        return out;
    }
}
