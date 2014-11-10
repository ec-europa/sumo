/*
 * GeoImageViewerView.java
 */
package org.geoimage.viewer.core;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.layout.FormAttachment;
import org.fenggui.layout.FormData;
import org.fenggui.render.jogl.EventBinding;
import org.fenggui.render.jogl.JOGLBinding;
import org.geoimage.def.GeoImageReader;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.ILayerListener;
import org.geoimage.viewer.core.layers.ConsoleLayer;
import org.geoimage.viewer.core.layers.LayerManager;
import org.geoimage.viewer.core.layers.image.CacheManager;
import org.geoimage.viewer.widget.GeoOverviewToolbar;
import org.geoimage.viewer.widget.InfoDialog;
import org.geoimage.viewer.widget.LayerManagerWidget;
import org.geoimage.viewer.widget.PluginManagerDialog;
import org.geoimage.viewer.widget.PreferencesDialog;
import org.geoimage.viewer.widget.TimeBarDialog;
import org.geoimage.viewer.widget.TransparentWidget;
import org.geoimage.viewer.widget.WWJPanel;
import org.geoimage.viewer.widget.WidgetManager;
import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.TaskMonitor;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.GLReadBufferUtil;

/**
 * The application's main frame.
 */
public class GeoImageViewerView extends FrameView implements GLEventListener {

    private LayerManager lm;
    private GeoContext geoContext;
    private int dxx = 0;
    private int dyy = 0;
    private boolean onRefresh = false;
    private CacheManager cm;
    private ConsoleLayer cl;
    private TimeBarDialog timeSlider;
    private WWJPanel wwjPanel = null;
    gov.nasa.worldwind.awt.WorldWindowGLCanvas wwjCanvas = null;
    private static boolean onScreenshot = false;
    private boolean worldwindpanelenabled = true;
    private InfoDialog infod;
    private GLU glu;

