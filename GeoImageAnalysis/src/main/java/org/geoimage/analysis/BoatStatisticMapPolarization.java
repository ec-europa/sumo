/*
 * 
 */
package org.geoimage.analysis;

import java.util.HashMap;
import java.util.Map;


public class BoatStatisticMapPolarization {
	class Statistics{
		protected int maxValue=0;
		protected double tileAvg=0;
		protected double tileStd=0;
		protected double threshold=0;
		protected double significance=0;
	}
	
	Map<String, Statistics> statisticsBoatForPolarization=null;
	private String[] pols={"HH","HV","VH","VV"};
	
	public BoatStatisticMapPolarization(){
		init();
	}
	private void init(){
		statisticsBoatForPolarization=new HashMap<String, Statistics>();
		statisticsBoatForPolarization.put("HH",new Statistics());
		statisticsBoatForPolarization.put("HV",new Statistics());
		statisticsBoatForPolarization.put("VH",new Statistics());
		statisticsBoatForPolarization.put("VV",new Statistics());
		//statisticsBoatForPolarization.put("merge",new Statistics());
	}
	
	public int getMaxValue(String polarization) {
		return this.statisticsBoatForPolarization.get(polarization).maxValue;
	}

	public void setMaxValue(int value,String polarization) {
		this.statisticsBoatForPolarization.get(polarization).maxValue=value;
	}
	
	public int[] getAllMaxValue() {
		int[] maxs=new int[4];
		int i=0;
		for(String k:this.pols){
			Statistics s=this.statisticsBoatForPolarization.get(k);
			maxs[i]=s.maxValue;
			i++;
		}
		return maxs;
	}

	

	public double getTileAvg(String polarization) {
		return this.statisticsBoatForPolarization.get(polarization).tileAvg;
	}

	public void setTileAvg(double tileAvg,String polarization) {
		this.statisticsBoatForPolarization.get(polarization).tileAvg=tileAvg;
	}

	
	public double[] getAllTileAvg() {
		double[] tAvg=new double[4];
		int i=0;
		for(String k:pols){
			Statistics s=this.statisticsBoatForPolarization.get(k);
			tAvg[i]=s.tileAvg;
			i++;
		}
		return tAvg;
	}
	
	public double getTileStd(String polarization) {
		return this.statisticsBoatForPolarization.get(polarization).tileStd;
	}

	public void setTileStd(double tileStd,String polarization) {
		this.statisticsBoatForPolarization.get(polarization).tileStd=tileStd;
	}

	
	public double[] getAllTileStd() {
		double[] tStd=new double[4];
		int i=0;
		for(String k:pols){
			Statistics s=this.statisticsBoatForPolarization.get(k);
			tStd[i]=s.tileStd;
			i++;
		}
		return tStd;
	}
	
	
	public double getTileThreshold(String polarization) {
		return this.statisticsBoatForPolarization.get(polarization).threshold;
	}

	public void setTreshold(double tthreshold,String polarization) {
		this.statisticsBoatForPolarization.get(polarization).threshold=tthreshold;
	}
	
	public double[] getAllTrhesh() {
		double[] thres=new double[4];
		int i=0;
		for(String k:pols){
			Statistics s=this.statisticsBoatForPolarization.get(k);
			thres[i]=s.threshold;
			i++;
		}
		return thres;
	}
	
	
	public double getSignificance(String polarization) {
		return this.statisticsBoatForPolarization.get(polarization).significance;
	}

	public void setSignificance(double significance,String polarization) {
		this.statisticsBoatForPolarization.get(polarization).significance=significance;
	}
	
	public double[] getAllSignificance() {
		double[] sign=new double[4];
		int i=0;
		for(String k:pols){
			Statistics s=this.statisticsBoatForPolarization.get(k);
			sign[i]=s.significance;
			i++;
		}
		return sign;
	}
}
