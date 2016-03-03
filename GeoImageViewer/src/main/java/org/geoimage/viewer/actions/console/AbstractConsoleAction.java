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

@Override
   public abstract String getCommand();
   public abstract boolean executeFromConsole();


   public void setCommandLine(String[] commandLine){
	   this.commandLine=commandLine;
   }
}
