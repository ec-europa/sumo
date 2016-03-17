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
import org.geoimage.viewer.actions.SumoActionEvent;
import org.geoimage.viewer.core.GeometryImage;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.analysisproc.AnalysisProcess;
import org.geoimage.viewer.core.analysisproc.VDSAnalysisProcessListener;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.api.ilayer.IMask;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.GenericLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.ComplexEditGeometryVectorLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.SimpleGeometryLayer;
import org.geoimage.viewer.util.JTSUtil;
import org.geoimage.viewer.widget.dialog.ActionDialog.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
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
    	super("chktile","none");
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
        	 GeometryImage bbanal=new GeometryImage("BBAnalysis",GeometryImage.POINT,cc);
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

						yy=layer.getRealTileSizeY()*row;
						xx=layer.getRealTileSizeX()*col;
						tileSizeY=layer.getRealTileSizeY();
						tileSizeX=layer.getRealTileSizeY();

						String direction="H"; //h= horizontal v=vertical
						if(commandLine.length==5)
							direction=commandLine[4];

						BlackBorderAnalysis borderAn=new BlackBorderAnalysis(sar,layer.getRealTileSizeX(),layer.getRealTileSizeY(),null);
						int[] threshs=borderAn.analyse(row,col,direction.equalsIgnoreCase("H"));

						List<Geometry> points=new ArrayList<>();
						GeometryFactory gf=new GeometryFactory();
						for(int i=0;i<threshs.length;i++){
							int offset=threshs[i];
							if(direction.equalsIgnoreCase("H")){
								if(xx<(sar.getWidth()/2)){
									Point p=gf.createPoint(new Coordinate(xx+offset,yy+i));
									points.add(p);
								}
							}else{

							}
						}
						GeometryImage giPoint=new GeometryImage(arg2, points);
						SimpleGeometryLayer offset=new SimpleGeometryLayer(layer,"bb test"+row+" "+col,giPoint);
						offset.setColor(Color.ORANGE);
						offset.setWidth(2.0f);
						LayerManager.addLayerInThread(offset);

						com.vividsolutions.jts.geom.Polygon box=JTSUtil.createPolygon(xx,yy, layer.getRealTileSizeX(), layer.getRealTileSizeY());
						List<Geometry> geoms=new ArrayList<>();
						geoms.add(box);
						GeometryImage gi=new GeometryImage(arg2, geoms);

						SimpleGeometryLayer sgl=new SimpleGeometryLayer(layer,"bb test"+row+" "+col,gi);
						sgl.setColor(Color.ORANGE);
						sgl.setWidth(2.0f);
						LayerManager.addLayerInThread(sgl);

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

			com.vividsolutions.jts.geom.Polygon box=JTSUtil.createPolygon(xx,yy, tileSizeX,tileSizeY);

			List <Geometry>gg=new ArrayList<>();
			gg.add(box);
			clayer.addGeometries("Tile", Color.YELLOW,2,GeometryImage.POLYGON, gg, true);
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
