package org.geoimage.viewer.core.batch;

import java.util.ArrayList;
import java.util.List;

import org.geoimage.viewer.core.batch.listener.BachListener;

public class BatchObserver {

	private static BatchObserver instance;
    private List<BachListener> listeners;

    private BatchObserver() {
        listeners = new  ArrayList<BachListener>();
    }

    public static synchronized BatchObserver getInstance() {
        if(instance == null) {
            instance = new BatchObserver();
        }

        return instance;
    }

    public synchronized void addBachListener(BachListener listener) {
        // Check if listener already exists in list, skip if it does
        for (BachListener listListener : listeners) {
            if(listListener.equals(listener)) {
                return;
            }
        }

        listeners.add(listener);
    }

    public synchronized void removeBachListener(BachListener listener) {
    	if(listeners.size()>0)
    		listeners.remove(listener);
    }

    /**
     *
     * @param image
     * @param msg
     */
    public void notifyError(String image,String msg) {
        for (BachListener listener : listeners) {
        }
    }



}
