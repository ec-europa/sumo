/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.widget;

import gov.nasa.worldwind.Model;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.fenggui.Button;
import org.fenggui.Container;
import org.fenggui.FengGUI;
import org.fenggui.ObservableLabelWidget;
import org.fenggui.Slider;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.event.ISliderMovedListener;
import org.fenggui.event.SliderMovedEvent;
import org.fenggui.event.mouse.IMousePressedListener;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.layout.GridLayout;
import org.fenggui.render.Binding;
import org.fenggui.render.ITexture;
import org.fenggui.render.Pixmap;
import org.fenggui.util.Color;
import org.geoimage.def.SarImageReader;
import org.geoimage.viewer.core.api.GeoContext;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.IImageLayer;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leforth
 */

public class GeoNavigationToolbar extends TransparentWidget {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(GeoNavigationToolbar.class);

	
    private class Compass extends ObservableLabelWidget {

        private double northAngle = 0.0;
        private ITexture texture = null;
        private URL imageFile = null;
        private IImageLayer il;


        public Compass(Container parent) {
            parent.addWidget(this);
            setSize(50, 50);
            this.imageFile = Model.class.getResource("/images/notched-compass.png");
            setExpandable(false);
            setShrinkable(false);
            this.il = null;
        }

        public void setDirection(double northAngle) {
            this.northAngle = northAngle;
        }

