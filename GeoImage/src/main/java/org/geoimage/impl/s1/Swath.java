/*
 * 
 */
package org.geoimage.impl.s1;

import java.util.List;

import jrc.it.annotation.reader.jaxb.SwathBoundsType;

public class Swath {
	/*
	 * <swath>IW1</swath>
        <azimuthTime>2014-06-07T20:51:20.861926</azimuthTime>
        <firstLineSensingTime>2014-06-07T20:50:57.387901</firstLineSensingTime>
        <lastLineSensingTime>2014-06-07T20:51:27.937704</lastLineSensingTime>
        <prf>1.717128973878037e+03</prf>
	 */
	private String name=null;
	private long azimuthTime=0;
	private long firstLineSensingTime=0;
	private long lastLineSensingTime=0;
	private double prf;
	private List<SwathBoundsType> bounds=null;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getAzimuthTime() {
		return azimuthTime;
	}

	public void setAzimuthTime(long azimuthTime) {
		this.azimuthTime = azimuthTime;
	}

	public long getFirstLineSensingTime() {
		return firstLineSensingTime;
	}

	public void setFirstLineSensingTime(long firstLineSensingTime) {
		this.firstLineSensingTime = firstLineSensingTime;
	}

	public long getLastLineSensingTime() {
		return lastLineSensingTime;
	}

	public void setLastLineSensingTime(long lastLineSensingTime) {
		this.lastLineSensingTime = lastLineSensingTime;
	}

	public double getPrf() {
		return prf;
	}

	public void setPrf(double prf) {
		this.prf = prf;
	}

	public List<SwathBoundsType> getBounds() {
		return bounds;
	}

	public void setBounds(List<SwathBoundsType> bounds) {
		this.bounds = bounds;
	}

	
	
	
	
	
}
