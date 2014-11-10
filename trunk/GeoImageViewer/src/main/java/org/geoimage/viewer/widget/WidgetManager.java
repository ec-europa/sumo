/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.widget;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thoorfr
 */
public class WidgetManager {
    
    static HashMap<String, Class<? extends TransparentWidget>> widgets=new HashMap<String, Class<? extends TransparentWidget>>();
    
    public static void addWidget(String name, Class<? extends TransparentWidget> clasS){
        widgets.put(name, clasS);
    }
    
    public static TransparentWidget createWidget(String name){
        TransparentWidget out=null;
        try {
            out = widgets.get(name).newInstance();
            out.setName(name);
        } catch (Exception ex) {
            Logger.getLogger(WidgetManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out;
    }
    
    public static String[] getWidgetNames(){
        return widgets.keySet().toArray(new String[]{});
    }

}
