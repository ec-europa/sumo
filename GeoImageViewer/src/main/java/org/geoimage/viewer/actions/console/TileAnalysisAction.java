package org.geoimage.viewer.actions.console;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import org.geoimage.analysis.BlackBorderAnalysis;
import org.geoimage.analysis.MaskGeometries;
import org.geoimage.analysis.VDSAnalysis;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;


public class TileAnalysisAction extends AbstractConsoleAction implements VDSAnalysisProcessListener{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(TileAnalysisAction.class);
	boolean done=false;
	private int xx = 0;
    private int yy = 0;
    private int tileSizeX = 0;
    private int tileSizeY = 0;
    private boolean stopping=false;
    private AnalysisProcess proc=null;


    public TileAnalysisAction(){
    	super("chktile","Tools/CheckTile");
    }


	@Override
	public String getDescription() {
		return "Analyze Tile Values";
	}


	private void runBBAnalysis(){
		//run the blackborder analysis for the s1 images
		BlackBorderAnalysis blackBorderAnalysis=null;
		GeoImageReader gir=SumoPlatform.getApplication().getCurrentImageReader();
		ImageLayer il=SumoPlatform.getApplication().getLayerManager().getCurrentImageLayer();
        if(gir instanceof Sentinel1){
                /*MaskVectorLayer mv=null;
           	 	if(bufferedMask!=null&&bufferedMask.length>0)
           	 		mv=(MaskVectorLayer)bufferedMask[0];
           	 	if(mv!=null)
           	 		blackBorderAnalysis= new BlackBorderAnalysis(gir,mv.getGeometries());
           	 	else*/
           	blackBorderAnalysis= new BlackBorderAnalysis(gir,il.getRealTileSizeX(),il.getRealTileSizeY(),null);
         }
         if(blackBorderAnalysis!=null){
        	 int nTile=SumoPlatform.getApplication().getConfiguration().getNumTileBBAnalysis();
        	 blackBorderAnalysis.analyse(nTile,BlackBorderAnalysis.ANALYSE_ALL);
        	 List<Coordinate>cc=blackBorderAnalysis.getCoordinatesThresholds();
        	 GeometricLayer bbanal=new GeometricLayer("BBAnalysis",GeometricLayer.POINT,cc);
        	 GenericLayer ivl = FactoryLayer.createComplexLayer(bbanal);
        	 ivl.setColor(Color.GREEN);
        	 ivl.setWidth(5);
        	 LayerManager.addLayerInThread(ivl);

         }
         //end blackborder analysis
	}


	@Override
	public boolean executeFromConsole() {
		try {
			SarImageReader sar=(SarImageReader) SumoPlatform.getApplication().getCurrentImageReader();
			ImageLayer layer=LayerManager.getIstanceManager().getCurrentImageLayer();

			if(layer!=null && commandLine.length>=3){
				String arg0=commandLine[1];
				String arg1=commandLine[2];
				String arg2=commandLine[3];

				//run for the black border analysis
				if(arg0.equalsIgnoreCase("bb")){
					if(commandLine.length==2 && arg1.equalsIgnoreCase("test")){
						runBBAnalysis();
					}else{
						int row=Integer.parseInt(arg1);
						int col=Integer.parseInt(arg2);
						String direction="H"; //h= horizontal v=vertical
						if(paramsAction.size()==4)
							direction=arg2;
						BlackBorderAnalysis borderAn=new BlackBorderAnalysis(sar,layer.getRealTileSizeX(),layer.getRealTileSizeY(),null);
						borderAn.analyse(row,col,direction.equalsIgnoreCase("H"));

						yy=borderAn.getSizeY()*row;
						xx=borderAn.getSizeX()*col;
						tileSizeY=borderAn.getSizeY();
						tileSizeX=borderAn.getSizeX();
					}
			//run vds analysis on a single tile
				}else if(arg0.equalsIgnoreCase("vds")){

					int row=Integer.parseInt(arg1);//args[1]);
					int col=Integer.parseInt(arg2);

					Float hh=1.5f;
					Float hv=1.5f;
					Float vh=1.5f;
					Float vv=1.5f;

					Float buffer=0.0f;

					if(commandLine.length>=5){
						buffer=Float.parseFloat(commandLine[4]);
						hh=Float.parseFloat(commandLine[5]);
						hv=Float.parseFloat(commandLine[6]);
						vh=Float.parseFloat(commandLine[7]);
						vv=Float.parseFloat(commandLine[8]);
					}


					MaskVectorLayer coastlineMask = null;
				    MaskVectorLayer iceMasks = null;
					//read the land mask
	                for (ILayer l : LayerManager.getIstanceManager().getChilds(layer)) {
	                    if (l instanceof IMask ) {
	                    	if( ((MaskVectorLayer) l).getMaskType()==MaskVectorLayer.COASTLINE_MASK){
	                    			coastlineMask=(MaskVectorLayer) l;
	                    	}else if( ((MaskVectorLayer) l).getMaskType()==MaskVectorLayer.ICE_MASK){
	                    			iceMasks=(MaskVectorLayer) l;
	                    	}
	                    }
	                }

	                MaskGeometries mg=null;
					IMask bufferedMask=null;
	                if(coastlineMask!=null){
	                	bufferedMask=FactoryLayer.createMaskLayer(coastlineMask.getName(),
	                		coastlineMask.getType(),0,((MaskVectorLayer)coastlineMask).getGeometriclayer(),
	           				coastlineMask.getMaskType());
	                	mg=new MaskGeometries("coast", bufferedMask.getGeometries());
	                }
	                MaskGeometries ice=null;
	                IMask iceMask=null;
	                if(iceMasks!=null){
	                	 iceMask=FactoryLayer.createMaskLayer(iceMasks.getName(),
	                		iceMasks.getType(),0,((MaskVectorLayer)iceMasks).getGeometriclayer(),
	           				iceMasks.getMaskType());

		                 ice=new MaskGeometries("ice", iceMask.getGeometries());
	                }

	                VDSAnalysis analysis = new VDSAnalysis(sar, mg,ice, Float.parseFloat(sar.getENL()), new Float[]{hh,hv,vh,vv},
	                		layer.getRealTileSizeX(),layer.getRealTileSizeY(),
	                		layer.getHorizontalTilesImage(),layer.getVerticalTilesImage());

					analysis.setAnalyseSingleTile(true);
					analysis.setxTileToAnalyze(col);
					analysis.setyTileToAnalyze(row);
					proc=new AnalysisProcess(sar,Float.parseFloat(sar.getENL()), analysis,0,0,null);
					proc.addProcessListener(this);

					Thread t=new Thread(proc);
	                t.setName("VDS_analysis_"+sar.getDisplayName(0));
	                t.start();

	                yy=layer.getRealTileSizeY()*row;
					xx=layer.getRealTileSizeX()*col;
					tileSizeY=layer.getRealTileSizeY();
					tileSizeX=layer.getRealTileSizeX();
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
