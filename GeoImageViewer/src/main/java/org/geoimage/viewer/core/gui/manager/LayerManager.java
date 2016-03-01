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

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.geoimage.opengl.OpenGLContext;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.TimeComponent;
import org.geoimage.viewer.core.api.IClickable;
import org.geoimage.viewer.core.api.IKeyPressed;
import org.geoimage.viewer.core.api.ILayerListener;
import org.geoimage.viewer.core.api.ILayerManager;
import org.geoimage.viewer.core.api.IMouseDrag;
import org.geoimage.viewer.core.api.IMouseMove;
import org.geoimage.viewer.core.api.ITime;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.layers.BaseLayer;
import org.geoimage.viewer.core.layers.ConsoleLayer;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.vectors.MaskVectorLayer;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thoorfr
 */
public class LayerManager implements ILayerManager, IClickable, IMouseMove, IMouseDrag, IKeyPressed,ILayerListener{

    //Each Listener list all layers
	private List<ILayerListener> listeners = new ArrayList<ILayerListener>();

	/**
	 * each Main Layer-> List of childs layers
	 */
	private HashMap<ILayer,List<ILayer>> layers = new HashMap<ILayer,List<ILayer>>();
	private  ConsoleLayer consoleLayer=null;
	private  BaseLayer baseLayer=null;
	private List<ILayer> remove = new ArrayList<ILayer>();
	private List<ILayer> added = new ArrayList<ILayer>();
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

    public void render(OpenGLContext context) {
        if (this.added.size() > 0) {
        	for(ILayer l:added){
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
            added.clear();
            try {
                SumoPlatform.getApplication().refresh();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        if (this.remove.size() > 0) {
        	for(ILayer l:remove){
        		//check if I removed a parent layer
        		if(l.getParent()==null){
        			List<ILayer> removed=layers.remove(l);

        			if(removed!=null)
        				for (ILayer r : removed) {
        					notifyLayerRemoved(r);
        				}
        			notifyLayerRemoved(l);
        		}else{
        			layers.get(l.getParent()).remove(l);
        			notifyLayerRemoved(l);
        		}
        	}
            remove.clear();
            try {
                SumoPlatform.getApplication().refresh();
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



    public void mouseClicked(Point imagePosition, int button,Object graphicContext) {
        for (ILayer l : getAllLayers()) {
            if (l.isActive()) {
                if (l instanceof IClickable) {
                    ((IClickable) l).mouseClicked(imagePosition, button, graphicContext);
                }
            }
        }
    }

    public void mouseMoved(Point imagePosition,Object graphicContext) {
        for (ILayer l : getAllLayers()) {
            if (l.isActive()) {
                if (l instanceof IMouseMove) {
                    ((IMouseMove) l).mouseMoved(imagePosition, graphicContext);
                }
            }
        }
    }

    /**
     *
     * @param layer
     * @param needActive
     */
    public void addLayer(ILayer layer,boolean needActive) {
        if (needActive && layer instanceof ImageLayer) {
            // look for other image layers active
            for (ILayer il : layers.keySet()) {
                if (il instanceof ImageLayer) {
                    if (il.isActive()) {
                        il.setActive(false);
                    }
                }
            }
        }
        layer.setActive(needActive);
        // now add layer
        this.added.add(layer);
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

    public void mouseDragged(Point initPosition, Point imagePosition, int button,Object graphicContext) {
        for (ILayer l : getAllLayers()) {
            if (l.isActive()) {
                if (l instanceof IMouseDrag) {
                    ((IMouseDrag) l).mouseDragged(initPosition, imagePosition, button, graphicContext);
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
  public MaskVectorLayer getChildMaskLayer(ILayer layer){
	  ILayer mask=getChildLayerByType(layer, MaskVectorLayer.class);
	  if(mask!=null)
		  return (MaskVectorLayer)mask;
	  return  null;
  }

  public ILayer getChildLayerByType(ILayer layer,Class type){
  	Collection<ILayer> ks=getChilds(layer);

  	for(ILayer k:ks){
  		if(k.getClass().isAssignableFrom(type))
  			return k;
  	}

  	return null;
  }


  public ImageLayer getCurrentImageLayer() {
  	if(!SumoPlatform.isBatchMode()){
	        for (ILayer l : getLayers().keySet()) {
	            if (l instanceof ImageLayer && l.isActive()) {
	                try {
	                    return (ImageLayer) l;
	                } catch (Exception ex) {
	                	logger.error(ex.getMessage(),ex);
	                }
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
		if(l instanceof ImageLayer){
			ImageLayer il=(ImageLayer)l;
			if(il!=null && il.getImageReader()!=null){
				il.getImageReader().dispose();
				il=null;
			}
		}else{
			l.dispose();
		}
	}


	@Override
	public void layerClicked(ILayer l) {
		if(l.getParent()==null){
			List<ILayer>childs=layers.get(l);
			if(l.isActive()){
				l.setActive(false);
				//disable other layer
				for(ILayer p:layers.keySet()){
					if(!p.equals(l)){
						p.setActive(false);

					}
				}
				if(childs!=null){
					for(ILayer c:childs){
						c.setActive(false);
					}
				}
			}else{
				l.setActive(true);
				if(childs!=null){
					for(ILayer c:childs){
						c.setActive(true);
					}
				}
			}
		}else{
			l.setActive(!l.isActive());
		}
	}

	 /**
     *
     * @param type
     * @param layer
     * @param il
     */
    public static boolean addLayerInThread(final ILayer layer) {
    	boolean done=false;
        if (layer != null) {
            new Thread(new Runnable() {
                public void run() {
                    SumoPlatform.getApplication().getLayerManager().addLayer((ILayer) layer);
                }
            }).start();
            done=true;
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(null, "Empty layer, not added to layers", "Warning", JOptionPane.ERROR_MESSAGE);
                }
            });
            done = true;
        }
        return done;
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


	@Override
	public void addLayer(ILayer layer) {
		addLayer(layer, true);

	}
}
