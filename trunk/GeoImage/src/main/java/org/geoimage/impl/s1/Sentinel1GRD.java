package org.geoimage.impl.s1;


import gov.nasa.worldwind.formats.tiff.GeotiffImageReader;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;

import org.geoimage.impl.TIFF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.imageio.plugins.tiff.TIFFImageReadParam;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReader;





/**
 * 
 * 
 * @author 
 */
public class  Sentinel1GRD extends Sentinel1 implements IIOReadProgressListener {
	private Logger logger= LoggerFactory.getLogger(Sentinel1GRD.class);
	private boolean readComplete=false;
	private boolean readAborted=false;
	
	protected int[] preloadedData;
    
    public Sentinel1GRD(String swath) {
    	super(swath);
    }


    @Override
    public int[] readTile(int x, int y, int width, int height,int band) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect = rect.intersection(getImage(band).bounds);
        int[] tile = new int[height * width];
        if (rect.isEmpty()) {
            return tile;
        }

        if (rect.y != preloadedInterval[0] || rect.y + rect.height != preloadedInterval[1]) {
            preloadLineTile(rect.y, rect.height,band);
        }

        int yOffset = getImage(band).xSize;
        int xinit = rect.x - x;
        int yinit = rect.y - y;
        for (int i = 0; i < rect.height; i++) {
            for (int j = 0; j < rect.width; j++) {
                int temp = i * yOffset + j + rect.x;
                try{
                	tile[(i + yinit) * width + j + xinit] = preloadedData[temp];
                }catch(ArrayIndexOutOfBoundsException e ){
                	logger.warn(e.getMessage());
                }	
            }
            }
        return tile;
    }
    
        @Override
    public void preloadLineTile(int y, int length,int band) {
        if (y < 0) {
            return;
        }
        preloadedInterval = new int[]{y, y + length};
        Rectangle rect = new Rectangle(0, y, getImage(band).xSize, length);

        TIFF tiff=getImage(band);
        try {
        	tiff.reader.addIIOReadProgressListener(this);
            TIFFImageReadParam tirp =(TIFFImageReadParam) tiff.reader.getDefaultReadParam();
            tirp.setSourceRegion(rect);
        	BufferedImage bi=null;
        	TIFFImageReader reader=tiff.reader;
        	try{
        		bi=reader.read(0, tirp);
        	}catch(Exception e){
        		logger.warn(e.getMessage()+" --  try to read again");
        		try {
    			    Thread.sleep(100);                 
    			} catch(InterruptedException exx) {
    			    Thread.currentThread().interrupt();
    			}
        		bi=reader.read(0, tirp);
        	}	
        	WritableRaster raster=bi.getRaster();
            preloadedData = raster.getSamples(0, 0, getImage(band).xSize, length, 0, (int[]) null);
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
        }finally{
        	tiff.reader.addIIOReadProgressListener(this);
        	readComplete=false;
        }
    }
        
   
    
        
    
    

	@Override
	public File getOverviewFile() {
		return null;
	}


	@Override
	public void thumbnailStarted(ImageReader source, int imageIndex,
			int thumbnailIndex) {
		
	}
	
	@Override
	public void thumbnailProgress(ImageReader source, float percentageDone) {
		
	}
	
	@Override
	public void thumbnailComplete(ImageReader source) {
		
	}
	
	@Override
	public void sequenceStarted(ImageReader source, int minIndex) {
		//logger.info("Operation Start");
	}
	
	@Override
	public void sequenceComplete(ImageReader source) {
		//logger.info("Operation Complete");
	}
	
	@Override
	public void readAborted(ImageReader source) {
		logger.warn("Reading aborted");
	}
	
	@Override
	public void imageStarted(ImageReader source, int imageIndex) {
		//logger.info("Operation Started");
		
	}
	
	@Override
	public void imageProgress(ImageReader source, float percentageDone) {
		//logger.info("Done:"+percentageDone);
	}
	
	@Override
	public void imageComplete(ImageReader source) {
		//logger.info("Read tile complete");
		readComplete=true;
	}
	
	
}
