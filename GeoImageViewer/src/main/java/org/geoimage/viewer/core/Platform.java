/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core;


import javax.media.opengl.awt.GLCanvas;

import org.geoimage.def.GeoImageReader;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.batch.Sumo;
import org.geoimage.viewer.core.configuration.PlatformConfiguration;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.ConsoleLayer;
import org.geoimage.viewer.core.layers.image.CacheManager;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.widget.TransparentWidget;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thoorfr
 */
public class Platform {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(Platform.class);

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
    
    public static GeoImageViewerView getMain() {
        return (GeoImageViewerView) GeoImageViewer.getApplication().getMainView();
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

    public static PlatformConfiguration getConfiguration() {
        return PlatformConfiguration.getConfigurationInstance();
    }

    public static void addWidget(TransparentWidget widget) {
        ((GeoImageViewerView) GeoImageViewer.getApplication().getMainView()).addWidget(widget);
    }

    public static ImageLayer getCurrentImageLayer() {
    	if(!Platform.batchMode){
	        for (ILayer l : getLayerManager().getLayers().keySet()) {
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
    
    public static GeoImageReader getCurrentImageReader(){
    	if(isBatchMode()){
    		return Sumo.getCurrentReader();
    	}else{
    		return getCurrentImageLayer().getImageReader();
    	}
    	
    }
    
 
    /** 
     * search the cache in the DB, if it doesn't exist then read the properties file
     * even if the file is empty a default path is used
     */
    public static String getCachePath() {
        String cache = Platform.getConfiguration().getCachePrefFolder();
        return cache;
    }
    
    
    
    
   
}
