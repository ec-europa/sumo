/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.widget;

import org.fenggui.border.PlainBorder;
import org.fenggui.event.ISliderMovedListener;
import org.fenggui.event.SliderMovedEvent;
import org.fenggui.render.Graphics;
import org.fenggui.util.Color;
import org.geoimage.viewer.core.api.IImageLayer;

/**
 *
 * @author thoorfr, ag
 * this slider has been developed for managing the contrast and the brightness
 * 
 */
public class Slider extends TransparentWidget {
    private org.fenggui.Slider slider;
    private static double cvalue=0; //AG used for setting the scale of the contrast slider
    
    public static String CONTRAST="Contrast";
    public static String BRIGHTNESS="Brightness";

    public Slider(boolean horizontal, String name) {
       super(name);
       setSize(200, 70);
       slider=new org.fenggui.Slider(horizontal);
       slider.setVisible(true);
       addWidget(slider);
       slider.getAppearance().add(new PlainBorder(new Color(200,200,200,50)));
    }
    
    public void addListener(ISliderMovedListener listener){
        slider.addSliderMovedListener(listener);
    }
    
    public void removeListener(ISliderMovedListener listener){
        slider.removeSliderMovedListener(listener);
    }

    @Override
    public void paint(Graphics g) {
        slider.getSliderButton().setVisible(!isTransparent());
        super.paint(g);
    }
    
    public void setValue(double value){
       if(cvalue==0){
           cvalue=2*value;
       }
       if(name.equals(CONTRAST)){
            slider.setValue(value/cvalue);
        }
        else  if(name.equals(BRIGHTNESS)){
            slider.setValue(value/100000+0.5);
        }
        else slider.setValue(value);
    }

    public double getValue(){
        if(name.equals(CONTRAST)){
            return slider.getValue()*cvalue;
        }
        else  if(name.equals(BRIGHTNESS)){
            return (slider.getValue()-0.5)*100000;
        }
        else return slider.getValue();
    }
    
    public static ISliderMovedListener createContrastListener(final IImageLayer layer){
        return new ISliderMovedListener() {
            public void sliderMoved(SliderMovedEvent sliderMovedEvent) {
                layer.setContrast((float)(sliderMovedEvent.getPosition()*cvalue));
            }
        };
    }
    
    public static ISliderMovedListener createBrightnessListener(final IImageLayer layer){
        return new ISliderMovedListener() {
            public void sliderMoved(SliderMovedEvent sliderMovedEvent) {
                layer.setBrightness((float)((sliderMovedEvent.getPosition()-0.5)*100000));
            }
        };
    }
    
}
