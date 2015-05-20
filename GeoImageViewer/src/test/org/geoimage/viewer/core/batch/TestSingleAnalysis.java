package org.geoimage.viewer.core.batch;



public class TestSingleAnalysis {

	
	public void testSumoMultipleAnalysis() {
		System.out.println("Start SUMO in batch mode for Single analysis");
		String[] args= {"-i","C:\\tmp\\input\\CSKS4_SCS_B_WR_03_HH_RD_SF_20140402210230_20140402210244\\CSKS4_SCS_B_WR_03_HH_RD_SF_20140402210230_20140402210244.h5",
				"-thh","1.5","-thv","1.5","-tvh","1.5","-tvv","1.5","-sh","C:\\tmp\\land polygon opsm 50m","-o","C:\\tmp\\output"};
		Sumo.main(args); 
	}

	
	
	public static void main(String[] args){
		new TestSingleAnalysis().testSumoMultipleAnalysis();
	}
	
}
