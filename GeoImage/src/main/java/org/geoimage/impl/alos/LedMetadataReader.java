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
5 Attitude data           8192
6 Radiometric data        9860
7 Data quality summary    1620
8 Calibration data        13212
9 Facility related        Variable
*/

public class LedMetadataReader {
	public static final int[] FIELD_14_File_ID=new int[]{49,16};
	public static final int[] FIELD_1_REC_NUM=new int[]{0,4};
	public static final int[] FIELD_PRF=new int[]{719+935,15};
	
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
		return reader.readB4(FIELD_1_REC_NUM[0], FIELD_1_REC_NUM[1],true);
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
	 * @throws IOException
	 */
	public void readDataSetSummary() throws IOException{
		String sceneid=reader.readBytesAsString((719+21),32);
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
			//LedMetadataReader lr=new LedMetadataReader(new File(
					//"H:////Radar-Images////AlosTrial////Alos2////WBD////PON_000000476_0000060609////LED-ALOS2029163650-141207-WBDR1.5RUD"));
			LedMetadataReader lr=new LedMetadataReader(new File(
					"H://sat//AlosTrialTmp//SM//0000054534_001001_ALOS2049273700-150422//LED-ALOS2049273700-150422-FBDR1.5RUD"));
		
			int val=lr.readFileID();
			
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
			
			lr.readDataSetSummary();
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
