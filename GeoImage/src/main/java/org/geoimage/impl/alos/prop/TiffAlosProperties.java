/*
 * 
 */
package org.geoimage.impl.alos.prop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.slf4j.LoggerFactory;

public class TiffAlosProperties extends AbstractAlosProperties {
    private static org.slf4j.Logger logger=LoggerFactory.getLogger(TiffAlosProperties.class);

   
	
	/**
	 * 
	 * @param propFile
	 */
	public TiffAlosProperties(String propFile){
		super(propFile);
	}
	/**
	 * 
	 * @param propFile
	 */
	public TiffAlosProperties(File propFile){
		super(propFile);
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

	@Override
	public float getPrf() {
		return 0;
	}
	

	
	
	public static void main(String[] args){
		TiffAlosProperties aa=new TiffAlosProperties("F:/SumoImgs/AlosTrialTmp/SM/0000054535_001001_ALOS2051343700-150506/summary.txt");
		aa.getCorners();
	}
	
}
