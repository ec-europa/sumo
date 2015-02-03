/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.gui.manager;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoimage.viewer.widget.TransparentWidget;

/**
 *
 * @author thoorfr
 */
public class WidgetManager {
    private static WidgetManager instance;
    static HashMap<String, Class<? extends TransparentWidget>> widgets=null;
    
    
    private WidgetManager(){
    	widgets=new HashMap<String, Class<? extends TransparentWidget>>();
    }
    
    public static WidgetManager getWManagerInstance(){
    	if(instance==null){
    		instance=new WidgetManager();
    	}
    	return instance;
    	
    } 
    
    
    public void addWidget(String name, Class<? extends TransparentWidget> clasS){
        widgets.put(name, clasS);
    }
    
    public TransparentWidget createWidget(String name){
        TransparentWidget out=null;
        try {
            out = widgets.get(name).newInstance();
            out.setName(name);
        } catch (Exception ex) {
            Logger.getLogger(WidgetManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out;
    }
    
    public String[] getWidgetNames(){
        return widgets.keySet().toArray(new String[]{});
    }

}
