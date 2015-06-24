/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.gui.manager;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.TimeComponent;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.IClickable;
import org.geoimage.viewer.core.api.IKeyPressed;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.ILayerListener;
import org.geoimage.viewer.core.api.ILayerManager;
import org.geoimage.viewer.core.api.IMouseDrag;
import org.geoimage.viewer.core.api.IMouseMove;
import org.geoimage.viewer.core.api.ITime;
import org.geoimage.viewer.core.layers.BaseLayer;
import org.geoimage.viewer.core.layers.ConsoleLayer;
import org.geoimage.viewer.core.layers.FastImageLayer;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thoorfr
 */
public class LayerManager implements ILayerManager, IClickable, IMouseMove, IMouseDrag, IKeyPressed,ILayerListener{

    //Each Listener list all layers
	protected List<ILayerListener> listeners = new ArrayList<ILayerListener>();	    
	
	/**
	 * each Main Layer-> List of childs layers
	 */
	protected HashMap<ILayer,List<ILayer>> layers = new HashMap<ILayer,List<ILayer>>();
	private  ConsoleLayer consoleLayer=null;
	private  BaseLayer baseLayer=null;
    protected List<ILayer> remove = new ArrayList<ILayer>();
    protected List<ILayer> add = new ArrayList<ILayer>();
    private static LayerManager manager=null;
    
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(LayerManager.class);
    

    private LayerManager() {
    	addListener(this);
    }
    
    
    public static LayerManager getIstanceManager(){
    	if(manager==null)
    		manager=new LayerManager();
    	return manager;
    }