    public GeoImageViewerView(SingleFrameApplication app) {
        super(app);

        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities( glprofile );
        mainCanvas = new GLCanvas(glcapabilities);
        mainCanvas.addGLEventListener(this);

        final FPSAnimator animator = new FPSAnimator(mainCanvas, 60, true);
	    animator.start();

        initComponents();
        Platform.getPreferences().insertIfNotExistRow("Screenshot file", "~/screenshot.jpg");
        //WidgetManager.addWidget("Navigation", GeoNavigationToolbar.class);
        WidgetManager.addWidget("Overview", GeoOverviewToolbar.class);
        //WidgetManager.addWidget("Time", CurrentTimeWidget.class);
        //WidgetManager.addWidget("Info", InfoWidget.class);
        // AG init the preferences
        Platform.getPreferences().insertIfNotExistRow(Platform.CACHE, System.getProperty("user.dir") + "/sumocache/");
        Platform.getPreferences().insertIfNotExistRow(Platform.PREFERENCES_LASTIMAGE, "");

        mainPanel.addComponentListener(new ComponentListener() {
            // This method is called after the component's size changes

            public void componentResized(ComponentEvent evt) {
                mainCanvas.setSize(sumopanel.getSize());
                mainCanvas.reshape(0, 0, 0, 0);
                if(geoContext!=null){
                	geoContext.setHeight(sumopanel.getHeight());
                	geoContext.setWidth(sumopanel.getWidth());
                }

            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentShown(ComponentEvent e) {
            }

            public void componentHidden(ComponentEvent e) {
            }
        });

        // status bar initialization - message timeout, idle icon and busy animation, etc

        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(true);


        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        //Launch a GC every XX sec
/*        new Thread(new Runnable() {

            public void run() {
                while (true) {
                    System.gc();

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GeoImageViewerView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
*/


        /**
         * Real Stuff
         */
        //setCacheManager(new CacheManager(java.util.ResourceBundle.getBundle("GeoImageViewer").getString("cache")));
        //reads the cache from the embedded database SUMO_DB
        //setCacheManager(new CacheManager(getCachePath()));
        lm = new LayerManager(null);
        lm.setName("Layers");
        lm.setIsRadio(true);
        cl = new ConsoleLayer();
        lm.addLayer(cl);
        infod = new InfoDialog(null, false);

        cl.setMenus(getMenuBar());
        cl.updateTab(jTabbedPane1);
        if (worldwindpanelenabled) {
            wwjPanel = new WWJPanel();
            wwjPanel.setPreferredSize(new Dimension(100,jTabbedPane1.getHeight()));
            jTabbedPane1.addTab("3D", wwjPanel);
            lm.addListenner(new ILayerListener() {

                public void layerAdded(ILayer l) {
                    if (l instanceof IImageLayer) {
                        wwjPanel.add((IImageLayer) l);

                    }
                }

                public void layerRemoved(ILayer l) {
                    if (l instanceof IImageLayer) {
                        wwjPanel.remove((IImageLayer) l);
                    }
                }

                public void layerClicked(ILayer l) {
                    if (l instanceof IImageLayer) {
                        wwjPanel.triggerState((IImageLayer) l);
                    }
                }
            });
        }

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                while (true) {
                    try {
                        Thread.sleep(25);
                        dxx /= 1.2;
                        dyy /= 1.2;
                        if (!geoContext.isDirty()) {
                            geoContext.setDirty(dxx != 0 | dyy != 0);
                        }
                        mainCanvas.repaint();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GeoImageViewer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        worker.execute();

        mainCanvas.addMouseMotionListener(new MouseMotionAdapter() {

            private boolean dragging = false;
            private Point init = null;

            @Override
            public void mouseMoved(MouseEvent e) {
                dragging = false;
                if(geoContext!=null){
	                if (e.isShiftDown()) {
	                    dxx = (int) (geoContext.getZoom() * (e.getX() - e.getComponent().getWidth() / 2) / 10);
	                    dyy = (int) (geoContext.getZoom() * (e.getY() - e.getComponent().getHeight() / 2) / 10);
	                    geoContext.setDirty(true);
	                } else {
	                    Point p = new Point();
	                    try {
	                        p.x = (int) (geoContext.getX() + e.getX() * geoContext.getWidth() / e.getComponent().getWidth() * geoContext.getZoom());
	                        p.y = (int) (geoContext.getY() + e.getY() * geoContext.getHeight() / e.getComponent().getHeight() * geoContext.getZoom());
	                        lm.mouseMoved(p, geoContext);
	                        
	                       // public void setImagePosition(Point imagePosition) {
	                       IImageLayer imgL=Platform.getCurrentImageLayer();
	                       if(imgL!=null){
	                    	   GeoImageReader gir=imgL.getImageReader();
	                    	   double[] geo= gir.getGeoTransform().getGeoFromPixel(p.x,p.y, "EPSG:4326");
	                    	   //double pixVal=gir.read(p.x, p.y);
	                    	   double lon=Math.floor(geo[0]*100000)/100000;
	                    	   double lat=Math.floor(geo[1]*100000)/100000;
	                    	   positionLabel.setText("  Lon:"+lon+ "  Lat:"+lat + "           x:"+p.x + "  y:"+p.y );//+ "    Pixel Value:"+pixVal);
	                       }
	                        
	                    } catch (Exception ex) {
	                    	ex.printStackTrace();
	                    }
	                }
                }
            }
            
            
            @Override
            public void mouseDragged(MouseEvent e) {
                /*AG
                dxx = (int) (geoContext.getZoom() * (e.getX() - e.getComponent().getWidth() / 2) / 10);
                dyy = (int) (geoContext.getZoom() * (e.getY() - e.getComponent().getHeight() / 2) / 10);
                geoContext.setDirty(true);
                 * */
            	if(geoContext!=null){
	                Point p = new Point();
	                p.x = (int) (geoContext.getX() + e.getX() * geoContext.getWidth() / e.getComponent().getWidth() * geoContext.getZoom());
	                p.y = (int) (geoContext.getY() + e.getY() * geoContext.getHeight() / e.getComponent().getHeight() * geoContext.getZoom());
	                if (!dragging) {
	                    dragging = true;
	                    init = p;
	                } else {
	                    lm.mouseDragged(init, p, e.getButton(), geoContext);
	                }
	                init = p;
            	}
            }
        });

        mainCanvas.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
            	if(geoContext!=null){
		            float zoom = (float) (geoContext.getZoom() * Math.pow(2, e.getWheelRotation() / 10.0));
		            //if (zoom < 1) {
		            //    zoom = 1;
		            //}
		            // check mouse position
		            int posX = e.getX();
		            int posY = e.getY();
		            // calculate the image position of the current position of the mouse
		            int x = (int) (geoContext.getX() + posX * geoContext.getWidth() / e.getComponent().getWidth() * geoContext.getZoom());
		            int y = (int) (geoContext.getY() + posY * geoContext.getHeight() / e.getComponent().getHeight() * geoContext.getZoom());
		            geoContext.setZoom(zoom);
		            // translate the image origin to have the same mouse position in the geocontext
		            geoContext.setX((int) (x - posX * geoContext.getWidth() / e.getComponent().getWidth() * geoContext.getZoom()));
		            geoContext.setY((int) (y - posY * geoContext.getHeight() / e.getComponent().getHeight() * geoContext.getZoom()));
		            geoContext.setDirty(true);
            	}
            }
        });

        

        /**
         * dealing with the keyboard entry: if ctrl and alt and shift are not used, redirecting to the console layer
         *
         * shift + arrows to move inside the image
         * shift+PgUp/PgDn to zoom in/out the image
         *
         */
        mainCanvas.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                if (!e.isControlDown() && !e.isAltDown() && !e.isShiftDown()) {
                    cl.addChar(e.getKeyChar());
                }
            }

