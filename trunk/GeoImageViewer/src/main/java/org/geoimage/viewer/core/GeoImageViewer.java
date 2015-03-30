/*
 * GeoImageViewerApp.java
 */
package org.geoimage.viewer.core;


import javax.swing.JPopupMenu;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.slf4j.LoggerFactory;

/**
 * The main class of the application. Note that the static method can work ONLY
 * AFTER the complete startup of the application
 */
public class GeoImageViewer extends SingleFrameApplication {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(GeoImageViewer.class);

   
    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        try {
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
