package org.geoimage.impl.alos;

import java.io.File;
import java.io.FileNotFoundException;

import org.geoimage.impl.imgreader.BinaryReader;

public class VolumeDirectoryReader {
	public static int VOLUME_DESCRIPTOR_SIZE=360;
	public static int FILE_POINTER_SIZE=360;
	public static int TEXT_SIZE=360;
	
	
	
	//Volume descriptor
	public static int[] LOG_VOL_ID=new int[]{61,15};	//type CH
	public static int[] VOL_ID=new int[]{77,15}; 		//type CH
	
	public static int[] LOG_VOL_CREATION_DATA=new int[]{113,7}; 		//type CH
	public static int[] LOG_VOL_CREATION_TIME=new int[]{121,7}; 		//type CH

	
	//file pointer  start from 360
	public static int[] REC_SEQ_NUM=new int[]{1,4}; 		//type B
	public static int[] REF_FILE_NUM=new int[]{17,3}; 		//type I4
	
	
	
	private BinaryReader reader=null;
	
	public VolumeDirectoryReader(final File fileInput) throws FileNotFoundException {
		this.reader=new BinaryReader(fileInput);
	}
	
	
	public String getLogicVolumeId(){
		//byte[] bs=reader.readBytes(position, numBytes);
	}
	
	
	
	
	
	
}
