/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.impl;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.sun.media.imageio.plugins.tiff.TIFFDirectory;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReader;

/**
 * This is a convenience class that warp a tiff image to easily use in the case
 * of one geotiff per band (like radarsat 2 images)
 * @author thoorfr
 */
public class TIFF {


    public TIFFImageReader reader;
    public TIFFDirectory td;
    public RenderedImage image=null;
    private File imageFile;
    
    protected int xSize = -1;
   	protected int ySize = -1;
    protected Rectangle bounds;
   

    /**
     * 
     * @param imageFile
     * @param band form files with multiple band
     */
	public TIFF(File imageFile,int band) {
    	this.imageFile=imageFile;
        try {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("tiff");
            boolean worked=false;
            int idx=0;
            while(readers.hasNext()&&!worked){
            	Object obj=readers.next();
            	if( obj instanceof TIFFImageReader){
            		reader = (TIFFImageReader)obj;
            		ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
            		reader.setInput(iis);
            		xSize=reader.getWidth(band);
            		ySize=reader.getHeight(band);
            		bounds=new Rectangle(0,0,xSize,ySize);
            		td = TIFFDirectory.createFromMetadata(reader.getImageMetadata(idx));
            		idx++;
            		worked=true;
            	}else{
            		
            	}
            }
            if(!worked){
            	Logger.getLogger(TIFF.class.getName()).log(Level.WARNING,"No reader avalaible for this image");
            }
        } catch (Exception ex) {
            Logger.getLogger(TIFF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getxSize() {
		return xSize;
	}

	public void setxSize(int xSize) {
		this.xSize = xSize;
	}

	public int getySize() {
		return ySize;
	}

	public void setySize(int ySize) {
		this.ySize = ySize;
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	public void dispose(){
        try {
            image = null;
            td=null;
            reader.dispose();
        } catch (Exception ex) {
            Logger.getLogger(TIFF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public File getImageFile() {
		return imageFile;
	}

	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}
}
