package org.geoimage.viewer.core.analysisproc;

public interface VDSAnalysisProcessListener {
	
	public void startAnalysis();
	public void startAnalysisBand(String message);
	public void calcAzimuthAmbiguity(String message);
	public void agglomerating(String message);
	public void endAnalysis();
	
	
	
	
}
