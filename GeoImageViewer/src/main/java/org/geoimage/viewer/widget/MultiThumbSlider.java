/*
 * 
 */

package org.geoimage.viewer.widget;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSliderUI;


/**
 * @version 1.0 9/3/99
 */
public class MultiThumbSlider extends JFrame {

    public MultiThumbSlider() {
        super("MThumbSlider Example");

        int n = 2;
        MThumbSlider mSlider = new MThumbSlider(n);
        mSlider.setValueAt(25, 0);
        mSlider.setValueAt(75, 1);
        mSlider.setUI(new BasicMThumbSliderUI());

        getContentPane().setLayout(new FlowLayout());
        //getContentPane().add(slider);
        getContentPane().add(mSlider);
    }

    public static void main(String args[]) {

        MultiThumbSlider f = new MultiThumbSlider();
        f.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.setSize(300, 100);
        f.show();
    }
}
class MThumbSlider extends JSlider {

    protected int thumbNum;
    protected BoundedRangeModel[] sliderModels;
    protected Icon[] thumbRenderers;
    protected Color[] fillColors;
    protected Color trackFillColor;
    private static final String uiClassID = "MThumbSliderUI";

    public MThumbSlider(int n) {
        createThumbs(n);
        updateUI();
    }

    protected void createThumbs(int n) {
        thumbNum = n;
        sliderModels = new BoundedRangeModel[n];
        thumbRenderers = new Icon[n];
        fillColors = new Color[n];
        for (int i = 0; i < n; i++) {
            sliderModels[i] = new DefaultBoundedRangeModel(50, 0, 0, 100);
            thumbRenderers[i] = null;
            fillColors[i] = null;
        }
    }

    public void updateUI() {
        AssistantUIManager.setUIName(this);
        super.updateUI();

    /*
     * // another way // updateLabelUIs();
     * setUI(AssistantUIManager.createUI(this)); //setUI(new
     * BasicMThumbSliderUI(this)); //setUI(new MetalMThumbSliderUI(this));
     * //setUI(new MotifMThumbSliderUI(this));
     */
    }

    public String getUIClassID() {
        return uiClassID;
    }

    public int getThumbNum() {
        return thumbNum;
    }

    public int getValueAt(int index) {
        return getModelAt(index).getValue();
    }

    public void setValueAt(int n, int index) {
        getModelAt(index).setValue(n);
    // should I fire?
    }

    public int getMinimum() {
        return getModelAt(0).getMinimum();
    }

    public int getMaximum() {
        return getModelAt(0).getMaximum();
    }

    public BoundedRangeModel getModelAt(int index) {
        return sliderModels[index];
    }

    public Icon getThumbRendererAt(int index) {
        return thumbRenderers[index];
    }

    public void setThumbRendererAt(Icon icon, int index) {
        thumbRenderers[index] = icon;
    }

    public Color getFillColorAt(int index) {
        return fillColors[index];
    }

    public void setFillColorAt(Color color, int index) {
        fillColors[index] = color;
    }

    public Color getTrackFillColor() {
        return trackFillColor;
    }

    public void setTrackFillColor(Color color) {
        trackFillColor = color;
    }
}

class BasicMThumbSliderUI extends BasicSliderUI implements
        MThumbSliderAdditional {

    MThumbSliderAdditionalUI additonalUi;
    MouseInputAdapter mThumbTrackListener;

    public static ComponentUI createUI(JComponent c) {
        return new BasicMThumbSliderUI((JSlider) c);
    }

    public BasicMThumbSliderUI() {
        super(null);
    }

    public BasicMThumbSliderUI(JSlider b) {
        super(b);
    }

    public void installUI(JComponent c) {
        additonalUi = new MThumbSliderAdditionalUI(this);
        additonalUi.installUI(c);
        mThumbTrackListener = createMThumbTrackListener((JSlider) c);
        super.installUI(c);
    }

    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        additonalUi.uninstallUI(c);
        additonalUi = null;
        mThumbTrackListener = null;
    }

    protected MouseInputAdapter createMThumbTrackListener(JSlider slider) {
        return additonalUi.trackListener;
    }

    protected TrackListener createTrackListener(JSlider slider) {
        return null;
    }

    protected ChangeListener createChangeListener(JSlider slider) {
        return additonalUi.changeHandler;
    }

    protected void installListeners(JSlider slider) {
        slider.addMouseListener(mThumbTrackListener);
        slider.addMouseMotionListener(mThumbTrackListener);
        slider.addFocusListener(focusListener);
        slider.addComponentListener(componentListener);
        slider.addPropertyChangeListener(propertyChangeListener);
        slider.getModel().addChangeListener(changeListener);
    }

    protected void uninstallListeners(JSlider slider) {
        slider.removeMouseListener(mThumbTrackListener);
        slider.removeMouseMotionListener(mThumbTrackListener);
        slider.removeFocusListener(focusListener);
        slider.removeComponentListener(componentListener);
        slider.removePropertyChangeListener(propertyChangeListener);
        slider.getModel().removeChangeListener(changeListener);
    }

    protected void calculateGeometry() {
        super.calculateGeometry();
        additonalUi.calculateThumbsSize();
        additonalUi.calculateThumbsLocation();
    }

    protected void calculateThumbLocation() {
    }
    Rectangle zeroRect = new Rectangle();

    public void paint(Graphics g, JComponent c) {

        Rectangle clip = g.getClipBounds();
        thumbRect = zeroRect;

        super.paint(g, c);

        int thumbNum = additonalUi.getThumbNum();
        Rectangle[] thumbRects = additonalUi.getThumbRects();

        for (int i = thumbNum - 1; 0 <= i; i--) {
            if (clip.intersects(thumbRects[i])) {
                thumbRect = thumbRects[i];

                paintThumb(g);

            }
        }
    }

    public void scrollByBlock(int direction) {
    }

    public void scrollByUnit(int direction) {
    }

    //
    // MThumbSliderAdditional
    //
    public Rectangle getTrackRect() {
        return trackRect;
    }

    public Dimension getThumbSize() {
        return super.getThumbSize();
    }

    public int xPositionForValue(int value) {
        return super.xPositionForValue(value);
    }

    public int yPositionForValue(int value) {
        return super.yPositionForValue(value);
    }
}

