/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoimage.impl.GeotiffWriter;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.iactions.AbstractAction;

/**
 *
 * @author thoorfr
 */
public class ExportGeotiffAction extends AbstractAction{

    public String getName() {
        return "exportGeotiff";
    }

    public String getDescription() {
        return "simple export of raster data in geotiff";
    }

    public String getPath() {
        return "Tools/Export Geotiff";
    }

    public boolean execute(String[] args) {
        final File f = new File(args[0]);
        new Thread(new Runnable() {
            public void run() {
                try {
                    Platform.setInfo("Exporting file...", -1);
                    f.createNewFile();
                    GeotiffWriter.create(Platform.getCurrentImageLayer().getImageReader(), 0,f.getAbsolutePath(),null);// Platform.getProgressBar());
                } catch (Exception ex) {
                    Logger.getLogger(ExportGeotiffAction.class.getName()).log(Level.SEVERE, null, ex);
                }
                Platform.setInfo("");
            }
        }).start();
        return true;
    }

    public List<Argument> getArgumentTypes() {
        Argument arg1 = new Argument("File path", Argument.FILE, false, null);
        Vector<Argument> out = new Vector<Argument>();
        out.add(arg1);
        return out;
    }
}
