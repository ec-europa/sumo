/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.io.File;

import org.geoimage.def.SarImageReader;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.configuration.PlatformConfiguration;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.io.SimpleShapefile;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author thoorfr.
 * this class is called when you want to load a coast line for an active image. The land mask is based on the GSHHS shapefile which is situated on /org/geoimage/viewer/core/resources/shapefile/.
 * 
 */
public class AddGenericWorldLayerAction extends AddWorldVectorLayerAction {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(AddGenericWorldLayerAction.class);
	
	private String name="";
    private File worldFile;

    public AddGenericWorldLayerAction(String actionName,File worldFile) {
    	name=actionName;
    	this.worldFile=worldFile;
    }

    public String getName() {
        return name;
    }

    public boolean execute(final String[] args) {
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
                            //addLayerInThread("noncomplexlayer", gl, (ImageLayer) l);
                            LayerManager.addLayerInThread(FactoryLayer.TYPE_NON_COMPLEX, gl, l);
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

    /*
     * 
     * @param type
     * @param layer
     * @param il
     *
    public void addLayerInThread(final String type, final GeometricLayer layer, final ImageLayer il) {
        if (layer != null) {
            new Thread(new Runnable() {

                public void run() {
                    GenericLayer ivl = FactoryLayer.createGenericLayer(type, layer, il.getImageReader(),"");
                    ivl.setColor(Color.GREEN);
                    ivl.setWidth(5);
                    Platform.getLayerManager().addLayer((ILayer) ivl);
                    done = true;
                }
            }).start();
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JOptionPane.showMessageDialog(null, "Empty layer, not added to layers", "Warning", JOptionPane.ERROR_MESSAGE);
                }
            });
            done = true;
        }
    }*/
    
    public String getPath() {
        return "Import/Coastline/"+name;
    }

}
