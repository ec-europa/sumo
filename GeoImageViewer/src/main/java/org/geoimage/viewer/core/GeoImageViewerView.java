/*
 * GeoImageViewerView.java
 */
package org.geoimage.viewer.core;

import java.awt.Dimension;
import java.awt.EventQueue;
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
import java.util.Hashtable;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.LayoutStyle;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.math3.util.FastMath;
import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.layout.FormAttachment;
import org.fenggui.layout.FormData;
import org.fenggui.render.jogl.EventBinding;
import org.fenggui.render.jogl.JOGLBinding;
import org.geoimage.def.GeoImageReader;
import org.geoimage.opengl.OpenGLContext;
import org.geoimage.viewer.actions.AddLastImageAction;
import org.geoimage.viewer.actions.SumoAbstractAction;
import org.geoimage.viewer.core.analysisproc.VDSAnalysisProcessListener;
import org.geoimage.viewer.core.api.ILayerListener;
import org.geoimage.viewer.core.api.iactions.IAction;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.gui.manager.LayerManagerWidget;
import org.geoimage.viewer.core.gui.manager.WidgetManager;
import org.geoimage.viewer.core.layers.BaseLayer;
import org.geoimage.viewer.core.layers.ConsoleLayer;
import org.geoimage.viewer.core.layers.image.CacheManager;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.layers.visualization.LayerPickedData;
import org.geoimage.viewer.widget.GeoOverviewToolbar;
import org.geoimage.viewer.widget.PluginManagerDialog;
import org.geoimage.viewer.widget.PreferencesDialog;
import org.geoimage.viewer.widget.TransparentWidget;
import org.geoimage.viewer.widget.WWJPanel;
import org.geoimage.viewer.widget.dialog.InfoDialog;
import org.geoimage.viewer.widget.dialog.TimeBarDialog;
import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.TaskMonitor;
import org.slf4j.LoggerFactory;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.GLReadBufferUtil;







/**
 * The application's main frame.
 */
public class GeoImageViewerView extends FrameView implements GLEventListener,VDSAnalysisProcessListener {

    private LayerManager lm;
    private OpenGLContext geoContext;
    private int dxx = 0;
    private int dyy = 0;
    private boolean onRefresh = false;
    private CacheManager cm;

    private ConsoleLayer cl;
    private BaseLayer base;

    private TimeBarDialog timeSlider;
    private WWJPanel wwjPanel = null;

    gov.nasa.worldwind.awt.WorldWindowGLCanvas wwjCanvas = null;

    private static boolean onScreenshot = false;
    private boolean worldwindpanelenabled = true;
    private InfoDialog infod;
    private GLU glu;
    private org.jdesktop.application.ResourceMap resourceMap;
    private javax.swing.ActionMap actionMap;
    
    private static GLWindow window;

	private static org.slf4j.Logger logger=LoggerFactory.getLogger(GeoImageViewerView.class);


	class LayerListener implements ILayerListener {

		public LayerListener() {}

	    public void layerAdded(ILayer l) {
	        if (l instanceof ImageLayer) {
	            wwjPanel.add((ImageLayer) l);
	        }
	    }

	    public void layerRemoved(ILayer l) {
	        if (l instanceof ImageLayer) {
	            wwjPanel.remove((ImageLayer) l);
	        }
	    }

	    public void layerClicked(ILayer l) {
	        if (l instanceof ImageLayer) {
	            wwjPanel.triggerState((ImageLayer) l);
	        }
	    }
	}




