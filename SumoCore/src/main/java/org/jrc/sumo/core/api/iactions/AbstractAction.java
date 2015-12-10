/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jrc.sumo.core.api.iactions;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author leforth
 */
public abstract class AbstractAction implements IAction {


    public void errorWindow(String message)
    {
        final String errorMessage = message;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

}
