/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.visualization;

import java.awt.Frame;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;

import javax.media.opengl.GLBase;

import org.geoimage.def.GeoImageReader;
import org.geoimage.opengl.OpenGLContext;
import org.geoimage.viewer.core.api.IMouseMove;
import org.geoimage.viewer.core.layers.GenericLayer;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.widget.dialog.ZoomDialog;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

/**
 *
 * @author thoorfr
 */
public class ZoomWindowLayer extends GenericLayer implements  IMouseMove {

    private String name = "";
    private boolean active = true;
    private BufferedImage image;
    private Point position;
    private boolean newP;
    private Texture texture;
    private ZoomDialog zd;
    private GeoImageReader gir;
    private int band;
    private int[] nat;
    private boolean automaticConstrast=true;

    public ZoomWindowLayer(ImageLayer parent) {
    	super(parent,"",null,null);
        init(parent);
        this.gir=parent.getImageReader();
        //this.gir = GeoImageReaderFactory.create(parent.getImageReader().getFilesList()[0]).get(0);
        this.band = parent.getActiveBand();
        //for (int b : parent.getBand()) {
        this.name += gir.getBandName(band) + " ";
        //}
        this.zd = new ZoomDialog(Frame.getFrames()[0], false, this);
        this.zd.setSize(700, 700);
        this.zd.setVisible(true);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void render(OpenGLContext context) {
        if (position == null) {
            return;
        }
        if (context.isDirty()) {
            if (!newP) {
                Point newposition = new Point((int) (context.getX() + context.getWidth() / 2 * context.getZoom()), (int) (context.getY() / 2 * context.getZoom()));
                if (newposition.distance(position) < 1) {
                    position = newposition;
                }
                newP = true;
            }

        }
        if (this.newP) {
            texture = null;
            image = null;
            this.zd.setSize(position.x - 50, position.y - 50);
            this.image = createImage(this.gir, position.x - 50, position.y - 50, 100, 100);
            BufferedImageOp temp = null;
            if (!isAutomaticConstrast()) {
                temp = new RescaleOp(((ImageLayer)super.getParent()).getContrast(), ((ImageLayer)super.getParent()).getBrightness(), null);
            } else {
                float average = 0;
                for (int i = 0; i < nat.length; i += 20) {
                    average = average + nat[i];
                }

                average = 20 * (average / nat.length);
                temp = new RescaleOp((1 << (8 * gir.getNumberOfBytes())) / 5 / average, 0, null);
            }
            if (image != null) {
                //this.texture = TextureIO.newTexture(temp.filter(image, image), false);
            	this.texture = AWTTextureIO.newTexture(((GLBase)context.getGL()).getGLProfile(),temp.filter(image, image), true);
            }
        }
        this.zd.setTexture(texture);

        if (this.texture != null) {
            this.zd.setTexture(texture);
            this.newP = false;
        }


    }

    private BufferedImage createImage(GeoImageReader gir, int x, int y, int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, gir.getType(true));

        //System.out.println(zoom);
        WritableRaster raster = bufferedImage.getRaster();

        // Put the pixels on the raster.
        //if (bands.length == 1) {
           // int band = band;
            nat = gir.readTile(x, y, width, height,band);
            raster.setPixels(0, 0, width, height, nat);

       /* } else {
            int b = 0;
            for (int band : bands) {
                gir.setBand(band);
                nat = gir.readTile(x, y, width, height);
                ///if (zoom == 1) {
                raster.setSamples(0, 0, width, height, b, nat);
                /*
                 * for (int h = 0; h < height; h++) {
                 *   int temp = h * width;
                 *   for (int w = 0; w < width; w++) {
                 *      raster.setSample(w, h, b, nat[temp + w]);
                 *   }
                 * }
                 **/
                /*
                 * } else {
                 *       for (int h = 0; h < height; h++) {
                 *          int temp = (int) (h * zoom) * (int) (width * zoom);
                 *         for (int w = 0; w < width; w++) {
                 *            try {
                 *               raster.setSample(w, h, b, nat[temp + (int) (w * zoom)]);
                 *          } catch (Exception e) {
                 *               }
                 *          }
                 *      }
                 *  }
                 **/
            /*    b++;
                if (b > raster.getNumBands()) {
                    break;
                }
            }

        }*/
        return bufferedImage;
    }
    
    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.zd.setVisible(active);
    }

    public boolean isRadio() {
        return false;
    }

  

    public String getDescription() {
        return "zoom of the image at full resolution";
    }

    public void dispose() {
        this.zd.setVisible(false);
        this.zd.dispose();
        this.gir.dispose();
    }

    public void mouseMoved(Point imagePosition,Object graphicContext) {
        this.position = imagePosition;
        this.newP = true;

    }

    /**
     * @return the automaticConstrast
     */
    public boolean isAutomaticConstrast() {
        return automaticConstrast;
    }

    /**
     * @param automaticConstrast the automaticConstrast to set
     */
    public void setAutomaticConstrast(boolean automaticConstrast) {
        this.automaticConstrast = automaticConstrast;
    }
}
