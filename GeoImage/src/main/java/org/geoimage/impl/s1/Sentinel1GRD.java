package org.geoimage.impl.s1;


import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.File;

import org.geoimage.impl.TIFF;
import org.geoimage.utils.IProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.imageio.plugins.tiff.TIFFImageReadParam;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReader;





/**
 * 
 * 
 * @author 
 */
public class  Sentinel1GRD extends Sentinel1 {//implements IIOReadProgressListener {
	private Logger logger= LoggerFactory.getLogger(Sentinel1GRD.class);
	//private boolean readComplete=false;
	//private boolean readAborted=false;
	
	protected short[] preloadedData;
    
    public Sentinel1GRD(String swath,File manifest) {
    	super(swath,manifest);
    	//gdal.AllRegister();
    }

    /**
     * 
     */
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
                	logger.warn("readTile function:"+e.getMessage());
                }	
            }
            }
        return tile;
    }
   
   
    
        
    @Override    
    public int[] readAndDecimateTile(int x, int y, int width, int height, double scalingFactor, boolean filter, IProgress progressbar,int band) {
        int outWidth = (int) (width * scalingFactor);
        int outHeight = (int) (height * scalingFactor);
        double deltaPixelsX = (double) width / outWidth;
        double deltaPixelsY = (double) height / outHeight;
        double tileHeight = height / (((double) (width * height) / MAXTILESIZE));
        int[] outData = new int[outWidth * outHeight];
        if (height / outHeight > 4) {
            double a = width*1.0 / outWidth;  //moltiplico per 1.0 per avere un risultato con i decimali
            double b = height*1.0 / outHeight;
            for (int i = 0; i < outHeight; i++) {
                for (int j = 0; j < outWidth; j++) {
                    try {
                        outData[i * outWidth + j] = readTileXY((int)(x + j * a), (int) (y + i * b),band);
                    } catch (Exception e) {
                    }
                }
            }
            return outData;
        }
        // load first tile
        int currentY = 0;
        int[] tile = readTile(0, currentY, width, (int) Math.ceil(tileHeight),band);
        if (progressbar != null) {
            progressbar.setMaximum(outHeight / 100);
        // start going through the image one Tile at a time
        }
        double posY = 0.0;
        for (int j = 0; j < outHeight; j++, posY += deltaPixelsY) {
            // update progress bar
            if (j / 100 - Math.floor(j / 100) == 0) {
                if (progressbar != null) {
                    progressbar.setCurrent(j / 100);
                // check if Tile needs loading
                }
            }
            if (posY > (int) Math.ceil(tileHeight)) {
                tile = readTile(0, currentY + (int) Math.ceil(tileHeight), width, (int) Math.ceil(tileHeight),band);
                posY -= (int) Math.ceil(tileHeight);
                currentY += (int) Math.ceil(tileHeight);

            }

            double posX = 0.0;
            for (int i = 0; i < outWidth; i++, posX += deltaPixelsX) {
                outData[i + j * outWidth] = tile[(int) posX * (int) posY];
            }
        }

        return outData;
    }     
        
    
	 /**
	  * 
	  * @param x
	  * @param y
	  * @param width
	  * @param height
	  * @param band
	  * @return
	  */
    public int readTileXY(int x, int y, int band) {
        Rectangle rect = new Rectangle(x, y, 1, 1);
        int val=0;

        	if (y < 0) {
 	            val= 0;
        	}else{
	 	        TIFF tiff=getImage(band);
	 	        TIFFImageReader reader=tiff.reader;
	 	        try {
	 	            TIFFImageReadParam tirp =(TIFFImageReadParam) tiff.reader.getDefaultReadParam();
	 	            tirp.setSourceRegion(rect);
	 	        	BufferedImage bi=null;
 	        		bi=reader.read(0, tirp);
 	        		DataBufferUShort raster=(DataBufferUShort)bi.getRaster().getDataBuffer();
 	        		short[] b=raster.getData();
	 	        	//short[] data=(short[])raster.getDataElements(0, 0, width,height, null);
	 	        	val=b[0];
	 	        } catch (Exception ex) {
	 	            logger.error(ex.getMessage(),ex);
	 	        }finally{
	 	        	reader.dispose();
	 	        }
        	}  
        
        return val;
    }
    
    /**
	  * 
	  * @param x
	  * @param y
	  * @param width
	  * @param height
	  * @param band
	  * @return
	  */
   public int[] read(int x, int y,int w,int h, int band) {
       Rectangle rect = new Rectangle(x, y, w, h);
       int data[]=null;

        TIFF tiff=getImage(band);
        TIFFImageReader reader=tiff.reader;
        try {
            TIFFImageReadParam tirp =(TIFFImageReadParam) tiff.reader.getDefaultReadParam();
            tirp.setSourceRegion(rect);
        	BufferedImage bi=null;
    		bi=reader.read(0, tirp);
    		DataBufferUShort raster=(DataBufferUShort)bi.getRaster().getDataBuffer();
    		short[] b=raster.getData();
    		data=new int[b.length];
        	for(int i=0;i<b.length;i++)
        		data[i]=b[i];
    		
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
        }finally{
        	reader.dispose();
        }
       
       return data;
   }

	
	public  void preloadLineTile(int y, int length,int band) {
        if (y < 0) {
            return;
        }
        preloadedInterval = new int[]{y, y + length};
        Rectangle rect = new Rectangle(0, y, getImage(band).xSize, length);
        
        TIFF tiff=getImage(band);
        rect=tiff.bounds.intersection(rect);
        
        try {
            TIFFImageReadParam tirp =(TIFFImageReadParam) tiff.reader.getDefaultReadParam();
            tirp.setSourceRegion(rect);
        	BufferedImage bi=null;
        	TIFFImageReader reader=tiff.reader;
        	try{

        			bi=reader.read(0, tirp);

        	}catch(Exception e){
        		logger.warn("Problem reading image POS x:"+0+ "  y: "+y +"   try to read again");
        		try {
    			    Thread.sleep(100);                 
    			} catch(InterruptedException exx) {
    			    Thread.currentThread().interrupt();
    			}
        		bi=reader.read(0, tirp);
        	}	
        	WritableRaster raster=bi.getRaster();
        	preloadedData=(short[])raster.getDataElements(0, 0, raster.getWidth(), raster.getHeight(), null);//tSamples(0, 0, raster.getWidth(), raster.getHeight(), 0, (short[]) null);
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
        }finally{
        	//tiff.reader.addIIOReadProgressListener(this);
        	//readComplete=false;
        	
        }
    }
	
	
	  

		@Override
		public File getOverviewFile() {
			return null;
		}
		
	  
	  
