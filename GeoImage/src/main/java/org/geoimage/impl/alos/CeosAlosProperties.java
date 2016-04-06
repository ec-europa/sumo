/*
 * 
 */
package org.geoimage.impl.alos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.geoimage.impl.imgreader.BinaryReader;
import org.slf4j.LoggerFactory;

public class CeosAlosProperties extends TiffAlosProperties {
    private static org.slf4j.Logger logger=LoggerFactory.getLogger(CeosAlosProperties.class);
	private File rudFile=null;
    
	/**
	 * 
	 * @param propFile
	 */
	public CeosAlosProperties(File propFile,File rudFile){
		super(propFile);
		this.rudFile=rudFile;
		try{
			loadFromBin();
		}catch(Exception e){
			logger.error(e.getMessage());
		}	
	}
	
	/**
	 * 
	 * @param fis
	 * @throws IOException
	 */
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
	public void loadFromBin() throws IOException{
		BinaryReader reader=new BinaryReader(this.rudFile);
		try{
			int n=reader.bytearray2Int(934, 15,false);
			logger.info("VAL:"+n);

			/*
			 * float nf=reader.bytearray2float((720*2+57),4,true);
			logger.info("VAL:"+nf);
			nf=reader.bytearray2float((720*2+57),4, false);
			 */
			
			/*int n=reader.readB4(720*2+57, 4, true);
			logger.info("VAL:"+n);
			n=reader.readB4(720*2+57, 4, false);
			logger.info("VAL:"+n);
			n=reader.readB4(720*2+57, 4, false);
*/
			
			
			/*byte[]bb=reader.readBytes(720+389, 4);
			logger.info("VAL:"+bb.toString());
			int aa=reader.readB3(720+389, 3);
			logger.info("VAL:"+aa);*/
			
		/*	float nf=reader.bytearray2float((720+4096+935),16,true);
			logger.info("VAL:"+nf);
			nf=reader.bytearray2float((720+4096+935),16,false);
			logger.info("VAL:"+nf);
			*/
		}catch(Exception e){
			logger.error(e.getMessage());
		}finally{
			reader.dispose();
		}
	}
	
	
	public static void main(String[] args){
		/*CeosAlosProperties aa=new CeosAlosProperties(
				new File("Y:/Images/AlosTrialTmp/SM/0000054534_001001_ALOS2049273700-150422/summary.txt"),
				new File("Y:/Images/AlosTrialTmp/SM/0000054534_001001_ALOS2049273700-150422/LED-ALOS2049273700-150422-FBDR1.5RUD")
				);*/
		
		CeosAlosProperties aa=new CeosAlosProperties(
				new File("H:\\Radar-Images\\AlosTrial\\Alos2\\WBD\\PON_000000476_0000060609\\summary.txt"),
				new File("H:\\Radar-Images\\AlosTrial\\Alos2\\WBD\\PON_000000476_0000060609\\LED-ALOS2029163650-141207-WBDR1.5RUD")
				);
		/*
		CeosAlosProperties aa=new CeosAlosProperties(
				new File("H:\\sat\\AlosTrialTmp\\SM\\0000054534_001001_ALOS2049273700-150422\\summary.txt"),
				new File("H:\\sat\\AlosTrialTmp\\SM\\0000054534_001001_ALOS2049273700-150422\\IMG-HH-ALOS2049273700-150422-FBDR1.5RUD")
				);
		aa.getCorners();*/
	}
	
}
