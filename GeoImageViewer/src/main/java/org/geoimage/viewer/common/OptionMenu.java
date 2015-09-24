package org.geoimage.viewer.common;

public class OptionMenu {
	private int optionId;
	private String optionDesc;
	private String value;
	
	
	public OptionMenu(int id,String desc){
		this.optionId=id;
		this.optionDesc=desc;
		this.value=desc;
	}
	public OptionMenu(int id,String desc,String value){
		this.optionId=id;
		this.optionDesc=desc;
		this.value=value;
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
	public String getValue() {
		return value;
	}
	public void setOptionDesc(String optionDesc) {
		this.optionDesc = optionDesc;
	}
	
	@Override
	public String toString(){
		return getValue();
	}
	
}
