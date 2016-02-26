/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.util.List;

import org.geoimage.def.GeoImageReader;
import org.geoimage.opengl.OpenGLContext;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.api.ILayerManager;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.widget.dialog.ActionDialog.Argument;

/**
 *
 * @author thoorfr
 */
public class HomeConsoleAction extends AbstractConsoleAction {

	public HomeConsoleAction(){
		super("home");
	}
	

    public String getDescription() {
        return "Reset active image layer to overview\n" +
                "Use \"home\"";

    }

    public boolean execute(String[] args) {
        ILayerManager lm = LayerManager.getIstanceManager();
        OpenGLContext geoc=SumoPlatform.getApplication().getGeoContext();
        for (ILayer l : lm.getLayers().keySet()) {
            if (l instanceof ImageLayer & l.isActive()) {
               GeoImageReader gir=((ImageLayer)l).getImageReader();
               
               int x=(gir.getWidth()/geoc.getWidth());
               int y=gir.getHeight()/geoc.getHeight();
               int zoom=y;
               if(x>y)
            	   zoom=x;
               
               zoom++;
               
               //Platform.getGeoContext().setX(-(Platform.getGeoContext().getWidth()*10-(gir.getWidth()/zoom))/2);
               geoc.setX(-(geoc.getWidth())/2);
               geoc.setY(0);
               
               geoc.setZoom(zoom);
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

	@Override
	public String getCommand() {
		return "home";
	}
}
