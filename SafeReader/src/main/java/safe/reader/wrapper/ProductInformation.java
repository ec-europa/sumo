package safe.reader.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProductInformation {
	//we use a list because some informations have multiple values 
	private HashMap<String, List<String>> productInformation;
	
	public static final String PRODUCT_TYPE="productType";
	public static final String INSTRUMENT_CONF_ID="instrumentConfigurationID";
	public static final String MISSION_DATA_ID="missionDataTakeID";
	public static final String TRANSMIT_RECEIVER_POLAR="transmitterReceiverPolarisation";
	public static final String PRODUCT_TIME_LINES_CAT="productTimelinessCategory";
	public static final String SLICE_PRODUCT_FLAG="sliceProductFlag";
	

	public ProductInformation(){
		productInformation=new HashMap<String, List<String>>();
	}
	
	/**
	 * return the values for this attribute information
	 * @param info
	 * @return
	 */
	public List<String> getValueInfo(String info){
		return productInformation.get(info);
	}
	
	
	/**
	 * Put a new value at the end of the list for this attribute information <info> 
	 * 
	 * @param info
	 * @param value
	 */
	public void putValueInfo(String info,String value){
		List<String> values=productInformation.get(info);
		
		
		if(values==null)
			values=new ArrayList<String>();
		
		values.add(info);
		productInformation.put(info,values);
	}
	
	
}
