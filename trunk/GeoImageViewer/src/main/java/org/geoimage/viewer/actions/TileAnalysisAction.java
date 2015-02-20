package org.geoimage.viewer.actions;

import java.util.ArrayList;
import java.util.List;

import org.geoimage.analysis.BlackBorderAnalysis;
import org.geoimage.def.GeoImageReader;
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

	@Override
	public boolean execute(String[] args) {
		try {
			if(Platform.getCurrentImageLayer()!=null && args.length>=2){
				int row=Integer.parseInt(args[0]);
				int col=Integer.parseInt(args[1]);
				String direction="H"; //h= horizontal v=vertical
				if(args.length==3)
					direction=args[2];
				
				
				
				BlackBorderAnalysis borderAn=new BlackBorderAnalysis((GeoImageReader)Platform.getCurrentImageLayer().getImageReader(),0);
				borderAn.analyse(row,col,direction.equalsIgnoreCase("H"));
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
