/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.impl.imgreader;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.GCP;
import org.gdal.gdalconst.gdalconstConstants;
import org.slf4j.LoggerFactory;

import it.geosolutions.imageio.gdalframework.GDALImageReaderSpi;
import it.geosolutions.imageio.gdalframework.GDALUtilities;

/**
 * This is a convenience class that warp a tiff image to easily use in the case
 * of one geotiff per band (like radarsat 2 images)
 * @author thoorfr
 */
public class GeoToolsGDALReader implements IReader {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(GeoToolsGDALReader.class);


    private File imageFile;
    public int xSize = -1;
    public int ySize = -1;
    private Rectangle bounds;
    private int band=1;
    private Dataset data;
	ImageInputStream iis = null;

	
    
    /**
     * 
     * @param imageFile
     * @param band form files with multiple band
     */
	public GeoToolsGDALReader(File imageFile,int band) {
    	this.imageFile=imageFile;
        try {
        	GDALUtilities.loadGDAL();
        	
        	/*int count = gdal.GetDriverCount();
			System.out.println(count + " available Drivers");
			for (int i = 0; i < count; i++) {
				try {
					Driver driver = gdal.GetDriver(i);
					System.out.println(" " + driver.getShortName() + " : "
							+ driver.getLongName());
				} catch (Exception e) {
					System.err.println("Error loading driver " + i);
				}
			}
*/        	
        	GDALImageReaderSpi spi=null;
        	IIORegistry iioRegistry = IIORegistry.getDefaultInstance();
            final Class<ImageReaderSpi> spiClass = ImageReaderSpi.class;
            final Iterator<ImageReaderSpi> iter = iioRegistry.getServiceProviders(spiClass,true);
            while (iter.hasNext()) {
                final ImageReaderSpi provider = (ImageReaderSpi) iter.next();
                if (provider instanceof GDALImageReaderSpi) {
                	spi=(GDALImageReaderSpi)provider;
                	break;
                }
            }
        	this.band=band;
        	data=GDALUtilities.acquireDataSet(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);
        	
    		try{
    			xSize=data.getRasterXSize();
    			ySize=data.getRasterYSize();
    			bounds=new Rectangle(0,0,xSize,ySize);
    		}catch(Exception e){
    			bounds=null;
    			logger.warn("Problem reading size information");
    		}	
        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }	
	}
	/**
	 * 
	 * @param x
	 * @param y
	 * @param offsetx
	 * @param offsety
	 * @return
	 */
	public short[] readShortValues(int x,int y,int offsetx,int offsety){
		int pixels = offsetx * offsety;
		Band b=data.GetRasterBand(band+1);
		//int type=gdal.GetDataTypeSize(b.getDataType());
		int buf_size = pixels;

		short[] dd = new short[buf_size];
		b.ReadRaster(x, y, offsetx, offsety,gdalconstConstants.GDT_UInt16, dd);
		return dd;
	}
	
	public List<GCP> getGCPS(){
		return this.data.GetGCPs();
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
		imageFile=null;
		GDALUtilities.closeDataSet(data);
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
