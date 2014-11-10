/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.widget;

import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.background.PlainBackground;
import org.fenggui.border.PlainBorder;
import org.fenggui.util.Color;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.layers.LayerManager;

/**
 *
 * @author thoorfr
 */
public class LayerManagerWidget extends Container {

    public static LayerManagerWidget instance;
    private LayerManager lm;

    public LayerManagerWidget(LayerManager lm, Display display) {
        LayerManagerWidget.instance = this;
        this.lm = lm;
        buildWidget(lm, this);
        ContainerAppearance appearance = getAppearance();
        appearance.add(new PlainBackground(new Color(0, 0, 0, 0)));
        appearance.add(new PlainBorder(new Color(0, 0, 0, 0)));
        //display.setAppearance(appearance);
        pack();
        layout();

    }

    private void buildWidget(final LayerManager lmm, Container parent) {
        Container c=new LayerWidget(lmm);
        parent.addWidget(c);
        if(!lmm.isActive()) return;
        for (final ILayer l : lmm.getLayers()) {
            if (l instanceof LayerManager) {
               buildWidget((LayerManager) l, c);
            } else {
                parent.addWidget(new LayerWidget(l));
            }
        }
    }

    public static void updateLayout() {
    	if(instance!=null){
    		instance.removeAllWidgets();
    		instance.buildWidget(instance.lm, instance);
    		instance.pack();
    		instance.layout();
    	}
    }
}
