/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.actions;

/**
 * class to handle progressive tasks
 * @author thoorfr
 */
public interface SumoActionListener {
    public void stop(String actionName);
	public void updateProgress(String msg,int progress,int max);
	public void startAction(String message,int size);
    public void setMessageInfo(String string);

}