            public void keyPressed(KeyEvent e) {

                if (e.isShiftDown()) {
                	if(geoContext!=null){
	                    if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
	                        float zoom = (float) (geoContext.getZoom() * 2);
	                        int posX = geoContext.getWidth() / 2;
	                        int posY = geoContext.getHeight() / 2;
	                        int x = (int) (geoContext.getX() + posX * geoContext.getWidth() / e.getComponent().getWidth() * geoContext.getZoom());
	                        int y = (int) (geoContext.getY() + posY * geoContext.getHeight() / e.getComponent().getHeight() * geoContext.getZoom());
	                        geoContext.setZoom(zoom);
	                        geoContext.setX((int) (x - posX * geoContext.getWidth() / e.getComponent().getWidth() * geoContext.getZoom()));
	                        geoContext.setY((int) (y - posY * geoContext.getHeight() / e.getComponent().getHeight() * geoContext.getZoom()));
	                        geoContext.setDirty(true);
	                    }else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
	                        float zoom = (float) (geoContext.getZoom() / 2);
	                        int posX = geoContext.getWidth() / 2;
	                        int posY = geoContext.getHeight() / 2;
	                        int x = (int) (geoContext.getX() + posX * geoContext.getWidth() / e.getComponent().getWidth() * geoContext.getZoom());
	                        int y = (int) (geoContext.getY() + posY * geoContext.getHeight() / e.getComponent().getHeight() * geoContext.getZoom());
	                        geoContext.setZoom(zoom);
	                        geoContext.setX((int) (x - posX * geoContext.getWidth() / e.getComponent().getWidth() * geoContext.getZoom()));
	                        geoContext.setY((int) (y - posY * geoContext.getHeight() / e.getComponent().getHeight() * geoContext.getZoom()));
	                        geoContext.setDirty(true);
	                    }else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
	                        dyy = (int) (5 * geoContext.getZoom());
	                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
	                        dyy = (int) (-5 * geoContext.getZoom());
	                    } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
	                        dxx = (int) (-5 * geoContext.getZoom());
	                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
	                        dxx = (int) (5 * geoContext.getZoom());
	                    }
                	}
                }
                lm.keyPressed(e);
            }

            public void keyReleased(KeyEvent e) {
            }
        });

        mainCanvas.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
            	if(geoContext!=null){
            		Point p = new Point();
            		p.x = (int) (geoContext.getX() + e.getX() * geoContext.getWidth() / e.getComponent().getWidth() * geoContext.getZoom());
            		p.y = (int) (geoContext.getY() + e.getY() * geoContext.getHeight() / e.getComponent().getHeight() * geoContext.getZoom());
            		PickedData.clear();
            		lm.mouseClicked(p, e.getButton(), geoContext);
            	}
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
        jTabbedPane1.setSelectedIndex(0);
    }

    /**
     * trigger a screenshot of the OpenGL canvas for the next frame,
     * parameters of the screenshot are set in the preferences
     */
    public static void screenshot() {
        onScreenshot = true;
    }

    public void init(GLAutoDrawable drawable) {
    	Display display = FengGUI.createDisplay(new JOGLBinding(mainCanvas, mainCanvas.getGL()));
    	geoContext = new GeoContext(display);
        geoContext.initialize(drawable.getContext());
        geoContext.setHeight(sumopanel.getHeight());
    	geoContext.setWidth(sumopanel.getWidth());
    	
        glu = new GLU();    // get GL Utilities

        display.setLayoutManager(new org.fenggui.layout.FormLayout());
        display.setDepthTestEnabled(true);
        display.setSize(mainCanvas.getWidth(), mainCanvas.getHeight());
        new EventBinding(mainCanvas, display);

        LayerManagerWidget lmw = new LayerManagerWidget(lm, display);
        display.addWidget(lmw);
        {
            FormData fd = new FormData();
            fd.left = new FormAttachment(0, 0);
            fd.bottom = new FormAttachment(0, 0);
            lmw.setLayoutData(fd);
        }

        // add overview window
        {
            FormData fd = new FormData();
            fd.left = new FormAttachment(0, 10);
            fd.top = new FormAttachment(100, -10);
            addWidget("Overview", fd, "");
        }

    }

    /**
     * force the refreshing of the underlying model of the OpenGL display
     */
    public void refresh() {
    	if(geoContext!=null){
    		geoContext.setDirty(true);
    	}
    	LayerManagerWidget.updateLayout();
    	mainCanvas.repaint();
    }

    /**
     * Needed implementation of method when you change the size of the main window
     * @param drawable
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        if (height <= 0) { // avoid a divide by zero error!
            height = 1;
        }

        final float h = (float) width / (float) height;
        GL gl = drawable.getGL();
        gl.getGL2().glMatrixMode(GL2.GL_PROJECTION);
        gl.getGL2().glLoadIdentity();
        glu.gluPerspective(0, h, 0.1f, 1);

        // Set the view port (display area) to cover the entire window
        gl.glViewport(0, 0, width, height);

    	glu.gluOrtho2D(0, 1, 0, 1);
    	gl.getGL2().glMatrixMode(GL2.GL_MODELVIEW);
    	gl.getGL2().glLoadIdentity();
    }

    /**
     * THE method handling with frames and screenshots...
     * @param drawable
     */
    public void display(GLAutoDrawable drawable) {
        if (onRefresh) {
            return;
        }
        //used by the Screenshot action
        //it save on a specified file a screenshot of the main view
        if (onScreenshot) {
            onScreenshot = false;
            try {
                JFileChooser fd = new JFileChooser(System.getProperty("user.dir"));
                fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fd.addChoosableFileFilter(new FileNameExtensionFilter("JPEG file", "jpg", "jpeg"));
                int returnVal = fd.showSaveDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File f = null;
                    String name = fd.getSelectedFile().getAbsolutePath();
                    if (name.endsWith("jpg") || name.endsWith("jpeg")) {
                        f = new File(name);
                    } else {
                        f = new File(name + ".jpg");
                    }
                    //TODO test the screenshot function
                    GLReadBufferUtil util=new GLReadBufferUtil(true,true);
                    util.readPixels(mainCanvas.getGL(), true);
                    util.write(f);
                    //Screenshot.writeToFile(f, mainCanvas.getWidth(), mainCanvas.getHeight());
                }

            } catch (Exception ex) {
                Logger.getLogger(GeoImageViewerView.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        onRefresh = true;
        geoContext.initialize(drawable.getContext());
        GL gl = geoContext.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        if (dxx != 0 || dyy != 0) {
            geoContext.setX(geoContext.getX() + (dxx/10));
            geoContext.setY(geoContext.getY() + (dyy/10));
            dxx=0;
            dyy=0;
        }
        geoContext.setDirty(true);
        lm.render(geoContext);
        geoContext.getFenguiDisplay().display();
        onRefresh = false;
        //menuBar.revalidate();
        //menuBar.repaint();
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    /**
     * sets a String in the satus bar
     * @param info: null or "" will clear the status bar
     */
    public void setInfo(String info) {
        setInfo(info, 0);

    }

    /**
     * sets a String in the satus bar with tiemout in milliseconds
     * @param info: null or "" will clear the status bar
     * @param timeout: not supported right now
     */
    public void setInfo(String information, final long timeout) {
        //iw.setInformation(information, timeout);
        statusMessageLabel.setText((information == null) ? "" : information);
    }

    public CacheManager getCacheManager() {
        return cm;
    }

    public void setCacheManager(CacheManager cm) {
        this.cm = cm;
    }

    public LayerManager getLayerManager() {
        return lm;
    }

    public GeoContext getGeoContext() {
        return geoContext;
    }

    public GLCanvas getMainCanvas() {
        return mainCanvas;
    }

    public ConsoleLayer getConsole() {
        return cl;
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = GeoImageViewer.getApplication().getMainFrame();
            aboutBox = new GeoImageViewerAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        GeoImageViewer.getApplication().show(aboutBox);
    }

    @Action
    public void showTimeSlider() {
        if (timeSlider == null || !timeSlider.isVisible()) {
            JFrame mainFrame = GeoImageViewer.getApplication().getMainFrame();
            timeSlider = new TimeBarDialog(mainFrame, false);
            timeSlider.setLocationRelativeTo(mainFrame);
            timeSlider.setVisible(true);
        }
    }

    /**
     * open the lastly successfully image (like using CTRL+L)
     */
    @Action
    public void openLastImage() {
        final String lastImage = Platform.getPreferences().readRow(Platform.PREFERENCES_LASTIMAGE);
        if (lastImage != null) {
            new Thread(new Runnable() {

                public void run() {
                    try {
                        getConsole().execute(new String[]{"image", "image", "file=" + lastImage, "buffer=false"});
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GeoImageViewerView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();
        }
    }

    /** search the cache in the DB, if it doesn't exist then read the properties file
     * even if the file is empty a default path is used
     */
    public String getCachePath() {
        String cache = Platform.getPreferences().readRow(Platform.CACHE);
        if (cache.equals("")) {
            Platform.getPreferences().insertIfNotExistRow(Platform.CACHE, java.util.ResourceBundle.getBundle("GeoImageViewer").getString("cache"));
            cache = java.util.ResourceBundle.getBundle("GeoImageViewer").getString("cache");
        }
        return cache;
    }

    /**
     * open the preferences dialog to set some parameters
     */
    @Action
    public void callPreferences() {
        PreferencesDialog pf = new PreferencesDialog();
        pf.setVisible(true);
    }

    /**
     * open the preferences dialog to set some parameters
     */
    @Action
    public void callPluginManager() {
        PluginManagerDialog dialog = new PluginManagerDialog(new javax.swing.JFrame(), true);
        dialog.setVisible(true);
    }

    /**
     * Method to create and put a widget "name" in the openGL display using the "fd" parameter
     * @param name so far one of ["Overview","Navigation", Time","Info"] as registered in
     * the WidgetManager class
     * @param fd controling the positionning of the widget within the OpenGL display
     * @return
     */
    public TransparentWidget addWidget(String name, FormData fd) {
        return addWidget(name, fd, name);
    }

    public TransparentWidget addWidget(String name, FormData fd, String title) {
        TransparentWidget tw = WidgetManager.createWidget(name);
        tw.setName(title);
        tw.setLayoutData(fd);
        geoContext.getFenguiDisplay().addWidget(tw);
        tw.hook(geoContext.getFenguiDisplay());

        geoContext.getFenguiDisplay().updateMinSize();
        geoContext.getFenguiDisplay().layout();

        return tw;
    }

    /**
     * Method to create and put a widget "name" in the openGL display using the "fd" parameter
     * @param widget the wdget to be put. It should contains LayoutData (call widget.setLayoutData(..) before
     */
    public void addWidget(TransparentWidget widget) {
        geoContext.getFenguiDisplay().addWidget(widget);
        widget.hook(geoContext.getFenguiDisplay());

        geoContext.getFenguiDisplay().updateMinSize();
        geoContext.getFenguiDisplay().layout();
    }

    /**
     * Methods to start/stop the busyIconTimer and to manipulate the progress bar
     */
    public void iconTimer(boolean start) {
        if (start) {
            busyIconTimer.start();
        } else {
            busyIconTimer.stop();
        }
    }

    public void setProgressMax(int max) {
        if (max == -1) {
            progressBar.setIndeterminate(true);
            return;
        }
        progressBar.setMaximum(max);
        progressBar.setIndeterminate(false);
        if (max == 0) {
            busyIconTimer.stop();
            statusAnimationLabel.setIcon(idleIcon);
        }

    }

    public void setProgressValue(int value) {
        progressBar.setValue(value);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu jMenuSystem = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenuImport = new javax.swing.JMenu();
        jMenuItemLastImage = new javax.swing.JMenuItem();
        javax.swing.JMenu jMenuHelp = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        jMenuTools = new javax.swing.JMenu();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        mainPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        sumopanel = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
       // jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        //jMenu4 = new javax.swing.JMenu();

        menuBar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        menuBar.setName("menuBar"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(GeoImageViewerView.class);
        jMenuSystem.setText(resourceMap.getString("jMenuSystem.text")); // NOI18N
        jMenuSystem.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jMenuSystem.setName("jMenuSystem"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(GeoImageViewerView.class, this);
        jMenuItem2.setAction(actionMap.get("callPreferences")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuSystem.add(jMenuItem2);

        jMenuItem3.setAction(actionMap.get("callPluginManager")); // NOI18N
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenuSystem.add(jMenuItem3);

        exitMenuItem.setAction(actionMap.get("quit"));
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        jMenuSystem.add(exitMenuItem);

        menuBar.add(jMenuSystem);

        jMenuImport.setText(resourceMap.getString("jMenuImport.text")); // NOI18N
        jMenuImport.setName("jMenuImport"); // NOI18N

        jMenuItemLastImage.setAction(actionMap.get("openLastImage")); // NOI18N
        jMenuItemLastImage.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItemLastImage.setName("jMenuItem1"); // NOI18N
        jMenuImport.add(jMenuItemLastImage);

        menuBar.add(jMenuImport);

        jMenuHelp.setText(resourceMap.getString("jMenuHelp.text")); // NOI18N
        jMenuHelp.setName("jMenuHelp"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        jMenuHelp.add(aboutMenuItem);

        menuBar.add(jMenuHelp);

        jMenuTools.setText(resourceMap.getString("jMenuTools.text")); // NOI18N
        jMenuTools.setName("jMenuTools"); // NOI18N

        jCheckBoxMenuItem1.setAction(actionMap.get("InfoDial")); // NOI18N
        jCheckBoxMenuItem1.setName("jCheckBoxMenuItem1"); // NOI18N
        jMenuTools.add(jCheckBoxMenuItem1);

        menuBar.add(jMenuTools);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        
        
        positionLabel= new JLabel();
 	    positionLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 	    positionLabel.setName("PositionLabel"); // NOI18N

 	    
        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, statusPanelLayout.createSequentialGroup()
            	.add(positionLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)	
                .add(statusMessageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(positionLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(statusMessageLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(statusAnimationLabel)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(3, 3, 3))
        );

        mainPanel.setName("MainPanel"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N
        jTabbedPane1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                GeoImageViewerView.this.focusGained(evt);
            }
        });

        sumopanel.setName("sumopanel"); // NOI18N
        sumopanel.setLayout(new java.awt.GridLayout(1, 0));

        mainCanvas.setName("mainCanvas"); // NOI18N
        sumopanel.add(mainCanvas);

        jTabbedPane1.addTab(resourceMap.getString("sumopanel.TabConstraints.tabTitle"), sumopanel); // NOI18N
        jTabbedPane1.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(jTabbedPane1.getSelectedIndex()==1){
					//sumopanel.setSize(sumopanel.getWidth()+1,sumopanel.getWidth()+1);
					wwjPanel.resizeWW();
				}
			}
		});

        org.jdesktop.layout.GroupLayout MainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(MainPanelLayout);
        MainPanelLayout.setHorizontalGroup(
            MainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1)//, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
        );
        MainPanelLayout.setVerticalGroup(
            MainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jTabbedPane1)//, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
        );

        jMenuBar1.setName("jMenuBar1"); // NOI18N

     //   jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
     //   jMenu1.setName("jMenu1"); // NOI18N
     //   jMenuBar1.add(jMenu1);

        jMenu2.setText(resourceMap.getString("jMenu2.text")); // NOI18N
        jMenu2.setName("jMenu2"); // NOI18N
        jMenuBar1.add(jMenu2);

        jMenuBar2.setName("jMenuBar2"); // NOI18N

        jMenu3.setName("jMenu3"); // NOI18N
        jMenuBar2.add(jMenu3);

   /*     jMenu4.setText(resourceMap.getString("jMenu4.text")); // NOI18N
        jMenu4.setName("jMenu4"); // NOI18N
        jMenuBar2.add(jMenu4);*/

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

private void focusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_focusGained
    refresh();
}//GEN-LAST:event_focusGained

    @Action
    public void infoDial() {
        infod.setVisible(jCheckBoxMenuItem1.isSelected());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel mainPanel;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
  //  private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
 //   private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenu jMenuImport;
    private javax.swing.JMenuItem jMenuItemLastImage;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenu jMenuTools;
    private javax.swing.JTabbedPane jTabbedPane1;
    private GLCanvas mainCanvas;
    private javax.swing.JMenuBar menuBar;
    public javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JPanel sumopanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private JLabel positionLabel;
    
    
	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub

	}
}