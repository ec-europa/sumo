/*
 * ThumbnailsDialog.java
 *
 * Created on April 17, 2008, 6:00 PM
 */

package org.geoimage.viewer.widget.dialog;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.geoimage.viewer.core.layers.thumbnails.ThumbnailsLayer;

/**
 *
 * @author  thoorfr
 */
public class ThumbnailsDialog extends javax.swing.JDialog {
    private ThumbnailsLayer layer;
    private BufferedImage image;
    
    /** Creates new form ThumbnailsDialog */
    public ThumbnailsDialog(java.awt.Frame parent, boolean modal, ThumbnailsLayer layer) {
        super(parent, modal);
        initComponents();
        this.layer=layer;
    }

    @Override
    public void paint(Graphics g) {
        //super.paint(g);
        g.drawImage(image, 0, 0, getWidth(), getHeight(),null);
    }
    
    
    public void setBufferedImage(BufferedImage image){
        if(image==null) return;
        this.image=image;
        repaint();
    }
    
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconImage(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
