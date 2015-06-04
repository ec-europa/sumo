package org.geoimage.impl.envi;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

import org.slf4j.LoggerFactory;


/**
 * Class that warp EnvisatImage class to read specifically SLC rasters
 * Made for performance issues
 * @author thoorfr
 */
public class EnvisatImage_SLC extends EnvisatImage {

	protected int xSize = -1;
	protected int ySize = -1;
	
    public EnvisatImage_SLC(File mainFile) {
    	super(mainFile);
    }

	private static org.slf4j.Logger logger=LoggerFactory.getLogger(EnvisatImage.class);

    

    @Override
    public int read(int x, int y,int band) {
        int result = 0;
        long temp = 0;
        byte[] pixelByte = new byte[4];
        // System.out.println(this.imageType);
        if (x >= 0 & y >= 0 & x < xSize & y < ySize) {
            try {
                temp = (y * (xOffset + xSize * 4) + xOffset + x * 4);
                fss.seek(temp + offsetBand);
                fss.read(pixelByte, 0, 4);
                byte interm0 = pixelByte[0];
                byte interm1 = pixelByte[1];
                byte interm2 = pixelByte[2];
                byte interm3 = pixelByte[3];
                long real=((interm0) << 8) | (interm1&0xFF);
                long img=((interm2) << 8) | (interm3&0xFF);
                result = (int)Math.sqrt(real*real+img*img);
            } catch (IOException e) {
            	logger.error("cannot read pixel (" + x + "," + y + ")", e);
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
        int tileOffset = offsetBand + (y * (xSize * 4 + xOffset));
        preloadedData = new byte[(xSize * 4 + xOffset) * length];
        try {
            fss.seek(tileOffset);
            fss.read(preloadedData);
        } catch (IOException e) {
        	logger.error("cannot preload the line tile", e);
        }
    }

    
    @Override
    public int[] readTile(int x, int y, int width, int height, int[] tile,int band) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect = rect.intersection(bounds);
        if (rect.isEmpty()) {
            return tile;
        }
        if (rect.y != preloadedInterval[0] || rect.y + rect.height != preloadedInterval[1]) {
            preloadLineTile(rect.y, rect.height,band);
        }
        int yOffset = xOffset + 4 * xSize;
        int xinit = rect.x - x;
        int yinit = rect.y - y;
        for (int i = 0; i < rect.height; i++) {
            for (int j = 0; j < rect.width; j++) {
                int temp = i * yOffset + j*4 + 4 * rect.x + xOffset;
                long real=(preloadedData[temp+0] << 8) | (preloadedData[temp+1]&0xff );
                long img=((preloadedData[temp+2]) << 8) | (preloadedData[temp+3]&0xff);
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

}
