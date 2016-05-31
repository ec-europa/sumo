/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.impl.imgreader;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;


import org.slf4j.LoggerFactory;

import com.sun.media.imageio.plugins.tiff.TIFFImageReadParam;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReader;

/**
 * This is a convenience class that warp a tiff image to easily use in the case
 * of one geotiff per band (like radarsat 2 images)
 * @author thoorfr
 */
public class TIFF implements IReader {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(TIFF.class);


    private File imageFile;
    private TIFFImageReader reader;
    public int xSize = -1;
    public int ySize = -1;
    private Rectangle bounds;
    
    
	ImageInputStream iis = null;

    
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
	}
	
	public BufferedImage read(int id,ImageReadParam param) throws IOException{
		return reader.read(id, param);
	}
	public BufferedImage read(int id,Rectangle rect) throws IOException{
		TIFFImageReadParam tirp =(TIFFImageReadParam) reader.getDefaultReadParam();
	    tirp.setSourceRegion(rect);
		return reader.read(id, tirp);
	}
	
	
    /* (non-Javadoc)
	 * @see org.geoimage.impl.ITIFF#getxSize()
	 */
    @Override
	public int getxSize() {
		return xSize;
	}

	/* (non-Javadoc)
	 * @see org.geoimage.impl.ITIFF#setxSize(int)
	 */
	@Override
	public void setxSize(int xSize) {
		this.xSize = xSize;
	}

	/* (non-Javadoc)
	 * @see org.geoimage.impl.ITIFF#getySize()
	 */
	@Override
	public int getySize() {
		return ySize;
	}

	/* (non-Javadoc)
	 * @see org.geoimage.impl.ITIFF#setySize(int)
	 */
	@Override
	public void setySize(int ySize) {
		this.ySize = ySize;
	}

	/* (non-Javadoc)
	 * @see org.geoimage.impl.ITIFF#getBounds()
	 */
	@Override
	public Rectangle getBounds() {
		return bounds;
	}

	/* (non-Javadoc)
	 * @see org.geoimage.impl.ITIFF#setBounds(java.awt.Rectangle)
	 */
	@Override
	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}
	/* (non-Javadoc)
	 * @see org.geoimage.impl.ITIFF#refreshBounds()
	 */
	@Override
	public void refreshBounds() {
		bounds=new Rectangle(0,0,xSize,ySize);
	}
	
	/* (non-Javadoc)
	 * @see org.geoimage.impl.ITIFF#dispose()
	 */
	@Override
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
    /* (non-Javadoc)
	 * @see org.geoimage.impl.ITIFF#getImageFile()
	 */
    @Override
	public File getImageFile() {
		return imageFile;
	}

	/* (non-Javadoc)
	 * @see org.geoimage.impl.ITIFF#setImageFile(java.io.File)
	 */
	@Override
	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}
	
		

}