/*

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
	}*/
	
	  
	  
	  /**GDAL test function
	   * 
	   * 
	   * 
     *  
     *
    public void preloadLineTile(int y,int length,int band){
    	 if (y < 0) {
             return;
         }
    	TIFF tiff=getImage(band);
    	String imgpath=tiff.getImageFile().getAbsolutePath();
    	Dataset data=gdal.Open(imgpath,gdalconstConstants.GA_ReadOnly);
		Band b=data.GetRasterBand(1);
		
		int buf_type = b.getDataType();
		int pixels=getImage(band).xSize*length;
        int buf_size = pixels * gdal.GetDataTypeSize(buf_type) / 16;
		
		ByteBuffer buffer = ByteBuffer.allocateDirect(buf_size);
        buffer.order(ByteOrder.nativeOrder());
		
		
		preloadedData=new short[buf_size];
		int ok=b.ReadRaster(0, y, getImage(band).xSize, length, gdalconstConstants.GDT_UInt16,preloadedData);
		if(ok!=0){
			System.out.println("Error reading tile:"+ok);
		}
		data.FlushCache();
		data.delete();
    }
	   * 
	   * 
	   * 
	   * 
	   * **
     * 
     * @param y
     * @param length
     * @param band
     *
    public int preloadLineTileScaling(int x, int y, int width, int height,int band){
      	 if (y < 0) {
               return 0;
           }
   	   	TIFF tiff=getImage(band);
   	   	String imgpath=tiff.getImageFile().getAbsolutePath();
   	   	Dataset data=gdal.Open(imgpath,gdalconstConstants.GA_ReadOnly);
   		Band b=data.GetRasterBand(1);
   			
   		int buf_type = b.getDataType();
   	    int buf_size = width * gdal.GetDataTypeSize(buf_type) /16;
   			
   		ByteBuffer buffer = ByteBuffer.allocateDirect(buf_size);
        buffer.order(ByteOrder.nativeOrder());
   		
   		
   		short[] pixVals=new short[buf_size];
   		int ok=b.ReadRaster(x, y, width,height, gdalconstConstants.GDT_UInt16,pixVals);
   		if(ok!=0){
   			System.out.println("Error creating overview:"+ok);
   		}
   		data.FlushCache();
   		data.delete();
   		return pixVals[0];
      }
    
	   */
	
}
