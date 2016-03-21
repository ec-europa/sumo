/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.geoimage.viewer.core.api.iactions.ISumoAction;
import org.geoimage.viewer.widget.dialog.ActionDialog;

/**
 *
 * @author leforth
 */
public abstract class SumoAbstractAction extends AbstractAction implements ISumoAction {
	protected String name=null;
	protected String absolutePath=null;
	protected Map<String,String> paramsAction;
	protected boolean done=false;

	//used for the progress bar
	protected int  actionSteps=-1;

	protected List<SumoActionListener> actionListeners=null ;


	@Override
	public abstract boolean execute();

	public Map<String, String> getParamsAction() {
		return paramsAction;
	}

	public boolean isDone(){
		return done;
	}


	public void setParamsAction(Map<String, String> paramsAction) {
		this.paramsAction = paramsAction;
	}


	public SumoAbstractAction(String name,String path){
		absolutePath=path;
		this.name=name;
		actionListeners=new ArrayList<>();
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

        if (getArgumentTypes() != null) {
            ActionDialog dialog=new ActionDialog(JFrame.getFrames()[0], true, this.getArgumentTypes());
            dialog.setVisible(true);
            boolean ok=dialog.isOk();
            if(ok){
            	paramsAction=dialog.getActionArgs();
            	dialog.dispose();
            }else{
            	return;
            }
        }
        Thread t=new Thread(() -> execute());
        t.start();
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


	public void addSumoActionListener(SumoActionListener listener){
		actionListeners.add(listener);
	}

	public void removeSumoActionListener(SumoActionListener listener){
		actionListeners.remove(listener);
	}

	public void notifyEvent(SumoActionEvent event){
		for(SumoActionListener list:actionListeners){
			if(event.getEventType()==SumoActionEvent.STARTACTION){
				list.startAction(event.getMessage(),this.actionSteps,this);
			}else if(event.getEventType()==SumoActionEvent.ENDACTION){
				list.stop(this.name,this);
			}else if(event.getEventType()==SumoActionEvent.STOP_ACTION){
				list.stop(this.name,this);
			}else if(event.getEventType()==SumoActionEvent.UPDATE_STATUS){
				list.updateProgress(event.getMessage()!=null?event.getMessage():" ", event.getProgress(), event.getActionSteps());
			}

		}
	}

	public int getActionSteps() {
		return actionSteps;
	}

	public void setActionSteps(int actionSteps) {
		this.actionSteps = actionSteps;
	}

}
