package org.geoimage.viewer.actions;

import java.util.ArrayList;
import java.util.List;

import org.geoimage.utils.IProgress;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.iactions.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TileAnalysisAction extends AbstractAction implements IProgress{
	private Logger logger = LoggerFactory.getLogger(TileAnalysisAction.class);
	boolean done=false;
	
	
	@Override
	public String getName() {
		return "Analyze Tile Values";
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
			
		
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
		setDone(true);
		return true;
	}

	@Override
	public List<Argument> getArgumentTypes() {
		List <Argument> args=new  ArrayList<Argument>();
		return args;
	}

	@Override
	public boolean isIndeterminate() {
		return true;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public int getMaximum() {
		return 0;
	}

	@Override
	public int getCurrent() {
		return 0;
	}

	@Override
	public String getMessage() {
		return "Clear the sumo cache?";
	}

	@Override
	public void setCurrent(int i) {
	}

	@Override
	public void setMaximum(int size) {
	}

	@Override
	public void setMessage(String string) {
	}

	@Override
	public void setIndeterminate(boolean value) {
	}

	@Override
	public void setDone(boolean value) {
		done=value;
		
	}

}
