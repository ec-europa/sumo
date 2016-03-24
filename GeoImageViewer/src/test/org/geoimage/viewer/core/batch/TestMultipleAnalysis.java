/*
 * 
 */
package org.geoimage.viewer.core.batch;



public class TestMultipleAnalysis {

	
	public void testSumoMultipleAnalysis() {
		String[] args= {"-gconf","C:\\tmp\\output\\analysis.conf"};
		Sumo.main(args); 
	}

	
	
	
	
	public static void main(String[] args){
		System.out.println("Start SUMO in batch mode for multiple analysis");
		new TestMultipleAnalysis().testSumoMultipleAnalysis();
	}
	
}
