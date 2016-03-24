/*
 * 
 */
package jrc.it.safe.reader.xpath.object.wrapper;

import java.util.List;

import jrc.it.annotation.reader.jaxb.BurstType;
import jrc.it.annotation.reader.jaxb.L1SwathType;

public class BurstInformation {
	private int  linePerBust;
	private List<BurstType> burstList;
	private int samplePerBust;
	private long count;
	
	


	public BurstInformation(L1SwathType  swat){
		linePerBust=swat.getLinesPerBurst().getValue().intValue();
		burstList=swat.getBurstList().getBurst();
		count=swat.getBurstList().getCount();
		samplePerBust=swat.getSamplesPerBurst().getValue().intValue();
		
	}
	
	public int getLinePerBust() {
		return linePerBust;
	}


	public void setLinePerBust(int linePerBust) {
		this.linePerBust = linePerBust;
	}


	public List<BurstType> getBurstList() {
		return burstList;
	}


	public void setBurstList(List<BurstType> burstList) {
		this.burstList = burstList;
	}


	public int getSamplePerBust() {
		return samplePerBust;
	}


	public void setSamplePerBust(int samplePerBust) {
		this.samplePerBust = samplePerBust;
	}
	
	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}
