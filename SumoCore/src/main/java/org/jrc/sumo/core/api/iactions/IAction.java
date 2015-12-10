package org.jrc.sumo.core.api.iactions;

import java.util.List;

import org.jrc.sumo.core.api.Argument;

public interface IAction {
	 	
		public String getName();
	    public String getDescription();
	    /**
	     * Gets the path to access the action from the menubar
	     * Should be of the form "Tools|Action|myAction|"
	     * @return
	     */
	    public String getPath();
	    public boolean execute(String[] args);
	    public List<Argument> getArgumentTypes();
}
