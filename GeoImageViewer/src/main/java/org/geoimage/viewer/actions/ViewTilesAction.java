/*
 * 
 */
package org.geoimage.viewer.actions;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.geoimage.analysis.ConstantVDSAnalysis;
import org.geoimage.def.GeoImageReader;
import org.geoimage.viewer.actions.console.AbstractConsoleAction;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.SimpleGeometryLayer;
import org.geoimage.viewer.util.GeometryExtractor;
import org.jrc.sumo.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class ViewTilesAction extends AbstractConsoleAction{
	private Logger logger = LoggerFactory.getLogger(ViewTilesAction.class);
	boolean done=false;
	int tileSize=0;


	public ViewTilesAction(){
    	super("viewtile","Tools/ViewTile");
    }


	@Override
	public String getDescription() {
		return "ViewTile";
	}



	@Override
	public boolean executeFromConsole() {
		try {
			done = false;

			if(super.commandLine!=null){
				tileSize=Integer.parseInt(commandLine[1]);
			}

	        new Thread(new Runnable() {

	            public void run() {
	                try {
	                	ImageLayer  l=LayerManager.getIstanceManager().getCurrentImageLayer();
	                	if(l!=null){
	                        try {
	                        	GeoImageReader gir=l.getImageReader();
	                        	if(tileSize==0){
	                        		tileSize = (int)(Constant.TILESIZE / gir.getPixelsize()[0]);
	                        		if(tileSize < Constant.TILESIZEPIXELS) tileSize = Constant.TILESIZEPIXELS;
	                        	}

	                        	List <Geometry> tiles=GeometryExtractor.getTiles(l.getImageReader().getWidth(),
	                        			l.getImageReader().getHeight(),tileSize,tileSize);
	                        	SimpleGeometryLayer sgl=new SimpleGeometryLayer(l, "tiles",tiles,SimpleGeometryLayer.LINESTRING);
	                            addLayerInThread(sgl);
	                        } catch (Exception ex) {
	                            logger.error(ex.getMessage(),ex);
	                        }
	                	}
	                } catch (Exception e) {
	                }finally{
	                	done=true;
	                }
	                notifyEvent(new SumoActionEvent(SumoActionEvent.ENDACTION,"",-1));
	            }
	        }).start();
	        return true;

		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
	}

	 /**
     *
     * @param type
     * @param layer
     * @param il
     */
    public void addLayerInThread(final SimpleGeometryLayer layer) {
        if (layer != null) {
            new Thread(new Runnable() {

                public void run() {
                    LayerManager.getIstanceManager().addLayer(layer);
                    done = true;
                }
            }).start();
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JOptionPane.showMessageDialog(null, "Error adding layer", "Warning", JOptionPane.ERROR_MESSAGE);
                }
            });
            done = true;
        }
    }


	@Override
	public String getCommand() {
		return "viewtile";
	}


	@Override
	public boolean execute() {
		if(paramsAction!=null&&!paramsAction.isEmpty()){
			tileSize=Integer.parseInt(paramsAction.values().iterator().next());
		}
		return executeFromConsole();
	}




}
