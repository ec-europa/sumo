/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.gui.manager;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geoimage.viewer.widget.TransparentWidget;

/**
 *
 * @author thoorfr
 */
public class WidgetManager {
    private static WidgetManager instance;
    static HashMap<String, Class<? extends TransparentWidget>> widgets=null;
	private static Logger logger=LogManager.getLogger();

    
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
        	logger.error(ex.getMessage(),ex);
        }
        return out;
    }
    
    public String[] getWidgetNames(){
        return widgets.keySet().toArray(new String[]{});
    }

}
