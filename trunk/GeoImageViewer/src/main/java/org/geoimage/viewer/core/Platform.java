/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.awt.GLCanvas;

import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.ConsoleLayer;
import org.geoimage.viewer.core.layers.image.CacheManager;
import org.geoimage.viewer.util.Constant;
import org.geoimage.viewer.widget.TransparentWidget;

/**
 *
 * @author thoorfr
 */
public class Platform {

    private static boolean batchMode=false;
    /*private static Thread currentThreadRunning=null;
    
    
    public Thread getCurrentThreadRunning() {
		return currentThreadRunning;
	}

	public static void setCurrentThreadRunning(Thread currentThread) {
		currentThreadRunning = currentThread;
	}

	public static void stopCurrentThread(){
		if(currentThreadRunning!=null&&currentThreadRunning.isAlive()){
			try{
				currentThreadRunning.interrupt();
			}catch(Exception ex){
				ex.printStackTrace();
			}	
		}
	}*/
	
	/**
     * 
     * @return true if sumo is running in batch mode
     */
    public static boolean isBatchMode() {
		return batchMode;
	}
    
    /**
     * set batch mode = true 
     */
	public static void setInBatchMode() {
		batchMode = true;
	}
	
	
	/**
	 * default
	 */
	public static void setInteractiveMode() {
		batchMode = false;
	}

	/*
	
	public static IProgress getProgressBar(){
        return new IProgress() {
            private boolean indeterminate=true;
            private boolean done=false;
            private String message;
            private int max=-1, cur=0;
            public boolean isIndeterminate() {
                return indeterminate;
            }

            public boolean isDone() {
                return done;
            }

            public int getMaximum() {
                return max;
            }

            public int getCurrent() {
                return cur;
            }

            public String getMessage() {
                return message;
            }

            public void setCurrent(int i) {
                cur=i;
                ((GeoImageViewerView) GeoImageViewer.getApplication().getMainView()).setProgressValue(i);
            }

            public void setMaximum(int size) {
                max=size;
                ((GeoImageViewerView) GeoImageViewer.getApplication().getMainView()).setProgressMax(size);
            }

            public void setMessage(String string) {
                message=string;
                ((GeoImageViewerView) GeoImageViewer.getApplication().getMainView()).setInfo(string);
            }

            public void setIndeterminate(boolean value) {
                indeterminate=value;
                ((GeoImageViewerView) GeoImageViewer.getApplication().getMainView()).setProgressMax(value?-1:0);
            }

            public void setDone(boolean value) {
                this.done=value;
            }
        };
    }*/

    public static ConsoleLayer getConsoleLayer() {
        return ((GeoImageViewerView) GeoImageViewer.getApplication().getMainView()).getConsole();
    }

    public static GeoContext getGeoContext() {
        return ((GeoImageViewerView) GeoImageViewer.getApplication().getMainView()).getGeoContext();
    }

    public static LayerManager getLayerManager() {
        return ((GeoImageViewerView) GeoImageViewer.getApplication().getMainView()).getLayerManager();
    }

    public static GLCanvas getMainCanvas() {
        return ((GeoImageViewerView) GeoImageViewer.getApplication().getMainView()).getMainCanvas();
    }

    public static void refresh() {
        ((GeoImageViewerView) GeoImageViewer.getApplication().getMainView()).refresh();
    }
    private static int maxPBar = 0;

    public static void setInfo(String info) {
        setInfo(info, 10000);
    }

    public static void setInfo(String info, long timeout) {
        //AG progress bar management
        int progress = 0;//AG
        GeoImageViewerView mainView=((GeoImageViewerView) GeoImageViewer.getApplication().getMainView());
        if (info == null || "".equals(info)) {
        	mainView.setInfo("");
            maxPBar = 0;//AG
            mainView.setProgressValue(0);//AG
            mainView.setProgressMax(0);//AG
            mainView.iconTimer(false);//AG
            return;
        }
        if (timeout==-1) {//AG
        	mainView.setProgressMax(-1);//AG
        }
        if (info.startsWith("Adding ")) {//AG
        	mainView.setProgressMax(-1);//AG
        } else if (info.startsWith("loading ")) {//AG
            progress = new Integer(info.replace("loading ", ""));      //AG
        }//AG
        else if(timeout>0){
        	mainView.setProgressMax(0);//AG
        }
        if (maxPBar < progress) {//AG
            maxPBar = progress;//AG
            mainView.setProgressMax(maxPBar);//AG
            mainView.iconTimer(true);//AG
        }
        mainView.setInfo(info);
        mainView.setProgressValue(maxPBar - progress);//AG
        if (progress == 1) {//AG
            maxPBar = 0;//AG
            mainView.setProgressValue(0);//AG
            mainView.iconTimer(false);//AG
        }
    }

    static void setCacheManager(CacheManager cacheManager) {
        ((GeoImageViewerView) GeoImageViewer.getApplication().getMainView()).setCacheManager(cacheManager);
    }

    public static PreferencesDB getPreferences() {
        return PreferencesDB.getPrefDBInstance();
    }

    public static void addWidget(TransparentWidget widget) {
        ((GeoImageViewerView) GeoImageViewer.getApplication().getMainView()).addWidget(widget);
    }

    public static IImageLayer getCurrentImageLayer() {
    	if(!Platform.batchMode){
	        for (ILayer l : getLayerManager().getLayers().keySet()) {
	            if (l instanceof IImageLayer && l.isActive()) {
	                try {
	                    return (IImageLayer) l;
	                } catch (Exception ex) {
	                    Logger.getLogger(Platform.class.getName()).log(Level.SEVERE, null, ex);
	                }
	            }
	        }
    	}    
        return null;
    }
    
 
    /** 
     * search the cache in the DB, if it doesn't exist then read the properties file
     * even if the file is empty a default path is used
     */
    public static String getCachePath() {
        String cache = Platform.getPreferences().readRow(Constant.PREF_CACHE);
        if (cache.equals("")) {
        	cache = java.util.ResourceBundle.getBundle("GeoImageViewer").getString("cache");
            Platform.getPreferences().updateRow(Constant.PREF_CACHE, cache);
        }
        return cache;
    }
    
    
    
    
   
}
