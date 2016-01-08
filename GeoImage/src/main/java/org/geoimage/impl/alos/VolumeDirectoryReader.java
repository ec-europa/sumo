package org.geoimage.impl.alos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.geoimage.impl.imgreader.BinaryReader;

public class VolumeDirectoryReader {
	public static int VOLUME_DESCRIPTOR_SIZE=360;
	public static int FILE_POINTER_SIZE=360;
	public static int TEXT_SIZE=360;
	
	
	
	//Volume descriptor records
	public static int[] LOG_VOL_ID=new int[]{60,15};	//type CH
	public static int[] VOL_ID=new int[]{76,15}; 		//type CH
	
	public static int[] LOG_VOL_CREATION_DATA=new int[]{112,8}; 		//type CH
	public static int[] LOG_VOL_CREATION_TIME=new int[]{120,8}; 		//type CH

	public static int[] NUM_POINTERS=new int[]{160,4}; 		//type I4
	
	
	//file pointer records  start from 360
	public static int[] REC_SEQ_NUM=new int[]{0,4}; 		//type B
	public static int[] REF_FILE_NUM=new int[]{16,4}; 		//type I4
	
	
	//text records
	public static int[] PROD_TYPE=new int[]{16,55-16}; 		//type B
	public static int[] LOC_DATE_TIME_STR=new int[]{56,115-56}; 		//type B
	
	
	
	
	private BinaryReader reader=null;
	
	public VolumeDirectoryReader(final File fileInput) throws FileNotFoundException {
		this.reader=new BinaryReader(fileInput);
	}
	
	
	/**************************** VOL DES *******************************************/
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getLogicVolumeId() throws IOException{
		byte[] bs=reader.readBytes(LOG_VOL_ID[0], LOG_VOL_ID[1]);
		String str = new String(bs, StandardCharsets.UTF_8); // for UTF-8 encoding
		return str;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getLogVolCreationData() throws IOException{
		byte[] bs=reader.readBytes(LOG_VOL_CREATION_DATA[0], LOG_VOL_CREATION_DATA[1]);
		String str = new String(bs, StandardCharsets.UTF_8); // for UTF-8 encoding
		return str;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public int getNumPointers() throws IOException{
		byte[] val=reader.readBytes(NUM_POINTERS[0], NUM_POINTERS[1]);
		String str = new String(val, StandardCharsets.UTF_8); // for UTF-8 encoding
		return Integer.parseInt(str.trim());
	}
	
	
	/**************************** FILE POINTER RECORDS - START FROM 360 *******************************************/
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getLogVolCreationTime() throws IOException{
		byte[] bs=reader.readBytes(LOG_VOL_CREATION_TIME[0], LOG_VOL_CREATION_TIME[1],VOLUME_DESCRIPTOR_SIZE);
		String str = new String(bs, StandardCharsets.UTF_8); // for UTF-8 encoding
		return str;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getVolumeId() throws IOException{
		byte[] bs=reader.readBytes(VOL_ID[0], VOL_ID[1],VOLUME_DESCRIPTOR_SIZE);
		String str = new String(bs, StandardCharsets.UTF_8); // for UTF-8 encoding
		return str;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public int getReqSeqNum() throws IOException{
		int val=reader.readB4(REC_SEQ_NUM[0]+VOLUME_DESCRIPTOR_SIZE, REC_SEQ_NUM[1],true);
		return val;
	}
	
	
		
	/** 
	 * 
	 * @return
	 * @throws IOException
	 */
	private int getRefFileNum() throws IOException{
		byte[] b=reader.readBytes(REF_FILE_NUM[0], REF_FILE_NUM[1],VOLUME_DESCRIPTOR_SIZE);
		String str = new String(b, StandardCharsets.UTF_8); // for UTF-8 encoding
		return Integer.parseInt(str.trim());   
	}
	
	
	/**************************** TEXT RECORDS - START FROM 720 *******************************************/
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getLocationDateTimeStr() throws IOException{
		int offset=VOLUME_DESCRIPTOR_SIZE+getNumPointers()*FILE_POINTER_SIZE;
		byte[] bs=reader.readBytes(LOC_DATE_TIME_STR[0], LOC_DATE_TIME_STR[1],offset);
		String str = new String(bs, StandardCharsets.UTF_8); // for UTF-8 encoding
		return str;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getProductType() throws IOException{
		int offset=VOLUME_DESCRIPTOR_SIZE+getNumPointers()*FILE_POINTER_SIZE;
		byte[] bs=reader.readBytes(PROD_TYPE[0], PROD_TYPE[1],offset);
		String str = new String(bs, StandardCharsets.UTF_8); // for UTF-8 encoding
		return str;
	}
	
	
	public static void main(String[] args){
		try {
			String h="H:/sat/AlosTrialTmp/SM/0000054534_001001_ALOS2049273700-150422/VOL-ALOS2049273700-150422-FBDR1.5RUD";
			String c="F:/SumoImgs/AlosTrialTmp/SM/0000054534_001001_ALOS2049273700-150422/VOL-ALOS2049273700-150422-FBDR1.5RUD";
			VolumeDirectoryReader vol=new VolumeDirectoryReader(new File(h));
			String xx=vol.getLogicVolumeId();
			System.out.println(xx);
			
			xx=vol.getVolumeId();
			System.out.println(xx);
			
			xx=vol.getLogVolCreationData();
			System.out.println(xx);
			
			xx=vol.getLogVolCreationTime();
			System.out.println(xx);
			
			int val=vol.getReqSeqNum();
			System.out.println(""+val);
			
			
			val=vol.getNumPointers();
			System.out.println("N POINTERS:"+val);
			
			
			val=vol.getRefFileNum();
			System.out.println(""+val);
			
			
			xx=vol.getLocationDateTimeStr();
			System.out.println(xx);

			
			xx=vol.getProductType();
			System.out.println(xx);

			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
}
