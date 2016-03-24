/*
 *
 */
package org.geoimage.viewer.core.analysisproc;

import org.geoimage.viewer.core.api.ilayer.ILayer;

public interface VDSAnalysisProcessListener {

	public void startAnalysis(String imageName);
	public void performVDSAnalysis(String message,int numSteps);
	public void nextVDSAnalysisStep(String message,int step,int numSteps);
	public void startBlackBorederAnalysis(String message);
	public void startAnalysisBand(String message);
	public void calcAzimuthAmbiguity(String message);
	public void agglomerating(String message);
	public void endAnalysis(String imageName);
	public void layerReady(ILayer layer);



}
