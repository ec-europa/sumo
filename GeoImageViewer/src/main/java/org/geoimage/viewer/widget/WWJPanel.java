/*
 * WWJPanel.java
 *
 * Created on October 24, 2008, 4:46 PM
 */
package org.geoimage.viewer.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;

import org.geoimage.def.GeoImageReader;
import org.geoimage.viewer.core.GeoImageViewer;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.TimeComponent;
import org.geoimage.viewer.core.gui.manager.LayerManager;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.core.wwj.H2Fetcher;
import org.geoimage.viewer.core.wwj.TimeRenderableLayer;
import org.geoimage.viewer.core.wwj.WWGeoImage;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.Renderable;

/**
 *
 * @author  leforth
 */
public class WWJPanel extends javax.swing.JPanel {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(WWJPanel.class);

    private TimeRenderableLayer catalogLayer;
    private TimeRenderableLayer acquisitionLayer;
    private TimeRenderableLayer imageLayer;

    /** Creates new form WWJPanel */
    //AG set jTabbedPane1 size to 0 instead of 225 to hide it
    public WWJPanel() {
        initComponents();

        // add tab panels
        CatalogPanel catalogPanel = new CatalogPanel(this);
        catalogPanel.setSize(new Dimension(200,jTabbedPane1.getHeight()/2));
        catalogPanel.setBorder(new LineBorder(new Color(255)));

        jTabbedPane1.add("Catalog", catalogPanel);
        PlanningPanel planningPanel = new PlanningPanel(this);
        jTabbedPane1.add("Planner", planningPanel);
        
        this.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
				initGlobe();
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
        
    }
    private void initGlobe(){
    	if(acquisitionLayer==null){
    		acquisitionLayer = new TimeRenderableLayer();
    		wwjCanvas.getModel().getLayers().add(acquisitionLayer);
    	}
    	if(imageLayer==null){
    		// add image layer to visualise the images currently opened
    		imageLayer = new TimeRenderableLayer();
    		wwjCanvas.getModel().getLayers().add(imageLayer);
    	}
    }
    /**
     * 
     * @param layer
     */
    public void add(ImageLayer layer) {
    	try{
	        if(layer==null) return;
	        initGlobe();
	        GeoImageReader reader=layer.getImageReader();
	        List<double[]> imageframe = layer.getImageReader().getFrameLatLon(reader.getWidth(),reader.getHeight());
	        if (imageframe != null) {
	            List<LatLon> ll = new ArrayList<LatLon>();
	            for (double[] c : imageframe) {
	                ll.add(new LatLon(Angle.fromDegreesLatitude(c[1]), Angle.fromDegreesLongitude(c[0])));
	            }
	            GlobeAnnotation ga=new GlobeAnnotation(layer.getName() + " opened", 
        		new Position(new LatLon(Angle.fromDegreesLatitude(imageframe.get(0)[1]), 
        								Angle.fromDegreesLongitude(imageframe.get(0)[0])), 
        								0));
	            
	            WWGeoImage gi = new WWGeoImage(new Polyline(ll,0),ga ,
	            		layer.isActive() ? Color.CYAN : Color.BLUE, layer.isActive() ? Color.CYAN : Color.BLUE);
	            
	            gi.setAnnotationVisible(layer.isActive());
	            gi.setDelegateOwner(layer);
	            imageLayer.addRenderable(gi);
	        }
    	}catch(Exception e){
    		logger.error(e.getMessage(),e);
    	}  
    }

    public void triggerState(ImageLayer layer){
        if(layer==null) return;
        WWGeoImage totrigger=null;
        for(Renderable wwg:imageLayer.getRenderables()){
            if(layer==((WWGeoImage)wwg).getDelegateOwner()){
                totrigger=(WWGeoImage)wwg;
                break;
            }
        }
        totrigger.setAnnotationVisible(layer.isActive());
    }

    public void remove(ImageLayer layer) {
        if(layer==null) return;
        Renderable toremove=null;
        for(Renderable wwg:imageLayer.getRenderables()){
            if(layer==((WWGeoImage)wwg).getDelegateOwner()){
                toremove=wwg;
                break;
            }
        }
        imageLayer.removeRenderable(toremove);
    }

    void setCatalogLayer(boolean ontop) {
        if ((catalogLayer != null) && (acquisitionLayer != null)) {
            if (ontop) {
                wwjCanvas.getModel().getLayers().remove(catalogLayer);
                wwjCanvas.getModel().getLayers().add(catalogLayer);
            } else {
                wwjCanvas.getModel().getLayers().remove(acquisitionLayer);
                wwjCanvas.getModel().getLayers().add(acquisitionLayer);
            }
        }
    }

