package jrc.it.safe.reader.xpath.object.wrapper;

public class AcquisitionPeriod {
	/*<safe:acquisitionPeriod>
    <safe:startTime>2013-04-25T18:23:37.712196</safe:startTime>
    <safe:stopTime>2013-04-25T18:24:22.622195</safe:stopTime>
  </safe:acquisitionPeriod>*/
	
	private String startTime;
	private String stopTime;
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getStopTime() {
		return stopTime;
	}
	public void setStopTime(String stopTime) {
		this.stopTime = stopTime;
	}

	
	
	
	
}
