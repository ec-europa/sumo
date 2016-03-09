package org.geoimage.viewer.core.api.iactions;

import java.util.List;

import javax.swing.Action;

import org.geoimage.viewer.widget.dialog.ActionDialog;

public interface ISumoAction extends Action{

		public String getName();
	    public String getDescription();
	    /**
	     * Gets the path to access the action from the menubar
	     * Should be of the form "Tools|Action|myAction|"
	     * @return
	     */
	    public String getPath();
	    public boolean execute();
	    public boolean isDone();
	    public List<ActionDialog.Argument> getArgumentTypes();
}
