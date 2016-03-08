/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.io.File;

import org.geoimage.def.SarImageReader;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.io.SimpleShapefile;
import org.geoimage.viewer.core.layers.GenericLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.geoimage.viewer.util.IProgress;
import org.jrc.sumo.configuration.PlatformConfiguration;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author thoorfr.
 * this class is called when you want to load a coast line for an active image. The land mask is based on the GSHHS shapefile which is situated on /org/geoimage/viewer/core/resources/shapefile/.
 *
 */
public class AddGenericWorldLayerAction extends SumoAbstractAction implements IProgress  {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(AddGenericWorldLayerAction.class);
    private File worldFile;

    public AddGenericWorldLayerAction(String actionName,File worldFile) {
    	super(actionName,"Import/Coastline/"+actionName);
    	this.worldFile=worldFile;
    }


    public boolean execute() {
        done = false;
        new Thread(new Runnable() {

            public void run() {
                SumoPlatform.setInfo("Importing land coastline "+name,-1);
                try {
                	ImageLayer  l=LayerManager.getIstanceManager().getCurrentImageLayer();
                	if(l!=null){
                        try {
                        	Polygon imageP=((SarImageReader)l.getImageReader()).getBbox(PlatformConfiguration.getConfigurationInstance().getLandMaskMargin(0));
                            GeometricLayer gl = SimpleShapefile.createIntersectedLayer(worldFile, imageP,l.getImageReader().getGeoTransform());

                            int t=MaskVectorLayer.COASTLINE_MASK;
                            //if(args[1].equalsIgnoreCase("ice"))
                            //	t=MaskVectorLayer.ICE_MASK;
                            GenericLayer mask=FactoryLayer.createMaskLayer(gl,t);

                            LayerManager.addLayerInThread(mask);
                        } catch (Exception ex) {
                           logger.error(ex.getMessage(), ex);
                        }
                	}
                } catch (Exception e) {
                }
                SumoPlatform.getApplication().setInfo(null);
            }
        }).start();
        return true;
    }

    public String getPath() {
        return "Import/Coastline/"+name;
    }


	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean isIndeterminate() {
		return true;
	}


	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public int getMaximum() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getCurrent() {
		// TODO Auto-generated method stub
		return 0;
	}




	@Override
	public void setCurrent(int i) {
		// TODO Auto-generated method stub

	}


	@Override
	public void setMaximum(int size) {
		// TODO Auto-generated method stub

	}



	@Override
	public void setIndeterminate(boolean value) {
		// TODO Auto-generated method stub

	}


	@Override
	public void setDone(boolean value) {
		// TODO Auto-generated method stub

	}







}
