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

public class LedReader {
	public static final int[] FIELD_14=new int[]{49,16};
	public static final int[] FIELD_1=new int[]{0,4};
	
	private BinaryReader reader=null;
	
	public LedReader(File input) throws FileNotFoundException {
		reader=new BinaryReader(input);
	}
	
	
	public int readB4(int pos,int len,boolean bigEndian) throws IOException{
		byte[] b=reader.readBytes(pos,len);
		int i=0;
		/*int i= (b[0]<<24)&0xff000000|
			       (b[1]<<16)&0x00ff0000|
			       (b[2]<< 8)&0x0000ff00|
			       (b[3]<< 0)&0x000000ff;
		*/
		if (bigEndian) {
            i= (((b[0] & 0xFF) << 24) + ((b[1] & 0xFF) << 16)
                    + ((b[2] & 0xFF) << 8) + ((b[3] & 0xFF)));
        } else {
            i= (((b[3] & 0xFF) << 24) + ((b[2] & 0xFF) << 16)
                    + ((b[1] & 0xFF) << 8) + ((b[0] & 0xFF)));
        }
		
		return i;
	}
	
	public String readString(int pos,int nbytes) throws IOException {
        final byte[] data=reader.readBytes(pos,nbytes);
        return new String(data);
    }
	
	public int readField1() throws IOException{
		return readB4(FIELD_1[0], FIELD_1[1],true);
	}
	
	public int readField6() throws IOException{
		return readB4(8, 4,true);
	}
	
	
	public void readDataSetSummary() throws IOException{
		String sceneid=readString((719+21),32);
		System.out.println(":"+sceneid);
		
		int first=readB4(719,4,false);
		System.out.println(":"+first);
		
		String ellipse=readString((719+165),16);
		System.out.println(":"+ellipse);
		
	}
	
	
	public static void main(String[]args){
		try {
			LedReader lr=new LedReader(new File("F:/SumoImgs/AlosTrialTmp/SM/0000054534_001001_ALOS2049273700-150422/LED-ALOS2049273700-150422-FBDR1.5RUD"));
			int val=lr.readField1();
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
