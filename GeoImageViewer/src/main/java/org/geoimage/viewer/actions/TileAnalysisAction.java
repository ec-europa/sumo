package org.geoimage.viewer.actions;

import java.util.ArrayList;
import java.util.List;

import org.geoimage.analysis.BlackBorderAnalysis;
import org.geoimage.analysis.VDSAnalysis;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.impl.s1.Sentinel1;
import org.geoimage.utils.IMask;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.analysisproc.AnalysisProcess;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.iactions.AbstractAction;
import org.geoimage.viewer.core.configuration.PlatformConfiguration;
import org.geoimage.viewer.core.factory.FactoryLayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.GeometricLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;

public class TileAnalysisAction extends AbstractAction{
	private Logger logger = LoggerFactory.getLogger(TileAnalysisAction.class);
	boolean done=false;
	
	
	@Override
	public String getName() {
		return "chktile";
	}

	@Override
	public String getDescription() {
		return "Analyze Tile Values";
	}

	@Override
	public String getPath() {
		return "Tools/CheckTile";
	}

	private void runBBAnalysis(){
		//run the blackborder analysis for the s1 images
		BlackBorderAnalysis blackBorderAnalysis=null;
		GeoImageReader gir=Platform.getCurrentImageReader();
        if(gir instanceof Sentinel1){
                /*MaskVectorLayer mv=null;
           	 	if(bufferedMask!=null&&bufferedMask.length>0)
           	 		mv=(MaskVectorLayer)bufferedMask[0];
           	 	if(mv!=null)
           	 		blackBorderAnalysis= new BlackBorderAnalysis(gir,mv.getGeometries());
           	 	else*/ 
           	blackBorderAnalysis= new BlackBorderAnalysis(gir,null);
         } 	
         if(blackBorderAnalysis!=null){
        	 int nTile=Platform.getConfiguration().getNumTileBBAnalysis();
        	 blackBorderAnalysis.analyse(nTile);
        	 List<Coordinate>cc=blackBorderAnalysis.getCoordinatesThresholds();
        	 GeometricLayer bbanal=new GeometricLayer("BBAnalysis",
        			 GeometricLayer.POINT,
        			 cc
        			 );
        	 LayerManager.addLayerInThread(
        			 GeometricLayer.POINT, 
        			 bbanal, 
        			 Platform.getCurrentImageLayer());
        	 
         }
         //end blackborder analysis
	}
	
	
	@Override
	public boolean execute(String[] args) {
		try {
			if(Platform.getCurrentImageLayer()!=null && args.length>=2){
				if(args[0].equalsIgnoreCase("bb")){
					if(args.length==2 && args[1].equalsIgnoreCase("test")){
						runBBAnalysis();
					}else{
						int row=Integer.parseInt(args[1]);
						int col=Integer.parseInt(args[2]);
						String direction="H"; //h= horizontal v=vertical
						if(args.length==4)
							direction=args[2];
						BlackBorderAnalysis borderAn=new BlackBorderAnalysis((GeoImageReader)Platform.getCurrentImageReader(),0,null);
						borderAn.analyse(row,col,direction.equalsIgnoreCase("H"));
					}	
				}else if(args[0].equalsIgnoreCase("vds")){
					SarImageReader sar=(SarImageReader) Platform.getCurrentImageReader();
					int row=Integer.parseInt(args[1]);
					int col=Integer.parseInt(args[2]);
					Float hh=Float.parseFloat(args[3]);
					Float hv=Float.parseFloat(args[4]);
					Float vh=Float.parseFloat(args[5]);
					Float vv=Float.parseFloat(args[6]);
					
					//read the land mask
					ArrayList<IMask> mask = new ArrayList<IMask>();
	                for (ILayer l : Platform.getLayerManager().getChilds(Platform.getCurrentImageLayer())) {
	                    if (l instanceof IMask ) {
	                        mask.add((IMask) l);
	                    }
	                }
					// create new buffered mask with bufferingDistance using the mask in parameters
	                final IMask[] bufferedMask = new IMask[mask.size()];
	                for (int i=0;i<mask.size();i++) {
	                	IMask maskList = mask.get(i);
	               		bufferedMask[i]=FactoryLayer.createMaskLayer(maskList.getName(), maskList.getType(), 0, ((MaskVectorLayer)maskList).getGeometriclayer());
	                }
					
					final VDSAnalysis analysis = new VDSAnalysis(sar, null, Float.parseFloat(sar.getENL()), new Float[]{hh,hv,vh,vv});
					analysis.setAnalyseSingleTile(true);
					analysis.setxTileToAnalyze(col);
					analysis.setyTileToAnalyze(row);
					AnalysisProcess proc=new AnalysisProcess(sar,Float.parseFloat(sar.getENL()), analysis, bufferedMask,0,0);
					proc.run();
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public List<Argument> getArgumentTypes() {
		List <Argument> args=new  ArrayList<Argument>();
		return args;
	}

	

}
