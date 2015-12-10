/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jrc.sumo.core.api;

import java.util.Date;

/**
 *
 * @author thoorfr
 */
public interface ITime {

    public Date getMaximumDate();

    public Date getMinimumDate();

    public String getTimeColumn();

    public void setMaximumDate(Date maximumDate);

    public void setMinimumDate(Date minimumDate);

    public void setTimeColumn(String timeColumn);
    
    public Date[] getDates();

}
