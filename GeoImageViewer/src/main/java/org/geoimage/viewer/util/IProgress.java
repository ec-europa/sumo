/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.util;

/**
 * class to handle progressive tasks
 * @author thoorfr
 */
public interface IProgress {
    public boolean isIndeterminate();
    public boolean isDone();
    public int getMaximum();
    public int getCurrent();
    public String getMessage();
    public void setCurrent(int i);
    public void setMaximum(int size);
    public void setMessage(String string);
    public void setIndeterminate(boolean value);
    public void setDone(boolean value);
}
