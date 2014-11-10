package org.geoimage.impl.s1;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;

import jrc.it.xml.wrapper.SumoJaxbSafeReader;

import org.geoimage.impl.TIFF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author
 */
public class Sentinel1SLCDebursted extends Sentinel1SLC {
	private Logger logger = LoggerFactory.getLogger(Sentinel1SLCDebursted.class);
	protected RandomAccessFile fss;
    protected byte[] preloadedData;
    
    protected SentinelDeburstUtil util;
    int lines=0;

	public Sentinel1SLCDebursted(SumoJaxbSafeReader safeReader, String swath) {
		super(safeReader, swath);
	}
	
	
	@Override
	public boolean initialise(File manifestXML) {
		boolean init = super.initialise(manifestXML);
		util=new SentinelDeburstUtil(getSafeReader(),getAnnotationReader());
		util.readBurstInformation();
		lines=util.getLinesToRemove();
		Collection<TIFF>tiffs=tiffImages.values();
		for(TIFF t:tiffs){
			t.ySize=t.ySize-util.getLinesToRemove();
			t.refreshBounds();
		}
		return init;
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
        }finally{
        	try {
				fss.close();
			} catch (IOException e) {
				logger.warn(e.getMessage());
			}
        }
    }

  
}
