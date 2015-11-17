package org.geoimage.impl.alos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;

public class AlosProperties extends Properties {
    private static org.slf4j.Logger logger=LoggerFactory.getLogger(AlosProperties.class);

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
    
    
    
    		/*="PROCESS:JAPAN-JAXA-ALOS2-EICS  20151105 053647"
    		Scs_SceneShift="0"
    		Pds_ResamplingMethod="CC"
    		Pds_UTM_ZoneNo="37"
    		Pds_OrbitDataPrecision="Precision"
    		Pds_AttitudeDataPrecision="Onboard"
    		Img_SceneCenterDateTime="20150422 09:02:36.377"
    		Img_SceneStartDateTime="20150422 09:02:31.377"
    		Img_SceneEndDateTime="20150422 09:02:41.377"
    		Img_ImageSceneCenterLatitude="-4.216"
    		Img_ImageSceneCenterLongitude="39.920"
    		Img_FrameSceneCenterLatitude="-4.218"
    		Img_FrameSceneCenterLongitude="39.929"
    		Img_FrameSceneLeftTopLatitude="-3.843"
    		Img_FrameSceneLeftTopLongitude="39.677"
    		Img_FrameSceneRightTopLatitude="-3.979"
    		Img_FrameSceneRightTopLongitude="40.312"
    		Img_FrameSceneLeftBottomLatitude="-4.456"
    		Img_FrameSceneLeftBottomLongitude="39.547"
    		Img_FrameSceneRightBottomLatitude="-4.592"
    		Img_FrameSceneRightBottomLongitude="40.182"
    		Img_OffNadirAngle="32.9"
    		Pdi_ProductDataSize="493.6"
    		Pdi_CntOfL15ProductFileName="5"
    		Ach_TimeCheck="GOOD"
    		Ach_AttitudeCheck="GOOD"
    		Ach_AbsoluteNavigationStatus=""
    		Ach_HouseKeepingDataCheck="GOOD"
    		Ach_OrbitCheck="GOOD"
    		Ach_OnBoardAttitudeCheck="GOOD"
    		Ach_LossLines="GOOD"
    		Ach_AbsoluteNavigationTime=""
    		Ach_PRF_Check=""
    		Ach_CalibrationDataCheck=""
    		Rad_PracticeResultCode="GOOD"
    		Lbi_Satellite="ALOS2"
    		Lbi_Sensor="SAR"
    		Lbi_ProcessLevel="1.5"
    		Lbi_ProcessFacility="EICS"
    		Lbi_ObservationDate="20150422"*/

    
	
	private File propFile=null;
	private List<String> imageNames;
	private List<String> polarizations;
	
	public AlosProperties(String propFile){
		this.propFile=new File(propFile);
		try {
			load(new FileInputStream(propFile));
			init();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public AlosProperties(File propFile){
		this.propFile=propFile;
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
	
	
	public void load(FileInputStream fis) throws IOException {
		Scanner in = new Scanner(fis);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream is=null;
		try{
	        while(in.hasNext()) {
	            out.write(in.nextLine().replace("\"","").getBytes());
	            out.write("\n".getBytes());
	        }
	        is = new ByteArrayInputStream(out.toByteArray());
	        super.load(is);
		}finally{    
	        is.close();
	        out.close();
	        in.close();
		}    
    }
	
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
	 * return the list of the image names
	 * @return
	 */
	public List<String> getImageNames(){
		return this.imageNames;
	}
	
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
	
	public Coordinate getCenter(){
		
		String lat=getProperty(PROP_CENTER_LAT);
		String lon=getProperty(PROP_CENTER_LON);
		Coordinate coor=new Coordinate(Double.parseDouble(lon),Double.parseDouble(lat));
		return coor;
	}	
	
	
	public static void main(String[] args){
		AlosProperties aa=new AlosProperties("F:/SumoImgs/AlosTrialTmp/SM/0000054535_001001_ALOS2051343700-150506/summary.txt");
		aa.getCorners();
		
	}
	
}
