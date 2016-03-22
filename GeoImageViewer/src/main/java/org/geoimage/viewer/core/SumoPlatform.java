/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core;


import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import org.geoimage.def.GeoImageReader;
import org.geoimage.opengl.OpenGLContext;
import org.geoimage.viewer.actions.SumoAbstractAction;
import org.geoimage.viewer.actions.SumoActionListener;
import org.geoimage.viewer.core.batch.Sumo;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.image.CacheManager;
import org.geoimage.viewer.core.layers.visualization.ConsoleLayer;
import org.geoimage.viewer.widget.TransparentWidget;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jrc.sumo.configuration.PlatformConfiguration;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thoorfr
 */
public class SumoPlatform extends SingleFrameApplication implements SumoActionListener{
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(SumoPlatform.class);

    private static boolean batchMode=false;
    private PluginsManager plManager=null;


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
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        try {
        	plManager=new PluginsManager();
        	JLabel label = new JLabel();
            label.setName("SUMO_1.3.0");
            show(new GeoImageViewerView(this));
        } catch (Throwable ex) {
        	logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of SumoPlatform
     */
    public static SumoPlatform getApplication() {
        return Application.getInstance(SumoPlatform.class);
    }


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

	public PluginsManager getPluginsManager(){
		return this.plManager;
	}

    public ConsoleLayer getConsoleLayer() {
        return ((GeoImageViewerView) getApplication().getMainView()).getConsole();
    }

    public OpenGLContext getGeoContext() {
        return ((GeoImageViewerView) getApplication().getMainView()).getGeoContext();
    }

    public LayerManager getLayerManager() {
        return LayerManager.getIstanceManager();
    }


    public GeoImageViewerView getMain() {
        return (GeoImageViewerView) getApplication().getMainView();
    }

    static void setCacheManager(CacheManager cacheManager) {
        ((GeoImageViewerView) getApplication().getMainView()).setCacheManager(cacheManager);
    }

    public PlatformConfiguration getConfiguration() {
        return PlatformConfiguration.getConfigurationInstance();
    }

    public void addWidget(TransparentWidget widget) {
        ((GeoImageViewerView) getApplication().getMainView()).addWidget(widget);
    }

    public  void refresh() {
        ((GeoImageViewerView) getApplication().getMainView()).refresh();
    }

    public GeoImageReader getCurrentImageReader(){
    	if(!isBatchMode()){
    		return LayerManager.getIstanceManager().getCurrentImageLayer().getImageReader();
    	}
    	return null;
    }


    /**
     * search the cache in the DB, if it doesn't exist then read the properties file
     * even if the file is empty a default path is used
     */
    public String getCachePath() {
        String cache = getConfiguration().getCachePrefFolder();
        return cache;
    }



    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
            JPopupMenu.setDefaultLightWeightPopupEnabled(false);
            launch(SumoPlatform.class, args);
    }



	@Override
	public void stop(String actionName,SumoAbstractAction action) {
		GeoImageViewerView mainView=((GeoImageViewerView) getApplication().getMainView());
		mainView.hideStopButton(action);
		mainView.setInfo(" done", 10000);
		mainView.iconTimer(false);
		mainView.setProgressMax(0);
	}


	@Override
	public void updateProgress(String msg,int progress,int max) {
		GeoImageViewerView mainView=((GeoImageViewerView) getApplication().getMainView());
		if(progress==-1){
            mainView.setInfo(msg);
            mainView.iconTimer(true);
            mainView.setProgressMax(1);
        } else {
            mainView.iconTimer(true);//AG
            mainView.setInfo(progress + "/" + max + " " + msg);
            mainView.setProgressValue(progress);//AG
            if(max>0)
            	mainView.setProgressMax(max);//AG
		}

	}

	@Override
	public void startAction(String message,int size,SumoAbstractAction action) {
		GeoImageViewerView mainView=((GeoImageViewerView) getApplication().getMainView());

		if(action!=null)
			mainView.showStopButton(action);

		mainView.setInfo(message);
		if(size==-1){//indeterminate
            mainView.setProgressIndeterminate();//AG
            mainView.iconTimer(false);//AG
		}else{
			mainView.setProgressMax(size);
			mainView.iconTimer(true);//AG
		}
		mainView.setProgressValue(0);//AG
	}

	@Override
	public void setMessageInfo(String msg) {
		GeoImageViewerView mainView=((GeoImageViewerView) getApplication().getMainView());
		mainView.setInfo(msg);
	}




/*
	public static void setInfo(String info, long timeout) {
        //AG progress bar management
        int progress = 0;//AG

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

        mainView.setInfo(info);
        mainView.setProgressValue(maxPBar - progress);//AG
        if (progress == 1) {//AG
            maxPBar = 0;//AG
            mainView.setProgressValue(0);//AG
            mainView.iconTimer(false);//AG
        }
    }*/
}
