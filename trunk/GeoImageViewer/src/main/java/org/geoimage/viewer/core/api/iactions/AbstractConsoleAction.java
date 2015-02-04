/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.api.iactions;


/**
 *
 * @author leforth
 */
public abstract class AbstractConsoleAction extends AbstractAction implements IConsoleAction {

   @Override
   public abstract String getCommand();

}
