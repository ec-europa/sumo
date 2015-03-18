/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.widget;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.fenggui.ObservableLabelWidget;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.render.Binding;
import org.fenggui.render.ITexture;
import org.fenggui.util.Color;
import org.geoimage.def.GeoImageReader;
import org.geoimage.def.SarImageReader;
import org.geoimage.java2d.util.ScaleTransformation;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.api.IImageLayer;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.layers.image.CacheManager;
import org.geoimage.viewer.util.Constant;


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
    private GeoOverviewToolbar bar;
    private GeoImageReader gir;
    private float contrast=1;
    private float brightness=1;
    
  
	public Overview(GeoOverviewToolbar toolbar) {
        setSize(Constant.OVERVIEW_SIZE, Constant.OVERVIEW_SIZE);
        this.bar=toolbar;
    }

    @Override
    public void paint(org.fenggui.render.Graphics g) {
        super.paint(g);
        if ((il == null) || (!il.isActive())) {
            il = null;
            onbuilding = false;
            texture = null;
            for (ILayer l : Platform.getLayerManager().getLayers().keySet()) {
                if (l instanceof IImageLayer && l.isActive()) {
                    il = (IImageLayer) l;
                    break;
                }
            }
        }

        // do not display window if no image
        setVisible(il != null);

        if (il != null) {
        	contrast=il.getContrast();
        	brightness=il.getBrightness();
            gir = il.getImageReader();
            // generate a suitable size image
            ratio = Math.max(((double) gir.getWidth()) / Constant.OVERVIEW_SIZE_DOUBLE , ((double) gir.getHeight()) / Constant.OVERVIEW_SIZE_DOUBLE);
            if (texture == null) {
                try {
                	if (!onbuilding) {
	                    new Thread(new Runnable() {
	                        public void run() {
	                            buildOverview();
	                        }
	                    }).start();
                	}    
                } catch (Exception ex) {
                    logger.error(ex);
                }
                if (((SarImageReader)gir).getOverViewImage() != null) {
                    texture = Binding.getInstance().getTexture(((SarImageReader)gir).getOverViewImage());
                }
            }
            if (texture != null) {
                g.setColor(new Color(255, 255, 255, bar.transparency));
                imagePosition = new Point(0,Constant.OVERVIEW_SIZE - texture.getImageHeight());
                g.drawImage(texture, imagePosition.x, imagePosition.y);
                
                // Draw bounding box - green
                g.setColor(new Color(0, 255, 0, bar.transparency));

                // draw current view bounding rectangle in image
                GeoContext ctx=Platform.getGeoContext(); 
                int xmin=imagePosition.x + (int) (ctx.getX() / ratio);
                int ymin=imagePosition.y + (int) ((gir.getHeight() - ctx.getY()) / ratio) - (int) (ctx.getHeight() * ctx.getZoom() / ratio);
                int xmax= (int) (ctx.getWidth() * ctx.getZoom() / ratio);
                int ymax=(int) (ctx.getHeight() * ctx.getZoom() / ratio);
              	g.drawWireRectangle(xmin, ymin,xmax, ymax);
            }

        }
        // in any case draw the minimize button
        if (this.minimize) {
            g.setColor(new Color(255, 255, 255, bar.transparency));
            g.drawWireRectangle(minimizeRectangle.x, minimizeRectangle.y + 2, minimizeRectangle.width, minimizeRectangle.height);
            g.drawWireRectangle(3, 5, 6, 4);
        } else {
            g.setColor(new Color(255, 255, 255, bar.transparency));
            g.drawWireRectangle(minimizeRectangle.x, minimizeRectangle.y + 2, minimizeRectangle.width, minimizeRectangle.height);
            g.drawFilledRectangle(3, 5, 6, 4);
        }
    }

    @Override
    public void mousePressed(MousePressedEvent evt) {
        if (this.minimizeRectangle.contains(new Point(evt.getLocalX(this), evt.getLocalY(this)))) {
            this.minimize = !this.minimize;
            bar.setForceTransparent(minimize);
        } else {
            if (!this.minimize) {
                if (texture != null) {
                    if (new Rectangle(imagePosition.x, imagePosition.y, texture.getImageWidth() + 2, texture.getImageHeight() + 2).contains(new Point(evt.getLocalX(this), evt.getLocalY(this)))) {
                        // set the new area to where the mouse was pressed
                        Platform.getGeoContext().setX((int) ((evt.getLocalX(this) - (imagePosition.x)) * ratio) - (int) (Platform.getGeoContext().getWidth() * Platform.getGeoContext().getZoom() / 2));
                        Platform.getGeoContext().setY((int) (gir.getHeight() - (evt.getLocalY(this) - (imagePosition.y)) * ratio) - (int) (Platform.getGeoContext().getHeight() * Platform.getGeoContext().getZoom()) / 2);
                    }
                }
            }
        }
    }

    /**
     * 
     */
    protected synchronized void buildOverview() {
        try {
            if (onbuilding) {
            	logger.debug( "Building overview............");
            	try {
            	    Thread.sleep(2000);                 
            	} catch(InterruptedException ex) {
            	    Thread.currentThread().interrupt();
            	}
            	return;
            }
            onbuilding = true;
            
            //search file in cache
            File f=CacheManager.getCacheInstance(gir.getDisplayName(0)).getOverviewFile();
            
            //if we have found the overview in cache we use it
            if (f.exists()) {
            	((SarImageReader)gir).setOverViewImage(ImageIO.read(f));
            } else if (!tryExistingOverviews()) { 
            	logger.debug("Generating overview file...");
            	File folder=f.getParentFile();
            	if(!folder.exists()){
            		folder=new File(folder.getAbsoluteFile()+"/");
            		folder.mkdir();
            	}
                // generate overview image
                BufferedImage temp = new BufferedImage((int) (gir.getWidth() * (1.0 / ratio)), (int) (gir.getHeight() * (1.0 / ratio)), BufferedImage.TYPE_USHORT_GRAY);
                // get a handle on the raster data
                WritableRaster raster = temp.getRaster();
                int[] data = gir.readAndDecimateTile(0, 0, gir.getWidth(), gir.getHeight(), 1.0 / ratio, true, null,0);
                raster.setSamples(0, 0, temp.getWidth(), temp.getHeight(), 0, data);
                RescaleOp rescale = new RescaleOp(contrast,brightness, null);
                rescale.filter(temp, temp);
                ImageIO.write(temp, "png", f);
                ((SarImageReader)gir).setOverViewImage(temp);
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
    protected boolean tryExistingOverviews() {
        boolean finded=false;
        
    	File f = gir.getOverviewFile();
    	if(f!=null&&f.exists()){
    		try{
        		BufferedImage temp=ScaleTransformation.scale(f,Constant.OVERVIEW_SIZE);
        		//create file in cache
                File overview = CacheManager.getCacheInstance(il.getName()).getOverviewFile();
                ImageIO.write(temp, "png", overview);
                ((SarImageReader)gir).setOverViewImage(ImageIO.read(overview));
                finded=true;
    		} catch (Exception ex) {
                logger.error(ex);
                finded=false;
            }    
    	}
    	return finded;
    }
    
    public GeoImageReader getGir() {
  		return gir;
  	}

  	public void setGir(GeoImageReader gir) {
  		this.gir = gir;
  		ratio = Math.max(((double) gir.getWidth()) / Constant.OVERVIEW_SIZE_DOUBLE , ((double) gir.getHeight()) / Constant.OVERVIEW_SIZE_DOUBLE);
  	}

}

/**
 *
 * @author leforth
 */
public class GeoOverviewToolbar extends TransparentWidget {

    public GeoOverviewToolbar() {
        super("Overview");
        addWidget(new Overview(this));
        main.setSize(Constant.OVERVIEW_SIZE, Constant.OVERVIEW_SIZE);
        setSize(Constant.OVERVIEW_SIZE, Constant.OVERVIEW_SIZE);
        setExpandable(false);
        setShrinkable(false);
    }
    
    
    /*
	public static void main(String args[]) {
		try{
			ImageIO.scanForPlugins();
			Object o=ImageIO.getImageReadersByFormatName("tiff");
			String safe="C:/tmp/sumo_images/S1/IW/S1A_IW_SLC__1SDH_20140502T170314_20140502T170344_000421_0004CC_1A90.SAFE/manifest.safe";

			List<GeoImageReader> readers= GeoImageReaderFactory.createReaderForName(safe);
			Sentinel1SLC slc=(Sentinel1SLC)(readers.get(0));
			
			Overview overview=new Overview(null);
			overview.setGir(slc);
			overview.buildOverview();
			
		}catch(Exception e){
			e.printStackTrace();
		}	
		
	}*/
}





