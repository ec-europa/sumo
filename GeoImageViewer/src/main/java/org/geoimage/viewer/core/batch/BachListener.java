package org.geoimage.viewer.core.batch;

import java.util.Date;

public interface BachListener {
	public void notifyError(String image, String msg);
	public void notifyStartAnalysis(String image, Date timeStart);
	public void notifyEndAnalysis(String image, Date timeEnd);
	
}
