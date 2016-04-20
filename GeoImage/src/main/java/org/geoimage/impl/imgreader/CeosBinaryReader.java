/*
 * 
 */
package org.geoimage.impl.imgreader;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.geoimage.impl.alos.prop.AbstractAlosProperties;
import org.geoimage.impl.alos.prop.TiffAlosProperties;


public class CeosBinaryReader extends BinaryReader implements IReader{
	AbstractAlosProperties prop=null;
	public final int OFF_SET=720+545;
	
	public CeosBinaryReader(File file,AbstractAlosProperties alosProp) throws FileNotFoundException{
		super(file);
		prop=alosProp;
	}
	
	
	/**
	 * 
	 * @param position
	 * @param numBytes
	 * @return
	 * @throws IOException
	 */
	public short[] readShort(int x,int y,int width ,int height) throws IOException{
		int position=(this.getxSize()-1)*(y-2)+x+OFF_SET;
		short[] dd =super.readShort(position, x, y, width, height);
		
		return dd;
	}
	
	/**
	 * 
	 * @param position
	 * @param numBytes
	 * @return
	 * @throws IOException
	 */
	public int[] readInt(int x,int y,int width ,int height) throws IOException{
		int position=(this.getxSize()-1)*(y-2)+x+OFF_SET;
		int[] dd =super.readInt(position, x, y, width, height);
		
		return dd;
	}
	
	/**
	 * 
	 * @param position
	 * @param numBytes
	 * @return
	 * @throws IOException
	 */
	public short readShort(int x,int y){
		try{
			int position=(this.getxSize())*(y)+x+OFF_SET;
			short[] dd =super.readShort(position, x, y, 1, 1);
			System.out.println("Val:"+dd[0]);
			return dd[0];
		}catch(Exception e){
			return -1;
		}	
	}
	
	
	@Override
	public int getxSize() {
		return prop.getNumberOfPixels();
	}
	@Override
	public void setxSize(int xSize) {
	}
	@Override
	public int getySize() {
		return prop.getNumberOfLines();
	}
	@Override
	public void setySize(int ySize) {
	}
	@Override
	public Rectangle getBounds() {
		return new Rectangle(0,0,prop.getNumberOfPixels(),prop.getNumberOfLines());
	}
	@Override
	public void setBounds(Rectangle bounds) {
		new Rectangle(0,0,prop.getNumberOfPixels(),prop.getNumberOfLines());
	}
	@Override
	public void refreshBounds() {
	}
	
	@Override
	public File getImageFile() {
		return super.getBinaryFile();
	}
	@Override
	public void setImageFile(File imageFile) {
		
	}
	
	public void printByteImages(){
		int x=0;
		int y=0;
		int val=0;
		do{
			val=readShort(x, y);
			x++;
			y++;
		}while(val>=00);
	}
	
	public static void main(String args[]) {
	//	File f = new File(
	//			"F:/SumoImgs/AlosTrialTmp/SM/0000054534_001001_ALOS2049273700-150422/IMG-HH-ALOS2049273700-150422-FBDR1.5RUD");
	//	File f2 = new File("H:/sat/AlosTrialTmp/SM/0000054534_001001_ALOS2049273700-150422/IMG-HH-ALOS2049273700-150422-FBDR1.5RUD");
		
		try {
			CeosBinaryReader bin=new CeosBinaryReader(
					new File("H:/sat/AlosTrialTmp/SM/0000054534_001001_ALOS2049273700-150422/IMG-HH-ALOS2049273700-150422-FBDR1.5RUD"),
					new TiffAlosProperties("H:/sat/AlosTrialTmp/SM/0000054534_001001_ALOS2049273700-150422/summary.txt"));
			bin.printByteImages();
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		

	}
	
}
