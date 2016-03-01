/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import org.geoimage.viewer.core.api.iactions.IAction;
import org.geoimage.viewer.widget.dialog.ActionDialog.Argument;

/**
 *
 * @author thoorfr
 */
public class CatalogConsoleAction extends AbstractAction implements IAction{

    public String getName() {
        return "catalog";
    }

    public String getDescription() {
        return "use \"catalog\" to access the catalog of datasources";
    }

    public String getPath() {
        return "Files/Catalog";
    }


    public List<Argument> getArgumentTypes() {
        return null;
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean execute() {
		return true;
	}

}
