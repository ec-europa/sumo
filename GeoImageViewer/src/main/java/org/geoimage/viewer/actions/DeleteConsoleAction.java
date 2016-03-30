/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.api.ILayerManager;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.widget.dialog.ActionDialog.Argument;

/**
 *
 * @author thoorfr
 */
public class DeleteConsoleAction extends SumoAbstractAction {

    public DeleteConsoleAction() {
		super("delete","Tools/Delete Active Image");
	}

    public String getDescription() {
        return "Remove layer in ImageLayer.\n" +
                "Use \"delete\" " +
                "Use \"delete imagelayer layername\" to delete the layer called \"layername\" or \"delete imagelayer\" to delete the image layer";
    }

    public boolean execute() {
        ILayerManager lm = SumoPlatform.getApplication().getLayerManager();
        for (ILayer l : lm.getAllLayers()) {
            // delete command
            if (paramsAction==null||paramsAction.size() == 0)
            {
                if (l instanceof ImageLayer & l.isActive()) {
                        lm.removeLayer(l);
                        return true;
                    }
            }
            // delete imagelayer command
            if (paramsAction!=null&&paramsAction.size() == 1){
            	Iterator it=paramsAction.values().iterator();
            	String arg0=(String)it.next();
                if (l instanceof ImageLayer & (l.getName().compareTo(arg0) == 0))
                {
                        lm.removeLayer(l);
                        return true;
                    }
            }
            // delete imagelayer layername command
            if (paramsAction!=null&&paramsAction.size() == 2){
            	Iterator it=paramsAction.values().iterator();
            	String arg0=(String)it.next();
            	String arg1=(String)it.next();
                if (l instanceof ImageLayer & (l.getName().compareTo(arg0) == 0))
                {
                    // loops through the image layer layers
                    for (ILayer lvector : ((ILayerManager)l).getAllLayers())
                    {
                        if(lvector.getName().compareTo(arg1) == 0)
                        {
                            ((ILayerManager)l).removeLayer(lvector);
                            return true;
                        }
                    }
                }
            }
        }
        JOptionPane.showMessageDialog(null, "Could not delete layer", "Error", JOptionPane.ERROR_MESSAGE);
        return true;
    }

    public List<Argument> getArgumentTypes() {
        return null;
    }
}
