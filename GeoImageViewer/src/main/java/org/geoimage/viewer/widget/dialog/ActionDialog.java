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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.slf4j.LoggerFactory;




/**
 *
 * @author  Pietro Argentieri
 */
public class ActionDialog extends javax.swing.JDialog {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(ActionDialog.class);

    //private IAction action;
    private Map<ActionDialog.Argument,JComponent> components = new HashMap<ActionDialog.Argument,JComponent>();
    private boolean ok=false;
    private Map<String,String> actionArgs=null;
    private String   actionName=null;
    private List<Argument> args = null;


/**
 * the class deals with possible arguments types that can be passed to execute a
 * ConsoleAction. It constructs automatically the GUI according to the settings of this class
 */
 public static class Argument {
    private String name;
    private Class<?> type;
    public final static Class<Integer> INTEGER = java.lang.Integer.class;
    public final static Class<Double> DOUBLE = java.lang.Double.class;
    public final static Class <Float>FLOAT = java.lang.Float.class;
    public final static Class <String>STRING = java.lang.String.class;
    public final static Class <Boolean>BOOLEAN = java.lang.Boolean.class;
    public final static Class <java.util.Date>DATE = java.util.Date.class;
    public final static Class <java.io.File>FILE = java.io.File.class;
    public final static Class <Void>DIRECTORY = Void.class;

    private boolean optional;
    private Object defaultValue;
    private Object[] values;
    private String description;

    private boolean conditional=false;
    private Argument condtionObject=null;
    private Object conditionValue=null;


    public Argument(String name, Class type, boolean optional, Object defaultValue,String description) {
        this.name = name;
        this.type = type;
        this.optional=optional;
        this.defaultValue=defaultValue;
        this.description=description;
    }

    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCondition(Argument condtionObj,Object value){
    	conditional=true;
    	condtionObject=condtionObj;
    	this.conditionValue=value;
    }


    /**
     * Using this method will force the UI to setup a combobox for choosing the values of the argument
     * @param values an array of chooseable values
     */
    public void setPossibleValues(Object[] values){
        this.values=values;
    }

    /**
     *
     * @return the possible values to be picked up
     */
    public Object[] getPossibleValues(){
        return values;
    }

    /**
     *
     * @return the name of the argument like "Threshold"
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the argument like "Threshold"
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * type of the argument, to be selected from the static field of the class,
     * like Argument.DATE for a date. It will trigger the right UI component in the dialog box
     * @return
     */
    public Class getType() {
        return type;
    }

    /**
     * Sets the default value
     * @param value
     */
    public void setValue(Class value) {
        this.type = value;
    }

