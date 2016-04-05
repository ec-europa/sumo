/*
 * 
 */
package org.geoimage.impl.alos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
			int n=reader.readB4(935, 4,false);
			logger.info("VAL:"+n);
		}catch(Exception e){
			logger.error(e.getMessage());
		}finally{
			reader.dispose();
		}
	}
	

	
	
	public static void main(String[] args){
		CeosAlosProperties aa=new CeosAlosProperties(
				new File("Y:/Images/AlosTrialTmp/SM/0000054534_001001_ALOS2049273700-150422/summary.txt"),
				new File("Y:/Images/AlosTrialTmp/SM/0000054534_001001_ALOS2049273700-150422/LED-ALOS2049273700-150422-FBDR1.5RUD")
				);
		aa.getCorners();
		
	}
	
}
