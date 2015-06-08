/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.IVectorLayer;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.factory.VectorIOFactory;
import org.geoimage.viewer.core.io.AbstractVectorIO;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.slf4j.LoggerFactory;

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
                Platform.setInfo("Importing land coastline "+name,-1);
                try {
                	IImageLayer  l=Platform.getCurrentImageLayer();
                	if(l!=null){
                        try {
                        	URL url=worldFile.toURI().toURL();
                            Map<String,Object> config=new HashMap<String,Object>();
                            config.put("url", url);
                            AbstractVectorIO shpio = VectorIOFactory.createVectorIO(VectorIOFactory.SIMPLE_SHAPEFILE, config);
                            GeometricLayer gl = shpio.read(l.getImageReader());
                            addLayerInThread("noncomplexlayer", gl, (IImageLayer) l);
                        } catch (Exception ex) {
                           logger.error(ex.getMessage(), ex);
                        }
                	}   
                } catch (Exception e) {
                }
                Platform.setInfo(null);
            }
        }).start();
        return true;
    }

    /**
     * 
     * @param type
     * @param layer
     * @param il
     */
    public void addLayerInThread(final String type, final GeometricLayer layer, final IImageLayer il) {
        if (layer != null) {
            new Thread(new Runnable() {

                public void run() {
                    IVectorLayer ivl = FactoryLayer.createVectorLayer(type, layer, il.getImageReader(),"");
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
    }
    
    public String getPath() {
        return "Import/Coastline/"+name;
    }

}