    /**
     * to indicate if the arguments has to be filled or not
     * @return
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * set true if the argument can be null
     * @param optional
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    /**
     *
     * @return return the default value
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value
     *
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}






    public String getActionName() {
		return actionName;
	}
    /**
     *
     * @param actionName
     */
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	/** Creates new form ActionDialog */
    @SuppressWarnings("rawtypes")
	public ActionDialog(java.awt.Frame parent, boolean modal,List<Argument> arguments) {
        super(parent, modal);
        this.args=arguments;

        initComponents();

        if (args != null) {
            argPanel.setLayout(new GridLayout(args.size(), 2));
            for (Argument arg : args) {

	                if (arg.getType() == Argument.STRING) {
	                    if (arg.getPossibleValues() == null) {
	                        JLabel label = new JLabel(arg.getDescription());
	                        JTextField tf = new JTextField("" + arg.getDefaultValue());
	                        tf.setName(arg.getName());
	                        components.put(arg,tf);
	                        argPanel.add(label);
	                        argPanel.add(tf);
	                    } else {
	                        JLabel label = new JLabel(arg.getDescription());
	                        JComboBox<Object> cb = new JComboBox<Object>(arg.getPossibleValues());
	                        cb.setName(arg.getName());
	                        components.put(arg,cb);
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
	                    JCheckBox cb = new JCheckBox(arg.getDescription());
	                    cb.setName(arg.getName());
	                    argPanel.add(new JLabel());
	                    components.put(arg,cb);
	                    argPanel.add(cb);
	                } else if (arg.getType() == Argument.DIRECTORY) {
	                    JLabel label = new JLabel(arg.getDescription());
	                    final JTextField tf = new JTextField("" + arg.getDefaultValue());
	                    tf.setName(arg.getName());
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

	                    components.put(arg,tf);
	                    argPanel.add(label);
	                    argPanel.add(tf);
	                    argPanel.add(new Label());
	                    argPanel.add(b);
	                    GridLayout gl = (GridLayout) argPanel.getLayout();
	                    gl.setRows(gl.getRows() + 1);
	                } else if (arg.getType() == Argument.FILE) {
	                    JLabel label = new JLabel(arg.getDescription());
	                    final JTextField tf = new JTextField("" + arg.getDefaultValue());
	                    tf.setName(arg.getName());
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

	                    components.put(arg,tf);
	                    argPanel.add(label);
	                    argPanel.add(tf);
	                    argPanel.add(new Label());
	                    argPanel.add(b);
	                    GridLayout gl = (GridLayout) argPanel.getLayout();
	                    gl.setRows(gl.getRows() + 1);
	                } else {
	                    JLabel label = new JLabel(arg.getDescription());
	                    JTextField tf = new JTextField("" + arg.getDefaultValue());
	                    tf.setName(arg.getName());
	                    components.put(arg,tf);
	                    argPanel.add(label);
	                    argPanel.add(tf);
	                }
	        }
            for (Argument arg : args) {
            	if(arg.conditional){
            		JComponent jcControl=components.get(arg);
            		jcControl.setVisible(false);
            		JComponent jc=components.get(arg.condtionObject);
            		if(jc!=null){
            			if(jc instanceof AbstractButton){
            				((AbstractButton)jc).addActionListener(evt -> {
            					if(((AbstractButton)jc).isSelected()){
            						//if(Boolean.parseBoolean((String)arg.conditionValue))
            						jcControl.setVisible(true);
            					}else{
    								jcControl.setVisible(false);
            					}
    						});
            			}
            			if(jc instanceof JComboBox){
            				((JComboBox)jc).addActionListener(evt -> {
            					if(((JComboBox)jc).getSelectedItem().equals(arg.conditionValue)){
            						jcControl.setVisible(true);
            					}else{
    								jcControl.setVisible(false);
            					}
    						});
            			}


            		}
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

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        argPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Action Parameters");

        okButton.setText("OK");
        okButton.addActionListener(evt -> okButtonActionPerformed(evt));

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(evt -> cancelButtonActionPerformed(evt));

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
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(argPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        this.setVisible(false);
        try {
            //String[] args = new String[components.size() ];
            this.actionArgs=new HashMap<>();
            int i = 0;
            for (JComponent c : components.values()) {
                if (c instanceof JTextField) {
                    //args[i++]= ((JTextField) c).getText();
                    actionArgs.put(((JTextField) c).getName(), ((JTextField) c).getText());
                } else if (c instanceof JComboBox) {
                    //args[i++] = ((JComboBox) c).getSelectedItem() + "";
                    actionArgs.put(((JComboBox) c).getName(),  ((JComboBox) c).getSelectedItem() + "");
                }
                else if (c instanceof JCheckBox) {
                   // args[i++] = ((JCheckBox) c).isSelected() + "";
                    actionArgs.put(((JCheckBox) c).getName(), ((JCheckBox) c).isSelected() + "");
                }
                /* else if (c instanceof DateControl) {
                    args[i++] = new Timestamp(((DateControl) c).getDate().getTime()).toString();
                }*/
            }
            ok=true;
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        	ok=false;
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    public Map<String,String> getActionArgs() {
		return actionArgs;
	}


	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.setVisible(false);
        ok=false;
    }//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel argPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton cancelButton;
    // End of variables declaration//GEN-END:variables
}
