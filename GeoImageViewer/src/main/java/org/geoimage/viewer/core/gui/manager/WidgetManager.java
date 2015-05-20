/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.gui.manager;

import java.util.HashMap;

import org.geoimage.viewer.widget.TransparentWidget;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thoorfr
 */
public class WidgetManager {
    private static WidgetManager instance;
    static HashMap<String, Class<? extends TransparentWidget>> widgets=null;
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(WidgetManager.class);

    
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
