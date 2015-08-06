/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.iactions.AbstractAction;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.io.SimpleShapefile;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thoorfr.
 * this class is called when you want to load a coast line for an active image. The land mask is based on the GSHHS shapefile which is situated on /org/geoimage/viewer/core/resources/shapefile/.
 * 
 */
public class MergeShapeFileAction extends AbstractAction  {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(MergeShapeFileAction.class);
	
    private JFileChooser fd;
    
    public MergeShapeFileAction() {
    	fd = new JFileChooser();
    }


    public boolean execute(final String[] args) {
    	final File shpFile=selectFile();
        if(shpFile!=null)
	        new Thread(new Runnable() {
	            public void run() {
	                Platform.setInfo("Building new coastline ",-1);
	                try {
	                	ImageLayer  l=Platform.getCurrentImageLayer();
	                	if(l!=null){
	                        try {
	                        	ImageLayer layer=Platform.getCurrentImageLayer();
	                        	MaskVectorLayer ml=Platform.getLayerManager().getChildMaskLayer(layer);
	                        	if(ml!=null){
	                        		GeometricLayer gl = SimpleShapefile.addShape(ml.getGeometriclayer(), shpFile,l.getImageReader().getGeoTransform());
	                        		LayerManager.addLayerInThread(FactoryLayer.TYPE_NON_COMPLEX, gl, (ImageLayer) l);
	                        	}	
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
     * @return
     */
    private File selectFile(){
    	File file=null;
    	FileFilter f = new FileFilter() {

            public boolean accept(File f) {
                return f.isDirectory() || f.getPath().endsWith("shp") || f.getPath().endsWith("SHP");
            }

            public String getDescription() {
                return "Shapefiles";
            }
        };
        fd.setFileFilter(f);

        int returnVal = fd.showOpenDialog(null);
        fd.removeChoosableFileFilter(f);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fd.getSelectedFile();
        } 
        return file;
    }
    
    public String getPath() {
        return "Import/Merge shape file";
    }


	@Override
	public String getName() {
		return "merge";
	}


	@Override
	public String getDescription() {
		return "Merge a new shape file with the current land mask";
	}


	@Override
	public List<Argument> getArgumentTypes() {
		return null;
	}

}
