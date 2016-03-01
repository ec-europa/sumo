/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

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
	protected Map<String,String> paramsAction;




	@Override
	public abstract boolean execute();//String[] args);

	public Map<String, String> getParamsAction() {
		return paramsAction;
	}



	public void setParamsAction(Map<String, String> paramsAction) {
		this.paramsAction = paramsAction;
	}


	public SumoAbstractAction(String name,String path){
		absolutePath=path;
		this.name=name;
	}

	public String getParamValue(String paramName){
		return paramsAction.get(paramName);
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
		Map<String,String> args=null;

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
        execute();
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

	@Override
	public List<ActionDialog.Argument> getArgumentTypes() {
		return null;
	}

}
