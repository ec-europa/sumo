/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.widget;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.IWidget;
import org.fenggui.LabelAppearance;
import org.fenggui.LayoutManager;
import org.fenggui.ObservableLabelWidget;
import org.fenggui.ObservableWidget;
import org.fenggui.background.GradientBackground;
import org.fenggui.border.BevelBorder;
import org.fenggui.event.IDragAndDropListener;
import org.fenggui.event.mouse.IMouseEnteredListener;
import org.fenggui.event.mouse.IMouseExitedListener;
import org.fenggui.event.mouse.IMousePressedListener;
import org.fenggui.event.mouse.MouseButton;
import org.fenggui.event.mouse.MouseEnteredEvent;
import org.fenggui.event.mouse.MouseExitedEvent;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.layout.BorderLayout;
import org.fenggui.layout.BorderLayoutData;
import org.fenggui.util.Color;
import org.geoimage.viewer.widget.fenggui.Label;

/**
 *
 * @author thoorfr
 */
public class TransparentWidget extends Container implements ITransparent {

    private boolean transparent = false;
    protected int transparency = 255;
    private Runnable transparizer = null;
    private ExecutorService pool;
    private IMouseEnteredListener mel = null;
    private IMouseExitedListener mexl = null;
    protected Container main = null;
    protected ObservableLabelWidget title = null;
    private Color titleColor = new Color(255, 255, 255, 200);
    private Color titleColorTransparent = new Color(255, 255, 255, 0);
    private TransparentWidget THIS = null;
    private BevelBorder bb;
    protected String name = "[Widget]";
    private boolean forceTransparent;

    public TransparentWidget() {
        this(" ");
    }

    public TransparentWidget(String name) {
        super();
        this.name = name;
        THIS = this;
        bb = new BevelBorder(new Color(255, 255, 255, 50), new Color(255, 255, 255, 200));

        title = new ObservableLabelWidget();
        title.addMousePressedListener(new IMousePressedListener() {

            public void mousePressed(MousePressedEvent mousePressedEvent) {
                if (mousePressedEvent.getButton().equals(MouseButton.RIGHT)) {
                    THIS.getDisplay().removeWidget(THIS);
                    removedFromWidgetTree();
                }
            }
        });
        title.setText(" " + name);
        super.setLayoutManager(new BorderLayout());
        super.addWidget(title);
        title.getAppearance().setTextColor(new Color(255, 255, 255, 200));
        title.setVisible(true);
        title.setLayoutData(BorderLayoutData.NORTH);
        main = new Container();
        super.addWidget(main);
        main.setLayoutData(BorderLayoutData.CENTER);
        pool = new ScheduledThreadPoolExecutor(1);
        transparizer = new Runnable() {

            public void run() {
                try {
                    if (transparent) {
                        Thread.sleep(5000);
                        while (transparency > 60) {
                            transparency -= 10;
                            Thread.sleep(10);
                        }
                        transparency = 60;
                    } else {
                        while (transparency < 255) {
                            transparency += 10;
                            Thread.sleep(10);
                        }
                        transparency = 255;
                    }
                } catch (InterruptedException ex) {
                    //Logger.getLogger(TransparentWidget.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        mel = new IMouseEnteredListener() {

            public void mouseEntered(MouseEnteredEvent mouseEnteredEvent) {
                mmouseEntered(mouseEnteredEvent);
                if (mouseEnteredEvent.getEntered() instanceof Label) {
                    LabelAppearance la = ((Label) mouseEnteredEvent.getEntered()).getAppearance();
                    Color c = la.getTextColor();
                    la.setTextColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 1));
                }
            }
        };

        mexl = new IMouseExitedListener() {

            public void mouseExited(MouseExitedEvent mouseExited) {
                mmouseExited(mouseExited);
                if (mouseExited.getExited() instanceof Label) {
                    LabelAppearance la = ((Label) mouseExited.getExited()).getAppearance();
                    Color c = la.getTextColor();
                    la.setTextColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.3f));
                }
            }
        };

        getAppearance().add(bb);
        title.addMouseEnteredListener(mel);
        title.addMouseExitedListener(mexl);

    }

    public void setName(String name) {
        title.setText(" " + name);
    }

    @Override
    public void addWidget(IWidget widget) {
        main.addWidget(widget);
        addListeners(widget);
    }

    @Override
    public void setLayoutManager(LayoutManager lm) {
        main.setLayoutManager(lm);
    }

    private void addListeners(IWidget widget) {
        if (widget instanceof ObservableWidget) {
            ((ObservableWidget) widget).addMouseEnteredListener(mel);
            ((ObservableWidget) widget).addMouseExitedListener(mexl);
        }
        if (widget instanceof org.fenggui.Slider) {
            ((org.fenggui.Slider) widget).getSliderButton().addMouseEnteredListener(mel);
            ((org.fenggui.Slider) widget).getSliderButton().addMouseExitedListener(mexl);
        }
        if (widget instanceof Container) {
            for (IWidget w : ((Container) widget).getContent()) {
                addListeners(w);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEnteredEvent mouseEnteredEvent) {
        if (forceTransparent) {
            return;
        }
        setTransparent(false);
        title.getAppearance().setTextColor(titleColor);
        getAppearance().add(new GradientBackground(new Color(100, 100, 100, 255), new Color(20, 20, 20, 255)));
        getAppearance().add(bb);
    }

    public void mmouseEntered(MouseEnteredEvent mouseEnteredEvent) {
        mouseEntered(mouseEnteredEvent);
    }

    @Override
    public void mouseExited(MouseExitedEvent mouseExitedEvent) {
        setTransparent(true);
        title.getAppearance().setTextColor(titleColorTransparent);
        getAppearance().removeAll();
    }

    public void mmouseExited(MouseExitedEvent mouseExitedEvent) {
        mouseExited(mouseExitedEvent);
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        if (this.transparent != transparent) {
            for (IWidget w : main.getContent()) {
                if (w instanceof ITransparent) {
                    ((ITransparent) w).setTransparent(transparent);
                }
            }
            this.transparent = transparent;
            pool.shutdownNow();
            pool = new ScheduledThreadPoolExecutor(1);
            pool.submit(transparizer);
        }
    }

    public void setForceTransparent(boolean transp) {
        this.forceTransparent = transp;
    }

    public void hook(Display display) {
        display.addDndListener(new WindowMoveDnDListenerImpl());
    }

    /**
     * @return the forceTransparent
     */
    public boolean isForceTransparent() {
        return forceTransparent;
    }

    class WindowMoveDnDListenerImpl implements IDragAndDropListener {

        int oldX = 0;
        int oldY = 0;

        public void select(int x, int y) {
            oldX = x;
            oldY = y;
        }

        public void drag(int x, int y) {
            // restrict dragging windows to the display
            // see http://www.fenggui.org/forum/index.php?topic=85.0
            if (x < 0 ||
                    y < 0 ||
                    x > getParent().getSize().getWidth() ||
                    y > getParent().getSize().getHeight()) {
                return;
            }
            move(x - oldX, y - oldY);
            oldX = x;
            oldY = y;
        }

        public void drop(int x, int y, IWidget dropOn) {
        }

        public boolean isDndWidget(IWidget w, int x, int y) {
            return w == title || main.getContent().contains(w);
        }
    }
}