    public void render(final GeoContext context) {
        if (this.add.size() > 0) {
        	for(ILayer l:add){
        		if(l.getParent()==null){
        			List<ILayer> childs=layers.get(l);
        			if(childs==null){
        				layers.put(l,new ArrayList<ILayer>());
        			}
        		}else{
        			List<ILayer> childs=layers.get(l.getParent());
        			if(childs==null){
        				childs=new ArrayList<ILayer>();
        				childs.add(l);
        				layers.put(l.getParent(),childs);
        			}else{
        				childs.add(l);
        			}
        		}
        		notifyLayerAdded(l);
        	}
            add.clear();
            try {
                Platform.refresh();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        if (this.remove.size() > 0) {
        	for(ILayer l:remove){
        		if(l.getParent()==null){
        			List<ILayer> removed=layers.remove(l);
        			if(removed!=null)
        				for (ILayer r : removed) {
        					notifyLayerRemoved(r);
        				}
        		}else{
        			layers.get(l.getParent()).remove(l);
        			notifyLayerRemoved(l);
        		}
        	}	
            remove.clear();
            try {
                Platform.refresh();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        for (ILayer lkey : getAllLayers()){//layers.keySet()) {
            if (lkey.isActive()) {
                try {
                    lkey.render(context);
                    /*for (ILayer child : layers.get(lkey)) {
                    	child.render(context);
                    }*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        consoleLayer.render(context);
    }


    public void setActiveRadioLayer(ILayer layer) {
        for (ILayer l : layers.keySet()) {
            if (l == layer) {
                l.setActive(true);
            } else {
                l.setActive(false);
            }
        }
    }



    public void mouseClicked(Point imagePosition, int button, GeoContext context) {
        for (ILayer l : getAllLayers()) {
            if (l.isActive()) {
                if (l instanceof IClickable) {
                    ((IClickable) l).mouseClicked(imagePosition, button, context);
                }
            }
        }
    }

    public void mouseMoved(Point imagePosition, GeoContext context) {
        for (ILayer l : getAllLayers()) {
            if (l.isActive()) {
                if (l instanceof IMouseMove) {
                    ((IMouseMove) l).mouseMoved(imagePosition, context);
                }
            }
        }
    }

    /** 
     * add a layer	
     */
    public void addLayer(ILayer layer) {
        // if we are adding an image layer turn off all the other image active layers
        if (layer instanceof FastImageLayer) {
            // look for other image layers active
            for (ILayer il : layers.keySet()) {
                if (il instanceof FastImageLayer) {
                    if (il.isActive()) {
                        il.setActive(false);
                    }
                }
            }
        }
        // now add layer
        this.add.add(layer);
        if (layer instanceof ITime) {
            TimeComponent.getTimeLayers().add((ITime) layer);
            TimeComponent.setDirty(true);
        }
    }

    /**
     * 
     * @param layer
     * @param needActive
     */
    public void addLayer(ILayer layer,boolean needActive) {
        if (needActive && layer instanceof FastImageLayer) {
            // look for other image layers active
            for (ILayer il : layers.keySet()) {
                if (il instanceof FastImageLayer) {
                    if (il.isActive()) {
                        il.setActive(false);
                    }
                }
            }
        }
        layer.setActive(needActive);
        // now add layer
        this.add.add(layer);
        if (layer instanceof ITime) {
            TimeComponent.getTimeLayers().add((ITime) layer);
            TimeComponent.setDirty(true);
        }
    }
    
    
    public void removeLayer(ILayer layer) {
        layer.setActive(false);
        this.remove.add(layer);
        if (layer instanceof ITime) {
            TimeComponent.getTimeLayers().remove((ITime) layer);
            TimeComponent.setDirty(true);
        }

    }

    public HashMap<ILayer,List<ILayer>> getLayers() {
        return layers;
    }

    public void mouseDragged(Point initPosition, Point imagePosition, int button, GeoContext context) {
        for (ILayer l : getAllLayers()) {
            if (l.isActive()) {
                if (l instanceof IMouseDrag) {
                    ((IMouseDrag) l).mouseDragged(initPosition, imagePosition, button, context);
                }
            }
        }
    }

    public void keyPressed(KeyEvent evt) {
        for (ILayer l : getAllLayers()) {
            if (l.isActive()) {
                if (l instanceof IKeyPressed) {
                    ((IKeyPressed) l).keyPressed(evt);
                }
            }
        }
    }

    public void addListener(ILayerListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(ILayerListener l) {
        this.listeners.remove(l);
    }

    public void notifyLayerAdded(ILayer layer){
    	for(ILayerListener l:listeners)
    		l.layerAdded(layer);
    }
    public void notifyLayerClicked(ILayer layer){
    	for(ILayerListener l:listeners)
    		l.layerClicked(layer);
    }
    public void notifyLayerRemoved(ILayer layer){
    	for(ILayerListener l:listeners)
    		l.layerRemoved(layer);
    }

    
    public List<ILayer> getAllLayers(){
    	
    	List<ILayer> all=new ArrayList<ILayer>();
    	Collection<ILayer> ks=layers.keySet();
    	for(ILayer k:ks){
    		all.add(k);
    		all.addAll(layers.get(k));
    	}
    	
    	return all;		
    }
    
  public ILayer getLayerByName(String name){
    	Collection<ILayer> ks=layers.keySet();
    	for(ILayer k:ks){
    		if(k.getName().equals(name))
    			return k;
    		else{
    			Collection<ILayer> childs=layers.get(k);
    			for(ILayer c:childs){
    				if(c.getName().equals(name))
    	    			return c;
    			}
    		}
    	}
    	
    	return null;		
    }
    
    
    /**
     * return the list of childs layers for a layer
     * @param l
     * @return
     */
    public List<ILayer> getChilds(ILayer l){
    	return layers.get(l);		
    }


	@Override
	public void layerAdded(ILayer l) {
		
	}


	@Override
	public void layerRemoved(ILayer l) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void layerClicked(ILayer l) {
		if(l.getParent()==null){
			List<ILayer>childs=layers.get(l);
			if(l.isActive()){
				//disable other layer
				for(ILayer p:layers.keySet()){
					if(!p.equals(l)){
						p.setActive(false);
						
					}	
				}
				if(childs!=null){
					for(ILayer c:childs){
						c.setActive(true);
					}
				}	
			}else{
				if(childs!=null){
					for(ILayer c:childs){
						c.setActive(false);
					}
				}
			}	
		}
	}

	
	public ConsoleLayer getConsoleLayer() {
		return consoleLayer;
	}


	public void setConsoleLayer(ConsoleLayer consoleLayer) {
		this.consoleLayer = consoleLayer;
	}


	public BaseLayer getBaseLayer() {
		return baseLayer;
	}


	public void setBaseLayer(BaseLayer baseLayer) {
		this.baseLayer = baseLayer;
	}
}
