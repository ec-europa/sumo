package org.geoimage.viewer.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.geoimage.analysis.BlackBorderAnalysis;
import org.geoimage.analysis.BlackBorderAnalysis.TileAnalysis;
import org.geoimage.analysis.ConstantVDSAnalysis;
import org.geoimage.def.GeoImageReader;
import org.geoimage.impl.s1.Sentinel1;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.iactions.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        	 int numTileH=gir.getWidth()/ConstantVDSAnalysis.TILESIZEPIXELS;
        	 for(int j=0;j<nTile;j++){
        		 for(int i=0;i<numTileH;i++){
	        		 TileAnalysis ta=blackBorderAnalysis.getAnalysisTile(j,i);
	        		 if(ta!=null&&ta.verTopCutOffArray!=null){
	        			 String vals=StringUtils.join(ta.verTopCutOffArray,',');
	        			 System.out.println("Tile:"+i+"  -  vals:"+vals);
	        		 } 
        		 }	 
        	 }	 
         }
         //end blackborder analysis
	}
	
	
	@Override
	public boolean execute(String[] args) {
		try {
			if(Platform.getCurrentImageLayer()!=null && args.length>=2){
				int row=Integer.parseInt(args[0]);
				int col=Integer.parseInt(args[1]);
				String direction="H"; //h= horizontal v=vertical
				if(args.length==3)
					direction=args[2];
				BlackBorderAnalysis borderAn=new BlackBorderAnalysis((GeoImageReader)Platform.getCurrentImageLayer().getImageReader(),0,null);
				borderAn.analyse(row,col,direction.equalsIgnoreCase("H"));
			}else if(args.length==0){
				runBBAnalysis();
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
