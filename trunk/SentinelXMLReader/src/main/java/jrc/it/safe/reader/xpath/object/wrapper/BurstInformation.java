package jrc.it.safe.reader.xpath.object.wrapper;

import java.util.List;

import jrc.it.annotation.reader.jaxb.Burst;
import jrc.it.annotation.reader.jaxb.SwathTiming;

public class BurstInformation {
	private int  linePerBust;
	private List<Burst> burstList;
	private int samplePerBust;
	private int count;
	
	


	public BurstInformation(SwathTiming  swat){
		linePerBust=swat.getLinesPerBurst().intValue();
		burstList=swat.getBurstList().getBurst();
		count=swat.getBurstList().getCount().intValue();
		samplePerBust=swat.getSamplesPerBurst().intValue();
		
	}
	
	public int getLinePerBust() {
		return linePerBust;
	}


	public void setLinePerBust(int linePerBust) {
		this.linePerBust = linePerBust;
	}


	public List<Burst> getBurstList() {
		return burstList;
	}


	public void setBurstList(List<Burst> burstList) {
		this.burstList = burstList;
	}


	public int getSamplePerBust() {
		return samplePerBust;
	}


	public void setSamplePerBust(int samplePerBust) {
		this.samplePerBust = samplePerBust;
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
