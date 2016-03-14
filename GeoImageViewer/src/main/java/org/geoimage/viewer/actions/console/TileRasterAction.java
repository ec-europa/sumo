package org.geoimage.viewer.actions.console;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.geoimage.analysis.MaskGeometries;
import org.geoimage.def.SarImageReader;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.analysisproc.AnalysisProcess;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.api.ilayer.IMask;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.SumoActionEvent;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.geoimage.viewer.widget.dialog.ActionDialog.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TileRasterAction extends AbstractConsoleAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(TileRasterAction.class);
	boolean done=false;
    private AnalysisProcess proc=null;


    public TileRasterAction(){
    	super("raster","None");
    }


	@Override
	public String getDescription() {
		return "Create rasters for a tile ";
	}




	@Override
	public boolean executeFromConsole() {
		try {
			SarImageReader sar=(SarImageReader) SumoPlatform.getApplication().getCurrentImageReader();
			ImageLayer layer=LayerManager.getIstanceManager().getCurrentImageLayer();

			if(layer!=null && commandLine.length>=3){
				String arg0=commandLine[0];
			//run vds analysis on a single tile
				if(arg0.equalsIgnoreCase("raster")){
					String arg1=commandLine[1];
					String arg2=commandLine[2];
					String folderOut=null;
					if(commandLine.length==4){
						String arg3=commandLine[3];
						folderOut=arg3;
					}
					int row=Integer.parseInt(arg1);
					int col=Integer.parseInt(arg2);

					//read the land mask
	                for (ILayer l : LayerManager.getIstanceManager().getChilds(layer)) {
	                    if (l instanceof IMask ) {
	                    	MaskVectorLayer mask=(MaskVectorLayer) l;
	                    	MaskGeometries	mg=new MaskGeometries(mask.getName(), mask.getGeometries());


	                    	if(folderOut==null)
	                    		folderOut=SumoPlatform.getApplication().getCachePath();

                    		File output=new File(folderOut+File.separator+mask.getName()+".png");

	                    	int y=layer.getRealTileSizeY()*row;
	    					int x=layer.getRealTileSizeX()*col;

	                    	boolean saved=mg.saveRaster(x, y, layer.getRealTileSizeX(),layer.getRealTileSizeY(), 0, 0, 1, output);

	                    }
	                }

				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}finally{
		}
		return true;
	}


	@Override
	public List<Argument> getArgumentTypes() {
		List <Argument> args=new  ArrayList<Argument>();
		return args;
	}



	public void nextVDSAnalysisStep(int numSteps){
		super.notifyEvent(new SumoActionEvent(SumoActionEvent.UPDATE_STATUS, "",numSteps));
	}




	@Override
	public void actionPerformed(ActionEvent e) {
		if(proc!=null&&e.getActionCommand().equals("STOP")){
			this.proc.setStop(true);

			SumoPlatform.getApplication().getMain().removeStopListener(this);
			this.proc=null;
		}
	}


	@Override
	public String getCommand() {
		return "raster";
	}


	@Override
	public boolean execute() {
		return executeFromConsole();
	}


}
