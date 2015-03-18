package org.geoimage.impl.radarsat;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.imageio.plugins.tiff.TIFFImageReadParam;

/**
 * A class that reads Radarsat 2 SLC images
 * @author gabbaan
 */
public class Radarsat2Image_SLC extends Radarsat2Image {

    private int[] preloadedDataReal;
    private int[] preloadedDataImg;
	private Logger logger= LoggerFactory.getLogger(Radarsat2Image_SLC.class);

    public Radarsat2Image_SLC() {
    }

 

    @Override
    public int[] readTile(int x, int y, int width, int height,int band) {

        Rectangle rect = new Rectangle(x, y, width, height);
        rect = rect.intersection(bounds);
        int[] tile = new int[height * width];
        if (rect.isEmpty()) {
            return tile;
        }
        if (rect.y != preloadedInterval[0] | rect.y + rect.height != preloadedInterval[1]) {
            preloadLineTile(rect.y, rect.height,band);
        }
        int yOffset =  getImage(band).xSize;
        int xinit = rect.x - x;
        int yinit = rect.y - y;
        for (int i = 0; i < rect.height; i++) {
            for (int j = 0; j < rect.width; j++) {
                int temp = i * yOffset + j + rect.x;
                long real=preloadedDataReal[temp];
                long img=preloadedDataImg[temp];
                tile[(i + yinit) * width + j + xinit] = (int)Math.sqrt(real*real+img*img);
            }
        }
        return tile;
    }

    @Override
    public int read(int x, int y,int band) {
        TIFFImageReadParam t = new TIFFImageReadParam();
        t.setSourceRegion(new Rectangle(x, y, 1, 1));
        try {            
            int img =  getImage(band).reader.read(0, t).getRaster().getSample(x, y, 1);
            int real =  getImage(band).reader.read(0, t).getRaster().getSample(x, y, 0);
            return (int) Math.sqrt(real * real + img * img);

        } catch (IOException ex) {
        	logger.error(ex.getMessage(),ex);
        } catch (ArrayIndexOutOfBoundsException ex) {
        	logger.warn(ex.getMessage());
        }catch(IllegalArgumentException iae){
        	logger.warn(iae.getMessage());
        }
        return -1;

    }

    @Override
    public void preloadLineTile(int y, int length,int band) {
        if (y < 0) {
            return;
        }
        preloadedInterval = new int[]{y, y + length};
        Rectangle rect = new Rectangle(0, y,  getImage(band).xSize, length);
        TIFFImageReadParam tirp = new TIFFImageReadParam();
        tirp.setSourceRegion(rect);
       
        try {
            preloadedDataReal =  getImage(band).reader.read(0, tirp).getRaster().getSamples(0, 0,  getImage(band).xSize, length, 0, (int[]) null);
            preloadedDataImg =  getImage(band).reader.read(0, tirp).getRaster().getSamples(0, 0,  getImage(band).xSize, length, 1, (int[]) null);
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }
    }


    @Override
    public int getNumberOfBytes() {
        return 4;
    }

    @Override
    public int getType(boolean oneBand) {
        if (oneBand || bands.size() < 2) {
            return BufferedImage.TYPE_USHORT_GRAY;
        } else {
            return BufferedImage.TYPE_INT_RGB;
        }
    }

    @Override
    public String getFormat() {
        return getClass().getCanonicalName();
    }
}