    public GeoImageViewerView(SingleFrameApplication app) {
    	super(app);

    	resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(GeoImageViewerView.class);
        actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(GeoImageViewerView.class, this);
    	
        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities( glprofile );

        mainCanvas = new GLCanvas(glcapabilities);
        mainCanvas.addGLEventListener(this);

        final FPSAnimator animator = new FPSAnimator(window, 45, true);
	    animator.start();

        initComponents();

        //WidgetManager.addWidget("Navigation", GeoNavigationToolbar.class);
        WidgetManager.getWManagerInstance().addWidget("Overview", GeoOverviewToolbar.class);
        //WidgetManager.addWidget("Time", CurrentTimeWidget.class);
        //WidgetManager.addWidget("Info", InfoWidget.class);
        // AG init the preferences


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


        /**
         * Real Stuff
         */
        lm = LayerManager.getIstanceManager();

        base=new BaseLayer(null);
        base.setName("Layers");
        base.setIsRadio(true);
        lm.setBaseLayer(base);

        cl = new ConsoleLayer(null);
        cl.setName("Console Layer");
        cl.setIsRadio(true);
        lm.setConsoleLayer(cl);


        infod = new InfoDialog(null, false);

        cl.updateTab(jTabbedPane1);
        if (worldwindpanelenabled) {
            wwjPanel = new WWJPanel();
            wwjPanel.setPreferredSize(new Dimension(100,jTabbedPane1.getHeight()));
            jTabbedPane1.addTab("3D", wwjPanel);
            lm.addListener(new GeoImageViewerView.LayerListener());
        }

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                while (true) {
                    try {
                        Thread.sleep(10);
                        dxx /= 1.2;
                        dyy /= 1.2;
                        if (!geoContext.isDirty()) {
                            geoContext.setDirty(dxx != 0 | dyy != 0);
                        }
                        mainCanvas.repaint();
                    } catch (InterruptedException ex) {
                    	logger.error(ex.getMessage(),ex);
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
	                        lm.mouseMoved(p,geoContext);

	                       // public void setImagePosition(Point imagePosition) {
	                       ImageLayer imgL=LayerManager.getIstanceManager().getCurrentImageLayer();
	                       if(imgL!=null){
	                    	   GeoImageReader gir=imgL.getImageReader();
	                    	   double[] geo= gir.getGeoTransform().getGeoFromPixel(p.x,p.y);
	                    	   double lon=FastMath.floor(geo[0]*100000)/100000;
	                    	   double lat=FastMath.floor(geo[1]*100000)/100000;

	                    	   int val=0;
	                    	   try{
	                    		  if(p.x>=0&&p.y>=0&&p.x<gir.getWidth()-1&&p.y<gir.getHeight()-1)
	                    			  val=gir.readPixel(p.x, p.y, imgL.getActiveBand());
	                    	   }catch(Exception ex){
	                    		   val=0;
	                    	   }

	                    	   StringBuilder infopos=new StringBuilder("  Lon:")
	                    	   		.append(lon).append("  Lat:")
	                    	   		.append(lat).append("           x:")
	                    	   		.append(p.x).append("  y:").append(p.y)
	                    	   		.append("  value:").append(val);

	                    	   positionLabel.setText(infopos.toString());
	                       }
	                    } catch (Exception ex) {
	                    	logger.warn(ex.getMessage());
	                    }
	                }
                }
            }


            @Override
            public void mouseDragged(MouseEvent e) {
            	if(geoContext!=null){
	                Point p = new Point();
	                p.x = (int) (geoContext.getX() + e.getX() * geoContext.getWidth() / e.getComponent().getWidth() * geoContext.getZoom());
	                p.y = (int) (geoContext.getY() + e.getY() * geoContext.getHeight() / e.getComponent().getHeight() * geoContext.getZoom());
	                if (!dragging) {
	                    dragging = true;
	                    init = p;
	                } else {
	                    lm.mouseDragged(init, p, e.getButton(),geoContext);
	                }
	                init = p;
            	}
            }
        });

        mainCanvas.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
            	if(geoContext!=null){
		            float zoom = (float) (geoContext.getZoom() * Math.pow(2, e.getWheelRotation() / 10.0));
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
            		LayerPickedData.clear();
            		lm.mouseClicked(p, e.getButton(),geoContext);
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
    	GLProfile.initSingleton();
    	Display display = FengGUI.createDisplay(new JOGLBinding(mainCanvas, mainCanvas.getGL()));
    	geoContext = new OpenGLContext(display);
        geoContext.initialize(drawable.getContext());
        geoContext.setHeight(sumopanel.getHeight());
    	geoContext.setWidth(sumopanel.getWidth());

        glu = new GLU();    // get GL Utilities

        display.setLayoutManager(new org.fenggui.layout.FormLayout());
        display.setDepthTestEnabled(true);
        display.setSize(mainCanvas.getWidth(), mainCanvas.getHeight());
        new EventBinding(mainCanvas, display);

        LayerManagerWidget lmw = LayerManagerWidget.getManagerInstance(display);
        lmw.buildWidget();
        display.addWidget(lmw.getWidget());


        // add overview window
        {
            FormData fd = new FormData();
            fd.left = new FormAttachment(0, 10);
            fd.top = new FormAttachment(100, -10);
            addWidget("Overview", fd, "");
        }

        geoContext.initialize(drawable.getContext());
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

        final float aspect = (float) width / (float) height;
        GL gl = drawable.getGL();
        gl.getGL2().glMatrixMode(GL2.GL_PROJECTION);
        gl.getGL2().glLoadIdentity();
        glu.gluPerspective(0, aspect, 0.1f, 1);

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
                    GLReadBufferUtil util=new GLReadBufferUtil(true,true);
                    util.readPixels(mainCanvas.getGL(), true);
                    util.write(f);
                }

            } catch (Exception ex) {
            	logger.error(ex.getMessage(),ex);
            }
        }
        onRefresh = true;
        GL gl = geoContext.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        if (dxx != 0 || dyy != 0) {
            geoContext.setX(geoContext.getX() + (dxx/10));
            geoContext.setY(geoContext.getY() + (dyy/10));
            dxx=0;
            dyy=0;
        }
        lm.render(geoContext);
        geoContext.getFenguiDisplay().display();
        onRefresh = false;
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

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

    public OpenGLContext getGeoContext() {
        return geoContext;
    }

    public GLCanvas getMainCanvas() {
        return mainCanvas;
    }

    public ConsoleLayer getConsole() {
        return cl;
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
        TransparentWidget tw = WidgetManager.getWManagerInstance().createWidget(name);
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

    public void addStopListener(ActionListener lis){
    	this.stopThreadButton.addActionListener(lis);
    	this.stopThreadButton.setVisible(true);
    }

    public void removeStopListener(ActionListener lis){
        EventQueue.invokeLater(() -> {
			stopThreadButton.removeActionListener(lis);
			stopThreadButton.setEnabled(false);//Visible(false);
		});
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menuBar = new javax.swing.JMenuBar();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItemReloadPlugin= new javax.swing.JMenuItem();
   
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        stopThreadButton=new JButton();
        mainPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        sumopanel = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        vectormenu = new javax.swing.JMenu();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        
        menuBar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        menuBar.setName("menuBar"); // NOI18N
        setMenuBar(menuBar);
        setMenus(menuBar);
        
        statusPanel.setName("statusPanel"); // NOI18N
        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        stopThreadButton.setName("Stop Button");
        stopThreadButton.setVisible(false);
        stopThreadButton.setActionCommand("STOP");

        positionLabel= new JLabel();
 	    positionLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
 	    positionLabel.setName("PositionLabel"); // NOI18N

        //org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
            .addGroup(GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
            	.addComponent(positionLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addComponent(statusMessageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
               	.addComponent(stopThreadButton,15, 15, 15)
                .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(positionLabel, GroupLayout.DEFAULT_SIZE, 19, GroupLayout.PREFERRED_SIZE)
                .addComponent(statusMessageLabel, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
                .addComponent(stopThreadButton,15, 15, 15)
                .addComponent(statusAnimationLabel)
                .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        mainPanel.setName("MainPanel"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        sumopanel.setName("sumopanel"); // NOI18N
        sumopanel.setLayout(new java.awt.GridLayout(1, 0));

        mainCanvas.setName("mainCanvas"); // NOI18N
        sumopanel.add(mainCanvas);

        jTabbedPane1.addTab(resourceMap.getString("sumopanel.TabConstraints.tabTitle"), sumopanel); // NOI18N

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


        jMenuBar2.setName("jMenuBar2"); // NOI18N

        jMenu3.setName("jMenu3"); // NOI18N
        jMenuBar2.add(jMenu3);

        setComponent(mainPanel);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    
    
    
    public void setMenus(JMenuBar menubar) {
    	javax.swing.JMenu jMenuSystem = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
    	
        
        jMenuSystem.setText(resourceMap.getString("jMenuSystem.text")); // NOI18N
        jMenuSystem.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jMenuSystem.setName("jMenuSystem"); // NOI18N

        jMenuItem2.setAction(actionMap.get("callPreferences")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuSystem.add(jMenuItem2);

        jMenuItem3.setAction(actionMap.get("callPluginManager")); // NOI18N
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenuSystem.add(jMenuItem3);

        jMenuItemReloadPlugin.setAction(actionMap.get("reloadPlugins")); // NOI18N
        jMenuItemReloadPlugin.setText(resourceMap.getString("jMenuItemReloadPlugin.text")); // NOI18N
        jMenuItemReloadPlugin.setName("jMenuItemReloadPlugin"); // NOI18N
        jMenuSystem.add(jMenuItemReloadPlugin);

        exitMenuItem.setAction(actionMap.get("quit"));
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        jMenuSystem.add(exitMenuItem);
        menuBar.add(jMenuSystem);

        
        javax.swing.JMenu jMenuImport = new javax.swing.JMenu();
        jMenuImport.setText(resourceMap.getString("jMenuImport.text"));
        jMenuImport.setName(resourceMap.getString("jMenuImport.text"));
        
        javax.swing.JMenuItem lastItem = new javax.swing.JMenuItem();
        lastItem.setText(resourceMap.getString("jMenuLast.text"));
        lastItem.setAction(actionMap.get("jMenuLast")); // NOI18N
        lastItem.setName("jMenuLast"); // NOI18N
        jMenuImport.add(lastItem);
        menuBar.add(jMenuImport);
        
        javax.swing.JMenu jMenuTools = new javax.swing.JMenu();
        jMenuTools.setText(resourceMap.getString("jMenuTools.text"));
        jMenuTools.setName(resourceMap.getString("jMenuTools.text"));
        menuBar.add(jMenuTools);
        
    	Map<String, JMenuItem> menus = new Hashtable<String, JMenuItem>();
        // fill with existing menu items
        for (int i = 0; i < menubar.getMenuCount(); i++) {
            JMenu menu = menubar.getMenu(i);
            String menutext = menu.getText() + "/";
            menus.put(menutext, menu);
            for (int j = 0; j < menu.getMenuComponentCount(); j++) {
                if (menu.getMenuComponent(j) instanceof JMenu) {
                    JMenu submenu = (JMenu) menu.getMenuComponent(j);
                    menus.put(menutext + submenu.getText() + "/", submenu);
                }
            }
        }

        JMenuItem temp = null;
        PluginsManager pl=SumoPlatform.getApplication().getPluginsManager();
        for (final IAction action : pl.getActions().values()) {
            if (!action.getPath().startsWith("$")) {
	            if (!pl.getPlugins().get(action.getName()).isActive()) {
	                continue;
	            }

	            String[] path = action.getPath().split("/");
	            if(action instanceof SumoAbstractAction){
		            JMenuItem mitem = null;
		            String mediumpath = "";
		            for (int i = 0; i < path.length; i++) {
		                mediumpath = new StringBuilder(mediumpath).append(path[i]).append("/").toString();
		                if (menus.containsKey(mediumpath)) {
		                    temp = menus.get(mediumpath);
		                } else {
		                    if (i == path.length - 1) {
		                    	((SumoAbstractAction)action).setMenuName(path[i]);
		                    	temp = new JMenuItem(action);
		                    } else {
		                        temp = new JMenu(path[i]);
		                    }
		                    menus.put(mediumpath, temp);
		                }
		                if (mitem == null) {
		                    mitem = temp;
		                    menubar.add((JMenu) temp);
		                } else {
		                    mitem.add(temp);
		                    mitem = temp;
		                }
		            }
	            }
            }
        }
        javax.swing.JMenu jMenuHelp = new javax.swing.JMenu();
        jMenuHelp.setText(resourceMap.getString("jMenuHelp.text")); // NOI18N
        jMenuHelp.setName("jMenuHelp"); // NOI18N
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        jMenuHelp.add(aboutMenuItem);

        menuBar.add(jMenuHelp);
    }

private void focusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_focusGained
    refresh();
}//GEN-LAST:event_focusGained
	
	@Action
	public void jMenuLast() {
		new AddLastImageAction().execute();
	}	

    @Action
    public void infoDial() {
        infod.setVisible(jCheckBoxMenuItem1.isSelected());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel mainPanel;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
  //  private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu vectormenu;
    private javax.swing.JMenu jMenu3;
 //   private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
   
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItemReloadPlugin;
    
    private javax.swing.JTabbedPane jTabbedPane1;
    private GLCanvas mainCanvas;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private JButton stopThreadButton;
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
		arg0.destroy();
	}

	@Override
	public void startAnalysis() {
		stopThreadButton.setVisible(true);
	}
	@Override
	public void endAnalysis() {
		stopThreadButton.setVisible(false);
	}

	@Override
	public void startAnalysisBand(String message) {
	}

	@Override
	public void calcAzimuthAmbiguity(String message) {
	}

	@Override
	public void agglomerating(String message) {
	}
	@Override
	public void layerReady(ILayer layer) {
	}
	@Override
	public void performVDSAnalysis(String message, int numSteps) {
	}
	@Override
	public void startBlackBorederAnalysis(String message) {
	}
	@Override
	public void nextVDSAnalysisStep(int numSteps) {
	}


	@Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = SumoPlatform.getApplication().getMainFrame();
            aboutBox = new GeoImageViewerAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        SumoPlatform.getApplication().show(aboutBox);
    }

    @Action
    public void showTimeSlider() {
        if (timeSlider == null || !timeSlider.isVisible()) {
            JFrame mainFrame = SumoPlatform.getApplication().getMainFrame();
            timeSlider = new TimeBarDialog(mainFrame, false);
            timeSlider.setLocationRelativeTo(mainFrame);
            timeSlider.setVisible(true);
        }
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
     * open the plugin dialog to set some parameters
     */
    @Action
    public void callPluginManager() {
        PluginManagerDialog dialog = new PluginManagerDialog(new javax.swing.JFrame(), true);
        dialog.setVisible(true);
    }

    /**
     * reload plugins
     */
    @Action
    public void reloadPlugins() {
    	SumoPlatform.getApplication().getPluginsManager().reloadPlugins();
    }


}