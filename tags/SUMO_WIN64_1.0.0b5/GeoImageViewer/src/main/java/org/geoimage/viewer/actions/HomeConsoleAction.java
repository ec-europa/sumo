/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.IConsoleAction;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.api.ILayerManager;

/**
 *
 * @author thoorfr
 */
public class HomeConsoleAction implements IConsoleAction {

    public String getName() {
        return "home";
    }

    public String getDescription() {
        return "Reset active image layer to overview\n" +
                "Use \"home\"";

    }

    public boolean execute(String[] args) {
        ILayerManager lm = Platform.getLayerManager();
        for (ILayer l : lm.getLayers()) {
            if (l instanceof IImageLayer & l.isActive()) {
               GeoImageReader gir=((IImageLayer)l).getImageReader();
               
               int x=(gir.getWidth()/Platform.getGeoContext().getWidth());
               int y=gir.getHeight()/Platform.getGeoContext().getHeight();
               int zoom=y;
               if(x>y)
            	   zoom=x;
               
               zoom++;
               
               //Platform.getGeoContext().setX(-(Platform.getGeoContext().getWidth()*10-(gir.getWidth()/zoom))/2);
               Platform.getGeoContext().setX(-(Platform.getGeoContext().getWidth())/2);
               Platform.getGeoContext().setY(0);
               
               Platform.getGeoContext().setZoom(zoom);
            }
        }
        return true;
    }

    public String getPath() {
        return "Tools/Home";
    }

    public List<Argument> getArgumentTypes() {
        return null;
    }
}
