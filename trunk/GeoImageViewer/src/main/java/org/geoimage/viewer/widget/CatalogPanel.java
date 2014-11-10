/*
 * CatalogPanel.java
 *
 * Created on October 27, 2008, 10:52 AM
 */

package org.geoimage.viewer.widget;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;

import org.geoimage.factory.GeoImageReaderFactory;
import org.geoimage.viewer.core.TimeComponent;
import org.jdesktop.application.Action;

/**
 *
 * @author  leforth
 */
public class CatalogPanel extends javax.swing.JPanel {
    private WWJPanel wwjPanel;
    private imagetypeModel imagetypeModel;
    private Date max = new Date(),  min = new Date(0);
    private Date[] dates = new Date[2];
    private int lastSlideValue = 0;
    boolean entered = false;
    boolean read = false;
    int inc = +1;
    int slideval = 0;
    private int refreshRate=100;

    private class imagetypeModel extends javax.swing.table.DefaultTableModel {

        private imagetypeModel(Object[][] object, String[] string) {
            super(object, string);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if(columnIndex == 0)
                return String.class;
            else
                return Boolean.class;
        }

    }

    /** Creates new form CatalogPanel */
    public CatalogPanel(final WWJPanel wwjPanel) {
        initComponents();

        this.wwjPanel = wwjPanel;

        imagetypeModel = new imagetypeModel(
            new Object [][] {
            },
            new String [] {
                "Image Type", "Show"
            }
        );
        for(int index = 0; index < GeoImageReaderFactory.FORMATS.size(); index++)
            imagetypeModel.addRow(new Object[]{GeoImageReaderFactory.FORMATS.get(index), new Boolean(true)});

        imagetypeModel.addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent e) {
                int selectedrow = jTable1.getSelectedRow();
                if((selectedrow != -1) && (selectedrow < jTable1.getRowCount()))
                {
                    updatecatalogDisplay();
                }
            }
        });
        jTable1.setModel(imagetypeModel);

        // initialise formatted dates
        jFormattedTextField1.setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))));
        jFormattedTextField1.setText("2004-01-01 12:00:00");
        jFormattedTextField2.setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))));
        jFormattedTextField2.setText((new Timestamp(new Date().getTime())).toString());

        jSlider3.setMaximum(jSlider2.getMaximum() - jSlider2.getValue() + jSlider1.getValue());
        jSlider3.setValue(slideval);
        TimeComponent.setDirty(true);

        // make sure the right layer is top
        addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                wwjPanel.setCatalogLayer(true);
            }

            public void focusLost(FocusEvent e) {
                wwjPanel.setCatalogLayer(false);
            }
        });

        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                while (isVisible()) {
                    try {
                        Thread.sleep(1000);
                        if (TimeComponent.isDirty()) {
                            TimeComponent.setDirty(false);
                            max = TimeComponent.getMaximumDate();
                            min = TimeComponent.getMinimumDate();
                            if (min == null || max == null) {
                                max = Timestamp.valueOf("9999-01-01 23:59:59");
                                min = new Date(0);
                            }
                            updateComponents();
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(CatalogPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        new Thread(task).start();
    }

    private void updateComponents() {
        jSlider3.setValue(slideval);
        if (jSlider1.getValue() > jSlider2.getValue()) {
            jSlider1.setValue(jSlider2.getValue() - 1);
        }
        dates[0] = new Timestamp(jSlider1.getValue() * (max.getTime() - min.getTime()) / jSlider1.getMaximum() + min.getTime());
        dates[1] = new Timestamp(jSlider2.getValue() * (max.getTime() - min.getTime()) / jSlider2.getMaximum() + min.getTime());
        TimeComponent.setDates(dates);
        jFormattedTextField1.setText(dates[0].toString().substring(0, 19));
        jFormattedTextField1.repaint();
        jFormattedTextField2.setText(dates[1].toString().substring(0, 19));
        jFormattedTextField2.repaint();
        updateUI();
        wwjPanel.refresh();
    }

    private void updatecatalogDisplay() {
        if(jCheckBox1.isSelected())
        {
            // build sql request
            String sql = "SELECT * FROM CATALOGUE WHERE IMAGETYPE IN (";
            for(int index = 0; index < jTable1.getRowCount(); index++)
                if((Boolean)jTable1.getValueAt(index, 1))
                    sql += "'" + jTable1.getValueAt(index, 0) + "', ";
            sql += "'')";
//            sql += " AND (DATE_CREATION >= '" + jFormattedTextField1.getText() + "')";
//            sql += " AND (DATE_CREATION <= '" + jFormattedTextField2.getText() + "')";
            Date[] date = wwjPanel.displayCatalogFiles(true);
            min = date[0];
            max = date[1];
            updateComponents();
        } else {
            wwjPanel.displayCatalogFiles(false);
            updateComponents();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCheckBox1 = new javax.swing.JCheckBox();
        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        startDateLabel = new javax.swing.JLabel();
        endDateLabel = new javax.swing.JLabel();
        jFormattedTextField2 = new javax.swing.JFormattedTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jSlider1 = new javax.swing.JSlider();
        jSlider2 = new javax.swing.JSlider();
        jSlider3 = new javax.swing.JSlider();

        setName("Form"); // NOI18N
        addHierarchyListener(new java.awt.event.HierarchyListener() {
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt) {
                formHierarchyChanged(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.geoimage.viewer.core.GeoImageViewer.class).getContext().getActionMap(CatalogPanel.class, this);
        jCheckBox1.setAction(actionMap.get("displayCatalogFiles")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.geoimage.viewer.core.GeoImageViewer.class).getContext().getResourceMap(CatalogPanel.class);
        jCheckBox1.setText(resourceMap.getString("jCheckBox1.text")); // NOI18N
        jCheckBox1.setName("jCheckBox1"); // NOI18N

        jFormattedTextField1.setText(resourceMap.getString("jFormattedTextField1.text")); // NOI18N
        jFormattedTextField1.setName("jFormattedTextField1"); // NOI18N
       // jFormattedTextField1.setMaximumSize(new Dimension(getWidth()/2, 100));

        startDateLabel.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        startDateLabel.setName("jLabel1"); // NOI18N

        endDateLabel.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        endDateLabel.setName("jLabel2"); // NOI18N

        jFormattedTextField2.setText(resourceMap.getString("jFormattedTextField2.text")); // NOI18N
        jFormattedTextField2.setName("jFormattedTextField2"); // NOI18N
      //  jFormattedTextField2.setMaximumSize(new Dimension(getWidth()/2, 100));

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setName("jTable1"); // NOI18N
        jScrollPane1.setViewportView(jTable1);
        jScrollPane1.setSize(getWidth(), getHeight()/2);
        jTable1.setSize(getWidth(), getHeight()/2);

        jSlider1.setMaximum(1000);
        jSlider1.setValue(0);
        jSlider1.setName("jSlider1"); // NOI18N
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });

        jSlider2.setMaximum(1000);
        jSlider2.setValue(100);
        jSlider2.setName("jSlider2"); // NOI18N
        jSlider2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider2StateChanged(evt);
            }
        });

        jSlider3.setMaximum(1000);
        jSlider3.setName("jSlider3"); // NOI18N
        jSlider3.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider3StateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(startDateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(endDateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jFormattedTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jSlider3, 0, 0, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jSlider2, 0, 0, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGap(10, 10, 10)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jSlider1, javax.swing.GroupLayout.Alignment.TRAILING)//, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                                .addComponent(jScrollPane1)))))//, 0, 0, Short.MAX_VALUE)))))
                .addContainerGap(335, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1)//, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider1)//, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startDateLabel)
                    .addComponent(jFormattedTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSlider2)//, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(endDateLabel)//, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSlider3)//, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(68, 68, 68))
        );
    }// </editor-fold>//GEN-END:initComponents

private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
if (entered) {
        return;
    }
    jSlider3.setMaximum(jSlider2.getMaximum() - jSlider2.getValue() + jSlider1.getValue());
    updateComponents();
}//GEN-LAST:event_jSlider1StateChanged

private void jSlider2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider2StateChanged
if (entered) {
        return;
    }
    jSlider3.setMaximum(jSlider2.getMaximum() - jSlider2.getValue() + jSlider1.getValue());
    updateComponents();
}//GEN-LAST:event_jSlider2StateChanged

private void jSlider3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider3StateChanged
if (entered) {
        return;
    }
    entered = true;
    slideval = jSlider3.getValue();
    jSlider1.setValue(jSlider1.getValue() + (jSlider3.getValue() - lastSlideValue));
    jSlider2.setValue(jSlider2.getValue() + (jSlider3.getValue() - lastSlideValue));
    lastSlideValue = jSlider3.getValue();
    updateComponents();
    entered = false;
}//GEN-LAST:event_jSlider3StateChanged

private void formHierarchyChanged(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_formHierarchyChanged
// TODO add your handling code here:
}//GEN-LAST:event_formHierarchyChanged

    @Action
    public void displayCatalogFiles() {
        updatecatalogDisplay();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JFormattedTextField jFormattedTextField2;
    private javax.swing.JLabel startDateLabel;
    private javax.swing.JLabel endDateLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JSlider jSlider2;
    private javax.swing.JSlider jSlider3;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

}