    Date[] displayCatalogFiles(boolean selected) {
        Date[] date = new Date[2];
        if (catalogLayer != null) {
            catalogLayer.setEnabled(false);
            catalogLayer.removeAllRenderables();
            catalogLayer = null;
        }
        if (selected) {
            try {
                catalogLayer = H2Fetcher.getGeometries();
                TimeComponent.getTimeLayers().add(catalogLayer);
                wwjCanvas.getModel().getLayers().add(catalogLayer);
                wwjCanvas.addSelectListener(new SelectListener() {

                    public void selected(SelectEvent event) {
                        if (event.getEventAction().equals(SelectEvent.LEFT_CLICK)) {
                            Object top = event.getTopObject();
                            if (top instanceof WWGeoImage) {
                                WWGeoImage pl = (WWGeoImage) top;
                                System.out.println(!pl.getAnnotationVisible());
                                pl.setAnnotationVisible(!pl.getAnnotationVisible());
                            }
                        } else {
                            if (event.getEventAction().equals(SelectEvent.RIGHT_CLICK)) {
                                System.out.println("Right click");
                                Object top = event.getTopObject();
                                if (top instanceof WWGeoImage) {
                                    GlobeAnnotation an = ((WWGeoImage) top).getAnnotation();
                                    LayerManager lm = null;
                                    try {
                                        lm = LayerManager.getIstanceManager();
                                    } catch (Exception e) {
                                    }
                                    if (lm == null) {
                                        GeoImageViewer.main(new String[]{"-i", an.getText()});
                                    } else {
                                        SumoPlatform.getApplication().getConsoleLayer().executeCommand(new String[]{"image", "image", "file=" + an.getText()});
                                    }
                                }
                            }
                        }
                    }
                });
            } catch (Exception ex) {
            	logger.error(ex.getMessage(),ex);
            }
            catalogLayer.setEnabled(true);
            date = catalogLayer.calculateMinMaxDates();
        } else {
            date[0] = new Date(0);
            date[1] = new Date();
        }
        wwjCanvas.redraw();
        return date;
    }

    void refresh() {
        wwjCanvas.redraw();
    }

    void removeAllAreas() {
        acquisitionLayer.removeAllRenderables();
    }

    void toggleAcquisitionArea(Geometry geometry, Timestamp acquisitiontime, boolean selected) {
        List<LatLon> ll = new ArrayList<LatLon>();
        if (geometry != null) {
            for (Coordinate c : geometry.getCoordinates()) {
                ll.add(new LatLon(Angle.fromDegreesLatitude(c.y), Angle.fromDegreesLongitude(c.x)));
            }
            Coordinate c = geometry.getCentroid().getCoordinate();
            WWGeoImage gi = new WWGeoImage(new Polyline(ll, 1000), new GlobeAnnotation(acquisitiontime.toString(), new Position(new LatLon(Angle.fromDegreesLatitude(c.y), Angle.fromDegreesLongitude(c.x)), 2000)), Color.ORANGE, Color.RED);
            gi.setAnnotationVisible(selected);
            acquisitionLayer.addRenderable(gi, acquisitiontime);
        }
    }
    /**
     * 
     */
    private void initComponents() {

        splitPane = new javax.swing.JSplitPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jTabbedPane1.setMaximumSize(new Dimension(200,getHeight()));
        
        wwjCanvas = new gov.nasa.worldwind.awt.WorldWindowGLCanvas();
        wwjCanvas.setPreferredSize(new java.awt.Dimension(1000, 800));
        wwjCanvas.setModel(new BasicModel());
        wwjCanvas.getSceneController().setVerticalExaggeration(10);

        setName("NASA Worldwind Layer"); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        splitPane.setDividerLocation(200);
        splitPane.setName("jSplitPane1"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });
        splitPane.setLeftComponent(jTabbedPane1);

        wwjCanvas.setName("wwjCanvas"); // NOI18N
        wwjCanvas.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                WWJPanel.this.focusGained(evt);
            }
        });

        addComponentListener(new ComponentListener() {
            // This method is called after the component's size changes
            public void componentResized(ComponentEvent evt) {
            	/*splitPane.setSize(((JPanel)evt.getSource()).getWidth(),((JPanel)evt.getSource()).getHeight());
                wwjCanvas.setSize(((JPanel)evt.getSource()).getWidth(),((JPanel)evt.getSource()).getHeight());
                wwjCanvas.reshape(0, 0, 0, 0);*/
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentShown(ComponentEvent e) {
            }

            public void componentHidden(ComponentEvent e) {
            }
        });

      splitPane.setRightComponent(wwjCanvas);
      add(splitPane);
    }// </editor-fold>//GEN-END:initComponents

private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
    setCatalogLayer(jTabbedPane1.getSelectedIndex() == 0);
}//GEN-LAST:event_jTabbedPane1StateChanged

private void focusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_focusGained
    refresh();
}//GEN-LAST:event_focusGained

public void resizeWW(){
	 splitPane.setRightComponent(wwjCanvas);
}

// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTabbedPane jTabbedPane1;
    private WorldWindowGLCanvas wwjCanvas;
    // End of variables declaration//GEN-END:variables
}
