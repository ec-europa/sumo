package org.geoimage.viewer.core.layers;

public class SumoActionEvent {
	public static final int STARTACTION=0;
	public static final int UPDATE_STATUS=1;
	public static final int ENDACTION=2;
	public static final int STOP_ACTION=99;
	public static final int ACTION_ERROR=100;


	private String message;
	private int progress=0;
	private int actionSteps=0;



	private int eventType;


	/**
	 *
	 * @param eventType
	 * @param msg
	 * @param progress
	 */
	public SumoActionEvent(int eventType,String msg,int progress) {
		this.message=msg;
		this.progress=progress;
		this.eventType=eventType;
	}

	/**
	 *
	 * @param eventType
	 * @param msg
	 * @param progress
	 */
	public SumoActionEvent(int eventType,String msg,int progress,int numSteps) {
		this.eventType=eventType;
		this.message=msg;
		this.progress=progress;
		this.actionSteps=numSteps;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public int getProgress() {
		return progress;
	}


	public void setProgress(int progress) {
		this.progress = progress;
	}


	public int getEventType() {
		return eventType;
	}


	public void setEventType(int eventType) {
		this.eventType = eventType;
	}

	public int getActionSteps() {
		return actionSteps;
	}


	public void setActionSteps(int actionSteps) {
		this.actionSteps = actionSteps;
	}
}
