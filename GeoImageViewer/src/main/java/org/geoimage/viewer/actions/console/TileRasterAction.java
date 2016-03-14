package org.geoimage.viewer.actions.console;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.geoimage.analysis.BlackBorderAnalysis;
import org.geoimage.analysis.ConstantVDSAnalysis;
import org.geoimage.analysis.MaskGeometries;
import org.geoimage.analysis.VDSAnalysis;
import org.geoimage.analysis.VDSAnalysis.ProgressListener;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.impl.s1.Sentinel1;
import org.geoimage.utils.PolygonOp;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.analysisproc.AnalysisProcess;
import org.geoimage.viewer.core.analysisproc.VDSAnalysisProcessListener;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.api.ilayer.IMask;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.GenericLayer;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.SumoActionEvent;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditGeometryVectorLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.geoimage.viewer.widget.dialog.ActionDialog.Argument;
import org.jrc.sumo.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;


public class TileRasterAction extends AbstractConsoleAction implements VDSAnalysisProcessListener,ActionListener{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(TileRasterAction.class);
	boolean done=false;
	private int xx = 0;
    private int yy = 0;
    private int tileSizeX = 0;
    private int tileSizeY = 0;
    private boolean stopping=false;
    private AnalysisProcess proc=null;


    public TileRasterAction(){
    	super("raster");
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


	private void init(){
		GeoImageReader gir=SumoPlatform.getApplication().getCurrentImageReader();

        int tileSize = (int)(Constant.TILESIZE / gir.getPixelsize()[0]);
        if(tileSize < Constant.TILESIZEPIXELS) tileSize = Constant.TILESIZEPIXELS;

        int horTilesImage = gir.getWidth() / tileSize;
        int verTilesImage = gir.getHeight()/ tileSize;

     // the real size of tiles
        tileSizeX = gir.getWidth() / horTilesImage;
        tileSizeX = gir.getHeight() / verTilesImage;

    }




	@Override
	public List<Argument> getArgumentTypes() {
		List <Argument> args=new  ArrayList<Argument>();
		return args;
	}

	@Override
	public void startAnalysis() {
		String message="Starting VDS Analysis";
    	super.notifyEvent(new SumoActionEvent(SumoActionEvent.STARTACTION, message, -1));


	}
	@Override
	public void performVDSAnalysis(String message,int numSteps) {
		if(!stopping){
        	super.notifyEvent(new SumoActionEvent(SumoActionEvent.UPDATE_STATUS, message, numSteps));
		}
	}

	@Override
	public void startAnalysisBand(String message) {
		if(!stopping){
			super.notifyEvent(new SumoActionEvent(SumoActionEvent.UPDATE_STATUS, message,-1));
		}
	}

	@Override
	public void calcAzimuthAmbiguity(String message) {
		if(!stopping){
			super.notifyEvent(new SumoActionEvent(SumoActionEvent.UPDATE_STATUS, message,-1));
		}
	}

	@Override
	public void agglomerating(String message) {
		if(!stopping){
			super.notifyEvent(new SumoActionEvent(SumoActionEvent.UPDATE_STATUS, message,-1));
		}
	}

	public void nextVDSAnalysisStep(int numSteps){
		super.notifyEvent(new SumoActionEvent(SumoActionEvent.UPDATE_STATUS, "",numSteps));
	}


	@Override
	public void endAnalysis() {
		SumoPlatform.getApplication().getMain().removeStopListener(this);

		if(proc!=null)
			proc.dispose();
		super.notifyEvent(new SumoActionEvent(SumoActionEvent.ENDACTION,"Analysis Complete",-1));
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
	public void layerReady(ILayer layer) {
		if(!SumoPlatform.isBatchMode()){
			ComplexEditGeometryVectorLayer clayer=(ComplexEditGeometryVectorLayer)layer;
			com.vividsolutions.jts.geom.Polygon box;
			try {
				double[] v1=new double[]{xx,yy};
				double[] v2=new double[]{xx+tileSizeX,yy};
				double[] v3=new double[]{xx+tileSizeY,yy+tileSizeX};
				double[] v4=new double[]{xx,yy+tileSizeY};
				double[] v5=new double[]{xx,yy};
				box = PolygonOp.createPolygon(v1,v2,v3,v4,v5);
				List <Geometry>gg=new ArrayList<>();
				gg.add(box);
				clayer.addGeometries("Tile", Color.YELLOW,2,GeometricLayer.POLYGON, gg, true);
			} catch (ParseException e) {
				logger.warn("box not added");
			}
			LayerManager.getIstanceManager().addLayer(layer);
		}
	}



	@Override
	public void startBlackBorederAnalysis(String message) {
		if(!stopping){
			super.notifyEvent(new SumoActionEvent(SumoActionEvent.STARTACTION,message,-1));
		}

	}


	@Override
	public String getCommand() {
		return "chktile";
	}


	@Override
	public boolean execute() {
		return executeFromConsole();
	}


}
