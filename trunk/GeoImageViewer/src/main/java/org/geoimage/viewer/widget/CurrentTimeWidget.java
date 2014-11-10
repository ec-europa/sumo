/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.widget;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fenggui.render.DirectTextRenderer;
import org.fenggui.util.Color;
import org.geoimage.viewer.widget.fenggui.Label;

/**
 *
 * @author thoorfr
 */
public class CurrentTimeWidget extends TransparentWidget {

    public CurrentTimeWidget() {
        super("Time");
        final Label l = new Label(new Date().toString());
        addWidget(l);
        l.getAppearance().setTextRenderer(new DirectTextRenderer());
        l.getAppearance().setTextColor(new Color(0, 255, 255,60));
        new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(1000);
                        l.setText(new Date().toString());
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(CurrentTimeWidget.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }
}
