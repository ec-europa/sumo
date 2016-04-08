/*
 * 
 */
package org.geoimage.impl.alos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.geoimage.impl.imgreader.BinaryReader;

/*
1 File descriptor         720 
2 Data set summary        4096
3 Map projection data     1620
4 Platform position data  4680
5 Attitude data           16384
6 Radiometric data        9860
7 Data quality summary    1620
8 Calibration data        13212
9 Facility related        Variable
*/

public class LedMetadataReader {
	private  final int[] FIELD_14_File_ID=new int[]{720+48,16};
	private  final int[] FIELD_1_REC_NUM=new int[]{0,4};
	
	//data set summary
	private  final int[] FIELD_PRF=new int[]{720+934,15};
	private  final int[] FIELD_INCIDENCE_CENTER=new int[]{720+484,7};
	private  final int[] FIELD_RADAR_WAVELENGTH=new int[]{720+500,15};
	
	//map project 
	private  final int offset_map_prj=720+4096;
	private  final int[] FIELD_ORBIT_INC=new int[]{offset_map_prj+140,15};
	private  final int[] FIELD_ALTITUDE=new int[]{offset_map_prj+189,15};
	
	//data quality
	private  final int offset_data_quality=720+4096+1620+4680+16384+9860;
	private  final int[] FIELD_AZ_AMB_AAR=new int[]{offset_data_quality+62,15};
	private  final int[] FIELD_REC_SEQ_NUM_AMB_AAR=new int[]{offset_data_quality+0,4};
	private  final int[] FIELD_RANGE_AMB_RAR=new int[]{offset_data_quality+78,15};
	
	
	private BinaryReader reader=null;
	/**
	 * 
	 * @param input
	 * @throws FileNotFoundException
	 */
	public LedMetadataReader(File input) throws FileNotFoundException {
		reader=new BinaryReader(input);
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public int readFileID() throws IOException{
		return reader.readB4(FIELD_14_File_ID[0], FIELD_14_File_ID[1],true);
	}
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public int readRecNum() throws IOException{
		return reader.readB4(8, 4,true);
	}
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public float readPrf()throws IOException{
		String prf=reader.readBytesAsString(FIELD_PRF[0],FIELD_PRF[1]);
		float val=Float.parseFloat(prf)/1000;
		return val;
	}
	
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String readIncidenceCenter()throws IOException{
		String a=reader.readBytesAsString(FIELD_INCIDENCE_CENTER[0],FIELD_INCIDENCE_CENTER[1]);
		return a;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String readRadarWaveLength()throws IOException{
		String a=reader.readBytesAsString(FIELD_RADAR_WAVELENGTH[0],FIELD_RADAR_WAVELENGTH[1]);
		return a;
	}
	
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String readAAR()throws IOException{
		String aar=reader.readBytesAsString(FIELD_AZ_AMB_AAR[0],FIELD_AZ_AMB_AAR[1]);
		return aar;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String readOrbitInc()throws IOException{
		String inc=reader.readBytesAsString(FIELD_ORBIT_INC[0],FIELD_ORBIT_INC[1]);
		return inc;
	}
	

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public float readSatAltitude()throws IOException{
		String a=reader.readBytesAsString(FIELD_ALTITUDE[0],FIELD_ALTITUDE[1]);
		return Float.parseFloat(a);
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public int readRecSeqNumberOfDataQuality()throws IOException{
		int aar=reader.readB4(FIELD_REC_SEQ_NUM_AMB_AAR[0],FIELD_REC_SEQ_NUM_AMB_AAR[1],true);
		return aar;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String readRangeAmbig()throws IOException{
		String rar=reader.readBytesAsString(FIELD_RANGE_AMB_RAR[0],FIELD_RANGE_AMB_RAR[1]);
		return rar;
	}
	
	
	/**
	 * 
	 * @throws IOException
	 */
	public void readDataSetSummary() throws IOException{
		String sceneid=reader.readBytesAsString((720+20),32);
		System.out.println(":"+sceneid);
		
		int first=reader.readB4(719,4,false);
		System.out.println(":"+first);
		
		String ellipse=reader.readBytesAsString((719+165),16);
		System.out.println(":"+ellipse);
		
		float prf=readPrf();
		System.out.println(":"+prf);
		
		
	}
	
	
	public static void main(String[]args){
		try {
			LedMetadataReader lr=new LedMetadataReader(new File(
					"H:////Radar-Images////AlosTrial////Alos2////WBD////PON_000000476_0000060609////LED-ALOS2029163650-141207-WBDR1.5RUD"));
			//LedMetadataReader lr=new LedMetadataReader(new File(
			///		"H://sat//AlosTrialTmp//SM//0000054534_001001_ALOS2049273700-150422//LED-ALOS2049273700-150422-FBDR1.5RUD"));
			System.out.println(":"+lr.readIncidenceCenter());
			System.out.println(":"+lr.readRadarWaveLength());
			System.out.println(":"+lr.readSatAltitude());
			System.out.println(":"+lr.readRangeAmbig());
			System.out.println(":"+lr.readOrbitInc());
			System.out.println(":"+lr.readPrf());
			System.out.println(":"+lr.readFileID());
			System.out.println(":"+lr.readAAR());
			System.out.println(":"+lr.readRecSeqNumberOfDataQuality());
			
			
			/*
			System.out.println(":"+val);
			
			String xx=lr.readString(61, 16);
			System.out.println(":"+xx);
			
			xx=lr.readString(33, 12);
			System.out.println(":"+xx);
			
			xx=lr.readString(48, 16);
			System.out.println(":"+xx);
			
			xx=lr.readString(64, 4);
			System.out.println(":"+xx);
			
			int aa=lr.readField6();
			System.out.println(":"+aa);*/
			
			//lr.readDataSetSummary();
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
