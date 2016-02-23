/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.actions;

import org.geoimage.viewer.core.api.iactions.IConsoleAction;

/**
 *
 * @author leforth
 */
public abstract class AbstractConsoleAction extends SumoAbstractAction implements IConsoleAction {

   public AbstractConsoleAction(String name) {
		super(name,"");
	}

@Override
   public abstract String getCommand();
   //TODO: auto-update pluginspublic abstract long getLastUpdateTimeStamp(); 
}
