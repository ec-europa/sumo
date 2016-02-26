/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.actions;

import java.util.List;

import org.geoimage.viewer.core.GeoImageViewerView;
import org.geoimage.viewer.widget.dialog.ActionDialog.Argument;

/**
 *
 * @author thoorfr+AG
 *
 * this class permits to save on file a screenshot of the main window.
 * It works with the GeoImageViewerView class changing the value of the onScreenshot Boolean variable, used by the method display().
 *
 */
public class ScreenShotConsoleAction extends SumoAbstractAction{

	public ScreenShotConsoleAction(){
		super("Screenshot","Tools/Screenshot");
	}
	

    public String getDescription() {
        return "Save a screenshot of your current view";
    }


    public boolean execute(String[] args) {
       GeoImageViewerView.screenshot();
       return true;
    }

    public List<Argument> getArgumentTypes() {
        return null;
    }

}
