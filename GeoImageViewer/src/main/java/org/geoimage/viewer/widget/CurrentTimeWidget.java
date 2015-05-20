/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.widget;

import java.util.Date;

import org.fenggui.render.DirectTextRenderer;
import org.fenggui.util.Color;
import org.geoimage.viewer.widget.fenggui.Label;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thoorfr
 */
public class CurrentTimeWidget extends TransparentWidget {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(CurrentTimeWidget.class);

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
                	logger.error(ex.getMessage(),ex);
                }
            }
        }).start();
    }
}
