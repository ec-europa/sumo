/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.widget;

import java.io.IOException;
import org.fenggui.render.Graphics;
import org.fenggui.render.Pixmap;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.tree.ITreeModel;
import org.fenggui.tree.Tree;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.ILayerManager;
import org.geoimage.viewer.core.layers.LayerManager;

/**
 *
 * @author thoorfr
 */
public class TreeLayerManagerWidget extends TransparentWidget {

    private LayerManager lm;
    private Tree tree;
    private Pixmap minusIcon;
    private ITreeModel<ILayer> model;

    public TreeLayerManagerWidget(LayerManager lmanager) {
        super("Layers");
        this.lm = lmanager;
        model = new ITreeModel<ILayer>() {

            public int getNumberOfChildren(ILayer node) {
                if (node instanceof ILayerManager) {
                    return ((ILayerManager) node).getLayers().size();
                } else {
                    return 0;
                }
            }

            public Pixmap getPixmap(ILayer node) {
                return null;
            }

            public ILayer getNode(ILayer parent, int index) {
                if (parent instanceof ILayerManager) {
                    return ((ILayerManager) parent).getLayers().get(index);
                } else {
                    return null;
                }
            }

            public String getText(ILayer node) {
                return "" + node.getName();
            }

            public ILayer getRoot() {
                return null;
            }
        };
    }

    @Override
    public void paint(Graphics g) {
        if (tree == null) {
            tree = new Tree<ILayer>();
            tree.setModel(model);
            tree.updateMinSize();
            addWidget(tree);
        }
        try {
            super.paint(g);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    @Override
    public void process(InputOutputStream stream) throws IOException, IXMLStreamableException {
        super.process(stream);
        minusIcon = stream.processChild("MinusIconPixmap", minusIcon, Pixmap.class);
    }
}
