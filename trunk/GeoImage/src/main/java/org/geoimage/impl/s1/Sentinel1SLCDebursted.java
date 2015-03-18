package org.geoimage.impl.s1;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

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

	public Sentinel1SLCDebursted(String swath) {
		super(swath);
	}
	
	
	@Override
	public boolean initialise(File manifestXML) {
		boolean init = super.initialise(manifestXML);
		/*try {
			SumoJaxbSafeReader safeReader;
			
				safeReader = new SumoJaxbSafeReader(manifestXML);
			
			//load the correct annotation file for the current images (per swath) 
			mainFolder=manifestXML.getParentFile();
			
			String bandName=getBandName(0);
			String nameFirstFile=tiffImages.get(bandName).getImageFile().getName();//new File(safeReader.getHrefsTiff()[0]).getName();
	        nameFirstFile=nameFirstFile.replace(".tiff", ".xml");
			String annotationFilePath=new StringBuilder(mainFolder.getAbsolutePath()).append("/annotation/").append(nameFirstFile).toString();
			SumoAnnotationReader annotationReader=new SumoAnnotationReader(annotationFilePath);
	
			util=new SentinelDeburstUtil(safeReader,annotationReader);
			
			
			util.readBurstInformation();
			lines=util.getLinesToRemove();
			Collection<TIFF>tiffs=tiffImages.values();
			for(TIFF t:tiffs){
				t.ySize=t.ySize-util.getLinesToRemove();
				t.refreshBounds();
			}
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		return init;
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

  
}
