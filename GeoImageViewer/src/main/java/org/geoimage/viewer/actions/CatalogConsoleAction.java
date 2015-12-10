/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.actions;

import java.util.List;

import org.jrc.sumo.core.api.Argument;
import org.jrc.sumo.core.api.iactions.IAction;

/**
 *
 * @author thoorfr
 */
public class CatalogConsoleAction implements IAction{

    public String getName() {
        return "catalog";
    }

    public String getDescription() {
        return "use \"catalog\" to access the catalog of datasources";
    }

    public String getPath() {
        return "Files/Catalog";
    }

    public boolean execute(String[] args) {
        return true;
    }

    public List<Argument> getArgumentTypes() {
        return null;
    }

}
