/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core;

import java.util.Date;
import java.util.Vector;

import org.geoimage.viewer.core.api.ITime;

/**
 *
 * @author thoorfr
 */
public class TimeComponent {

    
    static private Vector<ITime> timeLayers = new Vector<ITime>();
    private static boolean dirty;
    
     public static Vector<ITime> getTimeLayers() {
        return timeLayers;
    }

    public static void setTimeLayers(Vector<ITime> aTimeLayers) {
        timeLayers = aTimeLayers;
    }
    
    public static void setDirty(boolean value){
        dirty=value;
    }
    
    public static boolean isDirty(){
        return dirty;
    }
    
    public static Date getMinimumDate(){
        Date out=null;
        for(ITime l:timeLayers){
            if(out==null){
                out=l.getDates()[0];
            }
            else{
                Date[] temp=l.getDates();
                if(out.after(temp[0])){
                    out=temp[0];
                }
            }
        }
        return out;
    }
    
    public static Date getMaximumDate(){
        Date out=null;
        for(ITime l:timeLayers){
            if(out==null){
                out=l.getDates()[1];
            }
            else{
                Date[] temp=l.getDates();
                if(out.before(temp[1])){
                    out=temp[1];
                }
            }
        }
        return out;
    }
    
    public static void setDates(Date[] dates){
        for(ITime l:timeLayers){
           l.setMinimumDate(dates[0]);
           l.setMaximumDate(dates[1]);
        }
    }
    
    
    
    
}
