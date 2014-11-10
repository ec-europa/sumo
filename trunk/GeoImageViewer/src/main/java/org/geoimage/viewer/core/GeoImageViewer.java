/*
 * GeoImageViewerApp.java
 */
package org.geoimage.viewer.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.awt.GLCanvas;
import javax.swing.JPopupMenu;

import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.layers.ConsoleLayer;
import org.geoimage.viewer.core.layers.LayerManager;
import org.geoimage.viewer.core.layers.image.CacheManager;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application. Note that the static method can work ONLY
 * AFTER the complete startup of the application
 */
public class GeoImageViewer extends SingleFrameApplication {

    /**
     *
     * @return the cache manager dealing with caching image tiles
     */
    public static CacheManager getCacheManager() {
        return ((GeoImageViewerView) getApplication().getMainView()).getCacheManager();
    }
    /**
     *
     * @return the consoleLayer that deals with scripting actions
     */
    public static ConsoleLayer getConsoleLayer() {
        return ((GeoImageViewerView) getApplication().getMainView()).getConsole();
    }

    /**
     *
     * @return the context dealing with the current sate of the geolocalisation of the application
     */
    public static GeoContext getGeoContext() {
        return ((GeoImageViewerView) getApplication().getMainView()).getGeoContext();
    }

    /**
     *
     * @return the manager that deals with the different displayed layers
     */
    public static LayerManager getLayerManager() {
        return ((GeoImageViewerView) getApplication().getMainView()).getLayerManager();
    }

    /**
     *
     * @return the main class running the OpenGL display of the application
     */
    public static GLCanvas getMainCanvas() {
        return ((GeoImageViewerView) getApplication().getMainView()).getMainCanvas();
    }

    /**
     * trigger the refresh of the data displayed
     */
    public static void refresh() {
        ((GeoImageViewerView) getApplication().getMainView()).refresh();
    }

    /**
     * set the status with the String "info" for the next 10 seconds
     * @param info
     */
    public static void setInfo(String info) {
        ((GeoImageViewerView) getApplication().getMainView()).setInfo(info);
    }

    /**
     * set the status with the String "info" for the next "timeout" milliseconds
     * @param info
     * @param timeout
     */
    public static void setInfo(String info, long timeout) {
        ((GeoImageViewerView) getApplication().getMainView()).setInfo(info, timeout);
    }

    /** set a new cache manager to the application
     *
     * @param cacheManager
     */
    static void setCacheManager(CacheManager cacheManager) {
        ((GeoImageViewerView) getApplication().getMainView()).setCacheManager(cacheManager);
    }

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        try {
            show(new GeoImageViewerView(this));
        } catch (Throwable ex) {
        	Logger.getLogger(GeoImageViewer.class.getName()).log(Level.SEVERE, null, ex);
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
     * @return the instance of GeoImageViewerApp
     */
    public static GeoImageViewer getApplication() {
        return Application.getInstance(GeoImageViewer.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
            JPopupMenu.setDefaultLightWeightPopupEnabled(false);
            launch(GeoImageViewer.class, args);
    }
}
