/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.impl;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.slf4j.LoggerFactory;

import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReader;

/**
 * This is a convenience class that warp a tiff image to easily use in the case
 * of one geotiff per band (like radarsat 2 images)
 * @author thoorfr
 */
public class TIFF {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(TIFF.class);


    private File imageFile;
    public TIFFImageReader reader;
	public int xSize = -1;
    public int ySize = -1;
    public Rectangle bounds;
	ImageInputStream iis = null;

    public TIFFImageReader getReader() {
		return reader;
	}

	public void setReader(TIFFImageReader reader) {
		this.reader = reader;
	}
    
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
            while(readers.hasNext()&&!worked){
            	Object obj=readers.next();
            	if( obj instanceof TIFFImageReader){
            		reader = (TIFFImageReader)obj;
            		iis=ImageIO.createImageInputStream(imageFile);
            		reader.setInput(iis);
            		try{
            			xSize=reader.getWidth(band);
            			ySize=reader.getHeight(band);
            			bounds=new Rectangle(0,0,xSize,ySize);
            		}catch(Exception e){
            			bounds=null;
            			logger.warn("Problem reading size information");
            		}	
            		worked=true;
            	}else{
            		
            	}
            }
            if(!worked){
            	logger.warn("No reader avalaible for this image");
            }
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }	
//        }finally{
//        	if(iis!=null)
//				try {
//					iis.flush();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//        }
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
	public void refreshBounds() {
		bounds=new Rectangle(0,0,xSize,ySize);
	}
	
	public void dispose(){
		reader.dispose();
		imageFile=null;
		reader=null;
		try {
			iis.close();
			iis=null;
		} catch (IOException e) {
		}
    }
    public File getImageFile() {
		return imageFile;
	}

	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}
	
		

}
