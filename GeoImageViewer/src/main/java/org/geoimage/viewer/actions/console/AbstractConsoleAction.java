/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.actions.console;

import org.geoimage.viewer.actions.SumoAbstractAction;
import org.geoimage.viewer.core.api.iactions.IConsoleAction;

/**
 *
 * @author leforth
 */
public abstract class AbstractConsoleAction extends SumoAbstractAction implements IConsoleAction {
   protected  String[] commandLine=null;

   public AbstractConsoleAction(String name) {
		super(name,"");
	}

   public AbstractConsoleAction(String name,String actionPath) {
		super(name,actionPath);
	}

@Override
   public abstract String getCommand();
   public abstract boolean executeFromConsole();


   public void setCommandLine(String[] commandLine){
	   this.commandLine=commandLine;
   }


	@Override
	public boolean isIndeterminate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getMaximum() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCurrent() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setCurrent(int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMaximum(int size) {
		// TODO Auto-generated method stub

	}


	@Override
	public void setIndeterminate(boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDone(boolean value) {
		// TODO Auto-generated method stub

	}
}
