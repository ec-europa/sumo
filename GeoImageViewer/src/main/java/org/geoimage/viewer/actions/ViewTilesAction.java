package org.geoimage.viewer.actions;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.geoimage.analysis.ConstantVDSAnalysis;
import org.geoimage.def.GeoImageReader;
import org.geoimage.utils.GeometryExtractor;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.iactions.AbstractAction;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.SimpleGeometryLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class ViewTilesAction extends AbstractAction{
	private Logger logger = LoggerFactory.getLogger(ViewTilesAction.class);
	boolean done=false;
	int tileSize=0;
	
	@Override
	public String getName() {
		return "viewtile";
	}

	@Override
	public String getDescription() {
		return "ViewTile";
	}

	@Override
	public String getPath() {
		return "Tools/ViewTile";
	}

	@Override
	public boolean execute(String[] args) {
		try {
			done = false;
			if(args!=null){
				tileSize=Integer.parseInt(args[0]);
				
			}
			
	        new Thread(new Runnable() {

	            public void run() {
	                try {
	                	ImageLayer  l=Platform.getCurrentImageLayer();
	                	if(l!=null){
	                        try {
	                        	GeoImageReader gir=l.getImageReader();
	                        	if(tileSize==0){
	                        		tileSize = (int)(ConstantVDSAnalysis.TILESIZE / gir.getPixelsize()[0]);
	                        		if(tileSize < ConstantVDSAnalysis.TILESIZEPIXELS) tileSize = ConstantVDSAnalysis.TILESIZEPIXELS;
	                        	}	
	                    		
	                        	List <Geometry> tiles=GeometryExtractor.getTiles(l.getImageReader().getWidth(),l.getImageReader().getHeight(),tileSize);
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
	                Platform.setInfo(null);
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
                    Platform.getLayerManager().addLayer(layer);
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
	public List<Argument> getArgumentTypes() {
		//List <Argument> args=new  ArrayList<Argument>();
		return null;
	}

	

}
