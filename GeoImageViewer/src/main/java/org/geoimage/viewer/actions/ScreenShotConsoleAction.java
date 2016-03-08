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


    public boolean execute() {
       GeoImageViewerView.screenshot();
       return true;
    }

    public List<Argument> getArgumentTypes() {
        return null;
    }


	@Override
	public boolean isIndeterminate() {
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

}
