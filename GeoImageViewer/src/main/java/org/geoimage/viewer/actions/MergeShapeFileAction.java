/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.actions;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.geoimage.def.SarImageReader;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.io.SimpleShapefile;
import org.geoimage.viewer.core.layers.GenericLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.geoimage.viewer.widget.dialog.ActionDialog;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thoorfr.
 * this class is called when you want to load a coast line for an active image. The land mask is based on the GSHHS shapefile which is situated on /org/geoimage/viewer/core/resources/shapefile/.
 *
 */
public class MergeShapeFileAction extends SumoAbstractAction  {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(MergeShapeFileAction.class);

    private JFileChooser fd;

    public MergeShapeFileAction() {
    	super("merge","Import/Merge shape file");
    	fd = new JFileChooser();
    }


    public boolean execute() {
    	final File shpFile=selectFile();
        if(shpFile!=null)
	        new Thread(new Runnable() {
	            public void run() {
	                SumoPlatform.setInfo("Building new coastline ",-1);
	                try {
	                	ImageLayer  l=LayerManager.getIstanceManager().getCurrentImageLayer();
	                	if(l!=null){
	                        try {
	                        	MaskVectorLayer ml=LayerManager.getIstanceManager().getChildMaskLayer(l);
	                        	if(ml!=null){

	                        		SimpleFeatureCollection collectionsLayer=(SimpleFeatureCollection) ml.getGeometriclayer().getFeatureCollection();

	                        		GeometricLayer gl = SimpleShapefile.mergeShapeFile(collectionsLayer, shpFile,l.getImageReader().getGeoTransform(),
	                        				((SarImageReader)l.getImageReader()).getBbox(100));

	                        		int t=MaskVectorLayer.COASTLINE_MASK;
	                                if(paramsAction.get("data_type").equalsIgnoreCase("ice"))
	                                	t=MaskVectorLayer.ICE_MASK;
	                        		GenericLayer lay=FactoryLayer.createMaskLayer(gl,t);

	                        		LayerManager.addLayerInThread(lay);
	                        	}
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


	@Override
	public String getDescription() {
		return "Merge a new shape file with the current land mask";
	}


	@Override
	public List<ActionDialog.Argument> getArgumentTypes() {
		return null;
	}

}
