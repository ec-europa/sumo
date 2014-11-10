/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.widget;

import org.fenggui.render.DirectTextRenderer;
import org.fenggui.util.Color;
import org.geoimage.viewer.widget.fenggui.Label;

/**
 *
 * @author thoorfr
 */
public class InfoWidget extends TransparentWidget {

    private String info;
    private Label l;
    private Thread t;

    public InfoWidget() {
        super("Info");
        l = new Label();
        addWidget(l);
        setSize(400, 40);
        setShrinkable(false);
        setExpandable(false);
        l.getAppearance().setTextRenderer(new DirectTextRenderer());
        l.getAppearance().setTextColor(new Color(0, 255, 255, 60));
    }

    public void setInformation(String info, final long timeout) {
        this.info = info;
        l.setText(info);
        if (t != null || info.equals("")) {
            return;
        }
        if (timeout > 0) {
            t = new Thread(new Runnable() {

                public void run() {
                    try {
                        while (true) {
                            Thread.sleep(timeout);
                            l.setText("");
                        }
                    } catch (InterruptedException ex) {
                        //Logger.getLogger(CurrentTimeWidget.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    t = null;
                }
            });
            t.start();
        }
    }

    public String getInformation() {
        return this.info;
    }
}
