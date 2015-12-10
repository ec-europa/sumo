/*
 * ActionDialog.java
 *
 * Created on April 1, 2008, 11:09 AM
 */
package org.geoimage.viewer.widget.dialog;

import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.geoimage.viewer.core.SumoPlatform;
import org.jrc.sumo.core.api.Argument;
import org.jrc.sumo.core.api.iactions.IAction;
import org.slf4j.LoggerFactory;

/**
 *
 * @author  thoorfr
 */
public class ActionDialog extends javax.swing.JDialog {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(ActionDialog.class);

    private IAction action;
    private List<JComponent> components = new Vector<JComponent>();

    /** Creates new form ActionDialog */
    public ActionDialog(java.awt.Frame parent, boolean modal, IAction action) {
        super(parent, modal);
        this.action = action;
        List<Argument> args = action.getArgumentTypes();
        initComponents();
        if (args != null) {
            argPanel.setLayout(new GridLayout(args.size(), 2));
            for (Argument arg : args) {
                if (arg.getType() == Argument.STRING) {
                    if (arg.getPossibleValues() == null) {
                        JLabel label = new JLabel(arg.getName());
                        JTextField tf = new JTextField("" + arg.getDefaultValue());
                        components.add(tf);
                        argPanel.add(label);
                        argPanel.add(tf);
                    } else {
                        JLabel label = new JLabel(arg.getName());
                        JComboBox cb = new JComboBox(arg.getPossibleValues());
                        components.add(cb);
                        argPanel.add(label);
                        argPanel.add(cb);
                    }
                }/* else if (arg.getType() == Argument.DATE) {
                    JLabel label = new JLabel(arg.getName());
                    DateControl tf = new DateControl();
                    tf.setDateType(Consts.TYPE_DATE_TIME);
                    components.add(tf);
                    argPanel.add(label);
                    argPanel.add(tf);
                } */else if (arg.getType() == Argument.BOOLEAN) {
                    JCheckBox cb = new JCheckBox(arg.getName());
                    argPanel.add(new JLabel());
                    components.add(cb);
                    argPanel.add(cb);
                } else if (arg.getType() == Argument.DIRECTORY) {
                    JLabel label = new JLabel(arg.getName());
                    final JTextField tf = new JTextField("" + arg.getDefaultValue());
                    JButton b = new JButton("Choose...");
                    b.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            JFileChooser fc = new JFileChooser();
                            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                try {
                                    tf.setText(fc.getSelectedFile().getCanonicalPath());
                                } catch (IOException ex) {
                                	logger.error(ex.getMessage(),ex);
                                }
                            }
                        }
                    });

                    components.add(tf);
                    argPanel.add(label);
                    argPanel.add(tf);
                    argPanel.add(new Label());
                    argPanel.add(b);
                    GridLayout gl = (GridLayout) argPanel.getLayout();
                    gl.setRows(gl.getRows() + 1);
                } else if (arg.getType() == Argument.FILE) {
                    JLabel label = new JLabel(arg.getName());
                    final JTextField tf = new JTextField("" + arg.getDefaultValue());
                    JButton b = new JButton("Choose...");
                    b.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            JFileChooser fc = new JFileChooser();
                            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                try {
                                    tf.setText(fc.getSelectedFile().getCanonicalPath());
                                } catch (IOException ex) {
                                	logger.error(ex.getMessage(),ex);                                }
                            }
                        }
                    });

                    components.add(tf);
                    argPanel.add(label);
                    argPanel.add(tf);
                    argPanel.add(new Label());
                    argPanel.add(b);
                    GridLayout gl = (GridLayout) argPanel.getLayout();
                    gl.setRows(gl.getRows() + 1);
                } else {
                    JLabel label = new JLabel(arg.getName());
                    JTextField tf = new JTextField("" + arg.getDefaultValue());
                    components.add(tf);
                    argPanel.add(label);
                    argPanel.add(tf);
                }
            }
            pack();
            setBounds(getX(), getY(), getWidth(), getHeight() + 65 * args.size() - argPanel.getHeight());

        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        argPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Action Parameters");

        jButton1.setText("OK");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        argPanel.setLayout(new java.awt.GridLayout(2, 2));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(argPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(argPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        this.setVisible(false);
        try {
            String[] args = new String[components.size() + 1];
            args[0] = action.getName();
            int i = 1;
            for (JComponent c : components) {
                if (c instanceof JTextField) {
                    args[i++] = ((JTextField) c).getText();
                } else if (c instanceof JComboBox) {
                    args[i++] = ((JComboBox) c).getSelectedItem() + "";
                }
                else if (c instanceof JCheckBox) {
                    args[i++] = ((JCheckBox) c).isSelected() + "";
                }
                /* else if (c instanceof DateControl) {
                    args[i++] = new Timestamp(((DateControl) c).getDate().getTime()).toString();
                }*/
            }
            SumoPlatform.getApplication().getConsoleLayer().execute(args);
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);      
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel argPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    // End of variables declaration//GEN-END:variables
}
