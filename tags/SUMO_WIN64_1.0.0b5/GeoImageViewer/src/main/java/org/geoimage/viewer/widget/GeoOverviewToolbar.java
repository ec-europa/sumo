/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.widget;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.fenggui.ObservableLabelWidget;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.render.Binding;
import org.fenggui.render.ITexture;
import org.fenggui.util.Color;
import org.geoimage.def.GeoImageReader;
import org.geoimage.impl.CosmoSkymedImage;
import org.geoimage.impl.SarImageReader;
import org.geoimage.java2d.util.ScaleTransformation;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.layers.image.CacheManager;
import org.geoimage.viewer.util.Constant;

/**
 *
 * @author leforth
 */
public class GeoOverviewToolbar extends TransparentWidget {

    class Overview extends ObservableLabelWidget {
    	final Logger logger = Logger.getLogger(GeoOverviewToolbar.class);
    	
        private ITexture texture = null;
        protected Color backgroundColor = null;
        private IImageLayer il = null;
        private double ratio = -1.0;
        private Rectangle minimizeRectangle = new Rectangle(1, 1, 10, 8);
        private boolean minimize = false;
        private Point imagePosition;
        private boolean onbuilding = false;
        private BufferedImage bufferedImage = null;

        public Overview() {
            setSize(Constant.OVERVIEW_SIZE, Constant.OVERVIEW_SIZE);
        }