interface MThumbSliderAdditional {

    public Rectangle getTrackRect();

    public Dimension getThumbSize();

    public int xPositionForValue(int value);

    public int yPositionForValue(int value);
}

class MThumbSliderAdditionalUI {

    MThumbSlider mSlider;
    BasicSliderUI ui;
    Rectangle[] thumbRects;
    int thumbNum;
    private transient boolean isDragging;
    Icon thumbRenderer;
    Rectangle trackRect;
    ChangeHandler changeHandler;
    TrackListener trackListener;

    public MThumbSliderAdditionalUI(BasicSliderUI ui) {
        this.ui = ui;
    }

    public void installUI(JComponent c) {
        mSlider = (MThumbSlider) c;
        thumbNum = mSlider.getThumbNum();
        thumbRects = new Rectangle[thumbNum];
        for (int i = 0; i < thumbNum; i++) {
            thumbRects[i] = new Rectangle();
        }
        isDragging = false;
        trackListener = new MThumbSliderAdditionalUI.TrackListener(mSlider);
        changeHandler = new ChangeHandler();
    }

    public void uninstallUI(JComponent c) {
        thumbRects = null;
        trackListener = null;
        changeHandler = null;
    }

    protected void calculateThumbsSize() {
        Dimension size = ((MThumbSliderAdditional) ui).getThumbSize();
        for (int i = 0; i < thumbNum; i++) {
            thumbRects[i].setSize(size.width, size.height);
        }
    }

    protected void calculateThumbsLocation() {
        for (int i = 0; i < thumbNum; i++) {
            if (mSlider.getSnapToTicks()) {
                int tickSpacing = mSlider.getMinorTickSpacing();
                if (tickSpacing == 0) {
                    tickSpacing = mSlider.getMajorTickSpacing();
                }
                if (tickSpacing != 0) {
                    int sliderValue = mSlider.getValueAt(i);
                    int snappedValue = sliderValue;
                    //int min = mSlider.getMinimumAt(i);
                    int min = mSlider.getMinimum();
                    if ((sliderValue - min) % tickSpacing != 0) {
                        float temp = (float) (sliderValue - min) / (float) tickSpacing;
                        int whichTick = Math.round(temp);
                        snappedValue = min + (whichTick * tickSpacing);
                        mSlider.setValueAt(snappedValue, i);
                    }
                }
            }
            trackRect = getTrackRect();
            if (mSlider.getOrientation() == JSlider.HORIZONTAL) {
                int value = mSlider.getValueAt(i);
                int valuePosition = ((MThumbSliderAdditional) ui).xPositionForValue(value);
                thumbRects[i].x = valuePosition - (thumbRects[i].width / 2);
                thumbRects[i].y = trackRect.y;

            } else {
                int valuePosition = ((MThumbSliderAdditional) ui).yPositionForValue(mSlider.getValueAt(i)); // need
                thumbRects[i].x = trackRect.x;
                thumbRects[i].y = valuePosition - (thumbRects[i].height / 2);
            }
        }
    }

    public int getThumbNum() {
        return thumbNum;
    }

    public Rectangle[] getThumbRects() {
        return thumbRects;
    }
    private static Rectangle unionRect = new Rectangle();

    public void setThumbLocationAt(int x, int y, int index) {
        Rectangle rect = thumbRects[index];
        unionRect.setBounds(rect);

        rect.setLocation(x, y);
        SwingUtilities.computeUnion(rect.x, rect.y, rect.width, rect.height,
                unionRect);
        mSlider.repaint(unionRect.x, unionRect.y, unionRect.width,
                unionRect.height);
    }

    public Rectangle getTrackRect() {
        return ((MThumbSliderAdditional) ui).getTrackRect();
    }

