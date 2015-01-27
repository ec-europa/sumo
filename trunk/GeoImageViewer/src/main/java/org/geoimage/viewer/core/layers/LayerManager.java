/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.TimeComponent;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.IClickable;
import org.geoimage.viewer.core.api.IKeyPressed;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.ILayerListener;
import org.geoimage.viewer.core.api.ILayerManager;
import org.geoimage.viewer.core.api.IListenerUser;
import org.geoimage.viewer.core.api.IMouseDrag;
import org.geoimage.viewer.core.api.IMouseMove;
import org.geoimage.viewer.core.api.ITime;

/**
 *
 * @author thoorfr
 */
public class LayerManager implements ILayerManager, IClickable, IMouseMove, IMouseDrag, IKeyPressed, IListenerUser {

    
    List<ILayerListener> listeners = new ArrayList<ILayerListener>();
    protected List<ILayer> layers = new ArrayList<ILayer>();
  
    protected List<ILayer> remove = new ArrayList<ILayer>();
    protected List<ILayer> add = new ArrayList<ILayer>();
  

    public LayerManager() {
    }

    
    public void render(final GeoContext context) {
        if (this.add.size() > 0) {
            layers.addAll(add);
            for (ILayer l : add) {
                for (ILayerListener ll : listeners) {
                    ll.layerAdded(l);
                }
            }
            add.clear();
            try {
                Platform.refresh();
            } catch (Exception ex) {
                Logger.getLogger(LayerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (this.remove.size() > 0) {
            layers.removeAll(remove);
            for (ILayer l : remove) {
                for (ILayerListener ll : listeners) {
                    ll.layerRemoved(l);
                }
            }
            remove.clear();
            try {
                Platform.refresh();
            } catch (Exception ex) {
                Logger.getLogger(LayerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        for (ILayer l : layers) {
            if (l.isActive()) {
                try {
                    l.render(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
    
    public void setActive(ILayer layer) {
        for (ILayer l : layers) {
            if (l == layer) {
                l.setActive(true);
            } else {
                l.setActive(false);
            }
        }
        for (ILayerListener l : listeners) {
            l.layerClicked(layer);
        }
    }

    

    public void setActiveRadioLayer(ILayer layer) {
        for (ILayer l : layers) {
            if (l == layer) {
                l.setActive(true);
            } else {
                l.setActive(false);
            }
        }
    }



    public void mouseClicked(Point imagePosition, int button, GeoContext context) {
        for (ILayer l : layers) {
            if (l.isActive()) {
                if (l instanceof IClickable) {
                    ((IClickable) l).mouseClicked(imagePosition, button, context);
                }
            }
        }
    }

    public void dispose() {
        for (ILayer l : layers) {
        	l.setActive(false);
            l.dispose();
        }
        layers.clear();
    }

    public void mouseMoved(Point imagePosition, GeoContext context) {
        for (ILayer l : layers) {
            if (l.isActive()) {
                if (l instanceof IMouseMove) {
                    ((IMouseMove) l).mouseMoved(imagePosition, context);
                }
            }
        }
    }

    public void addLayer(ILayer layer) {
        // if we are adding an image layer turn off all the other image active layers
        if (layer instanceof FastImageLayer) {
            // look for other image layers active
            for (ILayer il : layers) {
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

    public void removeLayer(ILayer layer) {
        layer.setActive(false);
        this.remove.add(layer);
        if (layer instanceof ITime) {
            TimeComponent.getTimeLayers().remove((ITime) layer);
            TimeComponent.setDirty(true);
        }
    }

    public List<ILayer> getLayers() {
        return new ArrayList(layers);
    }

    public void mouseDragged(Point initPosition, Point imagePosition, int button, GeoContext context) {
        for (ILayer l : layers) {
            if (l.isActive()) {
                if (l instanceof IMouseDrag) {
                    ((IMouseDrag) l).mouseDragged(initPosition, imagePosition, button, context);
                }
            }
        }
    }

    public void keyPressed(KeyEvent evt) {
        for (ILayer l : layers) {
            if (l.isActive()) {
                if (l instanceof IKeyPressed) {
                    ((IKeyPressed) l).keyPressed(evt);
                }
            }
        }
    }

    public void addListenner(ILayerListener l) {
        this.listeners.add(l);
        for (ILayer ll : layers) {
            if (ll instanceof IListenerUser) {
                ((IListenerUser) ll).addListenner(l);
            }
        }
    }

    public void removeListenner(ILayerListener l) {
        this.listeners.remove(l);
        for (ILayer ll : layers) {
            if (ll instanceof IListenerUser) {
                ((IListenerUser) ll).removeListenner(l);
            }
        }
    }
}
