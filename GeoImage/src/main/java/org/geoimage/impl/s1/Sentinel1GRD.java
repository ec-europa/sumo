/*
 * 
 */
package org.geoimage.impl.s1;


import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.File;

import org.geoimage.impl.imgreader.TIFF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * 
 * @author 
 */
public class  Sentinel1GRD extends Sentinel1 {//implements IIOReadProgressListener {
	private Logger logger= LoggerFactory.getLogger(Sentinel1GRD.class);

	
	protected short[] preloadedData;
    
    public Sentinel1GRD(String swath,File manifest,String geolocationMethod) {
    	super(swath,manifest,geolocationMethod);
    }

    /**
     * 
     */
    @Override
    public int[] readTile(int x, int y, int width, int height,int band) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect = rect.intersection(getImage(band).getBounds());
        int[] tile = new int[height * width];
        if (rect.isEmpty()) {
            return tile;
        }

        if (rect.y != preloadedInterval[0] || rect.y + rect.height != preloadedInterval[1]||preloadedData.length<(rect.width*rect.height-1)) {
            preloadLineTile(rect.y, rect.height,band);
        }else{
        	//logger.debug("using preloaded data");
        }

        int yOffset = getImage(band).getxSize();
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
    public int[] readAndDecimateTile(int x, int y, int width, int height, double scalingFactor, boolean filter, int band) {
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
        double posY = 0.0;
        for (int j = 0; j < outHeight; j++, posY += deltaPixelsY) {
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

        	if (y < 0||y>this.getHeight()||x<0||x>this.getWidth()) {
 	            val= 0;
        	}else{
	 	        TIFF tiff=(TIFF)getImage(band);
	 	        try {
	 	        	BufferedImage bi=null;
 	        		bi=tiff.read(0, rect);
 	        		DataBufferUShort raster=(DataBufferUShort)bi.getRaster().getDataBuffer();
 	        		short[] b=raster.getData();
	 	        	//short[] data=(short[])raster.getDataElements(0, 0, width,height, null);
	 	        	val=b[0];
	 	        } catch (Exception ex) {
	 	            logger.error(ex.getMessage(),ex);
	 	        }
        	}  
        
        return val;
    }
    


	
	public  void preloadLineTile(int y, int length,int band) {
        if (y < 0) {
            return;
        }
        preloadedInterval = new int[]{y, y + length};
        Rectangle rect = new Rectangle(0, y, getImage(band).getxSize(), length);
        
        TIFF tiff=(TIFF)getImage(band);
        rect=tiff.getBounds().intersection(rect);
        
        try {
        	BufferedImage bi=null;
        	try{
        			bi=tiff.read(0, rect);
        	}catch(Exception e){
        		logger.warn("Problem reading image POS x:"+0+ "  y: "+y +"   try to read again");
        		try {
    			    Thread.sleep(100);                 
    			} catch(InterruptedException exx) {
    			    Thread.currentThread().interrupt();
    			}
        		bi=tiff.read(0, rect);
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
	
	public double[] getPixelsize() {
		return super.pixelsize;
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
		@Override
		public String getSensor() {
			return "S1";
		}

	
}
