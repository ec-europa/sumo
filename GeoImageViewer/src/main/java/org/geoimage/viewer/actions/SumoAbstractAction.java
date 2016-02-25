/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.geoimage.viewer.core.api.iactions.IAction;
import org.geoimage.viewer.widget.dialog.ActionDialog;

/**
 *
 * @author leforth
 */
public abstract class SumoAbstractAction extends AbstractAction implements IAction {
	private String name=null;
	private String absolutePath=null;


	@Override
	public abstract boolean execute(String[] args);


	public SumoAbstractAction(String name,String path){
		absolutePath=path;
		this.name=name;
	}


    public void errorWindow(String message)
    {
        final String errorMessage = message;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     *
     */
	public void actionPerformed(ActionEvent e) {
		String[] args=null;

        if (getArgumentTypes() != null) {
            ActionDialog dialog=new ActionDialog(JFrame.getFrames()[0], true, this.getArgumentTypes());
            dialog.setVisible(true);
            boolean ok=dialog.isOk();
            if(ok){
            	args=dialog.getActionArgs();
            	dialog.dispose();
            }else{
            	return;
            }
        }
        execute(args);
    }

    public String getName() {
        return name;
    }

    public void setMenuName(String menuName){
		 super.putValue(NAME, menuName);
	}

	/**
	 *
	 */
	public String getPath(){
		return absolutePath;
	}

}
