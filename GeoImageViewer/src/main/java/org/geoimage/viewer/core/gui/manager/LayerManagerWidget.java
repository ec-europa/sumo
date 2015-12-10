/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.gui.manager;

import java.util.Collection;
import java.util.List;

import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.layout.FormAttachment;
import org.fenggui.layout.FormData;
import org.fenggui.layout.RowLayout;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.widget.LayerWidget;
import org.jrc.sumo.core.api.layer.ILayer;


/**
 *
 * @author thoorfr
 */
public class LayerManagerWidget {

    private static LayerManagerWidget instance;
    private static Container wContainer=null;
    
    
    private LayerManagerWidget(){}
    
    public static LayerManagerWidget getManagerInstance(Display display){
    	if(instance==null){
    		instance=new LayerManagerWidget();
    		
    		wContainer=new Container();
	        
    		FormData fd = new FormData();
	        fd.left = new FormAttachment(0, 0);
	        fd.bottom = new FormAttachment(0, 0);
	        
	        wContainer.setLayoutData(fd);
	        
	        wContainer.pack();
	        wContainer.layout();
    	}
    	return instance;
    	
    }
    
    public void removeAllWidgets(){
    	 wContainer.removeAllWidgets();
    }
    
    public Container getWidget(){
    	return wContainer;
    }
    
    public void pack(){
    	wContainer.pack();
    	wContainer.layout();
    }
      
    public void buildWidget() {
    	wContainer.setLayoutManager(new RowLayout(true));
    	
    	LayerWidget console=new LayerWidget(SumoPlatform.getApplication().getLayerManager().getConsoleLayer());
    	//console.setMinSize(20,20);
    	
    	
    	wContainer.addWidget(console);
    	
    	Container parent=new LayerWidget(SumoPlatform.getApplication().getLayerManager().getBaseLayer());
    	wContainer.addWidget(parent,0);
    	parent.setLayoutManager(new RowLayout(false));

    	Collection<ILayer> ll=SumoPlatform.getApplication().getLayerManager().getLayers().keySet();
    	
        for (final ILayer l : ll) {
      			LayerWidget lw=new LayerWidget(l);
      			lw.setLayoutManager(new RowLayout(false));
      			parent.addWidget(lw);
      			
      			List<ILayer>childs= SumoPlatform.getApplication().getLayerManager().getLayers().get(l);
      			for(ILayer c:childs){
      				LayerWidget cw=new LayerWidget(c);
      				lw.addWidget(cw);
      			}	
      		}	
    }
    
    
    public static void updateLayout() {
    	if(instance!=null){
    		instance.removeAllWidgets();
    		instance.buildWidget();
    		instance.pack();
    	}
    }
}
