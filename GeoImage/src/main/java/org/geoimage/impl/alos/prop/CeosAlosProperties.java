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

public class CeosAlosProperties extends AbstractAlosProperties {
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

	@Override
	public float getSatelliteAltitude() {
		try {
			return this.ledprop.readSatAltitude();
		} catch (IOException e) {
			return 628000;
		}
	}
	
	
	
	
}