    public class ChangeHandler implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (!isDragging) {
                calculateThumbsLocation();
                mSlider.repaint();
            }
        }
    }

    public class TrackListener extends MouseInputAdapter {

        protected transient int offset;
        protected transient int currentMouseX,  currentMouseY;
        protected Rectangle adjustingThumbRect = null;
        protected int adjustingThumbIndex;
        protected MThumbSlider slider;
        protected Rectangle trackRect;
        protected boolean center;

        TrackListener(MThumbSlider slider) {
            this.slider = slider;
        }

        public void mousePressed(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }
            currentMouseX = e.getX();
            currentMouseY = e.getY();
            slider.requestFocus();
            isDragging = true;
            slider.setValueIsAdjusting(true);
            center=true;

            for (int i = 0; i < thumbNum; i++) {
                Rectangle rect = thumbRects[i];
                if (rect.contains(currentMouseX, currentMouseY)) {
                    offset = currentMouseX - rect.x;
                    adjustingThumbRect = rect;
                    adjustingThumbIndex = i;
                    center=false;
                    return;
                }
            }
        }

        public void mouseDragged(MouseEvent e) {
            if (!slider.isEnabled() || !isDragging || !slider.getValueIsAdjusting()) {
            return;
            }
            if (center) {
                currentMouseX = e.getX();
                slider.setValueIsAdjusting(true);
                Rectangle[] rect = thumbRects;
                int minXThumb = rect[0].x;
                int maxXThumb = rect[1].x;
                int middle = maxXThumb - minXThumb;  
                int halfThumbWidth = rect[0].width / 2;
                trackRect = getTrackRect();

                if(currentMouseX>=trackRect.x-halfThumbWidth && (currentMouseX - minXThumb + maxXThumb)<trackRect.x+(trackRect.width-1)-halfThumbWidth){
                setThumbLocationAt(currentMouseX, 0, 0);
                setThumbLocationAt(currentMouseX - minXThumb + maxXThumb, 0, 1);
                mSlider.setValueAt(ui.valueForXPosition(currentMouseX), 0);
                mSlider.setValueAt(ui.valueForXPosition(currentMouseX - minXThumb + maxXThumb), 1);
                }
                return;
            }
            
            int thumbMiddle = 0;
            currentMouseX = e.getX();
            currentMouseY = e.getY();

            Rectangle rect = thumbRects[adjustingThumbIndex];
            trackRect = getTrackRect();
            int halfThumbWidth = rect.width / 2;
            int thumbLeft = e.getX() - offset;
            int trackLeft = trackRect.x;
            int trackRight = trackRect.x + (trackRect.width - 1);

            thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
            thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

            setThumbLocationAt(thumbLeft, rect.y, adjustingThumbIndex);

            thumbMiddle = thumbLeft + halfThumbWidth;
            mSlider.setValueAt(ui.valueForXPosition(thumbMiddle),
                    adjustingThumbIndex);

        }

        public void mouseReleased(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }
            offset = 0;
            isDragging = false;
            mSlider.setValueIsAdjusting(false);
            mSlider.repaint();
        }

        public boolean shouldScroll(int direction) {
            return false;
        }
    }
}

class AssistantUIManager {

    public static ComponentUI createUI(JComponent c) {
        String componentName = c.getClass().getName();

        int index = componentName.lastIndexOf(".") + 1;
        StringBuffer sb = new StringBuffer();
        sb.append(componentName.substring(0, index));

        //
        // UIManager.getLookAndFeel().getName()
        // 
        // [ Metal ] [ Motif ] [ Mac ] [ Windows ]
        //   Metal CDE/Motif Macintosh Windows
        //

        String lookAndFeelName = UIManager.getLookAndFeel().getName();
        if (lookAndFeelName.startsWith("CDE/")) {
            lookAndFeelName = lookAndFeelName.substring(4, lookAndFeelName.length());
        }
        sb.append(lookAndFeelName);
        sb.append(componentName.substring(index));
        sb.append("UI");

        ComponentUI componentUI = getInstance(sb.toString());

        if (componentUI == null) {
            sb.setLength(0);
            sb.append(componentName.substring(0, index));
            sb.append("Basic");
            sb.append(componentName.substring(index));
            sb.append("UI");
            componentUI = getInstance(sb.toString());
        }

        return componentUI;
    }

    private static ComponentUI getInstance(String name) {
        try {
            return (ComponentUI) Class.forName(name).newInstance();
        } catch (ClassNotFoundException ex) {
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void setUIName(JComponent c) {
        String key = c.getUIClassID();
        String uiClassName = (String) UIManager.get(key);

        if (uiClassName == null) {
            String componentName = c.getClass().getName();
            int index = componentName.lastIndexOf(".") + 1;
            StringBuffer sb = new StringBuffer();
            sb.append(componentName.substring(0, index));
            String lookAndFeelName = UIManager.getLookAndFeel().getName();
            if (lookAndFeelName.startsWith("CDE/")) {
                lookAndFeelName = lookAndFeelName.substring(4, lookAndFeelName.length());
            }
            sb.append(lookAndFeelName);
            sb.append(key);
            UIManager.put(key, sb.toString());
        }
    }

    public AssistantUIManager() {
    }
}
