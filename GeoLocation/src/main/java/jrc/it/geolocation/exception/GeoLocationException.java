package jrc.it.geolocation.exception;

public class GeoLocationException extends Exception{
	public static final String MSG_CONV_FROM_GEO_TO_PIXEL="Problem converting from GEO coordinates to Pixels ";
	public static final String MSG_CONV_FROM_PIXEL_TO_GEO="Problem converting from pixels coordinates to Pixels ";
	
	
	public GeoLocationException(String msg){
		super(msg);
	}

}
