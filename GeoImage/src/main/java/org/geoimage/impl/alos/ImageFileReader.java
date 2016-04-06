/*
 * 
 */
package org.geoimage.impl.alos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.geoimage.impl.imgreader.BinaryReader;

public class ImageFileReader {
	public static final int START_SIGNAL_DATA=720;
	
	public static final int[] POS_NUMBER_OF_SAR_SIGN_DATA_REC=new int[]{180,6};
	public static final int[] POS_SAR_DATA_REC_LENGTH=new int[]{186,6};
	public static final int[] POS_SIGNAL_PRF=new int[]{719+57,3};
	
	private BinaryReader reader=null;
	
	public ImageFileReader(File input) throws FileNotFoundException {
		reader=new BinaryReader(input);
	}
	
	
	
	/*public int getNumberOfRecordszz() throws IOException{
		return reader.readB6(POS_NUMBER_OF_SAR_SIGN_DATA_REC[0], POS_NUMBER_OF_SAR_SIGN_DATA_REC[1]);
	}*/
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public int getSarDataLength() throws IOException{
		byte[] bb=reader.readBytes(POS_SAR_DATA_REC_LENGTH[0], POS_SAR_DATA_REC_LENGTH[1]);
		String s=new String(bb,StandardCharsets.UTF_8);
		int val=Integer.parseInt(s.trim());
		return val;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public int[] getNumberOfRecords() throws IOException{
		byte[] bb=reader.readBytes(POS_NUMBER_OF_SAR_SIGN_DATA_REC[0], POS_NUMBER_OF_SAR_SIGN_DATA_REC[1]);
		byte[] b1=Arrays.copyOfRange(bb,0, 2);
		byte[] b2=Arrays.copyOfRange(bb,3, 5);
		String s1=new String(b1,StandardCharsets.UTF_8);
		String s2=new String(b2,StandardCharsets.UTF_8);
		int[]res=new int[]{Integer.parseInt(s1.trim()),Integer.parseInt(s2.trim())};
		return res;
	}
	
	public float getPrf() throws IOException{
		String ff=reader.readBytesAsString(POS_SIGNAL_PRF[0],POS_SIGNAL_PRF[1]);
		float val=Float.parseFloat(ff);
		return val;
	}
	
	public static void main(String[] args){
		File input=new File("H://sat//AlosTrialTmp//SM//0000054534_001001_ALOS2049273700-150422//IMG-HH-ALOS2049273700-150422-FBDR1.5RUD");
		try {
			ImageFileReader read=new ImageFileReader(input);
			int[] x=read.getNumberOfRecords();
			
			System.out.println("XX:"+x[0]);
			System.out.println("XX:"+x[1]);
			
			int y=read.getSarDataLength();
			System.out.println("yy:"+y);
			
			float prf=read.getPrf();
			System.out.println("prf:"+prf);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
