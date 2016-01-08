/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.util.List;

import javax.swing.JOptionPane;

import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.ILayerManager;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.layers.image.ImageLayer;

/**
 *
 * @author thoorfr
 */
public class DeleteConsoleAction extends AbstractAction {

    public String getName() {
        return "delete";
    }

    public String getDescription() {
        return "Remove layer in ImageLayer.\n" +
                "Use \"delete\" " +
                "Use \"delete imagelayer layername\" to delete the layer called \"layername\" or \"delete imagelayer\" to delete the image layer";
    }

    public boolean execute(String[] args) {
        ILayerManager lm = SumoPlatform.getApplication().getLayerManager();
        for (ILayer l : lm.getAllLayers()) {
            // delete command
            if (args==null||args.length == 0)
            {
                if (l instanceof ImageLayer & l.isActive()) {
                        lm.removeLayer(l);
                        return true;
                    }
            }
            // delete imagelayer command
            if (args!=null&&args.length == 1)
            {
                if (l instanceof ImageLayer & (l.getName().compareTo(args[0]) == 0))
                {
                        lm.removeLayer(l);
                        return true;
                    }
            }
            // delete imagelayer layername command
            if (args!=null&&args.length == 2)
            {
                if (l instanceof ImageLayer & (l.getName().compareTo(args[0]) == 0))
                {
                    // loops through the image layer layers
                    for (ILayer lvector : ((ILayerManager)l).getAllLayers())
                    {
                        if(lvector.getName().compareTo(args[1]) == 0)
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

    public String getPath() {
        return "Tools/Delete Active Image";
    }
    
    public List<Argument> getArgumentTypes() {
        return null;
    }
}