        @Override
        public void paint(org.fenggui.render.Graphics g) {
            g.setColor(new Color(0, 0, 0, 0));
            g.drawFilledRectangle(0, 0, getWidth(), getHeight());
            g.setColor(new Color(255, 255, 255, transparency));
            if (texture == null) {
                try {
                    texture = Binding.getInstance().getTexture(imageFile);
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
            // check image has not changed
            {
                boolean found=false;
                for (ILayer l : Platform.getLayerManager().getLayers().keySet()) {
                    if (l instanceof IImageLayer && l.isActive()) {
                        if (il != (IImageLayer) l) {
                            try {
                                il = (IImageLayer) l;
                                northAngle = ((((SarImageReader)il.getImageReader()).getImageAzimuth()) / 180) * Math.PI;
                                System.out.println(northAngle);
                                BufferedImage bi = ImageIO.read(this.imageFile);
                                BufferedImage bi2 = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
                                Graphics2D g2d = bi2.createGraphics();
                                AffineTransform at = new AffineTransform();
                                at.rotate(northAngle, bi.getWidth() / 2.0, bi.getHeight() / 2.0);
                                g2d.drawImage(bi, at, null);
                                if(texture!=null)texture.dispose();
                                texture = Binding.getInstance().getTexture(bi2);
                            } catch (Exception ex) {
                            	logger.error(ex.getMessage(),ex);

                            }
                        }
                        found=true;
                        break;
                    }
                }
                if (!found && il!=null) {
                    il = null;
                    if (texture != null) {
                        texture.dispose();
                    }
                    try {
                        texture = Binding.getInstance().getTexture(imageFile);
                    } catch (IOException e) {
                        //e.printStackTrace();
                        }
                }
            }
            if (texture != null) {
                g.drawScaledImage(new Pixmap(texture), getWidth() / 2 - 25, getHeight() / 2 - 25, 50, 50);
            }

        }
    };

    private class ImageButton extends Button {

        private ITexture texture = null;
        protected Color backgroundColor = null;
        private URL imageFile = null;

        public ImageButton() {
        }

        public ImageButton(Container parent, URL imageFile) {
            this.imageFile = imageFile;
            setExpandable(false);
            setShrinkable(false);
            buildEventListener();
            parent.addWidget(this);
        }

        @Override
        public void paint(org.fenggui.render.Graphics g) {
            g.setColor(new Color(0, 0, 0, 0));
            g.drawFilledRectangle(0, 0, getWidth(), getHeight());
            g.setColor(new Color(255, 255, 255, transparency));
            if (texture == null) {
                try {
                    texture = Binding.getInstance().getTexture(imageFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (texture != null) {
                g.drawImage(texture, getWidth() / 2 - texture.getImageWidth() / 2, getHeight() / 2 - texture.getImageHeight() / 2);
            }
        }

        void buildEventListener() {

        }
    };

    private class Navigation extends ImageButton {

        public Navigation(Container parent) {
            super(parent, ImageButton.class.getResource("/org/geoimage/viewer/core/resources/Navigation.png"));
            setSize(50, 52);
        }

        private void mousePressedAction(MousePressedEvent evt)
        {
            int positionX = evt.getDisplayX() - getDisplayX();
            int positionY = evt.getDisplayY() - getDisplayY();
            int width = getSize().getWidth();
            int height = getSize().getHeight();
            if(positionX < width / 3)
                if(positionY > height / 3)
                    if(positionY < 2 * height / 3)
                    {
                        // clicked on the left arrow
                        System.out.println("left\n");
                        if(Platform.getGeoContext() instanceof GeoContext)
                            Platform.getGeoContext().setX((int)(Platform.getGeoContext().getX() - Platform.getGeoContext().getWidth() / 3));
                    }
            if(positionX > 2 * width / 3)
                if(positionY > height / 3)
                    if(positionY < 2 * height / 3)
                    {
                        // clicked on the right arrow
                        System.out.println("right\n");
                        if(Platform.getGeoContext() instanceof GeoContext)
                            Platform.getGeoContext().setX((int)(Platform.getGeoContext().getX() + Platform.getGeoContext().getWidth() / 3));
                    }
            if(positionX > width / 3)
                if(positionX < 2 * width / 3)
                    if(positionY < height / 3)
                    {
                        // clicked on the bottom arrow
                        System.out.println("bottom\n");
                        if(Platform.getGeoContext() instanceof GeoContext)
                            Platform.getGeoContext().setY((int)(Platform.getGeoContext().getY() + Platform.getGeoContext().getHeight() / 3));
                    } else if(positionY > 2 * height / 3) {                    
                        // clicked on the top arrow
                        System.out.println("top\n");
                        if(Platform.getGeoContext() instanceof GeoContext)
                            Platform.getGeoContext().setY((int)(Platform.getGeoContext().getY() - Platform.getGeoContext().getHeight() / 3));
                    }
        }

        @Override
        void buildEventListener() {
            addMousePressedListener(new IMousePressedListener() {

                public void mousePressed(MousePressedEvent evt) {
                    mousePressedAction(evt);
                }
            });
        }
 
    };

    private class Zoomslider extends Slider{

        private ITexture texture = null;
        private Color backgroundColor = null;
        private int maxZoomLevel = 32;
        private IImageLayer il;
        private float zoom = 0;


        public int getMaxZoomLevel() {
            return maxZoomLevel;
        }

        public void setMaxZoomLevel(int maxZoomLevel) {
            this.maxZoomLevel = maxZoomLevel;
        }

        public Zoomslider(boolean horizontal) {
            super(horizontal);
            setExpandable(false);
            setShrinkable(false);
            this.backgroundColor = new Color(255, 255, 255, 64);
            //buildEventListener();
        }
        
        @Override
        public void paint(org.fenggui.render.Graphics g) {

            // check image has not changed
            if ((il == null) || (!il.isActive())) {
                il = null;
                for (ILayer l : Platform.getLayerManager().getLayers().keySet()) {
                    if (l instanceof IImageLayer && l.isActive()) {
                        il = (IImageLayer) l;
                        // update the max zoom value
                        maxZoomLevel = ((int) Math.max(Math.log10(il.getImageReader().getWidth() / Platform.getGeoContext().getWidth()) / Math.log10(2), Math.log10(il.getImageReader().getHeight() / Platform.getGeoContext().getHeight()) / Math.log10(2))) + 1;
                        break;
                    }
                }
            }

            // set value to the current zoom value
            if(zoom != Platform.getGeoContext().getZoom())
            {
                zoom = Platform.getGeoContext().getZoom();
                if(Platform.getGeoContext() instanceof GeoContext)
                    setValue((zoom - 1) / Math.pow(2, maxZoomLevel));
            }

            g.setColor(new Color(0, 0, 0, 0));
            g.drawFilledRectangle(0, 0, getWidth(), getHeight());
            if (texture == null) {
                try {
                    texture = Binding.getInstance().getTexture(ImageButton.class.getResource("/org/geoimage/viewer/core/resources/Slider.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (texture != null) {
                g.setColor(new Color(255, 255, 255, transparency));
                g.drawImage(texture, getWidth() / 2 - texture.getImageWidth() / 2, 0);
                g.setColor(new Color(192, 192, 192, transparency));
                int width = 12;
                int height = 8;
                g.drawFilledRectangle(getWidth() / 2 - width / 2, (int) (getHeight() * getValue()) - height / 2, width, height);
                g.setColor(new Color(128, 128, 128, transparency));
                g.drawWireRectangle(getWidth() / 2 - width / 2, (int) (getHeight() * getValue()) - height / 2, width + 1, height);
            }

        }

        void buildEventListener() {
            addSliderMovedListener(new ISliderMovedListener() {

                public void sliderMoved(SliderMovedEvent arg0) {
                    if (Platform.getGeoContext() instanceof GeoContext) {
                        GeoContext geoContext = Platform.getGeoContext();
                        float zoom = (float) (1+(float) getValue() * Math.pow(2, maxZoomLevel));
                        int x = (int) (geoContext.getX() + geoContext.getWidth() * geoContext.getZoom() / 2);
                        int y = (int) (geoContext.getY() + geoContext.getHeight() * geoContext.getZoom() / 2);
                        geoContext.setZoom(zoom);
                        geoContext.setX((int) (x - geoContext.getWidth() * zoom / 2));
                        geoContext.setY((int) (y - geoContext.getHeight() * zoom / 2));
                    }
                }
            });

        }

 
    };

    public GeoNavigationToolbar() {
        super("Navigation");
        
        setTransparent(false);
        setLayoutManager(new GridLayout(6, 1));

        // insert compass widget
        new Compass(this);

        // insert plus button
        ImageButton plusButton = new ImageButton(this, ImageButton.class.getResource("/org/geoimage/viewer/core/resources/Minus.PNG"));

        // insert zoom level bar
        final Zoomslider zoomSlider = new Zoomslider(false);
        addWidget(zoomSlider);
        zoomSlider.setSize(20, 100);
        zoomSlider.setMaxZoomLevel(4);
        plusButton.addButtonPressedListener(new IButtonPressedListener() {

            public void buttonPressed(ButtonPressedEvent arg0) {
                zoomSlider.setValue(zoomSlider.getValue() + 0.1);
            }
        });
        // insert plus button
        ImageButton minusButton = new ImageButton(this, ImageButton.class.getResource("/org/geoimage/viewer/core/resources/Plus.PNG"));
        minusButton.addButtonPressedListener(new IButtonPressedListener() {
            public void buttonPressed(ButtonPressedEvent arg0) {
                zoomSlider.setValue(zoomSlider.getValue() - 0.1);
            }
        });

        // insert move widget
        new Navigation((Container) this);

        FengGUI.createButton(this, "test").setVisible(false);

    }
};
