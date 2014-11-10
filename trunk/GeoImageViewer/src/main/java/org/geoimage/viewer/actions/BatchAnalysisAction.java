/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.actions;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.api.IConsoleAction;
import org.geoimage.viewer.offline.BatchAnalysisGUI;

/**
 *
 * @author thoorfr
 */
public class BatchAnalysisAction implements IConsoleAction{

    public String getName() {
        return "batch";
    }

    public String getDescription() {
        return "use \"catalog\" to access the catalog of datasources";
    }

    public String getPath() {
        return "Tools/Image/Batch Analysis";
    }

    public boolean execute(String[] args) {
        try {
            BatchAnalysisGUI.main(null);
        } catch (Exception ex) {
            Logger.getLogger(BatchAnalysisAction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    public List<Argument> getArgumentTypes() {
        return null;
    }

}
