package org.geoimage.viewer.core.batch.listener;

public interface BachListener {
	public void notifyError(BatchEvent event);
	public void notifyStartAnalysis(BatchEvent event);
	public void notifyEndAnalysis(BatchEvent event);

}