        @Override
        public void paint(org.fenggui.render.Graphics g) {
            super.paint(g);
            if ((il == null) || (!il.isActive())) {
                il = null;
                bufferedImage = null;
                onbuilding = false;
                texture = null;
                for (ILayer l : Platform.getLayerManager().getLayers()) {
                    if (l instanceof IImageLayer && l.isActive()) {
                        il = (IImageLayer) l;
                        break;
                    }
                }
            }

            // do not display window if no image
            setVisible(il != null);

            if (il != null) {
                if (ratio < 0) {
                    GeoImageReader gir = il.getImageReader();
                    // generate a suitable size image
                    ratio = Math.max(((double) gir.getWidth()) / Constant.OVERVIEW_SIZE_DOUBLE
                    			  , ((double) gir.getHeight()) / Constant.OVERVIEW_SIZE_DOUBLE);
                }
                if (texture == null) {
                    try {
                        new Thread(new Runnable() {

                            public void run() {
                                buildOverview();
                            }
                        }).start();
                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                    if (bufferedImage != null) {
                        texture = Binding.getInstance().getTexture(bufferedImage);
                    }
                }
                if (texture != null) {
                    g.setColor(new Color(255, 255, 255, transparency));
                    imagePosition = new Point(0,Constant.OVERVIEW_SIZE - texture.getImageHeight());
                    g.drawImage(texture, imagePosition.x, imagePosition.y);
                    
                    // Draw bounding box - green
                    g.setColor(new Color(0, 255, 0, transparency));

                    // draw current view bounding rectangle in image
                    if (Platform.getGeoContext() instanceof GeoContext) {
                    	g.drawWireRectangle(imagePosition.x + (int) (Platform.getGeoContext().getX() / ratio), imagePosition.y + (int) ((il.getImageReader().getHeight() - Platform.getGeoContext().getY()) / ratio) - (int) (Platform.getGeoContext().getHeight() * Platform.getGeoContext().getZoom() / ratio), (int) (Platform.getGeoContext().getWidth() * Platform.getGeoContext().getZoom() / ratio), (int) (Platform.getGeoContext().getHeight() * Platform.getGeoContext().getZoom() / ratio));
                    }
                }

            }
            // in any case draw the minimize button
            if (this.minimize) {
                g.setColor(new Color(255, 255, 255, transparency));
                g.drawWireRectangle(minimizeRectangle.x, minimizeRectangle.y + 2, minimizeRectangle.width, minimizeRectangle.height);
                g.drawWireRectangle(3, 5, 6, 4);
            } else {
                g.setColor(new Color(255, 255, 255, transparency));
                g.drawWireRectangle(minimizeRectangle.x, minimizeRectangle.y + 2, minimizeRectangle.width, minimizeRectangle.height);
                g.drawFilledRectangle(3, 5, 6, 4);
                //g.drawLine(4, 2, 4, 2);
            }
        }

        @Override
        public void mousePressed(MousePressedEvent evt) {
            if (this.minimizeRectangle.contains(new Point(evt.getLocalX(this), evt.getLocalY(this)))) {
                this.minimize = !this.minimize;
                setForceTransparent(minimize);
            } else {
                if (!this.minimize) {
                    if (texture != null) {
                        if (new Rectangle(imagePosition.x, imagePosition.y, texture.getImageWidth() + 2, texture.getImageHeight() + 2).contains(new Point(evt.getLocalX(this), evt.getLocalY(this)))) {
                            // set the new area to where the mouse was pressed
                            Platform.getGeoContext().setX((int) ((evt.getLocalX(this) - (imagePosition.x)) * ratio) - (int) (Platform.getGeoContext().getWidth() * Platform.getGeoContext().getZoom() / 2));
                            Platform.getGeoContext().setY((int) (il.getImageReader().getHeight() - (evt.getLocalY(this) - (imagePosition.y)) * ratio) - (int) (Platform.getGeoContext().getHeight() * Platform.getGeoContext().getZoom()) / 2);
                        }
                    }
                }
            }
        }

        private synchronized void buildOverview() {
            try {
                if (onbuilding) {
                	logger.debug( "Building overview............");
                    return;
                }
                onbuilding = true;
                bufferedImage = null;
                File f=null;
                if(((SarImageReader)il.getImageReader()).isContainsMultipleImage()&&il.getImageReader() instanceof CosmoSkymedImage){
                	CosmoSkymedImage c=(CosmoSkymedImage)il.getImageReader();
                	f = new File(CacheManager.getCacheInstance(il.getName()).getOverviewURLForMultipleImages(il.getImageReader().getFilesList()[0],"_"+c.getGroup()));
                }else{
                	f = new File(CacheManager.getCacheInstance(il.getName()).getOverviewURL(il.getImageReader().getFilesList()[0]));
                	logger.debug("Overview file:"+f.getAbsolutePath());
            	}
                if (f.exists()) {
                    bufferedImage = ImageIO.read(f);
                } else if (!tryExistingOverviews()) { 
                	logger.debug("Generating overview file...");

                	f.getParentFile().mkdirs();
                    GeoImageReader gir = il.getImageReader();
                    // generate overview image
                    BufferedImage temp = new BufferedImage((int) (gir.getWidth() * (1.0 / ratio)), (int) (gir.getHeight() * (1.0 / ratio)), gir.getType(true));
                    // get a handle on the raster data
                    WritableRaster raster = temp.getRaster();
                    int[] data = gir.readAndDecimateTile(0, 0, gir.getWidth(), gir.getHeight(), 1.0 / ratio, true, null);
                    raster.setSamples(0, 0, temp.getWidth(), temp.getHeight(), 0, data);
                    RescaleOp rescale = new RescaleOp(il.getContrast(), il.getBrightness(), null);
                    rescale.filter(temp, temp);
                    ImageIO.write(temp, "png", f);
                    bufferedImage = temp;
                    logger.info("Overview image:"+f.getAbsolutePath());
                }

            } catch (Exception ex) {
                logger.error(ex.getMessage(),ex);
            }
        }
        
       

        

        /**
         *check if the overview images already exist
         * @return
         */
        private boolean tryExistingOverviews() {
            File f = new File(il.getImageReader().getFilesList()[0]).getParentFile();

            GeoImageReader reader=il.getImageReader();
            //added for mosaic cosmo-skymed because it contains multiple images
            if(reader instanceof CosmoSkymedImage){
            	try {
	            	CosmoSkymedImage readerCosmo=(CosmoSkymedImage)reader;

	            	String overviewName="preview_"+readerCosmo.getGroup()+".jpg";
	            	File overFile=new File(f.getAbsolutePath()+"/"+overviewName);System.out.println(overFile.getAbsolutePath());
	            	
	            	BufferedImage temp=ScaleTransformation.scale(overFile,Constant.OVERVIEW_SIZE);
                    File overview = new File(CacheManager.getCacheInstance(il.getName()).getOverviewURLForMultipleImages(il.getImageReader().getFilesList()[0],"_"+readerCosmo.getGroup()));
                    ImageIO.write(temp, "png", overview);
                    bufferedImage = ImageIO.read(overview);
                    return true;
            	 } catch (Exception ex) {
                     logger.error(ex);
                     return false;
                 }
            }else{
            	//search for overview images
	            for (File o : f.listFiles()) {
	                if (o.isDirectory()) {
	                    continue;
	                }

	                String simplefilename = o.getName().toLowerCase();
	                if ((simplefilename.endsWith(".jpg")||simplefilename.endsWith(".tif")) && (simplefilename.contains("preview") || simplefilename.contains("browse"))) {
	                    try {
	                        BufferedImage temp=ScaleTransformation.scale(o,Constant.OVERVIEW_SIZE);
	                        File overview = new File(CacheManager.getCacheInstance(il.getName()).getOverviewURL(il.getImageReader().getFilesList()[0]));
	                        ImageIO.write(temp, "png", overview);
	                        bufferedImage = ImageIO.read(overview);
	                        return true;
	                    } catch (IOException ex) {
	                    	logger.error(ex);
	                    	return false;
	                    }catch (Exception ex) {
	                    	return false;
	                    }
	                }
	            }
            }
            return false;
        }
    }
    
    

    public GeoOverviewToolbar() {
        super("Overview");
        addWidget(new Overview());
        main.setSize(Constant.OVERVIEW_SIZE, Constant.OVERVIEW_SIZE);
        setSize(Constant.OVERVIEW_SIZE, Constant.OVERVIEW_SIZE);
        setExpandable(false);
        setShrinkable(false);
    }
};
