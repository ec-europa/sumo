package org.geoimage.viewer.core.io;


public class WKTString {

	public static final String WKT3995="PROJCS[\"Stereographic_North_Pole\","
			+ "    GEOGCS[\"GCS_WGS_1984\","
			+ "        DATUM[\"WGS_1984\","
			+ "            SPHEROID[\"WGS_84\",6378137,298.257223563]],"
			+ "        PRIMEM[\"Greenwich\",0],"
			+ "        UNIT[\"Degree\",0.017453292519943295]],"
			+ "    PROJECTION[\"Polar_Stereographic\"],"
			+ "    PARAMETER[\"latitude_of_origin\",60],"
			+ "    PARAMETER[\"central_meridian\",-80],"
			+ "    PARAMETER[\"false_easting\",0],"
			+ "    PARAMETER[\"false_northing\",0],"
			+ "    UNIT[\"Meter\",1]]"
			+ ""; 
	
	public static final String WKTWGS84 = ""
			+ "	GEOGCS[\"WGS 84\","
			+ "	    DATUM[\"WGS_1984\","
			+ "	        SPHEROID[\"WGS 84\",6378137,298.257223563,"
			+ "	            AUTHORITY[\"EPSG\",\"7030\"]],"
			+ "	        AUTHORITY[\"EPSG\",\"6326\"]],"
			+ "	    PRIMEM[\"Greenwich\",0,"
			+ "	        AUTHORITY[\"EPSG\",\"8901\"]],"
			+ "	    UNIT[\"degree\",0.01745329251994328,"
			+ "	        AUTHORITY[\"EPSG\",\"9122\"]],"
			+ "	    AUTHORITY[\"EPSG\",\"4326\"]]\"\""
			+ "			+ ";
}
