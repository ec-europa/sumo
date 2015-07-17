/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.widget;

import java.util.HashMap;
import java.util.Map;

import org.fenggui.Button;
import org.fenggui.Container;
import org.fenggui.FengGUI;
import org.fenggui.event.mouse.IMousePressedListener;
import org.fenggui.event.mouse.MouseButton;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.layout.FormAttachment;
import org.fenggui.layout.FormData;
import org.fenggui.util.Color;
import org.geoimage.viewer.core.Platform;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.IThreshable;
import org.geoimage.viewer.core.layers.BaseLayer;
import org.geoimage.viewer.core.layers.ConsoleLayer;
import org.geoimage.viewer.widget.dialog.LayerDialog;

/**
 *
 * @author thoorfr
 */
public class LayerWidget extends Container {

    private ILayer layer;
    public ILayer getLayer() {
		return layer;
	}

	public void setLayer(ILayer layer) {
		this.layer = layer;
	}

	private static Map<ILayer, LayerDialog> dialogs = new HashMap<ILayer, LayerDialog>();

    public LayerWidget(ILayer layer) {
        this.layer = layer;
        createWidget();
    }

    private void createWidget() {
        final Button bl = FengGUI.createButton(this, layer.getName());
        bl.addMousePressedListener(new IMousePressedListener() {

            public void mousePressed(MousePressedEvent e) {
                if (e.getButton() == MouseButton.LEFT) {
                	if(!(layer instanceof ConsoleLayer || layer instanceof BaseLayer)){
                		if(layer.getParent()==null)
                			layer.setActive(!layer.isActive());
                		else{
                			if(layer.getParent().isActive())
                				layer.setActive(!layer.isActive());
                		}
                    	Platform.getLayerManager().notifyLayerClicked(layer);
                    	Platform.refresh();
                	}
                } else if (e.getButton() == MouseButton.RIGHT) {
                    LayerDialog dialog = dialogs.get(layer);
                    if (dialog == null) {
                        dialog = new LayerDialog(null, true, layer);
                        dialogs.put(layer, dialog);
                    }
                    dialog.setVisible(true);
                } else if (e.getButton() == MouseButton.MIDDLE) {
                    if (layer instanceof IThreshable && ((IThreshable) layer).isThreshable()) {
                        ThresholdBar bar = ThresholdBar.createThresholdBar((IThreshable) layer);
                        FormData fd = new FormData();
                        fd.left = new FormAttachment(0, 500);
                        fd.bottom = new FormAttachment(0, 0);
                        bar.setLayoutData(fd);
                        Platform.addWidget(bar);
                    }

                }
            }
        });

        if (layer!=null&&!layer.isActive()) {
            bl.getAppearance().setTextColor(Color.GRAY);
        } else {
            bl.getAppearance().setTextColor(Color.WHITE);
        }

    }
}
