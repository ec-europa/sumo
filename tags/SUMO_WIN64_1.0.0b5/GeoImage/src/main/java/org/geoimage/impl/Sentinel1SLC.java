package org.geoimage.impl;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import jrc.it.xml.wrapper.SumoJaxbSafeReader;

import org.geoimage.utils.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author
 */
public class Sentinel1SLC extends Sentinel1 {
	private Logger logger = LoggerFactory.getLogger(Sentinel1SLC.class);
	protected RandomAccessFile fss;
    protected byte[] preloadedData;

	public Sentinel1SLC(SumoJaxbSafeReader safeReader, String swath) {
		super(safeReader, swath);
	}
	

	  

    @Override
    public int read(int x, int y) {
        int result = 0;
        long temp = 0;
        byte[] pixelByte = new byte[4];

        if (x >= 0 & y >= 0 & x < getActiveImage().xSize & y < getActiveImage().ySize) {
            try {
                temp = (y * (getActiveImage().xSize * 4) +  + x * 4);
                fss.seek(temp);
                fss.read(pixelByte, 0, 4);
                byte interm0 = pixelByte[0];
                byte interm1 = pixelByte[1];
                byte interm2 = pixelByte[2];
                byte interm3 = pixelByte[3];
                long real=((interm0) << 8) | (interm1&0xFF);
                long img=((interm2) << 8) | (interm3&0xFF);
                result = (int)Math.sqrt(real*real+img*img);
            } catch (IOException ex) {
	        	logger.error(ex.getMessage(),ex);
            }
        }
        return result;
    }

    @Override
    public void preloadLineTile(int y, int length) {
        if (y < 0) {
            return;
        }
        preloadedInterval = new int[]{y, y + length};
        //positioning in file images y=rows image.xSize=cols 4=numero bytes 
        int tileOffset =  (y * (getActiveImage().xSize * 4 ));
        preloadedData = new byte[(getActiveImage().xSize * 4) * length];
        try {
        	File fimg = getActiveImage().getImageFile();
			fss = new RandomAccessFile(fimg.getAbsolutePath(), "r");
            fss.seek(tileOffset);
            fss.read(preloadedData);
        } catch (IOException ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }

    
    @Override
    public int[] readTile(int x, int y, int width, int height) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect = rect.intersection(getActiveImage().bounds);
        int[] tile = new int[height * width];
        if (rect.isEmpty()) {
            return tile;
        }
        if (rect.y != preloadedInterval[0] || rect.y + rect.height != preloadedInterval[1]) {
        	preloadLineTile(rect.y, rect.height);
        }
        int yOffset =  4 * getActiveImage().xSize;
        int xinit = rect.x - x;
        int yinit = rect.y - y;
        for (int i = 0; i < rect.height; i++) {
            for (int j = 0; j < rect.width; j++) {
            	//i*yOffset= start col position j*4=col position from start 4*rect.x=rows offset  
                int temp = i * yOffset + j*4 + 4 * rect.x ;
                
                byte[] bufferReal={preloadedData[temp+1], preloadedData[temp+0]};
                //true = use Big Endian
                long real = ByteUtils.byteArrayToShort(bufferReal,true);
                byte[] bufferImg={preloadedData[temp+3], preloadedData[temp+2]};
                long img = ByteUtils.byteArrayToShort(bufferImg,true);
                
                tile[(i + yinit) * width + j + xinit] = (int)Math.sqrt(real*real+img*img);
            }
        }
        return tile;
    }

   
    @Override
    public int getNumberOfBytes() {
        return 4;
    }
    @Override
    public String getFormat() {
        return getClass().getCanonicalName();
    }
	

	@Override
	public int getWidth() {
		return getActiveImage().xSize;
	}


	@Override
	public int getHeight() {
		return getActiveImage().ySize;
	}	
		
	public TIFF getActiveImage(){
		return tiffImages.get(getBandName(getBand()));
	}


	
	
	
	
	/*
	public static void main(String args[]){
		String path ="C://tmp//sumo_images//S1//IW//S1A_IW_SLC__1SDH_20140502T170314_20140502T170344_000421_0004CC_1A90.SAFE//measurement//s1a-iw1-slc-hh-20140502t170314-20140502t170342-000421-0004cc-001.tiff";
		String safePath ="C://tmp//sumo_images//S1//IW//S1A_IW_SLC__1SDH_20140502T170314_20140502T170344_000421_0004CC_1A90.SAFE//manifest.safe";
		SumoJaxbSafeReader safe;
		try {
			safe = new SumoJaxbSafeReader(safePath);
			Sentinel1SLC s1=new Sentinel1SLC(safe,"1");
			s1.readTile(0, 0, 200,200);
		} catch (JDOMException | IOException | JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
