package org.geoimage.common;

public class OptionMenu {
	private int optionId;
	private String optionDesc;
	
	
	public OptionMenu(int id,String desc){
		this.optionId=id;
		this.optionDesc=desc;
	}
	
	public int getOptionId() {
		return optionId;
	}
	public void setOptionId(int optionId) {
		this.optionId = optionId;
	}
	public String getOptionDesc() {
		return optionDesc;
	}
	public void setOptionDesc(String optionDesc) {
		this.optionDesc = optionDesc;
	}
	
	@Override
	public String toString(){
		return getOptionDesc();
	}
	
}
