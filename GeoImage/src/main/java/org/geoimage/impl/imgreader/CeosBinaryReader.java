package org.geoimage.impl.imgreader;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.geoimage.impl.alos.AlosProperties;


public class CeosBinaryReader extends BinaryReader implements IReader{
	AlosProperties prop=null;
	public final int OFF_SET=720;
	
	public CeosBinaryReader(File file,AlosProperties alosProp) throws FileNotFoundException{
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
		int position=this.getySize()*y+x+OFF_SET;
		short[] dd =super.readShort(position, x, y, width, height);
		
		return dd;
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
		return null;
	}
	@Override
	public void setImageFile(File imageFile) {
		
	}
	
}
