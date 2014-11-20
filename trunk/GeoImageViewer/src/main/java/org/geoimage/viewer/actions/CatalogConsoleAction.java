/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.actions;

import java.util.List;

import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.IConsoleAction;

/**
 *
 * @author thoorfr
 */
public class CatalogConsoleAction implements IConsoleAction{

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
