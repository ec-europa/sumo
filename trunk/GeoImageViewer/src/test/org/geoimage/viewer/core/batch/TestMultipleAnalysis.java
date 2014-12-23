package org.geoimage.viewer.core.batch;



public class TestMultipleAnalysis {

	
	public void testSumoMultipleAnalysis() {
		System.out.println("Start SUMO in batch mode for multiple analysis");
		String[] args= {"-d","C:\\tmp\\input","-gconf","C:\\tmp\\output\\analysis.conf"};
		Sumo.main(args); 
	}

	
	
	public static void main(String[] args){
		new TestMultipleAnalysis().testSumoMultipleAnalysis();
	}
	
}
