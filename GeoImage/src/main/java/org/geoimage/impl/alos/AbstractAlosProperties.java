/*
 * 
 */
package org.geoimage.impl.alos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;

public abstract class AbstractAlosProperties extends Properties {
    private static org.slf4j.Logger logger=LoggerFactory.getLogger(AbstractAlosProperties.class);

    public static final String PROP_ODI_SCENEID="Odi_SceneId";
    public static final String PROP_ODI_SITE_DATETIME="Odi_SiteDateTime";
    public static final String PROP_SCS_SCENE_ID="Scs_SceneID";
    public static final String PROP_PDS_PRODUCT_ID="Pds_ProductID";
    public static final String PROP_PIXEL_SPACING="Pds_PixelSpacing";
    public static final String PROP_L1_PRODUCT_FILE_NAMES="Pdi_L15ProductFileName";
		   /* Pdi_L15ProductFileName01="VOL-ALOS2049273700-150422-FBDR1.5RUD"
			Pdi_L15ProductFileName02="LED-ALOS2049273700-150422-FBDR1.5RUD"
			Pdi_L15ProductFileName03="IMG-HH-ALOS2049273700-150422-FBDR1.5RUD"
			Pdi_L15ProductFileName04="IMG-HV-ALOS2049273700-150422-FBDR1.5RUD"
			Pdi_L15ProductFileName05="TRL-ALOS2049273700-150422-FBDR1.5RUD"*/

    
    public static final String PROP_LEFT_TOP_LAT="Img_ImageSceneLeftTopLatitude";
    public static final String PROP_LEFT_TOP_LON="Img_ImageSceneLeftTopLongitude";
    public static final String PROP_RIGHT_TOP_LAT="Img_ImageSceneRightTopLatitude";
    public static final String PROP_RIGHT_TOP_LON="Img_ImageSceneRightTopLongitude";
    public static final String PROP_LEFT_BOT_LAT="Img_ImageSceneLeftBottomLatitude";
    public static final String PROP_LEFT_BOT_LON="Img_ImageSceneLeftBottomLongitude";
    public static final String PROP_RIGHT_BOT_LAT="Img_ImageSceneRightBottomLatitude";
    public static final String PROP_RIGHT_BOT_LON="Img_ImageSceneRightBottomLongitude";
    public static final String PROP_CENTER_LAT="Img_ImageSceneCenterLatitude";
    public static final String PROP_CENTER_LON="Img_ImageSceneCenterLongitude";
    
    public static final String PROP_BIT_X_PIX="Pdi_BitPixel";
    public static final String PROP_N_PIXELS="Pdi_NoOfPixels_0";
    public static final String PROP_N_LINES="Pdi_NoOfLines_0";
    public static final String PROP_PROD_FORM="Pdi_ProductFormat";
    
    public static final String PROP_START_DATE_TIME="Img_SceneStartDateTime";
    public static final String PROP_END_DATE_TIME="Img_SceneEndDateTime";
    
	
	private List<String> imageNames;
	private List<String> polarizations;
	private SimpleDateFormat df=new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");//"20150422 09:02:41.377");
	
	/**
	 * 
	 * @param propFile
	 */
	public AbstractAlosProperties(String propFile){
		try {
			load(new FileInputStream(propFile));
			init();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param propFile
	 */
	public AbstractAlosProperties(File propFile){
		try {
			load(new FileInputStream(propFile));
			init();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getPolarizations(){
		return polarizations;
	}
	
	/**
	 * 
	 * @param fis
	 * @throws IOException
	 */
	public abstract void load(FileInputStream fis) throws IOException ;
	
	/**
	 * init some properties
	 */
	private void init(){
		imageNames=new ArrayList<String>();
		polarizations=new ArrayList<String>();
		Enumeration<?> vals=this.propertyNames();
		
		while(vals.hasMoreElements()){
			String propName=(String)vals.nextElement();
			//search the image file names
			if(propName.startsWith(PROP_L1_PRODUCT_FILE_NAMES)){
				String val=(String)get(propName);
				if(val.startsWith("IMG-")){
					imageNames.add(val);
					polarizations.add(val.substring(4,6));
				}
			}
		}
	}
	
	/**
	 * @return
	 */
	public double getPixelSpacing(){
		String val=this.getProperty(PROP_PIXEL_SPACING);
		return Double.parseDouble(val);
		
	}
	
	/**
	 * @return
	 */
	public int getNumberOfLines(){
		String val=this.getProperty(PROP_N_LINES);
		return Integer.parseInt(val);
	}
	
	/**
	 * @return
	 */
	public int getNumberOfPixels(){
		String val=this.getProperty(PROP_N_PIXELS);
		return Integer.parseInt(val);
	}
	
	/**
	 * @return
	 */
	public String getProductFormat(){
		String val=this.getProperty(PROP_PROD_FORM);
		return val;
	}
	/**
	 * 
	 * @return
	 */
	public Date getStartDate(){
		String val=this.getProperty(PROP_START_DATE_TIME);
		Date d;
		try {
			d = df.parse(val);
		} catch (ParseException e) {
			d=new Date(0);
		}
		return d;
	}
	
	/**
	 * 
	 * @return
	 */
	public Date getEndDate(){
		String val=this.getProperty(PROP_END_DATE_TIME);
		Date d;
		try {
			d = df.parse(val);
		} catch (ParseException e) {
			d=new Date(0);
		}
		return d;
	}
	
	/**
	 * return the list of the image names
	 * @return
	 */
	public List<String> getImageNames(){
		return this.imageNames;
	}
	
	/**
	 * 
	 * @return
	 */
	public Coordinate[] getCorners(){
		Coordinate[] coor=new Coordinate[4];
		
		String lat=getProperty(PROP_LEFT_TOP_LAT);
		String lon=getProperty(PROP_LEFT_TOP_LON);
		coor[0]=new Coordinate(Double.parseDouble(lon),Double.parseDouble(lat));
		
		lat=getProperty(PROP_RIGHT_TOP_LAT);
		lon=getProperty(PROP_RIGHT_TOP_LON);
		coor[1]=new Coordinate(Double.parseDouble(lon),Double.parseDouble(lat));
		
		lat=getProperty(PROP_RIGHT_BOT_LAT);
		lon=getProperty(PROP_RIGHT_BOT_LON);
		coor[2]=new Coordinate(Double.parseDouble(lon),Double.parseDouble(lat));
		
		lat=getProperty(PROP_LEFT_BOT_LAT);
		lon=getProperty(PROP_LEFT_BOT_LON);
		coor[3]=new Coordinate(Double.parseDouble(lon),Double.parseDouble(lat));
		
		return coor;
	}
	
	/**
	 * 
	 * @return
	 */
	public Coordinate getCenter(){
		
		String lat=getProperty(PROP_CENTER_LAT);
		String lon=getProperty(PROP_CENTER_LON);
		Coordinate coor=new Coordinate(Double.parseDouble(lon),Double.parseDouble(lat));
		return coor;
	}	
	
}
