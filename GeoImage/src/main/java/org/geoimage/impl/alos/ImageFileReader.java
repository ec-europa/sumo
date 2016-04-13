/*
 * 
 */
package org.geoimage.impl.alos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.geoimage.impl.alos.prop.CeosAlosProperties;
import org.geoimage.impl.imgreader.BinaryReader;


public class ImageFileReader {
	/* Signal data is not present in product 1.1
	public static final int START_SIGNAL_DATA=719;
	
	public static final int[] POS_NUMBER_OF_SAR_SIGN_DATA_REC=new int[]{180,6};
	public static final int[] POS_SAR_DATA_REC_LENGTH=new int[]{186,6};*/
	//public static final int[] POS_SIGNAL_PRF=new int[]{719+57,3};

	//process data
	public static final int START_PROCESS_DATA=719;
	public static final int[] POS_SIGNAL_PRF=new int[]{START_PROCESS_DATA+57,3};
	
	
	private BinaryReader reader=null;
	private int numberOfLines=0;
	private int numberOfPixels=0;
	private int bitPerPixel=0;
	private int bytesPerRec=0;
	
	public ImageFileReader(File input,int numberOfLines,int numberOfPixels,int bitPerPixel) throws FileNotFoundException {
		reader=new BinaryReader(input);
		this.numberOfLines=numberOfLines;
		this.numberOfPixels=numberOfPixels;
		this.bitPerPixel=bitPerPixel;
		bytesPerRec=192+numberOfPixels*(bitPerPixel/8);
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public int getSarDataLength() throws IOException{
		byte[] bb=reader.readBytes(0,0);
		String s=new String(bb,StandardCharsets.UTF_8);
		int val=Integer.parseInt(s.trim());
		return val;
	}
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public int getSlantRangeFirst() throws IOException{
		//720+ (round( (NRec+ 1)/ 2)- 1)* recbyt
		int pos=START_PROCESS_DATA+((numberOfLines+1)/2)*bytesPerRec;
		int res=reader.readB4(pos+65,true);
		return res;
	}
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public int getSlantRangeMiddle() throws IOException{
		//720+ (round( (NRec+ 1)/ 2)- 1)* recbyt
		int pos=START_PROCESS_DATA+((numberOfLines+1)/2)*bytesPerRec;
		int res=reader.readB4(pos+69,true);
		return res;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public int getSlantRangeLast() throws IOException{
		//720+ (round( (NRec+ 1)/ 2)- 1)* recbyt
		int pos=START_PROCESS_DATA+((numberOfLines+1)/2)*bytesPerRec;
		int res=reader.readB4(pos+73,true);
		return res;
	}
	
	
	public static void main(String[] args){
		File input=new File("H://Radar-Images//AlosTrial//Alos2//WBD//PON_000000476_0000060609//IMG-HH-ALOS2029163650-141207-WBDR1.5RUD");
		try {
			CeosAlosProperties pp=new CeosAlosProperties(input.getParentFile().getAbsolutePath());
			
			ImageFileReader read=new ImageFileReader(input,
					pp.getNumberOfLines(),
					pp.getNumberOfPixels(),
					pp.getBitsPerPixel());
			
			
			
			int y=read.getSlantRangeMiddle();
			System.out.println("yy:"+y);
			
	//		float prf=read.getPrf();
	//		System.out.println("prf:"+prf);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
