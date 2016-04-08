/*
 * 
 */
package org.geoimage.impl.alos.prop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.geoimage.impl.alos.LedMetadataReader;
import org.geoimage.impl.alos.VolumeDirectoryReader;
import org.slf4j.LoggerFactory;

public class CeosAlosProperties extends TiffAlosProperties {
    private static org.slf4j.Logger logger=LoggerFactory.getLogger(CeosAlosProperties.class);
    private LedMetadataReader ledprop=null;
    private VolumeDirectoryReader volprop=null;
    
	/**
	 * 
	 * @param propFile
	 */
	public CeosAlosProperties(String imgPath){
		super(imgPath+File.separator+"summary.txt");
		File f=new File(imgPath);
		File[] files=f.listFiles();
		try {
			for(int i=0;i<files.length;i++){
				if(files[i].getName().startsWith("LED-"))
						ledprop=new LedMetadataReader(files[i]);
				if(files[i].getName().startsWith("VOL-"))
						volprop=new VolumeDirectoryReader(files[i]);

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
	
	public float getPrf(){
		Float prf;
		try {
			prf = ledprop.readPrf();
		} catch (IOException e) {
			logger.warn(e.getMessage(),e);
			return 0;
		}
		return prf;
	}
	
	/*public void loadFromBin() throws IOException{
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
	/*	}catch(Exception e){
			logger.error(e.getMessage());
		}finally{
			reader.dispose();
		}
	}*/
	
	
	public static void main(String[] args){
		/*CeosAlosProperties aa=new CeosAlosProperties(
				new File("Y:/Images/AlosTrialTmp/SM/0000054534_001001_ALOS2049273700-150422/summary.txt"),
				new File("Y:/Images/AlosTrialTmp/SM/0000054534_001001_ALOS2049273700-150422/LED-ALOS2049273700-150422-FBDR1.5RUD")
				);*/
		
		CeosAlosProperties aa=new CeosAlosProperties(
				"H:\\Radar-Images\\AlosTrial\\Alos2\\WBD\\PON_000000476_0000060609");
		//,new File("H:\\Radar-Images\\AlosTrial\\Alos2\\WBD\\PON_000000476_0000060609\\LED-ALOS2029163650-141207-WBDR1.5RUD")
				
		/*
		CeosAlosProperties aa=new CeosAlosProperties(
				new File("H:\\sat\\AlosTrialTmp\\SM\\0000054534_001001_ALOS2049273700-150422\\summary.txt"),
				new File("H:\\sat\\AlosTrialTmp\\SM\\0000054534_001001_ALOS2049273700-150422\\IMG-HH-ALOS2049273700-150422-FBDR1.5RUD")
				);
		aa.getCorners();*/
	}
	
}
