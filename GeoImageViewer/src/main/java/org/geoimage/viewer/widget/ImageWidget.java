/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.widget;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.fenggui.ObservableLabelWidget;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.render.Binding;
import org.fenggui.render.ITexture;
import org.fenggui.util.Color;
import org.jrc.sumo.util.Constant;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leforth
 */
public class ImageWidget extends TransparentWidget {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(ImageWidget.class);

    private Overview overview = null;
    
    class Overview extends ObservableLabelWidget {

        private ITexture texture = null;
        private Rectangle minimizeRectangle = new Rectangle(1, 1, 10, 8);
        private boolean minimize = false;

        public Overview() {
            setSize(Constant.OVERVIEW_SIZE, Constant.OVERVIEW_SIZE);
        }

        @Override
        public void paint(org.fenggui.render.Graphics g) {
            super.paint(g);

            if (texture != null) {
                g.setColor(new Color(255, 255, 255, transparency));
                g.drawImage(texture, 0, 0);
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
            }
        }
    }

    public void setImage(BufferedImage image) {
        if(this.overview.texture != null){
            this.overview.texture.dispose();
        }
        this.overview.texture=Binding.getInstance().getTexture(image);
        main.setSize(image.getWidth(), image.getHeight());
        setSize(image.getWidth(), image.getHeight()+44);
        setTransparent(false);
        new Thread(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                	logger.error(ex.getMessage(),ex);
                }
                setTransparent(true);
            }
        }).start();
    }

    public void seTitle(String title) {
        this.title.setText(" "+title);
    }

    public ImageWidget(String title) {
        super(title);
        overview = new Overview();
        addWidget(overview);
        main.setSize(Constant.OVERVIEW_SIZE, Constant.OVERVIEW_SIZE);
        setSize(Constant.OVERVIEW_SIZE, Constant.OVERVIEW_SIZE);
        setExpandable(false);
        setShrinkable(false);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if(overview.texture!=null){
            overview.texture.dispose();
        }
    }


};
