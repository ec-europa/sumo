package org.geoimage.impl.s1;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

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

	public Sentinel1SLC(String swath) {
		super(swath);
	}
	

    @Override
    public int read(int x, int y,int band) {
        int result = 0;
        long temp = 0;
        byte[] pixelByte = new byte[4];

        if (x >= 0 & y >= 0 & x < getImage(band).xSize & y < getImage(band).ySize) {
            try {
                temp = (y * (getImage(band).xSize * 4) +  + x * 4);
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
    public void preloadLineTile(int y, int length,int band) {
        if (y < 0) {
            return;
        }
        preloadedInterval = new int[]{y, y + length};
        //positioning in file images y=rows image.xSize=cols 4=numero bytes 
        int tileOffset =  (y * (getImage(band).xSize * 4 ));
        preloadedData = new byte[(getImage(band).xSize * 4) * length];
        try {
        	File fimg = getImage(band).getImageFile();
			fss = new RandomAccessFile(fimg.getAbsolutePath(), "r");
			fss.seek(tileOffset);
           	fss.read(preloadedData);
        } catch (IOException ex) {
        	logger.error(ex.getMessage(),ex);
        }finally{
        	try {
				fss.close();
			} catch (IOException e) {
				logger.warn(e.getMessage());
			}
        }
    }

    
    @Override
    public int[] readTile(int x, int y, int width, int height,int band) {
    	//System.out.println("read tile x:"+x+",y:"+y+",w:"+width+",h:"+height);
    	
        Rectangle rect = new Rectangle(x, y, width, height);
        rect = rect.intersection(getImage(band).bounds);
        int[] tile = new int[height * width];
        if (rect.isEmpty()) {
            return tile;
        }
        if (rect.y != preloadedInterval[0] || rect.y + rect.height != preloadedInterval[1]) {
        	preloadLineTile(rect.y, rect.height,band);
        }
        int yOffset =  4 * getImage(band).xSize;
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
	public File getOverviewFile() {
		return null;
	}


	

}
